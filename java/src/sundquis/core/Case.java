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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Case.java,v $
 * $Id: Case.java,v 1.4 2002/12/09 14:05:48 toms Exp $
 * $Log: Case.java,v $
 * Revision 1.4  2002/12/09 14:05:48  toms
 * no message
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
 * The interface used to associate actions with case statements
 *
 * @see Switch
 */
public interface Case {

	/**
	 * Encapsulates the action associated with this case.
	 *
	 * @param arg
	 *		The object argument for this case
	 *
	 * @return
	 *      The result, if any, of this action; <tt>null</tt> is returned
	 *      if there is no associated result.
	 */
	public Object doCase( Object arg );

}
