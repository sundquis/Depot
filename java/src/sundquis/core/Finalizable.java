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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Finalizable.java,v $
 * $Id: Finalizable.java,v 1.1 2001/10/18 15:36:23 toms Exp $
 * $Log: Finalizable.java,v $
 * Revision 1.1  2001/10/18 15:36:23  toms
 * Finalization capability.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

/**
 * Objects that wish to be finalized on JVM shutdown implement this interface
 * by exporting a public finalize method. The object then registers for
 * automatic finalization by calling <tt>Application.finalizeOnShutdown</tt>
 *
 * @see Application
 */
public interface Finalizable {

	public void finalize() throws Throwable;

}