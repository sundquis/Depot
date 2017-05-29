/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sundquis.core.Test;


/**
 * Strings represented by soft references to byte arrays.
 * 
 * NOT thread safe
 */
public class SoftString implements Comparable<SoftString> {

	// Where the persistent data resides
	private static class Location {

		private static final List<ByteFile> BYTE_FILES = new ArrayList<ByteFile>();
		
		private final int fileIndex;
		private final int offset;
		private final int length;
		
		private Location( String s ) {
			byte[] data = s.getBytes();
			
			ByteFile file = null;
			int i = 0;
			while ( file == null && i < BYTE_FILES.size() ) {
				if ( BYTE_FILES.get( i ).canAppend( data.length ) ) {
					file = BYTE_FILES.get( i );
				} else {
					i++;
				}
			}
			if ( file == null ) {
				file = new ByteFile();
				BYTE_FILES.add( file );
			}
			
			this.fileIndex = i;
			this.offset = file.add( data );
			this.length = data.length;
		}
		
		private String get() {
			return new String( BYTE_FILES.get( this.fileIndex ).read( this.offset, this.length ) );
		}
	}

	
	// Configurable minimum string length for soft references
	private static Integer THRESHOLD = Property.get( "threshold", 50, Property.INTEGER );

	
	private final Location location;
	
	// Prevents GC if not null
	@SuppressWarnings("unused")
	private final String hard;
	
	private SoftReference<String> ref;

	/**
	 * Create string reference; if the length is less than {@code THRESHOLD} use a
	 * hard reference, otherwise use a soft reference backed by disk storage.
	 *  
	 * @param s
	 * 		The string to represent
	 * @param threshold
	 * 		Strings this length or longer are held via a soft reference and can be GC'd
	 */
	@Test.Decl( "Throws assertion error for null strings" )
	@Test.Decl( "Can construct empty" )
	@Test.Decl( "Can construct short strings" )
	@Test.Decl( "Can construct long strings" )
	public SoftString( String s ) {
		Assert.nonNull( s );
		
		if ( s.length() < SoftString.THRESHOLD ) {
			this.location = null;
			this.hard = s;
		} else {
			this.location = new Location( s );
			this.hard = null;
		}
		this.ref = new SoftReference<String>( s );
	}
	
	@Override
	@Test.Decl( "Stress test correct value" )
	public String toString() {
		String result = ref.get();
		if ( result == null ) {
			result = this.location.get();
			this.ref = new SoftReference<String>( result );
		}
		
		return result;
	}
	
	@Override
	@Test.Decl( "Can sort large collections" )
	public int compareTo( SoftString other ) {
		return this.toString().compareTo( other.toString() );
	}

	
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return SoftString.class;
		}
		
		private SoftString soft;
		
		private int ORIG_THRESHOLD;
		
		@Override
		public Procedure beforeAll() {
			return new Procedure() {
				public void call() {
					ORIG_THRESHOLD = SoftString.THRESHOLD;
					SoftString.THRESHOLD = 5;
				}
			};
		}
		
		@Override
		public Procedure afterAll() {
			return new Procedure() {
				public void call() {
					SoftString.THRESHOLD = ORIG_THRESHOLD;
				}
			};
		}
		
		@Override
		public Procedure afterEach() {
			return new Procedure() {
				public void call() {
					soft = null;
				}
			};
		}
		
		private static class SoftStringAdapter extends SoftString {
			
			private SoftString mySoftString;
			
			public SoftStringAdapter( SoftString ss ) {
				super("");
				this.mySoftString = ss;
			}

			@Override
			public String toString() {
				// Force retrieval from ByteFile location
				if ( this.mySoftString.location != null ) {
					this.mySoftString.ref = new SoftReference<String>( null );
				}
				return this.mySoftString.toString();
			}

			@Override
			public int compareTo( SoftString other ) {
				return this.mySoftString.compareTo( other );
			}
						
		}

		@Test.Impl( src = "public SoftString(String)", desc = "Can construct empty" )
		public void SoftString_CanConstructEmpty( TestCase tc ) {
			soft = new SoftString( "" );
			tc.assertEqual( "",  soft.toString() );
		}

		@Test.Impl( src = "public SoftString(String)", desc = "Can construct long strings" )
		public void SoftString_CanConstructLongStrings( TestCase tc ) {
			String arg = Strings.rightJustify( "42",  SoftString.THRESHOLD,  '.' );
			soft = new SoftString( arg );
			tc.assertEqual( arg,  soft.toString() );
		}

		@Test.Impl( src = "public SoftString(String)", desc = "Can construct short strings" )
		public void SoftString_CanConstructShortStrings( TestCase tc ) {
			String arg = "A short string";
			soft = new SoftString( arg );
			tc.assertEqual( arg,  soft.toString() );
		}

		@Test.Impl( src = "public SoftString(String)", desc = "Throws assertion error for null strings" )
		public void SoftString_ThrowsAssertionErrorForNullStrings( TestCase tc ) {
			tc.expectError( AssertionError.class );
			soft = new SoftString( null );
		}
		
		@Test.Impl( src = "public String SoftString.toString()", desc = "Stress test correct value", weight = 10 )
		public void toString_StressTestCorrectValue( TestCase tc ) 
				throws FileNotFoundException, IOException {
			
			// find a really big file...
			// find . -type f -printf '%s %p\n' | sort -nr | less
			String sourceDir = App.get().sourceDirs()[0].toString();
			File bigSourceFile = Paths.get( sourceDir, "sundquis", "core", "ByteFile.java" ).toFile();
			
			List<SoftString> strings = new LinkedList<SoftString>();
			String line;
			int stressMultiplicty = 10;

			for ( int i = 0; i < stressMultiplicty; i ++ ) {
				try ( BufferedReader in = new BufferedReader( new FileReader( bigSourceFile) ) ) {
					while ( (line = in.readLine()) != null ) {
						strings.add( new SoftStringAdapter( new SoftString( line ) ) );
					}
				}
			}

			boolean match = true;
			Iterator<SoftString> iter = strings.iterator();
			for ( int i = 0; i < stressMultiplicty; i ++ ) {
				try ( BufferedReader in = new BufferedReader( new FileReader( bigSourceFile) ) ) {
					while ( (line = in.readLine()) != null ) {
						match &= iter.next().toString().equals( line );
					}
				}
			}
			match &= !iter.hasNext();

			tc.assertTrue( match );
		}

		@Test.Impl( src = "public int SoftString.compareTo(SoftString)", desc = "Can sort large collections" )
		public void compareTo_CanSortLargeCollections( TestCase tc ) throws FileNotFoundException, IOException {
			
			// find a really big file...
			// find . -type f -printf '%s %p\n' | sort -nr | less
			File bigSourceFile = App.get().sourceFile( ByteFile.class );
			
			List<SoftString> strings = new LinkedList<SoftString>();
			String line;
			int stressMultiplicty = 10;

			for ( int i = 0; i < stressMultiplicty; i ++ ) {
				try ( BufferedReader in = new BufferedReader( new FileReader( bigSourceFile) ) ) {
					while ( (line = in.readLine()) != null ) {
						strings.add( new SoftStringAdapter( new SoftString( line ) ) );
					}
				}
			}
			
			Collections.sort( strings );
			Collections.reverse( strings );
			
			boolean match = true;
			for ( int i = 0; i < stressMultiplicty; i++ ) {
				match &= strings.get( i ).toString().equals( "}" ); 
			}
			
			tc.assertTrue( match );
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
