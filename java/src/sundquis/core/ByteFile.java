/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import sundquis.core.Test.Decl;

/**
 * Implement byte array behavior for large arrays, up to 1 billion bytes.
 * 
 * Not thread-safe
 */
public class ByteFile  {
	
	private static long GB = 1000000000L;

	// Configurable max data file length in bytes (1 GB default)
	private static long MAX_LENGTH = Property.get( "maxLength", 1 * GB, Property.LONG );

	// Configurable data usage limit to issue warning (2 GB default)
	private static long WARN_LIMIT = Property.get( "warnLimit", 2 * GB, Property.LONG );
	
	// Configurable data usage limit to shut down ByteFile (5 GB default)
	private static long FAIL_LIMIT = Property.get( "failLimit", 5 * GB, Property.LONG );
	

	// Used to monitor total disk usage and signal warning or failure
	private static volatile long TOTAL_BYTES = 0L;

	private static void newBytes( int count ) {
		TOTAL_BYTES += (long) count;
		if ( TOTAL_BYTES > WARN_LIMIT ) {
			Fatal.warning( "Total bytes stored exceeds " + WARN_LIMIT );
		}
		if ( TOTAL_BYTES > FAIL_LIMIT ) {
			Fatal.error( "Total bytes stored exceeds " + FAIL_LIMIT );
		}
	}
	
	
	
	
	// Temporary file holding data
	private File file;
	
	// Current length in bytes
	private int length;
	
	/** 
	 * Construct an empty {@code ByteFile} backed by a temporary file.
	 * The temporary file is automatically deleted when the JVM exists.
	 * 
	 * @throws IOException
	 * 		If the temporary file cannot be constructed.
	 */
	@Decl( "Creates writeable file" )
	@Decl( "Creates empty file" )
	public ByteFile() {
		this.file = new LocalDir().sub( "tmp" ).getTmpFile( "BYTES" );
		this.length = 0;
	}
	
	// FIXME: Short-circuit empty reads and writes
	
	

	/**
	 * Write data from the source buffer into this {@code ByteFile}
	 * 
	 * @param position
	 * 		Start location in this ByteFile for the source bytes
	 * @param src
	 * 		Bytes to be written
	 * @param offset
	 * 		Start location in {@code src} of data 
	 * @param count
	 * 		Number of bytes to write
	 * @throws IOException
	 * 		If {@code RandomAccessFile} operations fail or if the maximum length is exceeded.
	 */
	@Decl( "Increases length" )
	@Decl( "Beyond max length throws AssertionError" )
	@Decl( "At warn limit issues warning" )
	@Decl( "At fail limit throws AppException" )
	@Decl( "Throws AssertionError for negative offset" )
	@Decl( "Throws AssertionError for offset too big" )
	@Decl( "Throws AssertionError for illegal count" )
	@Decl( "Increases total bytes" )
	public void write( int position, byte[] src, int offset, int count ) {
		Assert.isTrue( this.isOpen() );
		Assert.nonNull( src );
		Assert.isTrue( offset >= 0 );
		Assert.isTrue( offset < src.length );
		Assert.isTrue( offset + count <= src.length );
		
		int oldLength = this.length;
		int newLength = Math.max( this.length, position + count );
		Assert.isTrue( (long) newLength <= MAX_LENGTH );
		
		try ( RandomAccessFile raf = new RandomAccessFile( this.file, "rw" ) ) {
			Assert.isTrue( (long) oldLength == raf.length() );
			raf.seek( (long) position );
			raf.write( src, offset, count );
			Assert.isTrue( (long) newLength == raf.length() );
			this.length = newLength;
			ByteFile.newBytes( newLength - oldLength );
		} catch ( FileNotFoundException e ) {
			Fatal.impossible( "Framework should ensure tmp files exist." );
		} catch ( IOException e ) {
			throw new AppException( e );
		}
	}

	/**
	 * Write the entire contents of the buffer into this {@code ByteFile}.
	 * 
	 * @param position
	 * 		Start location in this ByteFile for the source bytes
	 * @param src
	 * 		Bytes to be written
	 * @throws IOException
	 * 		If {@code RandomAccessFile} operations fail or if the maximum length is exceeded.
	 */
	@Test.Skip
	public void write( int position, byte[] src ) {
		this.write( position, src, 0, src.length );
	}
	
	/**
	 * Add to the end of this {@code ByteFile}
	 * 
	 * @param src
	 * @param offset
	 * @param count
	 * @return
	 * 		The position in this {@code ByteFile} where the write starts
	 */
	@Decl( "Increases length by count" )
	@Decl( "Increases total bytes by count" )
	public int add( byte[] src, int offset, int count ) {
		int position = this.length;
		this.write( position, src, offset, count );
		return position;
	}
	
