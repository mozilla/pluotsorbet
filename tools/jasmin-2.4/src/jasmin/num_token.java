
package jasmin;
import java_cup.*;

/** This subclass of token represents symbols that need to maintain one
 *  number value as an attribute.  It maintains that value in the public
 *  field num_val.
 *
 * @see java_cup.runtime.str_token
 * @version last updated: 1/7/96
 * @author  Jon Meyer
 */

class num_token extends java_cup.runtime.token {

  /** Full constructor. */
  public num_token(int term_num, Number v)
    {
      /* super class does most of the work */
      super(term_num);

      num_val = v;
    }

  /** Constructor with default value of 0 */
  public num_token(int term_num)
    {
      this(term_num, new Integer(0));
    }

  /** The stored number reference. */
  public Number num_val;
};
