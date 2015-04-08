# A makefile for building the Asteroid Zone app: make -f asteroidzone.mk

JSR_256 = 0
JSR_082 = 0
JSR_179 = 0

RELEASE = 1
PACKAGE_TESTS = 1
CONSOLE = 0

CONFIG=config/asteroidzone.js

NAME = Asteroid Zone
DESCRIPTION = a classic game of shoot 'em up
ORIGIN = app://asteroidzone.mozilla.org

# TODO: add custom icons for the app.
#ICON_128 = img/asteroidzone-icon-128.png
#ICON_512 = img/asteroidzone-icon-512.png

include Makefile
