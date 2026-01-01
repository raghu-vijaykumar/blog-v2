+++
title= "Search Autocomplete"
tags = [ "system-design", "software-architecture", "interview", "search-autocomplete" ]
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
weight= 10
bookFlatSection= true
+++

# Design Search Autocomplete System

## Problem Statement

Design a type-ahead or autocomplete system for a search engine that provides relevant query suggestions as users type partial queries, based on trending queries from the last 24 hours, to improve search efficiency and user experience.

## Requirements

### Functional Requirements
- Provide up to 10 autocomplete suggestions for a given prefix input.
- Suggestions must be based on the most popular queries from the last 24 hours (trending queries).
- Support English alphabet, case-insensitive (convert to lowercase), maximum prefix length of 60 characters.
- No support for spell checking or typo correction.

### Non-Functional Requirements
- Handle billions of search queries daily.
- Respond to autocomplete requests within 240 milliseconds.
- Suggestions can be up to 1 hour stale.
- Eventual consistency is acceptable (different users may see slightly different or reordered suggestions at the same time).

## Key Constraints & Assumptions
- Daily query volume: Billions (assumed 2 billion queries/day for estimation).
- Average QPS for autocomplete: ~50,000 (given daily volume and typical traffic patterns).
- Data retention: Last 24 hours of queries for trend analysis.
- Storage: Massive dataset, so sharded key-value store for efficient lookups.
- Latency SLA: 240ms end-to-end for autocomplete response.
- Availability: High availability with eventual consistency.
- Assumption: No personalized suggestions; uniform across users.

## High-Level Design

The system uses a distributed microservices architecture with separate services for serving autocomplete requests and updating suggestions via batch processing. User search queries are logged and processed periodically to aggregate trending prefixes.

Key components:
- **Autocomplete Service**: Handles user requests, queries key-value store for prefix mappings, returns top suggestions.
- **Updater Service**: Logs search queries, runs batch processing (e.g., MapReduce) to compute top queries per prefix.
- **Big Data Pipeline**: Processes query logs to generate up-to-date prefix-to-suggestions mappings.
- **Distributed File System**: Stores raw query data and processed results.
- **Message Broker**: For change data capture (CDC) to sync updates.

```mermaid
graph TB
    A[User] --> B[Load Balancer]
    B --> C[Autocomplete Service]
    C --> D[Key-Value Store<br/>Prefix -> Sorted Suggestions]
    A --> E[Search Service]
    E --> F[Updater Service<br/>Logs Queries]
    F --> G[Distributed File System<br/>Raw Query Logs]
    G --> H[Batch Processing<br/>MapReduce Pipeline]
    H --> I[Processed Mappings<br/>Updated Suggestions]
    I --> J[Message Broker<br/>CDC Updates]
    J --> D
```

## Data Model

- **Raw Query Data**: Stored in distributed file system (e.g., HDFS or S3-compatible).
  - Schema: `{query: string, timestamp: long, popularity: int (default 1)}`
- **Filtered Queries**: Subset of queries from last 24 hours.
- **Prefix Mappings**: Key-value pairs in sharded store.
  - Key: Prefix string (lowercase).
  - Value: Sorted list of top 10 queries (by popularity), stored as JSON array.
- Storage Choice: Sharded key-value store (e.g., Redis Cluster or DynamoDB) for O(1) lookups; NoSQL for scalability.

## API Design

1. **Autocomplete Endpoint**:
   - **Request**: `GET /complete?prefix=<prefix>&limit=10`
   - **Response**: 
     ```json
     {
       "suggestions": ["query1", "query2", ...]
     }
     ```
   - Description: Returns top 10 suggestions for the given prefix, sorted by popularity.

2. **Search Endpoint** (referenced for query logging):
   - **Request**: `GET /search?query=<query>`
   - **Response**: Search results (implementation not detailed here, but triggers query logging).
   - Note: URL encoding required for special characters/spaces.

## Detailed Design

