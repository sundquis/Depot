/*
 * Copyright( c ) 2001 by Circus Software.
 * All rights reserved.
 *
 * This source code, and its documentation, are the confidential intellectual
 * property of Circus Software and may not be disclosed, reproduced,
 * distributed, or otherwise used for any purpose without the expressed
 * written permission of Circus Software.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Strings.java,v $
 * $Id: Strings.java,v 1.19 2003/06/06 15:53:05 etl Exp $
 * $Log: Strings.java,v $
 * Revision 1.19  2003/06/06 15:53:05  etl
 * Added another "pad" utility method.
 *
 * Revision 1.18  2003/05/27 18:53:23  etl
 * no message
 *
 * Revision 1.17  2003/04/15 17:38:48  etl
 * Added Levenshtien distance.
 *
 * Revision 1.16  2003/04/02 16:30:42  etl
 * Soundex utility and test cases.
 *
 * Revision 1.15  2003/03/21 18:50:31  etl
 * Added strip method and test cases.
 *
 * Revision 1.14  2003/01/27 18:27:28  toms
 * Moved toString(SpfSet) to SpfSet.toString(). The reference to SpfSet causes problems with our client bootstrapping procedure.
 *
 * Revision 1.13  2003/01/20 02:46:55  johnh
 * Added code to properly print out a SpfSet
 *
 * Revision 1.12  2002/12/16 20:02:47  toms
 * Removed reference to com.circus.spf, it causes bootstrap conflict
 *
 * Revision 1.11  2002/12/16 19:47:56  toms
 * Removed reference to com.circus.spf, it causes bootstrap conflict
 *
 * Revision 1.10  2002/12/15 18:16:43  johnh
 * Added SpfSet support to toString
 *
 * Revision 1.9  2002/05/05 22:19:35  johnh
 * Added boolean[] case to toString
 *
 * Revision 1.8  2002/04/17 22:19:39  toms
 * Added multi-table functionality.
 *
 * Revision 1.7  2002/01/27 01:26:44  johnh
 * Added toString method
 *
 * Revision 1.6  2001/11/14 12:06:16  toms
 * Added test class.
 *
 * Revision 1.5  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.3  2001/08/14 19:59:59  scottw
 * no message
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;


/**
 * String-related helper methods. These are typically used to help express
 * contract assertions.
 *
 * <p>
 * These methods illustrate Kent Beck's "Intention revealing message" construct.
 */
