SRCS=$(shell find . -name "*.java")

benchmark.jar: $(SRCS) ../java/classes.jar ../tests/tests.jar
	rm -rf build
	mkdir build
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar:../tests/tests.jar -extdirs "" -d ./build $(SRCS) > /dev/null
	cd build && jar cvf0 ../benchmark.jar *
	rm -rf build

../java/classes.jar:
	cd .. && make java/classes.jar

../tests/tests.jar:
	cd .. && make tests/tests.jar

clean:
	rm -f `find . -name "*.jar"`
