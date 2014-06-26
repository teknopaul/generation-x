#!/bin/bash
#
#  Compiles the JavaScript with node to quickly detect syntax errors
#
cd `dirname $0`

for jsfile in `ls -1 ../src/main/js/*.js`
do
	echo $jsfile
	node $jsfile
done

for jsfile in `ls -1 ../src/test/js/*.js`
do
	echo $jsfile
	node $jsfile
done

 
