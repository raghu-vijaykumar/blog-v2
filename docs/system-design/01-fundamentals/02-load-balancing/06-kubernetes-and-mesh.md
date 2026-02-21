---
title: Kubernetes and Service Mesh
---

# Kubernetes and service mesh

Understand native primitives and when to add a mesh.

## Kubernetes primitives
- Service types: ClusterIP (internal L4), NodePort, LoadBalancer (external via cloud LB).
- kube-proxy: iptables/IPVS for L4 distribution.
- Probes: readiness/liveness/startup influence LB membership and timing.

## Ingress and Gateway API
- L7 routing, TLS termination, can enable cookie affinity and advanced policies.
- Controllers: NGINX, Envoy, HAProxy; choose based on features and scale.

## Mesh features (Envoy/Linkerd/Consul)
- Per-request L7 balancing, retries, outlier detection, mTLS, traffic shifting.
- Central policy via control plane (xDS, CRDs); per-service overrides.

## Example (Ingress with cookie affinity)
```yaml
metadata:
  annotations:
    nginx.ingress.kubernetes.io/affinity: cookie
    nginx.ingress.kubernetes.io/session-cookie-name: app_affinity
```

## Production checklist
- Align probes with LB behavior; budget for warmup.
- Decide Ingress vs Gateway API and controller.
- Evaluate mesh overhead vs benefits; start with edge LB + good timeouts.
