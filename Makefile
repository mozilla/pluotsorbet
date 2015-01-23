.PHONY: all test tests j2me java certs app clean jasmin aot shumway
BASIC_SRCS=$(shell find . -maxdepth 2 -name "*.ts" -not -path "./build/*")
JIT_SRCS=$(shell find jit -name "*.ts" -not -path "./build/*")
SHUMWAY_SRCS=$(shell find shumway -name "*.ts")

all: java jasmin tests j2me shumway aot

test: all
	tests/runtests.py

jasmin:
	make -C tools/jasmin-2.4

build/j2me.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME"
	node tools/tsc.js --sourcemap --target ES5 references.ts -d --out build/j2me.js

build/j2me-jsc.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME JSC"
	node tools/tsc.js --sourcemap --target ES5 references-jsc.ts -d --out build/j2me-jsc.js

build/jsc.js: jsc.ts build/j2me-jsc.js
	@echo "Building J2ME JSC CLI"
	node tools/tsc.js --sourcemap --target ES5 jsc.ts --out build/jsc.js

j2me: build/j2me.js build/jsc.js

aot: java j2me
	@echo "Compiling ..."
	js build/jsc.js -cp java/classes.jar -d -jf java/classes.jar -mff aot-methods.txt > build/classes.jar.js
	js build/jsc.js -cp java/classes.jar tests/tests.jar -d -jf tests/tests.jar -mff aot-methods.txt > build/tests.jar.js
	if test -f program.jar; then \
		js build/jsc.js -cp java/classes.jar program.jar -d -jf program.jar -mff aot-methods.txt > build/program.jar.js; \
	fi
	@echo "Done"

closure: build/j2me.js aot
	java -jar tools/closure.jar --language_in ECMASCRIPT5 -O SHUMWAY_OPTIMIZATIONS build/j2me.js > build/j2me.cc.js \
		&& mv build/j2me.cc.js build/j2me.js
	java -jar tools/closure.jar --language_in ECMASCRIPT5 -O SIMPLE build/classes.jar.js > build/classes.jar.cc.js \
		&& mv build/classes.jar.cc.js build/classes.jar.js
	if test -f build/program.jar.js; then \
		java -jar tools/closure.jar --language_in ECMASCRIPT5 -O SIMPLE build/program.jar.js > build/program.jar.cc.js \
			&& mv build/program.jar.cc.js build/program.jar.js; \
	fi

shumway: $(SHUMWAY_SRCS)
	node tools/tsc.js --sourcemap --target ES5 shumway/references.ts --out build/shumway.js

tests:
	make -C tests

java:
	make -C java

certs:
	make -C certs

# Makes an output/ directory containing the packaged open web app files.
app: java certs
	tools/package.sh

clean:
	rm -f j2me.js `find . -name "*~"`
	rm -rf build
	make -C tools/jasmin-2.4 clean
	make -C tests clean
	make -C java clean
