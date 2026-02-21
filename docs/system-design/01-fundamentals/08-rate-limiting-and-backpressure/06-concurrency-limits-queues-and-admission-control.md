---
title: Concurrency Limits, Queues, and Admission Control
description: Bounding in-flight work with semaphores, queueing strategies, and queue-time admission to protect latency SLOs and prevent overload collapse.
---

## Overview
Concurrency limits cap the number of simultaneous requests a service processes, while queues buffer short bursts. Admission control decides when to reject early to keep queueing delay within SLOs.

## What, Why, When (and when-not)
What
- Permits/semaphores for in-flight caps; bounded queues for short-term absorption; policies to 429 when queue wait would break SLO.

Why
- Prevent thread/connection pool exhaustion, GC pressure, and tail-latency blowups; stabilize throughput under bursty input.

When
- Services with heavy CPU/IO per request, tight resource pools (DB conn, workers), or user-facing SLOs.

When-not
- Fire-and-forget pipelines where backpressure is applied elsewhere and latency budgets are not strict.

## Core concepts and variants
- Concurrency caps: fixed N, adaptive (AIMD), gradient-based (e.g., Netflix Concurrency Limits), per-tenant permits.
- Queues: FIFO, priority queues (by class/tenant), deadline-aware queues to drop expired work.
- Admission policies: queue-time budget, max queue depth, moving percentiles (P95) thresholds, per-tenant caps.

## Design decisions and trade-offs
- Throughput vs latency: deeper queues increase utilization but worsen tail; prefer small bounded queues with early rejects.
- Fairness: single global queue can starve small tenants; use per-tenant queues or weighted fair queuing.
- Adaptation: static limits are simple but brittle; adaptive limits track latency and failure to adjust permits.

## Algorithms and policies (conceptual)
Pseudocode: semaphore with queue-time admission (≤ 25 lines)
```pseudo
semaphore permits = N
queue Q
function admit(req):
  now = clock()
  if permits.try_acquire():
    req.start_ts = now
    return ALLOW
  if Q.size >= Q_MAX:
    return DENY
  Q.push({req, enq_ts: now})
  return QUEUED

function on_permit_released():
  if Q.empty():
    permits.release()
    return
  item = Q.pop()
  wait = clock() - item.enq_ts
  if wait > QUEUE_BUDGET:
    drop(item.req)
    on_permit_released()  # try next
  else:
    permits.acquire()  # take the permit
    start(item.req)
```

Adaptive concurrency (outline)
- Increase permits if recent P95 latency < target and error rate low; decrease on spikes (AIMD/gradient-based).

## Architecture and components
- Admission middleware at service ingress; per-tenant queues or buckets for fairness; integration with circuit breakers and rate limiters.
- Queue metrics exported (depth, age, drops) and tied to autoscaling.

## Operational considerations
- Set QUEUE_BUDGET = SLO_total − P95(service) − network_budget; keep queues small (O(10–100) per instance).
- Use deadlines/timeouts per request; cancel downstream calls when admission fails or deadlines reached.
- Prefer backpressure signals over silent delays; return 429 with Retry-After when rejecting.

## Examples
Example A (quantitative): Choosing N and queue budget
- If a worker handles 50 rps with P95=40 ms CPU and 40 ms IO (total ~80 ms), Little’s Law suggests N ≈ λ × W = 50 × 0.08 = 4 permits per core. With 8 cores: N≈32. Queue budget = 200 ms SLO − 80 ms proc − 30 ms network = 90 ms.

Example B (architectural): Priority queues for interactive vs batch
- Interactive requests go to a small, high-priority queue with stricter admission; batch jobs use a larger, low-priority queue that sheds first under load.

## Edge cases and anti-patterns
- Unbounded queues hide overload and cause timeouts; always bound queues and reject early.
- Single global queue for multi-tenant systems causes starvation; use per-tenant or weighted queues.

## Interactions with adjacent topics
- [Backpressure & Shedding](./04-backpressure-signals-and-load-shedding.md): thresholds drive shedding and breaker states.
- [Models & Algorithms](./01-models-and-algorithms.md): combine throughput limits with concurrency caps for robust protection.

## Production checklist
- Set static or adaptive concurrency per instance; bound queues and set queue-time budgets.
- Implement per-tenant or class-based queues if multi-tenant.
- Emit metrics: in-flight, queue depth/age, admissions/denies, P95/99 latency; autoscale on sustained saturation.

## Interview framing checklist
- How to choose concurrency limits and queue budgets for a latency SLO service?
- How to avoid starvation in multi-tenant queues?

## References
- Netflix “Concurrency Limits” and gradient-based controllers; Google SRE on overload and queueing
