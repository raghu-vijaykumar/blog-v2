---
sidebar_title: Google Maps System Design
---

# Google Maps System Design


### Problem Statement
Design a scalable location-based service that handles real-time user location updates, provides accurate navigation with estimated time of arrival (ETA), and renders maps efficiently on mobile and web clients, supporting billions of daily active users with global coverage.

### Requirements

#### Functional Requirements
- Real-time location tracking and updates from users.
- Route calculation between two points, including ETA based on traffic conditions.
- Map rendering with tiling for various zoom levels.
- Geocoding (address to coordinates) and reverse geocoding.
- Support for multiple travel modes (driving, walking, biking).
- Handle traffic data integration for accurate routing.

#### Non-Functional Requirements
- High accuracy for navigation to prevent wrong directions.
- Low latency for map rendering and navigation queries (`<500ms` P95).
- High availability (99.9% uptime).
- Scalable to 1 billion daily active users.
- Minimize data consumption and battery usage on client devices.
- Security for user location data (encryption and privacy compliance).

### Key Constraints & Assumptions
- **Scale**: 1 billion daily active users (DAU), with each user generating ~5 location updates per minute during navigation sessions averaging 35 minutes/week. Road data size: several terabytes of structured routing tiles; Map tile storage: ~70 petabytes (compressed).
- **Traffic**: Average navigation requests: 200k QPS steady-state, peaking at 1M QPS. Location updates: High write volume, batched client-side.
- **Assumptions** (not specified in source):
  - Global road coverage at ~99%; traffic data updated in near-real time.
  - Clients support WebSockets for real-time updates.
  - No multi-stop navigation initially; single origin-destination pairs.
  - Data centers distributed globally with CDN integration for low latency.

### High-Level Design
The system comprises three main services: Location Service (handles user location updates), Navigation Service (computes routes and ETA), and Map Rendering Service (serves tiles on-demand). Components interact via APIs, with data flowing through streams (e.g., Kafka) for analytics and updates.

Core components:
- **Client Apps**: Mobile/web frontends for UI.
- **API Gateway**: Routes requests, handles authentication.
- **Location Service**: Ingests and stores location updates.
- **Navigation Service**: Processes routing requests, integrates geocoding and pathfinding.
- **Map Rendering Service**: Serves precomputed map tiles from CDN.
- **Data Pipeline**: Processes raw road data into routing tiles, handles traffic updates.
- **Storage Layer**: Distributed databases and object storage for data persistence.

```mermaid
graph TD
    A[Client App] --> B[API Gateway]
    B --> C[Location Service]
    B --> D[Navigation Service]
    B --> E[Map Rendering Service]
    C --> F[Cassandra DB]
    C --> G[Kafka Stream]
    D --> H[Router/ETA Services]
    E --> I[CDN / Object Storage]
    G --> J[Analytics / Traffic Updates]
    H --> K[Geocoding DB]
    H --> L[Routing Tiles in S3]
```

### Data Model
- **Routing Tiles**: Stored in S3 object storage as compressed binary files (e.g., adjacency lists). Key: geohash-based. Schema: Node (intersection) → Edges (roads with metadata like speed, distance).
- **User Location Data**: Cassandra (wide-column store for high writes). Table: UserLocations (partition key: user_id; clustering key: timestamp); Columns: latitude, longitude, speed.
- **Geocoding Data**: Redis (key-value store for fast reads). Keys: address/places → lat/long pairs.
- **Traffic Data**: Derived from location updates; stored in time-series DB (e.g., InfluxDB) for historical analysis.
- **Map Tiles**: Precomputed images/vectors in CDN; retrieved via geohash URLs.

### API Design
Core endpoints exposed via RESTful APIs:

- **Location Updates**: `POST /v1/locations`  
  Body: `{ "user_id": "string", "locations": [{"lat": float, "lng": float, "timestamp": int}] }`  
  Batches updates for efficiency.

