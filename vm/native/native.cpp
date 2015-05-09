#include <stdint.h>
#include <stdlib.h>

uintptr_t heap = 0, bump = 0;

extern "C" {
	void lAdd(int64_t *result, int64_t *l, int64_t *r) {
		*result = *l + *r;
	}
	void lSub(int64_t *result, int64_t *l, int64_t *r) {
    *result = *l - *r;
  }
  void lDiv(int64_t *result, int64_t *l, int64_t *r) {
    *result = *l / *r;
  }
  void lMul(int64_t *result, int64_t *l, int64_t *r) {
    *result = *l * *r;
  }
  void lRem(int64_t *result, int64_t *l, int64_t *r) {
    *result = *l % *r;
  }
  void lShl(int64_t *result, int64_t *l, int32_t v) {
		*result = *l << v;
	}
	void lShr(int64_t *result, int64_t *l, int32_t v) {
  	*result = *l >> v;
  }
  void lUshr(int64_t *result, int64_t *l, int32_t v) {
  	*result = (uint64_t)*l >> v;
  }
  void lCmp(int32_t *result, int64_t *l, int64_t *r) {
    if (l > r) {
      *result = 1;
    } else if (l < r) {
      *result = -1;
    } else {
      *result = 0;
    }
  }

  uintptr_t gcMalloc(int32_t size) {
    return bump += (size + 3) & ~0x03;
  }
}

int main() {
  bump = heap = (uintptr_t)malloc(1024 * 1024 * 16);
}