#/bin/sh
. ./setEnv.sh

echo $JAVA_HOME/bin/java $MEM_ARGS -cp ./classes:$CLASSPATH Komuso2 $@
$JAVA_HOME/bin/java $MEM_ARGS -cp ./classes:$CLASSPATH Komuso2 $@