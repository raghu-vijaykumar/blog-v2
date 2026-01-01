+++
title= "Examples"
tags = [ "system-design", "software-architecture", "interview" ]
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
weight= 1
bookCollapseSection= true
+++

# The 5-Step System Design Process

## 1. Gather Functional Requirements

**Objective**: Understand what the system is supposed to do. This includes identifying the key functionalities the system must support and narrowing down the scope.

### Key Tasks:

- Ask clarifying questions to remove ambiguities.
- Define what parts of the system need to be designed and what parts are given (e.g., external APIs or services).

**Importance**: A solid understanding of functional requirements ensures that you're solving the right problem. Mistakes here lead to wasted effort and incorrect designs.

---

## 2. Capture Non-Functional Requirements

**Objective**: Define the qualities the system must exhibit, including performance, scalability, and high availability.

### Key Tasks:

- Identify workload requirements.
- Focus primarily on scalability, availability, and performance.

**Importance**: Non-functional requirements shape the architecture. Missing or misunderstanding these can lead to system failures under load or other unintended consequences.

---

## 3. Define the System's API & Sequence of Events

**Objective**: Create an API that outlines how users and other systems will interact with the system.

### Key Tasks:

- Use sequence diagrams to map out how various components will interact.
- Ensure that all use cases are covered, so no critical behavior is missed.

**Outcome**: A clear understanding of the external interactions with the system, which forms the foundation for the architecture.

---

## 4. Create the High-Level Architecture

**Objective**: Design a high-level architecture that meets the functional requirements.

### Key Tasks:

- Define the architectural style (e.g., microservices, monolithic, event-driven).
- Plan how requests will flow through the system and how data will be stored.

**Importance**: This step focuses on the core functionality and high-level structure of the system.

---

## 5. Optimize for Non-Functional Requirements

**Objective**: Refine the architecture to meet non-functional requirements such as performance, scalability, and availability.

### Key Tasks:

- Identify and eliminate single points of failure.
- Address bottlenecks.
- Optimize critical sections using techniques like data sharding or caching.

**Outcome**: A system design that not only works functionally but also performs well under load and meets reliability expectations.

---

# Back-of-the-envelope Estimation

In system design interviews, you may be asked to estimate performance requirements or system capacity. These are typically done using thought experiments and common performance numbers. Jeff Dean, a Google Senior Fellow, emphasizes the importance of understanding these mechanisms.

## Power of Two

When dealing with large data volumes, calculations rely on the basics of the power of two. Here are some common data units:

- 2^10 = ~1000 = 1KB
- 2^20 = ~1 million = 1MB
- 2^30 = ~1 billion = 1GB
- 2^40 = ~1 trillion = 1TB
- 2^50 = ~1 quadrillion = 1PB

These approximations are essential when estimating data sizes and storage needs.

## Latency Numbers Every Programmer Should Know

A well-known table, created by Jeff Dean, outlines the duration of typical computer operations. These numbers may be slightly outdated due to hardware improvements, but they still serve as good relative measures among operations:

| Operation                              | Latency         | Description                                                   |
| -------------------------------------- | --------------- | ------------------------------------------------------------- |
| L1 cache reference                     | 0.5ns           | Accessing data from the L1 cache                              |
| Branch mispredict                      | 5ns             | Time taken for a CPU branch misprediction                     |
| L2 cache reference                     | 7ns             | Accessing data from the L2 cache                              |
| Mutex lock/unlock                      | 100ns           | Time to lock and unlock a mutex                               |
| Main memory reference                  | 100ns           | Accessing data from main memory                               |
| Compress 1KB                           | 10,000ns = 10µs | Time to compress 1KB of data                                  |
| Send 2KB over 1Gbps network            | 20,000ns = 20µs | Time to send 2KB of data over a 1Gbps network                 |
| Read 1MB sequentially from memory      | 250µs           | Time to read 1MB of data sequentially from memory             |
| Round trip within the same data center | 500µs           | Time for a round trip within the same data center             |
| Disk seek                              | 10ms            | Time for a disk seek operation                                |
| Read 1MB sequentially from network     | 10ms            | Time to read 1MB of data sequentially from network            |
| Read 1MB sequentially from disk        | 30ms            | Time to read 1MB of data sequentially from disk               |
| Send packet CA -> Netherlands -> CA    | 150ms           | Time to send a packet from California to Netherlands and back |

