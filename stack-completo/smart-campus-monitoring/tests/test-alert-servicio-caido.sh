#!/bin/bash
# ============================================================
# test-alert-servicio-caido.sh - validacion de la alerta ServicioCaido
# Expr: absent(up{app="admin"}) or up{app="admin"} == 0 or
#       absent(up{app="data"})  or up{app="data"}  == 0
# for: 1m, severity: critical
#
# Estrategia: escalar admin a 0 replicas durante 90s. Prometheus
# pierde el target y la condicion up==0 se cumple. Tras 1m del for,
# la alerta pasa a firing. Luego restauramos admin a 1 replica.
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
DEPLOY="admin"   # el deployment que vamos a apagar
PROM="http://monitoring-prometheus-server.monitoring.svc.cluster.local"
ALERT="ServicioCaido"
TS=$(date +%Y%m%d-%H%M%S)
LOG="/tmp/test-alert-servicio-caido-${TS}.log"

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
    print('    inactive (sin alertas activas)')
else:
    for a in alerts:
        labels = a.get('labels', {})
        print(f\"    {a.get('state','?'):8s} | app={labels.get('app','?')} | activeAt={a.get('activeAt','?')}\")
" 2>/dev/null
}

replicas_actuales() {
  $KUBECTL get deployment $DEPLOY -n $NS -o jsonpath='{.status.replicas}' 2>/dev/null
}

say "=========================================================="
say "VALIDACION ALERTA: $ALERT"
say "Hora inicio: $(date '+%F %T')"
say "Deployment objetivo: $DEPLOY (namespace $NS)"
say "Log: $LOG"
say "=========================================================="

# FASE 1: estado inicial
say ""
say ">>> FASE 1: estado inicial"
say "  Replicas actuales de $DEPLOY: $(replicas_actuales)"
say "  Alerta:"
estado_alerta

# FASE 2: tumbar admin
say ""
say ">>> FASE 2: escalando $DEPLOY a 0 replicas"
say "  Hora: $(date '+%T')"
$KUBECTL scale deployment $DEPLOY -n $NS --replicas=0 | tee -a "$LOG"

# Esperar a que el pod realmente termine
say ""
say "  Esperando a que el pod $DEPLOY desaparezca..."
TIMEOUT=30
while [ $TIMEOUT -gt 0 ]; do
  count=$($KUBECTL get pods -n $NS -l app=$DEPLOY --no-headers 2>/dev/null | wc -l)
  if [ "$count" = "0" ]; then
    say "  Pod $DEPLOY desaparecido tras $((30-TIMEOUT))s"
    break
  fi
  sleep 2
  TIMEOUT=$((TIMEOUT - 2))
done

# FASE 3: esperar el "for: 1m" + buffer
say ""
say ">>> FASE 3: esperando 'for' (1m) + buffer (30s) = 1.5 min"
say "  La alerta debe pasar de inactive -> pending -> firing"
for i in 1 2 3; do
  sleep 30
  say ""
  say "T+$((i*30))s desde el escalado:"
  estado_alerta
done

# FASE 4: confirmar firing
say ""
say ">>> FASE 4: estado final tras apagado de $DEPLOY"
ESTADO_FINAL=$(estado_alerta)
echo "$ESTADO_FINAL" | tee -a "$LOG"

if echo "$ESTADO_FINAL" | grep -q "firing"; then
  say ""
  say "RESULTADO: ALERTA DISPARADA CORRECTAMENTE"
elif echo "$ESTADO_FINAL" | grep -q "pending"; then
  say ""
  say "RESULTADO: PENDING (esperar mas tiempo)"
else
  say ""
  say "RESULTADO: NO DISPARO (revisar deployment)"
fi

# FASE 5: tiempo para captura visual
say ""
say ">>> FASE 5: dejando 60s para captura en Grafana"
say "  Dashboard 'Smart Campus - Alertas y rendimiento'"
say "  Panel 1 'Estado de servicios' debe mostrar 'admin' en ROJO 'CAIDO'"
say "  Panel 6 'Alertas activas' debe listar ServicioCaido"
sleep 60

# FASE 6: restaurar el servicio
say ""
say ">>> FASE 6: restaurando $DEPLOY a 1 replica"
say "  Hora: $(date '+%T')"
$KUBECTL scale deployment $DEPLOY -n $NS --replicas=1 | tee -a "$LOG"

# Esperar a que el pod este Running de nuevo
say ""
say "  Esperando a que $DEPLOY este Running de nuevo..."
TIMEOUT=60
while [ $TIMEOUT -gt 0 ]; do
  ready=$($KUBECTL get deployment $DEPLOY -n $NS -o jsonpath='{.status.readyReplicas}' 2>/dev/null)
  if [ "$ready" = "1" ]; then
    say "  Pod $DEPLOY READY tras $((60-TIMEOUT))s"
    break
  fi
  sleep 3
  TIMEOUT=$((TIMEOUT - 3))
done

# FASE 7: verificar transicion firing -> inactive
say ""
say ">>> FASE 7: esperando transicion firing -> inactive (60s)"
sleep 60
say ""
say "  Estado final de la alerta tras restauracion:"
estado_alerta

say ""
say "=========================================================="
say "FIN. Log: $LOG"
say "=========================================================="
