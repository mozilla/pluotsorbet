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

NAME = Asteroid Zone
DESCRIPTION = a classic game of shoot 'em up
ORIGIN = app://asteroidzone.mozilla.org

# TODO: add custom icons for the app.
#ICON_128 = img/asteroidzone-icon-128.png
#ICON_512 = img/asteroidzone-icon-512.png

include Makefile
