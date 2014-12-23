                                // This is not visible outside the
                                // package. It is used to
                                // handle the various types
                                // of operands used by Insns.
package jas;

import java.io.*;

abstract class InsnOperand
{
  abstract void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError;
  abstract int size(ClassEnv e, CodeAttr code) throws jasError;
  abstract void resolve(ClassEnv e);
  void writePrefix(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  { return; }
}

                                // Used to implement targets of Insns
class LabelOperand extends InsnOperand
{
  Label target;
  Insn source;
  boolean wide;
  int ref;

  LabelOperand(Label l, Insn source, int line)
  { target = l; this.source = source; this.wide = false; this.ref = line; }
  LabelOperand(Label l, Insn source, boolean wide, int line)
  { target = l; this.source = source; this.wide = wide; this.ref = line; }
  int size(ClassEnv ce, CodeAttr code) { if (wide) return 4; else return 2; }
  void resolve(ClassEnv e) { return; }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    if (wide) { target.writeWideOffset(ce, source, out); }
    else {
      int offset = ce.getPc(target);
      if (source != null)
        offset -= ce.getPc(source);
      if (offset > 32767 || offset < -32768)
        throw new jasError
          ("reference from line " +ref+ " exceed size for short");
      target.writeOffset(ce, source, out); }
    }
  }

class UnsignedByteOperand extends InsnOperand
{
  int val;

  UnsignedByteOperand(int n) { val = n; }
  int size(ClassEnv ce, CodeAttr code)  { return 1; }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    if (val >= 256)
      throw
        new jasError("Operand is too large (" +val+ ") for this instruction");
    out.writeByte((byte)(0xff & val));
  }
  void resolve(ClassEnv e) { return; }
}

                                // This (conditionally) adds a wide
                                // prefix if the value is larger than
                                // 256
class UnsignedByteWideOperand extends InsnOperand
  implements RuntimeConstants
{
  int val;
  boolean Wide;

  UnsignedByteWideOperand(int n, boolean Wide)
  {
    val = n;
    this.Wide = (Wide || val > 255);
  }

  int size(ClassEnv ce, CodeAttr code)
  { return Wide ? 3: 1; }

  void writePrefix(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  {
    if (Wide)
      out.writeByte((byte)(opc_wide));
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  {
    if (Wide)
      out.writeShort((short)(0xffff & val));
    else
      out.writeByte((byte)(val & 0xff));
  }
  void resolve(ClassEnv e) { return; }
}

class ByteOperand extends InsnOperand
{
  int val;

  ByteOperand(int n) { val = n; }
  int size(ClassEnv ce, CodeAttr code) { return 1; }
  void resolve(ClassEnv e) { return; }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  { out.writeByte((byte)val); }
}

class IntegerOperand extends InsnOperand
{
  int val;

  IntegerOperand(int n) { val = n; }
  int size(ClassEnv ce, CodeAttr code) { return 4; }
  void resolve(ClassEnv e) { return; }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  { out.writeInt(val); }
}

class ShortOperand extends InsnOperand
{
  int offset;
  ShortOperand(int n) { offset = n; }
  void resolve(ClassEnv e) { return; }
  int size(ClassEnv ce, CodeAttr code) { return 2; }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  { out.writeShort((short)offset); }
}

class CPOperand extends InsnOperand
{
  CP cpe;
  boolean wide;
  int size(ClassEnv ce, CodeAttr code) { if (wide) return 2; else return 1; }
  CPOperand(CP cpe) { this.cpe = cpe; wide = true; }
  CPOperand(CP cpe, boolean wide)
  { this.cpe = cpe; this.wide = wide; }
  void resolve(ClassEnv e)
  { e.addCPItem(cpe); }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    int idx = e.getCPIndex(cpe);
    if (wide)
      { out.writeShort((short) idx); }
    else
      {
        if (idx > 255)
          { throw new jasError("exceeded size for small cpidx" + cpe); }
        out.writeByte((byte) (0xff & (idx)));
      }
  }
}

                                // these are unique enough that
                                // they need a separate handler for their
                                // args
class LdcOperand extends InsnOperand implements RuntimeConstants
{
  CP cpe;
  Insn source;
  boolean wide;

  int size(ClassEnv ce, CodeAttr code) throws jasError
  {
    if (wide)
      { return 2; }
    else
      {
                                // Should we promote it?
        int idx = ce.getCPIndex(cpe);
        if (idx > 255)
          {
            wide = true;
            source.opc = opc_ldc_w;
            return 2;
          }
        return 1;
      }
  }
  LdcOperand(Insn s, CP cpe) { source = s; this.cpe = cpe; wide = true; }
  LdcOperand(Insn s, CP cpe, boolean wide)
  { source = s; this.cpe = cpe; this.wide = wide; }
  void resolve(ClassEnv e)
  { e.addCPItem(cpe); }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    int idx = e.getCPIndex(cpe);
    if (wide)
      { out.writeShort((short) idx); }
    else
      {
        if (idx > 255)
          { throw new jasError("exceeded size for small cpidx" + cpe); }
        out.writeByte((byte) (0xff & (idx)));
      }
  }
}


class InvokeinterfaceOperand extends InsnOperand
{
  CP cpe;
  int nargs;

  InvokeinterfaceOperand(CP cpe, int nargs)
  { this.cpe = cpe; this.nargs = nargs; }

  int size(ClassEnv ce, CodeAttr code) { return 4; }

  void resolve(ClassEnv e)
  { e.addCPItem(cpe); }

  void write (ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(cpe));
    out.writeByte((byte) (0xff & nargs));
    out.writeByte(0);
  }
}

