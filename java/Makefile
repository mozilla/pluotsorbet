SRCS=$(shell find ./cldc1.1.1 -name *.java) $(shell find ./vm -name *.java) $(shell find ./midp -name *.java) $(shell find ./jsr-075 -name *.java) $(shell find ./custom -name *.java)
SRC_DIRS=cldc1.1.1/. vm/. midp/. jsr-075/. custom/.
JPP_DEFS=-DENABLE_JSR_205 -DENABLE_SSL -DENABLE_PUBLICKEYSTORE -DENABLE_JSR_211 -DENABLE_MULTIPLE_ISOLATES -DRECORD -DUSE_FILE_CONNECTION -DENABLE_JSR_234
JPP_SRCS=$(shell find . -name *.jpp)

ifeq ($(JSR_256),1)
SRCS += $(shell find ./jsr-256 -name *.java)
SRC_DIRS += jsr-256/.
JPP_DEFS += -DENABLE_JSR_256
JPP_SRCS += $(shell find ./jsr-256 -name *.jpp)
endif

JPP_DESTS=$(JPP_SRCS:.jpp=.java)
EXTRA=$(shell find . -name *.png) $(shell find . -name *.bin) $(shell find . -name *.xml)

VPATH=./cldc1.1.1 ./vm ./midp ./custom

classes.jar: $(SRCS) $(JPP_DESTS)
	rm -rf build build-src
	mkdir build build-src
	cp -a $(SRC_DIRS) build-src/
	javac -cp build-src -g:none -source 1.3 -target 1.3 -bootclasspath "" -extdirs "" -d ./build `find ./build-src -name *.java` > /dev/null
	cd build && jar cvf0 ../classes.jar *
	jar uvf0 classes.jar $(EXTRA)
	rm -rf build build-src

tools/Jpp.class: tools/Jpp.java
	javac $^

# Preprocess all .jpp files to generate corresponding .java files.
$(JPP_DESTS): tools/Jpp.class $(JPP_SRCS)
	$(foreach file,$(JPP_SRCS), java -classpath tools Jpp $(file) $(JPP_DEFS) -o $(file:.jpp=.java);)

clean:
	rm -f `find . -name "*.jar" -or -name "*~"` $(JPP_DESTS)
