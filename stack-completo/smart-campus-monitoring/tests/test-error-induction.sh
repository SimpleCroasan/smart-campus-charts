#!/bin/bash
# ============================================================
# test-error-induction.sh — Validacion: "el dashboard muestra errores reales"
# Induce un ERROR REAL autogenerado por cada componente silencioso en una
# instancia EFIMERA y desechable (no toca el despliegue real), y vuelca el
# log crudo para confirmar el termino de severidad que usa cada uno.
#
# Los pods llevan label app=<componente> y namespace smart-campus para que
# Alloy los recolecte y caigan en el panel correcto.
#
# VOCABULARIO DE SEVERIDAD descubierto (verificable en la salida):
#   MinIO   -> FATAL            (NO usa ERROR en arranque)
#   EMQX    -> [error] / ERROR:
#   MongoDB -> "s":"E"          (error de servidor; el de cliente es D1)
#   InfluxDB-> lvl=error / panic (caso especial, ver nota)
#
# Postgres y RabbitMQ NO se inducen aqui: ya emiten error real en sus
# tests de log (ERROR: y [error] PLAIN login refused).
# Requiere acceso a internet del cluster para descargar las imagenes.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"

# Sondea el log del pod hasta que exista (maneja descarga lenta de imagen)
dump_logs() {
  local pod=$1 tries=0 out=""
  while [ $tries -lt 30 ]; do
    out=$($KUBECTL logs "$pod" -n $NS 2>/dev/null)
    if [ -n "$out" ]; then echo "$out"; return 0; fi
    sleep 3; tries=$((tries+1))
  done
  echo "(sin logs tras 90s; el pod pudo no llegar a arrancar)"
}

induce() {  # induce <pod> <highlight_regex>
  local pod=$1 rgx=$2
  echo "----- LOG CRUDO de $pod -----"
  dump_logs "$pod"
  echo "----- TERMINO(S) DE SEVERIDAD detectado(s) -----"
  $KUBECTL logs "$pod" -n $NS 2>/dev/null | grep -iE "$rgx" || echo "(sin coincidencias del patron $rgx)"
  $KUBECTL delete pod "$pod" -n $NS --ignore-not-found >/dev/null 2>&1
  echo ""
}

echo "### Hora de inicio (anota para distinguir de operacion normal): $(date '+%F %T') ###"
echo ""

# Limpieza previa por si quedaron pods de una corrida anterior
$KUBECTL delete pod minio-err emqx-err mongodb-err influxdb-err -n $NS --ignore-not-found >/dev/null 2>&1

echo "=== 1/4 MinIO: credenciales invalidas (root user < 3 chars) ==="
$KUBECTL run minio-err --restart=Never -n $NS --labels="app=minio" \
  --image=quay.io/minio/minio \
  --env="MINIO_ROOT_USER=ab" --env="MINIO_ROOT_PASSWORD=12345678" \
  -- server /data >/dev/null 2>&1 || true
induce minio-err "FATAL|ERROR|PANIC"

echo "=== 2/4 EMQX: nombre de nodo invalido ==="
$KUBECTL run emqx-err --restart=Never -n $NS --labels="app=emqx" \
  --image=emqx/emqx:5.8 \
  --env="EMQX_NODE__NAME=nombre_invalido_sin_arroba" --env="EMQX_NODE__COOKIE=test" \
  >/dev/null 2>&1 || true
induce emqx-err "\[error\]|ERROR:|validation"

echo "=== 3/4 MongoDB: dbpath de solo lectura ==="
$KUBECTL run mongodb-err --restart=Never -n $NS --labels="app=mongodb" \
  --image=mongo:latest \
  -- mongod --dbpath /proc >/dev/null 2>&1 || true
induce mongodb-err '"s":"E"|"s":"F"|DBException'

echo "=== 4/4 InfluxDB: fallo de almacenamiento tras init del logger ==="
# Bypass del entrypoint (--command) para que influxd arranque su logger
# antes de fallar al abrir el bolt en una ruta de solo lectura (/proc).
$KUBECTL run influxdb-err --restart=Never -n $NS --labels="app=influxdb" \
  --image=influxdb:2.7.11 --command \
  -- influxd --bolt-path=/proc/influxd.bolt --engine-path=/proc/engine \
  >/dev/null 2>&1 || true
induce influxdb-err "lvl=error|panic|error"

echo "### FIN. Verifica en Grafana (rango: ultima 1 hora) ###"
echo "Panel por componente (filtro Error) y panel 'Errores en infraestructura'."
echo "MinIO debe mostrar FATAL; EMQX [error]; MongoDB \"s\":\"E\"; InfluxDB lvl=error o panic."
