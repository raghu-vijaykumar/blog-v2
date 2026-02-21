---
title: Observability and Capacity Planning
---

# Observability and capacity planning

This page provides a practical telemetry model for caches (any layer) and a simple way to capacity‑plan without overfitting. It also includes code snippets to instrument hit/miss metrics.

See also:
- Multi‑level caching: ./06-multilevel-hierarchical.md
- Consistency & invalidation: ./05-consistency-invalidation.md
- Distributed caches: ./04-distributed-caches.md

## Core metrics to track (per layer)

- Request hit ratio (RHR)
  - hits / total requests; segment by route/resource. Watch both average and distribution over time.
- Byte hit ratio (BHR)
  - bytes served from cache / total bytes; captures big‑object wins that RHR misses.
- Revalidation rate
  - fraction of requests answered 304/not modified; should displace full misses where possible.
- Miss rate and refill latency
  - time from miss → fresh object available; coalescing effectiveness (how many requests piggybacked vs dogpiled).
- Tail latency (p95/p99) and error contribution
  - end‑to‑end, broken down by cache layer; track percentage of SLO/SLA budget consumed by cache vs origin.
- Evictions and admission decisions
  - eviction rate over time; if using LFU/TinyLFU, measure rejected candidates and false evictions.
- Memory & fragmentation (for Redis)
  - used_memory vs max, mem_fragmentation_ratio, key count, avg item size, top key sizes.
- Top‑N keys and hot‑key share
  - % of traffic accounted for by top keys; identify hot partitions and stampede candidates.

Minimum SLO‑aligned dashboard
- RHR/BHR (overall + top routes)
- Miss refill latency (avg/p95), refresh errors
- Tail latency p95/p99 (with cache‑hit/miss split)
- Evictions and memory headroom
- Hot‑key % and top‑key list (sampled)

## Capacity planning model (back‑of‑the‑envelope)

1) Define the staleness budget and TTLs (per ./01-foundations.md). Pick soft TTL + jitter for critical paths.
2) Estimate unique keys within TTL window:
   - Unique ≈ req_per_sec × miss_rate × TTL_seconds (adjust for skew with a multiplier 0.5–1.0 if heavy reuse)
3) Size memory:
   - RAM ≈ unique × avg_item_size_bytes × overhead_factor (1.2–2.0 typical)
4) Validate with traces: compare predicted unique vs observed cardinality and adjust.

Skew/Zipf awareness
- Track top‑N keys by request share; many systems see 80–95% of traffic on a small hot set.
- Size for covering the hot set (e.g., top keys that account for 90% of requests) rather than all unique keys.
- Consider admission policies (TinyLFU) to prevent cold one‑offs from evicting hot entries.

Scaling rules of thumb
- If miss refill latency dominates p95: add request coalescing and SWR before adding memory.
- If evictions spike and BHR/RHR fall: increase memory or split large values; check fragmentation.
- If hot‑key % is high: shard duplicates of the hot key or add L1 caches to fan‑out reads.

## Alerting/threshold guidance

- Sustained RHR drop >10% with stable traffic → investigate origin performance or invalidation storms.
- Miss refill p95 > 2× origin median for >5m → request coalescing broken or upstream slow.
- Evictions > 5% of ops for >10m → memory pressure or item size drift.
- Redis mem_fragmentation_ratio > 1.5 for >10m → investigate allocator behavior and large value patterns.
- Hot‑key share > 20% on a single key → implement duplication/sharding and coalescing.

## Instrumentation examples

Java (Micrometer) — hit/miss counters
```java
import io.micrometer.core.instrument.MeterRegistry;

class CacheMetrics {
  private final MeterRegistry reg;
  public CacheMetrics(MeterRegistry reg){ this.reg = reg; }
  public void hit(){ reg.counter("cache_hits").increment(); }
  public void miss(){ reg.counter("cache_misses").increment(); }
}
```

Python (prometheus_client) — hit/miss counters
```python
from prometheus_client import Counter
cache_hits = Counter('cache_hits', 'Cache hits')
cache_misses = Counter('cache_misses', 'Cache misses')

def get(key, r):
    val = r.get(key)
    if val is not None:
        cache_hits.inc()
        return val
    cache_misses.inc()
    return None
```

Redis observability commands (ops/on-call)
```text
INFO memory          # fragmentation, used vs max
INFO stats           # keyspace hits/misses, ops/sec
SLOWLOG GET 128      # slow commands
LATENCY DOCTOR       # latency spikes analysis
redis-cli --hotkeys  # sampling mode for hot keys (production caution)
```

## Capacity worksheet (example)

- Traffic: 500 RPS; miss rate target 20%; TTL 300s; avg 3 KB/item; overhead 1.5.
- Unique ≈ 500 × 0.2 × 300 = 30,000 keys.
- RAM ≈ 30,000 × 3 KB × 1.5 ≈ 135 MB.
- With Zipf skew covering 90% in top 10k keys, a 64–128 MB Redis LRU with TinyLFU admission can still achieve high RHR if stampedes are controlled.
