# Learning path

## Level 0 — Baseline
Run `./scripts/lab.sh up core`, create an order, inspect `kubectl get pods -n lab -o wide`, then collect `./scripts/lab.sh evidence`.

## Level 1 — Kubernetes lifecycle
Learn the difference between Pod phase and Pod readiness. Use `kubectl describe pod`, `kubectl get events --sort-by=.lastTimestamp`, `kubectl logs --previous`, and the evidence timeline (`creationTimestamp`, `startTime`, container `startedAt`, condition `lastTransitionTime`).

## Level 2 — Metrics
Bring up Prometheus/Grafana with `./scripts/monitoring-up.sh`. Start with RED for applications (Rate, Errors, Duration), then USE for resources (Utilization, Saturation, Errors). Drill down from service to pod before opening logs.

## Level 3 — JVM
Inspect heap, GC pauses, live/blocked threads, CPU and process RSS. Compare JVM heap with Kubernetes container memory; an OOMKilled container and a Java `OutOfMemoryError` are different failures.

## Level 4 — Database
Watch Hikari active/idle/pending metrics, PostgreSQL sessions, slow queries and row locks. A service can be slow with low CPU if threads are blocked waiting for a DB connection.

## Level 5 — Kafka
Stop payment consumers and create orders. HTTP can remain healthy while consumer lag grows.

## Level 6 — Tracing
Bring up Tempo/OTel. Follow one order across Edge → Order → Inventory and Kafka-driven payment work.

## Level 7 — Centralized logging
Bring up ELK. Search by service, pod, request ID and trace ID. Compare with `kubectl logs --previous`.

## Level 8 — Incident drills
Use `incidents/README.md`. Form a hypothesis, gather evidence, identify root cause, recover, and prove recovery.
