---
title: Consistency, Coherence, Invalidation
---

# Consistency, coherence, and invalidation

Models
- Read-your-write vs eventual consistency trade-offs
- Monotonic reads, causal consistency expectations for user-facing data

Invalidation strategies
- Write-through invalidation at the point of truth
- Pub/Sub fanout to invalidate many cache nodes
- Versioned keys and namespace busting on deploys

Java — Redis pub/sub invalidation (simplified)
```java
import redis.clients.jedis.*;

// Publisher on write
try (Jedis j = new Jedis("localhost", 6379)) {
  String key = "user:v1:123";
  j.del(key);
  j.publish("invalidate", key);
}

// Subscriber process
new Thread(() -> {
  try (Jedis j = new Jedis("localhost", 6379)) {
    j.subscribe(new JedisPubSub(){
      @Override public void onMessage(String ch, String msg){ j.del(msg); }
    }, "invalidate");
  }
}).start();
```

Python — Redis pub/sub invalidation
```python
import redis
r = redis.Redis(host='localhost', port=6379)

def on_write(user_id):
    key = f"user:v1:{user_id}"
    r.delete(key)
    r.publish('invalidate', key)

def subscriber():
    pubsub = r.pubsub()
    pubsub.subscribe('invalidate')
    for m in pubsub.listen():
        if m['type'] == 'message':
            r.delete(m['data'].decode())
```

Deploys and schema changes
- Bump key namespace version (e.g., user:v2:...) during deploys
- Optional shadow cache to warm new keys before flip
