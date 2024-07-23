@echo off
del /s /q *.class
javac *.java
start powershell.exe -NoExit -Command "& { $Host.UI.RawUI.WindowTitle = 'Client 5000'; java Client 127.0.0.1 5000 }"
start powershell.exe -NoExit -Command "& { $Host.UI.RawUI.WindowTitle = 'Client 5001'; java Client 127.0.0.1 5001 }"
start powershell.exe -NoExit -Command "& { $Host.UI.RawUI.WindowTitle = 'Client 5002'; java Client 127.0.0.1 5002 }"
