#!/bin/bash
# ============================================================
# test-postgres-logs.sh — panel "PostgreSQL - Logs"
# Filtros (nivel_postgres): Error=ERROR: Warning=WARNING: Info=LOG:
#                           Queries lentas=duration: Conexiones=connection
# PostgreSQL emite errores de forma convencional (ERROR:, PANIC:).
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
POD="postgres-0"
USER="postgres"
DB="iot"

echo "=== Test ERROR ==="
$KUBECTL exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT * FROM tabla_que_no_existe;" 2>&1 || true
$KUBECTL exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT 1/0;" 2>&1 || true

echo "=== Test WARNING ==="
$KUBECTL exec $POD -n $NS -- psql -U $USER -d $DB -c "DO \$\$ BEGIN RAISE WARNING 'Test de warning para dashboard'; END \$\$;" 2>&1 || true

echo "=== Test LOG (checkpoint) ==="
$KUBECTL exec $POD -n $NS -- psql -U $USER -d $DB -c "CHECKPOINT;" 2>&1 || true

echo "=== Test DURATION (query lenta >100ms) ==="
$KUBECTL exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT pg_sleep(0.5);" 2>&1 || true

echo "=== Test CONEXIONES (connect+disconnect) ==="
$KUBECTL exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT 1;" 2>&1 || true

echo "=== Listo. Verifica en Grafana (rango: ultima 1 hora) ==="
