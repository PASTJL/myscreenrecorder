#!/bin/bash
export outputDir=/opt/screenRecordVideos
export PROJECT_HOME=/opt/myscreenrecorder
export JAVA_HOME=/opt/jdk-11.0.2
export JFX_HOME=/opt/javafx-sdk-11.0.2
export pathffmpeg=/usr/bin
export CLASSPATH=${PROJECT_HOME}/lib/jnativehook-2.1.0.jar:.:${PROJECT_HOME}/lib/myscreenrecorder-1.0.jar:${JFX_HOME}/lib/javafx.base.jar:${JFX_HOME}/lib/javafx.graphics.jar:${JFX_HOME}/lib/javafx.controls.jar:

${JAVA_HOME}/bin/java -Droot=${PROJECT_HOME} -DoutputDir=${outputDir} -Dpathffmpeg=${pathffmpeg} -Djava.library.path=${JFX_HOME}/lib --add-modules javafx.controls,javafx.base,javafx.media,javafx.graphics --module-path ${JFX_HOME}/lib:${CLASSPATH} -cp ${CLASSPATH} -Dhome=${PROJECT_HOME} com.jlp.myscreenrecorder.Main 
