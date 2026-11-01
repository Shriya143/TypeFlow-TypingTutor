package typingtutor;

import java.util.List;

/**
 * Storage contract. The rest of the app only talks to this interface,
 * so you can swap file storage for a real database (JDBC) later
 * without touching the GUI code.
 */
public interface StatsRepository {
    void save(SessionResult result);
    List<SessionResult> loadForUser(String user);
    List<SessionResult> loadAll();
}
