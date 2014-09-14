.PHONY: java tests certs

test: java tests
	killall python Python || true
	python -m SimpleHTTPServer &
	casperjs --engine=slimerjs test `pwd`/tests/automation.js 2>&1 | tee test.log
	if grep -q FAIL test.log; \
	then false; \
	else true; \
	fi
	killall python Python || true

tests:
	make -C tests

java:
	make -C java

certs:
	make -C certs

clean:
	rm -f j2me.js `find . -name "*~"`
	make -C tests clean
	make -C java clean
