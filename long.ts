// Copyright 2009 The Closure Library Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS-IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

module J2ME {
  /**
   * The closest floating-point representation to this long value.
   */
  export function longToNumber(l: number, h: number): number {
    return h * Constants.TWO_PWR_32_DBL + ((l >= 0) ? l : Constants.TWO_PWR_32_DBL + l);
  }

  export function returnLongValue(v: number) {
    if (isNaN(v) || !isFinite(v)) {
      tempReturn0 = 0;
      return 0;
    } else if (v <= -Constants.TWO_PWR_63_DBL) {
      // min value
      tempReturn0 = 0x80000000;
      return 0;
    } else if (v + 1 >= Constants.TWO_PWR_63_DBL) {
      // max value
      tempReturn0 = 0x7FFFFFFF;
      return 0xFFFFFFFF;
    } else if (v < 0) {
      var lowBits = returnLongValue(-v);
      var highBits = tempReturn0;
      if (lowBits === 0 && highBits === 0x80000000) {
        return lowBits;
      }
      // bitwise not
      lowBits = (~lowBits) | 0;
      highBits = (~highBits) | 0;

      // add one
      var a48 = highBits >>> 16;
      var a32 = highBits & 0xFFFF;
      var a16 = lowBits >>> 16;
      var a00 = lowBits & 0xFFFF;

      var b16 = 1 >>> 16;
      var b00 = 1 & 0xFFFF;

      var c48 = 0, c32 = 0, c16 = 0, c00 = 0;
      c00 += a00 + b00;
      c16 += c00 >>> 16;
      c00 &= 0xFFFF;
      c16 += a16 + b16;
      c32 += c16 >>> 16;
      c16 &= 0xFFFF;
      c32 += a32;
      c48 += c32 >>> 16;
      c32 &= 0xFFFF;
      c48 += a48;
      c48 &= 0xFFFF;

      tempReturn0 = (c48 << 16) | c32;
      return (c16 << 16) | c00;
    } else {
      tempReturn0 = (v / Constants.TWO_PWR_32_DBL) | 0;
      return (v % Constants.TWO_PWR_32_DBL) | 0;
    }
  }
}
