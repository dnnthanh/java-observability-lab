# Java Observability Lab

Production-style learning lab for **Java/Spring Boot + Kubernetes observability**.

## Business flow

```text
client
  -> edge-service (2 pods)
  -> order-service (3 pods)
       -> inventory-service (2 pods) -> PostgreSQL
       -> Kafka orders.created
            -> payment-worker (2 pods)
                 -> payment-provider-simulator (2 pods)
                 -> PostgreSQL
                 -> Kafka payments.completed
                      -> order-service -> CONFIRMED
```

The goal is not CRUD. The goal is to learn how to answer production questions such as:

- When was a pod created, started and marked Ready?
- Why is a pod Running but 0/1 Ready?
- Which one of three replicas is slow or returning 5xx?
- Was a restart caused by liveness, application crash or OOMKilled?
- Is latency caused by JVM GC, servlet threads, Hikari, PostgreSQL, Kafka lag or a downstream service?
- How do metrics, logs and traces point to the same request/pod?

## Source modules

- `common/` – correlation/access logging, lab fault controls, health indicators.
- `services/edge-service/`
- `services/order-service/`
- `services/inventory-service/`
- `services/payment-worker/`
- `services/payment-provider-simulator/`

Infrastructure, observability stack, incident labs and one-command scripts are being materialized in the next commit. The Java source is intentionally committed as normal files (no repository-import chunks).
