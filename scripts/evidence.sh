#!/usr/bin/env bash
set -euo pipefail
mkdir -p evidence
ts="$(date +%Y%m%d-%H%M%S)"
out="evidence/$ts"
mkdir -p "$out"
kubectl get pods -A -o wide > "$out/pods.txt"
kubectl get deployments -A > "$out/deployments.txt"
kubectl get events -A --sort-by=.lastTimestamp > "$out/events.txt"
kubectl -n lab get pods -o json > "$out/pods.json"
python3 - "$out/pods.json" > "$out/pod-lifecycle.txt" <<'PY'
import json,sys
d=json.load(open(sys.argv[1]))
for p in d["items"]:
  m=p["metadata"]; s=p.get("status",{})
  print(f"\nPOD {m['name']}")
  print(" creationTimestamp:",m.get("creationTimestamp"))
  print(" startTime:",s.get("startTime"))
  for cs in s.get("containerStatuses",[]):
    print(" container",cs["name"],"restartCount=",cs.get("restartCount"),"startedAt=",cs.get("state",{}).get("running",{}).get("startedAt"),"lastTermination=",cs.get("lastState",{}).get("terminated"))
  for c in s.get("conditions",[]):
    print(" condition",c.get("type"),"status=",c.get("status"),"transition=",c.get("lastTransitionTime"),"reason=",c.get("reason"))
PY
echo "Evidence written to $out"
