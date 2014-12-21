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

public class AnnotDefAttr
{
  static final CP attr = new AsciiCP("AnnotationDefault");
  Annotation ann;

  public AnnotDefAttr()
  { ann = new Annotation(); }

  public Annotation get()
  { return(ann); }

  void resolve(ClassEnv e)
  {
    e.addCPItem(attr);
    ann.resolve(e);
  }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(attr));
    out.writeInt(ann.size());
    ann.write(e, out);
  }
}
