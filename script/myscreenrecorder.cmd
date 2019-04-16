if exist D:\opt\ (set Disk=D) ELSE (set Disk=C)
Set PROJECT_HOME=%Disk%:\opt\myscreenrecorder
Set outputDir=%Disk%:\opt\screenRecordVideos
Set JAVA_HOME=%Disk%:\opt\jdk-11.0.2
Set JFX_HOME=%Disk%:\opt\javafx-sdk-11.0.2
Set pathffmpeg=%Disk%:\opt\ffmpeg-latest-win64-static
Set CLASSPATH=%PROJECT_HOME%\lib\jnativehook-2.1.0.jar;.;%PROJECT_HOME%\lib\myscreenrecorder-1.0.jar;%JFX_HOME%\lib\javafx.base.jar;%JFX_HOME%\lib\javafx.graphics.jar;%JFX_HOME%\lib\javafx.controls.jar;
REM start ""

 start "" "%JAVA_HOME%\bin\javaw" -Xms128m -Xmx256m -Droot=%PROJECT_HOME% -Dpathffmpeg=%pathffmpeg% -DoutputDir=%outputDir% -Djava.library.path=%JFX_HOME%\lib --add-modules javafx.controls,javafx.base,javafx.media,javafx.graphics --module-path %JFX_HOME%\lib;%CLASSPATH% -cp %CLASSPATH% -Dhome=%PROJECT_HOME% com.jlp.myscreenrecorder.Main 
Exit