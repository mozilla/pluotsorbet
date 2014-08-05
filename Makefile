test: java/tests.jar java/classes.jar
	casperjs --enginer=slimerjs `pwd`/tests/automation.js

java/tests.jar: java/classes.jar
	cd tests && make

java/classes.jar:
	cd java && make

clean:
	rm -f j2me.js tests/*.jar java/*.jar `find . -name "*~"`
