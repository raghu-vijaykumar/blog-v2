---
title: Distributed Caches (Redis/Memcached)
---

# Distributed caches in production

Topologies
- Redis standalone, Sentinel HA, Redis Cluster sharding (hash slots), consistent hashing
- Replication and failover behavior, read replicas for heavy read traffic

Persistence and durability
- RDB snapshots, AOF append-only; fsync policies (everysec vs always)

Memory management
- Eviction policies: noeviction, allkeys-*, volatile-*
- Fragmentation and memory sizing; avoid oversized values

Hot key mitigation
- Key hashing, replicated reads, local + remote hybrid caches, rate limiting

Java (Jedis) — connection pool and pipelining
```java
import redis.clients.jedis.*;
import java.util.List;

JedisPoolConfig cfg = new JedisPoolConfig();
cfg.setMaxTotal(64);
cfg.setMaxIdle(16);
try (JedisPool pool = new JedisPool(cfg, "localhost", 6379);
     Jedis j = pool.getResource()) {
  Pipeline p = j.pipelined();
  for (int i=0; i<100; i++) {
    p.incr("counter:"+i);
  }
  List<Object> res = p.syncAndReturnAll();
}
```

Python (redis-py) — connection pool and pipeline
```python
import redis
pool = redis.ConnectionPool(host='localhost', port=6379, max_connections=64)
r = redis.Redis(connection_pool=pool)
pipe = r.pipeline()
for i in range(100):
    pipe.incr(f"counter:{i}")
results = pipe.execute()
```

Operational tips
- Set sane timeouts (connect/read), enable TCP keepalive
- Monitor slowlog, ops/sec, hit ratio, evictions, memory fragmentation
- Backpressure: fail fast on saturation to protect origin
