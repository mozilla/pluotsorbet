SRCS=$(shell find . -not -path "./support/*" -name "*.java")

BUILDTIME_SUPPORT_DIR=support/buildtime
RUNTIME_SUPPORT_DIR=support/runtime
BUILDTIME_SUPPORT_SRCS=$(shell find $(BUILDTIME_SUPPORT_DIR) -name "*.java")
RUNTIME_SUPPORT_SRCS=$(shell find $(RUNTIME_SUPPORT_DIR) -name "*.java")

all: tests.jar

../java/classes.jar:
	cd ../java && make

tests.jar: $(SRCS)
	rm -rf build
	mkdir build
	# Build the buildtime support classes in-place, not in ./build, so they aren't available at runtime.
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar $(BUILDTIME_SUPPORT_SRCS) > /dev/null
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar:$(BUILDTIME_SUPPORT_DIR) -d ./build $^ > /dev/null
	javac -source 1.3 -target 1.3 -encoding UTF-8 -bootclasspath ../java/classes.jar -d ./build $(RUNTIME_SUPPORT_SRCS) > /dev/null
	jar cvfe tests.jar RunTests -C build/ .
	jar uvf tests.jar gnu/testlet/vm/test.png midlets/test.png midlets/favouriteartists/images/* midlets/blogwriter/images/* midlets/rlinks/images/* > /dev/null
	rm -rf build

clean:
	rm -f `find . -name "*.jar" -or -name "*~" -or -name "*.class"`
