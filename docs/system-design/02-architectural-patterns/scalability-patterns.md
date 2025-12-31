---
sidebar_label: "Scalability Patterns"
---


## Scalability Patterns

### Load Balancing in Scalable System Architectures

Load balancing is a software architecture pattern used to distribute incoming requests across multiple servers, allowing systems to scale efficiently and maintain performance under high traffic conditions. Single cloud servers are insufficient for handling high volumes of requests, leading to crashes or performance issues. Upgrading servers only postpones the problem. A dispatcher routes incoming requests to available worker servers, enabling load distribution and scalability.

#### Use Cases

- **HTTP Requests:** Distributes front-end requests (web/mobile) to back-end servers.
- **Microservices Architecture:** Manages service instances, enabling independent scaling for each service.

#### Implementation Methods

- **Cloud Load Balancing Services:**
  - Managed services that route requests and can scale automatically.
  - Avoid becoming a single point of failure.
    ![load-balancing](./images/load-balancing.png)
- **Message Brokers:**
  - Used for asynchronous, one-directional communication between services.
  - Useful for internal load balancing of message queues.
    ![msg-broker](./images/msg-broker-as-internal-load-balancer.png)

#### Routing Algorithms

- **Round Robin:** Sequentially distributes requests, suitable for stateless applications. Example: Most of the stateless API's
- **Sticky Sessions:** Routes requests from the same client to the same server, ideal for stateful applications. Example: Banking, Financial transactions, Multipart file upload.
- **Least Connection:** Directs requests to servers with the fewest active connections, suitable for long-term connections. Example: SQL, LDAP.

#### Auto Scaling Integration

- **Auto Scaling:** Automatically adjusts the number of servers based on metrics like CPU usage and traffic.
- Works in conjunction with load balancing to optimize resource use and cost.

##### Example of Auto Scaling Integration

**Scenario:**
A cloud-based e-commerce platform experiences variable traffic, with peaks during sales events and lower traffic during off-peak hours.

**Auto Scaling Integration Steps:**

![Auto Scaling](./images/auto-scaling-group.png)

1. **Monitoring and Metrics Collection:**

   - Server instances in the cloud environment run monitoring agents to collect metrics such as CPU utilization, memory usage, and network traffic.

2. **Defining Auto Scaling Policies:**

   - Policies are established based on the collected metrics. For example:
     - **Scale Up Policy:** Add more server instances if the average CPU utilization exceeds 70% for 5 consecutive minutes.
     - **Scale Down Policy:** Remove server instances if the average CPU utilization drops below 30% for 10 consecutive minutes.

3. **Load Balancer Coordination:**

   - The load balancer is configured to recognize the dynamic pool of server instances. It automatically adjusts the routing of incoming requests based on the current set of available instances.

4. **Implementation Example:**
   - **Cloud Load Balancer:** Utilizes a cloud provider's load balancing service to distribute incoming traffic across a pool of identical web server instances.
   - **Auto Scaling Group:** The web server instances are managed as an auto-scaling group within the cloud provider's infrastructure.
   - **Elasticity in Action:**
     - During a flash sale, the traffic spikes, causing the average CPU utilization to rise above 70%. The auto-scaling policy triggers, launching additional server instances to handle the increased load.
     - The load balancer detects the new instances and routes traffic to them, balancing the load.
     - After the sale, as traffic decreases, the average CPU utilization falls below 30%. The auto-scaling policy triggers the removal of excess instances, reducing costs.

**Benefits:**

- **Dynamic Scalability:** Automatically adapts to traffic changes, ensuring optimal performance.
- **Cost Efficiency:** Reduces infrastructure costs by scaling down during low-traffic periods.
- **Resilience:** Prevents server overload and potential crashes by distributing the load and adding capacity when necessary.

This example illustrates how auto scaling, combined with a load balancer, efficiently manages variable workloads in a cloud environment, providing both scalability and cost-effectiveness.

References

