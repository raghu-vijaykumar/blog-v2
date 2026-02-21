---
title: Health, Slow Start, Circuit Breaking, Retries, Pooling
---

# Health and resilience

Build reliability by detecting problems early and limiting blast radius.

## Health checks
- Active: periodic HTTP/TCP checks; require multiple successes/failures to flip.
- Passive: observe 5xx/timeouts and mark hosts unhealthy temporarily.
- Outlier detection: eject statistically bad performers for a base ejection time.

## Slow start (warmup)
- Gradually ramp traffic to new/rehabilitated instances to avoid cold caches/JIT spikes.
- Combine with readiness probes so traffic begins only when the app is ready.

## Circuit breaking
- Limits per upstream: max connections, max pending requests, max requests.
- Trip conditions: consecutive 5xx, high error fraction, timeouts.
- Goal: protect upstreams and callers from cascading failures.

## Retries (with budgets)
- Use per-try and overall timeouts; cap retries by a budget (e.g., ≤10% of original RPS).
- Prefer retryable idempotent methods; add jittered backoff; avoid retry storms.
- Consider hedging cautiously for high percentiles, with token and budget controls.

## Connection pooling
- Reuse HTTP/1.1 keep-alive or HTTP/2 multiplexed connections; tune max streams.
- Monitor pool saturation and queue times.

## Production checklist
- Define health check paths that exercise critical dependencies.
- Enable outlier detection and slow start by default.
- Set circuit breaker thresholds and retry budgets per route.
- Instrument: success/failure, ejection counts, retry rates, queue latency.
