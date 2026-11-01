package typingtutor;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The practice screen. Three games share one typing engine:
 *   Words     -> only the current word is shown; each correct word lifts the dog higher.
 *   Sentences -> the full sentence is shown (next one faded below); each finished word grows the snake.
 *   Java Code -> a full line is shown with a simple explanation of every part.
 * The text is split into "units" (a word, a sentence, or a code line). Typing a unit correctly moves to the next one.
 */
public class TypingPanel extends JPanel {
    private static final String WORDS = "Words", SENTENCES = "Sentences", CODE = "Java Code";

    private final String user;
    private final StatsRepository repo;
    private final Runnable onFinished;

    private final JComboBox<String> modeBox = new JComboBox<>(TextBank.MODES);
    private final JComboBox<Integer> timeBox = new JComboBox<>(new Integer[]{30, 60, 120});
    private final JButton startBtn = new JButton("Start");
    private final JLabel infoLbl = new JLabel();
    private final JTextPane display = new JTextPane();
    private final JTextField input = new JTextField();
    private final JLabel timeLbl = statLabel("Time", "0s");
    private final JLabel wpmLbl = statLabel("WPM", "0");
    private final JLabel accLbl = statLabel("Accuracy", "100%");

    private final DogScene dog = new DogScene();
    private final SnakeScene snake = new SnakeScene();
    private final JLabel codeLabel = new JLabel();
    private final CardLayout sceneCards = new CardLayout();
    private final JPanel scenePanel = new JPanel(sceneCards);

    private final SimpleAttributeSet pending = new SimpleAttributeSet();
    private final SimpleAttributeSet pendingNow = new SimpleAttributeSet();
    private final SimpleAttributeSet correct = new SimpleAttributeSet();
    private final SimpleAttributeSet wrong = new SimpleAttributeSet();
    private final SimpleAttributeSet current = new SimpleAttributeSet();
    private final SimpleAttributeSet[] fade = new SimpleAttributeSet[4];

    private String mode = WORDS;
    private List<String> units = new ArrayList<>();
    private final List<int[]> upcoming = new ArrayList<>();   // {start, end} of next words in the display
    private int idx;
    private int completedUnits;
    private int completedChars;
    private int baseWords;        // words in sentences already finished (Sentences mode)
    private int doneInCurrent;    // words finished so far inside the current sentence

    private boolean running;
    private long startNanos;
    private int limit;
    private int keystrokes;
    private int errors;
    private Map<Character, Integer> mistakes = new HashMap<>();
    private Timer timer;

    public TypingPanel(String user, StatsRepository repo, Runnable onFinished) {
        this.user = user;
        this.repo = repo;
        this.onFinished = onFinished;
        setOpaque(false);
        setLayout(new BorderLayout(0, 14));
        setBorder(BorderFactory.createEmptyBorder(16, 24, 20, 24));

        StyleConstants.setForeground(pending, Theme.MUTED);
        StyleConstants.setForeground(pendingNow, new Color(0xE0E7FF));
        StyleConstants.setForeground(correct, Theme.GOOD);
        StyleConstants.setForeground(wrong, Color.WHITE);
        StyleConstants.setBackground(wrong, Theme.BAD);
        StyleConstants.setForeground(current, Color.BLACK);
        StyleConstants.setBackground(current, Theme.ACCENT);
        for (int i = 0; i < fade.length; i++) {
            fade[i] = new SimpleAttributeSet();
            StyleConstants.setForeground(fade[i], Theme.mix(Theme.MUTED, Theme.CARD, 0.15 * i));
        }

        JPanel north = new JPanel(new BorderLayout(0, 8));
        north.setOpaque(false);
        north.add(buildTopBar(), BorderLayout.NORTH);
        RoundedPanel info = new RoundedPanel(new BorderLayout(), new Color(0, 0, 0, 80), 18);
        info.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        infoLbl.setForeground(Color.WHITE);
        infoLbl.setFont(Theme.UI);
        info.add(infoLbl);
        north.add(info, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildInput(), BorderLayout.SOUTH);

        ((AbstractDocument) input.getDocument()).setDocumentFilter(new TypingFilter());
        startBtn.addActionListener(e -> startSession());
        modeBox.addActionListener(e -> { if (!running) updateModeUI(); });
        wpmLbl.setToolTipText("WPM = (correct characters / 5) / minutes");
        updateModeUI();
    }

