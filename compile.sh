#/bin/sh
. ./setEnv.sh $*

rm -Rf classes/
mkdir classes
mkdir classes/templates

for src in src/*.java;do
 export SOURCE="$SOURCE $src"
done

cp -R src/templates/ classes/templates/

echo $JAVA_HOME/bin/javac -target 1.5 -classpath $CLASSPATH -d ./classes $SOURCE
$JAVA_HOME/bin/javac -target 1.5 -classpath $CLASSPATH -d ./classes $SOURCE
