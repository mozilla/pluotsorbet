/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package gnu.testlet.vm;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

class TestArithmetic implements Testlet {
    TestHarness th;

    private static int int_3 = -3;
    private static int int1 = 1;
    private static int int3 = 3;
    private static int int5 = 5;
    private static int int33 = 33;
    private static int int65 = 65;
    private static long long_3 = -3;
    private static long long_1 = -1;
    private static long long0 = 0;
    private static long long2 = 2;
    private static long long10000000000 = 10000000000L;
    private static long long0x0110000000000011 = 0x0110000000000011L;
    private static long long0x1010000000000101 = 0x1010000000000101L;
    private static long long0xBEEFBEEFCAFEBABE = 0xBEEFBEEFCAFEBABEL;

    public void test(TestHarness th) {
        this.th = th;

        itest();
        ltest();
        ftest();
        dtest();
        nanTestFloat();
        nanTestDouble();
    }

    private void itest() {
        int a = int3;
        th.check(a + 1, 4);
        th.check(a - 1, 2);
        th.check(a * 3, 9);
        th.check(a / 2, 1);
        th.check(a % 2, 1);
        th.check(-a, -3);
        th.check(++a, 4);

        a = int_3;
        th.check(a + 1, -2);
        th.check(a - 1, -4);
        th.check(a * 3, -9);
        th.check(a / 2, -1);
        th.check(a % 2, -1);
        th.check(-a, 3);
        th.check(++a, -2);

        a = int3;     // 0x00000011
        int b = int5; // 0x00000101

        th.check(a & b, 1);
        th.check(a | b, 7);
        th.check(a ^ b, 6);

        a = int_3; // 0xfffffffd;

        th.check(a << 1, -6);
        th.check(a >> 1, -2);
        th.check(a >>> 1, 2147483646);

        // funny Java shift cases
        a = int1;

        th.check(a << 32, 1);
        th.check((a << 16) << 16, 0);
        th.check(a << 33, 2);
        th.check(a << -1, -2147483648);
        th.check(a << -32, 1);
        th.check(a << -33, -2147483648);
        th.check(1 << int33, 2);

        th.check(a >> 32, 1);
        th.check((a >> 16) >> 16, 0);
        th.check(a >> 33, 0);
        th.check(a >> -1, 0);
        th.check(a >> -32, 1);
        th.check(a >> 33, 0);
        th.check(-4 >> int33, -2);

        th.check(a >>> 32, 1);
        th.check((a >>> 16) >>> 16, 0);
        th.check(a >>> 33, 0);
        th.check(a >>> -1, 0);
        th.check(a >>> -32, 1);
        th.check(a >>> -33, 0);
        th.check(-4 >>> int33, 2147483646);

        // IA32 bit test patterns
        th.check(((1 << int1) & int3) != 0);
        th.check(((1 << int33) & int3) != 0);

        th.check((int3 & (1 << int1)) != 0);
        th.check((int3 & (1 << int33)) != 0);

        th.check(((int3 >> int1) & 1) == 1);
        th.check(((int3 >> int33) & 1) == 1);

        th.check(((int3 >>> int1) & 1) == 1);
        th.check(((int3 >>> int33) & 1) == 1);

        // Rotate tests
        th.check((int5 << 1)|(int5 >>> -1), 10); // Rotate left by 1
        th.check((int5 >>> -1)|(int5 << 1), 10); // Rotate left by 1
        th.check((int5 << -1)|(int5 >>> 1), -2147483646); // Rotate right by 1
        th.check((int5 >>> 1)|(int5 << -1), -2147483646); // Rotate right by 1
        th.check((int5 << int1)|(int5 >>> -int1), 10); // Rotate left by 1
        th.check((int5 >>> -int1)|(int5 << int1), 10); // Rotate left by 1
        th.check((int5 << -int1)|(int5 >>> int1), -2147483646); // Rotate right by 1
        th.check((int5 >>> int1)|(int5 << -int1), -2147483646); // Rotate right by 1
    }

