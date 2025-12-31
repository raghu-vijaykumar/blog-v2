---
sidebar_label: "Reliability & Error Handling Patterns"
---


## Reliability & Error Handling Patterns

### Throttling or Rate Limiting Pattern

#### Introduction
The **Throttling or Rate Limiting** pattern is designed to enhance system reliability by controlling the rate at which resources are consumed. It helps prevent overconsumption of system resources, whether due to malicious activity, legitimate high traffic, or interactions with external services.

#### Problem Statement

![Rate Limiting Pattern](./images/rate-limiting-pattern.png)

Two main issues this pattern addresses:
1. **Overconsumption of Resources:** 
   - High request rates can lead to system overload, potentially causing slowdowns or outages.
   - Unexpected traffic spikes can trigger costly auto-scaling, resulting in financial strain.
2. **Overspending on External Services:** 
   - Systems interacting with external APIs or cloud services can accidentally consume more resources than budgeted, leading to unexpectedly high costs.

#### Types of Throttling
1. **Server-Side Throttling:** 
   - Limits the number of requests to protect the system's backend from overconsumption.
   - Common in scenarios where the system serves multiple clients through an API.

2. **Client-Side Throttling:** 
   - Prevents a client from exceeding a predetermined budget when calling external services.
   - Used when a system consumes external APIs or cloud services to avoid overspending.

#### Strategies for Handling Exceeding Limits
1. **Dropping Requests:** 
   - Requests exceeding the rate limit are dropped. An error response (e.g., HTTP 429 "Too Many Requests") can inform the client.
   - Suitable for non-critical services like fetching real-time data (e.g., stock prices).

2. **Queuing Requests:** 
   - Requests are queued and processed later when capacity allows.
   - Useful for critical operations like executing trades, where delaying is preferable to dropping.

3. **Service Degradation:** 
   - Adjust service quality instead of dropping or delaying requests, such as reducing video quality in streaming services.
   - Can also set an upper limit on resource usage, like the number of media items accessed per day.

#### Considerations for Implementation
1. **Global vs. Customer-Based Throttling:**
   - **Global Throttling:** A single limit applies to all clients, ensuring overall system stability but risking unfair resource allocation.
   - **Customer-Based Throttling:** Individual limits for each client, ensuring fair resource distribution but complicating total request rate management.

2. **External vs. Service-Based Throttling:**
   - **External Throttling:** Limits based on the overall number of API calls from clients, straightforward but can lead to internal service overload.
   - **Service-Based Throttling:** Specific limits for internal services, requiring complex tracking but better protecting individual system components.

#### Conclusion
Throttling is crucial for maintaining system reliability and controlling costs. The choice between different throttling strategies—global vs. customer-based, external vs. service-based—depends on the specific use case and system requirements. Understanding these considerations helps in implementing an effective throttling strategy that balances performance, cost, and resource allocation.

### Retry Pattern

#### Introduction
The **Retry Pattern** is a reliability architecture pattern used to handle errors by retrying operations that have failed. This pattern helps recover from transient issues in a system, such as network failures or temporary unavailability of services.

#### Problem Statement
In cloud environments, errors can occur at any time due to software, hardware, or network issues. When a client calls a service, which in turn may call another service, these errors can cause delays, timeouts, or outright failures. The Retry Pattern aims to handle such situations by retrying failed operations.

#### Key Considerations

![Retry Pattern](./images/retry-service.png)

1. **Error Categorization:**
   - **User Errors:** Errors caused by invalid user actions (e.g., HTTP 403 Unauthorized). These should not be retried; instead, return the error to the user.
   - **System Errors:** Internal errors that may be transient and recoverable (e.g., HTTP 503 Service Unavailable). These are candidates for retries.

2. **Choosing Errors to Retry:**
   - Only retry errors that are likely to be temporary and recoverable, such as timeouts or service unavailability.

3. **Delay and Backoff Strategies:**
   - **Fixed Delay:** A constant delay between retries (e.g., 100 ms).
   - **Incremental Delay:** Increasing the delay gradually after each retry (e.g., 100 ms, 200 ms, 300 ms).
   - **Exponential Backoff:** Exponentially increasing delay (e.g., 100 ms, 200 ms, 400 ms, 800 ms) to reduce load on recovering services.

4. **Randomization (Jitter):**
   - Adding randomness to the delay helps spread out retry attempts, reducing the chance of overloading the system simultaneously.

5. **Retry Limits and Time Boxing:**
   - Set limits on the number of retries or a maximum time period for retries to prevent indefinite retry attempts.

6. **Idempotency:**
   - Ensure that retrying an operation does not cause unintended side effects, such as double billing in a payment system. Only idempotent operations should be retried safely.

7. **Implementation Strategies:**
   - **Shared Library:** Implement retry logic in a reusable library or module.
   - **Ambassador Pattern:** Separate the retry logic from the main application code by running it as a separate process on the same server.

