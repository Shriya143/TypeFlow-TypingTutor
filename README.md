# TypeFlow - Typing Tutor with Speed Analytics (Java Swing)

## Run
Double-click run.bat   (needs JDK 11 or newer)

## The three games
- Words      : Dog Balloons. Only the current word is shown. Each correct word gives the dog a balloon
               and he floats higher (+10 m). Sky turns to space as he climbs.
- Sentences  : Snake Feast. The full sentence is shown (next sentence faded below). Type it normally,
               spaces included. Every finished word grows the snake by 2 segments.
- Java Code  : Learn Java. A full line is shown with a simple explanation of every part.

## Screens
Practice | My Stats (charts) | Leaderboard (Hall of Fame ranking of all players)

## Structure
- Main, MainFrame                 : start-up and window
- TypingPanel                     : the typing engine shared by the 3 games (keystroke scoring, live WPM)
- DogScene, SnakeScene            : animated game scenes drawn with Java2D
- CodeLesson                      : Java lines + explanations
- TextBank                        : words and sentences
- DashboardPanel, LeaderboardPanel, Ranking : analytics and ranking
- SessionResult, StatsRepository (interface), FileStatsRepository : model + storage
- FunBackground, RoundedPanel, Theme : look and feel

## How it works 
- A DocumentFilter on the input box sees every key before it is shown, so each keystroke is scored.
- A "unit" is one word (Words/Sentences) or one code line (Java Code). Typing the unit exactly moves on.
- WPM = (correct characters / 5) / minutes.  Accuracy = (keystrokes - errors) / keystrokes.
- A Swing Timer ticks every 100 ms for the clock; each scene has its own 33 ms animation timer.

## Upgrade roadmap
1. SQLite storage (SqliteStatsRepository implementing StatsRepository).
2. Password login, CSV export.
3. More code lessons (add a Snippet in CodeLesson).
