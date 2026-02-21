---
title: Read Policies and Staleness Control
description: How to serve reads from replicas safely—read-your-writes, monotonic reads, bounded staleness, timeline/session consistency, follower reads, and lag budgets with practical guidance.
---

## Overview
Read replicas scale reads and improve locality but introduce staleness. Read policies define which replicas may serve a request and which session guarantees must hold.

## What, Why, When (and when-not)
What
- Policies and mechanisms to control freshness when reading from followers: read-your-writes, monotonic reads, bounded staleness, and timeline/session consistency.

Why
- Reduce load on leaders and serve low-latency reads from nearby replicas while protecting user experience that depends on observing recent writes.

When
- High read:write ratios, geo-distributed users, or analytics-like queries where slight staleness is acceptable.

When-not
- Strictly linearizable reads are required (e.g., counters with immediate effects); use leader reads or consensus-backed read index.

## Core concepts and variants
Session guarantees
- Read-your-writes (RYW): a client sees its own successful writes in subsequent reads.
- Monotonic reads: reads never go back in time within a session.
- Monotonic writes and writes-follow-reads: ordering guarantees for causally related operations.

Bounded staleness
- Reads may lag by at most Δ time or L log positions. Reject/redirect to leader when exceeded.

Timeline consistency
- Reads are served from replicas that are guaranteed to be on the same timeline (no divergent forks). Achieved via epochs/GTIDs.

Follower/offloaded reads
- Followers handle read-only traffic using locally applied state; RYW/monotonicity must be layered on via tokens or sticky routing.

## Design decisions and trade-offs
- Freshness vs latency: bounding staleness increases leader fallback; always-on follower reads maximize latency reduction but can violate RYW.
- Sticky vs stateless routing: stick sessions to a replica to preserve monotonicity; stateless requires tokens to validate replica position.
- Complexity: RYW tokens and read indexes add plumbing; simple policy switches are easier but less precise.

## Algorithms/policies (conceptual)
RYW token policy
```pseudo
function afterWriteToken(commitLsn):
  return {minLsn: commitLsn, expiry: now()+5m}

function readPolicy(req, token, replicas):
  if token and now() < token.expiry:
    # choose replica whose appliedLsn >= token.minLsn else fall back to leader
    r = first(replicas where r.appliedLsn >= token.minLsn)
    if r: return r
    return LEADER
  else:
    return NEAREST_FOLLOWER_WITHIN_STALENESS
```

Bounded-staleness enforcement
```pseudo
function canServe(replica, maxLagMs):
  return replica.stalenessMs <= maxLagMs
```

## Architecture and components
- Router/gateway tracks replica lag (applied LSN or time), attaches/validates RYW tokens, and selects a replica according to policy.
- Database exposes replication position (e.g., WAL LSN, GTID, applied index) via lightweight endpoints.

Mermaid: Read routing with RYW token
```mermaid
flowchart LR
  C[Client] -- write --> L[Leader]
  L -- commit LSN=x --> C
  C -- token x --> R[Router]
  R -- choose follower with LSN>=x --> F1[Follower (fresh)]
  R -- else fallback --> L
```

## Operational considerations
- Export accurate lag: applied LSN, last commit timestamp, and confidence bounds. Avoid clock skew pitfalls when using time-based staleness.
- Define default max staleness (e.g., 200–500 ms for timelines, seconds/minutes for analytics). Expose per-endpoint overrides.
- Implement graceful degradation: if many followers exceed budget, progressively route to leaders and shed non-critical traffic.

## Examples
Example A (quantitative): Choosing follower read budget
- Median follower lag 50 ms, p99 180 ms under peak. Set max staleness = 200 ms. Expect ≤ 1% leader fallbacks; capacity-plan leaders for that surge.

Example B (architectural): Session RYW in web tier
- After a successful POST, the API issues a JWT containing minLsn. Subsequent GETs include the token; the edge router validates replica LSN and routes accordingly, falling back to the leader when necessary.

## Edge cases and anti-patterns
- Using wall-clock timestamps for freshness without clock synchronization can misroute reads. Prefer log positions or bounded clock models.
- Sticky routing to a single follower without health checks can degrade to a hot/laggy node.

## Interactions with adjacent topics
- [Consistency & CAP](../05-consistency-and-cap/): formal definitions of RYW/monotonic consistency and bounded staleness.
- [Load Balancing](../02-load-balancing/): session affinity and failover interplay with read stickiness.

## Production checklist
- Expose replica positions/lag; instrument router selection and fallback rates.
- Define global default and per-endpoint staleness budgets.
- Implement RYW tokens for mutating endpoints impacting UX.
- Alert on follower lag percentiles and fallback surge.

## Interview framing checklist
- How would you provide read-your-writes using followers without always reading the leader?
- What metrics drive your staleness budget, and how do you choose them?

## References
- Spanner bounded staleness; Postgres replica identity and hot standby; Dynamo session guarantees literature
