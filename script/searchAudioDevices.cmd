if exist D:\opt\ (set Disk=D) ELSE (set Disk=C)
%Disk%:\opt\ffmpeg-latest-win64-static\bin\ffmpeg -list_devices true -f dshow -i dummy