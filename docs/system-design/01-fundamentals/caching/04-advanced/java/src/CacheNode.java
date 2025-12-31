import java.util.HashMap;
import java.util.Map;

/**
 * Simulates a single cache node in a distributed cache cluster.
 * Each node has its own local cache and can be independently failed.
 */
public class CacheNode {
    private final String nodeId;
    private final Map<String, String> localCache = new HashMap<>();
    private boolean failed = false;
    private int operationsCount = 0;

    public CacheNode(String nodeId) {
        this.nodeId = nodeId;
    }

    public String get(String key) {
        operationsCount++;
        if (failed) {
            return null; // Failed nodes return null
        }
        return localCache.get(key);
    }

    public void put(String key, String value) {
        operationsCount++;
        if (!failed) {
            localCache.put(key, value);
        }
    }

    public void invalidate(String key) {
        operationsCount++;
        if (!failed) {
            localCache.remove(key);
        }
    }

    public boolean containsKey(String key) {
        return !failed && localCache.containsKey(key);
    }

    public void fail() {
        failed = true;
    }

    public void recover() {
        failed = false;
    }

    public boolean isFailed() {
        return failed;
    }

    public int size() {
        return failed ? 0 : localCache.size();
    }

    public int getOperationsCount() {
        return operationsCount;
    }

    public String getNodeId() {
        return nodeId;
    }
}
