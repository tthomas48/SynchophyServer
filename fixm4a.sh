#!/bin/bash
#
# Dump m4a to mp3


find $1 -name '*.m4a' -print0 | while read -d $'\0' file
do
                echo "Fixing $file"
		rm -f "$file.wav"
		mkfifo "$file.wav"
		mplayer -ao file "$file" -ao pcm:file="$file.wav" &
		dest=`echo "$file"|sed -e 's/m4a$/mp3/'`
		lame "$file.wav" "$dest"
done
