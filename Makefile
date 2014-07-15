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

j2me.js: $(SRC)
	cat $^ > $@

clean:
	rm -f j2me.js `find . -name "*~"`