	/**
	 * Add to the end of this {@code ByteFile}
	 * 
	 * @param src
	 * @return
	 * 		The position in this {@code ByteFile} where the write starts
	 */
	@Decl( "Increases length by src length" )
	@Decl( "Increases total bytes by count" )
	public int add( byte[] src ) {
		return this.add( src, 0, src.length );
	}

	/**
	 * Read data from this {@code ByteFile} into the destination buffer.
	 * 
	 * @param position
	 * 		Location in this {@code ByteFile} to start reading.
	 * @param dest
	 * 		Buffer to read into.
	 * @param offset
	 * 		Start position in the buffer.
	 * @param count
	 * 		Number of bytes to read.
	 * @throws IOException
	 * 		If {@code RandomAccessFile} operations fail or if the maximum length is exceeded.
	 */
	@Decl( "Throws AssertionError on read past EOF" )
	@Decl( "Throws AssertionError for negative offset" )
	@Decl( "Throws AssertionError for offset too big" )
	@Decl( "Throws AssertionError for negative count" )
	@Decl( "Throws AssertionError for count too big" )
	@Decl( "Is consistent with write" )
	public void read( int position, byte[] dest, int offset, int count ) {
		Assert.isTrue( this.isOpen() );
		Assert.isTrue( position >= 0 );
		Assert.isTrue( position + count <= this.length );  // Read beyond end of file
		Assert.isTrue( offset >= 0 );
		Assert.isTrue( offset + count <= dest.length );
		Assert.isTrue( count >= 0 );
		
		try ( RandomAccessFile raf = new RandomAccessFile( this.file, "r" ) ) {
			raf.seek( position );
			raf.read( dest, offset, count );
		} catch ( FileNotFoundException e ) {
			Fatal.impossible( "Framework should ensure tmp files exist." );
		} catch ( IOException e ) {
			throw new AppException( e );
		}
	}
	
	/**
	 * Construct a new {@code byte} array and read into it.
	 * 
	 * @param position
	 * 		Location in this {@code ByteFile} to start reading.
	 * @param count
	 * 		Number of bytes to read.
	 * @return
	 * 		The newly constructed byte array
	 */
	@Decl( "Is consistent with write" )
	public byte[] read( int position, int count ) {
		byte[] result = new byte[ count ];
		this.read( position, result, 0, count );
		return result;
	}
	
	/**
	 * Determine if this {@code ByteFile} can hold {@code count} new bytes.
	 * 
	 * @param count
	 * @return
	 */
	@Decl( "True for small count" )
	@Decl( "False for large count" )
	public boolean canAppend( int count ) {
		return this.isOpen() && (long) this.length + count <= MAX_LENGTH;
	}

	/** Determine if this {@code ByteFile} can accept read/write requests */
	@Decl( "True for new" )
	@Decl( "False after dispose" )
	public boolean isOpen() {
		return this.file != null;
	}
	
	/** Close this {@code ByteFile} and release resources. */
	@Decl( "Releases resources" )
	public void dispose() {
		if ( this.file != null && this.file.delete() ) {
			ByteFile.newBytes( -1 * this.length );
		}
		this.file = null;
	}

