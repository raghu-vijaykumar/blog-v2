---
title: Models and Algorithms
description: Fixed/sliding windows, token and leaky buckets, and GCRA with definitions, trade-offs, pseudocode, and worked examples.
---

## Overview
Rate limiting algorithms shape how requests are admitted over time. The right model smooths bursts, preserves fairness, and protects downstream latency with minimal state and compute.

## What, Why, When (and when-not)
What
- Counting- or credit-based policies that admit at most R requests per time and optionally a short burst B.

Why
- Prevent overload, smooth bursts, and ensure stable tail latency while allowing controlled short-term spikes.

When
- Any external API tier, shared microservice, or consumer of rate-bounded dependencies (e.g., databases, third-party APIs).

When-not
- In-process, single-tenant tasks where simple concurrency caps alone suffice and accuracy/time-window semantics add overhead.

## Core concepts and variants
- Fixed window: count in discrete windows [t, t+W). Cheap, but suffers boundary bursts.
- Sliding window: count over last W in continuous time or sub-bucket aggregation. Smoother but more state.
- Token bucket: tokens accrue at rate r, up to burst b. Consumes on admit; allows short bursts with bounded long-term rate.
- Leaky bucket: queue with constant drain d. Enforces steady departure; overflow drops. Equivalent to token bucket at limits, but queue-oriented.
- GCRA (credit-based): stores a single “theoretical arrival time” (TAT). Precisely spaces admits with minimal state and tunable burst tolerance.

## Design decisions and trade-offs
- State and CPU: sliding windows store more samples; token bucket and GCRA store O(1) per key.
- Smoothness: fixed windows are bursty; sliding and GCRA are smoother; token bucket allows configured burst.
- Clock dependence: window- and GCRA-based methods need stable time; use monotonic clocks and tolerate skew.
- Cost weighting: multi-token admits support expensive operations (e.g., writes = 2 tokens, reads = 1).

## Algorithms and policies (conceptual)
Pseudocode: GCRA (≤ 25 lines)
```pseudo
params: I = inter-arrival time (1/rate), tau = burst tolerance (seconds)
state: TAT = 0  # theoretical arrival time
function admit(now):
  if now < TAT - tau:
    return DENY, retry_after=(TAT - tau - now)
  # admit and update TAT
  TAT = max(TAT, now) + I
  return ALLOW
```

Pseudocode: Sliding window with sub-buckets (≤ 25 lines)
```pseudo
params: W = window (sec), N = buckets, limit = L
state: buckets[N], last_idx, last_ts
function admit(now):
  idx = floor(now / (W/N)) mod N
  if idx != last_idx:
    # advance and clear skipped buckets
    steps = (idx - last_idx + N) mod N
    for i in 1..steps:
      buckets[(last_idx + i) mod N] = 0
    last_idx = idx
  total = sum(buckets)
  if total >= L:
    return DENY
  buckets[idx] += 1
  return ALLOW
```

## Architecture and components
- Use in-memory O(1) state for instance-local enforcement; shard by key for hot tenants.
- Use centralized stores (Redis, DynamoDB) when global cross-instance fairness is required.

## Operational considerations
- Choose burst b to absorb expected jitter while preserving tail latency budgets.
- Prefer GCRA or token bucket for edge enforcement at very high QPS; sliding windows for analytics-friendly semantics.

## Examples
Example A (quantitative): Boundary burst in fixed windows
- Limit = 100 rps, W = 1s. A client can send 100 requests at t=0.99s and 100 at t=1.01s ⇒ 200 within 0.02s. Sliding or token bucket reduces this spike.

Example B (architectural): Choosing GCRA at the edge
- An API gateway handles 200k rps with per-tenant fairness. GCRA stores 1 timestamp per tenant key and avoids per-request counter writes to Redis, fitting L3 cache and minimizing tail latency.

## Edge cases and anti-patterns
- Using fixed windows for burst-sensitive paths; switch to token bucket or GCRA.
- Ignoring multi-token costs for expensive endpoints; they will dominate capacity unexpectedly.

## Interactions with adjacent topics
- [Retries & Idempotency](./05-retries-idempotency-and-client-behavior.md): rate limiting influences retry strategy and jitter.
- [Concurrency Limits](./06-concurrency-limits-queues-and-admission-control.md): combine rate and concurrency to bound both throughput and in-flight work.

## Production checklist
- Pick algorithm per scope: token bucket/GCRA for edge, sliding for analytics-like semantics.
- Configure rate r and burst b from capacity; use monotonic time.
- Add weighted costs for heavy endpoints.

## Interview framing checklist
- Compare token bucket, leaky bucket, sliding window, and GCRA; when to use each?
- How to encode burst tolerance while preserving SLOs?

## References
- Redis-cell and GCRA references; Nginx/Envoy docs; RFC 6585/9110 timing semantics
