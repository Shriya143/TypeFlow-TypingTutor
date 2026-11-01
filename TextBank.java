package typingtutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Supplies practice text. Words and sentences are returned as lists of single words to type. */
public final class TextBank {
    private TextBank() {}

    public static final String[] MODES = {"Words", "Sentences", "Java Code"};
    private static final Random RNG = new Random();

    private static final String[] WORDS = {
        "dog", "bone", "fly", "sky", "cloud", "star", "moon", "code", "java", "class",
        "object", "method", "string", "array", "loop", "thread", "stack", "queue", "list",
        "input", "output", "debug", "syntax", "memory", "file", "data", "logic", "build",
        "speed", "focus", "keyboard", "program", "variable", "package", "compile", "inherit",
        "practice", "rhythm", "improve", "level", "score", "winner", "balloon", "rocket"
    };

    private static final String[] SENTENCES = {
        "The dog runs fast.",
        "I love to code in Java.",
        "Practice makes you better.",
        "Type slowly and stay calm.",
        "A snake can grow very long.",
        "Coding is fun and easy.",
        "Every day I learn something new.",
        "The sun is bright today.",
        "Keep your fingers on the home row.",
        "Small steps lead to big wins.",
        "My laptop is my best friend.",
        "Good typists never look down.",
        "The snake is hungry and loves apples.",
        "Learning to type fast takes patience.",
        "Java is a popular programming language.",
        "I will type this sentence without mistakes.",
        "Every bug is a chance to learn.",
        "Fast fingers come from daily practice."
    };

    /** A long list of random words with no word repeated twice in a row. */
    public static List<String> words(int count) {
        List<String> out = new ArrayList<>();
        String last = "";
        while (out.size() < count) {
            String w = WORDS[RNG.nextInt(WORDS.length)];
            if (w.equals(last)) continue;
            out.add(w);
            last = w;
        }
        return out;
    }

    /** Full sentences, one after another (never the same one twice in a row). */
    public static List<String> sentences(int count) {
        List<String> out = new ArrayList<>();
        String last = "";
        while (out.size() < count) {
            String s = SENTENCES[RNG.nextInt(SENTENCES.length)];
            if (s.equals(last)) continue;
            out.add(s);
            last = s;
        }
        return out;
    }
}
