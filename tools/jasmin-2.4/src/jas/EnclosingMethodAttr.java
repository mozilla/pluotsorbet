/**
 * This attribute is used to define the enclosing method of this class
 * @author $Author: Daniel Reynaud $
 * @version $Revision: 1.0 $
 */

package jas;

import java.io.*;

public class EnclosingMethodAttr
{
  static final CP attr = new AsciiCP("EnclosingMethod");
  CP clscp,
     ntcp;

  String cls, mtd, dsc;

  /**
   * Create an EnclosingMethod attribute
   * @param cls Name of the enclosing class
   * @param mtd Name of the enclosing method (can be null)
   * @param dsc Name of the enclosing method descriptor (can be null)
   * @see ClassEnv#setEnclosingMethod
   */

  public EnclosingMethodAttr(String cls, String mtd, String dsc)
  { this.cls = cls;
    this.mtd = mtd;
    this.dsc = dsc;
    clscp = new ClassCP(cls);
    if(mtd!=null && dsc!=null)
      ntcp = new NameTypeCP(mtd, dsc);
  }

  void resolve(ClassEnv e)
  { e.addCPItem(attr);
    e.addCPItem(clscp);  // add the CONSTANT_Class
    clscp.resolve(e);    // add the CONSTANT_Utf8 for the class name
    if(ntcp!=null) {
      e.addCPItem(ntcp); // add the CONSTANT_NameAndType
      ntcp.resolve(e);   // add the two CONSTANT_Utf8
    }
 }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(attr));
    out.writeInt(4);
    out.writeShort(e.getCPIndex(clscp));
    out.writeShort(ntcp==null ? 0 : e.getCPIndex(ntcp));
  }
}

