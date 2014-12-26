.PHONY: all test tests java certs app clean jasmin

all: java jasmin tests

test: all
	tests/runtests.py

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