    // ------------------------------------------------------------------ layout
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(label("Game"));
        left.add(modeBox);
        left.add(label("Time (s)"));
        left.add(timeBox);
        styleButton(startBtn, Theme.YELLOW, Color.BLACK);
        left.add(startBtn);

        RoundedPanel right = new RoundedPanel(new FlowLayout(FlowLayout.RIGHT, 24, 0), Theme.CARD_ALPHA, 24);
        right.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        right.add(timeLbl);
        right.add(wpmLbl);
        right.add(accLbl);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenter() {
        display.setEditable(false);
        display.setFont(Theme.MONO);
        display.setBackground(Theme.CARD);
        display.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));
        JScrollPane sp = new JScrollPane(display);
        sp.setBorder(BorderFactory.createLineBorder(Theme.ACCENT, 2));
        sp.setPreferredSize(new Dimension(10, 130));

        RoundedPanel codeCard = new RoundedPanel(new BorderLayout(), Theme.CARD_ALPHA, 28);
        codeCard.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));
        codeLabel.setVerticalAlignment(SwingConstants.TOP);
        codeLabel.setFont(Theme.UI);
        codeCard.add(codeLabel);

        scenePanel.setOpaque(false);
        scenePanel.add(dog, WORDS);
        scenePanel.add(snake, SENTENCES);
        scenePanel.add(codeCard, CODE);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(sp, BorderLayout.NORTH);
        center.add(scenePanel, BorderLayout.CENTER);
        return center;
    }

    private JTextField buildInput() {
        input.setFont(Theme.MONO);
        input.setBackground(Theme.CARD);
        input.setForeground(Theme.TEXT);
        input.setCaretColor(Theme.ACCENT);
        input.setEnabled(false);
        input.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.YELLOW, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        return input;
    }

    private void updateModeUI() {
        mode = (String) modeBox.getSelectedItem();
        sceneCards.show(scenePanel, mode);
        dog.reset();
        snake.reset();
        codeLabel.setText(CodeLesson.idleHtml());
        String text;
        if (mode.equals(WORDS)) {
            text = "<b style='color:#FACC15'>DOG BALLOONS</b> - Type the word you see. Every correct word gives the dog "
                 + "a balloon and he floats higher (+10 m).";
        } else if (mode.equals(SENTENCES)) {
            text = "<b style='color:#FACC15'>SNAKE FEAST</b> - Type the whole sentence, spaces included. "
                 + "Every finished word makes the snake grow by 2!";
        } else {
            text = "<b style='color:#FACC15'>LEARN JAVA</b> - Type the whole line exactly as shown. "
                 + "A simple explanation of each part appears below.";
        }
        String hint = !mode.equals(WORDS) ? "" : " <span style='color:#C4B5FD'>(no need to press space)</span>";
        infoLbl.setText("<html>" + text + hint + "</html>");
        showIdleMessage();
    }

    private void showIdleMessage() {
        display.setFont(Theme.MONO);
        display.setText("Pick a game and press Start!");
        StyledDocument doc = display.getStyledDocument();
        doc.setCharacterAttributes(0, doc.getLength(), pending, true);
        align(StyleConstants.ALIGN_LEFT);
    }

    private void align(int alignment) {
        SimpleAttributeSet a = new SimpleAttributeSet();
        StyleConstants.setAlignment(a, alignment);
        StyledDocument doc = display.getStyledDocument();
        doc.setParagraphAttributes(0, doc.getLength(), a, false);
    }

    // ------------------------------------------------------------------ game flow
    public void startSession() {
        if (timer != null) timer.stop();
        mode = (String) modeBox.getSelectedItem();
        limit = (Integer) timeBox.getSelectedItem();
        units = mode.equals(WORDS) ? TextBank.words(400)
              : mode.equals(SENTENCES) ? TextBank.sentences(80)
              : CodeLesson.shuffledCodes(40);
        idx = 0;
        completedUnits = 0;
        completedChars = 0;
        baseWords = 0;
        doneInCurrent = 0;
        keystrokes = 0;
        errors = 0;
        mistakes = new HashMap<>();
        dog.reset();
        snake.reset();
        sceneCards.show(scenePanel, mode);

        running = false;
        input.setEnabled(true);
        input.setText("");
        showUnit();
        running = true;
        input.requestFocusInWindow();
        startBtn.setEnabled(false);
        modeBox.setEnabled(false);
        timeBox.setEnabled(false);

        startNanos = System.nanoTime();
        timer = new Timer(100, e -> tick());
        timer.start();
    }

    /** Shows the current unit (plus the next few words in Sentences mode). */
    private void showUnit() {
        String cur = units.get(idx);
        StringBuilder sb = new StringBuilder(cur);
        upcoming.clear();
        int lookahead = mode.equals(SENTENCES) ? 1 : 0;
        for (int k = 1; k <= lookahead && idx + k < units.size(); k++) {
            sb.append('\n');
            int start = sb.length();
            sb.append(units.get(idx + k));
            upcoming.add(new int[]{start, sb.length()});
        }
        display.setFont(mode.equals(WORDS) ? new Font(Font.MONOSPACED, Font.BOLD, 56)
                : mode.equals(SENTENCES) ? new Font(Font.MONOSPACED, Font.BOLD, 28) : Theme.MONO);
        display.setText(sb.toString());
        align(mode.equals(CODE) ? StyleConstants.ALIGN_LEFT : StyleConstants.ALIGN_CENTER);
        render("");
        if (mode.equals(CODE)) {
            codeLabel.setText(CodeLesson.toHtml(CodeLesson.find(cur), completedUnits));
        }
    }

    /** Colours characters: green = right, red = wrong, blue = next, white = rest of this word, grey = coming up. */
    private void render(String typed) {
        StyledDocument doc = display.getStyledDocument();
        String cur = units.get(idx);
        doc.setCharacterAttributes(0, doc.getLength(), pending, true);
        for (int k = 0; k < upcoming.size(); k++) {
            int[] r = upcoming.get(k);
            doc.setCharacterAttributes(r[0], r[1] - r[0], fade[Math.min(k, fade.length - 1)], true);
        }
        doc.setCharacterAttributes(0, cur.length(), pendingNow, true);
        int n = Math.min(typed.length(), cur.length());
        for (int i = 0; i < n; i++) {
            doc.setCharacterAttributes(i, 1, typed.charAt(i) == cur.charAt(i) ? correct : wrong, true);
        }
        if (n < cur.length()) doc.setCharacterAttributes(n, 1, current, true);
        display.setCaretPosition(0);
    }

    /** Intercepts every key so we can score it before it reaches the text field. */
    private class TypingFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int off, String s, AttributeSet a) throws BadLocationException {
            replace(fb, off, 0, s, a);
        }

        @Override
        public void replace(FilterBypass fb, int off, int len, String s, AttributeSet a) throws BadLocationException {
            if (!running) { super.replace(fb, off, len, s, a); return; }
            if (s == null || s.length() != 1) return;          // blocks paste
            handleChar(fb, s.charAt(0));
        }

        @Override
        public void remove(FilterBypass fb, int off, int len) throws BadLocationException {
            super.remove(fb, off, len);
            if (running) render(fb.getDocument().getText(0, fb.getDocument().getLength()));
        }
    }

    private void handleChar(DocumentFilter.FilterBypass fb, char c) throws BadLocationException {
        if (c == '\n' || c == '\t') return;
        if (c == ' ' && mode.equals(WORDS)) return;              // a single word never contains a space
        Document d = fb.getDocument();
        String typed = d.getText(0, d.getLength());
        String unit = units.get(idx);
        int pos = typed.length();

        fb.insertString(pos, String.valueOf(c), null);
        keystrokes++;
        if (pos < unit.length() && unit.charAt(pos) == c) {
            // correct key
        } else {
            errors++;
            if (pos < unit.length()) mistakes.merge(unit.charAt(pos), 1, Integer::sum);
        }

        String now = typed + c;
        if (now.equals(unit)) {
            completedUnits++;
            completedChars += unit.length() + 1;
            idx++;
            fb.remove(0, d.getLength());
            if (mode.equals(WORDS)) dog.setProgress(completedUnits);
            if (mode.equals(SENTENCES)) {
                baseWords += unit.split(" ").length;
                doneInCurrent = 0;
                snake.setProgress(baseWords);
            }
            if (idx >= units.size()) {
                SwingUtilities.invokeLater(this::finish);
            } else {
                showUnit();
            }
        } else {
            render(now);
            if (mode.equals(SENTENCES)) {
                int done = wordsFinished(now, unit);
                if (done > doneInCurrent) {            // never shrinks, even if you backspace
                    doneInCurrent = done;
                    snake.setProgress(baseWords + doneInCurrent);
                }
            }
        }
    }

    /** Counts the words of the sentence that are completely and correctly typed (word + space). */
    private static int wordsFinished(String typed, String sentence) {
        int ok = 0;
        while (ok < typed.length() && ok < sentence.length() && typed.charAt(ok) == sentence.charAt(ok)) ok++;
        int words = 0;
        for (int i = 0; i < ok; i++) if (sentence.charAt(i) == ' ') words++;
        return words;
    }

    private int correctChars() {
        if (idx >= units.size()) return completedChars;
        String typed = input.getText();
        String cur = units.get(idx);
        int c = 0;
        for (int i = 0; i < Math.min(typed.length(), cur.length()); i++) {
            if (typed.charAt(i) == cur.charAt(i)) c++;
        }
        return completedChars + c;
    }

    private double elapsedSeconds() {
        return (System.nanoTime() - startNanos) / 1_000_000_000.0;
    }

    private double currentWpm(double seconds) {
        if (seconds < 1) return 0;
        return (correctChars() / 5.0) / (seconds / 60.0);   // standard: 5 characters = 1 word
    }

    private double currentAccuracy() {
        return keystrokes == 0 ? 100 : (keystrokes - errors) * 100.0 / keystrokes;
    }

    private void tick() {
        double elapsed = elapsedSeconds();
        setStat(timeLbl, Math.max(0, (int) Math.ceil(limit - elapsed)) + "s");
        setStat(wpmLbl, String.format("%.0f", currentWpm(elapsed)));
        setStat(accLbl, String.format("%.0f%%", currentAccuracy()));
        if (elapsed >= limit) finish();
    }

    private void finish() {
        if (!running) return;
        running = false;
        timer.stop();
        input.setEnabled(false);
        startBtn.setEnabled(true);
        modeBox.setEnabled(true);
        timeBox.setEnabled(true);

        double seconds = Math.min(elapsedSeconds(), limit);
        double wpm = currentWpm(seconds);
        double acc = currentAccuracy();
        setStat(wpmLbl, String.format("%.0f", wpm));
        setStat(accLbl, String.format("%.0f%%", acc));

        repo.save(new SessionResult(user, mode, System.currentTimeMillis(),
                wpm, acc, (int) Math.round(seconds), mistakes));
        onFinished.run();

        List<Ranking.PlayerStat> board = Ranking.compute(repo.loadAll());
        int rank = Ranking.rankOf(board, user);
        String fun = mode.equals(WORDS) ? "Your dog flew " + (completedUnits * 10) + " m high!"
                : mode.equals(SENTENCES) ? "Your snake grew to length " + (3 + 2 * (baseWords + doneInCurrent)) + "!"
                : "Java lines learned: " + completedUnits;
        String msg = String.format("%s%n%nSpeed: %.1f WPM%nAccuracy: %.1f%%%nMistakes: %d%nTitle: %s%nRank: #%d of %d players",
                fun, wpm, acc, errors, Ranking.title(wpm), rank, board.size());
        JOptionPane.showMessageDialog(this, msg, "Session complete", JOptionPane.INFORMATION_MESSAGE);
    }

    // ------------------------------------------------------------------ small UI helpers
    private static String statHtml(String name, String value) {
        return "<html><div style='text-align:center'><span style='color:#C4B5FD;font-size:10px'>"
                + name.toUpperCase() + "</span><br><span style='color:#FFFFFF;font-size:20px'><b>"
                + value + "</b></span></div></html>";
    }

    private static JLabel statLabel(String name, String value) {
        JLabel l = new JLabel(statHtml(name, value));
        l.putClientProperty("name", name);
        return l;
    }

    private static void setStat(JLabel l, String value) {
        l.setText(statHtml((String) l.getClientProperty("name"), value));
    }

    private static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(Theme.BOLD);
        return l;
    }

    static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(Theme.BOLD);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
    }
}
