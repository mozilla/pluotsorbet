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
	javac -source 1.3 -bootclasspath ../java/classes.jar -d ./build $^
	cd build && jar cvf ../tests.jar *
	jar uvf tests.jar test.png
	rm -rf build

clean:
	rm -f `find . -name "*.jar" -or -name "*~"`
