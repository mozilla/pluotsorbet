#!/usr/bin/env bash
# This sets up a directory suitable for use as WebApp. It is expected to be
# run from your root j2me.js checkout after you've run |make test|.
# NB: I'm making no effort for this to be efficient or clever.

rm -rf $PACKAGE_DIR/
mkdir $PACKAGE_DIR
mkdir $PACKAGE_DIR/bld
mkdir $PACKAGE_DIR/config
mkdir $PACKAGE_DIR/libs
mkdir $PACKAGE_DIR/polyfill

# setup the root
# TODO: only copy benchmark.js/gc.html/gc.js if BENCHMARK=1.

cp *.html *.webapp $PACKAGE_DIR/.

# Copy over the individual JS files that are loaded by the app.
# This list is the union of files loaded by gc.html, index.html, and main.html.

cp benchmark.js $PACKAGE_DIR/.
cp gc.js $PACKAGE_DIR/.
cp index.js $PACKAGE_DIR/.
cp timer.js $PACKAGE_DIR/.
cp config/build.js $PACKAGE_DIR/config/.
cp config/default.js $PACKAGE_DIR/config/.
cp $CONFIG $PACKAGE_DIR/config/.
cp config/urlparams.js $PACKAGE_DIR/config/.
cp libs/compiled-method-cache.js $PACKAGE_DIR/libs/.
cp libs/load.js $PACKAGE_DIR/libs/.
cp libs/promise-6.0.0.js $PACKAGE_DIR/libs/.
cp libs/relooper.js $PACKAGE_DIR/libs/.
cp polyfill/IndexedDB-getAll-shim.js $PACKAGE_DIR/polyfill/.
cp bld/j2me.js $PACKAGE_DIR/bld/.
cp bld/shumway.js $PACKAGE_DIR/bld/.
cp bld/classes.jar.js $PACKAGE_DIR/bld/.
cp bld/main-all.js $PACKAGE_DIR/bld/.

# copy over jars/jads that are used for the webapp
# NB: we could be smart about this and parse the manifest, patches welcome!
#     grep 'launch_path' manifest.webapp | sed -E 's/.*jars=([^&]+)&.*$/\1/'
cp *.jar *.jad $PACKAGE_DIR/.

# setup java dir
mkdir $PACKAGE_DIR/java
cp java/*.jar $PACKAGE_DIR/java

# copy entire certs dir, it's possible we just need the ks files
cp -R certs $PACKAGE_DIR/.

# Merge app.js into another config file.
cat config/app.js >> $PACKAGE_DIR/config/default.js

# copy entire contents of style dir, except *.in preprocessor source files
cp -R style $PACKAGE_DIR/.
rm -r $PACKAGE_DIR/style/*.in

# setup tests dir, for now just the jar and js files
if [ $PACKAGE_TESTS -eq 1 ]; then
  mkdir $PACKAGE_DIR/tests
  cp tests/tests.jar $PACKAGE_DIR/tests/.
  cp tests/*.js $PACKAGE_DIR/tests/.
fi

# copy icons
mkdir $PACKAGE_DIR/img
cp img/icon-128.png $PACKAGE_DIR/img/.
cp img/icon-512.png $PACKAGE_DIR/img/.
