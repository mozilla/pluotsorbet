/* Copyright (C) 1999 Hewlett-Packard Company

   This file is part of Mauve.

   Mauve is free software; you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation; either version 2, or (at your option)
   any later version.

   Mauve is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Mauve; see the file COPYING.  If not, write to
   the Free Software Foundation, 59 Temple Place - Suite 330,
   Boston, MA 02111-1307, USA.
*/

// Tags: JDK1.0

package gnu.testlet.java.lang.Character;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class CharacterTest implements Testlet
{
  protected static TestHarness harness;
	public void test_Basics()
	{
	  
	    
		harness.check(!( Character.MIN_VALUE != '\u0000' ), 
			"test_Basics - 1" );
		harness.check(!( Character.MAX_VALUE != '\uffff' ), 
			"test_Basics - 2" );
		harness.check(!( Character.MIN_RADIX != 2 ), 
			"test_Basics - 3" );
		harness.check(!( Character.MAX_RADIX != 36 ), 
			"test_Basics - 4" );

		Character ch = new Character('b');
		harness.check(!( ch.charValue() != 'b' ), 
			"test_Basics - 5" );
	}

	public void test_toString()
	{
		Character ch = new Character('a');
		String str = ch.toString();

		harness.check(!( str.length() != 1 || !str.equals("a")), 
			"test_toString " );
	}


	public void test_equals()
	{
		Character ch1 = new Character('+');
		Character ch2 = new Character('+');
		Character ch3 = new Character('-');

		harness.check(!( !ch1.equals(ch2) || ch1.equals(ch3) || ch1.equals(null)), 
			"test_equals - 1" );
	}

	public void test_hashCode( )
	{
		Character ch1 = new Character('a');

		harness.check(!( ch1.hashCode() != (int) 'a' ), 
			"test_hashCode" );
	}


	public void test_digit( )
	{
		// radix wrong
		harness.check(!( Character.digit( 'a' , Character.MIN_RADIX - 1 ) != -1 ), 
			"test_digit - 1" );
		harness.check(!( Character.digit( 'a' , Character.MAX_RADIX + 1 ) != -1 ), 
			"test_digit - 2" );
	}


	public void test_others()
	{
		//calling them just for completion
// not supported		Character.getNumericValue( 'a' );
// not supported		Character.getType( 'a' );

	  Character.digit('\u0665', 10);
	  Character.digit('\u06F5', 10);
	  Character.digit('\u0968', 10);
	  Character.digit('\u06E8', 10);
	  Character.digit('\u0A68', 10);
	  Character.digit('\u0AE8', 10);
	  Character.digit('\u0B68', 10);
	  Character.digit('\u0BE8', 10);
	  Character.digit('\u0C68', 10);
	  Character.digit('\u0CE8', 10);
	  Character.digit('\u0D68', 10);
	  Character.digit('\u0E52', 10);
	  Character.digit('\u0ED2', 10);
	  Character.digit('\uFF12', 10);
	  Character.digit('\uFFFF', 10);

// not supported		Character.isISOControl( 'a' );
// not supported		Character.isIdentifierIgnorable( 'a' );
// not supported		Character.isJavaIdentifierPart( 'a' );
// not supported		Character.isJavaIdentifierStart( 'a' );
// not supported		Character.isJavaLetter( 'a' );

// not supported		Character.isLetter( 'a' );
		Character.isLowerCase( 'A' );
		Character.isLowerCase( 'a' );
		
// not supported		Character.isSpaceChar( 'a' );
// not supported		Character.isTitleCase( 'a' );
// not supported		Character.isUnicodeIdentifierPart( 'a' );
// not supported		Character.isUnicodeIdentifierStart( 'a' );
		Character.isUpperCase( 'a' );
		Character.isUpperCase( 'A' );
// not supported		Character.isWhitespace( 'a' );
// not supported		Character.toTitleCase( 'a' );
	}

	public void testall()
	{
		test_Basics();
		test_toString();
		test_equals();
		test_hashCode();
		test_digit();
		test_others();
	}

  public void test (TestHarness the_harness)
  {
    harness = the_harness;
    testall ();
  }

}
