echo off
SETLOCAL enabledelayedexpansion
call setEnv.cmd

echo %JAVA_HOME%\bin\java -classpath "%CLASSPATH%" MBeanDoc %*
%JAVA_HOME%\bin\java -classpath "%CLASSPATH%" MBeanDoc %*

start .\mbeandocroot\index.html

ENDLOCAL
