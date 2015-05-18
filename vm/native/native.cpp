#include "gc.h"
#include <stdio.h>
#include <stdint.h>
#include <stdlib.h>
#include <inttypes.h>

uintptr_t heap = 0, bump = 0;

// Formatting: Printing longs.
// printf("L: %" PRId64 ", R: %" PRId64, *l, *r);

extern "C" {
	void lAdd(int64_t *result, int64_t *l, int64_t *r) {
		*result = *l + *r;
	}
	void lNeg(int64_t *result, int64_t *l) {
    *result = -*l;
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
		*result = *l << (v & 0x3F);
	}
	void lShr(int64_t *result, int64_t *l, int32_t v) {
  	*result = *l >> (v & 0x3F);
  }
  void lUshr(int64_t *result, int64_t *l, int32_t v) {
  	*result = (uint64_t)*l >> (v & 0x3F);
  }
  void lCmp(int32_t *result, int64_t *l, int64_t *r) {
    if (*l > *r) {
      *result = 1;
    } else if (*l < *r) {
      *result = -1;
    } else {
      *result = 0;
    }
  }

  int allocation = 0;

  uintptr_t gcMalloc(int32_t size) {
//    return (uintptr_t)GC_MALLOC_UNCOLLECTABLE(size);
    // Bump allocator.
    uintptr_t curr = bump;
    bump += (size + 3) & ~0x03;
    return curr;
  }
}

int main() {
//   GC_INIT();
//   GC_set_max_heap_size(1024 * 1024 * 16);
  heap = bump = (uintptr_t)malloc(1024 * 1024 * 16);
}