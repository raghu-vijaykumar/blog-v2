+++
title= "Ride Sharing Service"
tags = [ "system-design", "software-architecture", "interview", "ride-sharing" ]
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
weight= 13
bookFlatSection= true
+++

# Ride Sharing Service

## Problem Statement

Design a highly scalable and highly available ride-sharing service that enables millions of users to seamlessly request rides from hundreds of thousands of drivers worldwide. The system must handle real-time matching, location tracking, payment processing, and ensure minimal wait times while maintaining 99.99% availability.

## Assumptions

- **Scale**: 100M monthly active users, 50K concurrent active drivers globally.
- **Traffic**: 100K QPS for ride requests, with spikes during peak hours.
- **Availability**: 99.99% uptime SLA.
- **Geography**: Global deployment across major cities.
- **User Behavior**: Users can share rides if multiple riders agree and the vehicle capacity allows.
- **Compliance**: Driver background checks and vehicle inspections are pre-approved.
- **Payments**: Integration with external payment gateways for credit cards and bank transfers.

## Constraints

- **Performance**: Ride matching within 5 seconds at p50 and 10 seconds at p99.
- **Latency**: User login in under 100ms, location updates every 5-10 seconds.
- **Data**: High volume of location data storage and retrieval for active drivers.
- **Security**: Secure handling of payment information, no storage of sensitive credit card data.
- **Concurrency**: Handle high concurrency for ride matching and payment processing without race conditions.

## High Level Design

```mermaid
graph TB
    UA[User App] --> AG[API Gateway]
    DA[Driver App] --> AG
    AG --> US[User Service]
    AG --> DS[Driver Service]
    AG --> RS[Ride Service]

    US --> DB1[(User DB: SQL)]
    DS --> DB2[(Driver DB: SQL)]
    RS --> DB3[(Ride DB: SQL)]

    DS --> LS[Location Service]
    LS -.-> DB4[(Location DB: NoSQL)]
    LS --> MB[Message Broker]

    RS --> MS[Matching Service]
    MS --> LS
    MS --> ETAS[External ETA Service]

    RS --> TS[Trip Service]
    TS --> DB3
    TS --> PS[Payment Service]
    TS --> NM[Notification Service]

    PS --> DB5[(Payment DB: SQL)]
    PS --> PG[Payment Gateway]

    TS --> TMGS[Trip Map Generator Service]
    NM --> TMGS

    MB --> C[Cache]
    DB1 --> S[(Shared Storage: Object Store)]
    DB2 --> S
    TMGS --> S
```

### Component Overview

- **API Gateway**: Routes requests, handles authentication, rate limiting, and load balancing.
- **User/Driver Apps**: Mobile apps for users and drivers.
- **User Service**: Manages user registration, authentication, and profiles.
- **Driver Service**: Manages driver onboarding, authentication, availability states, and profiles.
- **Ride Service**: Handles ride requests, matching, and execution.
- **Location Service**: Ingests and serves real-time driver locations.
- **Matching Service**: Core algorithm for matching users with drivers using proximity and ETA.
- **Trip Service**: Manages trip lifecycle, updates, and post-trip activities.
- **Payment Service**: Processes fare calculations, payments, and transfers to drivers.
- **Notification Service**: Sends push notifications and emails for ride updates, receipts, and maps.
- **Trip Map Generator Service**: Creates visual maps of completed trips using historical locations.

Databases:
- SQL for transactional data (users, drivers, rides, payments) with partitioning/sharding for scale.
- NoSQL (e.g., Redis/DynamoDB) for location data with geohashing-indexed queries.
- Object Store (e.g., S3) for profile images, trip maps.

Message Broker (e.g., Kafka) for event-driven updates between services.

## Data Model

### Core Entities

