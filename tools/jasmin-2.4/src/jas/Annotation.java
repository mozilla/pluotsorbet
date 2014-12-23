/**
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.0 $
 */


package jas;
                                // one class to ring them all...

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

/*
void addAnnotation() // default
void addAnnotation(boolean visible, String clname)
void addAnnotation(boolean visible, String clname, int paramnum)
*/

public class Annotation extends CP
{
  private Vector fields;
  private AsciiCP type;
  private Annotation parent;
  private AnnotationElement field;
  private boolean is_default;

  public static final void ParserError() throws jasError
  { throw new jasError("internal logic error in annotation parsing"); }

  private Annotation(Annotation parent, AsciiCP type)
  {
    this.type = type;
    this.parent = parent;
    field = null;
    fields = new Vector();
  }

  public Annotation(String type)
  { this(null, new AsciiCP(type)); }

  public Annotation() // fictive for AnnotationDefault
  { this(null, null); }

  public Annotation nestAnnotation() throws jasError
  {
    if(field == null) ParserError();
    Annotation tmp = new Annotation(this, field.nestType());
    field.addValue(tmp);
    return(tmp);
  }

  public Annotation endAnnotation() throws jasError
  {
    if(field != null) {
      field.done();
      field = null;
    }
    return(parent);
  }

  public void addField(String name, String type, String add)
    throws jasError
  {
    if(this.type == null && fields.size() != 0) ParserError();
    if(field != null) {
      field.done();
      field = null;
    }
    if((name == null) != (this.type == null)) ParserError();
    field = new AnnotationElement(name, type, add);
    fields.add(field);
  }

  public void addValue(Object value) throws jasError
  {
    if(field == null) ParserError();
    field.addValue(value);
  }

  void resolve(ClassEnv e)
  {
    if(type != null) e.addCPItem(type);
    for(Enumeration en = fields.elements(); en.hasMoreElements(); )
      ((AnnotationElement)en.nextElement()).resolve(e);
  }

  int size() throws jasError
  {
    if(field != null) ParserError();

    int len = 2 + 2;
    if(type == null) {
      if(fields.size() != 1) ParserError();
      len = 0;
    }
    for(Enumeration en = fields.elements(); en.hasMoreElements(); )
      len += ((AnnotationElement)en.nextElement()).size();
    return(len);
  }

  void write(ClassEnv e, DataOutputStream out) throws IOException, jasError
  {
    if(field != null) ParserError();

    if(type != null) {
      out.writeShort(e.getCPIndex(type));
      out.writeShort((short)fields.size());
    } else if(fields.size() != 1) ParserError();
    for(Enumeration en = fields.elements(); en.hasMoreElements(); )
      ((AnnotationElement)en.nextElement()).write(e, out);
  }
}
