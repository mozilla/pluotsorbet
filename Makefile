.PHONY: all test tests j2me java certs app clean jasmin aot shumway config-build benchmarks
BASIC_SRCS=$(shell find . -maxdepth 2 -name "*.ts" -not -path "./build/*") config.ts
JIT_SRCS=$(shell find jit -name "*.ts" -not -path "./build/*")
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

CHECKSUM := "$(RELEASE)$(PROFILE)$(JSR_256)$(JSR_179)"
OLD_CHECKSUM := "$(shell [ -f .checksum ] && cat .checksum)"
$(shell [ $(CHECKSUM) != $(OLD_CHECKSUM) ] && echo $(CHECKSUM) > .checksum)

toBool = $(if $(findstring 1,$(1)),true,false)
PREPROCESS = python tools/preprocess-1.1.0/lib/preprocess.py -s \
             -D RELEASE=$(call toBool,$(RELEASE)) \
             -D PROFILE=$(call toBool,$(PROFILE)) \
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

build/j2me.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME"
	node tools/tsc.js --sourcemap --target ES5 references.ts -d --out build/j2me.js

build/j2me-jsc.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME AOT Compiler"
	node tools/tsc.js --sourcemap --target ES5 references-jsc.ts -d --out build/j2me-jsc.js

build/jsc.js: jsc.ts build/j2me-jsc.js
	@echo "Building J2ME JSC CLI"
	node tools/tsc.js --sourcemap --target ES5 jsc.ts --out build/jsc.js

j2me: build/j2me.js build/jsc.js

aot: build/classes.jar.js
build/classes.jar.js: java/classes.jar build/jsc.js aot-methods.txt
	@echo "Compiling ..."
	js build/jsc.js -cp java/classes.jar -d -jf java/classes.jar -mff aot-methods.txt > build/classes.jar.js

build/tests.jar.js: tests/tests.jar build/jsc.js aot-methods.txt
	js build/jsc.js -cp java/classes.jar tests/tests.jar -d -jf tests/tests.jar -mff aot-methods.txt > build/tests.jar.js

build/program.jar.js: program.jar build/jsc.js aot-methods.txt
	js build/jsc.js -cp java/classes.jar program.jar -d -jf program.jar -mff aot-methods.txt > build/program.jar.js

tools/closure.jar:
	wget -O $@ https://github.com/mykmelez/closure-compiler/releases/download/v0.1/closure.jar

closure: build/classes.jar.js build/j2me.js tools/closure.jar
	java -jar tools/closure.jar --language_in ECMASCRIPT5 -O J2ME_OPTIMIZATIONS build/j2me.js > build/j2me.cc.js \
		&& mv build/j2me.cc.js build/j2me.js
	java -jar tools/closure.jar --language_in ECMASCRIPT5 -O SIMPLE build/classes.jar.js > build/classes.jar.cc.js \
		&& mv build/classes.jar.cc.js build/classes.jar.js

shumway: build/shumway.js
build/shumway.js: $(SHUMWAY_SRCS)
	node tools/tsc.js --sourcemap --target ES5 shumway/references.ts --out build/shumway.js

# We should update config/build.js everytime to generate the new VERSION number
# based on current time.
config-build: config/build.js.in
	$(PREPROCESS) -o config/build.js config/build.js.in

tests/tests.jar: tests
tests: java jasmin
	make -C tests

java/classes.jar: java
java:
	make -C java

certs:
	make -C certs

# Makes an output/ directory containing the packaged open web app files.
app: config-build java certs j2me aot
	tools/package.sh

benchmarks: java tests
	make -C bench

clean:
	rm -rf build
	rm -f config/build.js
	make -C tools/jasmin-2.4 clean
	make -C tests clean
	make -C java clean
	make -C bench clean
