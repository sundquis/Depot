/*

 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import sundquis.core.Test.Decl;

/**
 * @author sundquis
 *
 */
public class App implements Runnable {
	
	private static App instance = null;
	
	/** Retrieve the singleton instance */
	@Decl( "Is not null" )
	public static App get() {
		if ( App.instance == null ) {
			synchronized ( App.class ) {
				if ( App.instance == null ) {
					App.instance = new App();
				}
			}
		}
		return Assert.nonNull( App.instance );
	}

	private final File root;
	
	private final String description;
	
	private final String[] sourceDirs;
	
	private final List<OnShutdown> objectsForShutdown;
		
	private App() {
		String rootDirName = Property.get( "root", null, Property.STRING );
		Assert.nonEmpty( rootDirName );
		this.root = new File( rootDirName );
		Assert.rwDirectory( this.root );

		this.description = Property.get( "description",  "<none>",  Property.STRING );
		String[] DFLT_SRC = { this.root.toString() };
		this.sourceDirs = Property.get( "source.dirs", DFLT_SRC, Property.CSV );

		this.objectsForShutdown = new LinkedList<OnShutdown>();
		Runtime.getRuntime().addShutdownHook( new Thread( this ) );
	}

	/** Root directory for all application resources */
	@Decl( "Is not null" )
	@Decl( "Is readable" )
	@Decl( "Is wrieable" )
	public File root() {
		return this.root;
	}

	@Decl( "Is not empty" )
	@Decl( "Is not null" )
	public String description() {
		return this.description;
	}
	
	@Decl( "Is not empty" )
	@Decl( "Is not null" )
	public String[] sourceDirs() {
		return this.sourceDirs;
	}
	
	@Test.Decl( "Throws assertion error for null class" )
	@Test.Decl( "Throws App Excetion for missing source file" )
	@Test.Decl( "Returns non null" )
	@Test.Decl( "Returns readable" )
	@Test.Decl( "Returns container for nested class" )
	@Test.Decl( "Returns container for nested nested class" )
	@Test.Decl( "Returns container for local class" )
	@Test.Decl( "Returns container for anonymous class" )
	@Test.Decl( "Returns container for nested local class" )
	@Test.Decl( "Returns container for nested anonymous class" )
	public File sourceFile( Class<?> clazz ) {
		Assert.nonNull( clazz );

		Class<?> encl = clazz;
		while ( encl.getEnclosingClass() != null ) {
			encl = encl.getEnclosingClass();
		}

		String[] components = encl.getName().split( "\\." );
		components[components.length-1] += ".java"; 
		String[] dirs = this.sourceDirs();
		
		File result = null;
		int index = 0;
		while ( result == null && index < dirs.length ) {
			result = Paths.get( dirs[index],  components ).toFile();
			if ( ! result.exists() ) {
				result = null;
			}
			index++;
		}
		
		if ( result == null ) {
			Fatal.error( "No source file for " + clazz );
		}
		
		return result;
	}
	
	/** For objects that require clean-up before shutdown. */
	@Test.Skip
	public static interface OnShutdown {
		public void terminate();
	}

	/** Register for shutdown termination */
	@Decl( "Registers objects" )
	public void terminateOnShutdown( OnShutdown os ) {
		this.objectsForShutdown.add( Assert.nonNull( os ) );
	}

	/** @see java.lang.Runnable#run() */
	@Decl( "Calls terminate on shutdown" )
	@Override
	public void run() {
		for ( OnShutdown os : this.objectsForShutdown ) {
			try {
				os.terminate();
			} catch ( Throwable th ) {
				System.out.println( "Exception during shutdown: " + th.getMessage() );
				th.printStackTrace();
			}
		}
	}
	
	

	
	public static class Container implements TestContainer {
		
		@Override
		public Class<?> subjectClass() {
			return App.class;
		}
		
		public File mySource;
		
		public Procedure beforeAll() {
			return new Procedure() {
				public void call () {
					mySource = App.get().sourceFile( App.class );
				}
			};
		}

		
		@Test.Impl( src = "public App App.get()", desc = "Is not null" )
		public void get_IsNotNull( TestCase tc ) {
			tc.notNull( App.get() );
		}

		@Test.Impl( src = "public String App.description()", desc = "Is not empty" )
		public void description_IsNotEmpty( TestCase tc ) {
			tc.assertFalse( App.get().description().isEmpty() );
		}

		@Test.Impl( src = "public void App.run()", desc = "Calls terminate on shutdown" )
		public void run_CallsTerminateOnShutdown( TestCase tc ) {
			// TOGGLE
			/*
			class OS implements OnShutdown {
				int id;
				OS(int id) {this.id = id;}
				public void terminate() {
					System.out.println("Terminating [" + id + "] " + Thread.currentThread() );
				}
				
			};
			for ( int i = 0; i < 10; i++ ) {
				App.get().terminateOnShutdown( new OS(i) );
			}
			// */
			tc.pass();
		}

