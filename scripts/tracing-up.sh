#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
kubectl apply -f "$ROOT/deploy/core/namespace.yaml"
kubectl apply -f "$ROOT/deploy/observability/tempo.yaml"
kubectl -n observability rollout status deployment/tempo --timeout=180s
kubectl -n observability rollout status deployment/otel-collector --timeout=180s
echo "Tempo and OTel Collector are ready."
