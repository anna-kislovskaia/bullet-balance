#!/bin/sh

if [ -z $APP_HOME ]; then
	APP_HOME=`dirname $0`
fi

LIB=${APP_HOME}/lib
CONFIG=${APP_HOME}/configuration

if [ -z $JAVA_HOME ]; then
	JAVA=java
else 
	JAVA=$JAVA_HOME/bin/java
fi

CLASSPATH=$(find "$LIB" -name '*.jar' |xargs echo  |tr ' ' ':')

echo Using Java command: $JAVA
echo Using Java classpath: $CLASSPATH

$JAVA -cp $CLASSPATH com.bulletbalance.DemoLauncher