### Autocomplete Service
- Deployed as microservice behind load balancer with multiple instances for horizontal scaling.
- On request: Parse prefix (lowercase, validate length), query KV store, return cached suggestions.
- In-memory caching (local or Redis) for frequently accessed prefixes to meet 240ms latency.
- Data Structure: Sharded KV store with replication for read load balancing.

### Updater Service
- Logs every search query with timestamp to distributed FS.
- Batch pipeline runs every 30-60 minutes to process recent queries.

### Batch Processing Pipeline (MapReduce-style)
- **Map Stage**: For each query, emit all prefixes (e.g., for "hello world": "h", "he", ..., "hello world").
  - Filter: Only last 24 hours.
- **Reduce Stage**: Aggregate by prefix, count popularity, sort top 10 queries.
- **Output**: Overwrite KV store mappings.
- Optimizations: Sampling for high-volume prefixes; parallel processing across clusters.

### Data Synchronization
- **CDC**: Monitors KV store changes via message broker (e.g., Kafka).
- Autocomplete service subscribes and updates in-memory cache.
- Ensures near-real-time propagation without full batch delays.

## Scalability & Bottlenecks

- **Scalability**:
  - Horizontal scaling for services via load balancers and auto-scaling.
  - Database sharding with consistent hashing to distribute prefix keys across nodes.
  - Replication for read-heavy workloads (writes via batch).
  - Batch processing scales with more workers and distributed FS partitioning.

- **Bottlenecks & Solutions**:
  - High QPS on KV store: Shard and replicate reads; use caching layers.
  - Batch lag: Schedule more frequently (e.g., every 15 min), but balance with compute cost.
  - Data volume: Distributed FS handles petabytes; compression and partitioning reduce I/O.
  - Geographic scaling: Deploy in multiple regions with geo-DNS routing for low latency.
  - Monitoring: Track QPS, CPU, memory, and latency; auto-scale instances.

## Trade-offs & Alternatives

- **Trie vs. KV Store**: Trie provides fast prefix traversal but is memory-intensive and hard to scale/distribute. KV store (with batch updates) allows distributed scaling at cost of slower updates.
- **Real-time vs. Batch Updates**: Real-time (e.g., via streaming) ensures fresher data but increases complexity/cost; batch processing is simpler but introduces lag (acceptable per requirements).
- **CQRS Pattern**: Separates read (fast KV lookups) and write (batch aggregation) for optimized performance.
- **Single vs. Distributed DB**: Single DB limits scalability; distributed (with sharding) handles scale but adds operational complexity (e.g., replication lag).
- **Personalization**: Not implemented (uniform suggestions); could add user context but increases storage/query complexity.

## Future Improvements

- **Personalization**: Integrate user history for tailored suggestions (requires ML and additional storage).
- **Multi-language Support**: Extend to non-English alphabets (unicode handling).
- **Real-time Updates**: Use streaming processing (e.g., Kafka Streams, Spark Streaming) for sub-hour freshness.
- **Spell Correction**: Add fuzzy matching with edit distance algorithms.
- **Analytics**: Add metrics on suggestion click-through rates for better ranking.
- **Global Distribution**: Enhance cross-region replication and CDN for faster global responses.

## Interview Talking Points

1. Why batch processing over real-time for updates? Balances freshness with performance/scalability; eventuality consistency fits requirements.
2. Sharding strategy for KV store: Distribute prefixes to avoid hotspots; use hash-based partitioning.
3. How to handle high read QPS? Caching, replication, and load balancing across replicas.
4. Why not Trie? Excellent for small scales but memory/compute intensive at billions of queries; KV with precomputed suggestions is more scalable.
5. Latency optimization: In-memory caching, fast KV lookups, and geographic proximity.
6. Data volume estimation: Billions queries/day; focus on retention (24h), aggregation to reduce storage needs.
7. Failure handling: Replication ensures availability; CDC syncs data across regions.
8. Trade-offs in design: Batch vs. real-time; consistency vs. performance; detailed in trade-offs section.
9. Monitoring/KPIs: Track response time (`<240ms`), update lag (`<1h`), query coverage (suggestions hit rate).
10. Extensions: Real-time streams for fresher data; ML for ranking/reranking suggestions.
