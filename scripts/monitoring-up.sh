#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts >/dev/null 2>&1 || true
helm repo update
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack   --namespace observability --create-namespace   --set grafana.service.type=NodePort   --set grafana.service.nodePort=30030   --set grafana.adminPassword=admin   --set prometheus.prometheusSpec.podMonitorSelectorNilUsesHelmValues=false
kubectl apply -f "$ROOT/deploy/observability/podmonitor.yaml"
kubectl apply -f "$ROOT/deploy/observability/dashboard.yaml"
kubectl -n observability rollout status deployment/monitoring-grafana --timeout=240s
echo "Grafana: http://localhost:3000 user=admin password=admin"
