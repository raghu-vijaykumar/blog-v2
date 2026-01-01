+++
title= "Consistent Hashing"
tags = [ "system-design", "software-architecture", "interview", "consistent-hashing" ]
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
weight= 4
bookFlatSection= true
+++


# Design Distributed Caching with Consistent Hashing

## Problem Statement

Design a distributed caching system that can store and retrieve key-value pairs efficiently across multiple servers, minimizing cache misses and data migration when servers are added or removed, using consistent hashing to achieve scalable and fault-tolerant caching.

## Requirements

### Functional Requirements

- Store key-value pairs with support for CRUD operations (create, read, update, delete)
- Handle high-volume reads and writes across distributed nodes
- Automatic data redistribution when nodes join or leave the cluster

### Non-Functional Requirements

- Low latency responses (< 10ms for reads)
- High availability (99.9% uptime)
- Horizontal scalability to thousands of nodes
- Fault tolerance to node failures

## Key Constraints & Assumptions

- Daily active users: 100 million
- Read requests/second: 100,000 RPS (peak), Write requests/second: 10,000 RPS
- Total data size: 100 TB, Average key size: 100 bytes, Value size: 1 KB
- Server nodes: 100 initial servers, scaling to 1000+
- Assumption: Use SHA-256 hashing for uniform distribution
- SLA: 99.99% availability, P99 latency < 50ms

## High-Level Design

The system uses consistent hashing to distribute keys across a cluster of cache servers. Each server is responsible for a portion of the hash space, and virtual nodes ensure even distribution to avoid hotspots.

### Architecture Components

- **Clients**: Send GET/PUT/DELETE requests
- **Load Balancer**: Routes requests to appropriate servers (may use consistent hashing proxy)
- **Cache Servers**: In-memory key-value storage (e.g., Redis instances)
- **Hash Ring**: Circular data structure mapping keys to servers
- **Virtual Nodes**: Multiple replicas of physical servers on the ring for load balancing

```
graph TD
    Client[Client Requests] --> LB[Load Balancer]
    LB --> Server1[Cache Server 1]
    LB --> Server2[Cache Server 2]
    LB --> Server3[Cache Server 3]

    subgraph "Hash Ring"
        direction clockwise
        HN1[Hash Space 1] --> HN2[Hash Space 2]
        HN2 --> HN3[Hash Space 3]
        HN3 --> HN1
    end

    Server1 -.-> VN1[Virtual Node 1-1]
    Server1 -.-> VN2[Virtual Node 1-2]
    Server2 -.-> VN3[Virtual Node 2-1]
    Server2 -.-> VN4[Virtual Node 2-2]
    Server3 -.-> VN5[Virtual Node 3-1]
    Server3 -.-> VN6[Virtual Node 3-2]

    VN1 --> HN1
    VN2 --> HN2
    VN3 --> HN3
```

## Data Model

### Entities

- **Key**: String identifier (e.g., cache key)
- **Value**: Arbitrary data (TTL-optional)
- **Virtual Node**: Mapping of hash range to physical server

### Storage Choice

- **Primary Storage**: In-memory storage (Redis/Memcached) for fast access
- **Metadata Store**: Separate datastore for virtual node mappings (e.g., ZooKeeper or etcd for coordination)

### Schema Sketch

```sql
-- Simplified metadata store (conceptual)
CREATE TABLE virtual_nodes (
    vnode_id INT PRIMARY KEY,
    server_ip VARCHAR(255),
    hash_range_start BIGINT,
    hash_range_end BIGINT
);

-- Key-value storage (in-memory, no persistent schema)
-- key: hash(key) -> server via consistent hashing
```

## API Design

### Core Endpoints

#### GET /cache/{key}
**Request:**
```
GET /cache/{key}
```

**Response:**
```json
{
  "key": "user_123",
  "value": "cached_data",
  "ttl": 3600
}
```

#### PUT /cache/{key}
**Request:**
```
PUT /cache/{key}
Content-Type: application/json

{
  "value": "new_data",
  "ttl": 3600
}
```

**Response:**
```json
{
  "success": true,
  "message": "Key stored"
}
```

#### DELETE /cache/{key}

**Request:**
```
DELETE /cache/{key}
```

## Detailed Design

### Cache Layer
- Use Redis with consistent hashing client library
- Cache eviction: LRU when memory full

### Hash Ring Implementation
- Hash function: SHA-256 mapped to 64-bit integer range
- Server lookup: Clockwise search from key hash to first server
- **Assumption**: Implemented in a library like JumpHash or custom hash ring

### Virtual Nodes
- Each physical server has 100-200 virtual nodes
- Ensures even load distribution (std dev: 5-10%)
- Trade-off: More metadata overhead vs better balancing

### Replication & Failover
- Basic consistent hashing doesn't replicate; use server-side replication (e.g., Redis Cluster with 3 replicas per shard)

### Message Queue (if async writes)
- None required for synchronous caching

## Scalability & Bottlenecks

- **Horizontal Scaling**: Add servers with minimal (~1/N) key remapping
- **Load Balancing**: Virtual nodes prevent hotspots
- **Sharding**: Each virtual node owns a hash range
- **Concurrency**: Use multiple threads per server for high RPS
- **Bottleneck**: Network latency between client/load balancer; mitigated by regional CDNs

## Trade-offs & Alternatives

- **Consistent Hashing vs Modulo**: CH minimizes rehashing (K/N vs K Keys) but adds complexity
- **Virtual Nodes**: Improve load balance but require more memory for metadata and coordination
- **In-memory vs Disk Cache**: Faster access vs larger capacity
- **Single-Region vs Multi-Region**: Better latency/reliability vs increased complexity

## Future Improvements

- Auto-scaling based on load metrics
- Multi-region replication with conflict resolution
- Compression for larger datasets
- Cache warming strategies for cold starts
- Observability with metrics and tracing

## Interview Talking Points

1. Explain the rehashing problem in traditional modulo hashing
2. How consistent hashing distributes keys and minimizes remapping
3. Role of virtual nodes in achieving even load distribution
4. Trade-offs between minimal remapping and exact load balance
5. Scaling from 100 to 1000+ servers with consistent hashing
6. Handling node failures and data consistency
7. Real-world applications (DynamoDB, Cassandra, CDN caching)
8. Alternative hashing techniques (Rendezvous, Jump Hash)
