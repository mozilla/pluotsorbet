SRCS=$(shell find . -name "*.java")

all: tests.jar

../java/classes.jar:
	cd ../java && make

Testlets.java: $(SRCS) Makefile
	echo "public class Testlets {" > $@
	echo "  static String[] list = {" >> $@
	grep "implements Testlet" `find . -name "*.java"` | sed -e "s/^.\///" -e "s/\.java.*//" -e "s/\(.*\)/\"\1\",/" >> $@
	echo "  null};" >> $@
	echo "};" >> $@

tests.jar: $(SRCS) Testlets.java
	rm -rf build
	mkdir build
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar -d ./build $^
	cd build && jar cvfe ../tests.jar RunTests *
	jar uvf tests.jar gnu/testlet/vm/test.png midlets/test.png
	rm -rf build
	rm -f Testlets.java

clean:
	rm -f `find . -name "*.jar" -or -name "*~" -or -name "*.class"`