```sql
-- Users Table (SQL)
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    payment_info_id BIGINT,  -- References encrypted vault key
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Drivers Table (SQL)
CREATE TABLE drivers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    license_number VARCHAR(255) UNIQUE NOT NULL,
    vehicle_make VARCHAR(100),
    vehicle_model VARCHAR(100),
    vehicle_plate VARCHAR(20),
    bank_account_id BIGINT,  -- References encrypted vault key
    status ENUM('offline', 'available', 'in_trip'),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rides Table (SQL)
CREATE TABLE rides (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    driver_id BIGINT,
    pickup_lat DECIMAL(10,8),
    pickup_lng DECIMAL(11,8),
    dropoff_lat DECIMAL(10,8),
    dropoff_lng DECIMAL(11,8),
    status ENUM('requested', 'matched', 'in_progress', 'completed', 'cancelled'),
    estimated_fare DECIMAL(10,2),
    actual_fare DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    distance_km DECIMAL(8,3),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);
```

**Location Data**: Stored in NoSQL with geohash index for range queries.

**Payment Data**: Sensitive info encrypted and stored in secure vault; only IDs referenced in DB.

## API Design

### Authentication Endpoints
- **POST /api/auth/login** - Returns JWT for user/driver.
- **POST /api/auth/register** - Registers new user/driver.

### User Endpoints
- **POST /api/rides/request** - Request a ride with pickup/dropoff locations. Returns ride ID and estimated fare/ETA.
- **GET /api/rides/:id** - Get ride status and real-time updates.
- **PUT /api/rides/:id/cancel** - Cancel ride before start.

### Driver Endpoints
- **POST /api/drivers/activate** - Mark driver available with initial location.
- **POST /api/drivers/location** - Update location (every 5s).
- **PUT /api/drivers/status** - Update availability (available/in_trip/offline).

### Admin/monitoring
- Metrics endpoints for QPS, latency, availability.

**Example API Call**:

```json
POST /api/rides/request
Authorization: Bearer <JWT>
Content-Type: application/json

{
  "pickup_lat": 37.7749,
  "pickup_lng": -122.4194,
  "dropoff_lat": 37.7848,
  "dropoff_lng": -122.4094,
  "passenger_count": 2
}

Response:
{
  "ride_id": 12345,
  "estimated_fare": 25.50,
  "estimated_eta": "5 min",
  "driver_name": "John D.",
  "vehicle_info": "Blue Toyota Corolla"
}
```

## Component Details

### User Service
- **Tech Stack**: Node.js/Kinesis for handling high QPS, PostgreSQL sharded by user region.
- **Responsibilities**: Registration (with image upload to S3), auth (JWT with refresh tokens), profile updates.
- **Scaling**: Load balancer, read replicas, CDN for images.

### Driver Service
- **Tech Stack**: Similar to User Service, with caching for online status.
- **Responsibilities**: Registration (background check via external API), auth, status management.
- **Real-time**: Bidirectional WebSocket for location updates and ride assignments.

### Location Service
- **Tech Stack**: Go service, Redis with geohash for location storage.
- **Reasoning**: NoSQL for high write throughput (location updates every 5s from 50K drivers = 10K writes/sec), geohashing enables efficient proximity queries without scanning all data.

### Matching Service
- **Tech Stack**: Python async service using Kafka for real-time driver location events.
- **Algorithm**: Query geohash cells around user pickup, fetch nearby drivers (e.g., 500-1000 drivers), call external ETA API for top candidates, select based on ETA + distance + rating.
- **Challenges**: Trade-off between coverage and speed - too many ETA calls = slow response.

### Trip Service
- **Tech Stack**: Java Spring, MongoDB for trip logs.
- **Responsibilities**: Handles trip start/completion events, updates DB, triggers payment calculation.
- **Eventing**: Publishes to Kafka for downstream services like payment and notifications.

### Payment Service
- **Tech Stack**: PCI-compliant service, Stripe/Ach API integration, encrypted data vault.
- **Responsibilities**: Fare calculation (base + distance*cost_per_km + time*cost_per_min), transfer to drivers minus platform cut.
- **Reliability**: Retry logic for failed payments, idempotency to handle duplicates.

