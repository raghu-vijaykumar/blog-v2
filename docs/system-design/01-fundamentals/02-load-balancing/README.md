---
title: Load Balancing
---

# Load Balancing

This guide covers the core concepts, algorithms, and operational patterns for distributing traffic reliably and efficiently across backend instances and regions.

## Learning outcomes
- Choose between client-side and server-side load balancing; understand DNS/anycast and their trade-offs
- Select the right layer and proxying mode: L4 (transport) vs L7 (application); NAT vs proxy vs direct-server-return
- Apply algorithms: round-robin, weighted, least-connections/least-requests, power-of-two choices, EWMA/latency-aware, consistent hashing
- Engineer reliability: active/passive health checks, slow start/warmup, circuit breaking, retries with budgets, connection pooling
- Make state decisions: stateless services vs sticky sessions; session affinity via cookies/headers/IP; JWT-bound affinities
- Design global strategies: regional vs global LB, geo/proximity routing, failover, active-active, disaster scenarios
- Operate at scale: draining, surge handling, rate limiting, observability, autoscaling interplay, graceful deploys

## Module 1: Foundations and mental models
- Terminology and layers
  - Client-side vs server-side LB: libraries/sidecars on the caller vs centrally managed data plane at the edge/middle
  - L3/4 vs L7:
    - L4 (TCP/UDP): fast, opaque to HTTP/gRPC semantics; common for game servers, databases, MQTT
    - L7 (HTTP/gRPC): routing by path/host/headers; per-request balancing and richer policies
  - Topologies: single tier edge LB → service, tiered LBs (edge → internal), sidecar/mesh
  - Discovery: static lists, service discovery (DNS-SRV, Consul, Kubernetes Endpoints), xDS for Envoy
- DNS and Anycast
  - DNS-based LB (A/AAAA): simple, cached, coarse control; use low TTLs, health-aware DNS if possible
  - Anycast: same IP advertised from multiple PoPs → BGP picks nearest; great for global edges and DDoS absorption
- Proxying modes
  - NAT/DR (DSR): high throughput, limited L7 features
  - Reverse proxy: TLS termination, header-based routing, per-request policies

## Module 2: Core algorithms
- Round robin (RR) and weighted RR: distribute evenly; weights reflect capacity/heterogeneity
- Least-connections / least-requests: favor backends with fewer active requests
- Power-of-two choices: sample two at random, pick the better → close to optimal with low cost
- Latency-aware (EWMA): exponentially weighted moving average selects lowest observed latency
- Consistent hashing: map keys/users/tenants to nodes; minimizes remapping on membership changes; use virtual nodes
- Hot-spot mitigation: bounded loads, request shedding, concurrency caps

Example pseudocode: EWMA choice
```
choose_backend(backends):
  return argmin_b(EWMA_latency[b])
```

## Module 3: Health, slow start, circuit breaking, retries, pooling
- Health checks
  - Active: periodic HTTP/TCP checks; thresholds for pass/fail; path that exercises dependencies
  - Passive: observe failures/timeouts and mark as unhealthy
  - Outlier detection: temporarily eject high-error/latency outliers
- Slow start/warmup
  - Ramp traffic to new or recently healed instances to avoid cold cache/just-in-time compilation spikes
- Circuit breaking
  - Concurrency and pending-request limits per upstream; trip on error/timeout rates
- Retries
  - Use retry budgets (e.g., `<= 10%` of original RPS) and per-try/overall timeouts; avoid retry storms
- Connection pooling
  - Reuse keep-alive HTTP/2/gRPC connections; tune max connections and per-connection concurrency

## Module 4: Session affinity and state
- Statelessness preferred: enables true load distribution and elasticity
- When affinity is required
  - Sticky sessions via cookie (set by LB), header, or IP hash; or consistent hash on user/session ID
  - JWT-bound affinity: do not store server state in LB; use token to route while remaining stateless on servers
- Trade-offs
  - Affinity reduces effective balancing, complicates failover; always set reasonable stickiness TTL and fallback

## Module 5: Global vs regional LB
- Regional LB: traffic terminates in-region; simpler, lower latency to compute within region
- Global LB: routes to closest healthy region/PoP; features: geo/proximity routing, latency-based, failover
- Patterns
  - Active-active: serve from multiple regions; needs data replication/consistency strategy
  - Active-passive: cold/warm standby for DR with health-aware DNS or global proxies
- Geo routing considerations: compliance/sovereignty, sticky-to-region, split-brain avoidance during partitions

## Module 6: Kubernetes and service meshes
- Kubernetes
  - Service (ClusterIP/NodePort/LoadBalancer): kube-proxy (iptables/IPVS) does L4; cloud LB for external access
  - Ingress/Gateway API: L7 routing, TLS, can enable sticky sessions and advanced policies via controllers (NGINX, Envoy)
  - Pod readiness/liveness/startup probes align with LB health
- Service mesh (Envoy/Linkerd/Consul)
  - Per-request L7 load balancing, retries with budgets, outlier detection, mTLS, traffic shifting
  - Discovery and policy via control plane (xDS)

