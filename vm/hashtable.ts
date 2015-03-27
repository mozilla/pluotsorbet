/**
 * Port of Java java.util.Hashtable.
 */
module J2ME {
  import assert = Debug.assert;
  export class Uint8HashtableEntry {
    hash: number = 0;
    value: any = null;
    next: Uint8HashtableEntry = null;
    key: Uint8Array = null;
  }

  function arrayEqual(a: Uint8Array, b: Uint8Array): boolean {
    if (a === b) {
      return true;
    }
    if (a.length !== b.length) {
      return false;
    }
    for (var i = 0; i < a.length; i++) {
      if (a[i] !== b[i]) {
        return false;
      }
    }
    return true;
  }

  function arrayRangeEqual(a: Uint8Array, offset: number, length: number, b: Uint8Array): boolean {
    if (length !== b.length) {
      return false;
    }
    var j = offset;
    for (var i = 0; i < length; j++, i++) {
      if (a[j] !== b[i]) {
        return false;
      }
    }
    return true;
  }

  function arrayHash(array: Uint8Array): number {
    return arrayRangeHash(array, 0, array.length);
  }

  function arrayRangeHash(array: Uint8Array, offset: number, length: number): number {
    var h = 0;
    var l = offset + length;
    for (var i = offset; i < l; i++) {
      h = (Math.imul(31, h)|0 + array[i]|0);
    }
    return h;
  }

  function nullArray(capacity) {
    var array = new Array(capacity);
    for (var i = 0; i < capacity; i++) {
      array[i] = null;
    }
    return array;
  }

  export class Uint8Hashtable {
    table: Uint8HashtableEntry []
    count: number = 0;
    private threshold: number;
    private static loadFactorPercent = 75;

    constructor(initialCapacity: number) {
      release || assert(initialCapacity >= 0);
      if (initialCapacity == 0) {
        initialCapacity = 1;
      }
      this.table = nullArray(initialCapacity);
      this.threshold = ((initialCapacity * Uint8Hashtable.loadFactorPercent) / 100) | 0;
    }

    contains(key: Uint8Array) {
      var table = this.table;
      var hash = arrayHash(key);
      var index = (hash & 0x7FFFFFFF) % table.length;
      for (var e = table[index]; e !== null; e = e.next) {
        if ((e.hash === hash) && arrayEqual(e.key, key)) {
          return true;
        }
      }
      return false;
    }

    getByRange(key: Uint8Array, offset: number, length: number) {
      var table = this.table;
      var hash = arrayRangeHash(key, offset, length);
      var index = (hash & 0x7FFFFFFF) % table.length;
      for (var e = table[index]; e !== null; e = e.next) {
        if ((e.hash === hash) && arrayRangeEqual(key, offset, length, e.key)) {
          return e.value;
        }
      }
      return null;
    }

    get(key: Uint8Array) {
      var table = this.table;
      var hash = arrayHash(key);
      var index = (hash & 0x7FFFFFFF) % table.length;
      for (var e = table[index]; e !== null; e = e.next) {
        if ((e.hash === hash) && arrayEqual(e.key, key)) {
          return e.value;
        }
      }
      return null;
    }

    put(key: Uint8Array, value: any) {
      // Make sure the value is not null
      release || assert(value !== null);

      // Makes sure the key is not already in the hashtable.
      var table = this.table;
      var hash = arrayHash(key);
      var index = (hash & 0x7FFFFFFF) % table.length;
      for (var e = table[index]; e !== null; e = e.next) {
        if ((e.hash === hash) && arrayEqual(e.key, key)) {
          var old = e.value;
          e.value = value;
          return old;
        }
      }

      if (this.count >= this.threshold) {
        // Rehash the table if the threshold is exceeded
        this.rehash();
        return this.put(key, value);
      }

      // Creates the new entry.
      var e = new Uint8HashtableEntry();
      e.hash = hash;
      e.key = key;
      e.value = value;
      e.next = table[index];
      table[index] = e;
      this.count++;
      return null;
    }

    rehash() {
      var oldCapacity = this.table.length;
      var oldTable = this.table;

      var newCapacity = oldCapacity * 2 + 1;
      var newTable = nullArray(newCapacity);

      this.threshold = ((newCapacity * Uint8Hashtable.loadFactorPercent) / 100) | 0;
      this.table = newTable;

      for (var i = oldCapacity; i-- > 0;) {
        for (var old = oldTable[i]; old !== null;) {
          var e = old;
          old = old.next;

          var index = (e.hash & 0x7FFFFFFF) % newCapacity;
          e.next = newTable[index];
          newTable[index] = e;
        }
      }
    }
  }
}