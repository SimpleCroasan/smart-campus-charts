#!/bin/bash
# ============================================================
# test-alert-memoria-heap.sh - presion de memoria sobre data
# Expr: heap_used / heap_max > 0.03
# for: 5m, severity: critical
#
# Estrategia: bombardeo masivo con peticiones que crean objetos en
# heap. Combinamos /camera/list (Mongo findAll + serializacion JSON)
# y /mongo/deviceId/{deviceId} (otra serializacion). 100 paralelos
# durante 8 minutos para que el heap_used suba >3% y se sostenga.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
BASE_URL="${BASE_URL:-http://localhost}"
PROM="http://monitoring-prometheus-server.monitoring.svc.cluster.local"
ALERT="MemoriaHeapCritica"
TS=$(date +%Y%m%d-%H%M%S)
LOG="/tmp/test-alert-memoria-heap-${TS}.log"
PARALELOS=100
DURACION=480   # 8 minutos

say() { echo "$@" | tee -a "$LOG"; }

heap_actual() {
  $KUBECTL delete pod prom-debug -n monitoring --ignore-not-found >/dev/null 2>&1
  $KUBECTL run prom-debug --rm -i --restart=Never -n monitoring \
    --image=curlimages/curl --quiet -- \
    -s "${PROM}/api/v1/query" \
    --data-urlencode 'query=sum by (app) (jvm_memory_used_bytes{area="heap", app=~"admin|data"}) / sum by (app) (clamp_min(jvm_memory_max_bytes{area="heap", app=~"admin|data"}, 1))' 2>/dev/null \
    | python3 -c "
import sys, json
data = json.load(sys.stdin)
for r in data.get('data', {}).get('result', []):
    app = r['metric'].get('app', '?')
    value = float(r['value'][1]) * 100
    flag = ' <-- supera umbral 3%' if value > 3.0 else ''
    print(f'    {app}: {value:.2f}%{flag}')
" 2>/dev/null
}

estado_alerta() {
  $KUBECTL delete pod prom-debug -n monitoring --ignore-not-found >/dev/null 2>&1
  $KUBECTL run prom-debug --rm -i --restart=Never -n monitoring \
    --image=curlimages/curl --quiet -- \
    -s "${PROM}/api/v1/alerts" 2>/dev/null \
    | python3 -c "
import sys, json
data = json.load(sys.stdin)
found = False
for a in data.get('data', {}).get('alerts', []):
    if a.get('labels', {}).get('alertname') == '${ALERT}':
        found = True
        labels = a.get('labels', {})
        print(f\"    {a.get('state','?'):8s} | app={labels.get('app','?')} | activeAt={a.get('activeAt','?')}\")
if not found:
    print('    inactive')
" 2>/dev/null
}

say "=========================================================="
say "VALIDACION ALERTA: $ALERT"
say "Hora inicio: $(date '+%F %T')"
say "Estrategia: presion de memoria con $PARALELOS paralelos x $DURACION s"
say "Log: $LOG"
say "=========================================================="

say ""
say ">>> FASE 1: estado inicial"
say "  heap actual:"
heap_actual
say "  alerta:"
estado_alerta

say ""
say ">>> FASE 2: presion de memoria con $PARALELOS paralelos"
say "  Endpoints atacados:"
say "    - /data/camera/list (findAll Mongo + serializacion JSON)"
say "    - /data/mongo/deviceId/test (otra serializacion)"
say "  Inicio: $(date '+%T')"

FIN=$(( $(date +%s) + DURACION ))

# Lanzar PARALELOS procesos en background alternando endpoints
for i in $(seq 1 $PARALELOS); do
  (
    while [ $(date +%s) -lt $FIN ]; do
      curl -s -o /dev/null --max-time 5 "${BASE_URL}/data/camera/list"
      curl -s -o /dev/null --max-time 5 "${BASE_URL}/data/mongo/deviceId/test"
    done
  ) &
done

# Monitorear cada minuto
for min in 1 2 3 4 5 6 7 8; do
  sleep 60
  say ""
  say "T+${min}m:"
  say "  heap:"
  heap_actual
  say "  alerta:"
  estado_alerta
done

# Esperar a que terminen los procesos
wait
say ""
say "Fin carga: $(date '+%T')"

say ""
say ">>> FASE 3: estado final"
ESTADO=$(estado_alerta)
echo "$ESTADO" | tee -a "$LOG"

if echo "$ESTADO" | grep -q firing; then
  say ""
  say "RESULTADO: ALERTA DISPARADA - heap supero 3% sostenido"
elif echo "$ESTADO" | grep -q pending; then
  say ""
  say "RESULTADO: PENDING - heap supero el umbral pero falta tiempo"
else
  say ""
  say "RESULTADO: NO DISPARO - heap se mantuvo bajo el umbral"
fi

say ""
say ">>> FASE 4: 90s para captura en Grafana"
sleep 90

say ""
say "=========================================================="
say "FIN. Log: $LOG"
say "=========================================================="
