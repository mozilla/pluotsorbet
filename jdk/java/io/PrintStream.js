/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var PrintStream = module.exports = function() {
    if (this instanceof PrintStream) {        
    } else {
        return new PrintStream();
    }
};

PrintStream.getClassName = function() {
    return "java/io/PrintStream";
}
 
PrintStream.prototype.print = function() {
    util.print.apply(null, arguments);
};

PrintStream.prototype.println = function() {
    util.print.apply(null, arguments);
    util.print("\n");
};

PrintStream.prototype.format = function(fmt, args) {
    util.print(util.format.apply(null, [fmt].concat(args)));
}
