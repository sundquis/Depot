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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Contract.java,v $
 * $Id: Contract.java,v 1.5 2002/05/31 20:12:46 toms Exp $
 * $Log: Contract.java,v $
 * Revision 1.5  2002/05/31 20:12:46  toms
 * daily
 *
 * Revision 1.4  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.3  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

/**
 * Supports "Design by Contract". Every method asserts a contract
 * by specifying
 *      pre-conditions: boolean properties required of its parameters
 *      post-conditions: boolean properties gauranteed on its result
 *
 * In addition, methods may make assertions about state during execution
 */
public final class Contract {

	public static final boolean ENFORCE = true;

	//public static final Class EXCEPTION_CLASS = RuntimeException.class;
	public static final Class EXCEPTION_CLASS = AssertionError.class;

	/**
	 * Not inteneded to be instantiated.
	 */
	private Contract() {}

	public static void cAssert( boolean condition ) {
		if ( Contract.ENFORCE ) {
			//if ( !condition ) { throw new RuntimeException( "Assertion failed" ); }
			assert condition;
		}
	}

	public static void cAssert( boolean condition, String message ) {
		if ( Contract.ENFORCE ) {
			//if ( !condition ) { throw new RuntimeException( "Assertion failed: " + message ); }
			assert condition : message;
		}
	}

}
