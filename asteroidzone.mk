# A makefile for building the Asteroid Zone app.  To make the asteroidzone/
# directory for pushing to a device via WebIDE:
#
#   make -f asteroidzone.mk app
#
# To make a ZIP package containing the app for distribution to others:
#
#   make -f asteroidzone.mk package

JSR_256 = 0
JSR_082 = 0
JSR_179 = 0

RELEASE = 1
PACKAGE_TESTS = 1
CONSOLE = 0
PACKAGE_DIR = asteroidzone

CONFIG=config/asteroidzone.js

# The name of the app, which might be different from the name of the midlet.
# This name populates the app manifest, which determines the name of the app
# on the homescreen of the device.
NAME = Asteroid Zone

# The name of the midlet, which identifies the midlet on the splash screen.
MIDLET_NAME = Asteroid Zone

# The description and origin of the app, which populate the app manifest.
DESCRIPTION = a classic game of shoot 'em up
ORIGIN = app://asteroidzone.mozilla.org

# TODO: add custom icons for the app.
#ICON_128 = img/asteroidzone-icon-128.png
#ICON_512 = img/asteroidzone-icon-512.png

include Makefile
