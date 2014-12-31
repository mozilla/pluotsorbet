.PHONY: all test tests j2me java certs app clean jasmin
BASIC_SRCS=$(shell find . -maxdepth 2 -name "*.ts")
JIT_SRCS=$(shell find jit -name "*.ts")
SHUMWAY_SRCS=$(shell find shumway -name "*.ts")

all: java jasmin tests j2me shumway

test: all
	tests/runtests.py

jasmin:
	make -C tools/jasmin-2.4

build/j2me.js: $(BASIC_SRCS) $(JIT_SRCS)
	@echo "Building J2ME"
	node tools/tsc.js --sourcemap --target ES5 references.ts -d --out build/j2me.js

build/jsc.js: jsc.ts build/j2me.js
	@echo "Building J2ME JSC CLI"
	node tools/tsc.js --sourcemap --target ES5 jsc.ts --out build/jsc.js

j2me: build/j2me.js build/jsc.js

aot: java j2me
	js build/jsc.js -cp java/classes.jar -d -jf java/classes.jar -cff classes.txt > build/classes.jar.js
	js build/jsc.js -cp java/classes.jar program.jar -d -jf program.jar -cff classes.txt > build/program.jar.js

	java -jar tools/closure.jar --formatting PRETTY_PRINT -O SIMPLE build/classes.jar.js > build/classes.jar.cc.js
	java -jar tools/closure.jar --formatting PRETTY_PRINT -O SIMPLE build/program.jar.js > build/program.jar.cc.js

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
	make -C tools/jasmin-2.4 clean
	make -C tests clean
	make -C java clean
