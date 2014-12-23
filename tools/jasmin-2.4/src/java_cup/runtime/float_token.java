
package java_cup.runtime;

/** This subclass of token represents symbols that need to maintain one
 *  float value as an attribute.  It maintains that value in the public
 *  field int_val.
 *
 * @see java_cup.runtime.str_token
 * @version last updated: 1/7/96
 * @author  Scott Hudson
 */

public class float_token extends token {

  /** Full constructor. */
  public float_token(int term_num, float v)
    {
      /* super class does most of the work */
      super(term_num);

      float_val = v;
    }

  /** Constructor with default value of 0.0. */
  public float_token(int term_num)
    {
      this(term_num,0.0f);
    }

  /** The stored float value. */
  public float float_val;
};
