#!/usr/bin/env bash

DIR=`mktemp -d -t explode`
PWD=`pwd`
IN_FILE=$PWD/$1
OUT_FILE=$PWD/$2

echo "DIR: $DIR"
echo "IN_FILE: $IN_FILE"
echo "OUT_FILE: $OUT_FILE"

pushd $DIR
jar xf $IN_FILE
jar cfM0 $OUT_FILE *
popd

rm -rf "$DIR"