    private void ltest() {
        long a = long10000000000;
        long b = long2;

        th.check(a + b, 10000000002L);
        th.check(a - b, 9999999998L);
        th.check(a * b, 20000000000L);
        th.check(a / b, 5000000000L);
        th.check(a % b, 0);
        th.check(-b, -2);
        th.check(-a, -10000000000L);

        a = long_3;

        th.check(a + 2, -1);
        th.check(a - 2, -5);
        th.check(a * 3, -9);
        th.check(a / 2, -1);
        th.check(a % 2, -1);
        th.check(-a, 3);

        a = long0x0110000000000011;
        b = long0x1010000000000101;

        th.check(a & b, 4503599627370497L);
        th.check(a | b, 1229482698272145681L);
        th.check(a ^ b, 1224979098644775184L);

        // bit patterns that can be optimized for certain operators if converting
        // long operators into int operators
        long long0x00000000FFFFFFFF = 0x00000000FFFFFFFFL;
        long long0xFFFFFFFF00000000 = 0xFFFFFFFF00000000L;
        long long0x00000001FFFFFFFF = 0x00000001FFFFFFFFL;
        long long0xFFFFFFFF00000001 = 0xFFFFFFFF00000001L;
        long long0x0000000100000000 = 0x0000000100000000L;
        long long0x0000000100000001 = 0x0000000100000001L;

        a = long_1;

        th.check(a * long0x00000000FFFFFFFF, -4294967295L);
        th.check(a * long0xFFFFFFFF00000000, 4294967296L);
        th.check(a * long0x00000001FFFFFFFF, -8589934591L);
        th.check(a * long0xFFFFFFFF00000001, 4294967295L);
        th.check(a * long0x0000000100000000, -4294967296L);
        th.check(a * long0x0000000100000001, -4294967297L);

        th.check(a & long0x00000000FFFFFFFF, 4294967295L);
        th.check(a & long0xFFFFFFFF00000000, -4294967296L);
        th.check(a & long0x00000001FFFFFFFF, 8589934591L);
        th.check(a & long0xFFFFFFFF00000001, -4294967295L);
        th.check(a & long0x0000000100000000, 4294967296L);
        th.check(a & long0x0000000100000001, 4294967297L);

        a = long0;

        th.check(a | long0x00000000FFFFFFFF, 4294967295L);
        th.check(a | long0xFFFFFFFF00000000, -4294967296L);
        th.check(a | long0x00000001FFFFFFFF, 8589934591L);
        th.check(a | long0xFFFFFFFF00000001, -4294967295L);
        th.check(a | long0x0000000100000001, 4294967297L);

        th.check(a ^ long0x00000000FFFFFFFF, 4294967295L);
        th.check(a ^ long0xFFFFFFFF00000000, -4294967296L);
        th.check(a ^ long0x00000001FFFFFFFF, 8589934591L);
        th.check(a ^ long0xFFFFFFFF00000001, -4294967295L);
        th.check(a ^ long0x0000000100000001, 4294967297L);

        a = long0xBEEFBEEFCAFEBABE;

        th.check(a << 1, 9070106573795063164L);
        th.check(a << 2, -306530926119425288L);
        th.check(a << 3, -613061852238850576L);
        th.check(a << 4, -1226123704477701152L);
        th.check(a << 8, -1171235197933666816L);
        th.check(a << 16, -4688305491665879040L);
        th.check(a << 32, -3819410108757049344L);
        th.check(a << 33, -7638820217514098688L);
        th.check(a << 34, 3169103638681354240L);
        th.check(a << 35, 6338207277362708480L);
        th.check(a << 36, -5770329518984134656L);
        th.check(a << 40, -91551935198396416L);
        th.check(a << 48, -4990551337079930880L);
        th.check(a << 56, -4755801206503243776L);
        th.check(a << 63, 0L);
        th.check(a << 64, -4688318749957244226L);
        th.check(a << 65, 9070106573795063164L);
        th.check(a << int65, 9070106573795063164L);

        th.check(a >> 1, -2344159374978622113L);
        th.check(a >> 2, -1172079687489311057L);
        th.check(a >> 3, -586039843744655529L);
        th.check(a >> 4, -293019921872327765L);
        th.check(a >> 8, -18313745117020486L);
        th.check(a >> 16, -71538066863362L);
        th.check(a >> 32, -1091584273L);
        th.check(a >> 33, -545792137L);
        th.check(a >> 34, -272896069L);
        th.check(a >> 35, -136448035L);
        th.check(a >> 36, -68224018L);
        th.check(a >> 40, -4264002L);
        th.check(a >> 48, -16657L);
        th.check(a >> 56, -66L);
        th.check(a >> 63, -1L);
        th.check(a >> 64, -4688318749957244226L);
        th.check(a >> 65, -2344159374978622113L);
        th.check(a >> int65, -2344159374978622113L);

        th.check(a >>> 1, 6879212661876153695L);
        th.check(a >>> 2, 3439606330938076847L);
        th.check(a >>> 3, 1719803165469038423L);
        th.check(a >>> 4, 859901582734519211L);
        th.check(a >>> 8, 53743848920907450L);
        th.check(a >>> 16, 209936909847294L);
        th.check(a >>> 32, 3203383023L);
        th.check(a >>> 33, 1601691511L);
        th.check(a >>> 34, 800845755L);
        th.check(a >>> 35, 400422877L);
        th.check(a >>> 36, 200211438L);
        th.check(a >>> 40, 12513214L);
        th.check(a >>> 48, 48879L);
        th.check(a >>> 56, 190L);
        th.check(a >>> 63, 1L);
        th.check(a >>> 64, -4688318749957244226L);
        th.check(a >>> 65, 6879212661876153695L);
        th.check(a >>> int65, 6879212661876153695L);
    }

