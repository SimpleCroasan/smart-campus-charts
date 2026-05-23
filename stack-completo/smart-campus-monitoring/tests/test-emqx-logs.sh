#!/bin/bash
# ============================================================
# test-emqx-logs.sh — panel "EMQX (MQTT) - Logs"
# Filtros (nivel_emqx): Error=level=error  Warning=level=warning  Info=level=info
# EMQX 5.x escribe en logfmt con level=...; el panel filtra con |~
# ============================================================
NS="smart-campus"
POD="emqx-0"

echo "=== Test 1: INFO (cambio de nivel de log) ==="
kubectl exec $POD -n $NS -- emqx ctl log set-level info 2>&1 || true

echo ""
echo "=== Cobertura garantizada de nivel (logger directo) ==="
# Si 'emqx eval' no existe en tu build, estas lineas fallaran sin efecto;
# el reinicio de abajo y el trafico del microservicio data cubren los niveles.
kubectl exec $POD -n $NS -- emqx eval 'logger:error("Test ERROR para dashboard").' 2>&1 || true
kubectl exec $POD -n $NS -- emqx eval 'logger:warning("Test WARNING para dashboard").' 2>&1 || true
kubectl exec $POD -n $NS -- emqx eval 'logger:info("Test INFO para dashboard").' 2>&1 || true

echo ""
echo "=== Test 2: INFO + CONEXIONES (reinicio del pod) ==="
echo "Reiniciando emqx-0 (el StatefulSet lo recrea)..."
kubectl delete pod $POD -n $NS
kubectl wait --for=condition=Ready pod/$POD -n $NS --timeout=120s

echo ""
echo "=== Listo. Verifica en Grafana en ~60s ==="
echo "NOTA: el arranque genera level=info y, al reconectarse, el microservicio"
echo "data produce logs de conexion MQTT. Si las lineas de prueba no coinciden,"
echo "confirma el formato real de EMQX 5.8 (level= vs [error]) y ajusta nivel_emqx."
