/*
 * 
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;


/**
 * A class used to signal fatal runtime conditions.
 * 
 * These (with the exception of warning) throw AppException.
 * Contract violations (illegal arguments, etc.) use the Assert class and throw AssertionError
 */
@Test.Skip
public final class Fatal {

	/**
	 * Not intended to be instantiated.
	 */
	private Fatal() {}

	/**
	 * Indicates that an unimplemented block of code has been reached and
	 * includes a detail message in the exception.
	 *
	 * @param detail	Brief message describing or outlining needed implementation.
	 */
	public static void unimplemented( String detail ) {
		fail( "Unimplemented: " + detail );
	}


	/**
	 * Indicates that a fatal error has occurred and includes a detail
	 * message in the exception and a throwable cause.
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 *
	 * @param cause
	 *      The exception that caused the fatal error.
	 */
	public static void error( String detail, Throwable cause ) {
		fail( "Error: " + detail, cause );
	}
	
	/**
	 * Indicates that a fatal error has occurred and includes a detail
	 * message in the exception.
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void error( String detail ) {
		fail( "Error: " + detail );
	}
	
	/**
	 * Print a warning message about a serious and potentially fatal condition.
	 * 
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void warning( String detail ) {
		new AppException( "WARNING: " + detail ).printStackTrace();
	}

	/**
	 * Indicates that a state believed to be impossible has been reached
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 *      
	 * @param cause
	 * 		Throwable cause linked to the failure
	 */
	public static void impossible( String detail, Throwable cause ) {
		fail( "Error: " + detail, cause );
	}
	
	/**
	 * Indicates that a state believed to be impossible has been reached
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void impossible( String detail ) {
		fail( "Error: " + detail );
	}
	

	
	private static void fail( String msg ) {
		throw new AppException( msg );
	}
	
	private static void fail( String msg, Throwable cause ) {
		throw new AppException( msg, cause );
	}

}
