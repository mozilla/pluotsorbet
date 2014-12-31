/**
 * This is used to encapsulate a CodeAttr so it can be added
 * into a class.
 * @see ClassEnv#addMethod
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */
package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class Method
{
  short acc;
  CP name, desc;
  CodeAttr code;
  ExceptAttr excepts;
  SignatureAttr sig;
  DeprecatedAttr depr;
  AnnotationAttr annVis, annInvis;
  AnnotParamAttr annParamVis, annParamInvis;
  AnnotDefAttr annDef;
  Vector generic;
  /**
   * @param macc method access permissions. It is a combination
   * of the constants provided from RuntimeConstants
   * @param name CP item representing name of method.
   * @param desc CP item representing descnature for object
   */
  public Method (short macc, AsciiCP name, AsciiCP desc)
  {
    acc = macc;
    this.name = name;
    this.desc = desc;
    sig = null;
    code = null;
    excepts = null;
    depr = null;
    annVis = annInvis = null;
    annParamVis = annParamInvis = null;
    annDef = null;
    generic = new Vector();
  }

  public void setCode(CodeAttr cd, ExceptAttr ex)
  { code = cd; excepts = ex; }

  public void setSignature(SignatureAttr sig)
  { this.sig = sig; }

  public void setDeprecated(DeprecatedAttr depr)
  { this.depr = depr; }

  /**
   * Add a generic attribute to the method. A generic attribute
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

  public Annotation addAnnotation(boolean visible, String clsname, int paramnum)
    throws jasError
  {
    if(paramnum <= 0 || paramnum >= 256)
      throw new jasError("annotation parameter number can be in range 1-256", true);
    Annotation ann = new Annotation(clsname);
    AnnotParamAttr aa = visible ? annParamVis : annParamInvis;
    if(aa == null) {
      aa = new AnnotParamAttr(visible);
      if(visible) annParamVis = aa;
      else annParamInvis = aa;
    }
    aa.add(ann, paramnum-1);
    return(ann);
  }

  public Annotation addAnnotation() throws jasError
  {
    if(annDef != null) Annotation.ParserError();
    annDef = new AnnotDefAttr();
    return(annDef.get());
  }


  void resolve(ClassEnv e)
  {
    e.addCPItem(name);
    e.addCPItem(desc);
    if (code != null)  code.resolve(e);
    if (excepts != null)  excepts.resolve(e);
    if (sig != null)  sig.resolve(e);
    if (depr != null) depr.resolve(e);
    if (annVis != null) annVis.resolve(e);
    if (annInvis != null) annInvis.resolve(e);
    if (annParamVis != null) annParamVis.resolve(e);
    if (annParamInvis != null) annParamInvis.resolve(e);
    if (annDef != null) annDef.resolve(e);
    for (Enumeration gen = generic.elements(); gen.hasMoreElements();)
    {
      GenericAttr gattr = (GenericAttr)gen.nextElement();
      gattr.resolve(e);
    }
  }

  void write(ClassEnv e, DataOutputStream out)
    throws IOException, jasError
  {
    short cnt = (short)generic.size();
    out.writeShort(acc);
    out.writeShort(e.getCPIndex(name));
    out.writeShort(e.getCPIndex(desc));
    if (code != null) cnt++;
    if (excepts != null) cnt++;
    if (sig != null) cnt++;
    if (depr != null) cnt++;
    if (annVis != null) cnt++;
    if (annInvis != null) cnt++;
    if (annParamVis != null) cnt++;
    if (annParamInvis != null) cnt++;
    if (annDef != null) cnt++;
    out.writeShort(cnt);
    if (code != null) code.write(e, out);
    if (excepts != null) excepts.write(e, out);
    if (sig != null) sig.write(e, out);
    if (depr != null) depr.write(e, out);
    if (annVis != null) annVis.write(e, out);
    if (annInvis != null) annInvis.write(e, out);
    if (annParamVis != null) annParamVis.write(e, out);
    if (annParamInvis != null) annParamInvis.write(e, out);
    if (annDef != null) annDef.write(e, out);
    for (Enumeration gen = generic.elements(); gen.hasMoreElements();)
    {
      GenericAttr gattr = (GenericAttr)gen.nextElement();
      gattr.write(e, out);
    }
  }
}