- [Google Compute Engine Autoscaling Groups](https://cloud.google.com/compute/docs/autoscaler)
- [GKE Cluster Autoscaling](https://cloud.google.com/kubernetes-engine/docs/concepts/cluster-autoscaler)
- [Amazon EC2 Autoscaling](https://docs.aws.amazon.com/autoscaling/ec2/userguide/what-is-amazon-ec2-auto-scaling.html)

#### Considerations

- **Session Management:** Choosing the right routing algorithm depends on whether the application is stateless or stateful.
- **Scalability and Cost Efficiency:** Combining load balancing with auto scaling helps in dynamically resizing the backend infrastructure, ensuring optimal resource utilization and cost savings.

This documentation outlines the essential aspects of implementing and managing load balancing in scalable system architectures, highlighting methods, algorithms, and practical considerations for effective deployment.

### Pipes and Filters Architecture Pattern

![pipes and filter](./images/pipes-and-filter-approach.png)

#### Overview

- **Analogy:** Data flows like water through a series of pipes and filters.
- **Components:**
  - **Data Source:** Origin of the data, e.g., backend services, sensors.
  - **Filters:** Isolated software components that process data.
  - **Pipes:** Mechanisms like distributed queues or message brokers that connect filters.
  - **Data Sink:** Final destination for processed data, e.g., databases, external services.

#### Key Concepts

- **Filters:** Perform single operations, unaware of the rest of the pipeline.
- **Pipes:** Can store data temporarily or use message systems for notifications.


#### Benefits

1. **Decoupling:**
   - Allows different processing tasks to use different programming languages and technologies.
2. **Hardware Optimization:**
   - Each task can run on the most suitable hardware (e.g., specialized hardware for machine learning).
3. **Scalability:**
   - Each filter can be scaled independently based on workload needs.

#### Real-World Use Cases

- **Digital Advertising:** Processing streams of user activity data.
- **Internet of Things (IoT):** Data processing from end devices.
- **Media Processing:**
  - Video and audio processing pipelines, including chunking, thumbnail creation, resolution resizing, adaptive streaming, and captioning.

#### Example: Video Sharing Service

1. **Video Processing Pipeline:**

![video-sharing-arch](./images/video-sharing-service-architecture.png)

- **Chunking:** Split video into smaller chunks.
- **Thumbnail Extraction:** Select frames as thumbnails.
- **Resolution and Bitrate Adjustment:** Resize chunks for adaptive streaming.
- **Encoding:** Encode chunks into different formats.

2. **Audio Processing Pipeline:**
   - **Transcription:** Convert speech to text.
   - **Captioning and Translation:** Provide captions and translate into different languages.
   - **Content Moderation:** Detect copyrighted or inappropriate content.

#### Considerations

1. **Complexity:**
   - Maintaining this architecture can be complex, especially with granular filters.
2. **Stateless Filters:**
   - Each filter should be independent and stateless.
3. **Transaction Handling:**
   - Not suitable for scenarios requiring a single transaction across the entire pipeline.

The pipes and filters pattern is valuable for scenarios needing flexible, scalable, and decoupled processing pipelines but may not be ideal for transactional data processing.


### Scatter-Gather Architecture Pattern

![scatter-gatherer-pattern](./images/scatter-gatherer-pattern.png)

#### Overview

- **Components:**
  - **Sender/Requester:** Initiates the request.
  - **Workers:** Respond to the request; can be internal or external services.
  - **Dispatcher:** Distributes the request to all workers and collects responses.
  - **Aggregator:** Combines responses from workers into a single response.

#### Key Concepts

- **Parallel Processing:**
  - Requests are sent to all workers simultaneously, allowing for parallel processing.
  - Workers can be diverse, performing different functions or accessing different data.

#### Use Cases

1. **Search Services:**

![search service](./images/serach-service.png)

   - Users send a query, and internal workers search through various data subsets.
   - Results are aggregated and returned as a ranked list.

2. **Hospitality Services:**

![Hospitatlity service](./images/hospitality-service.png)

   - A request for hotel availability is sent to multiple hotels.
   - Responses are collected and sorted based on criteria like price or rating.

#### Considerations

1. **Timeouts:**
   - Set an upper limit for waiting for worker responses to avoid delays due to unresponsive workers.
2. **Decoupling:**
   - Use a message broker to decouple the dispatcher and workers, facilitating asynchronous communication.
3. **Long-running Tasks:**
   - For tasks requiring extensive processing, separate the dispatcher and aggregator.
   - Use unique identifiers for tracking and retrieving results.

#### Example Workflow

1. **Immediate Response Use Case:**

   - Dispatcher sends a request to workers.
   - Workers process and return responses quickly.
   - Aggregator compiles the results and sends them to the user.

2. **Long-running Task Use Case:**
   - Dispatcher assigns a unique ID to the request and sends it to workers.
   - Workers return partial results with the same ID.
   - Aggregator stores and compiles results, accessible via the unique ID.

##### Benefits

- **Scalability:**
  - Supports high scalability by enabling parallel processing across numerous workers.
- **Flexibility:**
  - Can integrate diverse internal and external services.
- **Resilience:**
  - Can continue processing even if some workers are unavailable or slow.

The scatter-gather pattern is versatile and widely used in many production systems, providing efficient parallel processing and aggregation of results.

### Execution Orchestrator Pattern

![executor-orchestrator](./images/executor-orchestrator-pattern.png)

#### Overview
- **Purpose:** Manages a sequence of operations across multiple services in microservices architecture.
- **Analogy:** Like a conductor in an orchestra, the orchestrator directs services without performing the business logic itself.

#### Key Concepts
- **Execution Orchestrator:** A centralized service that:
  - Calls different services in the right order.
  - Handles exceptions and retries.
  - Maintains the state of the flow until completion.
- **Microservices:** Individual services responsible for specific business logic, typically stateless and independently scalable.

#### Use Case Example: Video on Demand Service

![Video on Demand Service](./images/video-on-demand-user-onboarding.png)

- **User Registration Flow:**
  1. User fills out a registration form (username, password, payment).
  2. **Orchestrator Service** handles:
     - **User Service:** Validates username and password.
     - **Payment Service:** Authorizes credit card via a third-party API.
     - **Location Service:** Registers user location for content access.
     - **Recommendation Service:** Sets up user preferences.
     - **Email Service:** Sends a confirmation email with details.

#### Considerations
1. **Scalability:** 
   - Orchestrator and microservices can be scaled independently.
   - Orchestrator can be deployed across multiple instances for reliability.
2. **Failure and Recovery:**
   - Orchestrator handles errors, retries, and manages the flow's state.
   - Maintains a database to persist the state for recovery in case of failures.
3. **Avoiding Monolithic Tendencies:**
   - Keep the orchestrator focused on coordination, not business logic.

#### Benefits
- **Decoupling:** Microservices operate independently and are unaware of the orchestration.
- **Efficiency:** Supports parallel and sequential operations.
- **Flexibility:** Easy to modify the flow by updating the orchestrator.

This pattern is particularly useful in complex systems requiring coordination of multiple independent services, offering a scalable and maintainable solution for executing business logic workflows.

### Choreography Pattern

![Choreography Pattern](./images/choregraphy-pattern.png)

#### Overview
- **Purpose:** Helps scale complex flows of business transactions in microservices architecture.
- **Comparison:** Unlike the orchestration pattern, choreography uses asynchronous events without a central orchestrator.

#### Key Concepts
- **Microservices:** Decoupled services that communicate through a message broker.
- **Message Broker:** A distributed message queue that stores and distributes events.

#### Advantages
- **Loose Coupling:** Services operate independently and are not tightly coupled through a central orchestrator.
- **Scalability:** Easy to add or remove services and scale operations.
- **Cost Efficiency:** Services can be implemented as functions that only run when triggered, saving resources.

#### Example: Job Search Service

![Job Search Service](./images/job-search-service.png)

1. **User Registration:** 
   - User submits a form with their details and resume.
   - **Candidate Service:** Stores data and emits an event.
2. **Email Confirmation:** 
   - Triggered by the event, an email confirmation is sent to the user.
3. **Skills Parsing:** 
   - **Skills Parser Service:** Extracts and stores skills data, then emits an event.
4. **Job Search:** 
   - **Job Search Service:** Searches for job matches and emits results as an event.
5. **Job Notifications:** 
   - **Candidate Service:** Updates user records.
   - **Email Service:** Sends job notifications based on user preferences.

#### Considerations
1. **Debugging Challenges:** 
   - Troubleshooting issues can be difficult due to lack of a central coordinator.
   - Harder to trace the flow of events and identify issues.
2. **Testing Complexity:** 
   - Requires complex integration tests to catch issues before production.
   - Becomes more challenging as the number of services grows.
