package typingtutor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Saves sessions in a text file inside the user's home folder. */
public class FileStatsRepository implements StatsRepository {
    private final Path file;

    public FileStatsRepository() {
        Path dir = Paths.get(System.getProperty("user.home"), ".typingtutor");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("Could not create data folder: " + e.getMessage());
        }
        this.file = dir.resolve("sessions.txt");
    }

    @Override
    public void save(SessionResult r) {
        try {
            Files.write(file, (r.toLine() + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Could not save session: " + e.getMessage());
        }
    }

    @Override
    public List<SessionResult> loadForUser(String user) {
        List<SessionResult> list = new ArrayList<>();
        for (SessionResult r : loadAll()) {
            if (r.getUser().equalsIgnoreCase(user)) list.add(r);
        }
        return list;
    }

    @Override
    public List<SessionResult> loadAll() {
        List<SessionResult> list = new ArrayList<>();
        if (!Files.exists(file)) return list;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                SessionResult r = SessionResult.fromLine(line);
                if (r != null) list.add(r);
            }
        } catch (IOException e) {
            System.err.println("Could not read sessions: " + e.getMessage());
        }
        return list;
    }
}
