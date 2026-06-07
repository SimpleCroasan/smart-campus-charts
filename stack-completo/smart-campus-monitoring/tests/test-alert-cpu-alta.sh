#!/bin/bash
# ============================================================
# test-alert-cpu-alta.sh - validacion de la alerta CPUAltaSostenida
# Expr: avg by (app) (process_cpu_usage{app=~"admin|data"}) > 0.02
# for: 5m, severity: warning
#
# Estrategia: 10 curls paralelos contra /data/camera/list durante 7 min
# para mantener CPU sobre 2% sostenido. Suficiente para cumplir el for
# sin ser tan agresivo como para disparar otras reglas innecesariamente.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
BASE_URL="${BASE_URL:-http://localhost}"
PROM="http://monitoring-prometheus-server.monitoring.svc.cluster.local"
ALERT="CPUAltaSostenida"
TS=$(date +%Y%m%d-%H%M%S)
LOG="/tmp/test-alert-cpu-alta-${TS}.log"
PARALELOS=10
DURACION=420   # 7 minutos

say() { echo "$@" | tee -a "$LOG"; }

cpu_actual() {
  $KUBECTL delete pod prom-debug -n monitoring --ignore-not-found >/dev/null 2>&1
  $KUBECTL run prom-debug --rm -i --restart=Never -n monitoring \
    --image=curlimages/curl --quiet -- \
    -s "${PROM}/api/v1/query" \
    --data-urlencode 'query=avg by (app) (process_cpu_usage{app=~"admin|data"})' 2>/dev/null \
    | python3 -c "
import sys, json
data = json.load(sys.stdin)
for r in data.get('data', {}).get('result', []):
    app = r['metric'].get('app', '?')
    value = float(r['value'][1]) * 100
    flag = ' <-- supera umbral 2%' if value > 2.0 else ''
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
alerts = [a for a in data.get('data', {}).get('alerts', []) if a.get('labels', {}).get('alertname') == '${ALERT}']
if not alerts:
    print('    inactive')
else:
    for a in alerts:
        labels = a.get('labels', {})
        print(f\"    {a.get('state','?'):8s} | app={labels.get('app','?')} | activeAt={a.get('activeAt','?')}\")
" 2>/dev/null
}

say "=========================================================="
say "VALIDACION ALERTA: $ALERT"
say "Hora inicio: $(date '+%F %T')"
say "Estrategia: $PARALELOS paralelos contra /data/camera/list por ${DURACION}s"
say "Log: $LOG"
say "=========================================================="

say ""
say ">>> FASE 1: estado inicial"
say "  CPU actual:"
cpu_actual
say "  Alerta:"
estado_alerta

say ""
say ">>> FASE 2: carga ligera ($PARALELOS paralelos)"
say "  Inicio: $(date '+%T')"
FIN=$(( $(date +%s) + DURACION ))

# Lanzar paralelos
for i in $(seq 1 $PARALELOS); do
  (
    while [ $(date +%s) -lt $FIN ]; do
      curl -s -o /dev/null --max-time 5 "${BASE_URL}/data/camera/list"
    done
  ) &
done

# Monitorear cada minuto
for min in 1 2 3 4 5 6 7; do
  sleep 60
  say ""
  say "T+${min}m:"
  say "  CPU:"
  cpu_actual
  say "  Alerta:"
  estado_alerta
done

wait
say ""
say "Fin carga: $(date '+%T')"

say ""
say ">>> FASE 3: estado final"
ESTADO=$(estado_alerta)
echo "$ESTADO" | tee -a "$LOG"

if echo "$ESTADO" | grep -q firing; then
  say ""
  say "RESULTADO: ALERTA DISPARADA"
elif echo "$ESTADO" | grep -q pending; then
  say ""
  say "RESULTADO: PENDING"
else
  say ""
  say "RESULTADO: NO DISPARO"
fi

say ""
say ">>> FASE 4: 60s captura"
sleep 60
say ""
say "FIN. Log: $LOG"
