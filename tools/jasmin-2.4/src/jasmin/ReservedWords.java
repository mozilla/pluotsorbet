/* --- Copyright Jonathan Meyer 1996. All rights reserved. -----------------
 > File:        jasmin/src/jasmin/ReservedWords.java
 > Purpose:     Reserved words for Jasmin
 > Author:      Jonathan Meyer, 10 July 1996
 */

package jasmin;

import java.util.Hashtable;
import java_cup.runtime.*;

abstract class ReservedWords {
    private static Hashtable reserved_words;

    public static token get(String name) {
    	return (token)reserved_words.get(name);
    }

    public static boolean contains(String name) {
    	return reserved_words.get(name) != null;
    }

    //
    // scanner initializer - sets up reserved_words table
    //
    static {
        reserved_words = new Hashtable();

        // Jasmin directives
        reserved_words.put(".annotation", new token(sym.DANNOTATION));
        reserved_words.put(".attribute", new token(sym.DATTRIBUTE));
        reserved_words.put(".bytecode", new token(sym.DBYTECODE));
        reserved_words.put(".catch", new token(sym.DCATCH));
        reserved_words.put(".class", new token(sym.DCLASS));
        reserved_words.put(".deprecated", new token(sym.DDEPRECATED));
        reserved_words.put(".end", new token(sym.DEND));
        reserved_words.put(".field", new token(sym.DFIELD));
        reserved_words.put(".implements", new token(sym.DIMPLEMENTS));
        reserved_words.put(".inner", new token(sym.DINNER));
        reserved_words.put(".interface", new token(sym.DINTERFACE));
        reserved_words.put(".limit", new token(sym.DLIMIT));
        reserved_words.put(".line", new token(sym.DLINE));
        reserved_words.put(".method", new token(sym.DMETHOD));
        reserved_words.put(".set", new token(sym.DSET));
        reserved_words.put(".source", new token(sym.DSOURCE));
        reserved_words.put(".super", new token(sym.DSUPER));
        reserved_words.put(".throws", new token(sym.DTHROWS));
        reserved_words.put(".var", new token(sym.DVAR));
        reserved_words.put(".debug", new token(sym.DDEBUG));
        reserved_words.put(".enclosing", new token(sym.DENCLOSING));
        reserved_words.put(".signature", new token(sym.DSIGNATURE));
        reserved_words.put(".stack", new token(sym.DSTACK));

        // reserved_words used in Jasmin directives
        reserved_words.put("field", new token(sym.FIELD));
        reserved_words.put("from", new token(sym.FROM));
        reserved_words.put("method", new token(sym.METHOD));
        reserved_words.put("to", new token(sym.TO));
        reserved_words.put("is", new token(sym.IS));
        reserved_words.put("using", new token(sym.USING));
        reserved_words.put("signature", new token(sym.SIGNATURE));
        reserved_words.put("stack", new token(sym.STACK));
        reserved_words.put("offset", new token(sym.OFFSET));
        reserved_words.put("locals", new token(sym.LOCALS));
        reserved_words.put("use", new token(sym.USE));
        reserved_words.put("inner", new token(sym.INNER));
        reserved_words.put("outer", new token(sym.OUTER));
        reserved_words.put("class", new token(sym.CLASS));
        reserved_words.put("visible", new token(sym.VISIBLE));
        reserved_words.put("invisible", new token(sym.INVISIBLE));
        reserved_words.put("visibleparam", new token(sym.VISIBLEPARAM));
        reserved_words.put("invisibleparam", new token(sym.INVISIBLEPARAM));

        // Special-case instructions
        reserved_words.put("tableswitch", new token(sym.TABLESWITCH));
        reserved_words.put("lookupswitch", new token(sym.LOOKUPSWITCH));
        reserved_words.put("default", new token(sym.DEFAULT));

        // Access flags
        reserved_words.put("public", new token(sym.PUBLIC));
        reserved_words.put("private", new token(sym.PRIVATE));
        reserved_words.put("protected", new token(sym.PROTECTED));
        reserved_words.put("static", new token(sym.STATIC));
        reserved_words.put("final", new token(sym.FINAL));
        reserved_words.put("synchronized", new token(sym.SYNCHRONIZED));
        reserved_words.put("volatile", new token(sym.VOLATILE));
        reserved_words.put("transient", new token(sym.TRANSIENT));
        reserved_words.put("native", new token(sym.NATIVE));
        reserved_words.put("interface", new token(sym.INTERFACE));
        reserved_words.put("abstract", new token(sym.ABSTRACT));

        reserved_words.put("annotation", new token(sym.ANNOTATION));
        reserved_words.put("enum", new token(sym.ENUM));
        reserved_words.put("bridge", new token(sym.BRIDGE));
        reserved_words.put("varargs", new token(sym.VARARGS));
        reserved_words.put("fpstrict", new token(sym.STRICT));
        reserved_words.put("synthetic", new token(sym.SYNTHETIC));
    }
}

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, Aug 10 2006
    Added 'Wide' prefix support to IincOperand
*/