### Visualization of Latency Numbers

### Key Takeaways

- **Memory is fast, disk is slow**: Minimize disk access when possible.
- **Avoid disk seeks**: Disk seeks are expensive, optimize for sequential reads.
- **Compression is fast**: Compress data before transmission.
- **Data center round trips are costly**: Reduce them where possible to improve latency.

## Availability Numbers

High availability is the ability of a system to be continuously operational, aiming to minimize downtime. Services typically aim for availability between 99% and 100%.

A Service Level Agreement (SLA) defines the level of uptime required for a service. Cloud providers, such as AWS EC2, typically offer SLAs of 99.99%.

### Downtime Based on SLAs

Here's a breakdown of allowed downtime for various SLAs:

| Availability | Downtime Allowed per Month | Downtime Allowed per Year |
| ------------ | -------------------------- | ------------------------- |
| 99.9%        | 7.19 hours                 | 87.6 hours                |
| 99.99%       | 43.2 minutes               | 5.2 hours                 |
| 99.999%      | 4.3 minutes                | 52.6 minutes              |
| 99.9999%     | 25.9 seconds               | 2.43 minutes              |

## Example: Estimating Twitter QPS and Storage Requirements

Let's estimate the queries per second (QPS) and storage needs for Twitter based on some assumptions:

### Assumptions

- 300 million Monthly Active Users (MAU)
- 50% of users use Twitter daily
- Users post 2 tweets per day on average
- 10% of tweets contain media
- Data is stored for 5 years

### Estimations

#### Write Requests Per Second (RPS)
- 150 million * 2 tweets per day / 24 hours / 60 minutes / 60 seconds = 3400-3600 tweets per second
- Peak = 7000 TPS

#### Media Storage Per Day
- 300 million users * 10% = 30 million media posts per day
- Assuming 1MB per media file, 30 million * 1MB = 30TB per day
- Over 5 years: 30TB * 365 days * 5 years = 55PB

#### Tweet Storage Estimation
- 1 tweet = 64-byte ID + 140 bytes text + 1000 bytes metadata
- 3500 tweets/second * 60 seconds * 60 minutes * 24 hours = 302MB per day
- Over 5 years: 302MB * 365 days * 5 years = 551GB

## Estimation Table
Here’s a table showing the minimum and maximum ranges for different components.
| **Component**               | **Min**                        | **Max**                                 |
| --------------------------- | ------------------------------ | --------------------------------------- |
| **HDD (Storage)**           | 500 GB (consumer-level HDD)    | 20 TB (high-capacity enterprise HDD)    |
| **SSD (Storage)**           | 128 GB (consumer SSD)          | 30 TB (enterprise-level NVMe SSD)       |
| **Network (Throughput)**    | 100 Mbps (low-speed broadband) | 100 Gbps (high-performance data center) |
| **RAM (Memory)**            | 2 GB (small consumer devices)  | 12 TB (high-end servers with RDIMMs)    |
| **CPU (Cores)**             | 2 vCPUs (basic consumer CPUs)  | 128 vCPUs (high-performance cloud VMs)  |
| **Disk IOPS (HDD)**         | 75 - 100 (consumer HDD)        | 15,000+ (high-end enterprise HDDs)      |
| **Disk IOPS (SSD)**         | 5,000 (consumer SSD)           | 1,000,000+ (enterprise NVMe SSDs)       |
| **Latency (HDD)**           | ~5 – 10 ms (consumer HDD)      | ~2 – 5 ms (high-end enterprise HDD)     |
| **Latency (SSD)**           | `<1 ms (consumer SSD)`           | ~100 µs (high-end NVMe SSD)             |
| **Power Consumption (HDD)** | ~6 watts (consumer HDD)        | ~12+ watts (enterprise HDDs)            |
| **Power Consumption (SSD)** | ~2 watts (consumer SSD)        | ~10 watts (enterprise SSD)              |
| **Network Latency**         | ~10 ms (home network)          | `<1 ms (high-performance data center)`    |
| **Storage Latency**         | ~5-10 ms (HDD)                 | ~100 µs (NVMe SSD)                      |
| **Bandwidth**               | 10 Mbps (low-end)              | 400 Gbps (high-end networks)            |

