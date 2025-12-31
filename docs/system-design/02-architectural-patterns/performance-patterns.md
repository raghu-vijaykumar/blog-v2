---
sidebar_label: "Performance Patterns"
---

## Performance Patterns

### MapReduce Pattern
![map-reduce](./images/map-reduce-pattern.png)

#### Overview
- **Origin:** Introduced by Jeff Dean and S.J. Ghemawat from Google in 2004.
- **Purpose:** Simplifies processing of large data sets by distributing computation across many machines.

#### Key Concepts
- **Map Function:** Transforms input key-value pairs into intermediate key-value pairs.
- **Reduce Function:** Aggregates and processes intermediate pairs to produce final output.

#### Example: Word Count in Text Files
1. **Input:** Key-value pairs (filename, content).
2. **Map Function:** Emits (word, 1) for each word in the content.
3. **Shuffle and Sort:** Groups intermediate pairs by word.
4. **Reduce Function:** Sums the counts for each word.

#### Architecture

![map-reduce-architecture](./images/map-reduce-architecture.png)
> TODO: Add backup master and snapshot storage in the image


- **Master Node:** Orchestrates the computation, schedules tasks, and handles failures.
- **Worker Nodes:** Execute map and reduce tasks in parallel.
- **Data Distribution:** Input data is split into chunks for parallel processing.

#### Fault Tolerance
- **Worker Failures:** Master reassigns tasks and notifies reduce workers of new data locations.
- **Master Failures:** Can either restart the process or use a backup master to continue.

#### Cloud Integration
- **Scalability:** Cloud environments provide access to many machines, enabling large-scale data processing.
- **Cost Efficiency:** MapReduce's batch processing nature means paying only for resources used during the job, not for maintaining idle machines.


### Saga Pattern

#### Introduction
- **Context:** Microservices architecture, where each service has its own database.
- **Problem:** Ensuring data consistency across multiple databases without a central database, losing traditional ACID transactions.

#### Solution: Saga Pattern

![distributed-transaction](./images/saga-pattern-distributed-transaction-success.png)
![distributed-rollback](./images/distributed-rollback.png)

- **Definition:** Manages data consistency in distributed transactions by breaking them into a series of local transactions. If an operation fails, compensating transactions are executed to roll back.

#### Implementation Methods
1. **Execution Orchestrator Pattern:**
   - A central orchestrator service manages the transaction flow, calling services sequentially.
   - Decides whether to continue or rollback based on service responses.

2. **Choreography Pattern:**
   - No central orchestrator; services communicate through a message broker.
   - Each service listens for events and triggers subsequent steps or compensations as needed.

#### Example Scenario: Ticket Reservation System

![Ticket Reservation System](./images/movie-ticketing-system.png)

1. **Services Involved:** Order, Security, Billing, Reservation, Email, (Orchestration if using orchestrator pattern).
2. **Process:**
   - **Order Service:** Registers an order and sets it to pending.
   - **Security Service:** Validates the user (not a bot or blacklisted).
   - **Billing Service:** Authorizes the pending transaction on the user's credit card.
   - **Reservation Service:** Reserves the ticket for the user.
   - **Email Service:** Sends a confirmation email.

3. **Failure Handling:**
   - If any service returns a failure, compensating operations are triggered:
     - Cancel pending transactions.
     - Remove records from databases.
   - Compensating operations ensure consistency by undoing previous steps if necessary.

#### Conclusion
- **Purpose:** Maintains data consistency in a microservices environment by handling distributed transactions.
- **Flexibility:** Can be implemented using either the execution orchestrator pattern or the choreography pattern.
- **Resilience:** Provides mechanisms to complete transactions or roll back in case of failures.

The saga pattern is crucial for ensuring reliable operations and consistency in complex systems with distributed architectures.

### Transactional Outbox Pattern

#### Overview
- **Problem:** In event-driven architectures, ensuring that a database update and an event publication occur together reliably can be challenging. Specifically, there's a risk of losing events or data if a system crash occurs between these operations.

