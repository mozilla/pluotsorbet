package com.ibm.tck.javax.microedition.io.file.support;

import java.io.ByteArrayOutputStream;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
 
public class URLSupport {

	public static String getPathWithHost(String testpath) {
		int index = testpath.indexOf('/');
		
		if (index!=0)
			return testpath;
		else
			return "localhost" + testpath;
	}	
	
	public static String getPathWithoutHost(String testpath) {
		int index = testpath.indexOf('/');
		
		if (index!=0) {
			//incase the testpath is in this form:  <host>/<root>/<path>/
			testpath = testpath.substring(index,testpath.length());
		}
		return testpath;
	}


	/**
	 * Decode the <code>url</code>
	 */
	public static String getUnescapedForm(String url) {
		return decode(url);
	}
	
	/**
	 * First decode, then encode the <code>url</code>
	 * this needed to coever all the cases,
	 * where the <code>url</code> might be in escaped, or unescaped form, 
	 * and needs to be always returned in escaped form.
	 */
	public static String getEscapedForm(String url) {
		final String digits = "0123456789ABCDEF";
		return encode(decode(url), digits);
	}

	/**
	 * First decode, then encode the <code>url</code>
	 * this needed to coever all the cases,
	 * where the <code>url</code> might be in escaped, or unescaped form, 
	 * and needs to be always returned in escaped form.
	 */
	public static String getAlternativeEscapedForm(String url) {
		final String digits = "0123456789abcdef";
		return encode(decode(url), digits);
	}

	/** 
	 * All characters except for the following are escaped (converted into their 
	 * hexadecimal value prepended by '%'):
	 *     letters ('a'..'z', 'A'..'Z'),
	 *     numbers ('0'..'9'),
	 *     unreserved characters ('-', '_', '.', '!', '~', '*', '\'', '(', ')'), and
	 *     reserved characters ('/', ':')
	 * <p>
	 * For example: '#' -> %23
	 *
	 * @return java.lang.String	the string to be converted
	 * @param s java.lang.String	the converted string 
	 */
	 private static String encode(String s, String digits) {
		StringBuffer buf = new StringBuffer(s.length());
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if ((ch >= 'a' && ch <= 'z')
				|| (ch >= 'A' && ch <= 'Z')
				|| (ch >= '0' && ch <= '9')
				|| "-_.!~*\'()//:".indexOf(ch) > -1)
				buf.append(ch);
			else {
				byte[] bytes = new String(new char[] { ch }).getBytes();
				for (int j = 0; j < bytes.length; j++) {
					buf.append('%');
					buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
					buf.append(digits.charAt(bytes[j] & 0xf));
				}
			}
		}
		return buf.toString();
	}

	/**
	 * <p>
	 * '%' and two following hex digit characters are converted
	 *     to the equivalent byte value.
	 * All other characters are passed through unmodified.
	 * <p>
	 * e.g. "ABC %24%25" -> "ABC $%"
	 *
	 * @param		s java.lang.String	The encoded string.
	 * @return		java.lang.String	The decoded version.
	 */
	private static String decode(String s) {
		StringBuffer result = new StringBuffer(s.length());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < s.length();) {
			char c = s.charAt(i);
			if (c == '%') {
				out.reset();
				do {
					if (i + 2 >= s.length())
						throw new IllegalArgumentException("Incomplete % sequence at: " + i);
					int d1 = Character.digit(s.charAt(i+1), 16);
					int d2 = Character.digit(s.charAt(i+2), 16);
					if (d1 == -1 || d2 == -1)
						throw new IllegalArgumentException("Invalid % sequence (" + s.substring(i, i+3)+ ") at: " + String.valueOf(i));
					out.write((byte)((d1 << 4) + d2));
					i += 3;
				} while (i < s.length() && s.charAt(i) == '%');
				result.append(out.toString());
				continue;
			} else result.append(c);
			i++;
		}
		return result.toString();
	}
}
