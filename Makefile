classes.jar: $(shell find jdk -type f)
	cd jdk && jar cvf ../classes.jar $(^:jdk/%=%)

clean:
	rm -f `find . -name "*~"`
