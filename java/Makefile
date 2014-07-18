SRCS=$(shell find cldc1.1.1 -name *.java) $(shell find vm -name *.java)

classes.jar: $(SRCS)
	rm -rf build
	mkdir build
	javac -source 1.3 -d ./build $^
	cd build && jar cvf ../classes.jar *
	rm -rf build
