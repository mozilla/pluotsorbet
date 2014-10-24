/*
 * 	
 * 
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */
import java.io.*;


/**
 * A simple Java preprocessor.  It process the following kind of directives
 * in Java code:
 *    \/\* #ifdef <LABEL> \*\/
 *    \/\* #ifndef <LABEL> \*\/
 *    \/\* #else \*\/
 *    \/\* #endif \*\/
 * The directives have to be embedded in a comment.
 */
public class Jpp {

    FileInputStream input;
    FileOutputStream output;

    static String IFDEF = "#ifdef";
    static String IFNDEF = "#ifndef";
    static String ELSE = "#else";
    static String ENDIF = "#endif";

    String labels[];
    int nLabels = 0;
    boolean space = true;
    boolean trace = false;

    public static void main(String [] args) {
	if (args.length < 1)
	    prUsage();

	String input = null;
	String labels[] = new String[256];
	String destDir = null, destFile = null;
	int n = 0;
	boolean space = true;
	boolean trace = false;

	int i = 0;
	while (i < args.length) {
	    if (args[i].startsWith("-D")) {
		labels[n++] = args[i].substring(2);
	    } else if (args[i].equals("-d")) {
		if (i == args.length)
		    prUsage();
		i++;
		destDir = args[i];	
	    } else if (args[i].equals("-o")) {
		if (i == args.length)
		    prUsage();
		i++;
		destFile = args[i];	
	    } else if (args[i].equals("-nospace")) {
		space = false;
	    } else if (args[i].equals("-trace")) {
		trace = true;
	    } else {
		if (args[i].endsWith(".jpp"))
		    input = args[i];
	    }
	    i++;
	}

	if (input == null)
	    prUsage();

	if (destDir != null && destFile != null)
	    prUsage();

	Jpp jpp = new Jpp();
	jpp.process(input, labels, n, destDir, destFile, space, trace);
    }


    static void prUsage() {
	System.err.println(
	    "java Jpp <input>.jpp -D<label1> ... -D<labeln> " +
	    "[-d <output directory>] [-o <output file>] [-nospace] [-trace]");
	System.exit(-1);
    }


    void process(String inFile, String labels[], int nLabels, String destDir, 
	String destFile, boolean space, boolean trace) {

	this.labels = labels;
	this.nLabels = nLabels;
	this.space = space;
	this.trace = trace;

	String outFile;
	String separator = System.getProperty("file.separator");

	if (destFile != null) {
	    try {
		int idx = destFile.lastIndexOf(separator);
		if (idx != -1) {
		    String dir = destFile.substring(0, idx);
		    File f = new File(dir);
		    f.mkdirs();
		}
	    } catch (Exception e) {
		System.err.println(e);
	    }
	    outFile = destFile;
	} else if (destDir != null) {
	    int idx = inFile.lastIndexOf(separator);
	    if (idx != -1)
		outFile = inFile.substring(idx+1, inFile.length()-3) + "java";
	    else
		outFile = inFile.substring(0, inFile.length()-3) + "java";
	    outFile = destDir + separator + outFile;
	} else
	    outFile = inFile.substring(0, inFile.length()-3) + "java";

        (new File(outFile)).getParentFile().mkdirs();
        
	try {
	    input = new FileInputStream(inFile);
	} catch (IOException e) {
	    System.err.println("Cannot open input file: " + inFile + ":\n" + e);
            System.exit(-1);
	}

	try {
	    output = new FileOutputStream(outFile);
	} catch (IOException e) {
	    System.err.println("Cannot create output file: " + outFile +
                               ":\n" + e);
            System.exit(-1);
	}

	try {
	    processCode(true, false, null, 0);
	} catch (IOException e) {
	    System.err.println(e);
	    System.exit(-1);
	}
    }