## Module 7: Operations and observability
- Draining and deploys: connection draining on rollout; max surge/unavailable; respect keep-alive timeouts
- Surge protection: queue limits, shed excess early, backpressure, adaptive concurrency limits
- Autoscaling interplay: new pods are slow-started; scale from zero requires warmup paths
- Metrics: RPS, concurrency, error rates, retry rates/budgets, p50/p95/p99 per upstream and LB, ejection counts
- Dashboards: per-route, per-backend saturation; health status; affinity distribution; DNS TTL efficacy
- Runbooks: failover, region evacuation, canary and feature flags impact on routes

## Module 8: Security at the edge
- TLS termination, HSTS, ALPN (HTTP/2, HTTP/3), OCSP stapling
- mTLS for service-to-service within mesh
- WAF/bot management, rate limiting, IP reputation; DDoS absorption at anycast edge

---

## Practical configuration examples

NGINX (L7 HTTP) with least_conn, health check, slow start, simple stickiness
```nginx
upstream app_pool {
  least_conn;
  server app1.internal:8080 max_fails=3 fail_timeout=10s;
  server app2.internal:8080 max_fails=3 fail_timeout=10s;
  # ip_hash provides coarse stickiness. Prefer cookie-based affinity via NGINX Ingress or Plus.
  # ip_hash;  # uncomment if IP-based stickiness is acceptable
}

server {
  listen 443 ssl http2;
  server_name example.com;

  location /healthz { return 200 'ok'; }

  location / {
    proxy_pass http://app_pool;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_read_timeout 5s;
    proxy_connect_timeout 1s;
  }
}
```

HAProxy with health checks, slowstart, cookie persistence
```haproxy
frontend fe_http
  bind :443 ssl crt /etc/haproxy/certs/example.pem
  default_backend be_app

backend be_app
  balance leastconn
  option httpchk GET /healthz
  http-check expect status 200
  default-server inter 2s fall 3 rise 2 slowstart 10s maxconn 200
  cookie SRV insert indirect nocache
  server s1 app1.internal:8080 check cookie s1
  server s2 app2.internal:8080 check cookie s2
```

Envoy cluster with least_requests, outlier detection, circuit breakers
```yaml
static_resources:
  clusters:
  - name: app_cluster
    type: STRICT_DNS
    connect_timeout: 1s
    lb_policy: LEAST_REQUEST
    circuit_breakers:
      thresholds:
      - priority: DEFAULT
        max_connections: 1024
        max_pending_requests: 512
        max_requests: 1024
    outlier_detection:
      consecutive_5xx: 5
      interval: 5s
      base_ejection_time: 30s
      max_ejection_percent: 50
    load_assignment:
      cluster_name: app_cluster
      endpoints:
      - lb_endpoints:
        - endpoint: { address: { socket_address: { address: app1.internal, port_value: 8080 } } }
        - endpoint: { address: { socket_address: { address: app2.internal, port_value: 8080 } } }
```

Kubernetes: Service + NGINX Ingress with cookie affinity
```yaml
apiVersion: v1
kind: Service
metadata:
  name: app
spec:
  selector:
    app: app
  ports:
  - port: 80
    targetPort: 8080
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app
  annotations:
    nginx.ingress.kubernetes.io/affinity: cookie
    nginx.ingress.kubernetes.io/session-cookie-name: app_affinity
    nginx.ingress.kubernetes.io/proxy-read-timeout: "5"
spec:
  ingressClassName: nginx
  rules:
  - host: example.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: app
            port:
              number: 80
```

Global routing via DNS failover (concept)
- Use health-checked DNS (e.g., Route 53, Cloud DNS with Traffic Director) to return the nearest healthy region
- Keep TTLs modest (e.g., 30–60s); maintain region stickiness via cookies or geo-hints when needed

---

## Hands-on practice (pick 3–4)
- Configure HAProxy or Envoy with least-requests, outlier detection, and a retry budget; validate with a fault-injected backend
- Set up NGINX Ingress in Kubernetes with cookie affinity and measure distribution vs sticky off
- Implement consistent hashing for a key-space (e.g., tenant ID) and simulate node add/remove remapping
- Create a blue/green rollout with connection draining; verify zero 5xx during switch
- Build dashboards: per-backend p95 latency, request distribution, ejection counts, retry rate vs error budget

## Interview framing checklist
- Layer and topology: L4 vs L7, client vs server side, discovery, global vs regional
- Algorithm and state: RR/least/EWMA/Po2; stateless vs stickiness; consistent hash when needed
- Reliability: health checks, slow start, circuit breakers, retries/timeouts, budgets
- Operations: draining, autoscale warmup, surge limits; observability plan; security at edge

## References
- The Google SRE Book (latency, overload, and retries); Envoy docs (outlier detection, retries, circuit breakers)
- HAProxy configuration manual (balance algorithms, health checks, slow start)
- NGINX load balancing (RR, least_conn, hash) and Ingress annotations
- AWS ALB/NLB, GCP HTTP(S) LB, Azure Front Door/Traffic Manager docs
- The Power of Two Choices in Randomized Load Balancing (Mitzenmacher et al.)

---

Next steps
- Use the left sidebar to navigate fundamentals. When ready, add diagrams illustrating L4 vs L7 topologies, global routing, and failure flows.
- Consider adding subpages for “Algorithms” and “Global LB” if this page grows too long.