![Transactional Outbox Pattern](./images/transactional-outbox-pattern.png)

#### Solution: Transactional Outbox Pattern
- **Concept:** Involves adding an **Outbox Table** to the database to store messages intended for the message broker. Updates to both the business logic table (e.g., users) and the Outbox Table are performed within a single database transaction.

#### Implementation Steps
1. **Database Update and Message Logging:**
   - Instead of sending an event directly to the message broker, the service logs the message to the Outbox Table along with updating the primary business logic table.
   - Ensures atomicity: Both tables are updated together, or neither is, preventing partial updates.

2. **Message Sender/Relay Service:**
   - A separate service monitors the Outbox Table for new messages.
   - Upon finding a new message, it sends it to the message broker and marks it as sent (or deletes it).

#### Addressing Potential Issues
1. **Duplicate Events:**
   - **Cause:** A crash between sending a message and marking it as sent can result in duplicate events.
   - **Solution:** Implement "at least once" delivery semantics. Each message gets a unique ID. Consumers track processed message IDs to discard duplicates.

2. **Lack of Transaction Support:**
   - **Scenario:** Some databases, especially NoSQL ones, may not support multi-collection transactions.
   - **Solution:** Embed the Outbox message directly within the same document (or object) in the database. The sender service queries for documents with messages, sends them, and then clears the messages.

3. **Ordering of Events:**
   - **Problem:** Ensuring the order of related events, such as a user signup followed by a cancellation.
   - **Solution:** Assign each message a sequential ID. The sender service can then sort and send messages in the correct order based on these IDs.

#### Conclusion
- **Benefits:** The transactional outbox pattern provides a robust solution for ensuring data consistency and reliable event publication in distributed systems.
- **Considerations:** Addressing potential issues like duplicate events, lack of transaction support, and event ordering is crucial for effective implementation.

The transactional outbox pattern is an essential tool for maintaining consistency in microservices architectures, particularly in systems that rely heavily on event-driven communication.

### Materialized View Pattern

#### Overview
- **Purpose:** To optimize performance and cost efficiency in data-intensive applications by pre-computing and storing query results.

![Materialized View Pattern](./images/materialized-views.png)

#### Problem Statement
1. **Performance:** Complex queries, especially those involving multiple tables or databases, can be slow.
2. **Cost:** Repeatedly running the same complex queries can be resource-intensive, leading to high costs in a cloud environment.

#### Solution: Materialized View
- **Concept:** Create a read-only table (materialized view) that stores the pre-computed results of a specific query. This allows for quick data retrieval without recalculating the query each time.

#### Implementation
1. **Data Storage:**
   - Store materialized views as separate tables in the same database or in a read-optimized separate database.
   - In cases of frequent data updates, the materialized view can be regenerated immediately or on a fixed schedule.

2. **Example Use Case:**
   - **Scenario:** An online education platform with tables for courses and reviews.
   - **Need:** Display top courses based on average ratings for a specific topic.
   - **Solution:** Create a materialized view storing pre-computed course ratings, which can be filtered quickly based on the topic.

3. **Benefits:**
   - **Performance:** Significantly reduces query time by avoiding complex aggregations and joins.
   - **Cost Efficiency:** Saves resources by reducing the need for repeated complex query execution.

#### Considerations
1. **Storage Space:** Materialized views require additional storage, increasing costs. The trade-off between performance and space must be evaluated.
2. **Update Frequency:** 
   - **Same Database:** If the database supports materialized views, updates can be automatic and efficient.
   - **External Storage:** Requires manual or programmatic updates, which can add complexity.

#### Conclusion
- The materialized view pattern is a powerful tool for optimizing the performance of data-intensive applications. By pre-computing and storing query results, this pattern improves user experience and reduces operational costs, especially in environments where resource usage incurs significant expenses.

### CQRS (Command and Query Responsibility Segregation)

#### Overview
- **Purpose:** To separate the command (write) and query (read) responsibilities in a system into distinct services and databases, optimizing each for its specific workload.

![cqrs-pattern](./images/cqrs-pattern.png)

