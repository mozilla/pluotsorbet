Jasmin README file                         1 March 1997, Jonathan Meyer
                                              Last updated October 2004

Introduction
------------
Welcome to Jasmin version 1.1.

Jasmin is a Java Assembler Interface. It takes ASCII descriptions for Java
classes, written in a simple assembler-like syntax, using the Java
Virtual Machine instruction set. It converts them into binary Java class 
files suitable for loading into a Java interpreter.

Jasmin was originally written as the companion to the 
book "Java Virtual Machine", published by O'Reilly, written by 
Jon Meyer and Troy Downing. (See http://www.ora.com/catalog/javavm/).
The book is now out of print. However, the Jasmin assembler retains its
usefulness as a utility, and continues its life as an OpenSource project.

Background
----------
Jasmin is today a little long in the tooth. It was originally coded in
1996 as a stop-gap until Sun released their own assembler. It has seen 
no major upgrades since 1997. By 2004 Sun still has not released an 
official assembler, so I decided to release Jasmin as a sourceforge
project. Hopefully this will inject some fresh life into the project...

Home Page
---------
Check out the Jasmin home page at:

    http://jasmin.sourceforge.net

Requirements
------------
Jasmin is written in Java, and should work with most Java 1.1 environments. 

To run Jasmin you need to have a Java 2 Runtime Environment available (e.g. JDK 1.4). 
This can be downloaded from "http://www.javasoft.com/j2se/".

Getting Started
---------------
The Jasmin distribution contains a jasmin.jar file holding the Jasmin assembler. 
To run Jasmin, execute the Jarfile, specifying any files to assemble 
as command-line parameters, e.g. to assemble the "HelloWorld.j" file in examples,
first use cd to change into the Jasmin directory:

    cd c:\jasmin-1.1                                   [Windows]
or
    cd ~/jasmin-1.1                                    [Unix]

Then, to run Jasmin, use:

    java -jar jasmin.jar examples\HelloWorld.j         [Windows]
or
    java -jar jasmin.jar examples/HelloWorld.j         [Unix/MacOsX]
	

After running Jasmin as above, it generates a compiled HelloWorld.class file
in the examples directory.

You can then run the HelloWorld program by doing:

    java examples.HelloWorld

Build Instructions
------------------
Jasmin uses Ant as its build mechanism. See build.xml for instructions on how
to build Jasmin. In brief, you need to:

1. Start a Terminal or Command window. 
2. Change (cd) into the Jasmin directory
3. Make sure that java, javac etc. are on your path
4. Run build.bat (Windows) or build.sh (Unix).

For example, on Windows, this might look something like:

    cd c:\jasmin-1.1                 # change to Jasmin directory
    build all
			
Or, for Unix, it might be like:

    cd ~/jasmin-1.1                  # change to Jasmin directory
    ./build.sh all

These scripts use the build.xml configuration file to specify build parameters.

Where Next
----------
After trying Jasmin out, have a look at the HelloWorld.j source in the examples directory, 
try compiling and running the other examples.

There is documentation for Jasmin in the doc directory. You should probably
start with the 'guide.html' document. 

Files
-----
The following files are included in this distribution:

    README.txt  - this file
    jasmin.jar  - executable Jar file containing Jasmin assembler
    examples/   - directory containing example files written for Jasmin
    src/        - the Java source code and for the jasmin package
    lib/        - Contains Java sources for the java_cup and jas packages
    docs/       - various documentation files.

Copyright
---------
Jasmin is Copyright (1997-2004) Jonathan Meyer, under the terms of
the GNU General Public License. See license-jasmin.txt for more.

Jasmin uses the JAS package which has its own copyright - see lib/jas/README.
[sbktech.org no longer seem to be in existence, but the code lives
on in this project].

Jasmin utilizes Scott E. Hudson's Java Cup package v0.9e, which is also in 
the lib directory. See http://www.cs.princeton.edu/~appel/modern/java/CUP/
