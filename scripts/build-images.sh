#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/.." && pwd)"
cd "$root"
services=(edge-service order-service inventory-service payment-worker payment-provider-simulator)
for s in "${services[@]}"; do
  echo "==> building lab/$s:local"
  docker build --build-arg MODULE="$s" -t "lab/$s:local" .
done
