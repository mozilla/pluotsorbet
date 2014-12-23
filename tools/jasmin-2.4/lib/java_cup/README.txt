
This directory contains the Java CUP v0.9d release in source form.  You should
find the following files and subdirectories in this release:

  README            This file.
  java_cup          A subdirectory containing Java CUP and runtime sources.
  javacup.logo.gif  A logo image used by the manual.
  manual.html       A user's manual in HTML format.
  simple_calc       A subdirectory containing a small demo and test application.
  INSTALL           A shell script to install and test the system

To install the release, copy the contents of this directory (if you haven't
done so already) into a "classes" directory accessible to the java interpreter
(i.e., a directory that is listed in the colon separated list of directories
in your CLASSPATH environment variable).

Once files have been copied to an appropriate location, you should be able to
both compile and test the system by executing the INSTALL shell script
(sorry, but non Unix users are still on their own for this release
-- zip archive and directions for Win95 users coming soon).  
Again, be sure that you have placed these sources in a directory listed in 
your CLASSPATH environment variable (or changed your CLASSPATH to include 
this directory).  The INSTALL script will warn you if this is not the case. 

A manual page that explains the operation and use of the system can be found
in manual.html and from the Java CUP home page mentioned below.

This is an alpha-test release of the Java CUP system and is designed to run 
under the JDK-beta2 Java compiler.  Bug reports regarding the installation 
process or the system as a whole can be sent to java-cup@cc.gatech.edu.

The Java CUP home page where the latest information regarding Java CUP can be 
found (e.g., new releases) is:

  http://www.cc.gatech.edu/gvu/people/Faculty/hudson/java_cup/home.html

Enjoy,

Scott Hudson
Graphics, Visualization, and Usability Center 
Georgia Institute of Technology
	 
Last updated: 1/7/96 [SEH]

