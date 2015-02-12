SRC_DIRS=cldc1.1.1 vm midp custom
JPP_DEFS=-DENABLE_JSR_205 -DENABLE_SSL -DENABLE_PUBLICKEYSTORE -DENABLE_JSR_211 -DENABLE_MULTIPLE_ISOLATES -DRECORD -DUSE_FILE_CONNECTION -DENABLE_JSR_234

ifeq ($(JSR_256),1)
SRC_DIRS += jsr-256
JPP_DEFS += -DENABLE_JSR_256
endif

ifeq ($(JSR_082),1)
SRC_DIRS += jsr-082
endif

ifeq ($(JSR_179),1)
SRC_DIRS += jsr-179
JPP_DEFS += -DENABLE_JSR_179
endif

SRCS=$(shell find $(SRC_DIRS) -name *.java)
JPP_SRCS=$(shell find $(SRC_DIRS) -name *.jpp)
JPP_DESTS=$(JPP_SRCS:.jpp=.java)
EXTRA=$(shell find . -name *.png) $(shell find . -name *.bin) $(shell find . -name *.xml)

classes.jar: $(SRCS) $(JPP_DESTS)
	rm -rf build build-src
	mkdir build build-src
	$(foreach dir,$(SRC_DIRS),cp -a $(dir)/. build-src/;)
	find ./build-src -name *.java > build-srcs.txt
	javac -cp build-src -g:none -source 1.3 -target 1.3 -bootclasspath "" -extdirs "" -d ./build @build-srcs.txt > /dev/null
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
	rm -f build-srcs.txt