    private static float float0 = 0.0f;
    private static float float0_9 = 0.9f;
    private static float float1 = 1.0f;
    private static float float1_5 = 1.5f;
    private static float float2 = 2.0f;
    private static float float_maxint = (float)Integer.MAX_VALUE;
    private static float float_minint = (float)Integer.MIN_VALUE;
    private static double double0 = 0.0f;
    private static double double1 = 1.0f;
    private static double double2 = 2.0f;
    private static double double_maxint = (double)Integer.MAX_VALUE;
    private static double double_minint = (double)Integer.MIN_VALUE;
    private static float float_maxlong = (float)Long.MAX_VALUE;
    private static float float_minlong = (float)Long.MIN_VALUE;
    private static double double_maxlong = (double)Long.MAX_VALUE;
    private static double double_minlong = (double)Long.MIN_VALUE;

    private void ftest() {
        // f2i, d2i, f2l and d2l tests
        th.check((int)float0, 0);
        th.check((int)float1, 1);
        th.check((int)double0, 0);
        th.check((int)double1, 1);
        th.check((int)Float.NaN, 0);
        th.check((int)-Float.NaN, 0);
        th.check((int)Double.NaN, 0);
        th.check((int)-Double.NaN, 0);
        th.check((int)Float.POSITIVE_INFINITY, 2147483647);
        th.check((int)Float.NEGATIVE_INFINITY, -2147483648);
        th.check((int)float_maxint, 2147483647);
        th.check((int)float_minint, -2147483648);
        th.check((int)double_maxint, 2147483647);
        th.check((int)double_minint, -2147483648);

        th.check((long)float0, 0L);
        th.check((long)float1, 1L);
        th.check((long)double0, 0L);
        th.check((long)double1, 1L);
        th.check((long)Float.NaN, 0L);
        th.check((long)-Float.NaN, 0L);
        th.check((long)Double.NaN, 0L);
        th.check((long)-Double.NaN, 0L);
        th.check((long)Float.POSITIVE_INFINITY, 9223372036854775807L);
        th.check((long)Float.NEGATIVE_INFINITY, -9223372036854775808L);
        th.check((long)float_maxlong, 9223372036854775807L);
        th.check((long)float_minlong, -9223372036854775808L);
        th.check((long)double_maxlong, 9223372036854775807L);
        th.check((long)double_minlong, -9223372036854775808L);

        float a = float1;
        float b = float2;

        th.check(Integer.toHexString(Float.floatToIntBits(a + b)), Integer.toHexString(Float.floatToIntBits(3.0F)));  // fadd
        th.check(Integer.toHexString(Float.floatToIntBits(a - b)), Integer.toHexString(Float.floatToIntBits(-1.0F)));  // fsub
        th.check(Integer.toHexString(Float.floatToIntBits(a * b)), Integer.toHexString(Float.floatToIntBits(2.0F)));  // fmul
        th.check(Integer.toHexString(Float.floatToIntBits(a / b)), Integer.toHexString(Float.floatToIntBits(0.5F)));  // fdiv
        th.check(Integer.toHexString(Float.floatToIntBits(-a)), Integer.toHexString(Float.floatToIntBits(-1.0F)));  // fneg

        a = float1_5;
        b = float0_9;
        th.check(Integer.toHexString(Float.floatToIntBits(a % b)), Integer.toHexString(Float.floatToIntBits(0.6F)));  // frem

        th.check(String.valueOf(float0/float0), "NaN");
        th.check(String.valueOf(Float.NaN + Float.NaN), "NaN");
        th.check(String.valueOf(Float.NaN + float2), "NaN");
        th.check(String.valueOf(Float.NaN * float2), "NaN");
        th.check(String.valueOf((float0 / 0.0) * Float.POSITIVE_INFINITY), "NaN");
        th.check(String.valueOf((float1 / 0.0) * 0.0), "NaN");
        th.check(String.valueOf((float1 / 0.0) - (float1 / 0.0)), "NaN");
        th.check(String.valueOf((float1 / 0.0) / (float1 / 0.0)), "NaN");
        th.check(String.valueOf((-float1 / 0.0) * 0.0), "NaN");
        th.check(String.valueOf((-float1 / 0.0) - (-float1 / 0.0)), "NaN");
        th.check(!(Float.NaN > float1));
        th.check(!(Float.NaN < float1));
        th.check(Float.NaN != float1);
        th.check(Float.NaN != -float1);
        th.check(!(Float.NaN > Float.POSITIVE_INFINITY));
        th.check(!(Float.NaN < Float.POSITIVE_INFINITY));
        th.check(Float.POSITIVE_INFINITY == Float.POSITIVE_INFINITY);
        th.check(-Float.POSITIVE_INFINITY == Float.NEGATIVE_INFINITY);
        th.check(Float.NEGATIVE_INFINITY < float1);
        th.check((-float1/0.0) == (-float1/0.0));
        th.check(float1/0.0, Float.POSITIVE_INFINITY);
        th.check((float1/0.0) + 2.0, Float.POSITIVE_INFINITY);
        th.check((float1/0.0) * 0.5, Float.POSITIVE_INFINITY);
        th.check((float1/0.0) + (float1/0.0), Float.POSITIVE_INFINITY);
        th.check((float1/0.0) * (float1/0.0), Float.POSITIVE_INFINITY);
        th.check(Math.abs(-float1/0.0), Float.POSITIVE_INFINITY);
        th.check(-float1/0.0, Float.NEGATIVE_INFINITY);
        th.check((-float1/0.0) + 2.0, Float.NEGATIVE_INFINITY);
        th.check((-float1/0.0) * 0.5, Float.NEGATIVE_INFINITY);
        th.check((int)(float1/0.0), 2147483647);
        th.check((int)(-float1/0.0), -2147483648);
        th.check((int)Float.NaN, 0);
    }

