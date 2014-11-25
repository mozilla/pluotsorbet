.PHONY: all test tests j2me java certs app clean

all: java tests j2me

test: all
	rm -f test.log
	killall python Python || true
	python tests/httpServer.py &
	python tests/echoServer.py &
	cd tests && python httpsServer.py &
	cd tests && python sslEchoServer.py &
	casperjs --engine=slimerjs test `pwd`/tests/automation.js > test.log
	killall python Python || true
	python dumplog.py
	if grep -q FAIL test.log; \
	then false; \
	else true; \
	fi

j2me:
	@echo "Building J2ME"
	node tools/tsc.js --sourcemap --target ES5 references.ts -d --out build/j2me.js
	@echo "Building J2ME JSC CLI"
	node tools/tsc.js --sourcemap --target ES5 jsc.ts --out build/jsc.js

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
	make -C tests clean
	make -C java clean
