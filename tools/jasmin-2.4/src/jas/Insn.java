/**
 * An Insn is a generic instruction that is added to a
 * CodeAttr to build up the code for a method.
 * @see CodeAttr
 * @see RuntimeConstants
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */


package jas;

import java.io.*;
import java.util.*;


public class Insn implements RuntimeConstants
{
  int opc;
  InsnOperand operand;

                                // private constructor, for the
                                // "strange" opcodes
  Insn() { return; }
  /**
   * Instructions with no arguments are built with
   * this constructor.
   */
  public Insn(int opc)
    throws jasError
  {
    if (opcLengths[opc] == 1)
      { operand = null; this.opc = opc; return; }
    throw new jasError
      (opcNames[opc] + " cannot be used without more parameters");
  }

  private void check_short(int val, int opc) throws jasError
  {
    if (val > 32767 || val < -32768)
      throw new jasError
        (opcNames[opc] + " numeric value exceed size for short");
  }

  /**
   * Instructions that take a single numeric argument. These are
   * opc_bipush,
   * opc_sipush,
   * opc_ret,
   * opc_iload,
   * opc_lload,
   * opc_fload,
   * opc_dload,
   * opc_aload,
   * opc_istore,
   * opc_lstore,
   * opc_fstore,
   * opc_dstore,
   * opc_astore,
   * opc_newarray
   *
   * Note that an extra wide prefix is automatically added
   * for the following instructions if the numeric argument
   * is larger than 256. Also note that while the spec makes
   * no mention of opc_ret as being a "wideable" opcode, thats
   * how the VM is implemented.
   *
   * opc_ret:
   * opc_iload:
   * opc_lload:
   * opc_fload:
   * opc_dload:
   * opc_aload:
   * opc_istore:
   * opc_lstore:
   * opc_fstore:
   * opc_dstore:
   * opc_astore:
   *
   */

  /**
   * Added branch instructions
   */

  public Insn(int opc, int val, boolean Wide)
    throws jasError
  {
    this.opc = opc;
    switch (opc)
      {
      case opc_bipush:
          if(val > 127 || val < -128)
            throw new jasError("bipush value exceed size of byte", true);
          operand = new ByteOperand(val);
          break;

      case opc_sipush:
      case opc_goto:
      case opc_if_acmpeq:
      case opc_if_acmpne:
      case opc_if_icmpeq:
      case opc_if_icmpge:
      case opc_if_icmpgt:
      case opc_if_icmple:
      case opc_if_icmplt:
      case opc_if_icmpne:
      case opc_ifeq:
      case opc_ifge:
      case opc_ifgt:
      case opc_ifle:
      case opc_iflt:
      case opc_ifne:
      case opc_ifnonnull:
      case opc_ifnull:
      case opc_jsr:
        check_short(val, opc);
        operand = new OffsetOperand(this, val); break;

      case opc_goto_w:
      case opc_jsr_w:
        operand = new OffsetOperand(this, val, true); break;

      case opc_newarray:
        if(val < 0 || val > 255)
            throw new jasError("newarray counter is illegal", true);
        operand = new UnsignedByteOperand(val);
        break;

      case opc_ret:
      case opc_iload:
      case opc_lload:
      case opc_fload:
      case opc_dload:
      case opc_aload:
      case opc_istore:
      case opc_lstore:
      case opc_fstore:
      case opc_dstore:
      case opc_astore:
        operand = new UnsignedByteWideOperand(val, Wide);
        break;

      default:
        throw new jasError
          (opcNames[opc] + " does not take a numeric argument");
      }
  }


// used for relative offsets (ex : goto $+5)

  public Insn(int opc, int val, char relative)
    throws jasError
  {
    this.opc = opc;
    switch (opc)
      {
      case opc_goto:
      case opc_if_acmpeq:
      case opc_if_acmpne:
      case opc_if_icmpeq:
      case opc_if_icmpge:
      case opc_if_icmpgt:
      case opc_if_icmple:
      case opc_if_icmplt:
      case opc_if_icmpne:
      case opc_ifeq:
      case opc_ifge:
      case opc_ifgt:
      case opc_ifle:
      case opc_iflt:
      case opc_ifne:
      case opc_ifnonnull:
      case opc_ifnull:
      case opc_jsr:
        check_short(val, opc);
        operand = new RelativeOffsetOperand(this, val); break;

      case opc_goto_w:
      case opc_jsr_w:
        operand = new RelativeOffsetOperand(this, val, true); break;

      default:
        throw new jasError
          (opcNames[opc] + " does not take a relative numeric argument");
      }
  }

