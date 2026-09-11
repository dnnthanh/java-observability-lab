# Java Observability Lab

A production-style local lab for a Java/Spring developer to learn **Kubernetes lifecycle, metrics, centralized logs, distributed traces, JVM/DB/Kafka troubleshooting, and multi-instance incident investigation**.

## What runs

Business flow:

```text
client/k6
   |
   v
edge-service (2 pods)
   |
   v
order-service (3 pods) ----HTTP----> inventory-service (2 pods) ---> PostgreSQL
   |
   +---- Kafka: orders.created ----> payment-worker (2 pods)
                                         |
                                         +----HTTP----> payment-provider-simulator (2 pods)
                                         |
                                         +---- PostgreSQL
                                         +---- Kafka: payments.completed ---> order-service
```

A successful order becomes `CONFIRMED` only after the asynchronous payment flow completes.

Observability:

```text
Spring Actuator/Micrometer ---> Prometheus ---> Grafana
Kubernetes state ------------> kube-state-metrics --^
Micrometer tracing ----------> OTel Collector ---> Tempo
container logs ---> Filebeat ---> Logstash ---> Elasticsearch ---> Kibana
```

## Prerequisites

Mac/Linux with Docker. Install `kind`, `kubectl`, and optionally `helm` for Grafana/Prometheus.

Recommended:
- Docker Desktop: 8+ GB assigned RAM for `core`; 12–16 GB for the full ELK stack.
- Java/Maven are **not required locally** for the main path; application JARs are built inside Docker.

## One-command core lab

```bash
git clone https://github.com/dnnthanh/java-observability-lab.git
cd java-observability-lab
./scripts/lab.sh up core
```

This builds five images, creates a 3-node kind cluster, starts PostgreSQL/Kafka, deploys 11 application pods, and runs an end-to-end order smoke test.

Check:

```bash
kubectl get pods -n lab -o wide
curl http://localhost:8080/api/whoami
./scripts/lab.sh verify
./scripts/lab.sh evidence
```

Expected application replicas:

```text
edge-service                    2/2
order-service                   3/3
inventory-service               2/2
payment-worker                  2/2
payment-provider-simulator      2/2
```

## Full observability

After core is healthy:

```bash
./scripts/monitoring-up.sh
./scripts/tracing-up.sh
./scripts/logging-up.sh
```

Then:
- Grafana: http://localhost:3000 (`admin` / `admin`)
- Kibana: http://localhost:5601
- Edge API: http://localhost:8080

The full ELK profile is intentionally optional because Elasticsearch + Kibana + Logstash are memory-heavy on a laptop.

## Pod start / Ready / restart timeline

Run:

```bash
./scripts/lab.sh evidence
cat evidence/*/pod-lifecycle.txt
```

You will see, per pod:
- `metadata.creationTimestamp`
- Pod `status.startTime`
- container `startedAt`
- `restartCount`
- previous termination reason
- `PodScheduled`, `Initialized`, `ContainersReady`, `Ready` and their `lastTransitionTime`

Also practice:

```bash
kubectl -n lab get pods
kubectl -n lab describe pod <pod>
kubectl -n lab get events --sort-by=.lastTimestamp
kubectl -n lab logs <pod>
kubectl -n lab logs <pod> --previous
kubectl -n lab rollout status deploy/order-service
kubectl -n lab rollout history deploy/order-service
```

## Incident labs

Read `incidents/README.md`. There are 20 scenarios covering NotReady, liveness restart, CrashLoopBackOff, OOMKilled, CPU throttling, one-bad-instance, heap/GC pressure, thread saturation, Hikari exhaustion, slow SQL/DB locks, Kafka lag, downstream timeout, retry amplification, graceful shutdown, bad rollout and DNS/service discovery.

Some are automated:

```bash
./scripts/incident.sh 01 trigger   # one order pod NotReady
./scripts/incident.sh 01 reset

./scripts/incident.sh 07 trigger   # one slow order pod
./scripts/incident.sh 07 reset

./scripts/incident.sh 15 trigger   # stop payment consumers -> Kafka lag
./scripts/incident.sh 15 reset
```

## How to investigate

Do not start with `grep ERROR` every time.

```text
1. Confirm symptom/time window
2. Scope: endpoint -> service -> pod
3. Kubernetes state: desired/ready/restarts/events/rollout
4. RED: Rate / Errors / Duration
5. Saturation: CPU, memory, GC, threads, Hikari, Kafka lag
6. Trace: which span/downstream is slow?
7. Logs: explain the detailed failure
8. Recover and verify with the same signal
```

Detailed material:
- `docs/learning-path.md`
- `docs/troubleshooting-playbook.md`
- `incidents/README.md`

## CI proof

The repository has two workflows:
- **Fast CI** — Maven reactor tests + shell syntax.
- **kind Runtime Smoke** — builds all five Docker images, creates the kind cluster, deploys all core workloads, asserts the expected replicas, creates an order, and waits for it to become `CONFIRMED` through Inventory → Kafka → Payment.

## Tear down

```bash
./scripts/lab.sh down
```
