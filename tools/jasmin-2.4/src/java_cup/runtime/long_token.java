
package java_cup.runtime;

/** This subclass of token represents symbols that need to maintain one
 *  long value as an attribute.  It maintains that value in the public
 *  field int_val.
 *
 * @see java_cup.runtime.str_token
 * @version last updated: 1/7/96
 * @author  Scott Hudson
 */

public class long_token extends token {

  /** Full constructor. */
  public long_token(int term_num, long lv)
    {
      /* super class does most of the work */
      super(term_num);

      long_val = lv;
    }

  /** Constructor with default value of 0. */
  public long_token(int term_num)
    {
      this(term_num,0);
    }

  /** The stored long value. */
  public long long_val;
};
