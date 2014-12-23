/**
 * This class
 * is used to build up entries in a catch table.
 * @see Catchtable
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */

package jas;

import java.io.*;

public class CatchEntry
{
  Label start_pc, end_pc, handler_pc;
  int start_off, end_off, handler_off;
  CP catch_cpe;
  /**
   * Catch entries are created and then added to the Catchtable.
   * @param start Label marking the beginning of the area
   *       where the catch table is active.
   * @param end Label marking the end of the area where the
   *       table is active.
   * @param handler Label marking the entrypoint into the
   *  exception handling routine.
   * @param cat (usually a classCP) informing the VM to direct
   * any exceptions of this (or its subclasses) to the handler.
   * @see Catchtable
   */
  public
  CatchEntry(Label start, Label end, Label handler, CP cat)
  {
    start_pc = start;
    end_pc = end;
    handler_pc = handler;
    catch_cpe = cat;
  }

  /**
   * Catch entries are created and then added to the Catchtable.
   * @param start offset marking the beginning of the area
   *       where the catch table is active.
   * @param end offset marking the end of the area where the
   *       table is active.
   * @param handler offset marking the entrypoint into the
   *  exception handling routine.
   * @param cat (usually a classCP) informing the VM to direct
   * any exceptions of this (or its subclasses) to the handler.
   * @see Catchtable
   */
  public
  CatchEntry(int start, int end, int handler, CP cat)
  {
    start_off = start;
    end_off = end;
    handler_off = handler;
    catch_cpe = cat;
  }

  void resolve(ClassEnv e)
  { if (catch_cpe != null) e.addCPItem(catch_cpe); }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    if(start_pc!=null && end_pc != null && handler_pc != null) {
      start_pc.writeOffset(ce, null, out);
      end_pc.writeOffset(ce, null, out);
      handler_pc.writeOffset(ce, null, out);
    } else {
      out.writeShort(start_off);
      out.writeShort(end_off);
      out.writeShort(handler_off);
    }
    if (catch_cpe != null)
      { out.writeShort(e.getCPIndex(catch_cpe)); }
    else
      { out.writeShort(0); }
  }
}
