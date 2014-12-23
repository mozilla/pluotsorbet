/**
 * AnnotationElement are used by Annotation attributes
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.0 $
 */

package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class AnnotationElement
{
  private boolean array;
  private char sign;
  private CP name, exttype;
  private Vector values;

  private static final char type_int    = 'I';    // integer
  private static final char type_byte   = 'B';    // signed byte
  private static final char type_char   = 'C';    // unicode character
  private static final char type_short  = 'S';    // signed short
  private static final char type_bool   = 'Z';    // boolean true or false
  // end of int types
  private static final char type_float  = 'F';    // single precision IEEE foat
  private static final char type_double = 'D';    // double precision IEEE float
  private static final char type_long   = 'J';    // long integer
  //prefix
  private static final char type_array  = '[';
  //annotation special
  private static final char type_string = 's';    // constant string
  private static final char type_class  = 'c';    // return type descriptor
  //complex types
  private static final char type_enum   = 'e';    // enum constant (type + name)
  private static final char type_annot  = '@';    // nested annotation

  private static void badsignature() throws jasError
  { throw new jasError("invalid type signature for annotation field"); }

  public AnnotationElement(String name, String type, String exttype)
    throws jasError
  {
    this.name = null;
    if(name != null) this.name = new AsciiCP(name);
    values = new Vector();

    array = false;
    sign = type.charAt(0);
    if(sign != type_array) {
      if(type.length() != 1) badsignature();
    } else {
      array = true;
      if(type.length() != 2) badsignature();
      sign = type.charAt(1);
    }

    switch(sign) {
      default:
        badsignature();

      case type_enum:
      case type_annot:
        if(exttype == null) badsignature();
        this.exttype = new AsciiCP(exttype);
        break;

      case type_int:
      case type_byte:
      case type_char:
      case type_short:
      case type_bool:
      case type_float:
      case type_double:
      case type_long:
      case type_string:
      case type_class:
        if(exttype != null) badsignature();
        this.exttype = null;
        break;
    }
  }

  void addValue(Object value) throws jasError
  {
    if(value == null) Annotation.ParserError();

    if(!array && values.size() != 0)
      throw new jasError("too many values for nonarray annotation field type");

    CP cp = null;
    switch(sign) {
      case type_char:
      case type_bool:
      case type_byte:
      case type_short:
      case type_int:
        if(value instanceof Integer) {
          int val = ((Integer)value).intValue();
          boolean badval = false;
          switch(sign) {
            case type_bool:
              if(val < 0 || val > 1) badval = true;
              break;
            case type_char:
              if(val < 0 || val > 0xFFFF) badval = true;
              break;
            case type_byte:
              if(val < -128 || val > 127) badval = true;
            case type_short:
              if(val < -32768 || val > 32767) badval = true;
            default:    // type_int
              break;
          }
          if(badval)
            throw new jasError("annotation field value exceed range of type", true);
          cp = new IntegerCP(val);
        }
        break;
      case type_float:
        if(value instanceof Float)
          cp = new FloatCP(((Float)value).floatValue());
        break;
      case type_double:
        if(value instanceof Double) {
          cp = new DoubleCP(((Double)value).doubleValue());
        } else if(value instanceof Float) {
          cp = new DoubleCP(((Float)value).floatValue());
        }
        break;
      case type_long:
        if(value instanceof Long) {
          cp = new LongCP(((Long)value).longValue());
        } else if(value instanceof Integer) {
          cp = new LongCP(((Integer)value).intValue());
        }
        break;
      case type_string:
      case type_class:
      case type_enum:
        if(value instanceof String)
          cp = new AsciiCP((String)value);
        break;
      case type_annot:
        if(value instanceof Annotation)
          cp = (Annotation)value;
      default:
        break;
    }
    if(cp == null)
      throw new jasError("incompatible value for annotation field type");

    values.add(cp);
  }

  public AsciiCP nestType() throws jasError
  {
    if(sign != type_annot) Annotation.ParserError();
    return((AsciiCP)exttype);
  }

  public void done() throws jasError
  {
    switch(values.size()) {
      case 1:
        return;
      default:
        if(array) return;
        //pass thru
      case 0:
       Annotation.ParserError();
    }
  }

  void resolve(ClassEnv e)
  {
    if(name != null) e.addCPItem(name);
    if(sign == type_enum) e.addCPItem(exttype);
    for(Enumeration en = values.elements(); en.hasMoreElements(); ) {
      CP cp = ((CP)en.nextElement());
      if(sign != type_annot) e.addCPItem(cp);
      else cp.resolve(e);
    }
  }

  int size() throws jasError
  {
    done();

    int len;
    if(sign == type_annot) {
      len = values.size();  // tags
      for(Enumeration en = values.elements(); en.hasMoreElements(); )
        len += ((Annotation)en.nextElement()).size();
    } else {
      len = 1+2;
      if(sign == type_enum) len += 2;
      len *= values.size();
    }
    if(array) len += 1+2;
    if(name != null) len += 2;
    return(len);
  }

  void write(ClassEnv e,  DataOutputStream out) throws IOException, jasError
  {
    done();

    if(name != null) out.writeShort(e.getCPIndex(name));
    if(array) {
      out.writeByte(type_array);
      out.writeShort((short)values.size());
    }
    short id = 0;
    if(sign == type_enum) id = e.getCPIndex(exttype);
    for(Enumeration en = values.elements(); en.hasMoreElements(); ) {
      out.writeByte(sign);
      CP cp = ((CP)en.nextElement());
      switch(sign) {
        case type_annot:
           ((Annotation)cp).write(e, out);
           break;
        case type_enum:
          out.writeShort(id);
          //pass thru
        default:
          out.writeShort(e.getCPIndex(cp));
          break;
      }
    }
  }

}

