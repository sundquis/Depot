/*
 * 
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.File;
import java.io.IOException;

import sundquis.core.Test.Decl;


/*
 * This should be reworked to use java.nio.Path
 */
public class LocalDir {

	/**
	 * The enumerated type for possible extensions.
	 */
	@Test.Skip
	public static enum Type {

		/** No system type defined. */
		PLAIN( "plain" ),

		/** File used to store configuration information */
		PROPERTY( "xml" ),

		/** Temporary file*/
		TEMPORARY( "tmp" ),

		/** File contains comma separated values */
		CSV( "csv" ),

		/** File contains generic binary data */
		DATA( "dat" ),

		/** File contains serialized objects */
		SERIAL( "ser" ),

		/** Text file */
		TEXT( "txt" ),

		/** Java source file */
		SRC( "java" ),

		/** HTML file */
		HTML( "html" ),
		
		/** An xml file */
		XML( "xml" );
		
		private String extension;
		
		Type( String ext ) {
			this.extension = "." + ext;
		}
		
		public String getExtension() {
			return this.extension;
		}
		
	}

	private File file;
	
	private boolean createMissingDirs;

	public LocalDir( boolean createMissingDirs ) {
		this.createMissingDirs = createMissingDirs;
		this.file = Assert.rwDirectory( App.get().root() );
	}
	
	public LocalDir() {
		this( true );
	}

	/**
	 * Append a subdirectory to the current directory path.
	 *
	 * @param subDir
	 *	    The non-empty name of the sub-directory to be appended.
	 *
	 * @return
	 *      This LocalDir instance.
	 */
	@Decl( "Appending null subdir throws assertion error")
	@Decl( "Appending empty subdir throws assertion error")
	public LocalDir sub( String subDir ) {
		Assert.nonEmpty( subDir );
		this.file = new File( this.file, subDir );
		if ( !this.file.exists() && this.createMissingDirs ) {
			this.file.mkdir();
		}

		Assert.readableDirectory( this.file );
		return this;
	}

	/**
	 * Retrieves the directory currently represented by this LocalDir
	 * 
	 * @return
	 *      The non-null File instance representing the readable directory
	 */
	@Decl( "Is directory" )
	@Decl( "Exists" )
	@Decl( "Is readable")
	public File getDir() {
		return this.file;
	}

	/**
	 * Retrieve the named file of the given type in the current directory.
	 */
	@Decl( "throws Assertion Error if name is null" )
	@Decl( "throws Assertion Error if name is empty" )
	@Decl( "throws Assertion Error if type is null" )
	public File getFile( String name, Type type ) {
		Assert.nonEmpty( name );
		Assert.nonNull( type );
		return new File( this.file, name + type.getExtension() );
	}
	
	/** Retrieve a temporary file that will be deleted when the JVM exits */
	@Decl( "Temporary file exists" )
	@Decl( "Temporary file is readable" )
	@Decl( "Temporary file is writeable" )
	@Decl( "Throws arretion error if prefix is null" )
	@Decl( "Throws arretion error if prefix is empty" )
	@Decl( "Throws arretion error if prefix is short" )
	public File getTmpFile( String prefix ) {
		Assert.nonEmpty( prefix );
		Assert.isTrue( prefix.length() >= 3 );
		
		File f = null;
		try {
			f = File.createTempFile( prefix, Type.TEMPORARY.getExtension(), this.file );
		} catch ( IOException e ) {
			Fatal.error( "Framework requires access to teporary files.", e );
		}
		f.deleteOnExit();
		
		return Assert.rwFile( f );
	}
	
	
	
	public static class Container implements TestContainer {
		@Override
		public Class<?> subjectClass() {
			return LocalDir.class;
		}
		
		@Test.Impl( src = "public File LocalDir.getDir()", desc = "Exists" )
		public void getDir_Exists( TestCase tc ) {
			tc.assertTrue( new LocalDir().getDir().exists() );
		}

		@Test.Impl( src = "public File LocalDir.getDir()", desc = "Is directory" )
		public void getDir_IsDirectory( TestCase tc ) {
			tc.assertTrue( new LocalDir().getDir().isDirectory() );
		}

		@Test.Impl( src = "public File LocalDir.getDir()", desc = "Is readable" )
		public void getDir_IsReadable( TestCase tc ) {
			tc.assertTrue( new LocalDir().getDir().canRead() );
		}

		@Test.Impl( src = "public File LocalDir.getFile(String, LocalDir.Type)", desc = "throws Assertion Error if name is null" )
		public void getFile_ThrowsAssertionErrorIfNameIsNull( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().getFile( null, LocalDir.Type.PLAIN );
		}

		@Test.Impl( src = "public File LocalDir.getFile(String, LocalDir.Type)", desc = "throws Assertion Error if name is empty" )
		public void getFile_ThrowsAssertionErrorIfNameIsEmpty( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().getFile( "", LocalDir.Type.PLAIN );
		}

		@Test.Impl( src = "public File LocalDir.getFile(String, LocalDir.Type)", desc = "throws Assertion Error if type is null" )
		public void getFile_ThrowsAssertionErrorIfTypeIsNull( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().getFile( "Foo", null );
		}
		
		@Test.Impl( src = "public File LocalDir.getTmpFile(String)", desc = "Temporary file exists" )
		public void getTmpFile_TemporaryFileExists( TestCase tc ) {
			tc.assertTrue( new LocalDir().sub( "tmp" ).getTmpFile( "Foo" ).exists() );
		}

		@Test.Impl( src = "public File LocalDir.getTmpFile(String)", desc = "Temporary file is readable" )
		public void getTmpFile_TemporaryFileIsReadable( TestCase tc ) {
			tc.assertTrue( new LocalDir().sub( "tmp" ).getTmpFile( "Foo" ).canRead() );
		}

		@Test.Impl( src = "public File LocalDir.getTmpFile(String)", desc = "Temporary file is writeable" )
		public void getTmpFile_TemporaryFileIsWriteable( TestCase tc ) {
			tc.assertTrue( new LocalDir().sub( "tmp" ).getTmpFile( "Foo" ).canWrite() );
		}

		@Test.Impl( src = "public File LocalDir.getTmpFile(String)", desc = "Throws arretion error if prefix is empty" )
		public void getTmpFile_ThrowsArretionErrorIfPrefixIsEmpty( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().sub( "tmp" ).getTmpFile( "" );
		}

		@Test.Impl( src = "public File LocalDir.getTmpFile(String)", desc = "Throws arretion error if prefix is short" )
		public void getTmpFile_ThrowsArretionErrorIfPrefixIsShort( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().sub( "tmp" ).getTmpFile( "AB" );
		}

		@Test.Impl( src = "public File LocalDir.getTmpFile(String)", desc = "Throws arretion error if prefix is null" )
		public void getTmpFile_ThrowsArretionErrorIfPrefixIsNull( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().sub( "tmp" ).getTmpFile( null );
		}

		@Test.Impl( src = "public LocalDir LocalDir.sub(String)", desc = "Appending empty subdir throws assertion error" )
		public void sub_AppendingEmptySubdirThrowsAssertionError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().sub( "" );
		}

		@Test.Impl( src = "public LocalDir LocalDir.sub(String)", desc = "Appending null subdir throws assertion error" )
		public void sub_AppendingNullSubdirThrowsAssertionError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new LocalDir().sub( null );
		}

		

	}
	
	
	
	public static void main(String[] args) {
		System.out.println();
		
		//Test.noWarnings();
		new Test( Container.class ).eval();
		Test.printResults();

		System.out.println("\nDone!");
	}
	
	
}
