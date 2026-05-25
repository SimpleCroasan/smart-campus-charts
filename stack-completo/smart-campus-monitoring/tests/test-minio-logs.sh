#!/bin/bash
# ============================================================
# test-minio-logs.sh — panel "MinIO - Logs"
# Filtros (nivel_minio): Error=ERROR  Warning=WARN  Info=INFO
# MinIO es silencioso por defecto: solo loguea arranque (INFO) y errores
# reales. Un filtro Error vacio en condiciones normales = servidor sano
# (misma logica que MongoDB, RabbitMQ e InfluxDB).
# Los pods efimeros 'mc' requieren acceso a internet del cluster.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then
  KUBECTL="kubectl"
else
  KUBECTL="k3s kubectl"
fi

NS="smart-campus"
USER="admin"
PASS="password"

echo "=== Test 1: INFO (arranque) -> reinicio del pod ==="
$KUBECTL delete pod minio-0 -n $NS --ignore-not-found
echo "Esperando a que el StatefulSet recree el pod..."
sleep 5
$KUBECTL wait --for=condition=Ready pod/minio-0 -n $NS --timeout=120s

echo ""
echo "=== Test 2: ERROR (credenciales invalidas -> AccessDenied) ==="
$KUBECTL run mc-error --rm -i --restart=Never -n $NS \
  --image=quay.io/minio/mc \
  --env="MC_HOST_bad=http://$USER:clave_mala@minio:9000" \
  -- ls bad 2>&1 || true

echo ""
echo "=== Test 3: INFO/operaciones validas (crear bucket) ==="
$KUBECTL run mc-ok --rm -i --restart=Never -n $NS \
  --image=quay.io/minio/mc \
  --env="MC_HOST_ok=http://$USER:$PASS@minio:9000" \
  -- mb ok/test-dashboard 2>&1 || true

echo ""
echo "=== Limpieza del bucket de prueba ==="
$KUBECTL run mc-clean --rm -i --restart=Never -n $NS \
  --image=quay.io/minio/mc \
  --env="MC_HOST_ok=http://$USER:$PASS@minio:9000" \
  -- rb --force ok/test-dashboard 2>&1 || true

echo ""
echo "=== Listo. Verifica en Grafana (rango: ultima 1 hora) ==="
echo "NOTA: MinIO no loguea cada peticion S3 por defecto; el arranque produce"
echo "lineas INFO. El filtro Error puede quedar vacio aun con peticiones"
echo "rechazadas (servidor sano), igual que MongoDB/InfluxDB."
