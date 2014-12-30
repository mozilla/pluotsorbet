/**
 * VerifyFrame are part of StackMap/StackMapFrame attributes
 * @author $Author: Daniel Reynaud $
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.1 $
 */

package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class VerifyFrame
{
  private static final int SAME_FRAME_S0_min    = 0;
  private static final int SAME_FRAME_S0_max    = 63;
  private static final int SAME_FRAME_S1_min    = 64;   // + vi
  private static final int SAME_FRAME_S1_max    = 127;
  private static final int SAME_FRAME_S1        = 247;  // + off, vi
  private static final int CHOP_FRAME_S0_min    = 248;  // + off
//  private static final int CHOP_FRAME_S0_max    = 250;
  private static final int SAME_FRAME_S0        = 251;  // +off
//  private static final int APPEND_FRAME_S0_min  = 252;  // +off, vi[n]
  private static final int APPEND_FRAME_S0_max  = 254;
  private static final int FULL_FRAME           = 255;

  private Vector stack, locals;
  private int offset;
  private Label off_label;
  private boolean offset_defined;

  public VerifyFrame(Vector InitialFrame)
  { stack = new Vector();
    locals = InitialFrame;
    if(locals == null)
        locals = new Vector();
    offset_defined = false; }

  private void defineOffset() throws jasError
  { if(offset_defined) throw new jasError("offset already defined");
    offset_defined = true; }

  public void setOffset(int offset) throws jasError
  { defineOffset();
    this.offset = offset; }

  public void setOffset(Label label) throws jasError
  { defineOffset();
    off_label = label; }

  public void addStackItem(String item, String val) throws jasError
  { stack.add(new VerificationTypeInfo(item, val)); }

  public void addLocalsItem(String item, String val) throws jasError
  { locals.add(new VerificationTypeInfo(item, val)); }

  public boolean haveOffset()
  { return offset_defined; }


// make copy of locals frame (with type-independed counter)
  public Vector getFrame(int count) throws jasError
  {
    if(count > locals.size())
        throw new jasError("Counter exceed range", true);
    Vector result = new Vector(locals);
    if(count != 0)  // else -- full copy
        result.setSize(count);
    return result;
  }

  public int getOffset(CodeAttr ce) throws jasError
  {
    if(off_label != null) {
        offset = ce.getPc(off_label);
        off_label = null; // for speed in next's request
    }
    return offset;
  }

  void resolve(ClassEnv e)
  {
    Enumeration en = stack.elements();
    while(en.hasMoreElements())
      ((VerificationTypeInfo)en.nextElement()).resolve(e);

    en = locals.elements();
    while(en.hasMoreElements())
      ((VerificationTypeInfo)en.nextElement()).resolve(e);
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out,
             VerifyFrame prev)
  throws IOException, jasError
  {
    int off = getOffset(ce);
    int stack_size  = stack.size();
    int locals_size = locals.size();

    if(prev != this) { // JDK >= 6
      if(prev != null) { // not first element
        int prev_off = prev.getOffset(ce);
        if(prev_off >= off)
          throw new jasError("Write unsorted StackMapFrame");
        off -= prev_off + 1;
      }

      if(stack_size <= 1) { // else can't compact
        boolean can_compact = true;
        int prev_size = 0;
        if(prev == null) { // first record
          if(stack_size == 0) {
            if(locals_size > (APPEND_FRAME_S0_max - SAME_FRAME_S0))
              can_compact = false;
          } else if(locals_size != 0)
              can_compact = false;
        } else { // not first record
          int cmpcn = locals_size;
          prev_size = prev.locals.size();
          int delta = locals_size - prev_size;
          if(stack_size == 0) {
            if(delta >= 0) {
              if(delta > (APPEND_FRAME_S0_max - SAME_FRAME_S0))
                can_compact = false;
              cmpcn -= delta;
            } else if(delta < (CHOP_FRAME_S0_min - SAME_FRAME_S0))
              can_compact = false;
          } else if(delta != 0) can_compact = false;
          if(can_compact)
            while(--cmpcn >= 0)
              if(!((VerificationTypeInfo)
                    locals.elementAt(cmpcn)).isEqual(e, ce,
                        ((VerificationTypeInfo)
                                      prev.locals.elementAt(cmpcn))))
              {
                can_compact = false;
                break;
              }
        }
        if(can_compact) {
          if(stack_size != 0) { // only if SAME_FRAME_S1...
            if(off <= (SAME_FRAME_S1_max - SAME_FRAME_S1_min))
              out.writeByte((byte)(off + SAME_FRAME_S1_min));
            else {
              out.writeByte((byte)SAME_FRAME_S1);
              out.writeShort((short)off);
            }
            ((VerificationTypeInfo)stack.elementAt(0)).write(e, ce, out);
            return;
          }
          // stack is empty
          int wrdt = locals_size - prev_size;
          if(   wrdt == 0
             && off <= (SAME_FRAME_S0_max - SAME_FRAME_S0_min)) {
            out.writeByte((byte)(off + SAME_FRAME_S0_min));
            return;
          }
          out.writeByte((byte)(SAME_FRAME_S0 + wrdt));
          out.writeShort((short)off);
          while(--wrdt >= 0)
            ((VerificationTypeInfo)
              locals.elementAt(prev_size++)).write(e, ce, out);
          return;
        }
      }
      // can't compact -- write full frame
      out.writeByte((byte)FULL_FRAME);
    } // end of StackMapFrame(JDK>=1.6) mode

    out.writeShort((short)off);
    out.writeShort((short)locals_size);  // number_of_locals
  //  System.out.println("number of local items "+locals_size);
    Enumeration en = locals.elements();
    while(--locals_size >= 0)
      ((VerificationTypeInfo)en.nextElement()).write(e, ce, out);

    out.writeShort((short)stack_size);  // number_of_stack_items
  //  System.out.println("number of stack items "+stack_size);
    en = stack.elements();
    while(--stack_size >= 0)
      ((VerificationTypeInfo)en.nextElement()).write(e, ce, out);
  }
}
