---
title: "Object Storage"
tags: [ "system-design", "software-architecture", "interview", "object-storage" ]
author: "Me"
showToc: true
TocOpen: false
draft: false
hidemeta: false
comments: false
disableShare: false
disableHLJS: false
hideSummary: false
searchHidden: true
ShowReadingTime: true
ShowBreadCrumbs: true
ShowPostNavLinks: true
ShowWordCount: true
ShowRssButtonInSectionTermList: true
UseHugoToc: true
weight: 23
bookFlatSection: true
---

# Design Object Storage

## Problem Statement
An S3-like object storage service that stores massive amounts of unstructured data (up to 100PB annually) with high durability and low cost, similar to Amazon S3 or Google GCS. The system prioritizes scalability, cost-efficiency, and reliability for archival and backup use cases over high-performance reads/writes. Data is stored as immutable objects in buckets without hierarchical structure, accessed via RESTful APIs.

## Requirements

### Functional Requirements
- Create and manage buckets with globally unique names
- Upload, download, delete objects within buckets
- Support object versioning
- List objects in a bucket with prefix-based filtering
- Multipart uploads for large files
- Authenticate and authorize users via IAM (Identity and Access Management)

### Non-Functional Requirements
- Store up to 100PB of data annually
- 6 nines (99.9999%) data durability across multiple failure domains
- 4 nines (99.99%) service availability
- Cost efficiency while maintaining reliability
- Support both small (`<1MB`) and large (>64MB) objects effectively

## Key Constraints & Assumptions
- **Data Scale**: 100PB per year, with 20% small objects (`<1MB`, avg 0.5MB), 60% medium (1-64MB, avg 32MB), 20% large (>64MB, avg 200MB) *Assumption: Distribution based on typical object storage patterns*
- **Traffic**: 95% read operations, 5% writes *Assumption: Based on LinkedIn research for write-once-read-many patterns*
- **Durability SLA**: 6 nines, achieved via replication or erasure coding across failure domains
- **Availability SLA**: 4 nines, with redundancy in components
- **Cost**: Prioritize low-cost storage over high performance; target 50%+ cost reduction via erasure coding *Assumption: Hardware failure rate ~0.81% per HDD per year*
- **Latency**: Higher latency acceptable for cold data; strong consistency required
- **Constraints**: No hierarchical directory structure; RESTful API access; objects immutable except via versioning

## High-Level Design
The system decouples metadata from data storage for independent scaling. It consists of a load balancer, stateless API service, IAM for access control, metadata store for object/bucket info, and data store cluster using replication or erasure coding for durability.

Components:
- **Load Balancer**: Distributes API requests across API service replicas
- **API Service**: Orchestrates requests, handles authentication, and interfaces with metadata and data stores
- **IAM Service**: Manages user authentication and permissions
- **Metadata Store**: RDBMS cluster, sharded for bucket/object metadata
- **Data Store**: Distributed cluster of data nodes with placement service for data routing and reliability

```
%%{init: {'theme': 'neutral'}}%%
graph TD
    Client[Client] --> LB[Load Balancer]
    LB --> API[API Service]
    API --> IAM[IAM Service]
    API --> Metadata[Metadata Store]
    API --> Data[Data Store Cluster]
    Data --> Placement[Placement Service]
    Data --> Nodes[Data Nodes (Primary + Replicas)]

    classDef component fill:#e1f5fe,stroke:#01579b
    class LB,IAM,Metadata,Placement component
```

## Data Model
### Buckets Table
- `bucket_id` (UUID, Primary Key)
- `bucket_name` (String, Unique)
- `user_id` (UUID, Foreign Key)
- `created_at` (Timestamp)
- `region` (String) *Assumption: Regional buckets for geo-distribution*

### Objects Table
- `object_id` (UUID, Primary Key)
- `bucket_id` (UUID, Foreign Key)
- `object_name` (String)
- `version_id` (TIMEUUID) *For versioning*
- `size` (Int)
- `content_type` (String)
- `checksum` (String)
- `created_at` (Timestamp)
- `is_deleted` (Boolean) *For soft deletes*
- `location` (JSON) *Details on data node locations*

*Sharding*: Objects table sharded by hash(bucket_name, object_name) to distribute load. Buckets table small enough for single shard or sharded by user_id.

## API Design
Core RESTful endpoints:

- **Create Bucket**: `PUT /bucket/{bucket_name}`  
  Headers: Authorization, Content-Type, etc.  
  Response: 201 Created

- **Upload Object**: `PUT /bucket/{bucket_name}/{object_name}`  
  Headers: Authorization, Content-Type, Content-Length, x-amz-meta-* (custom metadata)  
  Body: Object data  
  Response: 201 Created, `Location: /bucket/{bucket_name}/{object_name}`

