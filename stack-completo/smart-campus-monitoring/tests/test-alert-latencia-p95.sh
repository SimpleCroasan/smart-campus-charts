#!/bin/bash
# ============================================================
# test-alert-latencia-p95.sh - validacion de la alerta LatenciaP95Alta
# Expr: histogram_quantile(0.95, ...) > 0.5
# for: 5m, severity: warning
#
# Estrategia: 30 curls paralelos continuos durante 6 minutos contra
# /data/camera/list para saturar el pool de Tomcat + MongoDB, forzando
# que la P95 de ese endpoint suba > 500ms sostenido.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
BASE_URL="${BASE_URL:-http://localhost}"
PROM="http://monitoring-prometheus-server.monitoring.svc.cluster.local"
ALERT="LatenciaP95Alta"
TARGET_URL="${BASE_URL}/data/camera/list"
TS=$(date +%Y%m%d-%H%M%S)
LOG="/tmp/test-alert-latencia-p95-${TS}.log"

say() { echo "$@" | tee -a "$LOG"; }

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
    print('inactive (sin alertas activas)')
else:
    for a in alerts:
        labels = a.get('labels', {})
        print(f\"{a.get('state','?')} | app={labels.get('app','?')} uri={labels.get('uri','?')} | activeAt={a.get('activeAt','?')}\")
" 2>/dev/null
}

# Funcion auxiliar para consultar P95 actual de un endpoint
p95_actual() {
  $KUBECTL delete pod prom-debug -n monitoring --ignore-not-found >/dev/null 2>&1
  $KUBECTL run prom-debug --rm -i --restart=Never -n monitoring \
    --image=curlimages/curl --quiet -- \
    -s "${PROM}/api/v1/query" --data-urlencode "query=histogram_quantile(0.95, sum by (app, uri, le) (rate(http_server_requests_seconds_bucket{app=\"data\", uri=\"/camera/list\"}[5m])))" 2>/dev/null \
    | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    results = data.get('data', {}).get('result', [])
    if results:
        for r in results:
            value = r.get('value', [None, '?'])[1]
            print(f'P95 /camera/list: {value}s')
    else:
        print('P95: sin datos')
except: print('P95: error parse')
" 2>/dev/null
}

say "=========================================================="
say "VALIDACION ALERTA: $ALERT"
say "Hora inicio: $(date '+%F %T')"
say "Endpoint: $TARGET_URL"
say "Log: $LOG"
say "=========================================================="

# FASE 1: estado inicial
say ""
say ">>> FASE 1: estado inicial"
say "Alerta:"; estado_alerta | tee -a "$LOG"
say "Latencia actual:"; p95_actual | tee -a "$LOG"

# FASE 2: provocar latencia con concurrencia
say ""
say ">>> FASE 2: 30 curls paralelos durante 6 minutos"
say "Inicio carga: $(date '+%T')"
FIN=$(( $(date +%s) + 360 ))   # 6 minutos
PARALELOS=30

# Lanzar 30 procesos en background que hacen requests continuos hasta el tiempo limite
for i in $(seq 1 $PARALELOS); do
  (
    while [ $(date +%s) -lt $FIN ]; do
      curl -s -o /dev/null --max-time 10 "$TARGET_URL"
    done
  ) &
done

# Esperar a que termine el periodo de carga
# Pero monitorear cada minuto
for min in 1 2 3 4 5 6; do
  sleep 60
  say ""
  say "T+${min}m de carga:"
  say "  $(p95_actual)"
  say "  $(estado_alerta)"
done

# Esperar a que terminen todos los background
wait
say ""
say "Fin carga: $(date '+%T')"

# FASE 3: confirmar firing tras el for: 5m
say ""
say ">>> FASE 3: estado final tras 6 min de carga"
ESTADO_FINAL=$(estado_alerta)
echo "$ESTADO_FINAL" | tee -a "$LOG"

if echo "$ESTADO_FINAL" | grep -q "firing"; then
  say ""
  say "RESULTADO: ALERTA DISPARADA CORRECTAMENTE"
elif echo "$ESTADO_FINAL" | grep -q "pending"; then
  say ""
  say "RESULTADO: ALERTA EN PENDING (no llego a firing - extender carga)"
else
  say ""
  say "RESULTADO: ALERTA NO DISPARO (P95 no cruzo 500ms o no se sostuvo)"
fi

# FASE 4: tiempo para captura visual
say ""
say ">>> FASE 4: dejando 90s para captura en Grafana"
say "Dashboard: 'Smart Campus - Alertas y rendimiento'"
say "Panel 'Latencia P95 por endpoint' debe mostrar la linea sobre 0.5s (umbral)"
sleep 90

# FASE 5: cierre
say ""
say ">>> FASE 5: nota sobre transicion firing -> inactive"
say "Sin carga, la P95 vuelve a valores bajos en ~5 min."
say "Confirma cierre del ciclo consultando estado mas tarde."

say ""
say "=========================================================="
say "FIN. Log completo en: $LOG"
say "=========================================================="
