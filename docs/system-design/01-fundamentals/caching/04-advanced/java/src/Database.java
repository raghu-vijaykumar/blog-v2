import java.util.HashMap;
import java.util.Map;

/**
 * Simple database simulation for distributed cache testing.
 * Tracks operations for testing cache effectiveness.
 */
public class Database {
    private final Map<String, String> data = new HashMap<>();
    private int readCount = 0;
    private int writeCount = 0;

    public String get(String key) {
        readCount++;
        return data.get(key);
    }

    public void update(String key, String value) {
        writeCount++;
        data.put(key, value);
    }

    public void delete(String key) {
        writeCount++;
        data.remove(key);
    }

    public boolean contains(String key) {
        readCount++;
        return data.containsKey(key);
    }

    public int size() {
        return data.size();
    }

    public int getReadCount() {
        return readCount;
    }

    public int getWriteCount() {
        return writeCount;
    }

    public void resetStats() {
        readCount = 0;
        writeCount = 0;
    }
}
