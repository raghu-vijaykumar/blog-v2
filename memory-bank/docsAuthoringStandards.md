# System‑Design Docs Standard (Durable)

Purpose
- Establish a repeatable, high-quality structure for “fundamentals” reference docs in `/docs/system-design/01-fundamentals`.
- Ensure single‑shot generation yields detailed, production‑useful pages without heavy working code.

Scope
- Applies to all fundamentals topics (caching, load balancing, data partitioning, replication, consistency/CAP, etc.).
- Deep‑dive “docs + code” structures remain available but are OFF by default for fundamentals.

Canonical page structure (exact order)
1) Overview (2–3 sentences)
2) What / Why / When (and when-not)
3) Core concepts and variants (precise definitions)
4) Design decisions and trade-offs (explicit pros/cons or short decision matrix)
5) Algorithms/policies (conceptual; short pseudocode allowed)
6) Architecture and components (where this lives; roles of routers, catalogs, indices, etc.)
7) Operational considerations (capacity, scaling, failure modes, observability, runbooks)
8) Examples (≥ 2 worked examples; at least one quantitative and one architectural)
9) Edge cases and anti-patterns
10) Interactions with adjacent topics (inline cross‑links when materially helpful)
11) Production checklist (actionable bullets)
12) Interview framing checklist (succinct prompts)
13) References (authoritative sources)

Examples policy
- At least 2 worked examples per fundamentals page.
- One quantitative (e.g., shard counts, throughput/backfill math).
- One architectural (e.g., routing flow, ring topology, read/write paths).
- Prefer familiar domains: SaaS multitenancy, e‑commerce orders/catalog, social timelines, IoT time-series.

Code/snippet policy (fundamentals)
- No colocated runnable code by default; fundamentals are doc‑only.
- Inline snippets are allowed when they clarify a policy/algorithm:
  - pseudocode, SQL, YAML, or small config.
  - ≤ 30 lines per snippet; keep self‑contained.
- Prefer diagrams and numerically worked examples over code.

Diagram policy
- Include 1–2 Mermaid diagrams when they add clarity (e.g., consistent hashing ring; router→shard flow).
- Keep diagrams simple, labeled, and legible.

Writing voice and style
- Reference tone: concise, declarative, no hand‑waving.
- Define terms before use; add a tiny glossary block only if necessary.
- Use clear headings; do not hardcode numbers (site CSS handles numbering for H2–H6).

Length target
- Aim for 1,500–2,500 words per fundamentals page (optimize for signal density).

Acceptance checklist (must pass before publish)
- Covers What/Why/When, core variants, trade‑offs, operations, 2+ worked examples, edge cases, adjacent‑topic links, production + interview checklists, and references.
- Snippets, if any, are ≤ 30 lines and necessary for clarity; no heavy/runnable code.
- At least one diagram for topology/flow when useful.
- Headings follow site numbering rules (no manual numbering in titles).

Relationship to deep‑dives
- Deep‑dive “docs + code” is reserved for implementation‑heavy topics and is explicitly opt‑in.
- Fundamentals should remain self‑contained reference pages.
