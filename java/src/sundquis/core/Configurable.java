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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Configurable.java,v $
 * $Id: Configurable.java,v 1.2 2003/03/24 00:44:29 johnh Exp $
 * $Log: Configurable.java,v $
 * Revision 1.2  2003/03/24 00:44:29  johnh
 * tweaked comments
 *
 * Revision 1.1  2002/09/19 23:26:49  toms
 * Support for reading, writing, and processing xml configuration elements.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

/**
 * Classes that want to use the configuration framework to read/load
 * configuration information implement this interface. Values are
 * stored independently for each application. Classes load and save
 * configurations by calling
 * <TT>Configuration.loadSystemConfiguration(Configurable)</TT>
 * <TT>Configuration.loadClassConfiguration(Configurable)</TT> and
 * <TT>Configuration.writeClassConfiguration(Configurable)</TT>.  
 
 * loadSystemConfiguration is used to access the System configuration,
 * where as if an object has its own personal configuration (for
 * example, a GUI component that remembers state from invokation to
 * invokation) it uses loadClassConfiguration.

 */
public interface Configurable {
	
	/**
	 * Read and process the externally supplied configuration
	 * information.  This is the callback used when loading system
	 * or class configuration items.  
	 
	 */

	public void process( Configuration item ) throws ConfigurationException;
	
}
