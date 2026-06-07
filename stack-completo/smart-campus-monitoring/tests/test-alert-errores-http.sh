#!/bin/bash
# ============================================================
# test-alert-errores-http.sh - validacion de la alerta ErroresHTTP
# Expr: rate(http_server_requests_seconds_count{status=~"4..|5.."}[5m]) > 0.1
# for: 2m, severity: warning
#
# Estrategia: provocar 500s sostenidos a >0.5 req/s durante ~3 minutos
# para que rate(5m) cruce 0.1 y se mantenga >= 2 minutos (el "for").
# Aprovecha el bug real IndexOutOfBoundsException de InfluxController.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
BASE_URL="${BASE_URL:-http://localhost}"
PROM="http://monitoring-prometheus-server.monitoring.svc.cluster.local"
ALERT="ErroresHTTP"
TS=$(date +%Y%m%d-%H%M%S)
LOG="/tmp/test-alert-errores-http-${TS}.log"

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
        print(f\"{a.get('state','?')} | app={labels.get('app','?')} uri={labels.get('uri','?')} status={labels.get('status','?')} | activeAt={a.get('activeAt','?')}\")
" 2>/dev/null
}

say "=========================================================="
say "VALIDACION ALERTA: $ALERT"
say "Hora inicio: $(date '+%F %T')"
say "Log: $LOG"
say "=========================================================="

say ""
say ">>> FASE 1: estado inicial de la alerta"
estado_alerta | tee -a "$LOG"

say ""
say ">>> FASE 2: provocando errores 5xx sostenidos (3 min)"
say "Atacando GET /data/influx/measurement/temperature/last cada 0.5s"
FIN=$(( $(date +%s) + 180 ))
COUNT=0
while [ $(date +%s) -lt $FIN ]; do
  HTTP=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/data/influx/measurement/temperature/last")
  COUNT=$((COUNT+1))
  if [ $((COUNT % 30)) -eq 0 ]; then
    say "  ... $COUNT peticiones enviadas (ultima: HTTP $HTTP)"
  fi
  sleep 0.5
done
say "Peticiones totales enviadas: $COUNT"

say ""
say ">>> FASE 3: esperando 'for' (2m) + buffer (30s) = 2.5 min"
say "La alerta debe pasar de inactive -> pending -> firing"
for i in 1 2 3 4 5; do
  sleep 30
  say ""
  say "T+$((i*30))s desde fin de carga:"
  estado_alerta | tee -a "$LOG"
done

say ""
say ">>> FASE 4: estado final de la alerta"
ESTADO_FINAL=$(estado_alerta)
echo "$ESTADO_FINAL" | tee -a "$LOG"

if echo "$ESTADO_FINAL" | grep -q "firing"; then
  say ""
  say "RESULTADO: ALERTA DISPARADA CORRECTAMENTE"
elif echo "$ESTADO_FINAL" | grep -q "pending"; then
  say ""
  say "RESULTADO: ALERTA EN PENDING (no llego a firing - aumentar tiempo de carga)"
else
  say ""
  say "RESULTADO: ALERTA NO DISPARO (revisar umbral o carga)"
fi

say ""
say ">>> FASE 5: dejando 90s para captura en dashboard de Grafana"
say "Abre el dashboard 'Smart Campus - Alertas y rendimiento'"
say "El panel 'Alertas activas' debe listar ${ALERT}"
sleep 90

say ""
say ">>> FASE 6: nota sobre la transicion firing -> inactive"
say "Para confirmar la transicion firing -> inactive, espera 5-10 min sin trafico"
say "y vuelve a consultar el estado de la alerta."

say ""
say "=========================================================="
say "FIN. Log completo en: $LOG"
say "=========================================================="
