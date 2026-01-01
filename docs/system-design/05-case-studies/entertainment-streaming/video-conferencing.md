+++
title= "Video Conferencing System"
tags = [ "system-design", "software-architecture", "interview", "video", "webrtc", "real-time" ]
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
weight= 33
bookFlatSection= true
+++

---

## Design Video Conferencing System

### Problem Statement
Design a scalable video conferencing platform that supports real-time audio/video communication for multiple participants. The system must handle varying network conditions, manage room-based sessions, and provide high-quality streams with minimal latency while supporting screen sharing and messaging features.

### Requirements

#### Functional Requirements
- Create and join video conference rooms with unique room IDs
- Real-time video/audio streaming for multiple participants (up to 100 per room)
- Screen sharing and presentation capabilities
- Text chat within conference rooms
- Recording of conference sessions
- User authentication and room access control

#### Non-Functional Requirements
- Low latency (`<150ms`) for audio/video streams
- High availability with 99.95% uptime
- Support for HD video (1080p) with adaptive bitrate
- Secure end-to-end encryption for streams
- Scalability to handle millions of concurrent users

### Key Constraints & Assumptions
- **Scale assumptions**: 10M daily active users, 500k concurrent users, 50k active conference rooms; 100 participant average per room during peak hours ^[Assumption: Based on video conferencing growth patterns.]
- **SLA**: 99.95% availability, p99 latency `<150ms` for streams, `<500ms` for joins
- **Network conditions**: Handle poor connectivity with adaptive quality (480p to 4K), support various bandwidths (100Kbps to 10Mbps)
- **Participant limits**: Rooms scale from 2 to 100 participants, with different quality settings for larger rooms

### High-Level Design
The system uses a hybrid peer-to-peer and server-side architecture with WebRTC for direct browser communication and SFU/MCU servers for multi-party calls. Signaling server manages room state and participant coordination.

```
graph TD
    A[Participant A] --> B[Signaling Server]
    A --> C[STUN/TURN Server]
    A --> D[SFU Server]
    E[Participant B] --> D
    E --> B
    F[Participant C] --> G[MCU Server]
    B --> H[Room Management Service]
    H --> I[Redis Cache]
    H --> J[PostgreSQL DB]
    D --> K[WebRTC Gateway]
    L[Load Balancer] --> D
    L --> G
    M[Media Server Cluster] --> N[NATS Message Bus]
    N --> O[Chat Service]
    N --> P[Recording Service]
```

^[Mermaid diagram showing hybrid P2P-SFU architecture for scalable video conferencing.]

### Data Model
- **Rooms**: Relational storage (PostgreSQL) with room_id, participants list, settings, creation_time
- **Participants**: Cached in Redis for real-time presence, with session state and media capabilities
- **Messages**: Time-series database for chat history and events
- **Recordings**: Object storage (S3) for video files with metadata in PostgreSQL

### API Design
WebSocket-based signaling with REST APIs:

- `POST /api/v1/rooms` - Create room: `{"name": "Team Meeting", "max_participants": 50}` → `{"roomId": "abc123", "join_url": "https://vc.com/join/abc123"}`
- `POST /api/v1/rooms/{roomId}/join` - Join room: `{"userId": "user1", "stream_capabilities": {"video": true, "audio": true}}` → WebSocket connection established
- `WebSocket events`: `{"type": "offer", "sdp": "..."}`, `{"type": "ice_candidate", "candidate": "..."}` for WebRTC signaling
- `POST /api/v1/rooms/{roomId}/record` - Start recording: `{"duration_minutes": 60}` → `{"recordingId": "rec001", "status": "started"}`
- `GET /api/v1/rooms/{roomId}/chat` - Fetch chat messages with pagination
^[APIs use JWT authentication, WebSockets maintain persistent connections for real-time signaling.]

### Detailed Design
- **Signaling Server**: Node.js with Socket.IO for WebRTC signaling, manages room state and participant discovery
- **Media Servers**: JanuSFUs for selective forwarding (small groups), MCUs for larger rooms (mixing streams)
- **STUN/TURN Servers**: Coturn for NAT traversal, handling 90% of connection issues automatically
- **Room Management**: Service for room lifecycle, participant limits, and access control policies
- **WebRTC Implementation**: Browser-native for direct peer connections, server-side transcoding when needed
- **Bandwidth Adaptation**: Adaptive bitrate streaming with SVC (Scalable Video Coding) for quality adjustment
- **Security**: DTLS-SRTP for media encryption, room passwords/tokens for access control
- **Caching**: Redis for room state and participant presence, with pub/sub for real-time updates

### Scalability & Bottlenecks
- **Horizontal Scaling**: Media server autoscaling based on room count and participant numbers
- **Load Distribution**: Geographic load balancers route users to nearest media servers for reduced latency
- **Participant Limits**: SFU for `<10 `participants, MCU/switching for larger rooms to reduce bandwidth
- **Caching Strategy**: Distributed Redis clusters for room state, 99% hit rate for active room data
- **Bottlenecks**: CPU-intensive transcoding on media servers; mitigated with GPU acceleration and workload distribution

### Trade-offs & Alternatives
- **SFU vs MCU**: SFU preserves quality but increases client bandwidth vs. MCU reduces bandwidth but adds latency/cpu
- **P2P vs Server-assisted**: P2P minimizes server load but struggles at scale vs. server-assisted more complex but scalable
- **Recording Options**: Server-side recording ensures quality vs. client-side more private but inconsistent
- **Persistent vs Ephemeral Rooms**: Ephemeral reduces storage needs vs. persistent enables meeting history/logs

### Future Improvements
- Integration with calendars for scheduling
- AI-powered noise cancellation and background blur
- Virtual backgrounds and avatars for privacy
- Breakout rooms for larger meetings
- Live transcription and translation

### Interview Talking Points
1. Explain SFU/MCU choice: SFU for scalability with selective forwarding vs. MCU for large rooms with mixing
2. Discuss WebRTC complexity: Browser-native enables direct peer connections but requires signaling servers
3. Address latency: Geographic distribution and peer selection minimize round-trip times
4. Compare P2P vs Server: P2P scales poorly beyond 5-6 participants vs. server-based more reliable at scale
5. Handle network issues: Adaptive bitrate streaming maintains continuity in poor conditions
6. Security approach: End-to-end encryption with secure key exchange for private communications
7. Bottleneck mitigation: Horizontal scaling and workload distribution handle concurrent user spikes
8. Quality vs Scale: Trading off resolution/frame rate vs. participant count in large rooms