Here is an example table for different use cases:

| **Use Case**                      | **Data Volume (per day)** | **Per Second Processing Volume** | **Network Throughput**                    | **Disk IOPS/Throughput**                          | **RAM Requirements**                               | **SQL DB Nodes** | **SQL DB Node Spec (CPU/RAM/Storage)**   | **NoSQL DB Nodes** | **NoSQL DB Node Spec (CPU/RAM/Storage)**          |
| --------------------------------- | ------------------------- | -------------------------------- | ----------------------------------------- | ------------------------------------------------- | -------------------------------------------------- | ---------------- | ---------------------------------------- | ------------------ | ------------------------------------------------- |
| **Simple Web Application**        | 100 MB – 1 GB             | 1.2 KB - 12 KB per second        | 10 Mbps - 100 Mbps                        | Moderate IOPS (~500 - 1,000)                      | 2 – 4 GB (General-purpose VMs)                     | 1 – 2 nodes      | 2 – 4 vCPUs, 4 – 8 GB RAM, 100 GB SSD    | 1 – 2 nodes        | 2 – 4 vCPUs, 8 GB RAM, 100 GB SSD                 |
| **Small Database (e.g., MySQL)**  | 1 GB – 10 GB              | 12 KB - 120 KB per second        | 100 Mbps - 1 Gbps                         | Moderate IOPS (~1,000 - 5,000)                    | 4 – 8 GB (Memory-optimized VMs)                    | 1 – 3 nodes      | 4 – 8 vCPUs, 8 – 16 GB RAM, 500 GB SSD   | 3 – 5 nodes        | 4 – 8 vCPUs, 16 GB RAM, 500 GB SSD                |
| **File Storage/Backup**           | 100 GB – 1 TB             | 1.2 MB - 12 MB per second        | 500 Mbps - 2 Gbps                         | Lower IOPS (~500 - 1,000)                         | 4 – 16 GB (More RAM not critical)                  | 2 – 4 nodes      | 4 vCPUs, 8 GB RAM, 1 TB HDD              | 3 – 5 nodes        | 4 vCPUs, 16 GB RAM, 1 TB HDD                      |
| **Video Streaming**               | 100 GB – 1 TB             | 1.2 MB - 12 MB per second        | 1 Gbps - 10 Gbps                          | High throughput (~100 MB/s or higher)             | 16 – 32 GB (For large buffers)                     | 3 – 5 nodes      | 8 vCPUs, 16 – 32 GB RAM, 2 TB SSD        | 5 – 10 nodes       | 8 vCPUs, 32 GB RAM, 2 TB SSD                      |
| **E-commerce System**             | 10 GB – 500 GB            | 120 KB - 6 MB per second         | 1 Gbps - 10 Gbps                          | Moderate IOPS (~5,000 - 10,000)                   | 16 – 64 GB (For session management and DB caching) | 3 – 5 nodes      | 16 vCPUs, 32 GB RAM, 1 – 2 TB SSD        | 5 – 10 nodes       | 16 – 32 vCPUs, 64 GB RAM, 2 TB SSD                |
| **Analytics/Batch Processing**    | 500 GB – 10 TB            | 6 MB - 120 MB per second         | 1 Gbps - 40 Gbps (parallel processing)    | High IOPS (~10,000 - 50,000)                      | 64 – 128 GB (Depends on dataset size and caching)  | 5 – 10 nodes     | 16 – 32 vCPUs, 64 GB RAM, 2 – 4 TB SSD   | 10 – 20 nodes      | 16 – 32 vCPUs, 64 GB RAM, 2 – 4 TB SSD            |
| **Big Data (e.g., Hadoop/Spark)** | 1 TB – 100 TB             | 12 MB - 1.2 GB per second        | 10 Gbps - 100 Gbps (distributed clusters) | High IOPS (50,000+) or high sequential throughput | 128 GB+ (RAM for in-memory processing and caching) | 10 – 50 nodes    | 32 – 64 vCPUs, 128 GB RAM, 4 – 10 TB HDD | 50 – 100 nodes     | 32 – 64 vCPUs, 128 – 256 GB RAM, 4 – 10 TB SSD    |
| **In-memory DB (e.g., Redis)**    | 100 GB – 1 TB             | 1.2 MB - 12 MB per second        | 1 Gbps - 10 Gbps                          | High IOPS (~100,000+)                             | 512 GB - 1 TB (To store data in memory)            | N/A (NoSQL only) | N/A                                      | 10 – 20 nodes      | 64 – 128 vCPUs, 512 GB RAM, SSD optional          |
| **AI/ML Model Training**          | 1 TB – 10 TB              | 12 MB - 120 MB per second        | 10 Gbps - 40 Gbps                         | High IOPS (~50,000+)                              | 256 GB - 1 TB (For in-memory computations)         | 5 – 10 nodes     | 32 vCPUs, 128 GB RAM, 4 TB SSD           | 10 – 20 nodes      | 32 – 64 vCPUs, 256 GB RAM, SSD/HDD mix            |
| **Financial Trading**             | 100 GB – 1 TB             | 1.2 MB - 12 MB per second        | 10 Gbps - 100 Gbps (ultra-low latency)    | Very high IOPS (100,000+)                         | 128 – 256 GB (For rapid processing)                | 10 – 20 nodes    | 32 – 64 vCPUs, 128 GB RAM, 2 – 4 TB SSD  | 20 – 50 nodes      | 32 – 64 vCPUs, 128 GB RAM, SSD (high-performance) |

