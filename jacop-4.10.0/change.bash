#!/bin/bash
echo changing file $1;
mv $1 $1.bak;
sed -e "s/@version 4.9/@version 4.10/g" < $1.bak > $1;
rm $1.bak;
# command to be executed 
# find src/ -name "*.java" -exec ./change.bash {} \;
