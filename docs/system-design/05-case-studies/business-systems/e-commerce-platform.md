+++
title= "E Commerce Platform"
tags = [ "system-design", "software-architecture", "interview", "e-commerce", "microservices" ]
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
weight= 32
bookFlatSection= true
+++

---

## Design E-commerce Platform

### Problem Statement
Design a scalable e-commerce platform that handles product catalog management, user shopping flows, inventory updates, payment processing, and order fulfillment. The system must support high traffic during peak shopping periods while maintaining consistency and low latency for critical operations like adding to cart and checkout.

### Requirements

#### Functional Requirements
- User registration, authentication, and profile management
- Product catalog with search, filtering, and recommendations
- Shopping cart and checkout process
- Payment processing and order management
- Inventory tracking and stock updates
- Order history and shipment tracking

#### Non-Functional Requirements
- High availability with 99.99% uptime during peak times
- Low latency (`<200ms`) for product search and cart operations
- High throughput for holiday sales (10k checkout transactions/min)
- Strong consistency for inventory and payment operations
- Data durability and audit trails for financial transactions

### Key Constraints & Assumptions
- **Scale assumptions**: 100M active users/month, 1B products in catalog, 50M daily active users for 30 days straight during holidays ^[Assumption: Similar to major e-commerce platforms.]
- **SLA**: 99.99% availability, p99 latency `<200ms` for core operations, `<2s` for search
- **Traffic patterns**: 80% read operations (catalog browsing), 20% writes (orders, inventory updates); read-after-write consistency needed for inventory
- **Data retention**: Order history retained indefinitely, user sessions expire in 24 hours

### High-Level Design
The platform uses a microservices architecture with separate services for user management, catalog, cart/checkout, inventory, payments, and orders. Traffic flows through API Gateway to respective services, with shared data layer and event-driven communication.

```
graph TD
    A[Users] --> B[API Gateway]
    B --> C[User Service]
    B --> D[Catalog Service]
    B --> E[Cart Service]
    B --> F[Checkout Service]
    F --> G[Payment Service]
    F --> H[Inventory Service]
    G --> I[Order Service]
    D --> J[Recommendation Service]
    E --> K[Redis Cache]
    D --> L[Elasticsearch]
    C --> M[PostgreSQL]
    I --> M
    H --> N[Cassandra DB]
    G --> O[Payment Gateway]
    P[Kafka Event Bus] --> Q[Notification Service]
    P --> R[Analytics Service]
```

^[Mermaid diagram showing microservices architecture with data flows and external integrations.]

### Data Model
- **Users**: Relational storage (PostgreSQL) with user_id (PK), profile data, preferences
- **Products**: Document store (MongoDB) for flexible catalog schema, with category hierarchy and variants
- **Inventory**: Time-series database (Cassandra) for stock levels across warehouses, supporting eventual consistency
- **Orders**: Relational storage with strong consistency for financial data (PostgreSQL)
- **Cart**: Temporary storage in Redis with TTL, session-based persistence

### API Design
RESTful APIs with GraphQL for complex queries:

- **POST /api/v1/users** - User registration: `{"email": "user@example.com", "password": "hash"}` → `{"userId": "123", "token": "jwt"}`
- **GET /api/v1/products?search=jeans&filters=brand,color** - Product search with facets
- **POST /api/v1/cart/{userId}/items** - Add to cart: `{"productId": "456", "quantity": 2}` → `{"cartId": "789", "total": 150.00}`
- **POST /api/v1/checkout** - Process order: `{"cartId": "789", "paymentMethod": "card"}` → `{"orderId": "ORD001", "status": "confirmed"}`
- **GET /api/v1/orders/{userId}** - Order history with status and tracking

^[APIs use JWT for authentication, support pagination for large lists.]

### Detailed Design
- **API Gateway**: Rate limiting, authentication, routing, and API composition using Kong or AWS API Gateway
- **User Service**: Manages authentication (OAuth/JWT), user profiles, and preferences
- **Catalog Service**: Product indexing in Elasticsearch for search, with cache warming for popular items
- **Cart Service**: Redis-based carts with optimistic locking for concurrent updates from multiple devices
- **Checkout Service**: Orchestrates payment, inventory reservation, and order creation as a saga pattern
- **Inventory Service**: Distributed inventory management with eventual consistency (CRDTs for stock reconciliation)
- **Payment Service**: PCI-compliant payment processing with tokenization, integrating with Stripe/PayPal
- **Order Service**: Tracks order lifecycle from creation to fulfillment, with webhook notifications
- **Event Bus**: Kafka streams events for analytics, inventory sync, and notifications
- **Caches**: Multi-level caching with CDN for product images, Redis for hot data, and application caches

### Scalability & Bottlenecks
- **Horizontal Scaling**: Stateless services scale independently; database read replicas for catalog queries
- **Sharding**: User data sharded by region, orders by user_id range, inventory by warehouse/product partition
- **Load Balancing**: Elastic LB with auto-scaling based on CPU usage, geographic distribution for global users
- **Caching Strategy**: 95% cache hit rate for product data, edge caching for static assets via CDN
- **Bottlenecks**: Inventory consistency during high-concurrency checkouts; mitigated with reservation locks and queues

### Trade-offs & Alternatives
- **Microservices vs Monolith**: Microservices enable independent scaling and teams but add complexity vs. simpler monolith
- **SQL vs NoSQL**: PostgreSQL for transactions/inventory consistency vs. Cassandra's scalability; hybrid approach used
- **Synchronous vs Asynchronous**: Cart updates are sync for UX vs. async checkout processing for resilience
- **Caching depth**: Extensive caching improves performance but adds consistency challenges (cache invalidation strategies)

### Future Improvements
- Real-time inventory syncing across warehouses
- AI-powered personalized recommendations
- Voice commerce with natural language shopping
- Multi-channel fulfillment (BOPIS, drone delivery)
- Advanced fraud detection with machine learning

### Interview Talking Points
1. Explain inventory consistency: Use reservation locks during checkout to prevent overselling
2. Discuss checkout flow: Saga pattern orchestrates distributed transactions for fault tolerance
3. Compare caching strategies: Multi-tier caching balances performance vs. consistency requirements
4. Address scaling: Microservices allow independent scaling of read-heavy catalog vs. write-heavy orders
5. Handle peak loads: Queue-based checkout processing prevents system overload
6. Data partitioning: Sharding by region minimizes latency for global users
7. Failure scenarios: Circuit breakers and retries handle payment gateway failures
8. Trade financial consistency: Strong consistency for payments vs. eventual consistency for inventory in non-critical scenarios