public final class Strings {

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	public static class TEST extends Test {
		
		public static void main( String[] args ) {
			new TEST().run();
		}

		public void run() {
			try {
				this.script();
			} catch ( Exception ex ) {
				this.testFail( "Unexpected exception", ex );
			} finally {
				this.testSummary();
			}
		}

		public void script() throws Exception {
			this.testEval( "5 dashes", ">" + Strings.padLeft( "", 5, '-' ) + "<" );
			this.testEval( "5 spaces", ">" + Strings.padLeft( "", 5, ' ' ) + "<" );
			
			String hello = "Hello World!";
			this.testCase( "Strip null", Strings.strip( null ), "" );
			this.testCase( "Strip empty", Strings.strip( "" ), "" );
			this.testCase( "Strip plain", Strings.strip( hello ), hello );
			this.testCase( "Strip space", Strings.strip( "\t" + hello + "  \n" ), hello );
			this.testCase( "Strip quotes", Strings.strip( "\"" + hello + "\"" ), hello );
			this.testCase( "Strip quotes and space",
				Strings.strip( "\"    " + hello + "\t\r\n \"" ), hello );
			this.testCase( "Strip leave unmatched (a)",
				Strings.strip( "\"" + hello ), "\"" + hello );
			this.testCase( "Strip leave unmatched (b)",
				Strings.strip( hello + "\"" ), hello + "\"" );
			this.testCase( "Strip leave embedded quotes",
				Strings.strip( "\"\"   " + hello + "   \"\"" ),
				"\"   " + hello + "   \"" );
			
			// http://www.archives.gov/research_room/genealogy/census/soundex.html
			String[] names = new String[] {
				"Washington",
				"Lee",
				"li",
				"LU",
				"Gutierrez",
				"Pfister",
				"Jackson",
				"Tymczak",
				"VanDeusen",
				"Ashcraft",
			};
			String[] codes = new String[] {
				"W252",
				"L000",
				"L000",
				"L000",
				"G362",
				"P236",
				"J250",
				"T522",
				"V532",
				"A261",
			};
			
			for ( int i = 0; i < names.length; i++ ) {
				this.testCase( names[i], Strings.soundex( names[i] ), codes[i] );
			}
		}
		
	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { Strings.TEST.main( args ); }

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */
	

	/**
	 * Not intended to be instantiated.
	 */
	private Strings() {}

	/**
	 * Tests if the string is null or empty.
	 *
	 * @param
	 *      The string to test.
	 *
	 * @return
	 *      True if the argument is null or a string of length zero.
	 */
	public static boolean isEmpty( String string ) {
		return( string == null ) ||( string.length() == 0 );
	}

	/**
	 * Tests if the string is not null and non-empty.
	 *
	 * @param
	 *      The string to test.
	 *
	 * @return
	 *      True if the argument is non-null and non-empty.
	 */
	public static boolean isNotEmpty( String string ) {
		return( string != null ) &&( string.length() > 0 );
	}

	/**
	 * Tests if the string is null or non-empty.
	 *
	 * @param
	 *      The string to test.
	 *
	 * @return
	 *      True if the argument is null or non-empty.
	 */
	public static boolean isNullOrNotEmpty( String string ) {
		return( string == null ) ||( string.length() > 0 );
	}

	public static String quoted( String string ) {
		String quoted = null;
		if ( string == null ) {
			quoted = "''";
		}
		else {
			quoted = "'" + string + "'";
		}
		return quoted;
	}

	public static boolean isHexDigit( char ch ) {
		boolean is = false;
		if ( ( ch >= 'a' ) &&( ch <= 'f' ) ) {
			is = true;
		}
		else {
			if ( ( ch >= 'A' ) &&( ch <= 'F' ) ) {
				is = true;
			}
			else {
				if ( ( ch >= '0' ) &&( ch <= '9' ) ) {
					is = true;
				}
			}
		}
		return is;
	}


	public static int hexDigitValue( char ch ) {
		int value = 0;
		if ( ( ch >= 'a' ) &&( ch <= 'f' ) ) {
			value = ( int )( ch - 'a' ) + 10;
		}
		else {
			if ( ( ch >= 'A' ) &&( ch <= 'F' ) ) {
				value = ( int )( ch - 'A' ) + 10;
			}
			else {
				if ( ( ch >= '0' ) &&( ch <= '9' ) ) {
					value = ( int )( ch - '0' );
				}
				else {
					Fatal.error( "Invalid hex digit" );
				}
			}
		}
		return value;
	}


	/**
	* Convert the given hexadecimal string into a valid Java/Unicode character.
	* The string must consist of only hexadecimal digits( in either uppercase
	* or lowercase ). The string can contain either 2 digits( for an ISO-8859
	* 8-bit character, automatically converted to its Unicode equivilent ), or
	* 4 digits.
	*
	* @param string
	*	A sequence of either two or four hexadecimal digits( in uppercase or
	*	lowercase ). Any other values or lengths are an error.
	*
	* @return
	*	Return the Unicode character equivilent for the given hexadecimal
	*	value.
	*/
	public static char toCharacter( String string ) {
		char ch = '\u0000';
		int end = string.length();
		if ( ( end == 2 ) ||( end == 4 ) ) {
			int value = 0;
			for ( int index = 0; index < end; index ++ ) {
				value = ( value * 16 ) + hexDigitValue( string.charAt( index ) );
			}
			ch = ( char )value;
		}
		else {
			Fatal.error( "Character conversion error" );
		}
		return ch;
	}
	
	
	/**
	 * If the width is non-zero, the arg is truncated
	 * or padded, as necessary, to fit the given width.
	 * If the width is negative, the field is left-padded.
	 * If the width is positive, the field is right-padded.
	 * If the width is zero, the field is taken as is.
	 */
	public static String pad( String s, int width, char pad ) {
		return (width == 0) ? s :
			(width < 0) ? Strings.padLeft( s, -1 * width, pad ) :
				Strings.padRight( s, width, pad );
	}


	public static String toStringPadded( int value, int length ) {
		String string = Integer.toString( value );
		for ( int count = string.length(); count < length; count ++ ) {
			string = "0" + string;
		}
		return string;
	}


	public static String padLeft( String original, int length, char pad ) {
		StringBuffer padded = new StringBuffer( original );
		
		while ( padded.length() < length ) {
			padded.append( pad );
		}
		padded.setLength( length );

		return padded.toString();
	}

	public static String padRight( String original, int length, char pad ) {
		StringBuffer padded = new StringBuffer();
		
		while ( padded.length() < length - original.length() ) {
			padded.append( pad );
		}
		
		padded.append( original );
		padded.setLength( length );
		
		return padded.toString();
	}
	
	/** A versital method for returning string representations of
	 * objects.  Expected to be used primarily during testing.  If
	 * used in any other context, consider rewriting to internally use
	 * StringBuffer.

	 * @param o the object to create the string representation for.
	 * @return the string representation.
	 **/
	public static String toString(Object o) {
		String result;
		if (o == null) {
			return "null";
		}
		if (o instanceof char[]) {
			return new String((char[]) o);
		}
		if (o instanceof long[]) {
			long[] a = (long[]) o;
			result = "[";
			for (int i = 0; i < a.length - 1; i++) {
				result = result + a[i] + ",";
			}
			if (a.length > 0) {
				result = result + a[a.length-1];
			}
			return result + "]";
		}
		if (o instanceof int[]) {
			int[] a = (int[]) o;
			result = "[";
			for (int i = 0; i < a.length - 1; i++) {
				result = result + a[i] + ",";
			}
			if (a.length > 0) {
				result = result + a[a.length-1];
			}
			return result + "]";
		}
		if (o instanceof byte[]) {
			byte[] a = (byte[]) o;
			result = "[";
			for (int i = 0; i < a.length - 1; i++) {
				result = result + a[i] + ",";
			}
			if (a.length > 0) {
				result = result + a[a.length-1];
			}
			return result + "]";
		}
		if (o instanceof boolean[]) {
			boolean[] a = (boolean[])o;
			result = "[";
			for (int i = 0; i < a.length - 1; i++) {
				result = result + a[i] + ",";
			}
			result = result + a[a.length-1] + "]";
			return result;
		}
		if (o instanceof Object[]) {
			Object[] a = (Object[]) o;
			result = "[";
			for (int i = 0; i < a.length - 1; i++) {
				result = result + toString(a[i]) + ",";
			}
			if (a.length > 0) {
				result = result + toString(a[a.length-1]);
			}
			return result + "]";
		}

		return o.toString();
		
	}
	
	/**
	 * If s is null or empty return empty.
	 * If s starts and ends with quotes remove them.
	 * Return the trimmed result
	 */
	public static String strip( String s ) {
		if ( Strings.isEmpty( s ) ) {
			return "";
		}

		if ( s.startsWith( "\"" ) && s.endsWith( "\"" ) ) {
			s = s.substring( 1, s.length() - 1 );
		}
		
		return s.trim();
	}
	
	// -1 means separator
	// -2 means skip
	private static final int[] SOUNDEX_CODE = new int[] {
		// A	B	C	D	E	F	G	H	I	J	K	L	M
		   -1,	1,	2,	3,	-1,	1,	2,	-2,	-1,	2,	2,	4,	5,
		// N	O	P	Q	R	S	T	U	V	W	X	Y	Z
		   5,	-1,	1,	2,	6,	2,	3,	-1,	1,	-2,	2,	-2,	2,
	};
	
	private static int soundexCode( char c ) {
		if ( c < 'A' || 'Z' < c ) {
			return -2;
		}
		
		return SOUNDEX_CODE[c - 'A'];
	}
	
	/**
	 * http://www.archives.gov/research_room/genealogy/census/soundex.html
	 */
	public static String soundex( String word ) {
		assert Strings.isNotEmpty( word );
		
		StringBuffer buf = new StringBuffer();
		
		word = word.toUpperCase();		
		char curChar = word.charAt(0);
		buf.append( curChar );
		int prevCode = soundexCode( curChar );
		int curCode = 0;
		int count = 0;
		int index = 1;
		while ( count < 3 && index < word.length() ) {
			curChar = word.charAt( index++ );
			curCode = soundexCode( curChar );
			if ( curCode == prevCode || curCode == -2 ) {
				// Skip consecutive equal code or skip-encoded chars
			} else if ( curCode == -1 ) {
				// Reset prev code
				prevCode = -1;
			} else {
				buf.append( curCode );
				count++;
				prevCode = curCode;
			}
		}
		
		while ( count < 3 ) {
			buf.append( "0" );
			count++;
		}
		
		return buf.toString();
	}
	
	public static int getLevenshteinDist( String v, String w ) {
		assert v != null;
		assert w != null;
		
		int[][] d = new int[v.length()+1][w.length()+1];
		int normal = 0, xpose = 0, vSkip = 0, wSkip = 0;
		int m1 = 0, m2 = 0;
		
		// ROW 0
		for ( int j = 0; j <= w.length(); j++ ) {
			d[0][j] = j;
		}
		
		for ( int i = 1; i <= v.length(); i++ ) {
			// COLUMN 0
			d[i][0] = i;
			
			for ( int j = 1; j <= w.length(); j++ ) {
				normal = d[i-1][j-1] + dist(v.charAt(i-1), w.charAt(j-1));
				if ( i > 1 && j > 1 && dist(v.charAt(i-2), w.charAt(j-1)) == 0
					&& dist(v.charAt(i-1), w.charAt(j-2)) == 0 ) {
					xpose = d[i-2][j-2] + 1;
				} else {
					xpose = 10000;
				}
				vSkip = d[i-1][j] + 1;
				wSkip = d[i][j-1] + 1;
				
				m1 = (normal < xpose) ? normal : xpose;
				m2 = (vSkip < wSkip) ? vSkip : wSkip;
				d[i][j] = (m1 < m2) ? m1 : m2;
			}
		}
		
		return d[v.length()][w.length()];
	}
	
	private static int dist( char c, char d ) {
		return c == d ? 0 : 1;
	}
	
	/**
	 * Forms a string by concatenating the given args, and separator
	 * If the corresponding width is non-zero, the arg is truncated
	 * or padded, as necessary, to fit the given width.
	 * If the width is negative, the field is left-padded.
	 * If the width is positive, the field is right-padded.
	 * If the width is zero, the field is taken as is.
	 */
	public static String join( String[] args, int[] widths, String separator, char pad ) {
		StringBuffer buf = new StringBuffer();
		
		for ( int i = 0; i < args.length; i++ ) {
			if ( i > 0 ) {
				buf.append( separator );
			}
			buf.append( Strings.pad( args[i], widths[i], pad ) );
		}
		
		return buf.toString();
	}
	
	public static String initCap( String s ) {
		return Strings.isEmpty( s ) ? s :
			Character.toUpperCase( s.charAt(0) ) + s.substring(1);
	}
	
}
