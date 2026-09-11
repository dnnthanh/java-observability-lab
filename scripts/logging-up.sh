#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

kubectl apply -f "$ROOT/deploy/core/namespace.yaml"
kubectl apply -f "$ROOT/deploy/observability/elk.yaml"

kubectl -n observability rollout status deployment/elasticsearch --timeout=480s
kubectl -n observability rollout status deployment/logstash --timeout=480s
kubectl -n observability rollout status daemonset/filebeat --timeout=300s
kubectl -n observability rollout status deployment/kibana --timeout=900s

echo "Kibana: http://localhost:5601"
