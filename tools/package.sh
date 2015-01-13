#!/usr/bin/env bash
# This sets up a directory suitable for use as WebApp. It is expected to be
# run from your root j2me.js checkout after you've run |make test|.
# NB: I'm making no effort for this to be efficient or clever.

PACKAGE_DIR="output"

rm -rf $PACKAGE_DIR/
mkdir $PACKAGE_DIR

# setup the root
cp *.js *.html *.webapp $PACKAGE_DIR/.

# copy over jars/jads that are used for the webapp
# NB: we could be smart about this and parse the manifest, patches welcome!
#     grep 'launch_path' manifest.webapp | sed -E 's/.*jars=([^&]+)&.*$/\1/'
cp *.jar *.jad $PACKAGE_DIR/.

# setup java dir
mkdir $PACKAGE_DIR/java
cp java/*.jar $PACKAGE_DIR/java

# copy entire certs dir, it's possible we just need the ks files
cp -R certs $PACKAGE_DIR/.

# copy entire classfile dir
cp -R classfile $PACKAGE_DIR/.

# copy entire contents of libs dir
cp -R libs $PACKAGE_DIR/.

# copy entire contents of midp dir
cp -R midp $PACKAGE_DIR/.

# copy entire contents of style dir
cp -R style $PACKAGE_DIR/.

# setup tests dir, for now just the jar and js files
mkdir $PACKAGE_DIR/tests
cp tests/tests.jar $PACKAGE_DIR/tests/.
cp tests/*.js $PACKAGE_DIR/tests/.

# copy icons
mkdir $PACKAGE_DIR/img
cp img/*.png $PACKAGE_DIR/img/.
