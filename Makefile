.PHONY: all test tests j2me java certs app clean jasmin aot shumway config-build benchmarks
BASIC_SRCS=$(shell find . -maxdepth 2 -name "*.ts" -not -path "./bld/*") config.ts
JIT_SRCS=$(shell find jit -name "*.ts" -not -path "./bld/*")
SHUMWAY_SRCS=$(shell find shumway -name "*.ts")
RELEASE ?= 0
VERSION ?=$(shell date +%s)
PROFILE ?= 0
BENCHMARK ?= 0

# Sensor support
JSR_256 ?= 1
export JSR_256

# Bluetooth support
JSR_082 ?= 1
export JSR_082

# Location service support
JSR_179 ?= 1
export JSR_179

# Create a checksum file to monitor the changes of the Makefile configuration.
# If the configuration has changed, we update the checksum file to let the files
# which depend on it to regenerate.

CHECKSUM := "$(RELEASE)$(PROFILE)$(BENCHMARK)$(JSR_256)$(JSR_082)$(JSR_179)"
OLD_CHECKSUM := "$(shell [ -f .checksum ] && cat .checksum)"
$(shell [ $(CHECKSUM) != $(OLD_CHECKSUM) ] && echo $(CHECKSUM) > .checksum)

toBool = $(if $(findstring 1,$(1)),true,false)
PREPROCESS = python tools/preprocess-1.1.0/lib/preprocess.py -s \
             -D RELEASE=$(call toBool,$(RELEASE)) \
             -D PROFILE=$(PROFILE) \
             -D BENCHMARK=$(call toBool,$(BENCHMARK)) \
             -D JSR_256=$(JSR_256) \
             -D JSR_179=$(JSR_179) \
             -D VERSION=$(VERSION)
PREPROCESS_SRCS = $(shell find . -name "*.in" -not -path config/build.js.in)
PREPROCESS_DESTS = $(PREPROCESS_SRCS:.in=)

all: config-build java jasmin tests j2me shumway aot benchmarks

$(shell mkdir -p build_tools)

XULRUNNER_VERSION=31.0
OLD_XULRUNNER_VERSION := $(shell [ -f build_tools/.xulrunner_version ] && cat build_tools/.xulrunner_version)
$(shell [ "$(XULRUNNER_VERSION)" != "$(OLD_XULRUNNER_VERSION)" ] && echo $(XULRUNNER_VERSION) > build_tools/.xulrunner_version)

SLIMERJS_VERSION=0.10.0pre
OLD_SLIMERJS_VERSION := $(shell [ -f build_tools/.slimerjs_version ] && cat build_tools/.slimerjs_version)
$(shell [ "$(SLIMERJS_VERSION)" != "$(OLD_SLIMERJS_VERSION)" ] && echo $(SLIMERJS_VERSION) > build_tools/.slimerjs_version)

PATH := build_tools/slimerjs-$(SLIMERJS_VERSION):${PATH}

UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)
ifeq ($(UNAME_S),Linux)
	XULRUNNER_PLATFORM=linux-$(UNAME_M)
	XULRUNNER_PATH=xulrunner/xulrunner
endif
ifeq ($(UNAME_S),Darwin)
	XULRUNNER_PLATFORM=mac
	XULRUNNER_PATH=XUL.framework/Versions/Current/xulrunner
endif
ifneq (,$(findstring MINGW,$(uname_S)))
	XULRUNNER_PLATFORM=win32
	XULRUNNER_PATH=xulrunner/xulrunner
endif
ifneq (,$(findstring CYGWIN,$(uname_S)))
	XULRUNNER_PLATFORM=win32
	XULRUNNER_PATH=xulrunner/xulrunner
endif

test: all build_tools/slimerjs-$(SLIMERJS_VERSION) build_tools/$(XULRUNNER_PATH)
	SLIMERJSLAUNCHER=build_tools/$(XULRUNNER_PATH) tests/runtests.py

build_tools/slimerjs-$(SLIMERJS_VERSION): build_tools/.slimerjs_version
	rm -rf build_tools/slimerjs*
	wget -P build_tools -N https://ftp.mozilla.org/pub/mozilla.org/labs/j2me.js/slimerjs-0.10.0pre-2014-12-17.zip
	unzip -o -d build_tools build_tools/slimerjs-0.10.0pre-2014-12-17.zip
	touch build_tools/slimerjs-$(SLIMERJS_VERSION)

