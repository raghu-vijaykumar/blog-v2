---
title: "Paxos Variant for Strong Consistency Guarantees"
description: "System design example for Paxos Variant for Strong Consistency Guarantees"
---

# Multi-Paxos: A Variant for Strong Consistency Guarantees

## Overview
Multi-Paxos is an optimized variant of the classic Paxos consensus algorithm designed to efficiently handle multiple consensus instances in sequence, rather than running an independent Paxos instance for each decision. It's particularly suited for distributed systems requiring strong consistency guarantees, such as distributed databases and state machines where a sequence of commands must be agreed upon across multiple nodes.

**Why it's Important**: While basic Paxos provides consensus for a single value, real-world systems often need to agree on a sequence of values (log replication). Multi-Paxos optimizes this by electing a leader once and reusing it for subsequent instances, dramatically improving throughput while maintaining Paxos' core safety properties: validity, agreement, termination, and integrity.

**Real-world Context**: Multi-Paxos powers distributed databases like Apache Cassandra, Google's Chubby, and forms the foundation for protocols like Raft (which is often described as an understandable Multi-Paxos). It's used in scenarios where strong consistency is critical during network partitions.

## Core Principles & Components

Multi-Paxos builds on Paxos' core roles (proposers, acceptors, learners) with additional optimizations for multiple consensus instances. The key innovation is the **leader election and reuse** mechanism.

**Core Components**:
1. **Proposers**: Nodes that initiate proposals (typically only one "leader" at a time proposes)
2. **Acceptors**: Maintain persistent state and vote on proposals
3. **Learners**: Learn the chosen values (often co-located with acceptors)

**Leader Role**: A distinguished proposer that coordinates consensus instances. The leader:
- Sends Phase 1 requests to establish its leadership
- Once elected, handles Phase 2 for sequential consensus instances
- Relinquishes leadership if it suspects failures

**State Transitions**:
```
stateDiagram-v2
    [*] --> Follower: Start
    Follower --> Candidate: Timeout
    Candidate --> Leader: Elected
    Leader --> Follower: FailureDetected
    note right of Leader: Optimizes Phase 2 for sequence
```

## Detailed Implementation Design

### A. Algorithm / Process Flow

Multi-Paxos operates in phases similar to basic Paxos but with optimizations:

1. **Leader Election Phase**:
   - Candidate proposers send Phase 1a (Prepare) messages with unique proposal numbers
   - Acceptors respond with promises and previously promised values
   - Candidate becomes leader if it receives promises from majority

2. **Log Replication Phase** (streamlined Phase 2):
   - Leader sends Accept messages with sequential slot numbers
   - Acceptors accept if not previously promised to higher numbers
   - Learners deliver once majority accept

**Failure Handling**:
- If leader fails, followers timeout and start new election
- Acceptors resend missing Phase 2 responses during catch-up
- Duplicates handled via idempotent operations

**Concurrency**: Multiple Phase 1 elections can overlap, but only one leader emerges via proposal number ordering.

```java
public class MultiPaxosConsensus {
    private final AtomicLong proposalNumber = new AtomicLong(0);
    private final Map<Long, Proposal> slots = new ConcurrentHashMap<>();
    private volatile boolean isLeader = false;

    public void runConsensus(LogEntry entry) {
        if (!isLeader) {
            return; // Only leader proposes
        }

        long slot = proposalNumber.incrementAndGet();
        Proposal proposal = new Proposal(slot, entry);

        // Phase 2a: Send to acceptors
        for (Acceptor acceptor : acceptors) {
            if (acceptor.accept(proposal)) {
                proposal.incrementAcceptCount();
            }
        }

        // Check majority
        if (proposal.getAcceptCount() > acceptors.size() / 2) {
            deliverToLearners(proposal);
        }
    }
}
```

### B. Data Structures & Configuration Parameters

**Core Data Structures**:
- **AcceptorState**: `promisedProposal`, `acceptedProposal`, `acceptedValue`
- **ProposalLedger**: Thread-safe map of slot numbers to proposals
- **LeaderContext**: Current term, last log index, commit index

**Tunable Parameters**:
- **ElectionTimeout**: Base delay before starting election (typically 150-300ms)
- **HeartbeatInterval**: Minimum time between heartbeats (lower = faster failure detection)
- **MaxBatchSize**: Commands per consensus instance (trade-off: latency vs throughput)

### C. Java Implementation Example

