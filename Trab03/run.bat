@echo off
del /s /q *.class
javac *.java
start powershell.exe java Client 127.0.0.1 5000
start powershell.exe java Client 127.0.0.1 5001