
package java_cup.runtime;

/** This subclass of token represents symbols that need to maintain one
 *  double value as an attribute.  It maintains that value in the public
 *  field int_val.
 *
 * @see java_cup.runtime.str_token
 * @version last updated: 1/7/96
 * @author  Scott Hudson
 */

public class double_token extends token {

  /** Full constructor. */
  public double_token(int term_num, double v)
    {
      /* super class does most of the work */
      super(term_num);

      double_val = v;
    }

  /** Constructor with default value of 0.0. */
  public double_token(int term_num)
    {
      this(term_num,0.0f);
    }

  /** The stored double value. */
  public double double_val;
};
