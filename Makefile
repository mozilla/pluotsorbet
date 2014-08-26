test: java/tests.jar java/classes.jar
	killall python Python || true
	python -m SimpleHTTPServer &
	if casperjs --engine=slimerjs test `pwd`/tests/automation.js | grep FAIL; \
	then false; \
	else true; \
	fi
	killall python Python || true

java/tests.jar: java/classes.jar
	cd tests && make

java/classes.jar:
	cd java && make

clean:
	rm -f j2me.js tests/*.jar java/*.jar `find . -name "*~"`
