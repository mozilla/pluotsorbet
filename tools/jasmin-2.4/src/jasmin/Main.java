/* --- Copyright Jonathan Meyer 1996. All rights reserved. -----------------
 > File:        jasmin/src/jasmin/Main.java
 > Purpose:     Runs Jasmin, parsing any command line arguments
 > Author:      Jonathan Meyer, 10 July 1996
 */


package jasmin;

import java.io.*;
import jas.jasError;
import jas.StackMap;

/**
 * Main is the main entry point for Jasmin - it supplies the main()
 * method, as well as a few other useful odds and ends.
 */
public class Main {

    /**
     * The Jasmin version
     */
    public static final String version = "v2.4";
    public static final boolean DEBUG = false;

    /* Path for place generated files */
    private String dest_path = null;

    /* Codepage for input files */
    private String encoding = null;

    /* Autogenerate linenumbers */
    private boolean generate_linenum = false;

    /* Commandline syntax (help) */
    private static final String help1 =
"usage: jasmin [-d <outpath>] [-g] [-e <encoding>] <file> [<file> ...]\n";
    private static final String help2 =
"           where   -g - autogenerate linenumbers\n" +
"                   -e - codepage for inputfile encoding\n" +
"                   -d - path for generated classfiles\n" +
"                file  - sourcefile (wildcards are allowed)\n";
    private static final String help3 =
"   or: jasmin -version\n" +
"   or: jasmin -help";

    private static void unarg_option(String opt)
    {
        System.err.println("Invaid command line: option " +opt+ " required argument");
        System.exit(-1);
    }

    private static void duplicate_option(String opt)
    {
        System.err.println("Duplicate option " +opt+ " ignored");
    }

    /**
     * Called to assemble a single file.
     * @param fname is the name of the file containing the Jasmin source code.
     */
    public final void assemble(String fname)
    {
        File out_file = null;
        FileOutputStream outp = null;
        File file = new File(fname);
        ClassFile classFile = new ClassFile();
        String iocause = fname + ": file not found";

        try {
            BufferedReader inp;
            {
              FileInputStream fs = new FileInputStream(fname);
              InputStreamReader ir;
              if(encoding == null)
                ir = new InputStreamReader(fs);
              else
                ir = new InputStreamReader(fs, encoding);
              inp = new BufferedReader(ir);
            }
            classFile.readJasmin(inp, file.getName(), generate_linenum);
            inp.close();

            // if we got some errors, don't output a file - just return.
            if (classFile.errorCount() > 0) {
                System.err.println(fname + ": Found "
                                    + classFile.errorCount() + " errors");
                return;
            }

            String class_path[] = (ScannerUtils.splitClassField(
                                                classFile.getClassName()));
            String class_name = class_path[1];

            // determine where to place this class file
            String dest_dir = dest_path;
            if (class_path[0] != null) {
                String class_dir = ScannerUtils.convertChars(
                                           class_path[0], "./",
                                           File.separatorChar);
                if (dest_dir != null) {
                    dest_dir = dest_dir + File.separator + class_dir;
                } else {
                    dest_dir = class_dir;
                }
            }
            iocause = class_name + ".class: file can't be created";
            if (dest_dir == null) {
                out_file = new File(class_name + ".class");
            } else {
                out_file = new File(dest_dir, class_name + ".class");

                // check that dest_dir exists

                File dest = new File(dest_dir);
                if (!dest.exists()) {
                    dest.mkdirs();
                }

                if (!dest.isDirectory()) {
                    throw new IOException("Cannot create directory");
                }
            }

            outp = new FileOutputStream(out_file);
            classFile.write(outp);
            outp.close();
            outp = null; // as marker
            System.out.println("Generated: " + out_file.getPath());

        } catch (java.io.FileNotFoundException e) {
            System.err.println(iocause);
            System.exit(-1);
        } catch (jasError e) {
            classFile.report_error("JAS Error: " + e.getMessage(), e.numTag);
        } catch (Exception e) {
            if(DEBUG)
                e.printStackTrace();
            classFile.report_error(fname + ": exception - <" +
                              e.getClass().getName() + "> " + e.getMessage() +
                              ".");
        }
        if (classFile.errorCount() > 0) {
            System.err.println(fname + ": Found "
                               + classFile.errorCount() + " errors");
            if (outp != null) {
                try {
                  outp.close();
                  out_file.delete();
                } catch(Exception e) {}
            }
        }
    }

    public static void main(String args[])
    {
        new Main().run(args);
    }

    public final void run(String args[])
    {
        int i;

        String files[] = new String[args.length];
        int num_files = 0;

        if (args.length == 0) {
          System.err.println(help1 + help3);
          System.exit(-1);
        }

        for (i = 0; i < args.length; i++) {
            if (args[i].equals("-help") || args[i].equals("-?")) {
              System.err.println(help1 + help2 + help3);
              System.exit(0);
            }
            if (args[i].equals("-version")) {
                System.out.println("Jasmin version: " + version);
                if(DEBUG)
                    System.out.println("(compiled with DEBUG flag on)");
                System.exit(0);
            }
            if (args[i].equals("-g")) {
                generate_linenum = true;
            } else if (args[i].equals("-d")) {
                if (++i >= args.length) unarg_option("-d");
                if (dest_path != null) duplicate_option("-d");
                else dest_path = args[i];
            } else if (args[i].equals("-e")) {
                if (++i >= args.length) unarg_option("-e");
                if (encoding != null) duplicate_option("-e");
                else encoding = args[i];
            } else {
                files[num_files++] = args[i];
            }
        }

        for (i = 0; i < num_files; i++) {
            StackMap.reinit();
            assemble(files[i]);
        }
    }
};

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, May 07 2010, redesing for dynamic class creation
--- Iouri Kharon, Feb 17 2006, correct some IO diagnostics
--- Jonathan Meyer, Mar 1 1997 tidied error reporting, renamed Jasmin->ClassFile
--- Jonathan Meyer, Feb 8 1997 added the assemble() method
--- Jonathan Meyer, July 24 1996 added -version flag.
*/
