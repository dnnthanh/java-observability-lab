#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cmd="${1:-help}"
profile="${2:-core}"
case "$cmd" in
  up)
    command -v docker >/dev/null
    command -v kind >/dev/null
    command -v kubectl >/dev/null
    "$ROOT/scripts/build-images.sh"
    "$ROOT/scripts/create-cluster.sh"
    "$ROOT/scripts/load-images.sh"
    "$ROOT/scripts/deploy-core.sh"
    if [[ "$profile" == "full" ]]; then
      command -v helm >/dev/null
      "$ROOT/scripts/monitoring-up.sh"
      "$ROOT/scripts/tracing-up.sh"
      "$ROOT/scripts/logging-up.sh"
    fi
    "$ROOT/scripts/verify-core.sh"
    ;;
  verify) "$ROOT/scripts/verify-core.sh" ;;
  evidence) "$ROOT/scripts/evidence.sh" ;;
  down) kind delete cluster --name "${KIND_CLUSTER_NAME:-observability-lab}" || true ;;
  *) echo "Usage: ./scripts/lab.sh up [core|full] | verify | evidence | down" ;;
esac
