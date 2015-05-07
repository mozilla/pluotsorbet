#include <stdint.h>

int main() {}

extern "C" {
	void lAdd(int64_t *result, int64_t *l, int64_t *r) {
		*result = *l + *r;
	}
	void lSub(int64_t *result, int64_t *l, int64_t *r) {
    *result = *l - *r;
  }
}