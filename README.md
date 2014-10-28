# j2me.js [![Build Status](https://travis-ci.org/andreasgal/j2me.js.svg)](https://travis-ci.org/andreasgal/j2me.js)

j2me.js is a J2ME virtual machine in JavaScript.

A few similar projects exist. The primary objective here is to keep this very simple and small and to leverage the phoneME JDK. In particular we are trying to implement as little as possible in JavaScript, re-using as much of the phoneME infrastructure and existing Java code as we can.

#### Run j2me.js in the SpiderMonkey shell

1. Download the SpiderMonkey shell (https://developer.mozilla.org/en-US/docs/Mozilla/Projects/SpiderMonkey/Introduction_to_the_JavaScript_shell)
2. Execute the jsshell.js file as follows: *js jsshell.js package.ClassName*
