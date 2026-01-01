+++
title= "Proximity Service"
tags = [ "system-design", "software-architecture", "interview", "proximity-service" ]
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
weight= 14
bookFlatSection= true
+++

# Proximity Service Design

## Problem Statement

A proximity service that enables location-based business searches, allowing users to find nearby businesses within a specified radius. This service supports high-scale read operations while managing business listings through CRUD operations.

## Requirements

### Functional Requirements

- **Location-based Search**: Return businesses within a user-defined radius based on latitude/longitude coordinates.
- **Business Management**: Allow creation, update, deletion, and retrieval of business listings.
- **Business Details View**: Provide detailed information about individual businesses.

### Non-Functional Requirements

- **Low Latency**: Query response time under 500ms for search requests.
- **High Availability**: 99.9% uptime with fault tolerance for regional failures.
- **Scalability**: Support for 100 million daily active users with 5,000 queries per second.

## Key Constraints & Assumptions

- **Scale Assumptions**: 100 million daily active users (DAU), 200 million total businesses, peak load of 5,000 search queries/second.
- **Data Assumptions**: Each business record is 1-10KB; location data is stored as 24 bytes per business.
- **Latency Constraints**: Search responses must complete within 500ms end-to-end.
- **Consistency**: Eventual consistency is acceptable for business updates (reflect within 24 hours).
- **Geographic Coverage**: Global coverage with focus on major metropolitan areas.
- **Assumption**: Users provide precise GPS coordinates; no location permission handling required in design scope.

## High-Level Design

The system follows a three-tier architecture with load balancers, stateless application services, and a relational database with read replicas for horizontal scaling.

```
graph TD
    A[Client App] --> B[Load Balancer]
    B --> C[Location-Based Service]
    C --> D[(Primary DB)]
    D --> E[(Read Replicas)]
    C --> F[Business Service]
    F --> D
    F --> E
    D -.->|Replication| E

    subgraph "External Services"
        A
    end

    subgraph "Application Layer"
        C
        F
    end

    subgraph "Data Layer"
        D
        E
    end
```

**Components:**
- **Load Balancer**: Distributes traffic across service instances using round-robin or least-connections algorithms.
- **Location-Based Service (LBS)**: Handles proximity searches using geospatial indexing.
- **Business Service**: Manages CRUD operations for business entities.
- **Database Layer**: Primary-secondary setup with PostgreSQL (or similar RDBMS) for ACID compliance and geospatial extensions.

## Data Model

### Business Entity
```sql
CREATE TABLE businesses (
    business_id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    phone VARCHAR(20),
    website VARCHAR(500),
    address JSONB, -- Structured address data
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_business_name ON businesses(name);
CREATE INDEX idx_business_updated ON businesses(updated_at);
```

### Location/Geospatial Index
```sql
CREATE TABLE business_locations (
    business_id BIGINT PRIMARY KEY REFERENCES businesses(business_id),
    latitude DECIMAL(10,8) NOT NULL,
    longitude DECIMAL(11,8) NOT NULL,
    geohash_4 VARCHAR(4) NOT NULL,
    geohash_5 VARCHAR(5) NOT NULL,
    geohash_6 VARCHAR(6) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()

-- Composite indexes for geospatial queries
CREATE INDEX idx_geohash_4_lat_lng ON business_locations(geohash_4, latitude, longitude);
CREATE INDEX idx_geohash_5_lat_lng ON business_locations(geohash_5, latitude, longitude);
CREATE INDEX idx_geohash_6_lat_lng ON business_locations(geohash_6, latitude, longitude);
```

**Storage Choice**: Relational database (PostgreSQL/MySQL) with geospatial extensions over NoSQL solutions for transactional consistency and complex queries. Total storage: ~2-5TB for businesses, ~10GB for location indexes.

## API Design

### Search Businesses Endpoint
```
GET /api/v1/search?lat={latitude}&lng={longitude}&radius={meters}&limit={count}&offset={offset}

Request Example:
GET /api/v1/search?lat=37.7749&lng=-122.4194&radius=1000&limit=20

Response:
{
  "businesses": [
    {
      "business_id": 12345,
      "name": "Starbucks",
      "distance_meters": 450,
      "latitude": 37.7749,
      "longitude": -122.4194
    }
  ],
  "total": 150,
  "has_more": true
}
```

### Business Management Endpoints
```
POST /api/v1/businesses
PUT /api/v1/businesses/{business_id}
DELETE /api/v1/businesses/{business_id}
GET /api/v1/businesses/{business_id}
```

Request Example (Create Business):
```json
{
  "name": "Local Coffee Shop",
  "description": "Best coffee in town!",
  "latitude": 37.7749,
  "longitude": -122.4194,
  "phone": "+1-555-0123",
  "address": {
    "street": "123 Main St",
    "city": "San Francisco",
    "state": "CA",
    "zip": "94102"
  }
}
```

