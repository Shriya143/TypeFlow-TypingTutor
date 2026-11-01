package typingtutor;

import java.util.HashMap;
import java.util.Map;

/** One finished typing session (a "record" in the database sense). */
public class SessionResult {
    private final String user;
    private final String mode;
    private final long timestamp;
    private final double wpm;
    private final double accuracy;
    private final int seconds;
    private final Map<Character, Integer> mistakes;

    public SessionResult(String user, String mode, long timestamp, double wpm,
                         double accuracy, int seconds, Map<Character, Integer> mistakes) {
        this.user = user;
        this.mode = mode;
        this.timestamp = timestamp;
        this.wpm = wpm;
        this.accuracy = accuracy;
        this.seconds = seconds;
        this.mistakes = mistakes;
    }

    public String getUser() { return user; }
    public String getMode() { return mode; }
    public long getTimestamp() { return timestamp; }
    public double getWpm() { return wpm; }
    public double getAccuracy() { return accuracy; }
    public int getSeconds() { return seconds; }
    public Map<Character, Integer> getMistakes() { return mistakes; }

    /** Converts this object to one line of text for saving to a file. */
    public String toLine() {
        StringBuilder m = new StringBuilder();
        for (Map.Entry<Character, Integer> e : mistakes.entrySet()) {
            if (m.length() > 0) m.append(',');
            m.append((int) e.getKey()).append(':').append(e.getValue());
        }
        return user + "|" + mode + "|" + timestamp + "|" + wpm + "|" + accuracy + "|" + seconds + "|" + m;
    }

    /** Rebuilds an object from a saved line. Returns null if the line is damaged. */
    public static SessionResult fromLine(String line) {
        try {
            String[] p = line.split("\\|", -1);
            Map<Character, Integer> mistakes = new HashMap<>();
            if (!p[6].isEmpty()) {
                for (String pair : p[6].split(",")) {
                    String[] kv = pair.split(":");
                    mistakes.put((char) Integer.parseInt(kv[0]), Integer.parseInt(kv[1]));
                }
            }
            return new SessionResult(p[0], p[1], Long.parseLong(p[2]), Double.parseDouble(p[3]),
                    Double.parseDouble(p[4]), Integer.parseInt(p[5]), mistakes);
        } catch (Exception ex) {
            return null; // skip bad lines instead of crashing
        }
    }
}
