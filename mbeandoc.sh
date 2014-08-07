#/bin/sh
. ./setEnv.sh

echo $JAVA_HOME/bin/java -cp $CLASSPATH MBeanDoc $1
$JAVA_HOME/bin/java -cp $CLASSPATH MBeanDoc $1

if [ `uname -s` = "Darwin" ]; then
  open ./mbeandocroot/index.html
fi
