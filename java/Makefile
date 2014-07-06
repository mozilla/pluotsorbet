CLDC1_1_1_SRC=$(shell find cldc1.1.1 -name *.java) $(shell find vm -name *.java)
#javac -sourcepath ../vm ../vm/com/sun/cldchi/jvm/JVM.java ../vm/com/sun/cldchi/io/ResourceInputStream.java `find . -name *.java`

cldc1_1_1.jar: $(CLDC1_1_1_SRC)
	rm -rf build
	mkdir build
	javac -d ./build $^
	cd build && jar cvf ../cldc1.1.1.jar *
	rm -rf build
