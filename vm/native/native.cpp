#include <stdint.h>

int main() {}

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


}