#### Key Concepts
1. **Command Operations:** Actions that mutate data, such as insertions, updates, and deletions.
2. **Query Operations:** Actions that read and return data without altering it.

#### Benefits of CQRS
1. **Separation of Concerns:** 
   - Command service handles business logic, validations, and data mutations.
   - Query service focuses solely on data retrieval and presentation.
2. **Independent Scalability:** 
   - The number of instances for each service can be scaled independently based on demand.
3. **Optimized Data Models:** 
   - Command database can be optimized for write operations.
   - Query database can be optimized for read operations, often using different database technologies.

#### Example Use Case: Online Store
- **Command Side:** 
  - Stores user reviews in a relational database.
  - Ensures business logic, such as validating purchases and review content.
- **Query Side:** 
  - Stores reviews and average ratings in a NoSQL database, optimized for quick retrieval.
  - Contains pre-aggregated data to minimize real-time computations.

#### Synchronization
- **Event Publishing:** 
  - On data mutation, the command service publishes events to keep the query database updated.
  - Can use message brokers or functions-as-a-service for event handling and data synchronization.
- **Eventual Consistency:** 
  - The system guarantees eventual consistency between command and query databases but not strict consistency.

#### Considerations
- **Complexity:** 
  - Requires managing multiple services, databases, and synchronization mechanisms.
  - Adds overhead but provides significant performance benefits.
- **Eventual Consistency:** 
  - Suitable for scenarios where eventual consistency is acceptable, not for strict consistency requirements.


### Combining CQRS and Materialized View Patterns in Microservices

#### Overview
- **Problem:** In a microservices architecture, data is often split across multiple services and databases, making it challenging to aggregate data efficiently for queries.
- **Solution:** Use a combination of CQRS and materialized view patterns to optimize data retrieval and maintain synchronization across services.

![CQRS and Materialized View Patterns](./images/cqrs-materialized-view.png)

#### Key Concepts
1. **Microservices Split:** 
   - Initially, all data might reside in a single database. Splitting into microservices involves dividing this data into separate databases, each handled by a specific service.
   - This division prevents traditional join operations across different microservices' databases.

2. **Performance Challenges:** 
   - Aggregating data across multiple services requires API calls, database queries, and programmatic joins, leading to significant performance overhead.

#### Solution: Combining Patterns
1. **CQRS (Command and Query Responsibility Segregation):**
   - Create a new microservice with a read-optimized database specifically for querying aggregated data.
   - This microservice only handles queries and does not perform data mutations.

2. **Materialized View:**
   - A materialized view is created to pre-aggregate and store relevant data from multiple microservices.
   - The view includes data necessary for user queries, combining information from different microservices.

#### Synchronization Methods
1. **Message Broker:**
   - Each microservice publishes events to a message broker when data changes.
   - The query microservice listens to these events and updates the materialized view accordingly.

2. **Cloud Function:**
   - A function as a service monitors tables in different services' databases.
   - On detecting changes, the function updates the materialized view in the query database.

#### Real-World Example: Online Education Platform
- **Setup:**
  - *Courses Microservice:* Manages course data (name, description, price).
  - *Reviews Microservice:* Handles reviews and ratings.
  - *Course Search Service:* New service with a materialized view that includes course details and reviews.

- **Data Flow:**
  - Changes in the course details or reviews trigger events.
  - The Course Search Service updates its materialized view based on these events, ensuring quick access to aggregated data.

#### Benefits
- **Efficient Data Aggregation:** 
  - Combines data from multiple microservices into a single, query-optimized view.
- **Performance Optimization:** 
  - Reduces the need for complex, runtime data joins and minimizes latency.
- **Scalability and Maintainability:** 
  - Isolates query logic, making it easier to maintain and scale based on query load.

#### Conclusion
- The combination of CQRS and materialized views effectively addresses the challenges of data aggregation in microservices architectures. This approach optimizes data retrieval, maintains synchronization, and allows for scalable and maintainable systems.

### Event Sourcing Pattern

#### Overview
Event sourcing is an architecture pattern where the state of an application is derived from a sequence of events rather than storing the current state directly.

