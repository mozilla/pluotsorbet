// IndexedDB-getAll-shim v1.1 - https://github.com/jdscheff/IndexedDB-getAll-shim
//
// Copyright (c) 2012 Jeremy Scheff
//
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

(function () {
    "use strict";

    var Event, IDBIndex, IDBObjectStore, IDBRequest, getAll;

    IDBObjectStore = window.IDBObjectStore || window.webkitIDBObjectStore || window.mozIDBObjectStore || window.msIDBObjectStore;
    IDBIndex = window.IDBIndex || window.webkitIDBIndex || window.mozIDBIndex || window.msIDBIndex;

    if (typeof IDBObjectStore === "undefined" || typeof IDBIndex === "undefined" || (IDBObjectStore.prototype.getAll !== undefined && IDBIndex.prototype.getAll !== undefined)) {
        return;
    }

    if (IDBObjectStore.prototype.mozGetAll !== undefined && IDBIndex.prototype.mozGetAll !== undefined) {
        IDBObjectStore.prototype.getAll = IDBObjectStore.prototype.mozGetAll;
        IDBIndex.prototype.getAll = IDBIndex.prototype.mozGetAll;
        return;
    }

    // https://github.com/axemclion/IndexedDBShim/blob/gh-pages/src/IDBRequest.js
    IDBRequest = function () {
        this.onsuccess = null;
        this.readyState = "pending";
    };
    // https://github.com/axemclion/IndexedDBShim/blob/gh-pages/src/Event.js
    Event = function (type, debug) {
        return {
            "type": type,
            debug: debug,
            bubbles: false,
            cancelable: false,
            eventPhase: 0,
            timeStamp: new Date()
        };
    };

    getAll = function (key) {
        var request, result;

        key = key !== undefined ? key : null;

        request = new IDBRequest();
        result = [];

        // this is either an IDBObjectStore or an IDBIndex, depending on the context.
        this.openCursor(key).onsuccess = function (event) {
            var cursor, e;

            cursor = event.target.result;
            if (cursor) {
                result.push(cursor.value);
                cursor.continue();
            } else {
                if (typeof request.onsuccess === "function") {
                    e = new Event("success");
                    e.target = {
                        readyState: "done",
                        result: result
                    };
                    request.result = result;
                    request.onsuccess(e);
                }
            }
        };

        return request;
    };

    IDBObjectStore.prototype.getAll = getAll;
    IDBIndex.prototype.getAll = getAll;
}());
