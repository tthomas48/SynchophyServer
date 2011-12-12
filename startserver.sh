#!/bin/sh
unset LD_LIBRARY_PATH
JAVA_HOME=~/src/j2sdk1.4.2_19/
JRE_HOME=$JAVA_HOME/jre
PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
java -cp bin:lib/commons-codec-1.5.jar:lib/gson-jdk14-1.7.1.jar:lib/isoviewer-1.1.jar:lib/jetty-6.1.26.jar:lib/jetty-util-6.1.26.jar:lib/jid3lib-0.5.4.jar:lib/jlme0.1.3.jar:lib/junit-4.10.jar:lib/last.fm-bindings-0.1.0.jar:lib/servlet-api-2.5-20081211.jar:lib/retrotranslator-runtime-1.2.9.jar:lib/backport-util-concurrent-3.1.jar:lib/derby.jar com.synchophy.server.HttpServer
