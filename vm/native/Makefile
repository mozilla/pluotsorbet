.PHONY: all

all: native.cpp
	# Make sure emcc in your path.
	emcc -IBoehm.js/include/ Boehm.js/.libs/libgc.dylib -Oz native.cpp -DNDEBUG -o native.raw.js --memory-init-file 0 -s TOTAL_STACK=16384 -s TOTAL_MEMORY=134217728 -s NO_FILESYSTEM=1 -s NO_BROWSER=1 -O3 \
	-s 'EXPORTED_FUNCTIONS=["_main", "_lAdd", "_lNeg", "_lSub", "_lShl", "_lShr", "_lUshr", "_lMul", "_lDiv", "_lRem", "_lCmp", "_gcMalloc"]' \
	-s 'DEFAULT_LIBRARY_FUNCS_TO_INCLUDE=["memcpy", "memset", "malloc", "free", "puts"]'
	echo "var ASM = (function(Module) {" >> native.js
	cat native.raw.js >> native.js
	echo "" >> native.js
	echo "  return Module;" >> native.js
	echo "})(ASM);" >> native.js
	mv native.js ../../libs/native.js
