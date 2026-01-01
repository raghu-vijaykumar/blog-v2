+++
title= "URL Shortner"
tags = [ "system-design", "software-architecture", "interview", "url-shortner" ]
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
weight= 7
bookFlatSection= true
+++

---

## Design URL Shortener

### Problem Statement
The system provides a URL shortening service that converts long URLs into concise short URLs for easy sharing and tracking. It handles high-volume requests for URL creation and redirection, ensuring quick response times and reliable access to original URLs.

### Requirements

#### Functional Requirements
- Shorten long URLs into unique, concise short URLs.
- Redirect users from short URLs to original long URLs.
- Support base62-encoded alphanumeric short URLs (7 characters for scalability).

#### Non-Functional Requirements
- High availability with minimal downtime.
- Low latency (`<100ms`) for shorten and redirect operations.
- High throughput to handle peak loads (1200 writes/sec, 12k reads/sec). ^[Assumption: Based on 100M URLs/day.]
- Fault-tolerant with automatic ID generation to prevent collisions.

### Key Constraints & Assumptions
- **Scale assumptions**: 100M URLs created/day (~1200/sec writes), 10:1 read/write ratio (~12k/sec reads); global user base expecting 365B total URLs over 10 years. ^[Assumption: Linear growth after 10 years.]
- **SLA**: 99.99% availability, p99 latency `<100ms`; 301 redirects for SEO/performance.
- **URL length**: Short URLs use 7 Base62 characters (62 possible chars: A-Z, a-z, 0-9), supporting ~3.5T unique URLs.
- **Data retention**: URLs persisted indefinitely; access patterns read-heavy (90% cache hit rate assumed). ^[Assumption: Standard for link sharing.]

### High-Level Design
The system uses a distributed architecture with load balancers, stateless service instances, and a NoSQL database for storage. Key components: API Gateway for traffic handling, shortener service for logic, token service for unique ID generation, cache for frequent lookups, and database for persistence.

```
graph TD
    A[Client Request] --> B[Load Balancer]
    B --> C[API Gateway]
    C --> D[Short URL Service]
    D --> E[Token Service]
    D --> F[Database/Storage]
    D --> G[Cache Layer]
    H[Client Redirect] --> I[Short URL Service]
    I --> G
    I --> F
    I --> J[Redirect to Long URL]
```

^[Mermaid diagram illustrating request flow from shortening to redirection.]

### Data Model
- **URL Mapping**: Key-value store with short_url as key, containing `{long_url, created_at, expires_at}`.
- **Storage Choice**: NoSQL database (e.g., Cassandra) for high write throughput, wide columns for analytics; partitioned by short URL hash for distribution.
- **Schema Sketch**: Table `url_mappings` - short_url (string, primary), long_url (string), created_at (timestamp), hits (counter), user_id (optional).

### API Design
RESTful endpoints for core operations:

- **POST /api/v1/shorten** - Shorten URL. Request: `{"longUrl": "https://example.com/very/long/url"}`; Response: `{"shortUrl": "http://short.ly/AbCdEf7", "status": "success"}`.
- **GET /api/v1/{shortUrl}** - Redirect short URL. Response: HTTP 301 redirect to long URL or 404 if invalid.
- **GET /api/v1/analytics/{shortUrl}** - Optional analytics. Response: `{"hits": 12345, "created": "2023-01-01"}`. ^[Assumed authentication via API key.]

^[APIs support JSON payloads; redirects use HTTP 301 for permanence.]

### Detailed Design
- **API Gateway & Service**: Stateless shortener service generates unique IDs via Base62 encoding; invokes token service for ID ranges.
- **Token Service**: Distributes non-overlapping number ranges to instances, ensuring global uniqueness without central coordination.
- **Database**: Cassandra nodes sharded globally; writes append new mappings, reads via primary key.
- **Caching**: Redis layer caches recent mappings; ~90% read hit rate from cache to reduce DB load.
- **Redirect Logic**: Cache-first lookup, fallback to DB; increments hit counter asynchronously.
- **Technology Choices**: Cassandra over MySQL for write-heavy loads; Redis for low-latency caching; Base62 over hashing to eliminate collisions at scale.

### Scalability & Bottlenecks
- **Horizontal Scaling**: Add service instances behind LB; database auto-scales with new nodes/shards.
- **Sharding & Partitioning**: URLs partitioned by hash for even load; consistent hashing minimizes rebalancing.
- **Caching & Replication**: Multi-tier caching (edge/CDN for global users); DB replicas for read availability.
- **Load Balancing**: Round-robin or least-connections at LB; auto-scaling based on CPU/memory.
- **Bottlenecks**: DB writes bottleneck at peak; mitigated by batching hits counter. Cache misses increase DB load; edge caches help.

### Trade-offs & Alternatives
- **Base62 vs. Hashing**: Base62 ensures no collisions (sequence-based) vs. simpler hashing but requires collision retries (risk of duplicates).
- **Cassandra vs. DynamoDB**: Cassandra preferred for open-source control vs. DynamoDB's managed ease; both handle scale but Cassandra cheaper.
- **301 vs. 302 Redirects**: 301 cached client-side (faster subsequent access, better SEO) vs. 302 tracks every hit (slower, maintains analytics).
- **Caching depth**: Extensive caching trades memory for performance vs. minimal caching for simplicity.

### Future Improvements
- Add custom short URLs with vanity slugs.
- Implement expiration dates with cleanup jobs.
- Integrate analytics dashboard for user engagement metrics.
- Support bulk shortening via batch APIs.
- Migrate to serverless for cost-effective scaling.

### Interview Talking Points
1. Explain Base62 encoding benefits: collision-free via unique IDs vs. hashing fragility at scale.
2. Discuss token service design: decentralized ID ranges ensure scalability without locks.
3. Compare redirect status codes: 301 for performance/SEO vs. 302 for analytics.
4. Highlight caching strategy: read-heavy loads demand multi-level (edge + in-memory) to avoid DB thrashing.
5. Address collision prevention: centralized ID generator would be bottleneck; distributed ranges better.
6. Trade costs: NoSQL for writes vs. SQL simplicity; designed for 99% cache hits.
7. Scalability: Partitioning + consistent hashing allows seamless growth.
8. Assumptions impact: URL length calculation ensures years-ahead capacity.
