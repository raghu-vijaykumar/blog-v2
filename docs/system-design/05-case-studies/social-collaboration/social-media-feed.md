+++
title= "Social Media Feed Timeline"
tags = [ "system-design", "software-architecture", "interview", "social-media", "feed", "timeline", "fan-out" ]
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
weight= 37
bookFlatSection= true
+++

---

## Design Social Media Feed Timeline

### Problem Statement
Design a scalable social media feed/timeline system that generates personalized, real-time content streams for users. The system must efficiently combine posts from followed users, ranked by relevance, while handling massive concurrency and providing sub-second response times for feed refreshes.

### Requirements

#### Functional Requirements
- Generate personalized feed/timeline for each user based on followed accounts
- Real-time updates when new content is posted
- Content ranking algorithms (engagement-based, chronological, algorithmic)
- Social interactions (likes, comments, shares)
- Feed pagination and infinite scroll support
- Content moderation and filtering

#### Non-Functional Requirements
- Sub-second response times for feed loads and refreshes
- High throughput for concurrent users (millions refreshing simultaneously)
- Low latency for real-time content delivery (`<100ms`)
- Scalability to billions of posts and relationships
- Fault tolerance with eventual consistency

### Key Constraints & Assumptions
- **Scale assumptions**: 1B active users, 100B relationships (follows), 10M new posts/sec during peak hours; feed size 500 posts per refresh ^[Assumption: Based on major social platforms usage patterns.]
- **SLA**: 99.9% availability, p50 feed load `<500ms`, p95 `<2s`
- **Content freshness**: near real-time updates (`<5 `seconds for new posts)
- **Storage**: 1PB+ daily ingestion, multi-year retention for engagement analytics

### High-Level Design
The system implements a hybrid feed generation approach combining push and pull strategies. High-volume users use push-on-write (fan-out) while low-volume users use pull-on-read. Content is cached at edge locations for fast delivery.

```
graph TD
    A[User Posts Content] --> B[Write API Gateway]
    B --> C[Content Service]
    C --> D[Post Persistence]
    C --> E[Feed Fan-out Service]
    E --> F[Push: User's Timeline Cache]
    G[User Loads Feed] --> H[Read API Gateway]
    H --> I[Feed Service]
    I --> J{Relationship Store}
    J --> K{Pull: Real-time Aggregation}
    J --> L{Push: Cached Timeline}
    K --> M[Content Store]
    L --> N[Timeline Cache]
    I --> O[Feed Ranking Engine]
    O --> P[ML Model Server]
```

^[Mermaid diagram showing hybrid push-pull feed generation architecture.]

### Data Model
- **Relationships**: Graph database for follower/following relationships with social graph algorithms
- **Posts**: Document store (DynamoDB) with metadata, content, and embedded media references
- **Feed Cache**: Redis clusters for personalized timeline caches with TTL and eviction policies
- **Engagement**: Time-series database for likes, comments, shares powering ranking algorithms

### API Design
RESTful and real-time WebSocket APIs:

- **POST /api/v1/posts** - Create post: `{"content": "text", "media": ["url1"], "visibility": "public"}` → `{"postId": "post123", "timestamp": 1234567890}`
- **GET /api/v1/feed?cursor=abc123&limit=50** - Get personalized feed with pagination
- **WebSocket /feed/stream** - Real-time feed updates: `{"type": "new_post", "data": {...}}`
- **POST /api/v1/posts/{postId}/engage** - Like/share/comment: `{"type": "like"}` → engagement tracking
- **GET /api/v1/users/{userId}/posts** - User's posts and timeline

^[APIs support OAuth 2.0 authentication and cursor-based pagination for infinite scrolling.]

### Detailed Design
- **Fan-out Service**: Write-through caching at post time, pushing to all follower caches (optimized for celebrity accounts)
- **Pull Strategy**: On-demand feed construction for niche/high-follower accounts to reduce memory pressure
- **Feed Ranking**: ML-based personalization considering recency, engagement, relationship strength, and user preferences
- **Cache Hierarchy**: Multi-tier caching with CDN edge for media, Redis for timelines, in-memory Ring buffers for real-time updates  
- **Event Streaming**: Kafka for decoupling post writes from fan-out operations, enabling asynchronous processing
- **Data Partitioning**: Sharding by user ID for relationships and post ownership, consistent hashing for feed caches

### Scalability & Bottlenecks
- **Horizontal Scaling**: Stateless feed services scale by user traffic, with geographic distribution for reduced latency
- **Cache Optimization**: 98% cache hit rate for hot timelines, aggressive TTL policies (1-24 hours) for cold content
- **Fan-out Limits**: Hybrid thresholds based on follower count (push for `<100k` followers, pull for >100k)
- **Database Sharding**: Global spread of user data across thousands of shards with automatic rebalancing
- **Bottlenecks**: Celebrity posts causing cache storms; mitigated with adaptive thresholds and queue buffering

### Trade-offs & Alternatives
- **Push vs Pull**: Push maximizes read performance but wastes resources on inactive users vs. pull saves resources but adds read latency
- **Recency vs Relevance**: Chronological simplest implementation vs. algorithmic ranking improves engagement but increases complexity
- **Strong vs Eventual Consistency**: Strong consistency ensures fresh feeds vs. eventual consistency enables horizontal scaling
- **Centralized vs Edge Caching**: Centralized simpler ops vs. edge enables global distribution and lower latency

### Future Improvements
- Advanced ML ranking with behavioral prediction
- Multi-device timeline synchronization  
- AR/VR content integration
- Real-time collaborative posting
- Social commerce integration

### Interview Talking Points
1. Explain hybrid push-pull: Push-on-write for power users reduces read load vs. pull-on-read for scale limits cache explosion
2. Discuss fan-out optimization: Adaptive thresholds prevent cache storms for celebrities while maintaining freshness
3. Address timeline consistency: Eventual consistency acceptable for social feeds vs. strong consistency for financial data
4. Compare feed ranking: ML improves engagement but adds latency vs. chronological ensures predictability and simplicity
5. Handle global scale: Geographic sharding and CDN ensure sub-second response worlds wherever users are located
6. Manage cache invalidation: Intelligent TTL policies balance freshness, memory usage, and computational costs
7. Approach peak traffic: Queue buffering and rate limiting handle sudden spikes from viral content
8. Implement personalization: Multi-armed bandits and reinforcement learning optimize ranking without overfitting