		@Test.Impl( src = "public void App.terminateOnShutdown(App.OnShutdown)", desc = "Registers objects" )
		public void terminateOnShutdown_RegistersObjects( TestCase tc ) {
			int before = App.get().objectsForShutdown.size();
			App.get().terminateOnShutdown( new OnShutdown(){public void terminate() {} } );
			int after = App.get().objectsForShutdown.size();
			tc.assertEqual( after - before, 1 );
		}
		
		@Test.Impl( src = "public File App.root()", desc = "Is not null" )
		public void root_IsNotNull( TestCase tc ) {
			tc.notNull( App.get().root() );
		}

		@Test.Impl( src = "public File App.root()", desc = "Is readable" )
		public void root_IsReadable( TestCase tc ) {
			tc.assertTrue( App.get().root().canRead() );
		}

		@Test.Impl( src = "public File App.root()", desc = "Is wrieable" )
		public void root_IsWrieable( TestCase tc ) {
			tc.assertTrue( App.get().root().canWrite() );
		}
		
		@Test.Impl( src = "public String App.description()", desc = "Is not null" )
		public void description_IsNotNull( TestCase tc ) {
			tc.notNull( App.get().description() );
		}

		@Test.Impl( src = "public String[] App.sourceDirs()", desc = "Is not empty" )
		public void sourceDirs_IsNotEmpty( TestCase tc ) {
			tc.assertTrue( App.get().sourceDirs().length > 0 );
		}

		@Test.Impl( src = "public String[] App.sourceDirs()", desc = "Is not null" )
		public void sourceDirs_IsNotNull( TestCase tc ) {
			tc.notNull( App.get().sourceDirs() );
		}
		
		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns container for anonymous class" )
		public void sourceFile_ReturnsContainerForAnonymousClass( TestCase tc ) {
			Object anon = new Object(){};
			tc.assertEqual( mySource, App.get().sourceFile( anon.getClass() ) );
		}

		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns container for local class" )
		public void sourceFile_ReturnsContainerForLocalClass( TestCase tc ) {
			class Local {}
			Local local = new Local();
			tc.assertEqual( mySource, App.get().sourceFile( local.getClass() ) );
		}

		public static class Nested {
			private class Inner {}
			
			private Inner inner = new Inner();
			
			private Object anon = new Object() {};
			
			private Object get() { return new Object(){}; }
		}
		
		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns container for nested anonymous class" )
		public void sourceFile_ReturnsContainerForNestedAnonymousClass( TestCase tc ) {
			Nested n = new Nested();
			tc.assertEqual( mySource, App.get().sourceFile( n.anon.getClass() ) );
		}

		@Test.Impl( 
			src = "public File App.sourceFile(Class)",
			desc = "Returns container for nested class" )
		public void sourceFile_ReturnsContainerForNestedClass( TestCase tc ) {
			Nested n = new Nested();
			tc.assertEqual( mySource, App.get().sourceFile( n.getClass() ) );
		}
		
		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns container for nested local class" )
		public void sourceFile_ReturnsContainerForNestedLocalClass( TestCase tc ) {
			Nested n = new Nested();
			tc.assertEqual( mySource, App.get().sourceFile( n.get().getClass() ) );
		}

		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns container for nested nested class" )
		public void sourceFile_ReturnsContainerForNestedNestedClass( TestCase tc ) {
			Nested n = new Nested();
			tc.assertEqual( mySource, App.get().sourceFile( n.inner.getClass() ) );
		}

		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns non null" )
		public void sourceFile_ReturnsNonNull( TestCase tc ) {
			tc.notNull( App.get().sourceFile( Property.class ) );
		}
					
		@Test.Impl( 
			src = "public File App.sourceFile(Class)", 
			desc = "Returns readable" )
		public void sourceFile_ReturnsReadable( TestCase tc ) {
			tc.assertTrue( App.get().sourceFile( ByteFile.class ).canRead() );
		}

		@Test.Impl( src = "public File App.sourceFile(Class)", desc = "Throws App Excetion for missing source file" )
		public void sourceFile_ThrowsAppExcetionForMissingSourceFile( TestCase tc ) {
			tc.expectError( AppException.class );
			App.get().sourceFile( Object.class );
		}

		@Test.Impl( src = "public File App.sourceFile(Class)", desc = "Throws assertion error for null class" )
		public void sourceFile_ThrowsAssertionErrorForNullClass( TestCase tc ) {
			tc.expectError( AssertionError.class );
			App.get().sourceFile( null );
		}
					
					
					
					
					
	
	}

	public static void main(String[] args) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		
		System.out.println("\nDone!");
	}

	
}
