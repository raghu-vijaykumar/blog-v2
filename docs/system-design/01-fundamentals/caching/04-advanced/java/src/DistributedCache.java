import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Distributed Cache implementation demonstrating advanced caching patterns.
 *
 * Shows consistency protocols, invalidation strategies, and failure handling
 * in distributed cache environments.
 */
public class DistributedCache {
    private final Map<String, CacheNode> nodes = new HashMap<>();
    private final Database database;
    private final Map<String, CompletableFuture<Void>> pendingUpdates = new HashMap<>();

    public DistributedCache(Database database) {
        this.database = database;
    }

    /**
     * Add a cache node to the cluster.
     */
    public void addNode(String nodeId) {
        nodes.put(nodeId, new CacheNode(nodeId));
    }

    /**
     * Remove a node from the cluster.
     */
    public void removeNode(String nodeId) {
        nodes.remove(nodeId);
    }

    /**
     * Get value from distributed cache.
     * Returns value from first available node.
     */
    public String get(String key) {
        for (CacheNode node : nodes.values()) {
            String value = node.get(key);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    /**
     * Put value in all active cache nodes.
     */
    public void put(String key, String value) {
        for (CacheNode node : nodes.values()) {
            node.put(key, value);
        }
    }

    /**
     * Broadcast invalidation to all nodes.
     */
    public void invalidate(String key) {
        for (CacheNode node : nodes.values()) {
            node.invalidate(key);
        }
    }

    /**
     * Write-through: Update database first, then all caches.
     * Ensures strong consistency.
     */
    public void writeThrough(String key, String value) {
        // Update database first
        database.update(key, value);

        // Then update all cache nodes
        put(key, value);
    }

    /**
     * Write-behind: Update caches first, database asynchronously.
     * Provides better performance with eventual consistency.
     */
    public void writeBehind(String key, String value) {
        // Update caches immediately
        put(key, value);

        // Asynchronously update database
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(100); // Simulate network/database delay
                database.update(key, value);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        pendingUpdates.put(key, future);
    }

    /**
     * Wait for all pending write-behind operations to complete.
     */
    public void flushPendingUpdates() {
        for (CompletableFuture<Void> future : pendingUpdates.values()) {
            future.join();
        }
        pendingUpdates.clear();
    }

    /**
     * Get from cache, or fetch from database and cache it.
     */
    public String getOrLoad(String key) {
        String value = get(key);
        if (value == null) {
            // Cache miss - load from database
            value = database.get(key);
            if (value != null) {
                put(key, value);
            }
        }
        return value;
    }

    /**
     * Simulate node failure.
     */
    public void failNode(String nodeId) {
        CacheNode node = nodes.get(nodeId);
        if (node != null) {
            node.fail();
        }
    }

    /**
     * Recover a failed node.
     */
    public void recoverNode(String nodeId) {
        CacheNode node = nodes.get(nodeId);
        if (node != null) {
            node.recover();
        }
    }

    /**
     * Get cluster statistics.
     */
    public ClusterStats getStats() {
        int totalNodes = nodes.size();
        int activeNodes = (int) nodes.values().stream().filter(node -> !node.isFailed()).count();
        int totalOperations = nodes.values().stream().mapToInt(CacheNode::getOperationsCount).sum();
        int totalEntries = nodes.values().stream().filter(node -> !node.isFailed()).mapToInt(CacheNode::size).sum();

        return new ClusterStats(totalNodes, activeNodes, totalOperations, totalEntries);
    }

    /**
     * Cluster statistics.
     */
    public static class ClusterStats {
        public final int totalNodes;
        public final int activeNodes;
        public final int totalOperations;
        public final int totalEntries;

        public ClusterStats(int totalNodes, int activeNodes, int totalOperations, int totalEntries) {
            this.totalNodes = totalNodes;
            this.activeNodes = activeNodes;
            this.totalOperations = totalOperations;
            this.totalEntries = totalEntries;
        }

        public double getAvailability() {
            return totalNodes == 0 ? 0.0 : (double) activeNodes / totalNodes;
        }
    }
}
