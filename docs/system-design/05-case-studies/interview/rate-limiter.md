+++
title= "Rate Limiter"
tags = [ "system-design", "software-architecture", "interview", "rate-limiter" ]
author = "Me"
showToc = true
TocOpen = false
draft = false
hidemeta = false
comments = false
disableShare = false
disableHLJS = false
hideSummary = false
searchHidden = true
ShowReadingTime = true
ShowBreadCrumbs = true
ShowPostNavLinks = true
ShowWordCount = true
ShowRssButtonInSectionTermList = true
UseHugoToc = true
weight= 3
bookFlatSection= true
+++

# Design a Rate Limiter

## Problem Statement

A rate limiter controls the rate of traffic sent from clients to a server, ensuring no more than a specified number of requests are processed within a given time window. Excess requests are dropped to protect the system from overload, abuse, and costs associated with downstream services.

## Requirements

### Functional Requirements
- Limit the number of requests based on configurable rules (e.g., by IP address, user ID, API endpoint).
- Support multiple rate-limiting algorithms (token bucket, sliding window, etc.).
- Return appropriate responses to clients when rate limits are exceeded (HTTP 429 Too Many Requests).
- Include headers in responses indicating remaining limits and retry times.
- Allow rate-limited requests to be queued or processed asynchronously if configured.

### Non-Functional Requirements
- Low latency (under 10ms per request check).
- Minimal memory usage across distributed instances.
- High availability and fault tolerance (e.g., continue functioning if cache instances fail).
- Horizontal scalability to handle millions of requests per second.
- Easy configuration and rule updates without downtime.

## Key Constraints & Assumptions
- **Scale**: Handles 10 million active users with peak traffic of 100,000 requests per second across multiple regions. *Assumption based on typical large-scale systems.*
- **Data Retention**: Rate-limiting data (counters, timestamps) retained for up to 24 hours. *Assumption for sliding window calculations.*
- **Latency SLA**: 99.9% of requests checked within 5ms to minimize impact on application response times.
- **Fault Tolerance**: System degrades gracefully; if Redis (cache) fails, fallback to in-memory with eventual consistency. *Assumption for distributed environments.*
- **Rate Limits**: Configurable per client type; e.g., anonymous users: 1000 requests/hour, authenticated: 10,000/hour. *Assumption based on common API practices.*

## High-Level Design

The system uses a distributed middleware architecture with rate limiters as stateless services querying a centralized cache for counters. Clients send requests to a load balancer, which routes them through the rate limiter middleware. If the rate limit is not exceeded, traffic passes to the application servers; otherwise, a 429 response is returned.

Key components:
- **Load Balancer**: Distributes requests across rate limiter instances.
- **Rate Limiter Instances**: Stateless services checking limits using algorithms like token bucket; query/update cache.
- **Centralized Cache (Redis Cluster)**: Stores rate-limiting counters and timestamps for distributed access.
- **Application Servers**: Handle validated requests.
- **Monitoring System**: Tracks dropped requests and performance metrics.

```mermaid
graph TD
    Client[Client] --> LB[Load Balancer]
    LB --> RL1[Rate Limiter Instance 1]
    LB --> RL2[Rate Limiter Instance 2]
    RL1 --> Cache[Redis Cluster]
    RL2 --> Cache
    RL1 --> AS[Application Servers]
    RL2 --> AS
    Cache --> AS
    RL1 -.->|Rejected: 429| Client
    RL2 -.->|Rejected: 429| Client
```

## Data Model

Key entities include rate-limiting rules and counters:

- **Rate Rule**:
  - `rule_id` (string): Unique identifier for the rule.
  - `key_type` (string): e.g., "user_id", "ip", "endpoint".
  - `algorithm_type` (string): e.g., "token_bucket", "sliding_window".
  - `bucket_size` (int): Tokens/bucket capacity.
  - `refill_rate` (int): Tokens per second/minute.
  - `window_size` (int): Time window in seconds.

Stored in a relational database (e.g., PostgreSQL) for persistence and easy updates.

- **Rate Counter** (per key, e.g., user_id):
  - `key` (string): Identifier (e.g., user ID).
  - `current_tokens` (int): Available tokens (for token bucket).
  - `last_refill` (timestamp): Last token refill time.
  - `request_log` (list of timestamps): Request timestamps for sliding window algorithms.

Stored in Redis for fast reads/writes: Hash keys like `rate_limit:{user_id}` with fields for counters and timestamps.

**Storage Choice**: Redis for counters due to its speed (in-memory) and atomic operations (e.g., INCR, EXPIRE). PostgreSQL for rules as they change infrequently and require ACID properties. *Redis chosen over databases for sub-millisecond performance in distributed rate limiting.*

## API Design

The rate limiter acts as middleware, so no public APIs typically exposed. Internally, instances use Redis commands.

Example interactions:

