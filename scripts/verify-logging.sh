#!/usr/bin/env bash
set -euo pipefail

cleanup() {
  [[ -n "${ES_PF:-}" ]] && kill "$ES_PF" 2>/dev/null || true
}
trap cleanup EXIT

for i in {1..5}; do
  curl -fsS http://localhost:8080/api/whoami >/dev/null
  sleep 1
done

kubectl -n observability port-forward svc/elasticsearch 19200:9200 >/tmp/es-pf.log 2>&1 &
ES_PF=$!
for i in {1..90}; do
  if curl -fsS http://127.0.0.1:19200/_cluster/health >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

for i in {1..90}; do
  result="$(curl -fsS -H 'content-type: application/json'     http://127.0.0.1:19200/lab-logs-*/_search     -d '{"size":3,"query":{"match_phrase":{"message":"access service=edge-service"}}}' 2>/dev/null || true)"
  if printf '%s' "$result" | python3 -c '
import json,sys
try:
    d=json.load(sys.stdin)
except Exception:
    raise SystemExit(1)
total=d.get("hits",{}).get("total",{})
value=total.get("value",0) if isinstance(total,dict) else total
raise SystemExit(0 if value > 0 else 1)
'; then
    echo "elasticsearch business access log: PASS"
    printf '%s' "$result" | python3 -c '
import json,sys
d=json.load(sys.stdin)
for h in d.get("hits",{}).get("hits",[])[:3]:
    print(h.get("_source",{}).get("message",""))
'
    echo "LOGGING PROOF PASS"
    exit 0
  fi
  sleep 2
done

kubectl -n observability logs daemonset/filebeat --tail=100 >&2 || true
kubectl -n observability logs deployment/logstash --tail=100 >&2 || true
curl -fsS http://127.0.0.1:19200/_cat/indices?v >&2 || true
echo "No business access log reached Elasticsearch" >&2
exit 1
