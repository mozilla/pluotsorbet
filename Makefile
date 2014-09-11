.PHONY: java tests certs

test: java tests
	killall python Python || true
	python -m SimpleHTTPServer &
	if casperjs --engine=slimerjs test `pwd`/tests/automation.js | grep FAIL; \
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