```java
public final class MultiPaxosNode {
    private final NodeId nodeId;
    private final List<NodeId> cluster;
    private final PersistentStore store;
    private final ScheduledExecutorService executor;

    // Volatile state
    private volatile LeaderId currentLeader;
    private volatile long currentTerm = 0;
    private volatile boolean isLeader = false;

    public MultiPaxosNode(NodeId nodeId, List<NodeId> cluster) {
        this.nodeId = nodeId;
        this.cluster = cluster;
        this.store = new PersistentStore();
        this.executor = Executors.newScheduledThreadPool(4);
    }

    public synchronized void propose(LogEntry entry) {
        if (!isLeader) {
            throw new IllegalStateException("Not leader");
        }

        long slot = store.getNextSlot();
        Proposal proposal = new Proposal(currentTerm, slot, entry);

        // Broadcast accept request
        AcceptRequest req = new AcceptRequest(proposal);
        cluster.stream()
              .filter(id -> !id.equals(nodeId))
              .forEach(id -> sendMessage(id, req));

        // Wait for majority response (simplified)
        waitForMajorityAcceptance(proposal);
    }

    private void electionTimeout() {
        if (!isLeader && currentLeader == null) {
            startElection();
        }
    }

    private void startElection() {
        currentTerm++;
        // Send prepare requests
        PrepareRequest req = new PrepareRequest(currentTerm, nodeId);
        cluster.forEach(id -> sendMessage(id, req));

        // Wait for replies...
    }
}
```

### D. Complexity & Performance

**Time Complexity**:
- **Normal Operation**: O(1) per log entry (Phase 2 is pre-optimized)
- **Leader Election**: O(1) rounds, but O(n) messages where n = cluster size
- **Catch-up**: O(log n) for log replay optimization

**Expected Performance**: At steady state, Multi-Paxos can achieve ~10,000 consensus instances/second in a 5-node cluster (real-world measurements from Raft implementations).

### E. Thread Safety & Concurrency

**Multi-threaded Scenarios**: Separate threads for:
- Message handling (network I/O)
- Election timeouts (scheduled executor)
- Log application (state machine execution)

**Locking Strategy**: 
- Coarse-grained locks for critical sections (election, role changes)
- Lock-free data structures for log appends when possible
- Atomic operations for term updates and commit indices

**Memory Barriers**: Critical for visibility of term and leadership changes across threads.

### F. Memory & Resource Management

**Heap Implications**: 
- Log storage grows linearly with entries (garbage collection pressure)
- Per-entry metadata (terms, indices) adds ~100 bytes overhead

**Resource Optimization**:
- Log compaction to discard old entries
- Snapshotting for state transfer to new nodes

### G. Advanced Optimizations

**Common Optimizations**:
1. **Pipeline**: Leader sends multiple entries without waiting for all responses
2. **Batch Proposals**: Group entries into single consensus instance
3. **Lease-based Heartbeats**: Reduce heartbeat frequency with time-bounded leases

**Variants**:
- **Egalitarian Paxos**: Distributed leadership without single bottleneck
- **Vertical Paxos**: Optimized for read-heavy workloads with learners-as-proxies

## Edge Cases & Error Handling

**Network Faults**: Split-brain handled via term-based leader validation. Lower-quality partitions may cause livelocks, mitigated by randomized timeouts.

**Duplicate Messages**: Idempotent operations; proposals with lower proposal numbers are ignored.

**Log Corruption**: Nodes with corrupted logs require state transfer from healthy majority.

## Configuration Trade-offs

**Election Timeout vs Stability**: Shorter timeout detects failures faster but increases false elections during network jitter.

**Batch Size vs Latency**: Larger batches improve throughput (up to 10x) but increase commit latency (sub-millisecond to ~10ms).

## Use Cases & Real-World Examples

**Distributed Databases**: Apache Cassandra uses Multi-Paxos for lightweight transactions with Paxos-style durability.

**Configuration Management**: Google's Chubby implements Multi-Paxos for consistent file system coordination.

**Service Meshes**: Istio's control plane uses it for policy distribution across multiple control plane replicas.

## Advantages & Disadvantages

**Benefits**:
- Guarantees strong consistency even during partitions (CP in CAP theorem)
- Handles byzantine faults through proper implementation
- Scales with commodity hardware configurations

**Trade-offs**:
- Higher latency than eventual consistency protocols like epidemic broadcasting
- Requires careful tuning of timeouts to balance liveness and safety
- Read operations may require consensus (Leader reads) for absolute consistency

## Alternatives & Comparisons

**vs Raft**: Raft (an equivalent algorithm) emphasizes understandability over Paxos' formal but complex messaging. Multi-Paxos is equivalent in performance but harder to implement correctly.

**vs Zab (Zookeeper Atomic Broadcast)**: Zab is essentially Multi-Paxos with pipelining optimizations; trades flexibility for performance.

**When to Use**: Prefer Multi-Paxos when formal safety proofs are required or when integrating into existing Paxos ecosystems. Use Raft for new systems prioritizing simplicity.

## Interview Talking Points

- Multi-Paxos optimizes basic Paxos by amortizing leader election across multiple consensus instances, reducing message complexity from O(n²) to O(n) per decision
- The safety invariant relies on majority quorums, ensuring no conflicting values even during network splits
- Leader leases prevent long-term brain splits while allowing fast failover (typically `<1` second)
- Real-world tuning involves 3-5x replication factor over minimum (f=1 can tolerate) for performance
- Common interview extension: How does Multi-Paxos handle read-your-writes consistency for client requests?
- Evolution point: Many systems start with Multi-Paxos then migrate to optimized variants like Raft as complexity dollars justify the switch
