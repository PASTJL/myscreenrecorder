# myscreenrecorder
<h1>Context</h1>
Under Windows, I could not (or did) find simple free software to make screenshots and
audio as video. Very useful for tutorials for example.</br>
There are some free but the recording time is limited or there is a watermark inserted at the
beginning and end of the recording.</br> Others generate uncompressed (very large) avi files with a size
limit of 2 GB.</br>
Under Linux, I know at least 2:</br>
- RecordMyDesktop =&gt; http://recordmydesktop.sourceforge.net/downloads.php </br>
- SimpleScreenRecorder =&gt; https://www.maartenbaert.be/simplescreenrecorder/</br>
Although my software can run under Linux, I will focus instead on its use on Windows. I
provide all the operating modes and scripts to make it work also under Linux, without details.
<br/>
<h1><b>myscreenrecorder</B></h1>
This application is just a <b>ffmpeg graphic front end </b>for recording the screen
and possibly audio through a microphone or through the speakers used (headphones or pc specific).</br>
<b color="red">ffmpeg is behind the scene, no need to know  and play with it.</b></br>
In addition to the software already mentioned, I also use a library (provided with the github
package) interface JNI JnativeHook available here =>
https://github.com/kwhat/jnativehook/releases/download/2.1.0/jnativehook-2.1.0.zip

The others logicials are : </br>
FFMPEG static : https://ffmpeg.zeranoe.com/builds/win64/static/ffmpeg-latest-win64-static.zip
</br>
Open JFX 11 : http://gluonhq.com/download/javafx-11-0-2-sdk-windows/
</br> OpenJDK 11 : https://download.java.net/java/GA/jdk11/9/GPL/openjdk-11.0.2_windows-x64_bin.zip

</br></br>
<b>To install , download the zip project from here, and read the manual under manuals directory  ( English and french) to finalize the installation and the configuration</b>