## Tips for Back-of-the-envelope Estimation

Back-of-the-envelope estimations are more about the problem-solving process than arriving at exact results. Interviewers are usually assessing your approach and reasoning.

### Key Tips

- **Rounding and Approximation**: Simplify calculations by rounding. For example, instead of calculating 99,987 / 9.1, approximate to 100,000 / 10.
- **State Assumptions**: Write down any assumptions you are making before starting your estimations.
- **Label Units**: Always specify units. For example, write 5MB instead of just 5.
- **Common Estimations**: Focus on estimating queries per second (QPS), peak QPS, storage needs, cache requirements, and the number of servers required.

---

# Scale From Zero to Millions of Users

Here, we're building a system that supports a few users & gradually scales it to support millions.

## Single Server Setup

To start off, we're going to put everything on a single server - web app, database, cache, etc.

![single-server-setup](/images/single-server-setup.png)

### What's the Request Flow?

- User asks DNS server for the IP of my site (i.e. `api.mysite.com -> 15.125.23.214`). Usually, DNS is provided by third-parties instead of hosting it yourself.
- HTTP requests are sent directly to server (via its IP) from your device.
- Server returns HTML pages or JSON payloads, used for rendering.

Traffic to the web server comes from either a web application or a mobile application:

- Web applications use a combo of server-side languages (i.e. Java, Python) to handle business logic & storage. Client-side languages (i.e. HTML, JS) are used for presentation.
- Mobile apps use the HTTP protocol for communication between mobile & the web server. JSON is used for formatting transmitted data.

## Database

As the user base grows, storing everything on a single server is insufficient. We can separate our database on another server so that it can be scaled independently from the web tier.

![database-separate-from-web](/images/database-separate-from-web.png)

