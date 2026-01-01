+++
title= "Top K Heavy Hitters"
tags = [ "system-design", "software-architecture", "interview", "top-k-heavy-hitters" ]
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
weight= 26
bookFlatSection= true
+++

---

## Design Top K Heavy Hitters

### Problem Statement
The system identifies and returns the top K most frequent items (heavy hitters) in a continuous stream of data within a specified time interval. It must process high-volume streams in real-time, supporting applications like trending services, analytics, or DDoS detection on platforms handling billions of events daily.

### Requirements

#### Functional Requirements
- Identify and retrieve the top K most frequent items (e.g., top 10 viewed videos) for a given time window (e.g., last 5 minutes, 1 hour).
- Support configurable time intervals and values of K.
- Process streaming data continuously without assuming bounded data size.
- Provide both approximate (fast) and exact (slow) results based on query needs.

#### Non-Functional Requirements
- High scalability to handle millions of events per second.
- Availability with fault tolerance to hardware failures.
- Low-latency retrieval (tens of milliseconds for approximate results, seconds for exact).
- High accuracy for exact results; acceptable approximation for fast results.

### Key Constraints & Assumptions
- **Scale assumptions**: 10 billion events/day, peak throughput of 500k events/sec; support K up to 1000 and time windows from 1 minute to 24 hours. ^[Assumption: Reasonable scale for major platforms like YouTube.]
- **SLA**: 99.9% availability, p99 latency less than 100ms for fast queries.
- **Data size**: Items IDs fit in 64-bit integers or short strings; no item metadata storage required.
- **Time precision**: Granular timestamps for events; system clocks synchronized via NTP.
- **Accuracy tolerance**: Fast path allows ~5% overestimation error; slow path requires exact counts. ^[Assumption: Based on practical frequencies in count-min sketch.]

### High-Level Design
The system uses a Lambda architecture with fast and slow processing paths, coupled with message queues and distributed storage. Key components include:

- **API Gateway**: Entry point for event ingestion and queries.
- **Load Balancer & Processors**: Distribute events to processors for aggregation.
- **Distributed Messaging (Kafka)**: Handles high-volume event streams.
- **Fast Path Processors**: Use Count-Min Sketch for approximate real-time results.
- **Slow Path Processors & MapReduce**: Batch processing for exact results over longer windows.
- **Storage Layer**: Distributed file system (e.g., HDFS) and cache for quick access.
- **Query Service**: Merges results from fast/slow paths for final delivery.

```
graph TD
    A[User Events] --> B[API Gateway]
    B --> C[Load Balancer]
    C --> D[Event Processors]
    D --> E[Kafka Topics]
    E --> F[Fast Path: Count-Min Sketch]
    F --> G[Cache/Filter Results]
    E --> H[Slow Path: Data Partitioner]
    H --> I[Partition Processors]
    I --> J[HDFS Storage]
    J --> K[MapReduce Jobs]
    K --> L[Exact Top K]
    M[Query Requests] --> B
    B --> N[Query Service]
    G --> N
    L --> N
    N --> O[Top K Response]
```

^[Mermaid diagram illustrating the flow from event ingestion to query response.]

### Data Model
- **Event Schema**: `{item_id: string/int, timestamp: epoch_ms, event_type: string}` (e.g., video_id or search_term).
- **Count Storage**: In fast path, in-memory Count-Min Sketch (2D array: width ~10k, height ~10, storing estimated counts).
- **Persistent Storage**: Key-value store (e.g., HDFS or S3) with partitioned files: partitions by time window, each containing `{item_id, exact_count}` maps.
- **Query Index**: Cached Top-K lists per time window, updated periodically.

### API Design
Core endpoints exposed via RESTful API:

- **POST /events** - Ingest events (batch support). Request: `[{item_id, timestamp, event_type}]`. Response: 202 Accepted.
- **GET /topk?k=10&window=5m** - Retrieve top K items. Query params: k (int), window (e.g., "5m", "1h"). Response: `[{"item_id": "vid123", "count": 1000}, ...]` with timestamp and approximation flag.

^[APIs assume JSON payloads and HTTP status codes; authentication via API keys.]

### Detailed Design
- **Event Ingestion & Pre-aggregation**: API Gateway buffers events in in-memory hash maps, flushing on thresholds or intervals to reduce load.
- **Distributed Messaging**: Kafka partitions events by item_id hash for even distribution, preventing hotspots.
- **Fast Path (Probabilistic)**: Processors update Count-Min Sketch; query merges multiple sketches for approximate top-K.
- **Slow Path (Exact)**: Data Partitioner assigns events to shards; Partition Processors aggregate in batches; MapReduce emits sorted frequency lists, merging across shards.
- **Technology Choices**: Kafka for durability & scalability; Redis for fast-path caching; Spark for MapReduce simplicity.

### Scalability & Bottlenecks
- **Horizontal Scaling**: Add processors/shards as load grows; load balancer auto-scales.
- **Data Partitioning & Sharding**: Consistent hashing across processors reduces hotspots.
- **Caching & Replication**: In-memory sketches replicated for availability; distributed storage handles  petabytes.
- **Bottlenecks**: Network I/O in merging sketches/lists; memory limits in sketches (tuned via width/height); MapReduce latency for long windows. Mitigate with CDN-like edge caching for queries.

### Trade-offs & Alternatives
- **Fast vs. Slow Path**: Fast path trades accuracy (~5-10% error) for speed (`<50ms` latency); slow path ensures exactness but adds 10-60s delay.
- **Count-Min Sketch vs. Alternatives**: Chosen for memory efficiency (O(1) space) over Lossy Counting or Space Saving; alternatives provide better guarantees but may need more space.
- **Lambda vs. Kappa Architecture**: Simplifies real-time + batch; Kappa (stream-only) could reduce complexity but risks losing historical data precision.
- **SQL vs. NoSQL**: NoSQL (e.g., Redis/S3) preferred for unstructured frequency data; SQL could work for exact counts but limits stream processing speed.

### Future Improvements
- Implement hybrid sketches (e.g., Count-Median) for better accuracy with slight space increase.
- Add machine learning to predict heavy hitters based on patterns.
- Introduce in-storage indexing for sub-second exact queries.
- Multi-datacenter replication for global consistency in queries.

### Interview Talking Points
1. Emphasize Lambda architecture benefits: balances real-time approximation with batch accuracy, common in big data systems.
2. Discuss Count-Min Sketch trade-offs: probabilistic but space-efficient for massive streams vs. exact but memory-intensive options.
3. Highlight partitioning strategies: ensures scalability but avoid uneven loads causing performance degradation.
4. Compare fast/slow paths: approximate results suit dashboards; exact for critical business metrics.
5. Data flow complexity: Event ingestion → processing → query merging; focus on bottlenecks like merging large K lists.
6. Assumptions impact: Scale estimates drive tech choices; flexible APIs allow varied K/window queries.
7. Alternatives evaluation: Kafka + Spark simplifies; Redis caching reduces storage loads.
8. Future-proofing: ML forecasting could evolve reactive to predictive analytics.
