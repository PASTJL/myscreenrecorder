if exist D:\opt\ (set Disk=D) ELSE (set Disk=C)
%Disk%:\opt\ffmpeg\bin\ffmpeg -list_devices true -f dshow -i dummy