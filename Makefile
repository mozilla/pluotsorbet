test: java/tests.jar java/classes.jar
	killall python || true
	python -m SimpleHTTPServer &
	casperjs --verbose --log-level=debug --engine=slimerjs test `pwd`/tests/automation.js
	killall python || true

java/tests.jar: java/classes.jar
	cd tests && make

java/classes.jar:
	cd java && make

clean:
	rm -f j2me.js tests/*.jar java/*.jar `find . -name "*~"`
