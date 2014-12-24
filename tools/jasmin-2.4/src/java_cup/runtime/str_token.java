
package java_cup.runtime;

/** This subclass of token represents symbols that need to maintain one
 *  String value as an attribute.  It maintains that value in the public
 *  field str_val.
 *
 * @see java_cup.runtime.int_token
 * @version last updated: 11/25/95
 * @author  Scott Hudson
 */

public class str_token extends token {
  /** Full constructor. */
  public str_token(int term_num, String v)
    {
      /* super class does most of the work */
      super(term_num);

      str_val = v;
    }

  /** Constructor for value defaulting to an empty string. */
  public str_token(int term_num)
    {
      this(term_num, "");
    }

  /** The stored string value. */
  public String str_val;

};
