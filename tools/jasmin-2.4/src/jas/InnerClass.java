/**
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.0 $
 */
package jas;

import java.io.*;

public class InnerClass
{
  ClassCP inner, outer;
  CP name;
  short access;

  public static int size()
  { return (4 * 2); }

  /**
   * Make up a new attribute
   * @see ClassEnv#addInnerClass
   */
  public InnerClass(short access, String name, String inner, String outer)
  {
    this.access = access;
    this.name = null;
    if(name != null) this.name = new AsciiCP(name);
    this.inner = null;
    if(inner != null) this.inner = new ClassCP(inner);
    this.outer = null;
    if(outer != null) this.outer = new ClassCP(outer);
  }

  void resolve(ClassEnv e)
  {
    if(name  != null) e.addCPItem(name);
    if(inner != null) {
      e.addCPItem(inner);
      inner.resolve(e);
    }
    if(outer != null) {
      e.addCPItem(outer);
      outer.resolve(e);
    }
  }

  void write(ClassEnv e, DataOutputStream out) throws IOException, jasError
  {
    short id = 0;
    if(inner != null) id = e.getCPIndex(inner);
    out.writeShort(id);
    id = 0;
    if(outer != null) id = e.getCPIndex(outer);
    out.writeShort(id);
    id = 0;
    if(name != null) id = e.getCPIndex(name);
    out.writeShort(id);
    out.writeShort(access);
  }
}