### Notification Service
- **Tech Stack**: AWS SES/Push notifications, Redis cache for templates.
- **Responsibilities**: Send receipts, trip maps, reminders.

## Scalability

- **Horizontal Scaling**: All services behind load balancers with auto-scaling based on CPU/memory.
- **Database Sharding**: Users/drivers sharded by region, rides by date/timestamp.
- **Caching**: Redis for hot locations, ride matching results, user profiles.
- **CDN**: For static assets and generated trip maps.
- **Geographical Distribution**: Deploy in multiple regions with DR failover.
- **Capacity Planning**: Monitor QPS, scale during events (e.g., New Year's Eve).

## Performance Optimizations

- **Bloom Filter for User Login**: In User Service cache to quickly check existing usernames (space-efficient vs hash tables).
- **Geohashing for Proximity**: Divides globe into hierarchical grid cells. Drivers stored with geohash prefix (e.g., length 6 for ~100m precision). Ride requests convert location to geohash, query range of prefixes (e.g., 8 adjacent cells), reducing candidates from millions to hundreds.
  - Benefit: Constant time queries vs O(N) distance calc.
  - Trade-off: Some false positives (drivers just outside cell) but acceptable.
- **Messaging**: Kafka for decoupling services, buffer spikes.
- **Indexing**: Geohash indexes, composite indexes on rides for fast lookups.

## Trade-offs and Alternatives

- **SQL vs NoSQL for Location**: SQL provides ACID but slower for high-write location data; NoSQL (eventual consistency) chosen for performance, with eventual consistency via Kafka events.
- **Centralized Matching vs Distributed**: Centralized Matching Service scales better than letting drivers poll, but introduces bottleneck. Alternative: Edge-based matching per region.
- **Exact Distance vs Approximate**: Geohashing approximates proximity; for precision, could hybrid with exact calc for top candidates.
- **Consistency for Ride Status**: Eventual consistency via events vs synchronous locking (slower in high concurrency).
- **Third-Party Dependencies**: External ETA/Payment APIs - monitor uptime, use circuit breakers, fallback to cached routes.
- **Cost vs Performance**: Sharding increases complexity but enables scale; consider geo-partitioning limits.

## Future Improvements

- **AI Demand Forecasting**: Predict surges, dynamic pricing, optimized matching.
- **Multi-modal Transport**: Integrate with public transit APIs for better routing.
- **Sustainability**: Promote shared/electric rides with incentives.
- **Analytics**: Real-time dashboards for ops using Kibana/Snowflake.
- **Security**: End-to-end encryption for locations, zero-knowledge proofs for payments.

## Interview Talking Points

1. **Why Geohashing over Quadtrees/KD-trees?** Simpler implementation, integrates well with existing DB indexes.
2. **Handling Hotspots**: During events, geohash hotspots may overload; shard locations by geohash prefix.
3. **Scalability Bottleneck**: Matching Service under spike; scale horizontally, add rate limiting, use A/B for faster matching (prioritize defaults).
4. **Data Consistency**: Trip completion - async events vs sync APIs; chose async for decoupling and performance.
5. **Payment Reliability**: Handle failures - webhook retries, manual reconciliation.
6. **Real-time Challenges**: WebSockets vs polling; WebSockets reduce server load but complex failover.
7. **Global Scale**: Cross-region ETA; route to nearest region, cache global ETAs.
8. **Security Trade-offs**: No card storage but bank vault; compliance costs performance.
9. **Performance Monitoring**: Use tools like New Relic for bottlenecks; e.g., if location writes slow, add more NoSQL instances.
10. **Evolution**: Start with MVP (SQL everywhere), add NoSQL/kafka as scale requires.

This design balances performance, scalability, and maintainability while addressing real-world constraints for a global ride-sharing platform.
