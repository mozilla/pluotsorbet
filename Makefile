.PHONY: all test tests java certs app clean

all: java tests

test: all
	rm -f test.log
	killall python Python || true
	python tests/httpServer.py &
	python tests/echoServer.py &
	cd tests && python httpsServer.py &
	cd tests && python sslEchoServer.py &
	cd tests && python waitServers.py
	casperjs --engine=slimerjs test `pwd`/tests/automation.js > test.log
	cp -r tests/profiles/fs-v1 test-profile-fs-v1
	casperjs --engine=slimerjs test -profile `pwd`/test-profile-fs-v1 `pwd`/tests/automation.js >> test.log
	rm -rf test-profile-fs-v1
	killall python Python || true
	python dumplog.py
	if grep -q FAIL test.log; \
	then false; \
	else true; \
	fi

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
