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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Fatal.java,v $
 * $Id: Fatal.java,v 1.6 2001/08/15 09:35:56 toms Exp $
 * $Log: Fatal.java,v $
 * Revision 1.6  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.4  2001/08/14 19:59:25  scottw
 * no message
 *
 * Revision 1.3  2001/08/01 19:21:41  scottw
 * Added parameterless unimplemented method()
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

/**
 * A class used to signal fatal runtime conditions.
 */
public final class Fatal {

	/**
	 * Not intended to be instantiated.
	 */
	private Fatal() {}

	/**
	 * Indicates that an unimplemented block of code has been reached and
	 * includes a detail message in the exception.
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void unimplemented( String detail ) {
		throw new FatalException( "Unimplemented: " + detail );
	}


	public static void unimplemented() {
		unimplemented( "This method is intentionally not implemented" );
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
		throw new FatalException( "Error: " + detail, cause );
	}

	/**
	 * Indicates that a fatal error has occurred and includes
	 * a throwable cause.
	 *
	 * @param cause
	 *      The exception that caused the fatal error.
	 */
	public static void error( Throwable cause ) {
		throw new FatalException( "Error", cause );
	}

	/**
	 * Indicates that a fatal error has occurred and includes a detail
	 * message in the exception.
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void error( String detail ) {
		throw new FatalException( "Error: " + detail );
	}

	public static void impossible() {
		impossible( "Unexpected defect condition" );
	}


	/**
	 * Indicates that a state believed to be impossible has been reached
	 * as a result of an exception toss.
	 *
	 * @param cause
	 *      The exception that caused the fatal error.
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void impossible( String detail, Throwable cause ) {
		throw new FatalException( "Error: " + detail, cause );
	}

	/**
	 * Indicates that a state believed to be impossible has been reached
	 * as a result of an exception toss.
	 *
	 * @param cause
	 *      The exception that caused the fatal error.
	 */
	public static void impossible( Throwable cause ) {
		throw new FatalException( "Error", cause );
	}

	/**
	 * Indicates that a state believed to be impossible has been reached.
	 *
	 * @param detail
	 *      A string detail message to include in the exception message.
	 */
	public static void impossible( String detail ) {
		throw new FatalException( "Error: " + detail );
	}

}