#### Conclusion
The Retry Pattern is a simple yet effective method for handling transient errors in a system. However, careful implementation is crucial to avoid pitfalls like retry storms, and to ensure retries are applied only in appropriate situations. Properly applying the Retry Pattern can enhance system reliability and provide a smoother experience for users.

### Circuit Breaker Pattern

#### Introduction
The **Circuit Breaker Pattern** is a software architecture pattern used to handle long-lasting errors and prevent cascading failures. It contrasts with the Retry Pattern, which is used for short, recoverable issues. The Circuit Breaker Pattern prevents requests from being sent to a failing service, thus saving resources and improving system stability.

#### Real-Life Example
Consider an online dating service that fetches profile images from an image service. If the image service is down for a significant time, retrying requests is futile. Instead, using a circuit breaker can prevent further attempts and conserve resources.

#### Key Concepts

![Circuit Breaker Pattern](./images/circuit-breaker-pattern.png)

1. **Circuit States:**
   - **Closed:** All requests are allowed through, and the system tracks success and failure rates.
   - **Open:** No requests are allowed through; failures are assumed to continue. The system immediately returns errors.
   - **Half-Open:** A limited number of requests are allowed to test if the service has recovered.

2. **Operation**
   - The circuit starts in a **Closed** state, allowing requests and tracking failures. If failures exceed a certain threshold, the circuit moves to an **Open** state, blocking requests. After a timeout, it transitions to a **Half-Open** state to test the service's health. Depending on the results, it either closes the circuit or returns to the open state.

#### Implementation Considerations

1. **Handling Requests in Open State:**
   - **Drop Requests:** Simply ignore them, with proper logging for analysis.
   - **Log and Replay:** Record the requests for later processing, useful in critical systems like e-commerce.

2. **Response Strategy in Open State:**
   - **Fail Silently:** Return empty responses or placeholders (e.g., a placeholder image in a dating app).
   - **Best Effort:** Provide cached or old data if available.

3. **Separate Circuit Breakers for Each Service:**
   - Each external service should have its own circuit breaker to prevent one failing service from affecting others.

4. **Asynchronous Health Checks:**
   - Instead of real requests in the Half-Open state, use small, asynchronous health checks to determine service recovery. This approach conserves resources and reduces impact on the recovering service.

5. **Implementation Location:**
   - As with the Retry Pattern, the Circuit Breaker can be implemented as a shared library or through an Ambassador Sidecar, especially useful for services in different programming languages.

#### Conclusion
The Circuit Breaker Pattern is crucial for managing long-lasting errors, preventing resource waste, and improving system resilience. It involves careful consideration of how to handle failed requests, the state management of the circuit, and the method of implementation. Properly implementing this pattern can significantly enhance the stability and reliability of a distributed system.

### Dead Letter Queue Pattern

#### Overview
The **Dead Letter Queue (DLQ)** pattern is designed to handle message delivery failures in an event-driven architecture. It helps manage errors in publishing and consuming messages through a message broker or distributed messaging system.

#### Event-Driven Architecture

![Dead Letter Queue Pattern](./images/dead-letter-q.png)

In an event-driven system, three key components are involved:
- **Event Publishers:** Produce messages or events.
- **Message Broker:** Manages channels, topics, or queues for message distribution.
- **Consumers:** Read and process incoming messages.

While this architecture offers benefits like decoupling and scalability, it also introduces potential points of failure.

#### Real-Life Example
Consider an online store with:
- **Order Service:** Publishes order events.
- **Inventory, Payment, Fulfillment Services:** Subscribe to these events and process them based on the product type (physical vs. digital).

Potential issues include:
- Order service publishing to a non-existent or full queue.
- Consumers facing issues reading or processing messages, which could clog the queue and delay other messages.

#### Dead Letter Queue (DLQ)

1. **Purpose:**
   - A special queue for messages that cannot be delivered or processed successfully.
   - Helps in isolating problematic messages and preventing them from affecting the main queue.

2. **Message Entry into DLQ:**
   - **Programmatic Publishing:** Consumers or publishers manually move messages to the DLQ if they encounter issues.
   - **Automated Configuration:** Message brokers can be configured to automatically move messages to the DLQ due to delivery failures or other issues.

3. **Configuration Support:**
   - Most open-source or cloud-based message brokers support DLQ functionality.

4. **Processing Messages in DLQ:**
   - **Monitoring and Alerting:** Regular monitoring ensures that messages in the DLQ are addressed and not forgotten.
   - **Error Details:** Attach headers or additional information to messages to understand and fix issues.
   - **Handling:**
     - **Automatic Republishing:** Once issues are resolved, messages can be moved back to the original queue for normal processing.
     - **Manual Processing:** Rare cases or special scenarios might require manual intervention.