    private void dtest() {
        double a = 1;
        double b = 2;

        th.check(Double.doubleToLongBits(a + b), Double.doubleToLongBits(3.0D));  // dadd
        th.check(Double.doubleToLongBits(a - b), Double.doubleToLongBits(-1.0D));  // dsub
        th.check(Double.doubleToLongBits(a * b), Double.doubleToLongBits(2.0D));  // dmul
        th.check(Double.doubleToLongBits(a / b), Double.doubleToLongBits(0.5D));  // ddiv
        th.check(Double.doubleToLongBits(-a), Double.doubleToLongBits(-1.0D));  // dneg

        a = 1.5;
        b = 0.9;
        th.check(Double.doubleToLongBits(a % b), Double.doubleToLongBits(0.6D));  // drem

        th.check(String.valueOf(double0/double0), "NaN");
        th.check(String.valueOf(Double.NaN + Double.NaN), "NaN");
        th.check(String.valueOf(Double.NaN + double2), "NaN");
        th.check(String.valueOf(Double.NaN * double2), "NaN");
        th.check(String.valueOf((double0 / 0.0) * Double.POSITIVE_INFINITY), "NaN");
        th.check(String.valueOf((double1 / 0.0) * 0.0), "NaN");
        th.check(String.valueOf((double1 / 0.0) - (double1 / 0.0)), "NaN");
        th.check(String.valueOf((double1 / 0.0) / (double1 / 0.0)), "NaN");
        th.check(String.valueOf((-double1 / 0.0) * 0.0), "NaN");
        th.check(String.valueOf((-double1 / 0.0) - (-double1 / 0.0)), "NaN");
        th.check(!(Double.NaN > double1));
        th.check(!(Double.NaN < double1));
        th.check(Double.NaN != double1);
        th.check(Double.NaN != -double1);
        th.check(!(Double.NaN > Double.POSITIVE_INFINITY));
        th.check(!(Double.NaN < Double.POSITIVE_INFINITY));
        th.check(Double.POSITIVE_INFINITY == Double.POSITIVE_INFINITY);
        th.check(-Double.POSITIVE_INFINITY == Double.NEGATIVE_INFINITY);
        th.check(Double.NEGATIVE_INFINITY < double1);
        th.check((-double1/0.0) == (-double1/0.0));
        th.check(double1/0.0, Double.POSITIVE_INFINITY);
        th.check((double1/0.0) + 2.0, Double.POSITIVE_INFINITY);
        th.check((double1/0.0) * 0.5, Double.POSITIVE_INFINITY);
        th.check((double1/0.0) + (double1/0.0), Double.POSITIVE_INFINITY);
        th.check((double1/0.0) * (double1/0.0), Double.POSITIVE_INFINITY);
        th.check(Math.abs(-double1/0.0), Double.POSITIVE_INFINITY);
        th.check(-double1/0.0, Double.NEGATIVE_INFINITY);
        th.check((-double1/0.0) + 2.0, Double.NEGATIVE_INFINITY);
        th.check((-double1/0.0) * 0.5, Double.NEGATIVE_INFINITY);
        th.check((int)(double1/0.0), 2147483647);
        th.check((int)(-double1/0.0), -2147483648);
        th.check((int)Double.NaN, 0);
    }

    private void nanTestFloat() {
        float zero = float0;
        float NaN = zero / zero;

        th.check(!(NaN < NaN));
        th.check(NaN != NaN);
        th.check(!(NaN > NaN));
    }

    private void nanTestDouble() {
        double zero = 0;
        double NaN = zero / zero;

        th.check(!(NaN < NaN));
        th.check(NaN != NaN);
        th.check(!(NaN > NaN));
    }
}
