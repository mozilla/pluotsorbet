.PHONY: all

LANG_FILES=$(shell find . -name "*.xml")

all: $(LANG_FILES)
	rm -rf build
	mkdir build
	$(foreach file,$(LANG_FILES), cp $(file) build/$(file).json;)

clean:
	rm -rf build
