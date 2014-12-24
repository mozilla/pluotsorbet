// Simple Example Scanner Class

import java_cup.runtime.*;

public class scanner {
  /* single lookahead character */
  protected static int next_char;

  /* advance input by one character */
  protected static void advance() throws java.io.IOException 
    { 
      next_char = System.in.read(); 
    }

  /* initialize the scanner */
  public static void init() throws java.io.IOException { advance(); }

  /* recognize and return the next complete token */
  public static token next_token() throws java.io.IOException
    {
      for (;;)
        switch (next_char)
	  {
	    case '0': case '1': case '2': case '3': case '4': 
	    case '5': case '6': case '7': case '8': case '9': 
	      /* parse a decimal integer */
	      int i_val = 0;
	      do {
	        i_val = i_val * 10 + (next_char - '0');
	        advance();
	      } while (next_char >= '0' && next_char <= '9');
	    return new int_token(sym.NUMBER, i_val);

	    case ';': advance(); return new token(sym.SEMI);
	    case '+': advance(); return new token(sym.PLUS);
	    case '-': advance(); return new token(sym.MINUS);
	    case '*': advance(); return new token(sym.TIMES);
	    case '/': advance(); return new token(sym.DIVIDE);
	    case '%': advance(); return new token(sym.MOD);
	    case '(': advance(); return new token(sym.LPAREN);
	    case ')': advance(); return new token(sym.RPAREN);

	    case -1: return new token(sym.EOF);

	    default: 
	      /* in this simple scanner we just ignore everything else */
	      advance();
	    break;
	  }
    }
};
