CERTS=$(wildcard *.der)

_main.ks: $(CERTS)
	rm -f _main.ks j2se_main.ks

	for file in $(CERTS) ; do \
		echo $$file ; \
		keytool -import -v -trustcacerts -alias $$file -file $$file -keystore j2se_main.ks -storepass javasdk ; \
		mekeytool -import -MEkeystore _main.ks -keystore j2se_main.ks -alias $$file -domain identified_third_party ; \
	done

	rm -f j2se_main.ks

clean:
	rm -f _main.ks j2se_main.ks
