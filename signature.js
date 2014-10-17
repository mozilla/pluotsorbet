/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Signature = (function() {
    var TYPE = {
        boolean:    'Z',
        byte:       'B',
        char:       'C',
        double:     'D',
        float:      'F',
        int:        'I',
        long:       'J',
        object:     'L',
        short:      'S',
        void:       'V',
        array:      '[',
        toString: function(s) {
            for(var type in this) {
                if (this[type] === s) {
                    return type;
                }
            }
            return null;
        }
    };

    function getINSlots(signature) {
        var slots = 0;

        var pos = 0;
        var isArray = false;
        while (pos < signature.length) {
            switch (signature[pos]) {
                case TYPE.long:
                case TYPE.double:
                    slots += isArray ? 1 : 2;
                    break;

                case TYPE.boolean:
                case TYPE.byte:
                case TYPE.char:
                case TYPE.float:
                case TYPE.int:
                case TYPE.short:
                    slots++;
                    break;

                case TYPE.object:
                    pos = signature.indexOf(';', pos + 1);
                    slots++;
                    break;

                case ')':
                    return slots;
                case TYPE.array:
                    isArray = true;
                    pos++;
                    continue;
            }

            isArray = false;
            pos++;
        }

        return slots;
    }

    return {
        getINSlots: getINSlots,
    };
})();
