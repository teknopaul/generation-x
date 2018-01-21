#!/bin/bash
#
# Publish a version
#
cd `dirname $0`

echo Enter Version
read VERSION

TODO need to update version where
pom.xml
git tag
perforce label
perhaps in the JS code?
README.md

# update pom.xml
mvn package

echo "Check for pending P4 changes"
read OK
p4 changes -s pending

echo "Check for pending GIT changes"
read OK
git status

# Github submit
git push origin master

# Maven cetral submit 
# mvn deploy


# Fresh meat
# Twitter
