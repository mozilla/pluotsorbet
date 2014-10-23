SRCS=$(shell find ./cldc1.1.1 -name *.java) $(shell find ./vm -name *.java) $(shell find ./midp -name *.java)
CUSTOM_SRCS=$(shell find ./custom -name *.java)
EXTRA=$(shell find . -name *.png) $(shell find . -name *.bin) $(shell find . -name *.xml)

VPATH=./cldc1.1.1 ./vm ./midp ./custom

classes.jar: $(SRCS) $(CUSTOM_SRCS)
	rm -rf build
	mkdir build
	javac -cp cldc1.1.1:vm:midp -source 1.3 -target 1.3 -d ./build $(SRCS) > /dev/null
	javac -sourcepath custom -cp build -source 1.3 -target 1.3 -d ./build $(CUSTOM_SRCS) > /dev/null
	cd build && jar cvf0 ../classes.jar *
	jar uvf0 classes.jar $(EXTRA)
	rm -rf build

clean:
	rm -f `find . -name "*.jar" -or -name "*~"`
