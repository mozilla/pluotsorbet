///* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2 -*- */
///* vim: set shiftwidth=2 tabstop=2 autoindent cindent expandtab: */
'use strict';

Native["java/lang/String.intern.()Ljava/lang/String;"] = function() {
    var string = util.fromJavaString(this);

    var internedString = J2ME.internedStrings.get(string);

    if (internedString) {
        return internedString;
    } else {
        J2ME.internedStrings.set(string, this);
        return this;
    }
};