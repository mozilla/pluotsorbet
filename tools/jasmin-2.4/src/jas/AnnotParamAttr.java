/**
 * This attribute can associated with a method, field or class.
 *
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.0 $
 */

package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class AnnotParamAttr
{
  CP attr;
  Vector anns;  // Vector<Vector<Annotation>>

  public AnnotParamAttr(boolean visible)
  {
    attr = new AsciiCP(visible ? "RuntimeVisibleParameterAnnotations" :
                                 "RuntimeInvisibleParameterAnnotations");
    anns = new Vector();
  }

  public void add(Annotation annotation, int paramnum)
  {
    Vector ap = null;
    int top = anns.size();
    if(paramnum < top) ap = (Vector)anns.elementAt(paramnum);
    if(ap == null) {
      if(paramnum >= top) anns.setSize(paramnum+1);
      anns.set(paramnum, ap = new Vector());
    }
    ap.add(annotation);
  }

  void resolve(ClassEnv e)
  {
    e.addCPItem(attr);
    for(int i = 0, top = anns.size(); i < top; i++) {
      Vector ap = (Vector)anns.elementAt(i);
      if(ap == null) continue;
      for(Enumeration en = ap.elements(); en.hasMoreElements(); )
        ((Annotation)en.nextElement()).resolve(e);
    }
  }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(attr));
    int top = anns.size(), len = 1 + 2*top;
    for(int i = 0; i < top; i++) {
      Vector ap = (Vector)anns.elementAt(i);
      if(ap != null)
        for(Enumeration en = ap.elements(); en.hasMoreElements(); )
          len += ((Annotation)en.nextElement()).size();
    }
    out.writeInt(len);
    out.writeByte((byte)top);
    for(int i = 0; i < top; i++) {
      Vector ap = (Vector)anns.elementAt(i);
      if(ap == null) out.writeShort(0);
      else {
        out.writeShort((short)ap.size());
        for(Enumeration en = ap.elements(); en.hasMoreElements(); )
          ((Annotation)en.nextElement()).write(e, out);
      }
    }
  }
}
