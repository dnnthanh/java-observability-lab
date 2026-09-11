#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
kubectl apply -f "$ROOT/deploy/core/namespace.yaml"
kubectl apply -f "$ROOT/deploy/core/postgres.yaml"
kubectl apply -f "$ROOT/deploy/core/kafka.yaml"
kubectl -n lab rollout status deployment/postgres --timeout=180s
kubectl -n lab rollout status deployment/kafka --timeout=240s

echo "==> creating Kafka topics"
for i in {1..20}; do
  if kubectl -n lab exec deploy/kafka -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list >/dev/null 2>&1; then break; fi
  sleep 3
done
kubectl -n lab exec deploy/kafka -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --create --if-not-exists --topic orders.created --partitions 3 --replication-factor 1
kubectl -n lab exec deploy/kafka -- /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --create --if-not-exists --topic payments.completed --partitions 3 --replication-factor 1

kubectl apply -f "$ROOT/deploy/core/apps.yaml"
for d in edge-service order-service inventory-service payment-worker payment-provider-simulator; do
  kubectl -n lab rollout status "deployment/$d" --timeout=300s
done
kubectl get pods -n lab -o wide
