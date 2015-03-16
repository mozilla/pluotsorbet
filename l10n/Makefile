LANG_FILES=$(shell find . -name "*.xml")
LANG_DESTS=../java/l10n/$(LANG_FILES:.xml=.json)

$(LANG_DESTS): $(LANG_FILES)
	rm -rf ../java/l10n/
	mkdir ../java/l10n/
	$(foreach file,$(LANG_FILES), ./xml_to_json.py $(file) ../java/l10n/$(file:.xml=.json);)