![event sourcing](./images/event-sourcing.png)

#### Traditional Data Handling
- **CRUD Operations:** Applications typically use Create, Read, Update, and Delete operations to manage data, focusing on the current state.
- **Current State:** The database stores the current state of entities, and any modifications overwrite previous states.

#### Need for Event Sourcing
- **Limited by Current State:** In some cases, knowing only the current state isn't sufficient. Historical data or the sequence of changes may be crucial.
- **Examples:**
  - **Banking:** Clients need to see transaction histories, not just current balances.
  - **E-commerce:** Merchants might need to understand inventory changes, not just the current stock level.

#### Event Sourcing Explained
- **Events Over State:** Instead of the current state, the system stores events that describe changes or facts about entities.
- **Immutability:** Events are immutable; once logged, they cannot be changed.
- **Replaying Events:** The current state is derived by replaying all events related to an entity.

#### Benefits of Event Sourcing
- **Complete History:** Retains a full history of changes, useful for auditing and troubleshooting.
- **Performance:** Improved write performance, as events are appended rather than updating a state.

#### Storing Events
1. **Database:** Each event can be stored as a record, allowing for complex queries and analytics.
2. **Message Broker:** Events can be published for consumption by other services, ensuring order and scalability.

#### Challenges and Optimizations
- **Replaying All Events:** Reconstructing state by replaying all events can be inefficient.
  - **Snapshots:** Periodically save the current state to reduce the number of events replayed.
  - **CQRS (Command Query Responsibility Segregation):** Separate systems for handling commands (writes) and queries (reads). This allows for efficient read operations using a read-optimized database.

#### Combining Event Sourcing and CQRS
- **Eventual Consistency:** Combining these patterns often results in eventual consistency, which may be acceptable depending on the use case.
- **Benefits:**
  - **Auditing and History:** Complete record of all changes.
  - **Efficient Writes and Reads:** Separate systems optimize performance.

### Big Data Processing and Lambda Architecture

#### Overview

In big data processing, two main strategies exist: batch processing and real-time processing. Batch processing allows for deep insights into historical data and the fusion of data from various sources, but it has a higher delay between data collection and availability for querying. Real-time processing provides immediate visibility and response to incoming data but is limited to recent information without deep historical context.

#### The Challenge

Deciding between batch and real-time processing can be difficult, as many systems require the benefits of both strategies. For instance, systems that aggregate logs and performance metrics, or those used in ride-sharing services, need both real-time insights for immediate actions and historical analysis for detecting patterns and optimizing performance.

#### Lambda Architecture

![Lambda Architecture](./images/lambda-pattern.png)

The Lambda Architecture addresses this challenge by combining batch and real-time processing, offering the best of both worlds. It consists of three layers:

1. **Batch Layer**: 
   - Manages the master dataset as a system of records, storing immutable data.
   - Pre-computes batch views for in-depth analysis and data corrections.
   - Operates on the entire dataset for perfect accuracy.

2. **Speed Layer**: 
   - Handles real-time data processing to provide low-latency views.
   - Processes data as it arrives and updates real-time views.
   - Focuses on recent data without complex corrections or historical context.

3. **Serving Layer**: 
   - Merges data from both batch and real-time views.
   - Responds to ad-hoc queries, providing a comprehensive view combining historical and real-time data.

#### Practical Application

A practical example of Lambda Architecture is in the ad tech industry, where advertisers and content producers interact through an ad-serving system. The system must process three types of events: ad views, ad clicks, and user purchases. These events are processed by both the batch and speed layers to provide:

- Real-time data, such as the number of users currently viewing ads.
- Combined data for queries like the total ads shown in the last 24 hours, merging batch and speed layer data.
- In-depth analysis, like determining the return on investment for advertisers, which requires historical data fusion and analytics.

#### Conclusion

Lambda Architecture effectively handles scenarios requiring both real-time and batch processing capabilities. It allows for comprehensive data analysis and immediate responsiveness, making it a versatile solution for modern big data systems.
