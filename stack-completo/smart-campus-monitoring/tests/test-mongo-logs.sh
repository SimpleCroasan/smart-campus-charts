#!/bin/bash
# ============================================================
# test-mongo-dashboard.sh
# Tests para el panel MongoDB del dashboard de Infraestructura
# ============================================================

NS="smart-campus"
POD="mongodb-0"
AUTH="-u root -p password --authenticationDatabase admin"

echo "=== Test 1: ERROR (\"s\":\"E\") ==="
kubectl exec $POD -n $NS -- mongosh --host localhost --port 27017 \
  -u "fake_user" -p "fake_pass" --authenticationDatabase admin \
  --eval "db.runCommand({ping:1})" 2>&1 || true

kubectl exec $POD -n $NS -- mongosh $AUTH --eval '
  try { db.adminCommand({invalidCommand: 1}); } catch(e) { print("Error: " + e.message); }
' 2>&1 || true

echo ""
echo "=== Test 2: WARNING (\"s\":\"W\") ==="
kubectl exec $POD -n $NS -- mongosh $AUTH --eval '
  try { db.getSiblingDB("admin").auth("intruso", "clave_mal"); } catch(e) {}
' 2>&1 || true

echo ""
echo "=== Test 3: QUERIES LENTAS (durationMillis) ==="
kubectl exec $POD -n $NS -- mongosh $AUTH iot --eval '
  db.setProfilingLevel(1, {slowms: 100});
  var bulk = [];
  for (var i = 0; i < 100000; i++) {
    bulk.push({x: i, data: "padding_" + Math.random().toString(36).repeat(10)});
  }
  db.test_slow.insertMany(bulk);
  db.test_slow.find({data: /padding_abc/}).toArray();
  db.test_slow.aggregate([
    {$group: {_id: "$data", count: {$sum: 1}}},
    {$sort: {count: -1}}
  ]).toArray();
  db.test_slow.drop();
  db.setProfilingLevel(0);
' 2>&1

echo ""
echo "=== Test 4: CONEXIONES (connection accepted/ended) ==="
kubectl exec $POD -n $NS -- mongosh $AUTH iot --eval "db.runCommand({ping:1})" 2>&1

echo ""
echo "=== Test 5: INFO (startup/shutdown) ==="
echo "Reiniciando pod mongodb-0 (el StatefulSet lo recrea automaticamente)..."
kubectl delete pod $POD -n $NS
echo "Esperando a que el pod vuelva a estar Ready..."
kubectl wait --for=condition=Ready pod/$POD -n $NS --timeout=120s

echo ""
echo "=== Tests completados. Verifica en Grafana en ~60s ==="
echo ""
echo "NOTA: Si el filtro Error no muestra resultados, es esperado."
echo "MongoDB loguea errores de operacion como \"s\":\"D1\" (Debug),"
echo "no como \"s\":\"E\". El filtro Error captura errores internos"
echo "del servidor (corrupcion, disco lleno), que son raros pero criticos."
