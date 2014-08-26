test: java/tests.jar java/classes.jar
	killall python Python || true
	python -m SimpleHTTPServer &
	casperjs --verbose --log-level=debug --engine=slimerjs test `pwd`/tests/automation.js
	killall python Python || true

java/tests.jar: java/classes.jar
	cd tests && make

java/classes.jar:
	cd java && make

clean:
	rm -f j2me.js `find . -name "*~"`
	cd tests && make clean
	cd java && make clean