class IincOperand extends InsnOperand
  implements RuntimeConstants
{
  int vindex, constt;
  boolean Wide;

  IincOperand(int vindex, int constt, boolean Wide)
  {
    this.vindex = vindex;
    this.constt = constt;
    this.Wide = (Wide ||
                 vindex > 255 ||
                 constt > 127 ||
                 constt < 127);
  }

  int size(ClassEnv ce, CodeAttr code)
  {
    return Wide ? 5 : 2;
  }

  void resolve(ClassEnv e) { return; }

  void writePrefix(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  {
    if (Wide)
      out.writeByte((byte)opc_wide);
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException
  {
    if (Wide)
      {
        out.writeShort((short)(0xffff & vindex));
        out.writeShort((short)(0xffff & constt));
      }
    else
      {
        out.writeByte((byte) (0xff & vindex));
        out.writeByte((byte) (0xff & constt));
      }
  }
}

class MultiarrayOperand extends InsnOperand
{
  CP cpe;
  int sz;

  MultiarrayOperand(CP cpe, int sz)
  { this.cpe = cpe; this.sz = sz; }
  void resolve(ClassEnv e) { e.addCPItem(cpe); }
  int size(ClassEnv ce, CodeAttr code) { return 3; }
  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    out.writeShort(e.getCPIndex(cpe));
    out.writeByte((byte)(0xff & sz));
  }
}

class LookupswitchOperand extends InsnOperand
{
  LabelOrOffset dflt;
  Insn source;
  int match[];
  LabelOrOffset jmp[];

  LookupswitchOperand(Insn s, LabelOrOffset def, int m[], LabelOrOffset j[])
  { dflt = def; jmp = j;  match = m;  source = s; }

  void resolve (ClassEnv e) { return; }
  int size(ClassEnv ce, CodeAttr code) throws jasError
  {
    int sz = 8;			// 4 + 4 + padding + jumptable
    int source_pc = code.getPc(source);
    if (((source_pc+1) % 4) != 0)
      {
				// need padding
	sz += (4 - ((source_pc+1) % 4));
      }

    if (jmp != null)
      { sz += 8*(jmp.length); }
    return sz;
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    int pad;
    int source_pc = ce.getPc(source);

    if (((source_pc+1) % 4) != 0)
      {				// need padding
	pad = (4 - ((source_pc+1) % 4));
	for (int x=0; x<pad; x++) out.writeByte(0);
      }

				// write offset to default
				// as a 4 byte signed value
    dflt.writeWideOffset(ce, source, out);
    if (jmp == null)
      { out.writeInt(0); }
    else
      {
	out.writeInt(jmp.length);
	for (int x=0; x<jmp.length; x++)
	  {
	    out.writeInt(match[x]);
	    jmp[x].writeWideOffset(ce, source, out);
	  }
      }
  }
}


class TableswitchOperand extends InsnOperand
{
  int min, max;
  LabelOrOffset dflt;
  LabelOrOffset jmp[];
  Insn source;

  TableswitchOperand(Insn s,int min, int max, LabelOrOffset def,
                     LabelOrOffset j[])
  {
    this.min = min; this.max = max;
    dflt = def; jmp = j; source = s;
  }

  void resolve(ClassEnv e) { return; }

  int size(ClassEnv ce, CodeAttr code)
    throws jasError
                                // the *real* reason for making it a
                                // method..
  {
    int sz = 12;                // 4+4+4+jmptable+padding...
    int source_pc = code.getPc(source);
    if (((source_pc+1) % 4) != 0)
      {                         // need padding
        sz += (4 - ((source_pc+1) % 4));
      }
    if (jmp != null)
      { sz += 4*(jmp.length); }
    return sz;
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
    throws IOException, jasError
  {
    int pad;
    int source_pc = ce.getPc(source);

    if (((source_pc+1) % 4) != 0)
      {                         // need padding
        pad = (4 - ((source_pc+1) % 4));
        for (int x=0; x<pad; x++) out.writeByte(0);
      }
    dflt.writeWideOffset(ce, source, out);
    out.writeInt(min);
    out.writeInt(max);
    int cnt = jmp.length;
    for (int x=0; x<cnt; x++)
      { jmp[x].writeWideOffset(ce, source, out); }
  }
}

class OffsetOperand extends InsnOperand {
  int val;
  boolean wide;
  Insn parent;

  OffsetOperand(Insn parent, int val) {
    this(parent, val, false);
  }

  OffsetOperand(Insn parent, int val, boolean wide) {
    this.parent = parent;
    this.val = val;
    this.wide = wide;
  }

  void resolve(ClassEnv e) { return; }

  int size(ClassEnv e, CodeAttr ce) {
    return wide ? 4 : 2;
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
                              throws IOException, jasError {
    if(wide)
      out.writeInt(val);
    else
      out.writeShort(val);
  }
}

class RelativeOffsetOperand extends InsnOperand {
  int val;
  boolean wide;
  Insn parent;

  RelativeOffsetOperand(Insn parent, int val) {
    this(parent, val, false);
  }

  RelativeOffsetOperand(Insn parent, int val, boolean wide) {
    this.parent = parent;
    this.val = val;
    this.wide = wide;
  }

  void resolve(ClassEnv e) { return; }

  int size(ClassEnv e, CodeAttr ce) {
    return wide ? 4 : 2;
  }

  void write(ClassEnv e, CodeAttr ce, DataOutputStream out)
                              throws IOException, jasError {
    if(wide)
      out.writeInt(val);
    else
      out.writeShort(val);
  }
}

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, Aug 10 2006
    Added 'Wide' prefix support to IincOperand and UnsideWideOperand
*/
