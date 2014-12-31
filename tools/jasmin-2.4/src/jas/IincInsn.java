/**
 * Some instructions are perniticky enough that its simpler
 * to write them separately instead of smushing them with
 * all the rest. the iinc instruction is one of them.
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */

package jas;

import java.io.*;


public class IincInsn extends Insn implements RuntimeConstants
{
  /**
   * @param vindex Index of variable to be incremented.
   * @param increment value to be added to the variable.
   *
   * A wide prefix is automatically added if either the
   * vindex exceeds 256, or the increment value lies
   * outside the range [-128, 127]
   *
   * The VM spec is unclear on how the wide instruction is implemented,
   * but the implementation makes <em>both</em> the constant and the
   * variable index 16 bit values for the wide version of this instruction.
   */
  public IincInsn(int vindex, int increment, boolean Wide)
  {
    opc = opc_iinc;
    operand = new IincOperand(vindex, increment, Wide);
  }
}

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, Aug 10 2006
    Added 'Wide' prefix support
*/
