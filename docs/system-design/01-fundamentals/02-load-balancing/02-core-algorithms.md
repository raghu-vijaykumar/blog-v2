---
title: Core Load Balancing Algorithms
---

# Core load balancing algorithms

Understand strengths, weaknesses, and selection guidance for common algorithms.

## What and why
- Round robin (RR): simple, fair over time. Good defaults when instances are homogeneous.
- Weighted RR: reflect heterogeneity (bigger nodes get more traffic).
- Least-connections / least-requests: favor backends with fewer in-flight requests; good for variable request durations.
- Power of Two Choices (Po2): randomly sample two, pick the better. Near-optimal balancing with minimal overhead.
- Latency/EWMA-aware: pick backends with lower observed latency; adapts to real performance.
- Consistent hashing: route by key (e.g., user/tenant) to improve cache locality or maintain affinity without sticky state.

## How and when
- High variance in request time → prefer least-requests or Po2.
- Heterogeneous capacity → use weights with RR or least-requests.
- Stateful shards or cache locality → consistent hashing with virtual nodes.
- Tail latency sensitive → latency-aware with safeguards (outlier ejection).

## Pseudocode examples

Power of Two Choices (least-requests)
```python
import random

def choose_po2(backends):
    a, b = random.sample(backends, 2)
    return a if a.inflight <= b.inflight else b
```

EWMA latency-aware (min EWMA)
```python
def ewma_update(ewma, sample, alpha=0.2):
    return alpha * sample + (1 - alpha) * ewma

def choose_ewma(backends):
    return min(backends, key=lambda b: b.ewma_latency_ms)
```

Consistent hashing with virtual nodes (concept)
- Build a hash ring of virtual nodes for each backend.
- Hash key (e.g., `tenantId`) to the ring; walk clockwise to pick server.
- On membership change, only a small fraction of keys remap.

## Trade-offs and pitfalls
- Least-requests can oscillate with stale inflight counts; consider smoothing and Po2.
- Latency-aware can get stuck if one host appears best; add outlier detection and slow start.
- Consistent hashing reduces balancing flexibility; cap per-node load (bounded loads).

## Production checklist
- Pick algorithm per route/service; validate with synthetic and shadow traffic.
- Add weights reflecting real capacity; adjust during deploys.
- Add safeguards: outlier ejection, concurrency caps, and retry budgets.
