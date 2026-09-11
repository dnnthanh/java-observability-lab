#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
CLUSTER="${KIND_CLUSTER_NAME:-observability-lab}"
if kind get clusters 2>/dev/null | grep -qx "$CLUSTER"; then
  echo "kind cluster $CLUSTER already exists"
else
  kind create cluster --name "$CLUSTER" --config "$ROOT/deploy/kind/cluster.yaml" --image kindest/node:v1.36.1
fi
kubectl cluster-info --context "kind-$CLUSTER"
