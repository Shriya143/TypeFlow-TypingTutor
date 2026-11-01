@echo off
if not exist out mkdir out
javac -d out src\typingtutor\*.java
java -cp out typingtutor.Main
pause
