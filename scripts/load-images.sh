#!/usr/bin/env bash
set -euo pipefail
CLUSTER="${KIND_CLUSTER_NAME:-observability-lab}"
for s in edge-service order-service inventory-service payment-worker payment-provider-simulator; do
  kind load docker-image "lab/$s:local" --name "$CLUSTER"
done
