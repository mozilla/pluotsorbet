SRCS=$(shell find ./cldc1.1.1 -name *.java) $(shell find ./vm -name *.java) $(shell find ./midp -name *.java)
CUSTOM_SRCS=$(shell find ./custom -name *.java)
JPP_DEFS=-DENABLE_JSR_205 -DENABLE_CHAMELEON -DENABLE_SSL -DENABLE_PUBLICKEYSTORE -DENABLE_JSR_211 -DENABLE_MULTIPLE_ISOLATES -DRECORD -DUSE_FILE_CONNECTION -DENABLE_JSR_234
JPP_SRCS=$(shell find . -name *.jpp)
JPP_DESTS=$(JPP_SRCS:.jpp=.java)
EXTRA=$(shell find . -name *.png) $(shell find . -name *.bin) $(shell find . -name *.xml)

VPATH=./cldc1.1.1 ./vm ./midp ./custom

classes.jar: $(SRCS) $(CUSTOM_SRCS) $(JPP_DESTS)
	rm -rf build
	mkdir build
	javac -cp cldc1.1.1:vm:midp:custom -source 1.3 -target 1.3 -bootclasspath "" -extdirs "" -d ./build $(SRCS) > /dev/null
	javac -sourcepath custom -cp build -source 1.3 -target 1.3 -bootclasspath "" -extdirs "" -d ./build $(CUSTOM_SRCS) > /dev/null
	cd build && jar cvf0 ../classes.jar *
	jar uvf0 classes.jar $(EXTRA)
	rm -rf build

tools/Jpp.class: tools/Jpp.java
	javac $^

# Preprocess all .jpp files to generate corresponding .java files.
$(JPP_DESTS): tools/Jpp.class $(JPP_SRCS)
	$(foreach file,$(JPP_SRCS), java -classpath tools Jpp $(file) $(JPP_DEFS) -o $(file:.jpp=.java);)

clean:
	rm -f `find . -name "*.jar" -or -name "*~"` $(JPP_DESTS)