### Which Databases to Use?

You can choose either a traditional relational database or a non-relational (NoSQL) one.

- Most popular relational DBs: MySQL, Oracle, PostgreSQL.
- Most popular NoSQL DBs: CouchDB, Neo4J, Cassandra, HBase, DynamoDB.

Relational databases represent & store data in tables & rows. You can join different tables to represent aggregate objects. NoSQL databases are grouped into four categories: key-value stores, graph stores, column stores & document stores.

For most use cases, relational databases are the best option. If not suitable, explore NoSQL databases, which might be better if:

- Application requires super-low latency.
- Data is unstructured or you don't need any relational data.
- You only need to serialize/deserialize data (JSON, XML, YAML, etc.).
- You need to store a massive amount of data.

## Vertical Scaling vs. Horizontal Scaling

- **Vertical scaling** (scale up): Adding more power to your servers (CPU, RAM, etc.).
- **Horizontal scaling** (scale out): Adding more servers to your pool of resources.

### Advantages of Horizontal Scaling

- Can handle larger traffic volumes.
- Avoids the hard limits of vertical scaling.
- Provides failover and redundancy.

## Load Balancer

A load balancer evenly distributes incoming traffic among web servers in a load-balanced set.

![load-balancer-example](/images/load-balancer-example.png)

### How It Works?

- If one server goes down, all traffic is routed to another server.
- More servers can be added to handle spikes in traffic.

## Database Replication

Database replication is usually achieved via master/slave replication (nowadays called primary/secondary replication). A master database supports writes, while slave databases store copies and support read operations.

![master-slave-replication](/images/master-slave-replication.png)

### Advantages

- **Better performance:** Enables more read queries to be processed in parallel.
- **Reliability:** If one database fails, data is still preserved.
- **High availability:** Data remains accessible as long as one instance is operational.

If a master or slave database goes offline, the system promotes a new master and adjusts slaves accordingly.

![master-slave-db-replication](/images/master-slave-db-replication.png)

### Updated Request Lifecycle

1. User gets the IP address of the load balancer from DNS.
2. User connects to the load balancer via IP.
3. HTTP request is routed to server 1 or server 2.
4. Web server reads user data from a slave database or routes data modifications to the master database.

## Cache

A cache is a temporary storage that stores frequently accessed data or results of expensive computations. In our web application, caching can reduce the need for expensive database queries.

### Cache Tier

The cache tier is a temporary storage layer that can be scaled independently from the database.

![cache-tier](/images/cache-tier.png)

### Considerations for Using Cache

- When to use: Useful when data is read frequently but modified infrequently.
- Expiration policy: Controls when cached data expires. Too short leads to frequent DB queries, too long risks stale data.
- Consistency: Ensures the cache is in sync with the database.
- Mitigating failures: Provision servers with extra memory or set them up in multiple locations.
- Eviction policy: Determines what happens when the cache is full. Common policies are LRU, LFU, FIFO.

## Content Delivery Network (CDN)

A CDN is a network of geographically dispersed servers, used for delivering static content (images, HTML, CSS, JS files).

Whenever a user requests static content, the CDN server closest to the user serves it.

![cdn](/images/cdn.png)

### Considerations for Using CDN

- **Cost:** CDNs are managed by third parties, so be mindful of cost.
- **Cache expiry:** Setting an appropriate cache expiry to balance request frequency and data staleness.
- **CDN fallback:** Clients should have a way to fall back if there's a temporary outage.
- **Invalidation:** Use APIs or object versioning to invalidate cache.

## Stateless Web Tier

To scale our web tier, we need to make it stateless by storing session data in persistent storage (relational database or NoSQL).

### Stateful Architecture

Stateful servers remember client data across different requests, making them less flexible.

![stateful-servers](/images/stateful-servers.png)

### Stateless Architecture

Stateless servers don't store user data, allowing HTTP requests to be served by any server.

![stateless-architecture](/images/stateless-architecture.png)

## Data Centers

