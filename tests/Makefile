SRCS=$(shell find . -not -path "./support/*" -name "*.java")

BUILDTIME_SUPPORT_DIR=support/buildtime
RUNTIME_SUPPORT_DIR=support/runtime
BUILDTIME_SUPPORT_SRCS=$(shell find $(BUILDTIME_SUPPORT_DIR) -name "*.java")
RUNTIME_SUPPORT_SRCS=$(shell find $(RUNTIME_SUPPORT_DIR) -name "*.java")

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
	# Build the buildtime support classes in-place, not in ./build, so they aren't available at runtime.
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar $(BUILDTIME_SUPPORT_SRCS)
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar:$(BUILDTIME_SUPPORT_DIR) -d ./build $^
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar -d ./build $(RUNTIME_SUPPORT_SRCS)
	cd build && jar cvfe ../tests.jar RunTests *
	jar uvf tests.jar gnu/testlet/vm/test.png midlets/test.png
	rm -rf build
	rm -f Testlets.java

clean:
	rm -f `find . -name "*.jar" -or -name "*~" -or -name "*.class"`
