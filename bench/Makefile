SRCS=$(shell find ./scimark2src/jnt -name *.java)


scimark2.jar: $(SRCS)
	rm -rf build
	mkdir build
	javac -source 1.3 -target 1.3 -encoding UTF-8 -extdirs "" -d ./build $(SRCS) > /dev/null
	cd build && jar cvf0 ../scimark2.jar *
	rm -rf build

clean:
	rm -f `find . -name "*.jar"`
