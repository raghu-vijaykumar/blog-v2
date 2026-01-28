---
title: Observability and Capacity Planning
---

# Observability and capacity planning

Metrics
- Request and byte hit ratio, revalidation rate, evictions, origin offload
- Tail latency (p95/p99) and error budget impact

Capacity model
- Working set ≈ arrival rate × TTL × average item size (adjust for skew)
- Watch cardinality and object size distribution (p50/p95)

Skew/Zipf awareness
- Track top‑N keys by request share; many systems see 80–95% of traffic on a small hot set.
- Size memory for the hot set you want to cover (e.g., top keys covering 90% of requests) rather than all unique keys.
- Monitor hot‑key percentage, stampede risk on those keys, and consider admission policies (TinyLFU) so cold one‑offs don’t evict hot entries.

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

def get(key):
    val = r.get(key)
    if val:
        cache_hits.inc(); return val
    cache_misses.inc(); return None
```