- **Download Object**: `GET /bucket/{bucket_name}/{object_name}`  
  Headers: Authorization, Range (for partial)  
  Response: 200 OK, Body: Object data

- **List Objects**: `GET /bucket/{bucket_name}?prefix={prefix}&marker={marker}&max-keys={100}`  
  Response: XML/JSON list with object names, sizes, etc.

- **Multipart Upload**:
  - Initiate: `POST /bucket/{bucket_name}/{object_name}?uploads` → Response: upload_id
  - Upload Part: `PUT /bucket/{bucket_name}/{object_name}?partNumber={num}&uploadId={id}` → Response: ETag
  - Complete: `POST /bucket/{bucket_name}/{object_name}?uploadId={id}` → 200 OK

*Assumptions*: OAuth2/JWT for Authorization; JSON for metadata responses; Error codes follow HTTP standards (401 Unauthorized, 403 Forbidden, 404 Not Found)*

## Detailed Design
### Metadata Store
- Sharded RDBMS (e.g., PostgreSQL/MySQL) or NoSQL (Cassandra) for object metadata
- Queries optimized for name-based lookups and prefix searches
- Listing via denormalized table sharded by bucket_id for efficiency
- Versioning: New object_id per version, TIMEUUID for ordering

### Data Store
- **Data Routing Service**: Stateless, queries placement service for node assignment, uses REST/gRPC
- **Placement Service**: Maintains cluster map, uses consistent hashing; Paxos/Raft for consensus (5-7 nodes)
- **Data Nodes**: Store data in files (WAL for small objects), SQLite/RocksDB for mapping tables
- **Replication**: Primary node replicates to 2+ secondaries before ack
- **Erasure Coding**: 8+4 scheme for cost efficiency (50% overhead vs 200% for replication)
- Object Persistence:
  - API → Data Routing → Placement assigns nodes → Data written to primary → Replicated → ACK
  - WAL merges small objects; Compaction reclaims space
- Consistency: Strong, waits for replication to complete

## Scalability & Bottlenecks
- **Horizontal Scaling**: API service stateless via load balancers (e.g., Nginx); Data nodes add linearly
- **Sharding**: Metadata by hash(name); Data via consistent hashing on placement service
- **Load Balancing**: Distribute requests; Geographic distribution with CDN
- **Caching**: Metadata cache (Redis) for frequent lookups; CDN for hot objects *Assumption: Cache 20% frequently accessed objects*
- **Bottlenecks**: 
  - IO-bound for large objects; Mitigate with parallel streams
  - Listing slow across shards; Use denormalized tables
  - Network if many data nodes; Cross-AZ replication adds latency but ensures durability
- **IOPS**: Scale data nodes; SSD for hot data, HDD for cold

## Trade-offs & Alternatives
- **Replication vs Erasure Coding**: Replication (3x copies) offers faster reads/writes and simpler implementation but 200% storage overhead; Erasure Coding (8+4) reduces cost by 50%, provides 11 nines durability, but slower (more computation, multi-node reads) and complex. Chose erasure coding for cost in cold storage.
- **SQL vs NoSQL for Metadata**: SQL for complex queries (joins, transactions); NoSQL (DynamoDB/Cassandra) for scale, eventual consistency. Chose SQL for strong consistency, sharded for scale.
- **Write-Ahead Log vs Individual Files**: WAL reduces inode overhead for small files but serializes writes; Individual files simpler but wasteful. Used WAL for efficiency.
- **Strong vs Eventual Consistency**: Strong ensures data integrity post-repl; Eventual faster but risky for financial data. Strong chosen for reliability.

## Future Improvements
- Implement compaction with garbage collector for orphaned/canceled uploads
- Add global replication for geo-disaster recovery (e.g., multi-region)
- Integrate with CDN for faster access to popular objects
- Support encryption at rest and in transit
- Add analytics/logging for usage patterns and performance monitoring
- Optimize for Tiered Storage (hot, warm, cold) with automated transitions

## Interview Talking Points
1. Object storage trades performance for durability/scalability compared to block/file storage, using immutable objects in flat namespaces.
2. Decoupling metadata from data allows independent scaling; metadata sharded by hash for balance.
3. Erasure coding vs replication: Coding saves 50% cost but increases read latency via multi-node reconstruction.
4. Versioning uses TIMEUUID for ordering; deletes create tombstone entries.
5. WAL optimizes small objects by merging; compaction reclaims space without downtime.
6. Placement service uses consistent hashing and consensus for fault-tolerance (tolerates 3/7 failures).
7. Multipart uploads handle large files; cleanup via garbage collector prevents storage waste.
8. Assumptions: 100PB/year scale, 95% reads, favor durability over speed for archival use.
9. Strong consistency via synchronous replication; trade-off for latency in distributed systems.
10. Listing objects is a known bottleneck; denormalized tables per bucket mitigate for common queries.
