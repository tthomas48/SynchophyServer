#!/bin/bash
#
# Dump m4a to mp3


find $1 -name '*.m4a' -print0 | while read -d $'\0' file
do
                echo "Fixing $file"
		rm -f "$file.wav"
                faad "$file"
                x=`echo "$file"|sed -e 's/.m4a/.wav/'`
                y=`echo "$file"|sed -e 's/.m4a/.mp3/'`
                lame -h -b 192 "$x" "$y"
                rm "$x"
done
