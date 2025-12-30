import java.util.HashMap;
import java.util.Map;

/**
 * Simulated database interface and implementation.
 * In real systems, this would be a real database connection.
 */
public interface Database {
    User getUser(String userId);
    void updateUser(String userId, User user);
    void createUser(User user);
}

/**
 * In-memory database simulation for demonstration.
 * Tracks access for testing cache effectiveness.
 */
class InMemoryDatabase implements Database {
    private final Map<String, User> users = new HashMap<>();
    private int accessCount = 0;

    public InMemoryDatabase() {
        // Pre-populate with some test users
        users.put("user:1", new User("user:1", "Alice Johnson", "alice@example.com"));
        users.put("user:2", new User("user:2", "Bob Smith", "bob@example.com"));
        users.put("user:3", new User("user:3", "Carol Davis", "carol@example.com"));
    }

    @Override
    public User getUser(String userId) {
        accessCount++; // Track database accesses
        return users.get(userId);
    }

    @Override
    public void updateUser(String userId, User user) {
        accessCount++;
        users.put(userId, user);
    }

    @Override
    public void createUser(User user) {
        accessCount++;
        users.put(user.getId(), user);
    }

    // For testing - check how many times database was accessed
    public int getAccessCount() {
        return accessCount;
    }

    public void resetAccessCount() {
        accessCount = 0;
    }

    public int getUserCount() {
        return users.size();
    }
}
