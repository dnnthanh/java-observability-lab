# Incident curriculum

Each incident is a troubleshooting exercise. Read the symptom first; do not jump to the expected learning signal.
For supported automated triggers run `./scripts/incident.sh <id> trigger`, then `reset`.

## INC-01 — Running but NotReady
One order pod is Running but 0/1 Ready. Investigate readiness, Service endpoints and pod conditions.

## INC-02 — Slow startup
A new pod takes unusually long before accepting traffic. Investigate startup probe and readiness transition.

## INC-03 — Liveness failure
A pod begins restarting. Investigate liveness, restart count and `kubectl logs --previous`.

## INC-04 — CrashLoopBackOff
A rollout cannot stabilize. Investigate events, exit reason and rollback.

## INC-05 — OOMKilled
Temporarily reduce memory limit, set a larger JVM heap, then allocate memory through `/lab/faults/memory`. Verify OOMKilled / exit 137 and compare JVM heap with cgroup memory.

## INC-06 — CPU throttling
Latency rises while one pod is CPU saturated. Correlate CPU limit/throttling with per-pod p95.

## INC-07 — One slow pod
Only one of three order replicas is slow. Group latency by pod.

## INC-08 — One bad pod returns 5xx
Intermittent 5xx appears behind an otherwise healthy deployment. Group errors by pod.

## INC-09 — Heap growth
Repeated retained allocations grow heap. Inspect heap and GC.

## INC-10 — GC pressure
Generate repeated allocations and clears while sending traffic. Correlate GC pauses with p99.

## INC-11 — Servlet thread saturation
Set delay on one pod and drive high concurrency. Inspect busy/max servlet threads and latency.

## INC-12 — Hikari pool exhaustion
Send more concurrent `/lab/db/hold-connection?seconds=15` requests than the pool size. Inspect Hikari active/idle/pending.

## INC-13 — Slow SQL
Call `/lab/db/slow?seconds=10` and inspect downstream latency and PostgreSQL activity.

## INC-14 — PostgreSQL row lock
Run `/lab/db/lock?product=SKU-1&seconds=30`, then create an order for SKU-1. Inspect blocker/waiter state.

## INC-15 — Kafka consumer lag
Scale payment-worker to zero while orders continue. HTTP can stay healthy while asynchronous lag grows.

## INC-16 — Payment provider timeout
Inject delay/error into provider and inspect asynchronous payment trace/error path.

## INC-17 — Retry amplification
Add retries as an exercise and compare no-backoff with exponential backoff behavior.

## INC-18 — Graceful shutdown
Run continuous load and delete one order pod. Observe readiness removal, preStop and in-flight requests.

## INC-19 — Bad rollout
Deploy an invalid image. Inspect ReplicaSet history, rollout status and rollback.

## INC-20 — Service discovery failure
Point Order to a nonexistent Inventory service. Investigate DNS, Service, Endpoints and application errors.
