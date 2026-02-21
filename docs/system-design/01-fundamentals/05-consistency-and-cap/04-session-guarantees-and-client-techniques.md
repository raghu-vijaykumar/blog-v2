---
title: Session Guarantees and Client Techniques
---

# Session Guarantees and Client Techniques

## Overview
Session guarantees provide per‑client monotonicity and read‑your‑writes without requiring global linearizability. They are enforced by lightweight client tokens, sticky routing, and read fences. This page details the guarantees, how to implement them, and the operational guardrails.

## What, Why, When (and when‑not)
What
- Per‑session properties: Read‑Your‑Writes (RYW), Monotonic Reads, Monotonic Writes, and Writes‑Follow‑Reads (causal linkage).

Why
- Preserve user expectations (e.g., “I see my own update immediately”) while keeping most reads low‑latency via followers/caches.

When
- Interactive UX and dashboards; cart/profile edits; admin consoles; any flow where “my last action” must be visible immediately to the same user.

When‑not
- Anonymous, stateless traffic with no expectation of self‑consistency (public browsing); long‑running batch jobs.

## Core concepts and variants
- Session token
  - Carries last seen commit position (LSN/GTID/commit_ts) and optionally causal metadata (vector/HLC component).
- Sticky sessions
  - Route a user/session to a stable replica to increase probability of monotonicity with minimal coordination.
- Read fences
  - Require replica to be at/after a target commit position before serving a read; otherwise wait or promote to leader.

## Design decisions and trade‑offs
- Token format: timestamps (commit_ts) are compact and amenable to fences; vector clocks provide stronger causality but are larger.
- Where to enforce: gateway enforces for all downstream services; or client library enforces per call. Central enforcement simplifies adoption; library avoids central bottlenecks.
- Promotion policy: bounded wait to reach fence (e.g., ≤ 50 ms), then promote to leader. Tune to balance UX vs leader load.

## Algorithms and policies (conceptual)
- Update token on write ack
  - Extract commit_ts/LSN from write response; store in session (cookie/header/in‑memory).
- On read
  - Prefer follower; block until replica ≥ token or time out and promote; include token in cache keys to avoid RYW anomalies with stale cache entries.

Example pseudocode: client middleware for RYW and monotonic reads (≤ 30 lines)
```pseudo
state session { lastCommitTs = 0 }

function onWriteResponse(resp):
  if resp.commitTs > session.lastCommitTs:
    session.lastCommitTs = resp.commitTs

function read(key):
  minTs = session.lastCommitTs
  # Try follower with fence
  v = router.readFromFollower(key, min_commit_ts=minTs, max_wait_ms=50)
  if v == TIMEOUT or v.ts < minTs:
    v = router.readFromLeader(key, min_commit_ts=minTs)
  return v
```

## Architecture and components
- Client SDK or gateway maintains per‑session tokens and attaches them via headers.
- Routers/readers understand min_commit_ts fences and promotion rules.
- Cache layer integrates with tokens (e.g., versioned keys or bypass on token > cached version).

Mermaid: Session token propagation
```mermaid
sequenceDiagram
  participant U as User Session
  participant G as Gateway/SDK
  participant W as Writer (Leader)
  participant R as Reader (Follower)
  U->>G: Update profile
  G->>W: Write
  W-->>G: Ack(commit_ts=500)
  G-->>U: 200; session.lastCommitTs=500
  U->>G: View profile
  G->>R: Read(min_commit_ts=500)
  R-->>G: Data@ts>=500 or PROMOTE
```

## Operational considerations
- Metrics: follower fence wait time, leader promotion rate, stale cache hit rate, token propagation failures.
- Limits: cap token size; set TTLs; guard against unbounded token growth for vector metadata.
- Failure modes: token loss on stateless clients—consider cookie storage; clock skew if using physical timestamps—prefer commit positions or HLC.

## Examples

Example A (quantitative): Leader promotion budgeting
- If 0.5% reads promote to leader and global read QPS is 100k/s, leader headroom must absorb +500/s. With per‑leader capacity 2k/s and 50 leaders, added 10/s per leader is negligible. Validate burst behavior during replica lag spikes.

Example B (architectural): Sticky sessions vs fences
- With sticky sessions (hash(user_id) → follower), most reads are monotonic without waiting. Still enforce fences for cross‑AZ failover or rebalancing when the sticky target changes.

## Edge cases and anti‑patterns
- Ignoring caches: serving stale cache entries after a write breaks RYW. Use write‑through or invalidate with version bumps.
- Infinite waits to reach fence: always bound waits and promote to prevent head‑of‑line blocking.
- Token not updated on error/retry: ensure idempotent writes return stable commit positions.

## Interactions with adjacent topics
- [Caching](../01-caching/): cache invalidation and versioned keys for RYW.
- [Replication](../04-replication/): follower lag determines fence feasibility and promotion rates.

## Production checklist
- Define token schema and propagation mechanism; secure against tampering (sign/encrypt if necessary).
- Implement follower fence waits with timeouts; log and count promotions.
- Integrate caches with tokens; measure stale hits and post‑write reads.
- Alert on fence wait p99 and promotion spikes.

## Interview framing checklist
- Explain RYW and monotonic reads; design a client token scheme.
- Discuss trade‑offs between sticky sessions and read fences.
- Outline how to integrate caches while preserving session guarantees.

## References
- DDIA (Session guarantees), Bailis et al. (Highly Available Transactions)
