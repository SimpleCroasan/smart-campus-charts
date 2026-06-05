#!/bin/bash
# ============================================================
# test-data-trace.sh — Capa 2: instrumentacion del microservicio DATA
# Valida que data:
#  (1) responde HTTP via Traefik (happy-path + error 4xx)
#  (2) honra traceparent inyectado por cliente (propagacion W3C)
#  (3) genera trace_id propio cuando no recibe traceparent
#  (4) propaga el trace_id al MDC -> logback -> Loki
#  (5) la traza queda almacenada en Tempo (verificable por API)
#  (6) sus metricas http_server_requests_seconds_count llegan a Prometheus
#  (7) los spans incluyen llamadas descendentes a MongoDB e InfluxDB
# ============================================================
if command -v kubectl >/dev/null 2>&1; then KUBECTL="kubectl"; else KUBECTL="k3s kubectl"; fi
NS="smart-campus"
BASE_URL="${BASE_URL:-http://localhost}"
SVC="data"

gen_hex() {
  if command -v openssl >/dev/null 2>&1; then openssl rand -hex $1
  else head -c $1 /dev/urandom | xxd -p -c 256; fi
}
CLIENT_TRACE=$(gen_hex 16)
TRACEPARENT="00-${CLIENT_TRACE}-$(gen_hex 8)-01"

echo "### Hora: $(date '+%F %T')"
echo "### Microservicio: ${SVC}"
echo "### TRACE_ID cliente: ${CLIENT_TRACE}"
echo ""

echo "=== Preflight: data responde ==="
PF=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 3 \
  -H "traceparent: ${TRACEPARENT}" "${BASE_URL}/data/camera/list")
if [ "$PF" = "000" ]; then
  echo "ERROR: ${BASE_URL} inaccesible. Usa port-forward:"
  echo "  $KUBECTL port-forward -n kube-system svc/traefik 8080:80"
  echo "y reintenta: BASE_URL=http://localhost:8080 bash \$0"
  exit 1
fi
echo "Traefik responde (HTTP $PF)."
echo ""

req() {
  local label=$1 method=$2 ruta=$3 tpflag=$4 body=$5 ctype=$6
  local hdr=""; [ "$tpflag" = "on" ] && hdr="-H traceparent:${TRACEPARENT}"
  local extra=""
  [ -n "$body" ] && extra="-H Content-Type:${ctype} --data-raw $body"
  printf "=== %s ===\n" "$label"
  curl -s -o /dev/null \
    -w "URL=${BASE_URL}${ruta}\nMETHOD=${method}\nTRACEPARENT=${tpflag}\nHTTP=%{http_code}  tiempo=%{time_total}s\n\n" \
    -X "$method" $hdr $extra "${BASE_URL}${ruta}"
}

echo "----- A) Propagacion W3C + spans multi-componente -----"
req "A1: GET camera/list (HTTP + Mongo)"               GET "/data/camera/list"                              on
req "A2: GET mongo por deviceId (HTTP + Mongo)"        GET "/data/mongo/deviceId/dispositivo_test"          on
req "A3: GET influx ultima medicion (HTTP + Influx)"   GET "/data/influx/measurement/temperature/last"      on
req "A4: GET influx medicion inexistente (error 4xx)"  GET "/data/influx/measurement/no_existe/last"        on

echo "----- B) Generacion autonoma de trace_id (sin header) -----"
req "B1: GET camera/list sin traceparent"              GET "/data/camera/list"                              off

echo "Esperando 6s para que Alloy recolecte logs y Tempo flushee spans..."
sleep 6

echo ""
echo "=== Verificacion 1: trace_id inyectado en logs del pod data ==="
$KUBECTL logs -l app=${SVC} -n $NS --tail=400 2>/dev/null | grep "$CLIENT_TRACE" | head -5 \
  || echo "(NO encontrado: revisar logback-spring.xml: %X{trace_id:-})"

echo ""
echo "=== Verificacion 2: traza en Tempo (API /api/traces/{id}) ==="
TEMPO_RESP=$($KUBECTL run tempo-curl-${SVC} --rm -i --restart=Never -n monitoring \
  --image=curlimages/curl --quiet -- \
  -s -o /dev/null -w "%{http_code}" \
  "http://monitoring-tempo.monitoring.svc.cluster.local:3100/api/traces/${CLIENT_TRACE}" 2>/dev/null)
case "$TEMPO_RESP" in
  200) echo "OK: Tempo devuelve 200 para ${CLIENT_TRACE}" ;;
  404) echo "AVISO: Tempo devuelve 404 (reintenta en 30s; trace_idle_period=20s)" ;;
  *)   echo "Tempo devolvio HTTP $TEMPO_RESP" ;;
esac

echo ""
echo "=== Verificacion 3: metrica HTTP de data en Prometheus ==="
QUERY='sum by (uri, method, status) (http_server_requests_seconds_count{app="data"})'
$KUBECTL run prom-curl-${SVC} --rm -i --restart=Never -n monitoring \
  --image=curlimages/curl --quiet -- \
  -s --get --data-urlencode "query=${QUERY}" \
  "http://monitoring-prometheus-server.monitoring.svc.cluster.local/api/v1/query" 2>/dev/null \
  | head -c 1000
echo ""

echo ""
echo "=== Pasos manuales en Grafana (rango: ultima 1 hora) ==="
echo "Loki  → query: {namespace=\"smart-campus\", app=\"${SVC}\"} |~ \"${CLIENT_TRACE}\""
echo "Tempo → Trace ID: ${CLIENT_TRACE}  (debe mostrar span HTTP + spans Mongo/Influx)"
echo "Trace ID data: ${CLIENT_TRACE}"