    boolean processCode(boolean include, boolean endif,
		        String token, int level) 
	throws IOException {
	byte bary[] = new byte[1024];
	int b;
	int commentCount = 0;
	int idx = 0;
	int bidx[] = new int[1];
	byte cr[] = new byte[1];

	while (true) {
	    b = input.read();
	    if (b < 0)
		break;


	    if ((b == '/' || b == '*') && commentCount == 1) {
		bary[idx++] = (byte)b;
		// Found the comment block.

		bidx[0] = idx;
		cr[0] = -1;
		String tk = getToken(bary, bidx, cr);
		if (tk == null)
		    break;
		idx = bidx[0];

		if (tk.equals(IFDEF) || tk.equals(IFNDEF)) {
		    bidx[0] = idx;
		    cr[0] = -1;
		    String tk2 = getToken(bary, bidx, cr);
		    if (tk2 == null)
			break;
		    idx = bidx[0];

		    skipComments(b == '/', cr[0]);

		    boolean elseFound;
		    boolean incl;

		    if (trace)
			trace(tk, tk2, level+1);

		    if (include) {
			if ((tk.equals(IFDEF) && isDefined(tk2)) ||
			    (tk.equals(IFNDEF) && !isDefined(tk2))) {
			    incl = true;
			} else {
			    incl = false;
			}
			elseFound = processCode(incl, true, tk2, level+1);
			if (elseFound)
			    elseFound = processCode(!incl, true, tk2, level+1);
		    } else {
			elseFound = processCode(false, true, tk2, level+1);
			if (elseFound)
			    elseFound = processCode(false, true, tk2, level+1);
		    }
		    if (elseFound)
			throw new IOException("Unmatched #else");
		    idx = 0;
		} else if (tk.equals(ELSE)) {
		    if (trace)
			trace(tk, "for " + token, level);
		    if (!endif)
			throw new IOException("Unmatched #else");
		    skipComments(b == '/', cr[0]);
		    idx = 0;
		    return true;
		} else if (tk.equals(ENDIF)) {
		    if (trace)
			trace(tk, "for " + token, level);
		    if (!endif)
			throw new IOException("Unmatched #endif");
		    skipComments(b == '/', cr[0]);
		    idx = 0;
		    return false;
		} else {
		    // No match.
		    // Write the code if it should be included.
		    // Otherwise, only write the CR to maintain
		    // the line numbers.
		    if (include)
			output.write(bary, 0, idx);
		    else if (space)
			replaceCR(bary, 0, idx);
		    idx = 0;
		}
		commentCount = 0;
	    } else if (b == '/') {
		commentCount = 1;
		bary[idx++] = (byte)b;
	    } else { 
		if (commentCount != 0) {
		    // Write the code if it should be included.
		    // Otherwise, only write the CR to maintain
		    // the line numbers.
		    if (include)
			output.write(bary, 0, idx);
		    else if (space)
			replaceCR(bary, 0, idx);
		    idx = 0;
		    commentCount = 0;
		}
		if (include)
		    output.write(b);
		else if (space && (b == 10 || b == 13))
		    output.write(b);
	    }
	}

	if (endif)
	    throw new IOException("Missing #endif");

	return false;
    }


    void skipComments(boolean slash, byte cr) throws IOException {
	int b;

	// Handle double slash type of comments.
	if (slash) {
	    if (cr != -1) {
		if (space)
		    output.write(cr);
	        return;
	    }
	    while (true) {
		b = input.read();
		if (b < 0)
		    break;
		if (b == 10 || b == 13) {
		    if (space)
			output.write(b);
		    break;
		}
	    }
	    return;
	}

	int commentCount = 0;

	while (true) {
	    b = input.read();
	    if (b < 0)
		throw new IOException("Incomplete comments block");

	    if (b == '*') {
		commentCount = 1;
	    } else if (b == '/' && commentCount == 1) {
		commentCount = 0;
		break;
	    } else {
		if (space && (b == 10 || b == 13))
		    output.write(b);
		commentCount = 0;
	    }
	}
    }


    /**
     * Parse the characters to only write te CR's.
     */
    void replaceCR(byte b[], int off, int len) throws IOException {
	for (int i = 0; i < len; i++) {
	    if (space && (b[off+i] == 10 || b[off+i] == 13))
		output.write(b[off+i]);
	}
    }


    /**
     * Extract the next token.
     */
    byte token[] = new byte[1024];

    String getToken(byte bary[], int idx[], byte cr[]) throws IOException {
	int b;
	int i = 0;

	// Discard the leading spaces.
	do {
	    b = input.read();
	    if (b < 0)
		break;
	    bary[idx[0]++] = (byte)b;
	} while (b == ' ' || b == '\t' || b == '*' || b == '/' || 
		b == 10 || b == 13);

	while (true) {
	    if (b == ' ' || b == '\t' || b == 10 || b == 13) {
		if (cr != null && (b == 10 || b == 13))
		    cr[0] = (byte)b;
		break;
	    } else
		token[i++] = (byte)b;

	    b = input.read();
	    if (b < 0)
		break;
	    bary[idx[0]++] = (byte)b;
	}

	if (i == 0)
	    return null;

	return new String(token, 0, i);
    }


    /**
     * Check if the label is defined.
     */
    boolean isDefined(String label) {
	for (int i = 0; i < nLabels; i++) {
	    if (label.equals(labels[i]))
		return true;
	}
	if (System.getProperty(label) != null)
	    return true;
	return false;
    }

    void trace(String tk1, String tk2, int level) {
	for (; level > 0; level--)
	    System.out.print("  ");
	System.out.println(tk1 + " " + tk2);
    }
}