build_tools/$(XULRUNNER_PATH): build_tools/.xulrunner_version
	rm -rf build_tools/XUL* build_tools/xul*
	wget -P build_tools -N https://ftp.mozilla.org/pub/mozilla.org/xulrunner/releases/$(XULRUNNER_VERSION)/runtimes/xulrunner-$(XULRUNNER_VERSION).en-US.$(XULRUNNER_PLATFORM).tar.bz2
	tar x -C build_tools -f build_tools/xulrunner-$(XULRUNNER_VERSION).en-US.$(XULRUNNER_PLATFORM).tar.bz2 -m

$(PREPROCESS_DESTS): $(PREPROCESS_SRCS) .checksum
	$(foreach file,$(PREPROCESS_SRCS),$(PREPROCESS) -o $(file:.in=) $(file);)

jasmin:
	make -C tools/jasmin-2.4

relooper:
	make -C jit/relooper/

bld/j2me.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME"
	node tools/tsc.js --sourcemap --target ES5 references.ts -d --out bld/j2me.js

bld/j2me-jsc.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME AOT Compiler"
	node tools/tsc.js --sourcemap --target ES5 references-jsc.ts -d --out bld/j2me-jsc.js

bld/jsc.js: jsc.ts bld/j2me-jsc.js
	@echo "Building J2ME JSC CLI"
	node tools/tsc.js --sourcemap --target ES5 jsc.ts --out bld/jsc.js

j2me: bld/j2me.js bld/jsc.js

aot: bld/classes.jar.js
bld/classes.jar.js: java/classes.jar bld/jsc.js aot-methods.txt
	@echo "Compiling ..."
	js bld/jsc.js -cp java/classes.jar -d -jf java/classes.jar -mff aot-methods.txt > bld/classes.jar.js

bld/tests.jar.js: tests/tests.jar bld/jsc.js aot-methods.txt
	js bld/jsc.js -cp java/classes.jar tests/tests.jar -d -jf tests/tests.jar -mff aot-methods.txt > bld/tests.jar.js

bld/program.jar.js: program.jar bld/jsc.js aot-methods.txt
	js bld/jsc.js -cp java/classes.jar program.jar -d -jf program.jar -mff aot-methods.txt > bld/program.jar.js

tools/closure.jar:
	wget -O $@ https://github.com/mykmelez/closure-compiler/releases/download/v0.1/closure.jar

closure: bld/classes.jar.js bld/j2me.js tools/closure.jar
	java -jar tools/closure.jar --language_in ECMASCRIPT5 -O J2ME_OPTIMIZATIONS bld/j2me.js > bld/j2me.cc.js \
		&& mv bld/j2me.cc.js bld/j2me.js
	java -jar tools/closure.jar --language_in ECMASCRIPT5 -O SIMPLE bld/classes.jar.js > bld/classes.jar.cc.js \
		&& mv bld/classes.jar.cc.js bld/classes.jar.js

shumway: bld/shumway.js
bld/shumway.js: $(SHUMWAY_SRCS)
	node tools/tsc.js --sourcemap --target ES5 shumway/references.ts --out bld/shumway.js

# We should update config/build.js everytime to generate the new VERSION number
# based on current time.
config-build: config/build.js.in
	$(PREPROCESS) -o config/build.js config/build.js.in

tests/tests.jar: tests
tests: java jasmin
	make -C tests

LANG_FILES=$(shell find l10n -name "*.xml")
LANG_DESTS=$(LANG_FILES:%.xml=java/%.json) java/custom/com/sun/midp/i18n/ResourceConstants.java java/custom/com/sun/midp/l10n/LocalizedStringsBase.java

java/classes.jar: java
java: $(LANG_DESTS)
	make -C java

$(LANG_DESTS): $(LANG_FILES)
	rm -rf java/l10n/
	mkdir java/l10n/
	$(foreach file,$(LANG_FILES), tools/xml_to_json.py $(file) java/$(file:.xml=.json);)
	mkdir -p java/custom/com/sun/midp/i18n/ java/custom/com/sun/midp/l10n/
	tools/xml_to_java_classes.py l10n/en-US.xml

certs:
	make -C certs

# Makes an output/ directory containing the packaged open web app files.
app: config-build java certs j2me aot
	tools/package.sh

benchmarks: java tests
	make -C bench

clean:
	rm -rf bld
	rm -f $(PREPROCESS_DESTS)
	make -C tools/jasmin-2.4 clean
	make -C tests clean
	make -C java clean
	rm -rf java/l10n/
	rm -f java/custom/com/sun/midp/i18n/ResourceConstants.java java/custom/com/sun/midp/l10n/LocalizedStringsBase.java
	make -C bench clean
