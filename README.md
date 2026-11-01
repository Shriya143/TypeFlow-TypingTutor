<p align="center">
  <img src="logo.png" alt="Chandigarh University" width="140">
</p>

<h1 align="center">TypeFlow – Typing Tutor with Speed Analytics</h1>
<p align="center"><b>Chandigarh University</b> · 2nd Year Java Project</p>

| | |
|---|---|
| **Student** |Shriya|
| **UID / Roll No.** | 25BAI10016 |
| **Course / Section** | Object-Oriented Programming using Java |
| **Guided by** | Mr.Pravindra Kumar Gole |


---

## 1. About the project
TypeFlow is a desktop typing tutor written in **Java (Swing)**. Instead of a boring typing test, it turns practice into three small games, tracks your speed and accuracy, and ranks all players on a leaderboard. It also teaches basic Java syntax while you type.

**Problem it solves:** beginners find typing practice boring and get no clear feedback. TypeFlow gives instant, visual feedback and shows progress over time.

## 2. Features
| Game | How it works |
|---|---|
| **Dog Balloons** (Words) | Type the word shown. Every correct word gives the dog a balloon and he floats higher (+10 m). The sky changes from day to space. |
| **Snake Feast** (Sentences) | Type the whole sentence. Every finished word makes the snake grow by 2 segments. The next sentence is shown faded. |
| **Learn Java** (Java Code) | Type a real Java line. A simple explanation of each part (`int`, `for`, `i++`...) is shown below it. |

Other features: live **WPM** and **accuracy**, **My Stats** dashboard (progress chart and most-missed keys), **Hall of Fame leaderboard** with rank and titles (Beginner → Legend), player profiles, paste blocking, colourful animated UI.

## 3. Screenshots
**Dog Balloons**
![Dog](screenshots/1-dog-balloons.png)

**Snake Feast**
![Snake](screenshots/2-snake-feast.png)

**Learn Java**
![Java](screenshots/3-learn-java.png)

**My Stats**
![Stats](screenshots/4-my-stats.png)

**Leaderboard**
![Leaderboard](screenshots/5-leaderboard.png)

## 4. How to run
### Java desktop app
1. Install **JDK 11 or newer** (check with `javac -version`).
2. Download this repository (**Code → Download ZIP**) and extract it.
3. Double-click **`run.bat`** (Windows). Or in a terminal:
   ```
   mkdir out
   javac -d out src/typingtutor/*.java
   java -cp out typingtutor.Main
   ```
4. Enter your player name and start typing.

### Web demo (runs in any browser)
Open `web/index.html`, or use the live link above. To get a link, enable **GitHub Pages**: *Settings → Pages → Branch: main, folder: /(root) → Save*. Your page will be at `https://<username>.github.io/<repo>/web/`.

### Adding the university logo
Save the official logo as **`logo.png`** in the project root (next to `run.bat`). It then appears in the README and in the app's header automatically.

## 5. Project structure
```
TypingTutor/
├── run.bat                 one-click launcher
├── logo.png                (add the university logo here)
├── web/index.html          browser version
├── screenshots/            images used in this README
└── src/typingtutor/
    ├── Main, MainFrame            start-up and main window
    ├── TypingPanel                typing engine shared by all 3 games
    ├── DogScene, SnakeScene       animated scenes (Java2D)
    ├── CodeLesson, TextBank       lessons, words and sentences
    ├── DashboardPanel             charts (progress + most-missed keys)
    ├── LeaderboardPanel, Ranking  ranking of all players
    ├── SessionResult              model class (one finished session)
    ├── StatsRepository            interface for storage
    ├── FileStatsRepository        saves sessions to a text file
    └── FunBackground, RoundedPanel, Theme   look and feel
```

## 6. How it works
- A **DocumentFilter** on the input box sees every key before it is shown, so each keystroke is scored instantly and pasting is blocked.
- The text is split into **units** (a word, a sentence, or a code line). Typing a unit exactly moves to the next one and updates the game scene.
- **WPM** = (correct characters ÷ 5) ÷ minutes  (5 characters = 1 word, the standard).
- **Accuracy** = (keystrokes − errors) ÷ keystrokes × 100.
- A Swing **Timer** ticks every 100 ms for the clock; each scene has its own ~30 fps animation timer.
- Results are saved through the `StatsRepository` interface, so the file storage can be swapped for a database without changing the screens.

## 7. Java concepts used
Classes and objects · Encapsulation · **Interfaces** (`StatsRepository`) · Inheritance (`extends JPanel`) · Inner classes · Collections (`List`, `Map`) · Lambdas · Exception handling · File I/O · Event handling · Swing GUI · Java2D graphics · Timers (multithreading on the event thread).

## 8. Future scope
SQLite/MySQL database with JDBC · password login · more lessons and difficulty levels · online multiplayer · CSV/PDF report export.

## 9. Data storage
Sessions are saved in `C:\Users\<you>\.typingtutor\sessions.txt` (one line per session).
