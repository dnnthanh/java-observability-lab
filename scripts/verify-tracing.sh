#!/usr/bin/env bash
set -euo pipefail

cleanup() {
  [[ -n "${TEMPO_PF:-}" ]] && kill "$TEMPO_PF" 2>/dev/null || true
}
trap cleanup EXIT

payload='{"customerId":"TRACE","productCode":"SKU-1","quantity":1,"amount":12345}'
curl -fsS -X POST http://localhost:8080/api/orders -H 'content-type: application/json' -d "$payload" >/tmp/trace-order.json

kubectl -n observability port-forward svc/tempo 13200:3200 >/tmp/tempo-pf.log 2>&1 &
TEMPO_PF=$!
for i in {1..60}; do
  curl -fsS http://127.0.0.1:13200/ready >/dev/null 2>&1 && break
  sleep 2
done

search_service() {
  local service="$1" out="/tmp/tempo-${service}.json"
  for i in {1..45}; do
    curl -fsSG http://127.0.0.1:13200/api/search       --data-urlencode "q={ resource.service.name = \"$service\" }"       --data-urlencode "limit=20" > "$out" || true
    if python3 - "$out" <<'PY'
import json,sys
try:
    d=json.load(open(sys.argv[1]))
except Exception:
    raise SystemExit(1)
raise SystemExit(0 if d.get("traces") else 1)
PY
    then
      echo "$service trace search: PASS"
      return 0
    fi
    sleep 2
  done
  cat "$out" >&2 || true
  echo "no Tempo trace found for $service" >&2
  return 1
}

search_service edge-service
search_service order-service
search_service inventory-service

echo "TRACING PROOF PASS"