Clients are geo-routed to the nearest data center based on their IP address.

![data-centers](/images/data-centers.png)

In the event of an outage, traffic is rerouted to a healthy data center.

![data-center-failover](/images/data-center-failover.png)

## Message Queues

Message queues enable asynchronous communication and decouple producers from consumers.

![message-queue](/images/message-queue.png)

Example use-case: Photo processing tasks.

![photo-processing-queue](/images/photo-processing-queue.png)

## Logging, Metrics, and Automation

As the web application grows, monitoring tooling becomes essential.

- **Logging:** Error logs can be emitted to a data store.
- **Metrics:** Collect various types of metrics to monitor system health.
- **Automation:** Continuous integration and deployment detect problems early and improve productivity.

## Database Scaling

There are two approaches to database scaling:

### Vertical Scaling

Adding more resources (CPU, RAM, etc.) to your database nodes. This approach has hardware limits and can be expensive.

### Horizontal Scaling

Add more database nodes instead of upgrading a single one. Sharding is a common horizontal scaling technique.

![database-sharding](/images/database-sharding.png)

In this setup, data is distributed across shards using a partition key.

![user-data-in-shards](/images/user-data-in-shards.png)

## Millions of Users and Beyond

Scaling a system is iterative. Here are key takeaways:

- Keep the web tier stateless.
- Build redundancy at every layer.
- Cache frequently accessed data.
- Support multiple data centers.
- Host static assets in CDNs.
- Scale your data tier via sharding.
- Split your big application into multiple services.
- Monitor your system & use automation.

---

# System Design Examples

Here is a comprehensive list of system design examples to help you practice and understand real-world applications:

- [Ad Click Event Aggregation](technical-concepts/ad-click-event.md)
- [CDN](distributed-systems/cdn.md)
- [Chat](interview/chat.md)
- [Collaborative Document Editing](social-collaboration/collaborative-document-editing.md)
- [Consistent Hashing](interview/consistent-hashing.md)
- [Digital Wallet](business-systems/digital-wallet.md)
- [Distributed Message Queue](interview/distributed-message-queue.md)
- [E Commerce Platform](business-systems/e-commerce-platform.md)
- [Email Service](interview/email-service.md)
- [ELT System](technical-concepts/elt.md)
- [ETL System](technical-concepts/etl.md)
- [Google Drive](data-replication/google-drive.md)
- [Google Maps](location-services/google-maps.md)
- [Hotel Reservation](business-systems/hotel-reservation.md)
- [Image Sharing with News Feed](social-collaboration/image-sharing-with-news-feed.md)
- [Key Value Store](interview/key-value-store.md)
- [Live Streaming](entertainment-streaming/live-streaming.md)
- [Metrics Monitoring and Alerting](distributed-systems/metrics-monitoring-and-alerting.md)
- [Music Streaming Platform](entertainment-streaming/music-streaming.md)
- [Nearby Friends](location-services/nearby-friends.md)
- [Notification System](distributed-systems/notification-system.md)
- [Object Storage](distributed-systems/object-store.md)
- [Online Banking System](business-systems/online-banking.md)
- [Payment System](business-systems/payment-system.md)
- [Proximity Service](location-services/proximity-service.md)
- [Rate Limiter](interview/rate-limiter.md)
- [Real-time Gaming Leaderboard](technical-concepts/realtime-leaderboard.md)
- [Ride Sharing Service](business-systems/ride-sharing-service.md)
- [Search Autocomplete](interview/search-autocomplete.md)
- [Stock Exchange](business-systems/stock-exchange.md)
- [Top K Heavy Hitters](technical-concepts/top-k-heavy-hitters.md)
- [URL Shortener](interview/url-shortener.md)
- [UUID Generator](interview/uuid-generator.md)
- [Video Conferencing System](entertainment-streaming/video-conferencing.md)
- [Video Sharing Platform](entertainment-streaming/video-sharing-platform.md)
- [Web Crawler](distributed-systems/web-crawler.md)