- **Check Rate** (internal to middleware):
  - Query Redis: `HGETALL rate_limit:{key}`; increment if allowed.
  - Lua script for atomicity: Ensures counter update and limit check in one operation.

- **Configured Responses**:
  - Success (200/OK): Forward request to backend.
  - Rate Limited (429 Too Many Requests):
    ```
    Headers:
      X-RateLimit-Remaining: 50
      X-RateLimit-Limit: 100
      X-RateLimit-Retry-After: 60

    Body:
      {"message": "Rate limit exceeded. Try again in 60 seconds."}
    ```

For management (optional):
- `GET /rules` - Fetch active rate rules.
- `POST /rules` - Update rules (e.g., change limit for a key).

## Detailed Design

### Components

- **Rate Limiter Service**: Implements algorithms (e.g., token bucket: refill tokens periodically, decrement on request). Deployed as containers or Kubernetes pods in a service mesh for auto-scaling.
- **Redis Cluster**: Sharded by key for even distribution (e.g., `CRC32(key) % num_shards`). Uses pub/sub for rule updates across instances.
- **Load Balancer**: Uses consistent hashing to route similar keys to the same rate limiter instance, reducing cache misses.
- **Fallback Mechanism**: If Redis unreachable, use local in-memory with exponential backoff and sync later.

**Algorithm Choice**: Token bucket for its simplicity and burst support. Parameters tuned via configuration; e.g., bucket size = window_size * refill_rate for smooth limits. *Token bucket chosen over sliding window for memory efficiency (no need to store all timestamps).*

**Technology Reasoning**:
- **Redis**: Chosen over alternatives (e.g., Memcached) for atomic operations and Lua scripting to prevent race conditions without locks.
- **Microservices**: Allows independent scaling of rate limiter from application.
- **Autoscaling**: Kubernetes HPA based on CPU/memory to handle traffic spikes.

## Scalability & Bottlenecks

- **Horizontal Scaling**: Add rate limiter instances behind load balancer; Redis shards auto-scale.
- **Sharding**: Partition keys across Redis nodes (by hash); hot keys (e.g., viral users) may need dedicated shards.
- **Caching**: Rules cached locally in rate limiter instances for faster access.
- **Load Balancing**: Distributes load evenly; sticky routing for same keys minimizes cross-instance calls.

**Bottlenecks**:
- Redis performance: Single instance caps at ~100K ops/sec; clustering mitigates.
- Network latency: Regional deployment reduces round-trip times.
- Hot keys: Monitor and apply per-key sharding to prevent skew.

## Trade-offs & Alternatives

- **Token Bucket vs. Sliding Window Log**:
  - Trade-off: Token bucket allows bursts but may over-allow near window edges; sliding log accurate but memory-intensive (stores every timestamp). *Chose token bucket for 99% of cases where precision isn't critical; sliding log for compliance-heavy systems.*
  
- **Centralized Redis vs. Distributed Cache**:
  - Trade-off: Redis simplifies synchronization but introduces single point of failure (mitigated by clustering); distributed (e.g., Couchbase) reduces latency but complicates consistency.

- **Server-side vs. Client-side Rate Limiting**:
  - Trade-off: Server-side reliable but increases server load; client-side reduces requests but easily bypassed. *Server-side preferred for security.*

Alternatives: Third-party services (e.g., Cloudflare Rate Limiting) for quick setup, but custom allows fine-tuned control.

## Future Improvements

- **Adaptive Limits**: Integrate ML to dynamically adjust limits based on user behavior or system health.
- **Multi-Region Replication**: Geo-distributed Redis for low-latency global support with active-active replication.
- **Analytics Dashboard**: Real-time metrics for rule optimization and abuse pattern detection.
- **Soft/Hard Limits**: Extend to soft limits allowing occasional overflows with billing/notification.
- **Rate Limits at L3/L7**: Add network-level (DDoS protection) or API-layer granularity.

## Interview Talking Points

1. **Why server-side over client-side?** Client unreliable/malleable; server ensures enforcement regardless of client.
2. **Handling distributed race conditions?** Use Redis Lua scripts for atomic operations, avoiding locks' performance hit.
3. **Algorithm choice impact?** Token bucket for bursts (e.g., Amazon use); sliding window for precision (e.g., compliance APIs).
4. **Scalability challenges?** Centralized state requires clustering/sharding; monitor hotspots.
5. **Fault tolerance?** Redis clustering with fallback to local state and event sync.
6. **Trade-offs in storage?** Redis for speed vs. RDBMS for rules' consistency.
7. **Monitoring what?** Drop rates, latency; adjust rules if over-blocking.
8. **Real-world examples?** Stripe uses token bucket; Twitter limits tweets/hour.
9. **Optimization techniques?** Sharding, local caching, eventual consistency for high throughput.
10. **Extensibility?** Pluggable algorithms, rule engines for diverse throttling needs.
