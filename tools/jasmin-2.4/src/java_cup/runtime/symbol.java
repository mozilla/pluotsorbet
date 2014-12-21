
package java_cup.runtime;

/** This class represents a (terminal or non-terminal) symbol that, among
 *  other things can be placed on the parse stack.  Symbols are used to 
 *  keep track of state on the parse stack.  The symbol currently on top
 *  of the stack contains the current state in the parse_state field.
 *  In addition to the parse_state field, symbols also maintain a record
 *  of the symbol number that they represent in the sym field.  Finally, 
 *  symbols are used contain to any attributes used by semantic action (this
 *  is done via fields added in subclasses -- see for example, int_token and
 *  str_token).
 *
 * @see java_cup.runtime.token
 * @see java_cup.runtime.int_token
 * @see java_cup.runtime.str_token
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */

public class symbol {

  /** Full constructor. */
  public symbol(int sym_num, int state)
    {
      sym = sym_num;
      parse_state = state;
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Constructor without a known state. */
  public symbol(int sym_num)
    {
      this(sym_num, -1);
    }

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The symbol number of the terminal or non terminal being represented */
  public int sym;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The parse state to be recorded on the parse stack with this symbol.
   *  This field is for the convenience of the parser and shouldn't be 
   *  modified except by the parser. 
   */
  public int parse_state;
};
