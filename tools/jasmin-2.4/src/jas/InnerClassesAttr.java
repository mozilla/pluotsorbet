/**
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.0 $
 */

package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class InnerClassesAttr
{
  static final CP attr = new AsciiCP("InnerClasses");
  private Vector list;

  public InnerClassesAttr()
  { list = new Vector(); }

  public void addInnerClass(InnerClass item)
  { list.add(item); }

  void resolve(ClassEnv e)
  {
    e.addCPItem(attr);
    for(Enumeration en = list.elements(); en.hasMoreElements(); )
      ((InnerClass)en.nextElement()).resolve(e);
  }

  void write(ClassEnv e, DataOutputStream out) throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(attr));
    out.writeInt((list.size() * InnerClass.size()) + 2);
    out.writeShort((short)list.size());
    for(Enumeration en = list.elements(); en.hasMoreElements(); )
      ((InnerClass)en.nextElement()).write(e, out);
  }
}

