SRC = zipfile.js \
      util.js \
      gLong.js \
      classfile/reader.js \
      classfile/accessflags.js \
      classfile/attributetypes.js \
      classfile/classfile.js \
      classfile/signature.js \
      classfile/tags.js \
      classinfo.js \
      arraytypes.js \
      signature.js \
      opcodes.js \
      classes.js \
      native.js \
      frame.js \
      thread.js \
      threads.js \
      jvm.js \
      main.js

TESTS_SRC = $(shell ls tests/*.java)
TESTS_BIN = $(TESTS_SRC:.java=.class)

tests: $(TESTS_BIN)

java/classes.jar:
	cd java && make

%.class: %.java java/classes.jar
	javac -Xlint:-options -source 1.3 -bootclasspath java/classes.jar $<

j2me.js: $(SRC)
	cat $^ > $@

clean:
	rm -f j2me.js tests/*.class `find . -name "*~"`
