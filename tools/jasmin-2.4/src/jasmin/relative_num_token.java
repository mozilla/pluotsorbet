
package jasmin;
import java_cup.*;

/** This subclass of token represents numbers beginning with '+' or '-'
 *
 * @see java_cup.runtime.str_token
 * @version last updated: 26/10/05
 * @author  Jon Meyer
 */

class relative_num_token extends java_cup.runtime.int_token {
    public relative_num_token(int term_num, int v) {
        super(term_num, v);
    }
}
