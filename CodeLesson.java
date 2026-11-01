package typingtutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Java lines to type, each with a simple explanation of every part. */
public final class CodeLesson {
    private CodeLesson() {}

    public static class Snippet {
        public final String code;
        public final String summary;
        public final String[][] parts;   // {piece of code, what it means}

        Snippet(String code, String summary, String[][] parts) {
            this.code = code;
            this.summary = summary;
            this.parts = parts;
        }
    }

    private static final List<Snippet> ALL = new ArrayList<>();

    static {
        ALL.add(new Snippet("int age = 18;",
            "Makes a box called age and puts the number 18 inside it.",
            new String[][]{{"int", "the type: a whole number"}, {"age", "the name of the box (a variable)"},
                {"=", "put the value on the right into the box"}, {"18", "the value we store"},
                {";", "every Java statement ends with a semicolon"}}));
        ALL.add(new Snippet("String name = \"Riya\";",
            "Makes a box called name that stores some text.",
            new String[][]{{"String", "the type: text"}, {"name", "the name of the box"},
                {"=", "store what is on the right"}, {"\"Riya\"", "text always goes inside double quotes"},
                {";", "end of the statement"}}));
        ALL.add(new Snippet("System.out.println(\"Hello\");",
            "Shows the word Hello on the screen.",
            new String[][]{{"System.out.println", "the command that prints a line"},
                {"(\"Hello\")", "what to print, written inside brackets"}, {";", "end of the statement"}}));
        ALL.add(new Snippet("for (int i = 0; i < 5; i++) {",
            "A loop that repeats the code inside it 5 times.",
            new String[][]{{"for", "start a repeat loop"}, {"int i = 0;", "a counter that starts at 0"},
                {"i < 5;", "keep repeating while the counter is below 5"}, {"i++", "add 1 to the counter each round"},
                {"{", "the code to repeat starts here"}}));
        ALL.add(new Snippet("if (score > 50) {",
            "Asks a yes or no question. If yes, the code inside runs.",
            new String[][]{{"if", "ask a question"}, {"(score > 50)", "the question: is score bigger than 50?"},
                {"{", "code for the yes answer starts here"}}));
        ALL.add(new Snippet("int[] marks = new int[5];",
            "Makes a row of 5 boxes, each holding a whole number.",
            new String[][]{{"int[]", "an array: many numbers in one row"}, {"marks", "the name of the array"},
                {"new int[5]", "build space for 5 numbers"}, {";", "end of the statement"}}));
        ALL.add(new Snippet("ArrayList<String> list = new ArrayList<>();",
            "Makes an empty list of text that can grow as big as you want.",
            new String[][]{{"ArrayList<String>", "a growing list that holds text"}, {"list", "the name of the list"},
                {"new ArrayList<>()", "create a new empty list"}, {";", "end of the statement"}}));
        ALL.add(new Snippet("public static void main(String[] args) {",
            "The starting point. Java begins running your program here.",
            new String[][]{{"public", "anyone can use it"}, {"static", "runs without making an object first"},
                {"void", "gives nothing back"}, {"main", "the special name Java looks for"},
                {"(String[] args)", "extra input from outside (we often ignore it)"}, {"{", "the method body starts"}}));
        ALL.add(new Snippet("while (lives > 0) {",
            "Keeps repeating the code inside as long as lives is above 0.",
            new String[][]{{"while", "repeat as long as the question is true"}, {"(lives > 0)", "is lives still above 0?"},
                {"{", "the repeated code starts here"}}));
        ALL.add(new Snippet("return a + b;",
            "Adds two numbers and sends the answer back.",
            new String[][]{{"return", "send a result back to whoever called"}, {"a + b", "add the two numbers"},
                {";", "end of the statement"}}));
        ALL.add(new Snippet("Scanner sc = new Scanner(System.in);",
            "Creates a tool that reads what the user types on the keyboard.",
            new String[][]{{"Scanner", "a tool for reading input"}, {"sc", "our name for the tool"},
                {"new Scanner(...)", "create the tool"}, {"System.in", "read from the keyboard"}}));
        ALL.add(new Snippet("class Dog extends Animal {",
            "Makes a new class Dog that gets everything Animal has (inheritance).",
            new String[][]{{"class", "start a new blueprint"}, {"Dog", "the name of the blueprint"},
                {"extends", "inherit from another class"}, {"Animal", "the parent class we copy from"},
                {"{", "the class body starts"}}));
    }

    /** Snippet codes in random order, repeated to fill the requested count. */
    public static List<String> shuffledCodes(int count) {
        List<String> out = new ArrayList<>();
        String last = "";
        while (out.size() < count) {
            List<Snippet> round = new ArrayList<>(ALL);
            Collections.shuffle(round);
            for (Snippet s : round) {
                if (s.code.equals(last)) continue;
                out.add(s.code);
                last = s.code;
            }
        }
        return out;
    }

    public static Snippet find(String code) {
        for (Snippet s : ALL) if (s.code.equals(code)) return s;
        return ALL.get(0);
    }

    public static String idleHtml() {
        return "<html><div style='color:#FACC15;font-size:16px'><b>Learn Java while you type</b></div><br>"
             + "<div style='color:#FFFFFF;font-size:14px'>Press Start. Every line you type comes with a simple<br>"
             + "explanation of what each part means.</div></html>";
    }

    private static final String[] CHIP = {"#8B5CF6", "#0EA5E9", "#D97706", "#DB2777", "#059669", "#EA580C"};

    public static String toHtml(Snippet s, int learned) {
        StringBuilder sb = new StringBuilder("<html><body style='width:760px'>");
        sb.append("<div style='color:#FACC15;font-size:15px'><b>What does this line do?</b></div>");
        sb.append("<div style='color:#FFFFFF;font-size:14px'>").append(esc(s.summary)).append("</div><br>");
        sb.append("<table cellpadding='4' cellspacing='3'>");
        for (int i = 0; i < s.parts.length; i++) {
            sb.append("<tr><td bgcolor='").append(CHIP[i % CHIP.length]).append("'><font face='Monospaced' color='#FFFFFF'><b>")
              .append(esc(s.parts[i][0]).replace(" ", "&nbsp;")).append("</b></font></td>")
              .append("<td><font color='#E0E7FF'>&nbsp;= ").append(esc(s.parts[i][1])).append("</font></td></tr>");
        }
        sb.append("</table><br><font color='#4ADE80'>Lines learned so far: ").append(learned).append("</font>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String esc(String t) {
        return t.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
