.PHONY: all test tests java certs app clean jasmin

all: java jasmin tests

test: all
	rm -f test.log
	killall python Python || true
	python tests/httpServer.py &
	python tests/echoServer.py &
	cd tests && python httpsServer.py &
	cd tests && python sslEchoServer.py &
	cd tests && python waitServers.py
	casperjs --engine=slimerjs test `pwd`/tests/automation.js | tee test.log
	casperjs --engine=slimerjs test `pwd`/tests/fs/automation.js | tee -a test.log
	killall python Python || true
	python dumplog.py
	if grep -q FAIL test.log; \
	then false; \
	else true; \
	fi

jasmin:
	make -C tools/jasmin-2.4

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
