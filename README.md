# Smart Campus UIS — Despliegue en K3s

Este repositorio contiene los Helm charts para desplegar la plataforma **Smart Campus** sobre **K3s** (Kubernetes ligero).

| Chart | Namespace | Contenido |
|---|---|---|
| `stack-completo/smart-campus` | `smart-campus` | Frontend admin, microservicios `admin` y `data`, MySQL, MongoDB, InfluxDB, MinIO, RabbitMQ, EMQX e IngressRoute de Traefik |
| `stack-completo/smart-campus-monitoring` | `monitoring` | Prometheus, Grafana, Loki, Tempo, Alloy y MinIO para almacenamiento de logs, con dashboards y alertas |

El documento tiene tres partes:

1. [Instalar K3s](#parte-1--instalar-k3s) (Linux, Ubuntu/Debian y Windows con WSL2)
2. [Desplegar Smart Campus en K3s](#parte-2--desplegar-smart-campus-en-k3s)
3. [Configuración, operación y solución de problemas](#parte-3--configuración-operación-y-solución-de-problemas)

> Si solo quieres probar la plataforma con Docker Compose, usa la carpeta `despliegue-simple/` (no requiere K3s).

> [!NOTE]
> En toda la guía `kubectl` se ejecuta como `k3s kubectl`.

---

# Parte 1 — Instalar K3s

Compatible con Linux · Ubuntu/Debian · Windows (WSL2).

## Si estás en Windows

K3s solo corre en Linux. En Windows usa WSL2: instala Windows Subsystem for Linux y sigue esta guía desde dentro de tu distro Linux.

Desde PowerShell como administrador:

```powershell
# Habilitar WSL e instalar Ubuntu
wsl --install

# Verificar que estés en WSL2 (debe mostrar VERSION 2)
wsl -l -v

# Si aparece VERSION 1, actualizar:
wsl --set-version Ubuntu 2
```

Una vez dentro de Ubuntu, sigue la guía desde el Paso 1.

## Instalación en Linux

Aplica para cualquier distribución Linux moderna con kernel 3.10+. Las notas marcadas como **Ubuntu/Debian** indican pasos o comandos que difieren en esas distribuciones.

### Paso 1 — Verificar requisitos del sistema

```bash
# Verificar versión del kernel (debe ser 3.10+)
uname -r

# Verificar arquitectura (x86_64, arm64 o armv7)
uname -m

# Verificar RAM disponible
free -h
```

> [!IMPORTANT]
> K3s necesita al menos 512 MB de RAM para arrancar, pero en la práctica consume cerca de 1.6 GB en estado estable. Para **solo K3s** se recomiendan 4 GB. Para correr **el stack completo de Smart Campus + monitoreo** se recomiendan **8 GB o más** (hay varias bases de datos, brokers, dos JVM de Spring Boot, Prometheus, Loki, Tempo y Grafana).

### Paso 2 — Habilitar systemd (solo WSL2 / Ubuntu moderno)

K3s necesita systemd como init system. En la mayoría de distribuciones Linux ya está activo. En WSL2 hay que habilitarlo explícitamente.

**Ubuntu/Debian en WSL2:** systemd no está habilitado por defecto. Edita o crea `/etc/wsl.conf`:

```bash
sudo nano /etc/wsl.conf
```

Añade o verifica que contenga:

```ini
[boot]
systemd=true
```

Guarda el archivo y, desde PowerShell en Windows, reinicia WSL:

```powershell
wsl --shutdown
wsl
```

Verifica que systemd esté corriendo:

```bash
systemctl --no-pager status
# Debe mostrar: State: running
```

### Paso 3 — Configurar iptables en modo legacy

K3s no es compatible con nftables, que usan por defecto las distribuciones modernas. Este paso configura el sistema para usar `iptables-legacy`.

**Ubuntu/Debian:** instala iptables si no está presente:

```bash
sudo apt install -y iptables
```

```bash
# Cambiar a iptables-legacy
sudo update-alternatives --set iptables /usr/sbin/iptables-legacy
sudo update-alternatives --set ip6tables /usr/sbin/ip6tables-legacy

# Verificar
iptables --version
# Debe mostrar: iptables v1.x.x (legacy)
```

> [!IMPORTANT]
> No saltes este paso. Si k3s arranca con nftables activo, el networking entre pods no funcionará correctamente y los servicios no se verán entre sí.

### Paso 4 — Limitar recursos de WSL2 (solo Windows)

Sin límites explícitos, WSL2 puede consumir toda la RAM disponible del sistema. Esto se configura en Windows, no dentro de Linux.

Crea o edita el archivo `.wslconfig` en tu carpeta de usuario de Windows:

```powershell
# Ruta: C:\Users\TuUsuario\.wslconfig
notepad $env:USERPROFILE\.wslconfig
```

Contenido recomendado para correr **el stack completo** (ajusta según la RAM de tu equipo):

```ini
[wsl2]
memory=8GB
processors=4
swap=2GB
```

Reinicia WSL para aplicar los cambios:

```powershell
wsl --shutdown
wsl
```

```bash
# Verificar desde Linux
free -h
# La memoria total debe coincidir con el valor configurado
```

### Paso 5 — Instalar K3s

El instalador oficial detecta automáticamente la arquitectura, descarga el binario correcto y configura k3s como servicio del sistema.

```bash
curl -sfL https://get.k3s.io | sh -
```

Espera 60 segundos y verifica que el servicio esté corriendo:

```bash
sudo systemctl status k3s
# Debe mostrar: Active: active (running)

# Verificar el nodo
sudo k3s kubectl get nodes
# Debe mostrar STATUS: Ready
```

### Paso 6 — Configurar el acceso sin sudo

Por defecto el archivo de configuración del cluster solo puede leerlo root. Este paso es necesario para usar `helm` y `k3s kubectl` como usuario normal.

```bash
# Copiar la configuración al directorio del usuario
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
chmod 600 ~/.kube/config

# Añadir al perfil de la shell para que persista
echo 'export KUBECONFIG=~/.kube/config' >> ~/.bashrc
source ~/.bashrc

# Verificar sin sudo
k3s kubectl get nodes
```

### Paso 7 — Reducir consumo (opcional pero recomendado)

Deshabilitar `metrics-server` y cambiar el backend de flannel reduce el consumo.

```bash
# Editar el servicio de k3s
sudo nano /etc/systemd/system/k3s.service
```

Busca la línea `ExecStart=` y reemplázala por:

```ini
ExecStart=/usr/local/bin/k3s \
    server \
    --disable metrics-server \
    --flannel-backend=host-gw
```

Aplica los cambios:

```bash
sudo systemctl daemon-reload
sudo systemctl restart k3s
```

> [!CAUTION]
> **No deshabilites Traefik** (`--disable traefik`). Los charts usan `IngressRoute` y `Middleware` de Traefik (`traefik.io/v1alpha1`) para enrutar el frontend, `/admin`, `/data` y `/grafana`. Sin Traefik, el `helm install` falla con un error del tipo `no matches for kind "IngressRoute"`.

**Ubuntu/Debian en WSL2:** `--flannel-backend=host-gw` es especialmente importante porque evita el uso de VXLAN, que tiene problemas con el networking de WSL.

### Paso 8 — Instalar Helm

Los despliegues de la Parte 2 usan Helm 3.

```bash
curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Verificar
helm version
```

### Paso 9 — Verificación final

```bash
k3s kubectl get nodes
# NAME       STATUS   ROLES                  AGE   VERSION
# hostname   Ready    control-plane,master   5m    v1.x.x+k3s1

k3s kubectl get pods -A
# Todos deben estar en Running o Completed (incluido traefik)
```

---

# Parte 2 — Desplegar Smart Campus en K3s

## Requisitos previos

- K3s funcionando con el nodo en `Ready` y **Traefik activo** (Parte 1).
- `helm` y `k3s kubectl` funcionando sin sudo (Pasos 6 y 8).
- Conexión a internet para descargar las imágenes de Docker Hub (`semillerocage/*`). Los charts de monitoreo ya vienen compilados dentro del repositorio.

```bash
git clone https://github.com/SimpleCroasan/smart-campus-charts.git
cd smart-campus-charts/stack-completo
```

## Arquitectura

```
                    ┌────────────── Traefik (puerto 80) ──────────────┐
  Navegador ───────►│  /          → frontend-admin  :80               │
                    │  /admin/*   → admin           :8080  (strip)    │
                    │  /data/*    → data            :8080  (strip)    │
                    │  /grafana   → monitoring-grafana :80            │
                    └─────────────────────────────────────────────────┘

  namespace smart-campus                      namespace monitoring
  ───────────────────────                     ────────────────────
  frontend-admin, admin, data                 Prometheus, Grafana
  MySQL, MongoDB, InfluxDB                    Loki (+ MinIO), Tempo
  MinIO, RabbitMQ, EMQX                       Alloy (receptor OTLP :4317)
```

> [!IMPORTANT]
> Los namespaces y el nombre del release de monitoreo **no son configurables**: el chart `smart-campus` tiene el namespace `smart-campus` fijo en sus templates, y los microservicios envían las trazas a `monitoring-alloy-otlp.monitoring.svc.cluster.local`. Por eso el chart de monitoreo debe instalarse como release `monitoring` en el namespace `monitoring`.

Tanto `admin` como `data` escuchan en el puerto **8080**. Traefik recibe todo por el puerto 80 y enruta según el prefijo de la ruta: `/admin` va al microservicio `admin`, `/data` al microservicio `data` (quitando el prefijo antes de reenviar) y `/` al frontend.

## Los dos tipos de despliegue

Los comandos se ejecutan **desde dentro de la carpeta del chart** y usan `.` como ruta del chart.

| Despliegue | Valores que usa | Cuándo usarlo |
|---|---|---|
| **Normal** | Solo `values.yaml` | Levanta todo con la configuración por defecto |
| **Custom** | `values.yaml` + `values-custom.yaml` | Eliges qué componentes levantar y qué imágenes usar (local o nube) |

El despliegue custom se distingue únicamente por añadir `-f values-custom.yaml`; lo que pongas ahí sobrescribe a `values.yaml`.

## Despliegue paso a paso

### Paso 1 — Desplegar la aplicación (`smart-campus`)

```bash
cd smart-campus
```

**Despliegue normal:**

```bash
helm install smart-campus . -n smart-campus --create-namespace
```

**Despliegue custom:**

```bash
helm install smart-campus . -n smart-campus --create-namespace -f values-custom.yaml
```

> [!WARNING]
> La **primera vez** el comando debe llevar `-n smart-campus --create-namespace` juntos. Los templates usan el namespace `smart-campus` pero no lo crean. Si omites alguno de los dos flags, la instalación falla (`namespaces "smart-campus" not found`) y deja un release fallido registrado en `default`, que provoca el error `cannot re-use a name that is still in use` en los siguientes intentos. Si te pasa, mira [Solución de problemas](#error-cannot-re-use-a-name-that-is-still-in-use--namespaces-smart-campus-not-found).
>
> Esto es solo para la primera instalación. Para aplicar cambios después, usa `helm upgrade --install` (ver [Aplicar cambios](#aplicar-cambios-helm-upgrade---install)).

Sigue el arranque (tarda unos 2 a 4 minutos; `admin` y `data` esperan a sus dependencias):

```bash
k3s kubectl get pods -n smart-campus -w
```

Cuando todos estén en `Running` y `READY 1/1`, la aplicación está lista.

### Paso 2 — Desplegar el monitoreo (`smart-campus-monitoring`)

```bash
cd ../smart-campus-monitoring
```

**Despliegue normal:**

```bash
helm install monitoring . -n monitoring --create-namespace
```

**Despliegue custom:**

```bash
helm install monitoring . -n monitoring --create-namespace -f values-custom.yaml
```

> [!WARNING]
> El release **debe llamarse `monitoring`** y el namespace también (`-n monitoring`): los microservicios envían las trazas a `monitoring-alloy-otlp.monitoring.svc.cluster.local` y los datasources dependen de ese nombre. Igual que en `smart-campus`, la **primera vez** lleva `-n monitoring --create-namespace` juntos; para cambios posteriores usa `helm upgrade --install`.

```bash
k3s kubectl get pods -n monitoring -w
```

El flag `observability.lowPriority: true` (en `values-custom.yaml`) asigna una PriorityClass baja al stack de observabilidad para que sea lo primero que se desaloje si el nodo se queda sin recursos. Ponlo en `false` para pruebas de estrés.

### Paso 3 — Acceder

Traefik escucha en el puerto 80 del nodo. Reemplaza `<IP-DEL-NODO>` por la columna `INTERNAL-IP` de:

```bash
k3s kubectl get nodes -o wide
```

| Servicio | URL |
|---|---|
| Frontend admin | `http://<IP-DEL-NODO>/` |
| API admin | `http://<IP-DEL-NODO>/admin/...` |
| API data | `http://<IP-DEL-NODO>/data/...` |
| Grafana | `http://<IP-DEL-NODO>/grafana` (usuario `admin`, contraseña definida en `grafana.adminPassword`) |

---

# Parte 3 — Configuración, operación y solución de problemas

## Configurar con `values-custom.yaml`

Para el despliegue custom, `values-custom.yaml` es el archivo que editas. Hay uno en cada chart (`smart-campus/` y `smart-campus-monitoring/`).

### Habilitar y deshabilitar componentes

En `smart-campus/values-custom.yaml` cada componente tiene un flag `enabled`. Ponlo en `false` para no desplegarlo:

```yaml
mysql:
  enabled: true
mongo:
  enabled: true
influxdb:
  enabled: false     # no se despliega
minio:
  enabled: true
rabbitmq:
  enabled: false     # no se despliega
emqx:
  enabled: true

dataService:
  enabled: true
adminService:
  enabled: false     # no se despliega
frontendAdmin:
  enabled: true
```

Para usarlo:

1. Cambia el `enabled` de lo que quieras encender o apagar.
2. Aplica el cambio con `helm upgrade --install` (ver [Aplicar cambios](#aplicar-cambios-helm-upgrade---install)).
3. Verifica con `k3s kubectl get pods -n smart-campus`.

Qué debes saber:

- Los initContainers de `admin` y `data` solo esperan a los componentes habilitados, así que apagar una dependencia no los bloquea.
- Un microservicio que necesite una base de datos que apagaste fallará al conectar. Apaga solo lo que no uses.
- Al apagar un componente, Helm elimina su Deployment/StatefulSet y su Service. Sus volúmenes (PVC) y datos se conservan, y si lo vuelves a activar retoma sus datos.
- Es útil en equipos con poca RAM, por ejemplo apagando `influxdb`, `rabbitmq` y `emqx` si no estás probando ingesta de datos.

### Elegir imagen local o de la nube

Los tres componentes propios (`dataService`, `adminService` y `frontendAdmin`) pueden usar imágenes de Docker Hub o imágenes locales. La elección es **por servicio** con `imageSource`:

| `imageSource` | Origen de la imagen | `imagePullPolicy` |
|---|---|---|
| `remote` | Docker Hub (`semillerocage/...`) | `IfNotPresent` |
| `local` | containerd del nodo (imagen que tú construiste) | `Never` |

Indica siempre `imageSource`, `image` y `tag` **juntos**. Se puede mezclar. Ejemplo: `admin` local y `data` remoto:

```yaml
adminService:
  enabled: true
  imageSource: local
  image: admin-service
  tag: latest

dataService:
  enabled: true
  imageSource: remote
  image: semillerocage/data-service
  tag: "1.5"

frontendAdmin:
  enabled: true
  imageSource: remote
  image: semillerocage/front-end-admin
  tag: "1.1"
```

Notas:

- Con `local`, la imagen debe estar importada en K3s **antes** de aplicar. Si no, el pod queda en `ErrImageNeverPull`.
- `frontendAdmin` no tiene código en este repositorio, así que siempre usa la imagen remota.
- Si reconstruyes una imagen local con el mismo tag (`latest`), reimporta y reinicia el pod: `k3s kubectl rollout restart -n smart-campus deploy/admin` (o `deploy/data`).
- Para volver a la nube, pon `imageSource: remote` con su `image` y `tag` y aplica el cambio.

## Aplicar cambios (`helm upgrade --install`)

`helm install` falla si el release ya existe y `helm upgrade` falla si no existe. `helm upgrade --install` sirve para ambos casos: instala si es la primera vez y actualiza (redespliega) si ya existe. Es el comando para aplicar cualquier cambio de valores o de templates.

Desde la carpeta del chart:

```bash
# smart-campus (despliegue normal)
helm upgrade --install smart-campus . -n smart-campus 

# smart-campus (despliegue custom)
helm upgrade --install smart-campus . -n smart-campus  -f values-custom.yaml

# monitoring (normal / custom)
helm upgrade --install monitoring . -n monitoring 
helm upgrade --install monitoring . -n monitoring  -f values-custom.yaml
```

> [!IMPORTANT]
> Helm no recuerda el `-f` anterior. Usa siempre el mismo comando en cada upgrade: si desplegaste con `-f values-custom.yaml` y haces un upgrade sin él, vuelves a los valores de `values.yaml`.

> [!NOTE]
> Helm solo reinicia un pod si cambia su template (imagen, tag, variables, etc.). Si reconstruiste una imagen con el **mismo tag**, el upgrade no detecta cambios: reinicia el pod con `k3s kubectl rollout restart`.

## Historial y rollback

Cada `helm upgrade` crea una nueva **revisión**. Si algo se rompe, puedes volver a una anterior.

```bash
# 1. Ver el historial de revisiones
helm history smart-campus -n smart-campus
# REVISION  STATUS      DESCRIPTION
# 1         superseded  Install complete
# 2         superseded  Upgrade complete
# 3         deployed    Upgrade complete

# 2. Volver a la revisión anterior
helm rollback smart-campus -n smart-campus

# 3. O a una revisión concreta (por ejemplo la 2)
helm rollback smart-campus 2 -n smart-campus

# 4. Verificar
k3s kubectl get pods -n smart-campus
```

Lo mismo aplica para el monitoreo (`helm history monitoring -n monitoring`, `helm rollback monitoring -n monitoring`).

Qué debes saber:

- El rollback **crea una revisión nueva** (la 4 en el ejemplo, con el contenido de la 2); no borra el historial.
- Revierte los manifiestos (imágenes, configuración, `enabled`, etc.), **pero no los datos**: los PVC y su contenido siguen igual.

## Dónde aparece el release de Helm

El release de Helm **no queda en `default`**: queda en el namespace que definiste en el `helm install` con `-n`.

- `helm install smart-campus . -n smart-campus --create-namespace` → release `smart-campus` en el namespace `smart-campus`.
- `helm install monitoring . -n monitoring --create-namespace` → release `monitoring` en el namespace `monitoring`.

Solo quedaría en `default` si omites `-n` (por ejemplo, en una instalación fallida), y por eso hay que evitarlo.

```bash
helm list -n smart-campus  # releases de ese namespace
helm list -A               # todos los namespaces (úsalo si no ves tu release)
```

> [!IMPORTANT]
> Usa siempre el mismo `-n` que usaste al instalar en `upgrade`, `rollback`, `history` y `uninstall`. Si usas otro, Helm no encuentra el release o crea uno nuevo que choca con los recursos ya existentes.

## Operación diaria

```bash
# Ver releases instalados
helm list -A

# Ver pods
k3s kubectl get pods -A

# Logs de un microservicio
k3s kubectl logs -n smart-campus deploy/admin -f
k3s kubectl logs -n smart-campus deploy/data -f

# Reiniciar un microservicio
k3s kubectl rollout restart -n smart-campus deploy/admin
```

## Encender y apagar el cluster

```bash
# Apagar (libera toda la RAM; los volúmenes se conservan)
sudo systemctl stop k3s

# Encender (los pods vuelven solos)
sudo systemctl start k3s
```

## Desinstalar

```bash
helm uninstall monitoring  -n monitoring
helm uninstall smart-campus -n smart-campus
```

Si el release quedó registrado en `default` (por una instalación fallida sin `-n`), se desinstala **sin** `-n`:

```bash
helm uninstall smart-campus
```

> [!WARNING]
> `helm uninstall` **no borra los volúmenes** de las bases de datos. Para eliminar también los datos:
>
> ```bash
> k3s kubectl delete pvc --all -n smart-campus
> k3s kubectl delete pvc --all -n monitoring
> k3s kubectl delete namespace smart-campus monitoring
> ```

## Desinstalar K3s

```bash
/usr/local/bin/k3s-uninstall.sh
```

> [!WARNING]
> Este comando elimina también todos los datos del cluster, incluyendo volúmenes y configuraciones. No hay vuelta atrás.

## Solución de problemas de K3s

### El nodo no aparece como Ready

K3s tarda entre 30 y 90 segundos en el primer arranque. Si después de ese tiempo el nodo sigue en `NotReady`:

```bash
sudo journalctl -u k3s -n 100 --no-pager
k3s kubectl describe node
```

### kubectl: "the server is currently unable to handle the request"

El API server todavía está inicializando. Espera 60 segundos y vuelve a intentar.

## Solución de problemas del despliegue

### Error `cannot re-use a name that is still in use` / `namespaces "smart-campus" not found`

Se debe a instalar sin `-n smart-campus --create-namespace` juntos. El primer intento falla porque el namespace no existe y deja un release fallido en `default`; los siguientes chocan con ese nombre. Para solucionarlo:

```bash
# Ver releases de todos los namespaces, incluidos los fallidos
helm list -A -a

# Borrar el release fallido (está en default, por eso sin -n)
helm uninstall smart-campus

# Instalar de nuevo, con ambos flags
helm install smart-campus . -n smart-campus --create-namespace
```

Con el monitoreo es igual: `helm uninstall monitoring` y luego `helm install monitoring . -n monitoring --create-namespace`.

### `no matches for kind "IngressRoute"` / `"Middleware"`

Traefik no está instalado (K3s se arrancó con `--disable traefik`). Quita ese flag de `/etc/systemd/system/k3s.service`, haz `sudo systemctl daemon-reload` y `sudo systemctl restart k3s`. Verifica con `k3s kubectl get pods -n kube-system | grep traefik`.

### Pods en `ImagePullBackOff`

Revisa que el tag exista en Docker Hub: `k3s kubectl describe pod <pod> -n smart-campus`.

### Pods en `ErrImageNeverPull`

Tienes `imageSource: local` pero la imagen no está importada en el containerd de K3s. Impórtala, o cambia a `imageSource: remote`.

### Pods en `Pending`

```bash
k3s kubectl describe pod <pod> -n <namespace>
k3s kubectl get pvc -A
```

Casi siempre es falta de RAM/CPU del nodo. Sube el límite en `.wslconfig` (Paso 4 de la Parte 1) o apaga componentes con `enabled: false`.

### `admin` o `data` se quedan en `Init:0/N`

Los initContainers esperan a que cada dependencia esté disponible. Revisa cuál no está arrancando:

```bash
k3s kubectl get pods -n smart-campus
```

### `CrashLoopBackOff` en `admin` o `data`

```bash
k3s kubectl logs -n smart-campus deploy/admin --previous
```

Suele ser una credencial incorrecta o una base de datos que aún no terminó de inicializar. Si cambiaste contraseñas **después** del primer despliegue, los volúmenes conservan las anteriores: borra los PVC del componente afectado o revierte el valor.

### Grafana no muestra trazas ni logs

El release del chart de monitoreo debe llamarse `monitoring` y estar en el namespace `monitoring`; con otro nombre los servicios y datasources no resuelven.
