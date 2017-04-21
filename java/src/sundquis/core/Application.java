package com.circus.core;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

/**
 * A singleton used by components to get information about the runtime
 * environment and the application configuration and used to interact
 * with the application environment.
 * <P>
 * This abstract class relies on the user defined property
 * <tt>application.classname</tt> to determine a concrete subclass.
 * That subclass contains the application specific configuration
 * information. It is also assumed that the <tt>application.home</tt>
 * is set to the root of the source code tree.
 */
public class Application {

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	public static class TEST extends Test {

		public static void main( String[] args ) {
			TEST t = new TEST();
			t.run();
			t.testSummary();
		}

		public void run() {
			try {
				script();
			} catch ( Throwable ex ) {
				this.testFail( "Unexpected exception", ex );
				ex.printStackTrace();
			}
		}

		public void script() throws Throwable {
			this.testCase( "Application",  Application.getInstance().getClass(), Application.class );
			this.testEval( "Root", Application.getInstance().getRootDir() );
			this.testEval( "ConfigDir", Application.getInstance().getConfigDir() );
		}

	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { Application.TEST.main( args ); }

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */
	
	/**
	 * Singleton instance.
	 */
	private static Application instance = null;
	
	/**
	 * The root directory, required for all applications and determined
	 * by the property <tt>application.home</tt>
	 * It should be the root directory of the circus cvsroot.
	 */
	private File rootDir = null;
	
	/**
	 * Holds the collection of objects that have registered for finalization.
	 */
	private Set objectsToFinalize = null;

	/**
	 * Gets the singleton <tt>Application</tt> instance.
	 *
	 * @return
	 *      The non-null Application instance.
	 */
	public static Application getInstance() {
		/*
		 * makeInstance is synchronized using double-checked locking
		 * to avoid the race condition associated with lazy instantiation.
		 */
		if ( Application.instance == null ) {
			synchronized ( Application.class ) {
				if ( Application.instance == null ) {
					Application.instance = new Application();
				}
			}
		}

		assert Application.instance != null;
		return instance;
	}

	/**
	 * Constructor. All <tt>Application</tt> instances read the property
	 * <tt>application.home</tt> to find the root directory. This should
	 * be the root of the circus cvs tree. The property <tt>configuration.home</tt>
	 * is used to find the config.xml file.
	 *
	 * @throws FatalException
	 *      Thrown if the <tt>application.home</tt> property is not defined
	 *      or if the home directory is missing or inaccessible.
	 */
	protected Application() {
		String rootDirName = System.getProperty( "application.home" );
		if ( Strings.isEmpty( rootDirName ) ) {
			rootDirName = System.getProperty( "CIRCUS_HOME", "" );
		}
		this.rootDir = new File( rootDirName );
		if ( !this.rootDir.exists() ) {
			Fatal.error( "Missing home directory: " + rootDirName );
		}
		if ( !this.rootDir.canRead() || !this.rootDir.canWrite() ) {
			Fatal.error( "Home directory inaccessible: " + rootDirName );
		}
		
		Runtime.getRuntime().addShutdownHook( new Finalizer() );
	}

	/**
	 * Convenience method that reads a system property and asserts that the
	 * value is not null.
	 *
	 * @param key
	 *      The non-empty string name of the property.
	 *
	 * @return
	 *      The non-empty string value of the property.
	 *
	 * @throws FatalException
	 *      Thrown if the property is not defined.
	 */
	protected static String getProp( String key ) {
		assert Strings.isNotEmpty( key );

		String prop = System.getProperty( key );
		if ( prop == null ) {
			Fatal.error( "Undefined property: " + key );
		}

		assert Strings.isNotEmpty( prop );
		return prop;
	}

	/**
	 * @return
	 *      The non-null file object representing the application's home
	 *      directory.
	 */
	public File getRootDir() {
		assert this.rootDir != null;
		assert this.rootDir.canWrite();
		
		return this.rootDir;
	}
	
	/**
	 * A directory for application specific configuration information
	 *		<rootDir>/home/<configHome>
	 * where <rootDir> is the directory described above and <configHome>
	 * is the value of the <tt>configuration.home</tt> property.
	 */
	public synchronized File getConfigDir() {
		String configDirName = System.getProperty( "configuration.home" );
		if ( Strings.isEmpty( configDirName ) ) {
			configDirName = System.getProperty( "CIRCUS_CONFIG", "" );
		}
		
		// Allow relative paths in configuration.home
		List components = new ArrayList();
		components.add( "home" );
		components.addAll( Arrays.asList( configDirName.split( "/" ) ) );
		
		File configDir = new LocalFile( (String[]) components.toArray( new String[]{} ),
			LocalFile.Type.DIR ).getFile();
		
		if ( !configDir.exists() && !configDir.mkdirs() ) {
			Fatal.error( "Missing config directory: " + configDirName );
		}
		if ( !configDir.canRead() || !configDir.canWrite() ) {
			Fatal.error( "Config directory inaccessible: " + configDirName );
		}
		
		return configDir;
	}


	/**
	 * Registers the given object for finalization. When the JVM is terminated
	 * best effort will be made to run the finalizers of all registered objects.
	 *
	 * @param object
	 *      A non-null finalizable object.
	 */
	public synchronized void finalizeOnShutdown( Finalizable object ) {
		Contract.cAssert( object != null );

		if ( this.objectsToFinalize == null ) {
			this.objectsToFinalize = new HashSet();
		}
		this.objectsToFinalize.add( object );
	}

	private class Finalizer extends Thread {

		private Finalizer() {}

		public void run() {
			synchronized (Application.this) {
				if ( Application.this.objectsToFinalize != null ) {
					Iterator i = Application.this.objectsToFinalize.iterator();
					while ( i.hasNext() ) {
						try {
							Finalizable fin = ((Finalizable) i.next());
							fin.finalize();
						} catch ( Throwable ex ) {
							System.out.println("Application.Finalizer.run: Caught Exception " + ex.getMessage());
							ex.printStackTrace();
						}
					}
				}
			}
		}

	}

}
