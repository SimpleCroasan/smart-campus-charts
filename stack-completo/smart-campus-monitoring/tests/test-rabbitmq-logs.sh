#!/bin/bash
# ============================================================
# test-rabbitmq-logs.sh — panel "RabbitMQ - Logs"
# Filtros (nivel_rabbitmq): Error=error  Warning=warning  Info=info
# Estrategia: PROVOCAR eventos reales del broker (sin lineas sinteticas).
# HALLAZGOS:
#  - El rechazo de login solo se registra ante conexion AMQP real (5672).
#  - La alarma de memoria debe CRUZARSE para emitir [warning]; cambiar
#    solo el umbral se registra como [info].
#  Requiere internet del cluster (imagen python:3.12-slim).
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
POD="rabbitmq-0"

echo "=== Test 1: ERROR real (login AMQP rechazado) ==="
$KUBECTL run amqp-bad --rm -i --restart=Never -n $NS --image=python:3.12-slim -- \
  sh -c "pip install -q pika 2>/dev/null; python -c \"import pika; pika.BlockingConnection(pika.ConnectionParameters(host='rabbitmq', credentials=pika.PlainCredentials('admin','clave_incorrecta')))\"" 2>&1 || true

echo ""
echo "=== Test 2: WARNING real (alarma de memoria disparada) ==="
# Umbral absoluto por debajo del uso actual -> [warning] alarm set.
# Aplica back-pressure a publicadores hasta restaurar (~8s).
$KUBECTL exec $POD -n $NS -- rabbitmqctl set_vm_memory_high_watermark absolute "50MB" 2>&1 || true
echo "Esperando a que el monitor de memoria dispare la alarma..."
sleep 8
$KUBECTL exec $POD -n $NS -- rabbitmqctl set_vm_memory_high_watermark 0.4 2>&1 || true

echo ""
echo "=== Verificacion en el log del broker ==="
sleep 2
$KUBECTL logs $POD -n $NS --tail=60 | grep -iE "refused|resource limit alarm" || echo "(sin coincidencias)"

echo ""
echo "=== Listo. Verifica en Grafana (rango: ultima 1 hora) ==="
echo "INFO: las conexiones del microservicio data cubren el nivel info de forma natural."
