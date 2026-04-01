#!/bin/bash
NS="smart-campus"
POD="postgres-0"
USER="postgres"
DB="iot"

echo "=== Test ERROR ==="
kubectl exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT * FROM tabla_que_no_existe;" 2>&1 || true
kubectl exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT 1/0;" 2>&1 || true

echo "=== Test WARNING ==="
kubectl exec postgres-0 -n smart-campus -- psql -U postgres -d iot -c "
DO \$\$ BEGIN RAISE WARNING 'Test de warning para dashboard'; END \$\$;
"

echo "=== Test LOG (checkpoint) ==="
kubectl exec $POD -n $NS -- psql -U $USER -d $DB -c "CHECKPOINT;" 2>&1

echo "=== Test DURATION (query lenta >100ms) ==="
kubectl exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT pg_sleep(0.5);" 2>&1

echo "=== Test CONEXIONES (connect+disconnect) ==="
kubectl exec $POD -n $NS -- psql -U $USER -d $DB -c "SELECT 1;" 2>&1

echo "=== Listo. Verifica en Grafana en ~30s ==="