
package java_cup;
 
import java.util.Hashtable;
import java.util.Enumeration;

/** This class represents a production in the grammar.  It contains
 *  a LHS non terminal, and an array of RHS symbols.  As various 
 *  transformations are done on the RHS of the production, it may shrink.
 *  As a result a separate length is always maintained to indicate how much
 *  of the RHS array is still valid.<p>
 * 
 *  I addition to construction and manipulation operations, productions provide
 *  methods for factoring out actions (see  remove_embedded_actions()), for
 *  computing the nullability of the production (i.e., can it derive the empty
 *  string, see check_nullable()), and operations for computing its first
 *  set (i.e., the set of terminals that could appear at the beginning of some
 *  string derived from the production, see check_first_set()).
 * 
 * @see     java_cup.production_part
 * @see     java_cup.symbol_part
 * @see     java_cup.action_part
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */

public class production {

  /*-----------------------------------------------------------*/
  /*--- Constructor(s) ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Full constructor.  This constructor accepts a LHS non terminal,
   *  an array of RHS parts (including terminals, non terminals, and 
   *  actions), and a string for a final reduce action.   It does several
   *  manipulations in the process of  creating a production object.
   *  After some validity checking it translates labels that appear in
   *  actions into code for accessing objects on the runtime parse stack.
   *  It them merges adjacent actions if they appear and moves any trailing
   *  action into the final reduce actions string.  Next it removes any
   *  embedded actions by factoring them out with new action productions.  
   *  Finally it assigns a unique index to the production.<p>
   *
   *  Factoring out of actions is accomplished by creating new "hidden"
   *  non terminals.  For example if the production was originally: <pre>
   *    A ::= B {action} C D
   *  </pre>
   *  then it is factored into two productions:<pre>
   *    A ::= B X C D
   *    X ::= {action}
   *  </pre> 
   *  (where X is a unique new non terminal).  This has the effect of placing
   *  all actions at the end where they can be handled as part of a reduce by
   *  the parser.
   */
  public production(
    non_terminal    lhs_sym, 
    production_part rhs_parts[], 
    int             rhs_l,
    String          action_str)
    throws internal_error
    {
      int         i;
      action_part tail_action;

      /* remember the length */
      if (rhs_l >= 0)
	_rhs_length = rhs_l;
      else if (rhs_parts != null)
	_rhs_length = rhs_parts.length;
      else
	_rhs_length = 0;
	
      /* make sure we have a valid left-hand-side */
      if (lhs_sym == null) 
	throw new internal_error(
	  "Attempt to construct a production with a null LHS");

      /* translate labels appearing in action strings */
      action_str = translate_labels(
			 rhs_parts, rhs_l, action_str, lhs_sym.stack_type());

      /* count use of lhs */
      lhs_sym.note_use();

      /* create the part for left-hand-side */
      _lhs = new symbol_part(lhs_sym);

      /* merge adjacent actions (if any) */
      _rhs_length = merge_adjacent_actions(rhs_parts, _rhs_length);

      /* strip off any trailing action */
      tail_action = strip_trailing_action(rhs_parts, _rhs_length);
      if (tail_action != null) _rhs_length--;

      /* allocate and copy over the right-hand-side */
      _rhs = new production_part[_rhs_length];
      for (i=0; i<_rhs_length; i++)
	_rhs[i] = rhs_parts[i];

      /* count use of each rhs symbol */
      for (i=0; i<_rhs_length; i++)
	if (!_rhs[i].is_action())
	  ((symbol_part)_rhs[i]).the_symbol().note_use();

      /* merge any trailing action with action string parameter */
      if (action_str == null) action_str = "";
      if (tail_action != null && tail_action.code_string() != null)
	action_str = tail_action.code_string() + action_str;

      /* stash the action */
      _action = new action_part(action_str);

      /* rewrite production to remove any embedded actions */
      remove_embedded_actions();

      /* assign an index */
      _index = next_index++;

      /* put us in the global collection of productions */
      _all.put(new Integer(_index),this);

      /* put us in the production list of the lhs non terminal */
      lhs_sym.add_production(this);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Constructor with no action string. */
  public production(
    non_terminal    lhs_sym, 
    production_part rhs_parts[], 
    int             rhs_l)
    throws internal_error
    {
      this(lhs_sym,rhs_parts,rhs_l,null);
    }
 
  /*-----------------------------------------------------------*/
  /*--- (Access to) Static (Class) Variables ------------------*/
  /*-----------------------------------------------------------*/
 
  /** Table of all productions.  Elements are stored using their index as 
   *  the key.
   */
  protected static Hashtable _all = new Hashtable();
 
  /** Access to all productions. */
  public static Enumeration all() {return _all.elements();};

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
 
  /** Total number of productions. */
  public static int number() {return _all.size();};

  /** Static counter for assigning unique index numbers. */
  protected static int next_index;

  /*-----------------------------------------------------------*/
  /*--- (Access to) Instance Variables ------------------------*/
  /*-----------------------------------------------------------*/

  /** The left hand side non-terminal. */
  protected symbol_part _lhs;

  /** The left hand side non-terminal. */
  public symbol_part lhs() {return _lhs;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** A collection of parts for the right hand side. */
  protected production_part _rhs[];

  /** Access to the collection of parts for the right hand side. */
  public production_part rhs(int indx) throws internal_error
    {
      if (indx >= 0 && indx < _rhs_length)
	return _rhs[indx];
      else
	throw new internal_error(
	  "Index out of range for right hand side of production");
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** How much of the right hand side array we are presently using. */
  protected int _rhs_length;

  /** How much of the right hand side array we are presently using. */
  public int rhs_length() {return _rhs_length;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** An action_part containing code for the action to be performed when we 
   *  reduce with this production. 
   */
  protected action_part _action;

  /** An action_part containing code for the action to be performed when we 
   *  reduce with this production. 
   */
  public action_part action() {return _action;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Index number of the production. */
  protected int _index;

  /** Index number of the production. */
  public int index() {return _index;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Count of number of reductions using this production. */
  protected int _num_reductions = 0;

  /** Count of number of reductions using this production. */
  public int num_reductions() {return _num_reductions;}

  /** Increment the count of reductions with this non-terminal */
  public void note_reduction_use() {_num_reductions++;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Is the nullability of the production known or unknown? */
  protected boolean _nullable_known = false;

  /** Is the nullability of the production known or unknown? */
  public boolean nullable_known() {return _nullable_known;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Nullability of the production (can it derive the empty string). */
  protected boolean _nullable = false;

  /** Nullability of the production (can it derive the empty string). */
  public boolean nullable() {return _nullable;}

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** First set of the production.  This is the set of terminals that 
   *  could appear at the front of some string derived from this production.
   */
  protected terminal_set _first_set = new terminal_set();

  /** First set of the production.  This is the set of terminals that 
   *  could appear at the front of some string derived from this production.
   */
  public terminal_set first_set() {return _first_set;}

  /*-----------------------------------------------------------*/
  /*--- Static Methods ----------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Determine if a given character can be a label id starter. 
   * @param c the character in question. 
   */
  protected static boolean is_id_start(char c)
    {
      return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == '_');

      //later need to handle non-8-bit chars here
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Determine if a character can be in a label id. 
   * @param c the character in question.
   */
  protected static boolean is_id_char(char c)
    {
      return is_id_start(c) || (c >= '0' && c <= '9');
    }

  /*-----------------------------------------------------------*/
  /*--- General Methods ---------------------------------------*/
  /*-----------------------------------------------------------*/

  /** Determine the translation for one label id found within a code_string. 
   *  Symbols appearing in the RHS correspond to objects found on the parse
   *  stack at runtime.  The code to access them, becomes code to access an
   *  object at the appropriate offset from the top of the stack, and then
   *  cast that to the proper type.
   *
   * @param id_str    the name of the id to be translated.
   * @param act_pos   the original position of the action it appears in.
   * @param label_map a hash table mapping labels to positions in the RHS.
   * @param type_map  a hash table mapping labels to declared symbol types.
   */
  protected String label_translate(
    String    id_str,     /* the id string we are (possibly) translating */
    int       act_pos,    /* position of the action                      */
    Hashtable label_map,  /* map from labels to positions in the RHS     */
    Hashtable label_types)/* map from labels to stack types              */
    {
      Integer label_pos;
      String  label_type;
      int     offset;

      /* look up the id */
      label_pos  = (Integer)label_map.get(id_str);

      /* if we don't find it, just return the id */
      if (label_pos == null) return id_str;

      /* extract the type of the labeled symbol */
      label_type = (String)label_types.get(id_str);

      /* is this for the LHS? */
      if (label_pos.intValue() == -1)
        {
	  /* return the result object cast properly */
	  return "((" + label_type + ")" + emit.pre("result") + ")";
         }

       /* its a RHS label */

       /* if the label appears after the action, we have an error */
       if (label_pos.intValue() > act_pos)
         {
	   /* emit an error message */
	   System.err.println("*** Label \"" + id_str + 
	     "\" appears in action before it appears in production");
	    lexer.error_count++;

	    // later need to print the production this is in
    
	    /* just return the id unchanged */
	      return id_str;
	  }

      /* calculate the stack offset as the difference in position from 
	 label to action minus one */
      offset = (act_pos - label_pos.intValue())-1;

      /* translation is properly cast element at that offset from TOS */
      return "(/*"+id_str+"*/("+label_type+")" + 
       emit.pre("stack") + ".elementAt(" + emit.pre("top") +"-"+ offset + "))";
   
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Translate all the label names within an action string to appropriate code.
   * @param act_string  the string to be translated
   * @param act_pos     the position that the action originally held in the 
   *                    production.
   * @param label_map   a hash table mapping labels to positions in the RHS.
   * @param type_map    a hash table mapping labels to declared symbol types.
   */
  protected String action_translate(
    String    act_string,  /* the action string                     */
    int       act_pos,     /* the position of the action on the RHS */
    Hashtable label_map,   /* map from labels to RHS positions      */
    Hashtable label_types) /* map from labels to symbol stack types */
    {
      int          id_start;
      int          pos;
      int          len;
      String       id_str;
      boolean      in_id;
      StringBuffer result;
      char         buffer[];

      /* if we have no string we are done */
      if (act_string == null || act_string.length()== 0) return act_string;

      len = act_string.length();

      /* set up a place to put the result */
      result = new StringBuffer(len + 50);

      /* extract string into array */
      buffer = new char[len + 1];
      act_string.getChars(0, len, buffer, 0);

      /* put terminator in buffer so we can look one past the end */
      buffer[len] = '\0';

      /* walk down the input buffer looking for identifiers */
      in_id = false;
      for (pos = id_start = 0; pos <= len; pos++)
	{
	  /* are we currently working on an id? */
	  if (in_id)
	    {
	      /* does this end the id? */
	      if (!is_id_char(buffer[pos]))
		{
		  /* extract the id string and translate it */
		  id_str = new String(buffer, id_start, pos - id_start);
		  result.append(
		      label_translate(id_str, act_pos, label_map,label_types));

		  /* copy over the ending character */
		  if (buffer[pos] != '\0')
	            result.append(buffer, pos, 1);

		  /* and we are done with this id */
		  in_id = false;
		}
	      else
		{
		  /* we are still in the id, so just keep going */
		}
	    }
	  else /* we are not inside an id */
	    {
	      /* start a new id? */
	      if (is_id_start(buffer[pos]))
	        {
		  /* start keeping these chars as an id */
	          in_id = true;
	          id_start = pos;
	        }
	     else
	       {
	         /* just copy over the char */
		 if (buffer[pos] != '\0')
	           result.append(buffer, pos, 1);
	       }
	    }
	}

      /* return the accumulated result */
      return result.toString();
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Translate label names to appropriate code within all action strings. 
   * @param rhs          array of RHS parts.
   * @param rhs_len      how much of rhs to consider valid.
   * @param final_action the final action string of the production. 
   * @param lhs_type     the object type associated with the LHS symbol.
   */ 
  protected String translate_labels(
    production_part  rhs[], 
    int              rhs_len, 
    String           final_action,
    String           lhs_type)
    {
      Hashtable   label_map   = new Hashtable(11);
      Hashtable   label_types = new Hashtable(11);
      symbol_part part;
      action_part act_part;
      int         pos;

      /* walk down the parts and extract the labels */
      for (pos = 0; pos < rhs_len; pos++)
	{
	  if (!rhs[pos].is_action())
	    {
	      part = (symbol_part)rhs[pos];

	      /* if it has a label enter it in the tables */
	      if (part.label() != null)
		{
		  label_map.put(part.label(), new Integer(pos));
		  label_types.put(part.label(), part.the_symbol().stack_type());
		}
	    }
	}

      /* add a label for the LHS */
      label_map.put("RESULT", new Integer(-1));
      label_types.put("RESULT", lhs_type);

      /* now walk across and do each action string */
      for (pos = 0; pos < rhs_len; pos++)
	{
	  if (rhs[pos].is_action())
	    {
	       act_part = (action_part)rhs[pos];
	       act_part.set_code_string(
		 action_translate(
		   act_part.code_string(), pos, label_map, label_types));
	    }
	}

      /* now do the final action string at the position after the last */
      return action_translate(final_action, rhs_len, label_map, label_types);

    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Helper routine to merge adjacent actions in a set of RHS parts 
   * @param rhs_parts array of RHS parts.
   * @param len       amount of that array that is valid.
   * @return          remaining valid length.
   */
  protected int merge_adjacent_actions(production_part rhs_parts[], int len)
    {
      int from_loc, to_loc, merge_cnt;

      /* bail out early if we have no work to do */
      if (rhs_parts == null || len == 0) return 0;

      merge_cnt = 0;
      to_loc = -1;
      for (from_loc=0; from_loc<len; from_loc++)
	{
	  /* do we go in the current position or one further */
	  if (to_loc < 0 || !rhs_parts[to_loc].is_action() 
			 || !rhs_parts[from_loc].is_action())
	    {
	      /* next one */
	      to_loc++;

	      /* clear the way for it */
	      if (to_loc != from_loc) rhs_parts[to_loc] = null;
	    }

	  /* if this is not trivial? */
	  if (to_loc != from_loc)
	    {
	      /* do we merge or copy? */
	      if (rhs_parts[to_loc] != null && rhs_parts[to_loc].is_action() && 
		  rhs_parts[from_loc].is_action())
	      {
	        /* merge */
	        rhs_parts[to_loc] = new action_part(
		  ((action_part)rhs_parts[to_loc]).code_string() +
		  ((action_part)rhs_parts[from_loc]).code_string());
	        merge_cnt++;
	      }
	    else
	      {
	        /* copy */
	        rhs_parts[to_loc] = rhs_parts[from_loc];
	      }
	    }
	}

      /* return the used length */
      return len - merge_cnt;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Helper routine to strip a trailing action off rhs and return it
   * @param rhs_parts array of RHS parts.
   * @param len       how many of those are valid.
   * @return          the removed action part.
   */
  protected action_part strip_trailing_action(
    production_part rhs_parts[],
    int             len)
    {
      action_part result;

      /* bail out early if we have nothing to do */
      if (rhs_parts == null || len == 0) return null;

      /* see if we have a trailing action */
      if (rhs_parts[len-1].is_action())
	{
	  /* snip it out and return it */
	  result = (action_part)rhs_parts[len-1];
	  rhs_parts[len-1] = null;
	  return result;
	}
      else
	return null;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Remove all embedded actions from a production by factoring them 
   *  out into individual action production using new non terminals.
   *  if the original production was:  <pre>
   *    A ::= B {action1} C {action2} D 
   *  </pre>
   *  then it will be factored into: <pre>
   *    A ::= B NT$1 C NT$2 D
   *    NT$1 ::= {action1}
   *    NT$2 ::= {action2}
   *  </pre>
   *  where NT$1 and NT$2 are new system created non terminals.
   */
  protected void remove_embedded_actions() throws internal_error
    {
      non_terminal new_nt;
      production   new_prod;

      /* walk over the production and process each action */
      for (int act_loc = 0; act_loc < rhs_length(); act_loc++)
	if (rhs(act_loc).is_action())
	  {
	    /* create a new non terminal for the action production */
	    new_nt = non_terminal.create_new();

	    /* create a new production with just the action */
	    new_prod = new action_production(this, new_nt, null, 0, 
			((action_part)rhs(act_loc)).code_string());

	    /* replace the action with the generated non terminal */
	    _rhs[act_loc] = new symbol_part(new_nt);
	  }
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Check to see if the production (now) appears to be nullable.
   *  A production is nullable if its RHS could derive the empty string.
   *  This results when the RHS is empty or contains only non terminals
   *  which themselves are nullable.
   */
  public boolean check_nullable() throws internal_error
    {
      production_part part;
      symbol          sym;
      int             pos;

      /* if we already know bail out early */
      if (nullable_known()) return nullable();

      /* if we have a zero size RHS we are directly nullable */
      if (rhs_length() == 0)
	{
	  /* stash and return the result */
	  return set_nullable(true);
	}

      /* otherwise we need to test all of our parts */
      for (pos=0; pos<rhs_length(); pos++)
	{
	  part = rhs(pos);

	  /* only look at non-actions */
	  if (!part.is_action())
	    {
	      sym = ((symbol_part)part).the_symbol();

	      /* if its a terminal we are definitely not nullable */
	      if (!sym.is_non_term()) 
		return set_nullable(false);
	      /* its a non-term, is it marked nullable */
	      else if (!((non_terminal)sym).nullable())
		/* this one not (yet) nullable, so we aren't */
	        return false;
	    }
	}

      /* if we make it here all parts are nullable */
      return set_nullable(true);
    }

  /** set (and return) nullability */
  boolean set_nullable(boolean v)
    {
      _nullable_known = true;
      _nullable = v;
      return v;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Update (and return) the first set based on current NT firsts. 
   *  This assumes that nullability has already been computed for all non 
   *  terminals and productions. 
   */
  public terminal_set check_first_set() throws internal_error
    {
      int    part;
      symbol sym;

      /* walk down the right hand side till we get past all nullables */
      for (part=0; part<rhs_length(); part++)
	{
	  /* only look at non-actions */
	  if (!rhs(part).is_action())
	    {
	      sym = ((symbol_part)rhs(part)).the_symbol();

	      /* is it a non-terminal?*/
	      if (sym.is_non_term())
		{
		  /* add in current firsts from that NT */
		  _first_set.add(((non_terminal)sym).first_set());

		  /* if its not nullable, we are done */
		  if (!((non_terminal)sym).nullable())
		    break;
		}
	      else
		{
	          /* its a terminal -- add that to the set */
		  _first_set.add((terminal)sym);

		  /* we are done */
		  break;
		}
	    }
	}

      /* return our updated first set */
      return first_set();
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Equality comparison. */
  public boolean equals(production other)
    {
      if (other == null) return false;
      return other._index == _index;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Generic equality comparison. */
  public boolean equals(Object other)
    {
      if (!(other instanceof production))
	return false;
      else
	return equals((production)other);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Produce a hash code. */
  public int hashCode()
    {
      /* just use a simple function of the index */
      return _index*13;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Convert to a string. */
  public String toString() 
    {
      String result;
      
      /* catch any internal errors */
      try {
        result = "production [" + index() + "]: "; 
        result += ((lhs() != null) ? lhs().toString() : "$$NULL-LHS$$");
        result += " :: = ";
        for (int i = 0; i<rhs_length(); i++)
	  result += rhs(i) + " ";
        result += ";";
        if (action()  != null && action().code_string() != null) 
	  result += " {" + action().code_string() + "}";

        if (nullable_known())
	  if (nullable())
	    result += "[NULLABLE]";
	  else
	    result += "[NOT NULLABLE]";
      } catch (internal_error e) {
	/* crash on internal error since we can't throw it from here (because
	   superclass does not throw anything. */
	e.crash();
	result = null;
      }

      return result;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Convert to a simpler string. */
  public String to_simple_string() throws internal_error
    {
      String result;

      result = ((lhs() != null) ? lhs().the_symbol().name() : "NULL_LHS");
      result += " ::= ";
      for (int i = 0; i < rhs_length(); i++)
	if (!rhs(i).is_action())
	  result += ((symbol_part)rhs(i)).the_symbol().name() + " ";

      return result;
    }

  /*-----------------------------------------------------------*/

};