- **Navigation Request**: `GET /v1/navigate?origin=address&destination=address&mode=driving`  
  Response: `{ "status": "ok", "steps": [...], "eta": "1h 30m", "distance": "50km", "polyline": "encoded_string" }`

- **Map Tiles**: `GET /v1/tiles/{geohash}?zoom=12`
  Returns tile image/vector data.

- **Geocoding**: `GET /v1/geocode?address=string`  
  Response: `{ "lat": 37.422, "lng": -122.084 }`

### Detailed Design
- **Location Service**: Ingests batches of location data via API; writes to Cassandra for persistence. Streams data to Kafka for downstream processing (e.g., traffic analysis). Load balanced across regions. Technology: Cassandra for durability; Kafka for decoupling and reliability over RabbitMQ due to higher throughput for large-scale location data.
- **Navigation Service**: Sub-components include Geocoding (Redis for lookups), Router (A* algorithm on routing tiles from S3), ETA (ML-based predictions from traffic data), Ranker (filters e.g., avoid tolls). Asynchronous updater keeps tiles current. Uses hierarchical routing tiles to minimize computation. Technology: S3 for blob storage (cost-effective, scalable); Azure Functions/AWS Lambda for pathfinding if needed for auto-scaling.
- **Map Rendering Service**: Clients calculate geohash for tiles; fetches from CDN for low latency. Supports vector tiles to reduce bandwidth (client-side rendering). Technology: CDN like Cloudflare for global distribution; vector formats (e.g., Mapbox GL) over raster for efficiency.
- **Data Pipeline**: Offline batch processing transforms raw road data (TB-scale) into tiles using tools like OpenStreetMap processors. Continuous streaming from Kafka updates traffic in real-time.

### Scalability & Bottlenecks
- **Horizontal Scaling**: Location service scales writes via Cassandra's eventual consistency; Navigation via sharded routing tiles and load-balanced router instances. CDN handles map tiles globally.
- **Caching**: Aggressive caching of tiles and geocodes (CDN/Redis); single-digit ms response times.
- **Sharding**: Location data sharded by region/geohash; routing tiles by quadtree partitioning.
- **Replication**: Cross-region for availability; multi-master for navigation.
- **Bottlenecks**: High-frequency location updates (mitigated by batching); Large route computations (hierarchical tiles reduce from hours to seconds).

### Trade-offs & Alternatives
- **Storage Choices**: Cassandra (writes) vs. DynamoDB (cost); S3 (tiles) vs. database (e.g., MongoDB for metadata) – S3 preferred for blob efficiency but lacks indexing.
- **Routing Algorithm**: A* on tiles vs. precomputed shortest paths – A* chosen for real-time flexibility; Dijkstra variant for simplicity.
- **Traffic Integration**: Real-time ML ETA vs. static models – ML improves accuracy but increases latency/compute.
- **Alternatives**: Centralized map storage (too slow for global scale); Dynamically generated tiles (high CPU vs. precompute trade-off).

### Future Improvements
- Add multi-stop navigation for enterprise clients.
- Implement adaptive rerouting: Track active routes in DB (user_id → tile hierarchy); detect traffic incidents via streaming data and push updates via WebSockets.
- Enhance with vector tiles for better rendering and offline support.
- Integrate AR/VR for street view.

### Interview Talking Points
1. **Scope Definition**: Clarify features (e.g., location updates vs. full Google Maps) and constraints (1B DAU, ~70PB tiles) to focus design.
2. **Data Handling**: Batch location updates in Cassandra; stream to Kafka for traffic analytics – balance write performance vs. consistency needs.
3. **Scalability Trade-offs**: Precompute tiles/paths (fast queries) vs. real-time computation (freshness but higher latency/latency).
4. **Technology Choices**: Kafka over RabbitMQ for throughput; S3 over DB for large blobs; CDN for global tile delivery to minimize edge latencies.
5. **Bottlenecks Mitigation**: Hierarchical routing tiles reduce computation from full-graph (hours) to focused traversal (seconds).
6. **Accuracy vs. Performance**: A* algorithm for optimal paths; vector tiles save bandwidth but require client processing power.
