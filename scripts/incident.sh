#!/usr/bin/env bash
set -euo pipefail
id="${1:-}"
action="${2:-trigger}"
pod_call() {
  local app="$1" path="$2" port="${3:-18080}"
  local pod
  pod="$(kubectl -n lab get pod -l "app=$app" -o jsonpath='{.items[0].metadata.name}')"
  kubectl -n lab port-forward "pod/$pod" "$port:8080" >/tmp/lab-pf.log 2>&1 &
  local pf=$!
  sleep 2
  curl -fsS -X POST "http://127.0.0.1:$port$path"
  kill "$pf" 2>/dev/null || true
  wait "$pf" 2>/dev/null || true
}
case "$id:$action" in
  01:trigger) pod_call order-service '/lab/faults/readiness?up=false' ;;
  01:reset) pod_call order-service '/lab/faults/readiness?up=true' ;;
  03:trigger) pod_call order-service '/lab/faults/liveness?up=false' ;;
  03:reset) kubectl -n lab rollout restart deploy/order-service ;;
  04:trigger) kubectl -n lab set env deploy/order-service LAB_CRASH_ON_START=true ;;
  04:reset) kubectl -n lab set env deploy/order-service LAB_CRASH_ON_START- ;;
  06:trigger) pod_call order-service '/lab/faults/cpu?seconds=60' ;;
  07:trigger) pod_call order-service '/lab/faults/delay?ms=4000' ;;
  07:reset) pod_call order-service '/lab/faults/delay?ms=0' ;;
  08:trigger) pod_call order-service '/lab/faults/errors?percent=100' ;;
  08:reset) pod_call order-service '/lab/faults/errors?percent=0' ;;
  09:trigger) pod_call order-service '/lab/faults/memory?mb=100' ;;
  09:reset) pod_call order-service '/lab/faults/memory/clear' ;;
  15:trigger) kubectl -n lab scale deploy/payment-worker --replicas=0 ;;
  15:reset) kubectl -n lab scale deploy/payment-worker --replicas=2 ;;
  19:trigger) kubectl -n lab set image deploy/order-service app=lab/order-service:does-not-exist ;;
  19:reset) kubectl -n lab set image deploy/order-service app=lab/order-service:local ;;
  20:trigger) kubectl -n lab set env deploy/order-service INVENTORY_URL=http://inventory-does-not-exist:8080 ;;
  20:reset) kubectl -n lab set env deploy/order-service INVENTORY_URL=http://inventory-service:8080 ;;
  *) echo "Incident $id/$action is documented in incidents/README.md"; exit 2 ;;
esac
