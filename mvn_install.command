#!/bin/sh
here="`dirname \"$0\"`"
cd "$here" || exit 1

mvn clean install