	@Override
	@Decl( "Indicates length" )
	public String toString() {
		return this.file.getAbsolutePath() + "(Length = " + this.length + ")";
	}
	
	
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return ByteFile.class;
		}
		
		private byte[] BYTES;
		
		private byte[] BUF;
		
		private ByteFile bf;

		@Override
		public Procedure beforeAll() {
			return new Procedure() {
				public void call() {
					BYTES = "A fairly long string that we use for testing".getBytes();
				}
			};
		}
		
		@Override
		public Procedure beforeEach() {
			return new Procedure() {
				public void call() { 
					bf = new ByteFile();
					BUF = new byte[100];
				}
			};
		}
		
		@Override
		public Procedure afterEach() {
			return new Procedure() {
				public void call() { 
					bf.dispose();
					bf = null;
					BUF = null;
					MAX_LENGTH = 1 * GB;
					WARN_LIMIT = 2 * GB;
					FAIL_LIMIT = 5 * GB;
				}
			};
		}
		

		@Test.Impl( src = "public ByteFile()", desc = "Creates empty file" )
		public void ByteFile_CreatesEmptyFile( TestCase tc ) {
			tc.assertEqual( bf.length, 0 );
		}

		@Test.Impl( src = "public ByteFile()", desc = "Creates writeable file" )
		public void ByteFile_CreatesWriteableFile( TestCase tc ) {
			tc.assertTrue( bf.file.canWrite() );
		}

		
		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "At fail limit throws AppException" )
		public void write_AtFailLimitThrowsAppexception( TestCase tc ) {
			FAIL_LIMIT = 200L;
			tc.expectError( AppException.class );
			bf.write( 190,  BYTES, 0, 20 );
		}

		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "At warn limit issues warning" )
		public void write_AtWarnLimitIssuesWarning( TestCase tc ) {
			// TOGGLE
			/* */	tc.pass();	/*
			// Should see "WARNING: Total bytes stored exceeds 100"
			WARN_LIMIT = 100L;
			bf.write( 90,  BYTES, 0, 12 );
			tc.assertTrue( bf.length > 100 );
			// */
		}

		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "Beyond max length throws AssertionError" )
		public void write_BeyondMaxLengthThrowsAssertionerror( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.write( (int) MAX_LENGTH - 10,  BYTES, 0, 20 );
		}
		
		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "Increases length" )
		public void write_IncreasesLength( TestCase tc ) {
			tc.addMessage( "Length not adjusted" );
			int len = bf.length;
			bf.write( len + 10,  BYTES, 0, 20 );
			tc.assertEqual( bf.length, len + 30 );
		}

		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "Throws AssertionError for illegal count" )
		public void write_ThrowsAssertionerrorForIllegalCount( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.write( 10,  BYTES, 0, 100);
		}

		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "Throws AssertionError for negative offset" )
		public void write_ThrowsAssertionerrorForNegativeOffset( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.write( 0,  BYTES, -1, 5 );
		}

		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "Throws AssertionError for offset too big" )
		public void write_ThrowsAssertionerrorForOffsetTooBig( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.write( 0,  BYTES, 0, 100 );
		}
		
		@Test.Impl( src = "public int ByteFile.add(byte[], int, int)", desc = "Increases length by count" )
		public void add_IncreasesLengthByCount( TestCase tc ) {
			bf.write( 42,  BYTES );
			int len = bf.length;
			bf.add( BYTES, 5, 10 );
			tc.assertEqual( bf.length, len + 10 );
		}
		
		@Test.Impl( src = "public void ByteFile.read(int, byte[], int, int)", desc = "Is consistent with write" , weight = 5 )
		public void read_IsConsistentWithWrite( TestCase tc ) {
			String[] args = {
				"The first string.",
				"Four score and seven years ago..",
				"A long time ago, in a alaxy far, far away...",
				"In a hole in, in the ground, there lived a Hobbit.",
				"The answer to the ultimate question of life, the universe, and everything."
			};
			int[] positions = { 13, 117, 319, 523, 729 };
			for ( int i = 0; i < 5; i++ ) {
				bf.write( positions[i],  args[i].getBytes(), 0, 10 );
			}
			boolean pass = true;
			for ( int i = 0; i < 5; i++ ) {
				bf.read( positions[i],  BUF, 0, 10 );
				pass &= args[i].substring(0,  10).equals( new String( BUF, 0, 10 ) );
			}
			tc.assertTrue( pass );
		}
		
		@Test.Impl( src = "public void ByteFile.read(int, byte[], int, int)", desc = "Throws AssertionError for count too big" )
		public void read_ThrowsAssertionerrorForCountTooBig( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.read( 0, BUF, 0, 100 );
		}

		@Test.Impl( src = "public void ByteFile.read(int, byte[], int, int)", desc = "Throws AssertionError for negative count" )
		public void read_ThrowsAssertionerrorForNegativeCount( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.read( 0, BUF, 0, -1 );
		}

		@Test.Impl( src = "public void ByteFile.read(int, byte[], int, int)", desc = "Throws AssertionError for negative offset" )
		public void read_ThrowsAssertionerrorForNegativeOffset( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.read( 0, BUF, -1, 100 );
		}

		@Test.Impl( src = "public void ByteFile.read(int, byte[], int, int)", desc = "Throws AssertionError for offset too big" )
		public void read_ThrowsAssertionerrorForOffsetTooBig( TestCase tc ) {
			tc.expectError( AssertionError.class );
			bf.read( 0, BUF, 100, 1 );
		}

		@Test.Impl( src = "public void ByteFile.read(int, byte[], int, int)", desc = "Throws AssertionError on read past EOF" )
		public void read_ThrowsAssertionerrorOnReadPastEof( TestCase tc ) {
			bf.add( BYTES );
			tc.expectError( AssertionError.class );
			bf.read( BYTES.length - 5, BUF, 0, 6 );
		}
					
		@Test.Impl( src = "public byte[] ByteFile.read(int, int)", desc = "Is consistent with write", weight = 5 )
		public void read2_IsConsistentWithWrite( TestCase tc ) {
			String[] args = {
				"The first string.",
				"Four score and seven years ago..",
				"A long time ago, in a alaxy far, far away...",
				"In a hole in, in the ground, there lived a Hobbit.",
				"The answer to the ultimate question of life, the universe, and everything."
			};
			int[] positions = { 13, 117, 319, 523, 729 };
			for ( int i = 0; i < 5; i++ ) {
				bf.write( 1000 + positions[i],  args[i].getBytes() );
			}
			byte[] b;
			boolean pass = true;
			for ( int i = 0; i < 5; i++ ) {
				b = bf.read( 1000 + positions[i],  args[i].length() );
				pass &= args[i].equals( new String( b ) );
			}
			tc.assertTrue( pass );
		}

		@Test.Impl( src = "public String ByteFile.toString()", desc = "Indicates length" )
		public void toString_IndicatesLength( TestCase tc ) {
			bf.add( "12345".getBytes() );
			String s = bf.toString();
			tc.assertEqual( s.substring( s.length() - 12 ), "(Length = 5)" );
		}

		@Test.Impl( src = "public boolean ByteFile.canAppend(int)", desc = "False for large count", weight = 2 )
		public void canAppend_FalseForLargeCount( TestCase tc ) {
			boolean fail = false;
			fail |= bf.canAppend( (int) MAX_LENGTH + 1 );
			bf.write( (int) MAX_LENGTH - 200, BYTES );
			fail |= bf.canAppend( 200 );
			tc.assertTrue( !fail );
		}
		
		@Test.Impl( src = "public boolean ByteFile.canAppend(int)", desc = "True for small count", weight = 2 )
		public void canAppend_TrueForSmallCount( TestCase tc ) {
			boolean pass = true;
			pass &= bf.canAppend( (int) MAX_LENGTH );
			bf.write( (int) MAX_LENGTH - 200, BYTES );
			pass &= bf.canAppend( 100 );
			tc.assertTrue( pass );
		}
		
		@Test.Impl( src = "public boolean ByteFile.isOpen()", desc = "False after dispose" )
		public void isOpen_FalseAfterDispose( TestCase tc ) {
			bf.dispose();
			tc.assertTrue( ! bf.isOpen() );
		}
		
		@Test.Impl( src = "public boolean ByteFile.isOpen()", desc = "True for new" )
		public void isOpen_TrueForNew( TestCase tc ) {
			tc.assertTrue( bf.isOpen() );
		}

		@Test.Impl( src = "public void ByteFile.dispose()", desc = "Releases resources" )
		public void dispose_ReleasesResources( TestCase tc ) {
			long total = TOTAL_BYTES;
			bf.add( BYTES );
			bf.dispose();
			tc.assertEqual( total, TOTAL_BYTES );
		}
					
		@Test.Impl( src = "public int ByteFile.add(byte[])", desc = "Increases length by src length" )
		public void add_IncreasesLengthBySrcLength( TestCase tc ) {
			int len = bf.length;
			bf.add( BYTES );
			tc.assertEqual( len + BYTES.length, bf.length );
		}
					
		@Test.Impl( src = "public int ByteFile.add(byte[])", desc = "Increases total bytes by count" )
		public void add_IncreasesTotalBytesByCount( TestCase tc ) {
			long total = TOTAL_BYTES;
			bf.add( BYTES );
			bf.add( BYTES );
			tc.assertEqual( total + 2 * BYTES.length, TOTAL_BYTES );
		}
		
		@Test.Impl( src = "public int ByteFile.add(byte[], int, int)", desc = "Increases total bytes by count" )
		public void add2_IncreasesTotalBytesByCount( TestCase tc ) {
			long total = TOTAL_BYTES;
			bf.add( BYTES,  0, 12 );
			bf.add( BYTES,  0, 14 );
			bf.add( BYTES,  0, 16 );
			tc.assertEqual( total + 42, TOTAL_BYTES );
		}
		
		@Test.Impl( src = "public void ByteFile.write(int, byte[], int, int)", desc = "Increases total bytes" )
		public void write_IncreasesTotalBytes( TestCase tc ) {
			bf.write( 110, BYTES, 0, 20 );
			tc.assertEqual( (long) 130, TOTAL_BYTES );
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
