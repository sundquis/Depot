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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/FatalException.java,v $
 * $Id: FatalException.java,v 1.4 2001/08/15 09:35:56 toms Exp $
 * $Log: FatalException.java,v $
 * Revision 1.4  2001/08/15 09:35:56  toms
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
 * The class used to signal a fatal defect such as reaching an unimplemented
 * code block.
 */
public class FatalException extends ApplicationException {

	/**
	 * Constructs an exception with <code>null</code> as its detail message.
	 */
	public FatalException() {
		super();
	}

	/**
	 * Constructs an exception with specified detail message.
	 *
	 * @param msg
	 *      The detail message.
	 */
	public FatalException( String msg ) {
		super( msg );
	}

	/**
	 * Constructs an exception with specified cause and a detail message
	 * consisting of the class name and detail of the cause.
	 *
	 * @param cause
	 *      The exception that caused this exception.
	 */
	public FatalException( Throwable cause ) {
		super( cause );
	}

	/**
	 * Constructs an exception with specified cause and detail message.
	 *
	 * @param cause
	 *      The exception that caused this exception.
	 *
	 * @param msg
	 *      The detail message.
	 */
	public FatalException( String msg, Throwable cause ) {
		super( msg, cause );
	}

}
