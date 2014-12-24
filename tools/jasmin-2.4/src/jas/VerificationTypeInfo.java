/**
 * VerificationTypeInfo are used by StackMap attributes
 * @author $Author: Daniel Reynaud $
 * @version $Revision: 1.0 $
 */

package jas;

import java.io.*;

public class VerificationTypeInfo
{
  private int tag;
  private ClassCP cls;
  private int index;
  private Label un_label;

  public VerificationTypeInfo(String item) throws jasError
  { this(item, null); }

  public VerificationTypeInfo(String item, String val)
    throws jasError {
    if(item.equals("Top"))
        tag = 0;
    else if(item.equals("Integer"))
        tag = 1;
    else if(item.equals("Float"))
        tag = 2;
    else if(item.equals("Long"))
        tag = 4;
    else if(item.equals("Double"))
        tag = 3;
    else if(item.equals("Null"))
        tag = 5;
    else if(item.equals("UninitializedThis"))
        tag = 6;
    else if(item.equals("Object")) {
        if(val==null) throw new jasError("Object requires a class name");
        cls = new ClassCP(val);
        tag = 7;
    }
    else if(item.equals("Uninitialized")) {
        if(val==null)
            throw new jasError("Uninitialized requires an integer or label");
        try {
            index = Integer.parseInt(val);
        } catch(Exception e) {
            un_label = new Label(val);
        }
        tag = 8;
    }
    else throw new jasError("Unknown item verification type : "+item);
  }

  void resolve(ClassEnv e)
  {
    if(tag == 7) {
      cls.resolve(e);
      e.addCPItem(cls);
    }
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeByte(tag);
    if(tag == 7)
      out.writeShort(e.getCPIndex(cls));
    else if(tag == 8) { // Uninitialized
// the following is not fully compliant to the CLDC spec !
      if(un_label != null)
        un_label.writeOffset(ce, null, out);
      else {
        if((index&0xFFFF) == index) // fits as a short
          out.writeShort(index);
        else
          out.writeInt(index);
      }
    }
  }

// next methods used for StackMapFrame attribute (jdk1.6)
  private int getOffset(CodeAttr ce) throws jasError
  {
    if(un_label != null)
        return un_label.getOffset(ce);
    return index;
  }

  boolean isEqual(ClassEnv e, CodeAttr ce, VerificationTypeInfo cmp)
  throws jasError
  {
    if(tag != cmp.tag)
        return false;

    if(tag == 7)
        return e.getCPIndex(cls) == e.getCPIndex(cmp.cls);
    if(tag == 8)
        return getOffset(ce) == cmp.getOffset(ce);
    return true;
  }
}
