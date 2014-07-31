SRCS=$(shell cd cldc1.1.1 && find . -name *.java) $(shell cd vm && find . -name *.java) $(shell cd midp && find . -name *.java)

VPATH=./cldc1.1.1 ./vm ./midp

classes.jar: $(SRCS)
	rm -rf build
	mkdir build
	javac -sourcepath ./cldc1.1.1 -sourcepath ./vm -sourcepath ./midp -source 1.3 -target 1.3 -d ./build $^
	cd build && jar cvf ../classes.jar *
	jar uvf classes.jar assets/0/skin.bin
	rm -rf build

clean:
	rm -f `find . -name "*.jar" -or -name "*~"`
