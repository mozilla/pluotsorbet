/**
 * @see StackMapAttr
 * @see StackMapFrameAttr
 * @author $Author: Daniel Reynaud $
 * @author $Author: Iouri Kharon $
 * @version $Revision: 1.3 $
 */

package jas;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;

public class StackMap
{
  static private final int JDK_SMF_MIN = 50;

  static CP attr = null;
  static boolean java6 = false;

  protected Vector frames;

  public static void reinit()
  { attr = null;
    java6 = false; }

  protected StackMap(CP attr)
  { this.attr = attr;
    frames = new Vector(); }

  public StackMap(ClassEnv e)
  {
    if(attr == null)
    {
        if(e.version_hi >= JDK_SMF_MIN) java6 = true;
        attr = new AsciiCP(java6 ? "StackMapTable" : "StackMap");
    }
    frames = new Vector();
  }

  public void addFrame(VerifyFrame f)
  { frames.add(f); }

  // get copy of previous locals frame (possible with choping)
  public Vector getLastFrame(int count) throws jasError
  {
      if(frames.isEmpty())
          return null;
      return ((VerifyFrame)frames.lastElement()).getFrame(count);
  }

  // this method call BEFORE write method
  public int size(ClassEnv e, CodeAttr ce)
  {
    try {
      if(java6) shellSort(ce);
      return write(e, ce, null);
    } catch(IOException ex) {
      System.err.println("UNEXPECTED IO EXCEPTION");
      ex.printStackTrace();
    } catch(jasError ex) {
      System.err.println("UNEXPECTED JAS ERROR");
      ex.printStackTrace();
    }
    return 0;
  }

  void resolve(ClassEnv e)
  { e.addCPItem(attr);

    Enumeration en = frames.elements();
    while(en.hasMoreElements())
      ((VerifyFrame)en.nextElement()).resolve(e);
  }

  int write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    // writing to a buffer first, so that we can print the length
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    DataOutputStream bufout = new DataOutputStream(buf);

    // not fully compliant to the CLDC spec !
    bufout.writeShort(frames.size());
    VerifyFrame prev = null;  // prepare for StackMapFrameAttr
    Enumeration en = frames.elements();
    while(en.hasMoreElements())
    {
      VerifyFrame cur = (VerifyFrame)en.nextElement();
      if(!java6) prev = cur;  // as flag
      cur.write(e, ce, bufout, prev);
      prev = cur;
    }
    int len = buf.toByteArray().length;
    if(out != null) // else, call for size calculation
    {
      out.writeShort(e.getCPIndex(attr));
      out.writeInt(len);
      buf.writeTo(out);
    }
    return (2 + 4) + len;
  }

  // sort (method of Shell) frames by offset (before writing)
  // used for StackMapFrameAttr mode.
  private void shellSort(CodeAttr ce) throws jasError
  {
    int n = frames.size();
    if(--n <= 0) return;
    int g = 3;
    if(g > n) g = 1;
    do {
      int i = g;
      do {
        VerifyFrame tmp = (VerifyFrame)frames.elementAt(i);
        int jn, j, ts = tmp.getOffset(ce);
        for(j = i; j >= g; j = jn) {
          jn = j - g;
          VerifyFrame t1 = (VerifyFrame)frames.elementAt(jn);
          if(t1.getOffset(ce) <= ts) break;
          frames.setElementAt(t1, j);
        }
        frames.setElementAt(tmp, j);
      }while(++i <= n);
    }while((g /= 2) > 0);
  }
}

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, May 07 2010, reset java6-mode for new compiled file
*/
