package jas;

/**
 * Error thrown on problems encountered while running the
 * basic jas assembler itself.
 * @author $Author: jonmeyerny $
 * @version $Revision: 1.1 $
 */

public class jasError extends Exception
{
  public boolean numTag;
  public jasError() { super(); numTag = false; }
  public jasError(String s) { super(s);  numTag = false; }
  public jasError(String s, boolean isNum) { super(s); numTag = true; }
}
