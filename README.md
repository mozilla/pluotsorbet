# j2me.js

j2me.js is a small J2ME virtual machine in JavaScript.

A few similar projects exist. My primary objective is to keep this very simple and small and to leverage the CDLC JDK. In particular I am trying to implement as little as possible in JavaScript, re-using as much of the CDLC infrastructure and existing Java code as I can.

The VM core itself is based on node-jvm, which has a very nice classfile parser but is quite buggy and incomplete. I had to rewrite most parts of it, and except for the classfile parser not much code survived. That having said, node-jvm was very inspirational and an excellent starting point to define structure.

The VM is able to execute a good amount of tests from Mauve and js2me.js and work has started to support MIDP2.0 MIDlets. The native support for MIDlets is in midp.js and we are close to being able to launch a MIDlet.

To try this out at home run "make" in java/ and tests/ and then open "index.html" in Firefox.
