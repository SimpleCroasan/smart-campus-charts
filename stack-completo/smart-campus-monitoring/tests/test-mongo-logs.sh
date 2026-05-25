#!/bin/bash
# ============================================================
# test-mongo-logs.sh — panel "MongoDB - Logs relevantes"
# Filtros (nivel_mongodb): Error="s":"E"|"s":"F"  Warning="s":"W"
#   Queries lentas=durationMillis  Conexiones=Connection accepted/ended
#   Info=startup/shutdown
# HALLAZGO: MongoDB loguea los errores de operacion de cliente como
#   "s":"D1" (Debug), no como "s":"E". El filtro Error captura errores
#   internos del servidor (corrupcion, disco), raros pero criticos.
#   Un filtro Error vacio en operacion normal = servidor sano.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
POD="mongodb-0"
AUTH="-u root -p password --authenticationDatabase admin"

echo "=== Test 1: ERROR (\"s\":\"E\") ==="
$KUBECTL exec $POD -n $NS -- mongosh --host localhost --port 27017 \
  -u "fake_user" -p "fake_pass" --authenticationDatabase admin \
  --eval "db.runCommand({ping:1})" 2>&1 || true
$KUBECTL exec $POD -n $NS -- mongosh $AUTH --eval '
  try { db.adminCommand({invalidCommand: 1}); } catch(e) { print("Error: " + e.message); }
' 2>&1 || true

echo ""
echo "=== Test 2: WARNING (\"s\":\"W\") ==="
$KUBECTL exec $POD -n $NS -- mongosh $AUTH --eval '
  try { db.getSiblingDB("admin").auth("intruso", "clave_mal"); } catch(e) {}
' 2>&1 || true

echo ""
echo "=== Test 3: QUERIES LENTAS (durationMillis) ==="
$KUBECTL exec $POD -n $NS -- mongosh $AUTH iot --eval '
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
' 2>&1 || true

echo ""
echo "=== Test 4: CONEXIONES (connection accepted/ended) ==="
$KUBECTL exec $POD -n $NS -- mongosh $AUTH iot --eval "db.runCommand({ping:1})" 2>&1 || true

echo ""
echo "=== Test 5: INFO (startup/shutdown) ==="
echo "Reiniciando pod mongodb-0 (el StatefulSet lo recrea automaticamente)..."
$KUBECTL delete pod $POD -n $NS --ignore-not-found
echo "Esperando a que el StatefulSet recree el pod..."
sleep 5
$KUBECTL wait --for=condition=Ready pod/$POD -n $NS --timeout=120s

echo ""
echo "=== Tests completados. Verifica en Grafana (rango: ultima 1 hora) ==="
echo "NOTA: si el filtro Error no muestra resultados, es esperado."
echo "MongoDB loguea errores de operacion como \"s\":\"D1\" (Debug), no \"s\":\"E\"."
echo "El filtro Error captura errores internos del servidor (raros pero criticos)."
