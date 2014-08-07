echo off
SETLOCAL enabledelayedexpansion
call setEnv.cmd

echo %JAVA_HOME%\bin\java %MEM_ARGS% -classpath "%CLASSPATH%" Komuso2 %*
%JAVA_HOME%\bin\java %MEM_ARGS% -classpath "%CLASSPATH%" Komuso2 %*

ENDLOCAL