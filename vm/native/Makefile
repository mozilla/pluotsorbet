.PHONY: all

UNAME_S := $(shell uname -s)
UNAME_M := $(shell uname -m)
ifeq ($(UNAME_S),Linux)
	BOEHM_LIB=libgc.so
endif
ifeq ($(UNAME_S),Darwin)
	BOEHM_LIB=libgc.dylib
endif
ifneq (,$(findstring MINGW,$(uname_S)))
	BOEHM_LIB=libgc.dll
endif
ifneq (,$(findstring CYGWIN,$(uname_S)))
	BOEHM_LIB=libgc.dll
endif

Boehm.js/.libs/$(BOEHM_LIB):
	cd Boehm.js
	emconfigure ./configure --without-threads --disable-threads __EMSCRIPTEN__=1
	emmake make
	cd ..

all: native.cpp
	emcc -IBoehm.js/include/ Boehm.js/.libs/$(BOEHM_LIB) -Oz native.cpp -DNDEBUG -o native.raw.js --memory-init-file 0 -s TOTAL_STACK=16384 -s TOTAL_MEMORY=134217728 -s NO_FILESYSTEM=1 -s NO_BROWSER=1 -O3 \
	-s 'EXPORTED_FUNCTIONS=["_main", "_lAdd", "_lNeg", "_lSub", "_lShl", "_lShr", "_lUshr", "_lMul", "_lDiv", "_lRem", "_lCmp", "_gcMalloc"]' \
	-s 'DEFAULT_LIBRARY_FUNCS_TO_INCLUDE=["memcpy", "memset", "malloc", "free", "puts"]'
	echo "var ASM = (function(Module) {" >> native.js
	cat native.raw.js >> native.js
	echo "" >> native.js
	echo "  return Module;" >> native.js
	echo "})(ASM);" >> native.js
	mv native.js ../../libs/native.js
