.PHONY: all

all:
	node tsc.js --sourcemap --target ES5 references.ts -d --out build/opt.js
	node tsc.js --sourcemap --target ES5 shell.ts --out build/shell.js

clean:
	rm build/opt.js