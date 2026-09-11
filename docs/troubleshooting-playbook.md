# Production troubleshooting playbook

1. Confirm the symptom and exact time window.
2. Scope the blast radius: endpoint, service, deployment, or one pod.
3. Check desired/Ready replicas, restarts, termination reason, events and rollout history.
4. Check RED: Rate, Errors, Duration.
5. Check saturation: CPU throttling, memory, JVM heap/GC, servlet threads, Hikari pending, Kafka lag.
6. Use tracing to locate the slow span/downstream.
7. Use logs to explain detail, not as the only first signal.
8. Recover safely.
9. Verify with the same signal that proved the incident.
10. Capture a timeline.

```bash
kubectl -n lab get deploy,pods -o wide
kubectl -n lab get events --sort-by=.lastTimestamp
kubectl -n lab describe pod <pod>
kubectl -n lab logs <pod> --previous
kubectl -n lab rollout history deploy/order-service
./scripts/lab.sh evidence
```
