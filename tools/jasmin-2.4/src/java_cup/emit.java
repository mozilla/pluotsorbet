
package java_cup;

import java.io.PrintStream;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Date;

/**
 * This class handles emitting generated code for the resulting parser.
 * The various parse tables must be constructed, etc. before calling any
 * routines in this class.<p>
 *
 * Three classes are produced by this code:
 *   <dl>
 *   <dt> symbol constant class
 *   <dd>   this contains constant declarations for each terminal (and
 *          optionally each non-terminal).
 *   <dt> action class
 *   <dd>   this non-public class contains code to invoke all the user actions
 *          that were embedded in the parser specification.
 *   <dt> parser class
 *   <dd>   the specialized parser class consisting primarily of some user
 *          supplied general and initialization code, and the parse tables.
 *   </dl><p>
 *
 *  Three parse tables are created as part of the parser class:
 *    <dl>
 *    <dt> production table
 *    <dd>   lists the LHS non terminal number, and the length of the RHS of
 *           each production.
 *    <dt> action table
 *    <dd>   for each state of the parse machine, gives the action to be taken
 *           (shift, reduce, or error) under each lookahead symbol.<br>
 *    <dt> reduce-goto table
 *    <dd>   when a reduce on a given production is taken, the parse stack is
 *           popped back a number of elements corresponding to the RHS of the
 *           production.  This reveals a prior state, which we transition out
 *           of under the LHS non terminal symbol for the production (as if we
 *           had seen the LHS symbol rather than all the symbols matching the
 *           RHS).  This table is indexed by non terminal numbers and indicates
 *           how to make these transitions.
 *    </dl><p>
 *
 * In addition to the method interface, this class maintains a series of
 * public global variables and flags indicating how misc. parts of the code
 * and other output is to be produced, and counting things such as number of
 * conflicts detected (see the source code and public variables below for
 * more details).<p>
 *
 * This class is "static" (contains only static data and methods).<p>
 *
 * @see java_cup.main
 * @version last update: 11/25/95
 * @author Scott Hudson
 */

/* Major externally callable routines here include:
     symbols               - emit the symbol constant class
     parser                - emit the parser class

   In addition the following major internal routines are provided:
     emit_package          - emit a package declaration
     emit_action_code      - emit the class containing the user's actions
     emit_production_table - emit declaration and init for the production table
     do_action_table       - emit declaration and init for the action table
     do_reduce_table       - emit declaration and init for the reduce-goto table

   Finally, this class uses a number of public instance variables to communicate
   optional parameters and flags used to control how code is generated,
   as well as to report counts of various things (such as number of conflicts
   detected).  These include:

   prefix                  - a prefix string used to prefix names that would
			     otherwise "pollute" someone else's name space.
   package_name            - name of the package emitted code is placed in
			     (or null for an unnamed package.
   symbol_const_class_name - name of the class containing symbol constants.
   parser_class_name       - name of the class for the resulting parser.
   action_code             - user supplied declarations and other code to be
			     placed in action class.
   parser_code             - user supplied declarations and other code to be
			     placed in parser class.
   init_code               - user supplied code to be executed as the parser
			     is being initialized.
   scan_code               - user supplied code to get the next token.
   start_production        - the start production for the grammar.
   import_list             - list of imports for use with action class.
   num_conflicts           - number of conflicts detected.
   nowarn                  - true if we are not to issue warning messages.
   not_reduced             - count of number of productions that never reduce.
   unused_term             - count of unused terminal symbols.
   unused_non_term         - count of unused non terminal symbols.
   *_time                  - a series of symbols indicating how long various
			     sub-parts of code generation took (used to produce
			     optional time reports in main).
*/

public class emit {

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Only constructor is private so no instances can be created. */
  private emit() { }

  /*-----------------------------------------------------------*/
  /*--- Static (Class) Variables ------------------------------*/
  /*-----------------------------------------------------------*/

  public static String input_file_name;

