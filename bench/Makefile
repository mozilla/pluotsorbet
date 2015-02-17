SRCS=$(shell find . -name "*.java")

benchmark.jar: $(SRCS)
	rm -rf build
	mkdir build
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar -extdirs "" -d ./build $(SRCS) > /dev/null
	cd build && jar cvf0 ../benchmark.jar *
	rm -rf build

clean:
	rm -f `find . -name "*.jar"`
