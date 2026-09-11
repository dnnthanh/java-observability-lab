#!/usr/bin/env bash
set -euo pipefail

cleanup() {
  [[ -n "${PROM_PF:-}" ]] && kill "$PROM_PF" 2>/dev/null || true
  [[ -n "${GRAFANA_PF:-}" ]] && kill "$GRAFANA_PF" 2>/dev/null || true
}
trap cleanup EXIT

kubectl -n observability port-forward svc/monitoring-kube-prometheus-prometheus 19090:9090 >/tmp/prom-pf.log 2>&1 &
PROM_PF=$!
kubectl -n observability port-forward svc/monitoring-grafana 13000:80 >/tmp/grafana-pf.log 2>&1 &
GRAFANA_PF=$!

for i in {1..60}; do
  if curl -fsS http://127.0.0.1:19090/-/ready >/dev/null 2>&1 &&      curl -fsS -u admin:admin http://127.0.0.1:13000/api/health >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

curl -fsS http://localhost:8080/api/whoami >/dev/null
sleep 20

query_has_result() {
  local q="$1" name="$2"
  local body
  body="$(curl -fsSG http://127.0.0.1:19090/api/v1/query --data-urlencode "query=$q")"
  printf '%s' "$body" | python3 -c '
import json,sys
name=sys.argv[1]
d=json.load(sys.stdin)
r=d.get("data",{}).get("result",[])
if not r:
    raise SystemExit(f"no Prometheus result for {name}")
print(f"{name}: PASS ({len(r)} series)")
' "$name"
}

query_has_result 'kube_pod_status_ready{namespace="lab",condition="true"}' kubernetes-ready
query_has_result 'jvm_memory_used_bytes{service="order-service",area="heap"}' jvm-heap
query_has_result 'http_server_requests_seconds_count{service="edge-service"}' http-server
query_has_result 'http_server_requests_seconds_bucket{service="edge-service"}' http-histogram

curl -fsS -u admin:admin http://127.0.0.1:13000/api/dashboards/uid/java-observability-lab   | python3 -c 'import json,sys; d=json.load(sys.stdin); assert d["dashboard"]["uid"]=="java-observability-lab"; print("grafana-dashboard: PASS")'

echo "METRICS PROOF PASS"