  /** The prefix placed on names that pollute someone else's name space. */
  public static String prefix = "CUP$";

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Package that the resulting code goes into (null is used for unnamed). */
  public static String package_name = null;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Name of the generated class for symbol constants. */
  public static String symbol_const_class_name = "sym";


  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Name of the generated parser class. */
  public static String parser_class_name = "parser";

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** User declarations for direct inclusion in user action class. */
  public static String action_code = null;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** User declarations for direct inclusion in parser class. */
  public static String parser_code = null;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** User code for user_init() which is called during parser initialization. */
  public static String init_code = null;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** User code for scan() which is called to get the next token. */
  public static String scan_code = null;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The start production of the grammar. */
  public static production start_production = null;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** List of imports (Strings containing class names) to go with actions. */
  public static Stack import_list = new Stack();

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Number of conflict found while building tables. */
  public static int num_conflicts = 0;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Do we skip warnings? */
  public static boolean nowarn = false;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Count of the number on non-reduced productions found. */
  public static int not_reduced = 0;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Count of unused terminals. */
  public static int unused_term = 0;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Count of unused non terminals. */
  public static int unused_non_term = 0;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /* Timing values used to produce timing report in main.*/

  /** Time to produce symbol constant class. */
  public static long symbols_time          = 0;

  /** Time to produce parser class. */
  public static long parser_time           = 0;

  /** Time to produce action code class. */
  public static long action_code_time      = 0;

  /** Time to produce the production table. */
  public static long production_table_time = 0;

  /** Time to produce the action table. */
  public static long action_table_time     = 0;

  /** Time to produce the reduce-goto table. */
  public static long goto_table_time       = 0;

