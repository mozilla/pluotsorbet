# j2me.js

j2me.js is a small J2ME virtual machine in JavaScript.

A few similar projects exist. My primary objective is to keep this very simple and small and to leverage the CDLC JDK. In particular I am trying to implement as little as possible in Java, re-using as much of the CDLC infrastructure as I can.

The VM core itself is based on node-jvm, which has a very nice classfile parser but is quite buggy and incomplete.

A few other projects like js2me.js exist, but they tend to implement the class library in JavaScript which is a bad idea because its a lot of work, and really hard to get right from a compatibility perspective (Java has a lot of quirks and poorly documented edge cases).

The VM is able to execute a "Hello World" Java program, which if you know anything about the Java bootstrap sequence, is quite complex.

To try this at home run the Makefile in java/ to generate the the class library jar (which is decompressed on the fly in JavaScript, using zip.js) and then load index.html in the browser (full disclosure: I only tried this in Firefox).

I will likely add some test automation in node.js next.
