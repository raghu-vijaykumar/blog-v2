+++
title= "CDN"
tags = [ "system-design", "software-architecture", "interview", "cdn" ]
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
weight= 30
bookFlatSection= true
+++

# Design Content Delivery Network (CDN)

## Problem Statement
A Content Delivery Network (CDN) distributes static and streaming content (e.g., images, videos, HTML) to users worldwide with high performance and availability. It caches content at edge locations close to users to minimize latency, reduce origin server load, and ensure reliable delivery for billions of requests daily.

## Requirements

### Functional Requirements
- Cache and deliver static files and video content from edge servers
- Route requests to the nearest available edge server
- Invalidate cached content when origin changes
- Support secure content delivery via HTTPS

### Non-Functional Requirements
- High availability: 99.99% uptime with fault tolerance
- Low latency: `<100ms` response time for content delivery
- High throughput: Handle 100 million requests/second globally
- Security: DDoS protection and HTTPS encryption

## Key Constraints & Assumptions
- **Scale**: 1 billion active users globally, 50 TB of data ingested daily, 100 million RPS (assumption: ~100 requests per user per day spread across time zones)
- **Content**: Primarily static (updated weekly) and video (updated daily); dynamic content not cached
- **Latency SLA**: `<100ms` median response time; 99.9% of requests under 500ms
- **Assumptions**: Origin servers are managed externally; CDN handles distribution only; sufficient network bandwidth available; regional laws allow data replication

## High-Level Design

The CDN architecture consists of origin servers (content source), globally distributed edge servers (cache nodes), a DNS-based routing system, load balancers, and a management layer.

### Architecture Diagram
```mermaid
graph TD
    User[User] --> DNS[DNS Resolver]
    DNS --> LB[Global Load Balancer]
    LB --> Edge1[Edge Server 1<br/>e.g., US East]
    LB --> Edge2[Edge Server 2<br/>e.g., Europe]
    LB --> Edge3[Edge Server 3<br/>e.g., Asia]
    Edge1 --> Cache[Cache Layer<br/>Redis/Memcached]
    Edge2 --> Cache
    Edge3 --> Cache
    Cache --> Origin[Origin Servers<br/>S3/Storage]
    CMS[Content Management System] --> Edge1
    CMS --> Edge2
    CMS --> Edge3
```

**Workflow**: User requests content; DNS routes to nearest edge; edge checks cache (hit: serve directly; miss: fetch from origin, cache, serve). CMS handles invalidation.

## Data Model
- **Content Storage**: Binary files stored in object storage (e.g., S3-compatible) at origins; no complex relationships needed
- **Metadata Database**: NoSQL (e.g., DynamoDB) for content metadata
  - Key: content_id (string)
  - Fields: url (string), size (int), ttl (int), last_modified (timestamp), region (string, for sharding)

Store choice: NoSQL for high read throughput; blob storage for binary data to avoid RDBMS overhead.

## API Design
CDN is infrastructure-level, but key internal interfaces:

- **Cache Invalidation API** (for CMS/oracle-driven updates):
  - `POST /invalidate/{content_id}` - Removes specific content from all caches
  - `POST /invalidate/wildcard` - Batch invalidate by URL pattern

- **Metrics API** (for monitoring):
  - `GET /metrics/edge/{server_id}` - Returns cache hit rate, latency, bandwidth
No public user-facing APIs; requests are HTTP/HTTPS to content URLs.

## Detailed Design

### Edge Servers
Distributed globally across 100+ PoPs; each PoP has multiple servers. Use nginx/Varnish for caching with LRU eviction. Auto-scale with Kubernetes.

### Cache Layer
Multi-level: In-memory (Redis for hot data), disk for warm; policy: TTL-based with LRU fallbacks. Hit ratio target: 80%.

### Origins
Web servers or cloud storage; CDN pulls via signed requests. Sharded by content hash for load distribution.

### Load Balancing
DNS-based routing (e.g., Route53) for geography; within PoP, hardware LBs (e.g., AWS ELB). Anycast for routing efficiency.

### Content Management
Dashboard/API for upload invalidation; event-driven replication to edges via Kafka for log-based updates.

Technologies: Kafka for real-time updates (high throughput, persistence) vs RabbitMQ (simpler, less overhead; chosen for global scale).

## Scalability & Bottlenecks

- **Horizontal Scaling**: Add edge PoPs; auto-scale servers via containers; shard origins by content hash
- **Caching**: Redis cluster for high throughput; minimizes origin requests by 80-90%
- **Data Replication**: CDN-y (push-based) to edge caches; async replication for non-critical updates
- **Load Balancing**: Geo-DNS with Anycast IP; handles millions of DNS requests/sec
- **Bottlenecks**: DNN (Decentralized Network Nature) with free tier hamstrings scaling; DDOS if unprotected; mitigation via rate limiting and WAF

## Trade-offs & Alternatives
- **Cache vs Origin**: Higher cache hit ratio reduces latency but increases bandwidth costs; trade-off: 20% extra storage for 5x performance gain
- **Edge Proliferation**: More PoPs improve latency but raise ops costs/ complexity; assumption: 100 PoPs strike balance
- **Technology Choices**: NoSQL (high-write throughput) vs RDBMS (consistency for metadata); monolith CDN control plane vs microservices (faster dev but more orchestration)
- **Security**: Full TLS adds latency (~10-20ms); alternatives: QUIC for faster encryption setup

## Future Improvements
- Dynamic content caching (e.g., ESI for assembly)
- Edge compute (serverless at edge for personalization)
- Global sharding optimization
- AI-driven predictive caching
- Multi-CDN integration for redundancy

## Interview Talking Points
1. Scale estimation: Calculate edge server count based on RPS/data, assuming 10Gbps/link and 80% hit ratio
2. Cache invalidation strategies: Write-through vs lazy vs event-driven; trade-off consistency vs performance
3. Failure modes: Network partition between regions; mitigation via multi-region replication and health checks
4. Security depth: DDoS scrubbing with Cloudflare-like services; trade-off cost vs protection level
5. Cost optimization: Bandwidth > compute; prioritize high-hit ratio over large cache sizes
6. Monitoring criticals: Cache hit ratio drop, latency spikes, bandwidth patterns
7. Pool integration: How CDN interfaces with origins (signed URLs) and clients (redirects/responses)
8. Trade-offs: Global coverage vs consistency; eventual vs strong; capacity planning assumptions
