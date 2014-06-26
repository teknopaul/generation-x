#!/bin/bash
#
# Make the browser version of the code
#
cd `dirname $0`
cat ../src/main/js/xgen-path.js \
    ../src/main/js/xgen-nodelist.js \
    ../src/main/js/xgen-dom.js | awk -f browserify.awk > ../src/main/js/xgen-browser.js

which jsmin && cat ../src/main/js/xgen-browser.js | jsmin > ../src/main/js/xgen-min.js

