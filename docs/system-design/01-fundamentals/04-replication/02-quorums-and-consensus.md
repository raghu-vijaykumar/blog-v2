---
title: Quorums and Consensus
description: Read/write quorum math, majority rules, linearizable reads, and consensus (Raft/Paxos) for leader election and commit with examples, trade-offs, and diagrams.
---

## Overview
Quorums ensure overlapping read and write sets so that clients observe the latest committed values without contacting all replicas. Consensus protocols provide a way for a group of replicas to agree on a single leader and a totally ordered log despite failures.

## What, Why, When (and when-not)
What
- Quorum: a subset of replicas large enough to make a decision (commit/read) that is consistent with other decisions.
- Consensus: algorithms (Raft/Paxos) that elect a leader and replicate a log with crash fault tolerance.

Why
- Quorums reduce latency vs all-acks while maintaining safety. Consensus gives unambiguous leadership, prevents split brain, and enables linearizable operations.

When
- Use R/W quorums in leaderless or multi-primary KV systems. Use consensus in leader-based databases, metadata stores, and control planes.

When-not
- For purely asynchronous follower reads where staleness is acceptable (no need for quorum). For single-node prototypes or read-only caches.

## Core concepts and variants
Quorum math
- With replication factor N, choose write quorum W and read quorum R so that R + W > N. This guarantees at least one overlapping replica holds the latest write.
- Majority quorum: W = R = floor(N/2) + 1. Common defaults: N=3 → W=2,R=2; N=5 → W=3,R=3.

Tunable quorums
- Favor writes: W=majority, R=1 for low-latency reads that may be stale (eventual until repair).
- Favor reads: W=N, R=1 for read-mostly workloads needing strong freshness (slower writes).
- Weighted/latency-aware quorums: prefer nearby replicas while meeting redundancy across failure domains.

Linearizable reads
- Leader-based: read from leader after ensuring leadership is fresh (lease not expired) or after a ReadIndex (Raft) round.
- Leaderless: perform R quorum read and optionally read repair by writing back the freshest version.

Consensus (Raft/Paxos) essentials
- Terms/epochs: monotonically increasing leadership eras prevent old leaders from committing new entries (fencing).
- Leader election: majority vote grants leadership; only a leader appends to the log.
- Commit index: an entry is committed once replicated on a majority with ordering preserved.
- Log matching: followers reject out-of-order appends; leader backs up to the last matching index.

## Design decisions and trade-offs
- Latency: quorum operations wait on the k-th fastest replica; majority of N=5 implies waiting for the 3rd fastest.
- Availability: with majority quorums, the system tolerates up to floor((N-1)/2) failures. Tuning W/R changes which operations remain available under failures.
- Consistency: majority quorums with proper repair provide read-after-write and, with consensus, linearizability. Tunable quorums without consensus risk divergent histories unless conflicts are resolved.
- Cost: higher N improves durability and availability but increases storage and replication bandwidth.

## Algorithms/policies (conceptual)
Quorum selection policy
```pseudo
function chooseQuorum(replicas, k):
  # Prefer healthy, low-latency, and diverse failure domains
  candidates = sortBy(health desc, sameAZPenalty, latency asc)(replicas)
  return topK(candidates, k)
```

Leader linearizable read (Raft-style)
```pseudo
function linearizableRead():
  idx = raft.readIndex()  # round-trip to majority to confirm leadership and commit index
  waitUntil(localCommitIndex >= idx)
  return readFromStateMachine()
```

## Architecture and components
- Replica set (N nodes), leader (for consensus systems), RPC for AppendEntries/Prepare-Accept (Raft/Paxos), quorum calculator, health/latency probes.

Mermaid: Quorum overlap
```mermaid
graph LR
  subgraph Replicas N=5
    A[A]
    B[B]
    C[C]
    D[D]
    E[E]
  end
  subgraph Write quorum W=3
    A
    C
    E
  end
  subgraph Read quorum R=3
    B
    C
    D
  end
  C --- Overlap((Overlap ensures latest visible))
```

## Operational considerations
- Place replicas across failure domains so that a majority survives common outages (e.g., spread across 3 AZs).
- Track quorum success latency distribution; hedge requests to more replicas to reduce tail.
- Guard against clock skew if using lease-based reads; use monotonic clocks/TrueTime-like bounds for safety.

## Examples
Example A (quantitative): Availability under failures
- N=5, majority quorums. The system tolerates up to 2 failures. With 3 nodes up, both reads and writes succeed. With only 2 up, both fail (safety preserved).

Example B (architectural): Control-plane store with Raft
- Metadata (shard map) stored in a 5-node Raft group across 3 AZs. Leadership stickiness prefers AZ with best connectivity; clients do ReadIndex for linearizable reads when mutating routing tables.

## Edge cases and anti-patterns
- Asymmetric partitions (leader isolated but minority) must fail closed; without fencing, old leaders can corrupt history.
- Mixing tunable quorums with non-idempotent writes without versioning can cause lost updates.

## Interactions with adjacent topics
- [Consistency & CAP](../05-consistency-and-cap/): R/W semantics and linearizability assumptions.
- [Failover & Fencing](./05-failover-promotion-and-fencing.md): epochs and preventing split brain.

## Production checklist
- Choose N, W, R to meet latency and availability targets; validate R+W>N.
- Place replicas across AZs/regions for failure independence.
- Implement hedging/timeouts and idempotency tokens.
- For consensus, verify ReadIndex/lease-read safety and clock assumptions.

## Interview framing checklist
- Given N and latency SLOs, how would you choose W and R? What failures can you tolerate?
- How do Raft linearizable reads work? What are the pitfalls with lease reads?

## References
- Raft (Ongaro & Ousterhout); Paxos (Lamport)
- Cassandra/Dynamo tunable quorum docs; CockroachDB/Etcd Raft implementations