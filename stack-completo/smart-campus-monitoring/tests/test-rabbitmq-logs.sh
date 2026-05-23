#!/bin/bash
# ============================================================
# test-rabbitmq-logs.sh — panel "RabbitMQ - Logs"
# Filtros (nivel_rabbitmq): Error=error  Warning=warning  Info=info
# RabbitMQ escribe [error]/[warning]/[info]; el panel filtra con |~
# ============================================================
NS="smart-campus"
POD="rabbitmq-0"

echo "=== Test 1: ERROR/WARNING (autenticacion rechazada) ==="
# Login fallido -> RabbitMQ registra el rechazo (PLAIN login refused)
kubectl exec $POD -n $NS -- rabbitmqctl authenticate_user usuario_falso clave_mala 2>&1 || true

echo ""
echo "=== Test 2: WARNING (alarma de memoria) ==="
# NOTA: el watermark bajo dispara [warning] real, pero aplica back-pressure
# a los publicadores ~2s hasta que se restaura. Se auto-resuelve. Si prefieres
# cero efecto sobre el microservicio data, comenta este bloque: la linea
# logger:warning de abajo ya cubre el nivel.
kubectl exec $POD -n $NS -- rabbitmqctl set_vm_memory_high_watermark 0.01 2>&1 || true
sleep 2
kubectl exec $POD -n $NS -- rabbitmqctl set_vm_memory_high_watermark 0.4 2>&1 || true

echo ""
echo "=== Cobertura garantizada de nivel (logger directo) ==="
kubectl exec $POD -n $NS -- rabbitmqctl eval 'logger:error("Test ERROR para dashboard").' 2>&1 || true
kubectl exec $POD -n $NS -- rabbitmqctl eval 'logger:warning("Test WARNING para dashboard").' 2>&1 || true
kubectl exec $POD -n $NS -- rabbitmqctl eval 'logger:info("Test INFO para dashboard").' 2>&1 || true

echo ""
echo "=== Listo. Verifica en Grafana en ~30s ==="
echo "NOTA: las conexiones (nivel info) las genera el microservicio data al"
echo "conectarse al broker AMQP, sin accion manual."
