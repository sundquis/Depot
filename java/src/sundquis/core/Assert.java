/*

 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.File;

@Test.Skip
public class Assert {
	
	public static <T> T nonNull( T obj ) {
		if ( obj == null ) {
			Assert.fail();
		}
		return obj;
	}
	
	public static String nonEmpty( String s ) {
		if ( s == null || s.isEmpty() ) {
			Assert.fail();
		}
		return s;
	}
	
	public static String boundedString( String s, int minLength, int maxLength ) {
		if ( s == null || s.length() < minLength || s.length() > maxLength ) {
			Assert.fail();
		}
		return s;
	}
	
	public static File readableDirectory( File f ) {
		if ( f == null || !f.exists() || !f.isDirectory() || !f.canRead() ) {
			Assert.fail();
		}
		return f;
	}
	
	public static File writeableDirectory( File f ) {
		if ( f == null || !f.exists() || !f.isDirectory() || !f.canWrite() ) {
			Assert.fail();
		}
		return f;
	}
	
	public static File rwDirectory( File f ) {
		if ( f == null || !f.exists() || !f.isDirectory() || !f.canRead() || !f.canWrite() ) {
			Assert.fail();
		}
		return f;
	}
	
	public static File readableFile( File f ) {
		if ( f == null || !f.exists() || f.isDirectory() || !f.canRead() ) {
			Assert.fail();
		}
		return f;
	}
	
	public static File writeableFile( File f ) {
		if ( f == null || !f.exists() || f.isDirectory() || !f.canWrite() ) {
			Assert.fail();
		}
		return f;
	}
	
	public static File rwFile( File f ) {
		if ( f == null || !f.exists() || f.isDirectory() || !f.canRead() || !f.canWrite() ) {
			Assert.fail();
		}
		return f;
	}
	
	public static Integer nonZero( Integer n ) {
		if ( n == 0 ) {
			Assert.fail();
		}
		return n;
	}
	
	public static Integer positive( Integer n ) {
		if ( n <= 0 ) {
			Assert.fail();
		}
		return n;		
	}
	
	public static Integer nonNeg( Integer n ) {
		if ( n < 0 ) {
			Assert.fail();
		}
		return n;		
	}
	
	public static Integer lessThan( Integer n, Integer max ) {
		if ( n >= max ) {
			Assert.fail();
		}
		return n;
	}
	
	public static boolean isTrue( boolean predicate ) {
		if ( ! predicate ) {
			Assert.fail();
		}
		return predicate;
	}

	
	private static void fail() {
		throw new AssertionError();
	}
	


}
