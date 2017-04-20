/*
 * Copyright(c) 2004 by Thomas Sundquist.
 * All rights reserved.
 *
 * This source code, and its documentation, are the confidential intellectual
 * property of Thomas Sundquist and may not be disclosed, reproduced,
 * distributed, or otherwise used for any purpose without the expressed
 * written permission of Thomas Sundquist.
 *
 * Application.java
 *
 * Created on August 9, 2004, 1:39 PM
 */

package sundq.core;

import java.io.*;
import java.util.*;

public final class Application {
	
	// Singleton instance, lazy instantiation
	private static Application instance = null;

	// System properties
	private Properties systemProperties = null;
	// Relative root dir for all application files
	private File root = null;
	// For implementation of Finalizable
	private Stack objectsToFinalize = null;
	
	private Application() {
		this.loadProperties();
		Runtime.getRuntime().addShutdownHook( new Finalizer() );
	}
	
	public static Application getInstance() {
		if ( Application.instance == null ) {
			synchronized ( Application.class ) {
				if ( Application.instance == null ) {
					Application.instance = new Application();
				}
			}
		}
		
		assert Application.instance != null;
		return Application.instance;
	}
	
	public File getRootDir() {
		if ( this.root == null ) {
			synchronized ( this ) {
				if ( this.root == null ) {
					this.root = this.buildRoot();
				}
			}
		}
		
		assert this.root != null;
		return this.root;
	}
	
	public String getProperty( String name, String dflt ) {
		return this.systemProperties.getProperty( name, dflt );
	}
	
	/**
	 * Registers the given object for finalization. When the JVM is terminated
	 * best effort will be made to run the finalizers of all registered objects.
	 *
	 * @param object
	 *      A non-null finalizable object.
	 */
	public synchronized void finalizeOnShutdown( Finalizable object ) {
		assert object != null;

		if ( this.objectsToFinalize == null ) {
			this.objectsToFinalize = new Stack();
		}
		this.objectsToFinalize.add( object );
	}

	// Very order-sensitive!
	private File buildRoot() {
		String path = null;
		
		if ( path == null ) {
			path = this.systemProperties.getProperty( "application.home" );
		}
		
		if ( path == null ) {
			path = this.systemProperties.getProperty( "user.dir" );
		}
		
		if ( path == null ) {
			path =".";
		}

		File root = new File( path );
		assert root.exists() : "Root does not exist: " + root;
		assert root.canWrite() : "Must have write access to root: " + root;
		return root;
	}
	
	public void reload() {
		Application.instance = null;
	}
	
	private void loadProperties() {
		this.systemProperties = (Properties) System.getProperties().clone();
	}
	
	// Thread that handles finalization of all registered objetcs.
	private class Finalizer extends Thread {

		private Finalizer() {}

		public void run() {
			while ( objectsToFinalize != null && !objectsToFinalize.isEmpty() ) {
				try {
					((Finalizable) objectsToFinalize.pop()).finalize();
				} catch ( Throwable ex ) {
					System.err.println("Application.Finalizer.run: Caught Exception " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		}

	}
	
}
