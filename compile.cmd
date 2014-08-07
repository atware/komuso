echo off
SETLOCAL enabledelayedexpansion
call setEnv.cmd

for %%i in (.\src\*.java) do set SOURCE=!SOURCE! %%i

xcopy src\* classes\ /Y /S

echo %JAVA_HOME%\bin\javac -classpath %CLASSPATH% -d .\classes %SOURCE%
%JAVA_HOME%\bin\javac -classpath "%CLASSPATH%" -d .\classes %SOURCE%

ENDLOCAL