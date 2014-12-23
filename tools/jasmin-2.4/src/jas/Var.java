/**
 * Used to make up new field entries. Fields for a class can have
 * an additional "ConstantValue" attribute associated them,
 * which the java compiler uses to represent things like
 * static final int blah = foo;
 *
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */

package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class Var
{
  short var_acc;
  CP name, desc;
  SignatureAttr sig;
  ConstAttr const_attr;
  DeprecatedAttr depr;
  AnnotationAttr annVis, annInvis;
  Vector generic;

  /**
   * @param vacc access permissions for the field
   * @param name name of the field
   * @param desc type of the field
   * @param cattr Extra constant value information. Passing this as
   * null will not include this information for the record.
   * @see RuntimeConstants
   */
  public Var(short vacc, CP name, CP desc, ConstAttr cattr)
  {
    var_acc = vacc;
    this.name = name;
    this.desc = desc;
    const_attr = cattr;
    sig = null;
    depr = null;
    annVis = annInvis = null;
    generic = new Vector();
  }

  public void setSignature(SignatureAttr sig)
  { this.sig = sig; }

  public void setDeprecated(DeprecatedAttr depr)
  { this.depr = depr; }

  /**
   * Add a generic attribute to the field. A generic attribute
   * contains a stream of uninterpreted bytes which is ignored by
   * the VM (as long as its name doesn't conflict with other names
   * for attributes that are understood by the VM)
   */
  public void addGenericAttr(GenericAttr g)
  { generic.addElement(g); }

  /*
   * procedure group for annotation description
  */
  public Annotation addAnnotation(boolean visible, String clsname)
  {
    Annotation ann = new Annotation(clsname);
    AnnotationAttr aa = visible ? annVis : annInvis;
    if(aa == null) {
      aa = new AnnotationAttr(visible);
      if(visible) annVis = aa;
      else annInvis = aa;
    }
    aa.add(ann);
    return(ann);
  }

  void resolve(ClassEnv e)
  {
    e.addCPItem(name);
    e.addCPItem(desc);
    if (const_attr != null)
      { const_attr.resolve(e); }
    if(sig != null)
      sig.resolve(e);
    if(depr != null)
      depr.resolve(e);
    if(annVis != null)
      annVis.resolve(e);
    if(annInvis != null)
      annInvis.resolve(e);
    for(Enumeration gen = generic.elements(); gen.hasMoreElements(); )
    {
      GenericAttr gattr = (GenericAttr)gen.nextElement();
      gattr.resolve(e);
    }
  }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(var_acc);
    out.writeShort(e.getCPIndex(name));
    out.writeShort(e.getCPIndex(desc));

    short nb = (short)generic.size();

    if (const_attr != null)
      nb++;
    if(sig != null)
      nb++;
    if(depr != null)
      nb++;
    if(annVis != null)
      nb++;
    if(annInvis != null)
      nb++;
    out.writeShort(nb);
    if (const_attr != null)
      const_attr.write(e, out);
    if(sig != null)
      sig.write(e, out);
    if(depr != null)
      depr.write(e, out);
    if(annVis != null)
      annVis.write(e, out);
    if(annInvis != null)
      annInvis.write(e, out);
    for (Enumeration gen = generic.elements(); gen.hasMoreElements();)
    {
      GenericAttr gattr = (GenericAttr)gen.nextElement();
      gattr.write(e, out);
    }
  }
}
