+++
title= "Chat"
tags = [ "system-design", "software-architecture", "interview", "chat" ]
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
weight= 9
bookFlatSection= true
+++

# Design Instant Messaging

## Problem Statement
An instant messaging platform enabling real-time communication with direct 1-to-1 messaging, group chats, and public channels. It must support millions of daily active users, handle high message throughput, ensure message persistence, and maintain low latency for a seamless chat experience. *Assumption: Text-only messages initially, user base scales to 10M DAU, message throughput up to 100K msgs/sec.*

## Requirements

### Functional Requirements
- **Direct Messaging**: Users send private messages to individuals.
- **Group Messaging**: Users create and participate in group chats with multiple members.
- **Channel Messaging**: Users create and join public channels for topic-based discussions.
- **Message Persistence**: Store and retrieve message history indefinitely.
- **User Presence**: Indicate online status for users.
- **User Management**: Registration, login, and profile management.

### Non-Functional Requirements
- **Scalability**: Handle 10M daily active users and 100K messages/second.
- **Availability**: 99.99% uptime.
- **Latency**: `<1s` for online delivery at 99th percentile, `<2s` for offline catch-up.
- **Durability**: 100% message persistence with replication.

## Key Constraints & Assumptions
- User base: 10M DAU (daily active users), with peaks of 50M concurrent users.
- Message size: Up to 10KB per message.
- Data retention: Indefinite message history.
- Geographic distribution: Global users, with data centers in multiple regions.
- Security: End-to-end encryption for messages.
-* Assumptions marked where information was incomplete.

## High-Level Design

The system uses a centralized architecture with WebSocket-like bidirectional communication (e.g., via WebSockets) for real-time messaging. Key components include client apps, API Gateway, User Service, Messaging Service, Groups/Channels Service, Chat History Service, and databases (SQL for metadata, NoSQL for messages).

```
graph TD
    Client[Mobile/Web App] --> API_Gateway[API Gateway]
    API_Gateway --> User_Service[User Service]
    API_Gateway --> Messaging_Service[Messaging Service]
    Messaging_Service --> PubSub[Message Broker (e.g., Kafka)]
    PubSub --> Groups_Service[Groups & Channels Service]
    Messaging_Service --> Chat_History[Chat History Service (NoSQL)]
    User_Service --> User_DB[User DB (SQL)]
    Groups_Service --> Groups_DB[Groups DB (SQL)]
    Chat_History --> Chat_DB[Message DB (NoSQL)]
```

## Data Model

- **Users Table** (SQL): id, email, password_hash, username, profile_pic_url, created_at.
- **Groups Table** (SQL): id, name, creator_id, created_at.
- **Channels Table** (SQL): id, name, owner_id, url, created_at.
- **Memberships Table** (SQL): id, user_id, group_id/channel_id, role (admin/member), joined_at.
- **Messages Collection** (NoSQL): `{id, sender_id, recipient_id(s), content, timestamp, msg_type}`.
  - Sharded by chat_id (e.g., group/channel/user pair).

Storage: SQL for relational data (e.g., PostgreSQL), NoSQL for messages (e.g., Cassandra) due to high volume.

## API Design

- **POST /users/signup**: Body `{email, password, username}`. Response `{user_id, token}`.
- **POST /users/login**: Body `{email, password}`. Response `{token}`.
- **POST /messages/send**: Auth token, Body `{recipients: [user_id/group_id/channel_id], content}`. Response `{msg_id}`.
- **GET /messages/history?chat_id=X&since=Y**: Returns paginated messages.
- **GET /groups/create**: Body `{name, members: [user_id]}`. Response `{group_id}`.

Sample Send Message:
```
POST /messages/send
Authorization: Bearer <token>
{
  "recipients": ["group_123"],
  "content": "Hello world!"
}
```

## Detailed Design

- **API Gateway**: Handles rate limiting, authentication, routing to services. Uses Redis for caching.
- **User Service**: Manages auth and profiles. Uses JWT for tokens.
- **Messaging Service**: Manages WebSocket connections, pushes messages via PubSub. Uses Redis for connection mapping (user_id -> server).
- **Groups/Channels Service**: Handles membership and permissions.
- **Chat History Service**: Stores/retrieves messages, with caching for recent data.
- **Message Broker**: Kafka for async message distribution to scale writes.
- **Databases**: SQL with replication, NoSQL sharded by primary key.

Protocol: WebSockets for bidirectional comms, fallback to long-polling.

## Scalability & Bottlenecks

- **Horizontal Scaling**: Services run on Kubernetes with auto-scaling. Messaging service partitioned by user shards (e.g., hash(user_id)).
- **Load Balancing**: Nginx for API Gateway, consistent hashing for database shards.
- **Sharding**: DB sharded by user_id (SQL), chat_id (NoSQL).
- **Caching**: Redis for user sessions, message caches, group memberships.
- **Replication**: Multi-region replication for global distribution.
- **CDN**: For static assets like profile images.
*Bottleneck: Messaging throughput mitigated by Kafka queues; assume 1B msgs/day peak.

## Trade-offs & Alternatives

- **SQL vs NoSQL for Messages**: NoSQL chosen for scalability and writes at scale; trade-off is eventual consistency (vs. SQL ACID for complex queries).
- **WebSockets vs Polling**: WebSockets reduce server load (polling would require constant requests); trade-off is connection management complexity.
- **Centralized vs Peer-to-Peer**: Centralized allows persistence and groups; P2P better for privacy but poor for groups and history.
- **Kafka vs RabbitMQ**: Kafka preferred for high throughput and partitioning; RabbitMQ easier for small-scale but less scalable.

## Future Improvements

- Add media support (integrate S3 for storage).
- End-to-end encryption at message level.
- Push notifications via FCM/APNs.
- Analytics and moderation services.
- Microservices migration if monolithic complexity increases.

## Interview Talking Points
1. Why centralized over P2P? Simplifies persistence and groups at scale.
2. Scalability challenge: Handling 100K msgs/sec with low latency.
3. Database choice: NoSQL for messages due to volume; SQL for metadata.
4. WebSockets trade-off: Persistent connections vs polling inefficiency.
5. Sharding strategy: By user_id/chap_id to distribute load.
6. Caching role: Reduces DB hits for active chats and sessions.
7. High availability: Replication and multi-AZ deployment.
8. Bottlenecks mitigated: Async processing with message queues.
9. Future concerns: Media integration and global replication.
10. Cost vs performance: Balance with CDN and managed cloud services.
