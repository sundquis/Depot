/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import sundquis.core.App;
import sundquis.core.Assert;
import sundquis.core.TestContainer;
import sundquis.core.Test;

/**
 * @author sundquis
 *
 */
@Test.Skip
public class TestRunner {

		
	// All packages in the named source directory
	public static void testSourceDir( String sourceDirName ) {
		Assert.nonEmpty( sourceDirName );
		
		File root = new File( sourceDirName );
		List<File> dirs = new ArrayList<File>();

		findDirs( root, dirs );
		
		for ( File file : dirs ) {
			String packageName = "";
			for ( Path comp : root.toPath().relativize( file.toPath() ) ) {
				packageName += "." + comp;
			}
			testPackage( sourceDirName, packageName.substring(1) );
		}
	}


	private static void findDirs( File curDir, List<File> dirs ) {
		Assert.rwDirectory( curDir );
		Assert.nonNull( dirs );
		
		File[] subDirs = curDir.listFiles( (f) -> f.isDirectory() );
		for ( File file : subDirs ) {
			dirs.add( file );
			findDirs( file, dirs );
		}
	}

	
	// All classes in the named package relative to any directory in SRC_DIRS
	public static void testPackage( String packageName ) {
		Assert.nonEmpty( packageName );
		
		for ( String sourceDirName : App.get().sourceDirs() ) {
			testPackage( sourceDirName, packageName );
		}
	}

	
	// All classes in the named package relative to the named directory
	public static void testPackage( String sourceDirName, String packageName ) {
		Assert.nonEmpty( sourceDirName );
		Assert.nonEmpty( packageName );
		
		File dir = Paths.get( sourceDirName, packageName.split( "\\." ) ).toFile();
		if ( dir.exists() ) {
			File[] sourceFiles = dir.listFiles( (d, n)  -> n.endsWith("java"));
			for ( File f : sourceFiles ) {
				testClass( packageName + "." + f.getName().replace( ".java",  "") );
			}
		}
	}

	
	// Tests for named class, if its a container, or all inner class containers
	@SuppressWarnings("unchecked")
	public static void testClass( String className ) {
		Assert.nonEmpty( className );
		
		Class<?> clazz = null;
		List<Class<? extends TestContainer>> containers = new ArrayList<Class<? extends TestContainer>>();  
		try {
			clazz = ClassLoader.getSystemClassLoader().loadClass( className );
			if ( TestContainer.class.isAssignableFrom( clazz ) && ! clazz.isInterface() ) {
				containers.add( (Class<? extends TestContainer>) clazz );
			}
			Class<?>[] classes = clazz.getDeclaredClasses();
			for ( Class<?> c : classes ) {
				if ( TestContainer.class.isAssignableFrom( c ) && ! c.isInterface() ) {
					containers.add( (Class<? extends TestContainer>) c );
				}
			}
		} catch ( ClassNotFoundException ex ) {
			ex.printStackTrace();
		}
		
		if ( containers.isEmpty() ) {
			if ( clazz.getAnnotation( Test.Skip.class ) == null ) {
				Test.addWarning( "No container found for " + className );
			}
		}

		for ( Class<? extends TestContainer> c : containers ) {
			new Test( c ).eval();
		}
	}
	
	

	
	
	
	

	public static void main(String[] args) {

		System.out.println();
		
		Test.summaryOnly();
		//Test.noWarnings();
		//Test.noStubs();
		
		//testPackage( "sundquis.core" );
		testSourceDir( "/home/sundquis/book/Dropbox/java/projects/DEV/devsrc" );

		Test.printResults();

		System.out.println("\nDone!");

	}
}
