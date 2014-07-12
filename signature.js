/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var Signature = {};

Signature.TYPE = {
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

Signature.parse = (function () {
    function _parse(part) {
        var res = [];
        if (part != '') {
            var isArray = false;
            var pos = 0;
            while (pos < part.length) {
                switch(part[pos]) {
                    case TYPE.boolean:
                    case TYPE.byte:
                    case TYPE.char:
                    case TYPE.double:
                    case TYPE.float:
                    case TYPE.int:
                    case TYPE.long:
                    case TYPE.short:
                        res.push( { type: TYPE.toString(part[pos]), isArray: isArray } );
                        isArray = false;
                        break;
                    case TYPE.object:
                        var className = '';
                        while (part[++pos] !== ';') {
                            className += part[pos];
                        }
                        res.push( { type: "object", isArray: isArray, className: className } );
                        isArray = false;
                        break;
                    case TYPE.array:
                        isArray = true;
                        break;
                }
                pos++;
            }
        }
        return res;
    }

    return function(s) {
        var IN = s.split(')')[0].substr(1);
        var OUT = s.split(')')[1];
        return {
            IN: _parse(IN),
            OUT: _parse(OUT),
            toString: new Function(util.format("return \"%s\"", s))
        };
    };
})();
