#!/bin/bash
# ============================================================
# test-emqx-logs.sh — panel "EMQX (MQTT) - Logs"
# Filtros (nivel_emqx): Error=error Warning=warning Info=info (formato [nivel])
# Estrategia: PROVOCAR eventos reales (sin lineas sinteticas).
# Service EMQX es headless (ClusterIP None); conectar via -h emqx funciona.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
POD="emqx-0"

echo "=== Test 1: WARNING real (takeover de client-id) ==="
# Dos sesiones persistentes con el MISMO client-id. La segunda conexion
# expulsa a la primera -> EMQX registra el desalojo (kicked/takeover).
$KUBECTL run mqtt-takeover --rm -i --restart=Never -n $NS --image=eclipse-mosquitto -- \
  sh -c '
    (mosquitto_sub -h emqx -p 1883 -i CLIENTE_DUP -c -t test/x -C 2 >/dev/null 2>&1 &)
    sleep 2
    mosquitto_sub -h emqx -p 1883 -i CLIENTE_DUP -c -t test/x -C 1 -W 3 >/dev/null 2>&1
    echo "takeover ejecutado"
  ' 2>&1 || true

echo ""
echo "=== Test 2: INFO + CONEXIONES (reinicio del pod) ==="
$KUBECTL delete pod $POD -n $NS --ignore-not-found
echo "Esperando a que el StatefulSet recree el pod..."
sleep 5
$KUBECTL wait --for=condition=Ready pod/$POD -n $NS --timeout=120s

echo ""
echo "=== Verificacion en el log de EMQX ==="
sleep 2
$KUBECTL logs $POD -n $NS --tail=80 | grep -iE "\[error\]|\[warning\]|takeover|kicked|discard|conflict|duplicate" || echo "(sin error/warning)"

echo ""
echo "=== Listo. Verifica en Grafana (rango: ultima 1 hora) ==="
