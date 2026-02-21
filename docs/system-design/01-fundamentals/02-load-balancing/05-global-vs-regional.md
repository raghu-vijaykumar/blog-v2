---
title: Global vs Regional Load Balancing
---

# Global vs regional load balancing

Choose scope based on latency, resilience, and data constraints.

## Regional LB
- Terminates within a region/VPC; simpler, lower intra-region latency.
- Pair with zonal diversity and multi-AZ failover.

## Global LB
- Anycast edges or global HTTP(S) proxies route to nearest healthy region/PoP.
- Policies: geo/proximity, latency-based, and failover routing.

## Patterns
- Active-active: serve from multiple regions; needs data replication and consistency plan.
- Active-passive: warm/cold standby; simpler data story; longer RTO/RPO.

## DNS vs global proxies
- DNS: health-checked, low TTLs, coarse control; beware caching and sticky resolvers.
- Global L7: precise per-request routing, TLS termination, rich policies.

## Production checklist
- Define region stickiness expectations and compliance boundaries.
- Test regional evacuation and failback.
- Ensure observability per-region and route.