  /** Do we produce calls debug_gammar in generated parser? */
  public static String debug_grammar = null;

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Build a string with the standard prefix.
   * @param str string to prefix.
   */
  protected static String pre(String str) {return prefix + str;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit a package spec if the user wants one.
   * @param out stream to produce output on.
   */
  protected static void emit_package(PrintStream out)
    {
      /* generate a package spec if we have a name for one */
      if (package_name != null)
	out.println("package " + package_name + ";\n");

    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit code for the symbol constant class, optionally including non terms,
   *  if they have been requested.
   * @param out            stream to produce output on.
   * @param emit_non_terms do we emit constants for non terminals?
   */
  public static void symbols(PrintStream out, boolean emit_non_terms)
    {
      terminal term;
      non_terminal nt;

      long start_time = System.currentTimeMillis();

      /* top of file */
      out.println();
      out.println("//----------------------------------------------------");
      out.println("// The following code was generated by " +
							   version.title_str);
      out.println("// " + new Date());
      out.println("//----------------------------------------------------");
      out.println();
      emit_package(out);

      /* class header */
      out.println(
	    "/** JavaCup generated class containing symbol constants. */");
      out.println("public class " + symbol_const_class_name + " {");

      out.println("  /* terminals */");

      /* walk over the terminals */              /* later might sort these */
      for (Enumeration e = terminal.all(); e.hasMoreElements(); )
	{
	  term = (terminal)e.nextElement();

	  /* output a constant decl for the terminal */
	  out.println("  static final int " + term.name() + " = " +
		      term.index() + ";");
	}

      /* do the non terminals if they want them (parser doesn't need them) */
      if (emit_non_terms)
	{
          out.println("\n  /* non terminals */");

          /* walk over the non terminals */       /* later might sort these */
          for (Enumeration e = non_terminal.all(); e.hasMoreElements(); )
	    {
	      nt = (non_terminal)e.nextElement();

	      /* output a constant decl for the terminal */
	      out.println("  static final int " + nt.name() + " = " +
		          nt.index() + ";");
	    }
	}

      /* end of class */
      out.println("};\n");

      symbols_time = System.currentTimeMillis() - start_time;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit code for the non-public class holding the actual action code.
   * @param out        stream to produce output on.
   * @param start_prod the start production of the grammar.
   */
  protected static void emit_action_code(PrintStream out, production start_prod)
    throws internal_error
    {
      production prod;

      long start_time = System.currentTimeMillis();

      /* class header */
      out.println();
      out.println(
       "/** JavaCup generated class to encapsulate user supplied action code.*/"
      );
      out.println("class " +  pre("actions") + " {");

      /* user supplied code */
      if (action_code != null)
	{
	  out.println();
          out.println(action_code);
	}

      /* constructor */
      out.println();
      out.println("  /** Constructor */");
      out.println("  " + pre("actions") + "() { }");

      /* action method head */
      out.println();
      out.println("  /** Method with the actual generated action code. */");
      out.println("  public final java_cup.runtime.symbol " +
		     pre("do_action") + "(");
      out.println("    int                        " + pre("act_num,"));
      out.println("    java_cup.runtime.lr_parser " + pre("parser,"));
      out.println("    java.util.Stack            " + pre("stack,"));
      out.println("    int                        " + pre("top)"));
      out.println("    throws java.lang.Exception");
      out.println("    {");

      /* declaration of result symbol */
      out.println("      /* object for return from actions */");
      out.println("      java_cup.runtime.symbol " + pre("result") + ";");
      out.println();

      /* switch top */
      out.println("      /* select the action based on the action number */");
      out.println("      switch (" + pre("act_num") + ")");
      out.println("        {");

      /* emit action code for each production as a separate case */
      for (Enumeration p = production.all(); p.hasMoreElements(); )
	{
	  prod = (production)p.nextElement();

	  /* case label */
          out.println("          /*. . . . . . . . . . . . . . . . . . . .*/");
          out.println("          case " + prod.index() + ": // " +
					  prod.to_simple_string());

	  /* give them their own block to work in */
	  out.println("            {");

          /* user supplied code for debug_grammar() */
          if (debug_grammar != null)
            out.println("             " +debug_grammar+ "(\"" +
                        prod.to_simple_string() + "\");");

	  /* create the result symbol */
	  out.println("              " + pre("result") + " = new " +
	    prod.lhs().the_symbol().stack_type() + "(/*" +
	    prod.lhs().the_symbol().name() + "*/" +
	    prod.lhs().the_symbol().index() + ");");

	  /* if there is an action string, emit it */
	  if (prod.action() != null && prod.action().code_string() != null &&
	      !prod.action().equals(""))
	    out.println("              " + prod.action().code_string());

	  /* end of their block */
	  out.println("            }");

	  /* if this was the start production, do action for accept */
	  if (prod == start_prod)
	    {
	      out.println("          /* ACCEPT */");
	      out.println("          " + pre("parser") + ".done_parsing();");
	    }

	  /* code to return lhs symbol */
	  out.println("          return " + pre("result") + ";");
	  out.println();
	}

      /* end of switch */
      out.println("          /* . . . . . .*/");
      out.println("          default:");
      out.println("            throw new Exception(");
      out.println("               \"Invalid action number found in " +
				  "internal parse table\");");
      out.println();
      out.println("        }");

      /* end of method */
      out.println("    }");

      /* end of class */
      out.println("};\n");

      action_code_time = System.currentTimeMillis() - start_time;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit the production table.
   * @param out stream to produce output on.
   */
  protected static void emit_production_table(PrintStream out)
    {
      production all_prods[];
      production prod;

      long start_time = System.currentTimeMillis();

      /* do the top of the table */
      out.println();
      out.println("  /** production table */");
      out.println("  protected static final short _production_table[][] = {");

      /* collect up the productions in order */
      all_prods = new production[production.number()];
      for (Enumeration p = production.all(); p.hasMoreElements(); )
	{
	  prod = (production)p.nextElement();
	  all_prods[prod.index()] = prod;
	}

      /* do one entry per production */
      out.print("    ");
      for (int i = 0; i<production.number(); i++)
	{
	  prod = all_prods[i];

	  /* make the table entry */
	  out.print("    {");
	  out.print(/* lhs symbol # */ prod.lhs().the_symbol().index() + ", ");
	  out.print(/* rhs size */     prod.rhs_length() + "}");

	  /* put in a comma if we aren't at the end */
	  if (i < production.number()-1) out.print(", ");

	  /* 5 entries per line */
	  if ((i+1) % 5 == 0)
	    {
	      out.println();
	      out.print("    ");
	    }
	}

      /* finish off the table initializer */
      out.println("  };");

      /* do the public accessor method */
      out.println();
      out.println("  /** access to production table */");
      out.println("  public short[][] production_table() " +
						 "{return _production_table;}");

      production_table_time = System.currentTimeMillis() - start_time;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit the action table.
   * @param out             stream to produce output on.
   * @param act_tab         the internal representation of the action table.
   * @param compact_reduces do we use the most frequent reduce as default?
   */
  protected static void do_action_table(
    PrintStream        out,
    parse_action_table act_tab,
    boolean            compact_reduces)
    throws internal_error
    {
      parse_action_row row;
      parse_action     act;
      int              red;

      long start_time = System.currentTimeMillis();

      out.println();
      out.println("  /** parse action table */");
      out.println("  protected static final short[][] _action_table = {");

      /* do each state (row) of the action table */
      for (int i = 0; i < act_tab.num_states(); i++)
	{
	  /* get the row */
	  row = act_tab.under_state[i];

	  /* determine the default for the row */
	  if (compact_reduces)
	    row.compute_default();
	  else
	    row.default_reduce = -1;

	  out.print("    /*" + i + "*/{");

	  /* do each column */
	  for (int j = 0; j < row.size(); j++)
	    {
	      /* extract the action from the table */
	      act = row.under_term[j];

	      /* skip error entries these are all defaulted out */
	      if (act.kind() != parse_action.ERROR)
		{
		  /* first put in the symbol index, then the actual entry */

		  /* shifts get positive entries of state number + 1 */
		  if (act.kind() == parse_action.SHIFT)
		    {
		    out.print(j + "," +
			    (((shift_action)act).shift_to().index() + 1) + ",");
		    }

		  /* reduce actions get negated entries of production# + 1 */
		  else if (act.kind() == parse_action.REDUCE)
		    {
		      /* if its the default entry let it get defaulted out */
		      red = ((reduce_action)act).reduce_with().index();
		      if (red != row.default_reduce)
		        out.print(j + "," + (-(red+1)) + ",");
		    }

		  /* shouldn't be anything else */
		  else
		    throw new internal_error("Unrecognized action code " +
		      act.kind() + " found in parse table");
		}
	    }

	  /* finish off the row with a default entry */
	  if (row.default_reduce != -1)
	    out.println("-1," + (-(row.default_reduce+1)) + "},");
	  else
	    out.println("-1,0},");
	}

      /* finish off the init of the table */
      out.println("  };");

      /* do the public accessor method */
      out.println();
      out.println("  /** access to parse action table */");
      out.println("  public short[][] action_table() {return _action_table;}");

      action_table_time = System.currentTimeMillis() - start_time;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit the reduce-goto table.
   * @param out     stream to produce output on.
   * @param red_tab the internal representation of the reduce-goto table.
   */
  protected static void do_reduce_table(
    PrintStream out,
    parse_reduce_table red_tab)
    {
      lalr_state       goto_st;
      parse_action     act;

      long start_time = System.currentTimeMillis();

      out.println();
      out.println("  /** reduce_goto table */");
      out.println("  protected static final short[][] _reduce_table = {");

      /* do each row of the reduce-goto table */
      for (int i=0; i<red_tab.num_states(); i++)
	{
	  out.print("    /*" + i + "*/{");

	  /* do each entry in the row */
	  for (int j=0; j<red_tab.under_state[i].size(); j++)
	    {
	      /* get the entry */
	      goto_st = red_tab.under_state[i].under_non_term[j];

	      /* if we have none, skip it */
	      if (goto_st != null)
		{
		  /* make entries for the index and the value */
		  out.print(j + "," + goto_st.index() + ",");
		}
	    }

	  /* end row with default value */
	  out.println("-1,-1},");
	}

      /* finish off the init of the table */
      out.println("  };");

      /* do the public accessor method */
      out.println();
      out.println("  /** access to reduce_goto table */");
      out.println("  public short[][] reduce_table() {return _reduce_table;}");
      out.println();

      goto_table_time = System.currentTimeMillis() - start_time;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Emit the parser subclass with embedded tables.
   * @param out             stream to produce output on.
   * @param action_table    internal representation of the action table.
   * @param reduce_table    internal representation of the reduce-goto table.
   * @param start_st        start state of the parse machine.
   * @param start_prod      start production of the grammar.
   * @param compact_reduces do we use most frequent reduce as default?
   */
  public static void parser(
    PrintStream        out,
    parse_action_table action_table,
    parse_reduce_table reduce_table,
    int                start_st,
    production         start_prod,
    boolean            compact_reduces)
    throws internal_error
    {
      long start_time = System.currentTimeMillis();

      /* top of file */
      out.println();
      out.println("//----------------------------------------------------");
      out.println("// The following code was generated by " +
							version.title_str);
      out.println("// " + new Date());
      out.println("//----------------------------------------------------");
      out.println();
      emit_package(out);

      /* user supplied imports */
      for (int i = 0; i < import_list.size(); i++)
	out.println("import " + import_list.elementAt(i) + ";");

      /* class header */
      out.println();
      out.println("public class " + parser_class_name +
		  " extends java_cup.runtime.lr_parser {");

      /* constructor */
      out.println();
      out.println("  /** constructor */");
      out.println("  public " + parser_class_name + "() {super();}");

      /* emit the various tables */
      emit_production_table(out);
      do_action_table(out, action_table, compact_reduces);
      do_reduce_table(out, reduce_table);

      /* instance of the action encapsulation class */
      out.println("  /** instance of action encapsulation class */");
      out.println("  protected " + pre("actions") + " action_obj;");
      out.println();

      /* action object initializer */
      out.println("  /** action encapsulation object initializer */");
      out.println("  protected void init_actions()");
      out.println("    {");
      out.println("      action_obj = new " + pre("actions") + "();");
      out.println("    }");
      out.println();

      /* access to action code */
      out.println("  /** invoke a user supplied parse action */");
      out.println("  public java_cup.runtime.symbol do_action(");
      out.println("    int                        act_num,");
      out.println("    java_cup.runtime.lr_parser parser,");
      out.println("    java.util.Stack            stack,");
      out.println("    int                        top)");
      out.println("    throws java.lang.Exception");
      out.println("  {");
      out.println("    /* call code in generated class */");
      out.println("    return action_obj." + pre("do_action(") +
                  "act_num, parser, stack, top);");
      out.println("  }");
      out.println("");


      /* method to tell the parser about the start state */
      out.println("  /** start state */");
      out.println("  public int start_state() {return " + start_st + ";}");

      /* method to indicate start production */
      out.println("  /** start production */");
      out.println("  public int start_production() {return " +
		     start_production.index() + ";}");
      out.println();

      /* methods to indicate EOF and error symbol indexes */
      out.println("  /** EOF symbol index */");
      out.println("  public int EOF_sym() {return " + terminal.EOF.index() +
					  ";}");
      out.println();
      out.println("  /** error symbol index */");
      out.println("  public int error_sym() {return " + terminal.error.index() +
					  ";}");
      out.println();

      /* user supplied code for user_init() */
      if (init_code != null)
	{
          out.println();
	  out.println("  /** user initialization */");
	  out.println("  public void user_init() throws java.lang.Exception");
	  out.println("    {");
	  out.println(init_code);
	  out.println("    }");
	}

      /* user supplied code for scan */
      if (scan_code != null)
	{
          out.println();
	  out.println("  /** scan to get the next token */");
	  out.println("  public java_cup.runtime.token scan()");
	  out.println("    throws java.lang.Exception");
	  out.println("    {");
	  out.println(scan_code);
	  out.println("    }");
	}

      /* user supplied code */
      if (parser_code != null)
	{
	  out.println();
          out.println(parser_code);
	}

      /* end of class */
      out.println("};");

      /* put out the action code class */
      emit_action_code(out, start_prod);

      parser_time = System.currentTimeMillis() - start_time;
    }

    /*-----------------------------------------------------------*/
};