  /**
   * Instructions that take a Label as an argument. These are
   * opc_jsr,
   * opc_goto,
   * opc_if_acmpne,
   * opc_if_acmpeq,
   * opc_if_icmpge,
   * opc_if_icmple,
   * opc_if_icmpgt,
   * opc_if_icmplt,
   * opc_if_icmpne,
   * opc_if_icmpeq,
   * opc_ifge,
   * opc_ifgt,
   * opc_ifne,
   * opc_ifle,
   * opc_iflt,
   * opc_ifeq,
   * opc_ifnull,
   * opc_ifnonnull,
   * opc_goto_w,
   * opc_jsr_w
   */
  public Insn(int opc, Label target, int line)
    throws jasError
  {
    this.opc = opc;
    switch(opc)
      {
      case opc_jsr:
      case opc_goto:
      case opc_if_acmpne:
      case opc_if_acmpeq:
      case opc_if_icmpge:
      case opc_if_icmple:
      case opc_if_icmpgt:
      case opc_if_icmplt:
      case opc_if_icmpne:
      case opc_if_icmpeq:
      case opc_ifge:
      case opc_ifgt:
      case opc_ifne:
      case opc_ifle:
      case opc_iflt:
      case opc_ifeq:
      case opc_ifnull:
      case opc_ifnonnull:
        operand = new LabelOperand(target, this, line);
        break;
      case opc_goto_w:
      case opc_jsr_w:
        operand = new LabelOperand(target, this, true, line);
        break;
      default:
        throw new jasError
          (opcNames[opc] + " does not take a label as its argument");
      }
  }
  /**
   * This constructor is used for instructions that take a CP item
   * as their argument. These are
   * opc_anewarray,
   * opc_ldc_w,
   * opc_ldc2_w,
   * opc_invokedynamic,
   * opc_invokenonvirtual,
   * opc_invokestatic,
   * opc_invokevirtual,
   * opc_new,
   * opc_checkcast,
   * opc_instanceof,
   * opc_getstatic,
   * opc_putstatic,
   * opc_getfield,
   * opc_putfield,
   * opc_ldc
   */
  public Insn(int opc, CP arg)
    throws jasError
  {
    this.opc = opc;
    switch(opc)
      {
      case opc_anewarray:
      case opc_invokedynamic:
      case opc_invokenonvirtual:
      case opc_invokestatic:
      case opc_invokevirtual:
      case opc_new:
      case opc_checkcast:
      case opc_instanceof:
      case opc_getstatic:
      case opc_putstatic:
      case opc_getfield:
      case opc_putfield:
        operand = new CPOperand(arg);
        break;
      case opc_ldc2_w:
      case opc_ldc_w:
        operand = new LdcOperand(this, arg);
        break;
      case opc_ldc:
        operand = new LdcOperand(this, arg, false);
        break;
      default:
        throw new jasError
          (opcNames[opc] + " does not take a CP item as an argument");
      }
  }

                                // This allows the Insn a chance to
                                // add things to the global env if
                                // necessary. The CPInsnOperands
                                // use  this to add the CP to the
                                // classEnv
  void resolve(ClassEnv e)
  { if (operand != null) { operand.resolve(e); } }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    if (operand != null)
      operand.writePrefix(e, ce, out);
    out.writeByte((byte) opc);
    if (operand != null)
      operand.write(e, ce, out);
  }

  int size(ClassEnv e, CodeAttr ce)
    throws jasError
  {
    if (operand == null) return 1;
    return (1 + operand.size(e, ce));
  }

  public String toString() {
    return "instruction "+opc+" "+((operand!=null)?operand.toString():"");
  }
}

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, Aug 10 2006
    Added 'wide' prefix to some instructions
*/
