
package java_cup.runtime;

/** This subclass of token represents symbols that need to maintain one
 *  int value as an attribute.  It maintains that value in the public
 *  field int_val.
 *
 * @see java_cup.runtime.str_token
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */

public class int_token extends token {

  /** Full constructor. */
  public int_token(int term_num, int iv)
    {
      /* super class does most of the work */
      super(term_num);

      int_val = iv;
    }

  /** Constructor with default value of 0. */
  public int_token(int term_num)
    {
      this(term_num,0);
    }

  /** The stored int value. */
  public int int_val;
};
