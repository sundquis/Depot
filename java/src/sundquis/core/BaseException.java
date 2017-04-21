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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/BaseException.java,v $
 * $Id: BaseException.java,v 1.8 2003/04/04 21:42:14 etl Exp $
 * $Log: BaseException.java,v $
 * Revision 1.8  2003/04/04 21:42:14  etl
 * Added version ids.
 *
 * Revision 1.7  2003/03/21 14:22:40  etl
 * Removed obsolete "CAUSE" in getMessage (1.4 does this for free).
 *
 * Revision 1.6  2002/05/31 17:48:49  toms
 * Use 1.4 exception chaining.
 *
 * Revision 1.5  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.4  2001/09/25 10:03:06  toms
 * Over-rode getMessage to show cause.
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
 * The base class for all exceptions. This class incorporates the 1.4
 * excpetion chaining feature; when we upgrade this class can be removed.
 *
 * @see BaseRuntime
 */
public class BaseException extends Exception {

    static final long serialVersionUID = 6058040941354186052L;
	
	/**
	 * Constructs an exception with <code>null</code> as its detail message.
	 */
	public BaseException() {
		super();
	}

	/**
	 * Constructs an exception with specified detail message.
	 *
	 * @param msg
	 *      The detail message.
	 */
	public BaseException( String msg ) {
		super( msg );
	}

	/**
	 * Constructs an exception with specified cause and a detail message
	 * consisting of the class name and detail of the cause.
	 *
	 * @param cause
	 *      The exception that caused this exception.
	 */
	public BaseException( Throwable cause ) {
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
	public BaseException( String msg, Throwable cause ) {
		super( msg, cause );
	}

}
