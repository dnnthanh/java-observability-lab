#!/usr/bin/env bash
set -euo pipefail
require_ready() {
  local name=$1 expected=$2
  local ready desired
  ready="$(kubectl -n lab get deployment "$name" -o jsonpath='{.status.readyReplicas}' 2>/dev/null || echo 0)"
  desired="$(kubectl -n lab get deployment "$name" -o jsonpath='{.spec.replicas}')"
  ready="${ready:-0}"
  echo "$name ready=$ready desired=$desired expected=$expected"
  test "$desired" = "$expected"
  test "$ready" = "$expected"
}
require_ready edge-service 2
require_ready order-service 3
require_ready inventory-service 2
require_ready payment-worker 2
require_ready payment-provider-simulator 2

echo "==> E2E order flow"
payload='{"customerId":"CUS-1","productCode":"SKU-1","quantity":2,"amount":199900}'
response="$(curl -fsS -X POST http://localhost:8080/api/orders -H 'content-type: application/json' -d "$payload")"
echo "$response"
order_id="$(printf '%s' "$response" | python3 -c 'import json,sys; print(json.load(sys.stdin)["id"])')"
test -n "$order_id"

deadline=$((SECONDS+90))
while (( SECONDS < deadline )); do
  body="$(curl -fsS "http://localhost:8080/api/orders/$order_id" || true)"
  echo "$body"
  status="$(printf '%s' "$body" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d.get("status",""))' 2>/dev/null || true)"
  if [[ "$status" == "CONFIRMED" ]]; then
    echo "E2E PASS order=$order_id status=$status"
    exit 0
  fi
  sleep 2
done
echo "E2E FAILED: order did not become CONFIRMED" >&2
kubectl -n lab get pods
kubectl -n lab logs deploy/order-service --tail=120 || true
kubectl -n lab logs deploy/payment-worker --tail=120 || true
exit 1