## Detailed Design

### Location-Based Service (LBS)
- **Purpose**: Executes fast geospatial queries using Geohash indexing.
- **Technology**: Stateless Java/Python service with in-memory caching for geohash neighbor calculations.
- **Key Algorithm**: 
  1. Determine geohash precision based on search radius (length 4-6 for 20km-0.5km grids).
  2. Generate 9 query geohashes (center + 8 neighbors) to handle boundary cases.
  3. Execute SQL query with LIKE pattern matching on geohash column.
  4. Calculate Haversine distance for precise filtering and ranking.
- **Bottlenecks**: Geohash boundary queries can return false positives; mitigated by distance calculation.
- **Scaling**: Horizontal scaling with load balancer; database read replicas handle query load.

### Business Service
- **Purpose**: Handles create/read/update/delete operations for business entities.
- **Technology**: RESTful service with ORM layer for database interactions.
- **Data Consistency**: Uses database transactions for writes; eventual consistency for read replicas.
- **Caching Strategy**: No caching initially due to data freshness requirements; monitor for hot business profiles.
- **Write Load**: Low-frequency updates allow primary database to handle all writes.

### Database Layer
- **Primary Database**: Handles all write operations; maintains ACID compliance.
- **Read Replicas**: 3-5 replicas for read scaling; eventual consistency (replication lag `<30 `seconds).
- **Failover**: Automated promotion of replica to primary during outages.
- **Backup**: Daily snapshots with point-in-time recovery for disaster recovery.

## Scalability & Bottlenecks

- **Read Scalability**: Database read replicas can scale horizontally up to 10-20 instances; beyond that, consider sharding by geographic regions (country/state).
- **Write Scalability**: Master database can handle 1,000-5,000 writes/second; business table can be sharded by business_id if needed.
- **Geospatial Query Bottlenecks**: Large radius searches return many candidates; mitigated by pagination and user behavior analysis (most searches `<1k`m).
- **Location Indexing Bottlenecks**: Geohash table growth is linear; 10GB table fits in memory for fast queries.
- **Network Bottlenecks**: Latency-sensitive queries require edge deployment in multiple regions using CDNs or edge compute.
- **Fault Tolerance**: Multi-AZ deployment ensures regional failures don't impact global availability.

## Trade-offs & Alternatives

- **SQL vs NoSQL**: SQL chosen for complex joins and transactions over NoSQL (e.g., DynamoDB) to maintain referential integrity between business and location data. Trade-off: SQL complexity vs. NoSQL scalability for pure read workloads.
- **Geohash vs R-Tree**: Geohash selected for simplicity and database-native support over R-Tree (e.g., PostGIS) due to easier operational management. Trade-off: Approximate results requiring post-processing vs. exact boundary queries.
- **Read Replicas vs Sharding**: Read replicas prioritized for simplicity over early sharding; moving from horizontal scaling to vertical partitioning later. Trade-off: Eventual consistency delays vs. complex distributed transactions.
- **In-Memory Caching**: Foregone initially for location data (fits in RAM) to avoid cache invalidation complexity. Trade-off: Potential database load vs. cache management overhead.
- **Microservices vs Monolith**: Services separated but share database to avoid distributed transactions. Trade-off: Loose coupling vs. shared data dependencies.

## Future Improvements

- **Real-time Updates**: Implement change data capture (CDC) for near-real-time business updates using Kafka streams.
- **Advanced Filtering**: Add category-based search, ratings, and reviews integration.
- **Personalization**: Incorporate user preferences and behavioral data using recommendation engine.
- **Global Sharding**: Partition database by geographic regions for true global scale (>1B businesses).
- **Mobile Optimization**: Edge computing deployment for sub-100ms response times.
- **Analytics Pipeline**: Add click-tracking and search analytics for business insights.

## Interview Talking Points

1. **Geospatial Indexing Choice**: Geohash over PostGIS R-Tree for operational simplicity despite approximate boundary handling.
2. **Read-Heavy Design**: Prioritized database read replicas over caching due to small, frequently-accessed dataset.
3. **Query Optimization**: Used geohash prefix matching with post-processing distance calculation to balance performance and accuracy.
4. **Scale Trade-offs**: Accepted eventual consistency (24hr updates) to enable massive read scaling through replicas.
5. **Fault Tolerance**: Multi-region deployment ensures high availability despite single-region database failures.
6. **Data Partitioning Strategy**: Geographic sharding deferred until bottlenecked; vertically scaled read replicas first.
7. **API Design Decisions**: RESTful endpoints with pagination to handle variable result sets efficiently.
8. **Monitoring Strategy**: Focus on query latency and cache hit rates as primary scalability indicators.
