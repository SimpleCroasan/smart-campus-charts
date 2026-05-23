#!/bin/bash
# ============================================================
# test-influxdb-logs.sh — panel "InfluxDB - Logs"
# Filtros (nivel_influxdb): Error=level=error  Warning=level=warn  Info=level=info
# OJO: InfluxDB 2.x suele loguear en logfmt como  lvl=info  (NO level=info).
#      Si el panel queda vacio, ese es el motivo: revisa el prefijo real y
#      ajusta nivel_influxdb -> Error: lvl=error, Warning: lvl=warn, etc.
# ============================================================
NS="smart-campus"
POD="influxdb-0"
TOKEN="8_kiyghJzhqlZENCLnTbc-90hkNfq1U5DS4pRCdMPQdhLY48LW33hsM73nQl1zs_sLVc5IPvJNWsUHZHimZfJw=="
ORG="smart-campus"
HOST="http://localhost:8086"

echo "=== Test 1: ERROR (token invalido -> 401) ==="
kubectl exec $POD -n $NS -- influx query --host $HOST --token TOKEN_INVALIDO --org $ORG \
  'from(bucket:"messages") |> range(start:-1m)' 2>&1 || true

echo ""
echo "=== Test 2: ERROR (consulta a bucket inexistente) ==="
kubectl exec $POD -n $NS -- influx query --host $HOST --token $TOKEN --org $ORG \
  'from(bucket:"bucket_que_no_existe") |> range(start:-1m)' 2>&1 || true

echo ""
echo "=== Test 3: INFO (consulta valida) ==="
kubectl exec $POD -n $NS -- influx bucket list --host $HOST --token $TOKEN --org $ORG 2>&1 || true

echo ""
echo "=== Test 4: INFO (arranque) -> reinicio del pod ==="
kubectl delete pod $POD -n $NS
kubectl wait --for=condition=Ready pod/$POD -n $NS --timeout=120s

echo ""
echo "=== Listo. Verifica en Grafana en ~60s ==="
echo "NOTA: si el filtro Error queda vacio, casi seguro es lvl= vs level=."
