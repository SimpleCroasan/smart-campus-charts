#!/bin/bash
# ============================================================
# test-minio-logs.sh — panel "MinIO - Logs"
# Filtros (nivel_minio): Error=ERROR  Warning=WARN  Info=INFO
# MinIO es silencioso por defecto: solo loguea arranque (INFO) y errores
# reales. Un filtro Error vacio en condiciones normales = servidor sano
# (misma logica que la limitacion documentada de MongoDB).
# Requiere acceso a internet del cluster para descargar la imagen mc.
# Credenciales por MC_HOST_* (no necesita shell dentro de la imagen mc).
# ============================================================
NS="smart-campus"
USER="admin"
PASS="password"

echo "=== Test 1: INFO (arranque) -> reinicio del pod ==="
kubectl delete pod minio-0 -n $NS
kubectl wait --for=condition=Ready pod/minio-0 -n $NS --timeout=120s

echo ""
echo "=== Test 2: ERROR (credenciales invalidas -> AccessDenied) ==="
kubectl run mc-error --rm -i --restart=Never -n $NS \
  --image=quay.io/minio/mc \
  --env="MC_HOST_bad=http://$USER:clave_mala@minio:9000" \
  -- ls bad 2>&1 || true

echo ""
echo "=== Test 3: INFO/operaciones validas (crear bucket) ==="
kubectl run mc-ok --rm -i --restart=Never -n $NS \
  --image=quay.io/minio/mc \
  --env="MC_HOST_ok=http://$USER:$PASS@minio:9000" \
  -- mb ok/test-dashboard 2>&1 || true

echo ""
echo "=== Limpieza del bucket de prueba ==="
kubectl run mc-clean --rm -i --restart=Never -n $NS \
  --image=quay.io/minio/mc \
  --env="MC_HOST_ok=http://$USER:$PASS@minio:9000" \
  -- rb --force ok/test-dashboard 2>&1 || true

echo ""
echo "=== Listo. Verifica en Grafana en ~60s ==="
echo "NOTA: MinIO no loguea cada peticion S3 por defecto; el arranque produce"
echo "lineas INFO. Para trazar peticiones puede habilitarse 'mc admin trace'."
