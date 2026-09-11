#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
kubectl apply -f "$ROOT/deploy/core/namespace.yaml"
kubectl apply -f "$ROOT/deploy/observability/elk.yaml"
kubectl -n observability rollout status deployment/elasticsearch --timeout=360s
kubectl -n observability rollout status deployment/logstash --timeout=300s
kubectl -n observability rollout status deployment/kibana --timeout=360s
echo "Kibana: http://localhost:5601"
