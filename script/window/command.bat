@echo off

call run

:run
::set filter_complex="select=eq(pict_type\,I)[out];[out]scale=-2:720"
set filter_complex="select[out];[out]scale=-2:720"

set command=ffmpeg -loglevel info ^
-i C:\Users\Administrator\Downloads\output.mp4 ^
-vcodec libx264 ^
-b:v 400k ^
-acodec libmp3lame ^
-b:a 64k ^
-ar 44100 ^
-r 25 ^
-pix_fmt yuv420p ^
-profile:v baseline ^
-level 3.0 ^
-vf %filter_complex% ^
-subq 5 ^
-trellis 1 ^
-refs 1 ^
-coder 0 ^
-me_range 16 ^
-keyint_min 25 ^
-g 30 ^
-pix_fmt yuv420p ^
-sc_threshold 40 ^
-i_qfactor 0.71 ^
-flags +loop -cmp +chroma -partitions +parti4x4+partp8x8+partb8x8 ^
-rc_eq 'blurCplx^(1-qComp)' -qcomp 0.6 -qmin 10 -qmax 51 -qdiff 4 -async 2 ^
-x264opts colorprim=bt709:transfer=bt709:colormatrix=bt709:deblock=-1,1:open_gop=1 ^
-tune ssim ^
-threads 0 ^
-y ^
C:\Users\Administrator\Downloads\output_001.mp4

set time1=%time%
call :time2sec %time1%
set t1=%ns%

echo %command%
%command%

set time2=%time%
call :time2sec %time2%
set t2=%ns%
set /a tdiff=%t2% - %t1%
echo run time %tdiff% seconds.

pause
call :run
goto :eof

:time2sec
set tt=%1
set hh=%tt:~0,2%
set mm=%tt:~3,2%
set ss=%tt:~6,2%
set /a ns=(%hh%*60+%mm%)*60+%ss%
goto :eof

