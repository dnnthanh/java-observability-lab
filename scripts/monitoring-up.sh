#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

helm repo add prometheus-community https://prometheus-community.github.io/helm-charts >/dev/null 2>&1 || true
helm repo update
helm upgrade --install monitoring prometheus-community/kube-prometheus-stack   --namespace observability --create-namespace   --set grafana.service.type=NodePort   --set grafana.service.nodePort=30030   --set grafana.adminPassword=admin   --set grafana.sidecar.dashboards.searchNamespace=ALL   --set grafana.sidecar.datasources.enabled=true   --set grafana.sidecar.datasources.label=grafana_datasource   --set prometheus.prometheusSpec.podMonitorSelectorNilUsesHelmValues=false   --set prometheus.prometheusSpec.retention=2h

kubectl apply -f "$ROOT/deploy/observability/podmonitor.yaml"
kubectl apply -f "$ROOT/deploy/observability/dashboard.yaml"
kubectl apply -f "$ROOT/deploy/observability/alerts.yaml"
kubectl apply -f "$ROOT/deploy/observability/grafana-datasources.yaml"

kubectl -n observability rollout status deployment/monitoring-grafana --timeout=300s
kubectl -n observability rollout status deployment/monitoring-kube-state-metrics --timeout=300s
prom_sts="$(kubectl -n observability get statefulset -l app.kubernetes.io/name=prometheus -o jsonpath='{.items[0].metadata.name}')"
test -n "$prom_sts"
kubectl -n observability rollout status "statefulset/$prom_sts" --timeout=300s

echo "Grafana: http://localhost:3000 user=admin password=admin"
