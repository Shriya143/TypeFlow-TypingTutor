package typingtutor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Works out the leaderboard: one row per player, sorted by best speed. */
public final class Ranking {
    private Ranking() {}

    public static class PlayerStat {
        public final String name;
        public final double best;
        public final double avgAccuracy;
        public final int sessions;

        PlayerStat(String name, double best, double avgAccuracy, int sessions) {
            this.name = name;
            this.best = best;
            this.avgAccuracy = avgAccuracy;
            this.sessions = sessions;
        }
    }

    public static List<PlayerStat> compute(List<SessionResult> all) {
        Map<String, List<SessionResult>> byUser = new LinkedHashMap<>();
        for (SessionResult r : all) {
            byUser.computeIfAbsent(r.getUser().toLowerCase(), k -> new ArrayList<>()).add(r);
        }
        List<PlayerStat> list = new ArrayList<>();
        for (List<SessionResult> sessions : byUser.values()) {
            double best = 0, acc = 0;
            for (SessionResult r : sessions) {
                best = Math.max(best, r.getWpm());
                acc += r.getAccuracy();
            }
            list.add(new PlayerStat(sessions.get(0).getUser(), best, acc / sessions.size(), sessions.size()));
        }
        list.sort((a, b) -> {
            int c = Double.compare(b.best, a.best);
            return c != 0 ? c : Double.compare(b.avgAccuracy, a.avgAccuracy);
        });
        return list;
    }

    /** Returns 1-based rank, or -1 if the player has not played yet. */
    public static int rankOf(List<PlayerStat> list, String user) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equalsIgnoreCase(user)) return i + 1;
        }
        return -1;
    }

    public static String title(double wpm) {
        if (wpm < 20) return "Beginner";
        if (wpm < 35) return "Learner";
        if (wpm < 50) return "Speedster";
        if (wpm < 70) return "Pro Typist";
        return "Legend";
    }
}
