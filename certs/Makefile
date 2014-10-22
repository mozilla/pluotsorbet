CERTS=$(wildcard *.der)

MEKEYTOOL ?= mekeytool

_main.ks: $(CERTS)
	rm -f _main.ks j2se_main.ks

	for file in $(CERTS) ; do \
		echo $$file ; \
		keytool -import -v -trustcacerts -alias $$file -file $$file -keystore j2se_main.ks -storepass javasdk ; \
		$(MEKEYTOOL) -import -MEkeystore _main.ks -keystore j2se_main.ks -alias $$file -domain identified_third_party ; \
	done

_test.ks: _main.ks ../tests/cert.der
	rm -f _test.ks j2se_test.ks
	cp _main.ks _test.ks
	cp j2se_main.ks j2se_test.ks

	keytool -import -v -trustcacerts -alias ../tests/cert.der -file ../tests/cert.der -keystore j2se_test.ks -storepass javasdk ; \
	$(MEKEYTOOL) -import -MEkeystore _test.ks -keystore j2se_test.ks -alias ../tests/cert.der -domain identified_third_party ; \

clean:
	rm -f _main.ks j2se_main.ks _test.ks j2se_test.ks
