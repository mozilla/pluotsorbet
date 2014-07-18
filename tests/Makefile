SRCS=$(shell find . -name "*.java")

all: tests.jar

../java/classes.jar:
	cd ../java && make

tests.jar: $(SRCS)
	rm -rf build
	mkdir build
	javac -source 1.3 -bootclasspath ../java/classes.jar -d ./build $^
	cd build && jar cvf ../tests.jar *
	rm -rf build

clean:
	rm -f `find . -name "*.jar" -or -name "*~"`
