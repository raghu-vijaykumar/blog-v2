+++
title= "Music Streaming Platform"
tags = [ "system-design", "software-architecture", "interview", "streaming", "cdn", "audio" ]
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
weight= 35
bookFlatSection= true
+++

---

## Design Music Streaming Platform

### Problem Statement
Design a scalable music streaming service that delivers high-quality audio content to millions of users simultaneously. The platform must handle real-time streaming, playlist management, content discovery, and personalization while maintaining audio quality and low latency playback across various devices and network conditions.

### Requirements

#### Functional Requirements
- High-quality audio streaming (lossless to compressed formats)
- Playlist creation and management
- Music discovery through recommendations and search
- Artist/pages and listener profiles
- Social features (sharing, following)
- Offline download capabilities
- Audio ads placement and targeting

#### Non-Functional Requirements
- Low startup latency (`<500ms`) for song playback
- Support for variable bitrates (96kbps to lossless)
- High availability with 99.99% uptime for streaming
- Global content delivery with adaptive quality
- Scalability to 100M+ concurrent streams

### Key Constraints & Assumptions
- **Scale assumptions**: 500M users, 100M daily active listeners, 10k concurrent song streams/sec; 100M songs in catalog ^[Assumption: Scaled to major streaming service levels.]
- **SLA**: 99.99% availability, p99 streaming latency `<2s`, instant playback resume within 200ms
- **Audio Quality**: Support multiple formats (MP3, AAC, FLAC) with adaptive bitrate based on network
- **Storage**: Total audio content ~50PB, increasing 10%/year with new releases

### High-Level Design
The platform uses a CDN-edge architecture with global content distribution. Audio chunks are cached at edge locations, while metadata is stored centrally. Recommendation engines run offline for personalization.

```
graph TD
    A[Mobile/Web Player] --> B[API Gateway]
    B --> C[Streaming Service]
    C --> D[CDN Edge]
    D --> E[Origin Storage S3]
    C --> F[User Service]
    F --> G[PostgreSQL]
    C --> H[Playlist Service]
    H --> I[Cassandra DB]
    J[Recommendation Engine] --> K[Data Lake]
    K --> L[Ml Models]
    B --> M[Search Service]
    M --> N[Elasticsearch]
    O[Artist Upload] --> P[Transcoding Service]
    P --> E
    Q[Kafka Event Stream] --> R[Analytics Service]
    Q --> S[Notification Service]
```

^[Mermaid diagram showing CDN-backed music streaming with real-time and offline processing.]

### Data Model
- **User Profiles**: PostgreSQL for user accounts, preferences, and subscription metadata
- **Playlists**: Cassandra time-series for playlist contents with eventual consistency across devices
- **Music Metadata**: Elasticsearch for search indexing, with song/artist/album relationships
- **Audio Files**: Object storage (S3) with multiple quality encodings (96k, 320k, lossless)
- **Listening History**: Distributed storage for user activity logs powering recommendations

### API Design
RESTful and streaming APIs:

- `GET /api/v1/stream/{songId}?quality=320` - Audio streaming: Returns chunked audio data with range headers for seeking
- `POST /api/v1/playlists` - Create playlist: `{"name": "Workout Mix", "songs": ["song1", "song2"]}` → `{"playlistId": "pl123"}`
- `GET /api/v1/recommendations/{userId}` - Get recommendations based on listening history
- `POST /api/v1/search` - Search: `{"query": "rock 80s", "type": "song,artist,album"}` → paginated results
- `PUT /api/v1/playback/progress` - Update progress: `{"songId": "song1", "position": 45, "deviceId": "mobile"}`
- `WebSocket /status/{userId}` - Real-time updates: Friend activity, new releases

^[APIs use adaptive bitrate selection and support resumable downloads.]

### Detailed Design
- **Streaming Engine**: Akamai/Global CDN for audio chunks with edge caching and compression
- **Transcoding Pipeline**: Automated quality conversion for multiple devices and bandwidths
- **Recommendation System**: Machine learning models trained on user behavior, using collaborative and content-based filtering
- **Search & Discovery**: Elasticsearch with fuzzy matching, phonetic algorithms for artist names
- **Cache Strategy**: Multi-level: CDN edge for popular songs (80% hit rate), application cache for metadata
- **Offline Downloads**: Encrypted local storage with license validation and expiry
- **Ads Integration**: Server-side ad insertion with user targeting based on listening habits
- **Monitoring**: Real-time metrics for stream quality, buffering events, and regional performance

### Scalability & Bottlenecks
- **Horizontal Scaling**: Stateless streaming services scale based on concurrent users per region
- **Global Distribution**: 100+ edge locations ensure local content delivery minimizing latency
- **Adaptive Bitrate**: Dynamic quality adjustment reduces bandwidth pressure during peak usage
- **Storage Sharding**: Audio files distributed across multiple S3 regions with replication
- **Bottlenecks**: Peak hourly streaming load (increases 10x during commute times); mitigated by predictive scaling

### Trade-offs & Alternatives
- **CDN vs Origin**: CDN minimizes latency but increases costs vs. origin reduces expenses but slower delivery
- **Centralized vs Distributed**: Central metadata simplifies relationships vs. distributed (consistency challenges)
- **Personalization**: Real-time recommendations more engaging vs. offline preprocessing more scalable
- **Free vs Paid**: Ad-supported tier drives user acquisition vs. subscription-only improves stream quality/revenue

### Future Improvements
- Social listening with shared queues
- Artist/artist collaboration features
- High-resolution audio support (DSD, MQA)
- Voice-controlled playback and discovery
- Immersive formats (spatial audio, 360° video)

### Interview Talking Points
1. Explain adaptive bitrate: Client measures bandwidth and switches quality dynamically for uninterrupted playback
2. Discuss CDN strategy: Global edge distribution ensures `<500ms` startup latency worldwide
3. Address personalization: Recommendation models trained on listening patterns and collaborative filtering
4. Compare storage: Object storage for audio files vs. relational databases for metadata trade-offs
5. Handle peak loads: Predictive scaling based on historical data prevents service degradation
6. Quality vs. Cost: Balancing lossless audio accessibility vs. storage/data transfer costs at scale
7. Offline capabilities: Encrypted downloads with licensing validation ensure content protection
8. Analytics pipeline: Event streaming captures user behavior for real-time insights and recommendations
