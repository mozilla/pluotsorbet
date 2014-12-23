/**
 * This attribute is used to mark class/method/field as deprecated.
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */

package jas;

import java.io.*;

public class DeprecatedAttr
{
  static final CP attr = new AsciiCP("Deprecated");

  /**
   * Create a deprecated attribute.
   * @see ClassEnv#setDeprecated
   */

  public DeprecatedAttr() { }

  void resolve(ClassEnv e) 
     { e.addCPItem(attr); }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(attr));
    out.writeInt(0);
  }
}

