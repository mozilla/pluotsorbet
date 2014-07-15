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

%.class: %.java
	javac -classpath java/cldc1.1.1.jar $<

j2me.js: $(SRC)
	cat $^ > $@

clean:
	rm -f j2me.js `find . -name "*~"`
