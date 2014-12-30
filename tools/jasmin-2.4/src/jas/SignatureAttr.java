/**
 * This attribute is used to represent the signature of a class, 
 * field or method
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */

package jas;

import java.io.*;

public class SignatureAttr
{
  static final CP attr = new AsciiCP("Signature");

  CP signature;

  /**
   * Create a Signature attribute.
   * @param signature The signature of the class, field or method
   * @see ClassEnv#setSignature
   */

  public SignatureAttr(String signature)
  { this.signature = new AsciiCP(signature); }

  /**
   * Create a signature attribute, with more control over attribute name
   * @param signature CP to be associated as the signature or the attribute
   * @see ClassEnv#setSignature
   */
  public SignatureAttr(CP signature)
  { this.signature = signature; }

  void resolve(ClassEnv e)
  { e.addCPItem(attr); e.addCPItem(signature); }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(attr));
    out.writeInt(2);
    out.writeShort(e.getCPIndex(signature));
  }
}

