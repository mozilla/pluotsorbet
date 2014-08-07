SRCS=$(shell cd cldc1.1.1 && find . -name *.java) $(shell cd vm && find . -name *.java) $(shell cd midp && find . -name *.java)
EXTRA=$(shell find . -name *.png) $(shell find . -name *.bin) $(shell find . -name *.xml)

VPATH=./cldc1.1.1 ./vm ./midp

classes.jar: $(SRCS)
	rm -rf build
	mkdir build
	javac -sourcepath ./cldc1.1.1 -sourcepath ./vm -sourcepath ./midp -source 1.3 -target 1.3 -d ./build $^ > /dev/null
	cd build && jar cvf ../classes.jar *
	jar uvf classes.jar $(EXTRA)
	rm -rf build

clean:
	rm -f `find . -name "*.jar" -or -name "*~"`
