/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import sundquis.core.App.OnShutdown;
import sundquis.util.FifoQueue;
import sundquis.util.MultiQueue;
import sundquis.util.Queue;

/**
 * @author sundquis
 *
 */
public class Trace {
	
	// Can disable all tracing
	private static Boolean ENABLED = Property.get( "enabled",  true,  Property.BOOLEAN );

	// Subdirectory of <root> holding trace files
	private static String TRACE_DIR_NAME = Property.get( "dir.name",  "trace",  Property.STRING );
	
	// Issue warning when this many lines written
	private static Integer WARN_LIMIT = Property.get( "warn.limit",  1000000,  Property.INTEGER );

	// Stop tracing when this many lines written
	private static Integer FAIL_LIMIT = Property.get( "fail.limit",  100000000,  Property.INTEGER );
	
	// Number of lines to hold in buffer before writing to file
	private static Integer MAX_BUFFER_SIZE = Property.get( "max.buffer.size",  100,  Property.INTEGER );
	
	// Should messages be echoed to standard out
	private static Boolean ECHO_ON = Property.get( "echo.messages",  false,  Property.BOOLEAN );

	
	

	@Test.Skip
	private static class MsgHandler implements Runnable, OnShutdown, Consumer<String> {

		// Client write calls add messages to the queue. MsgHandler thread processes
		private final Queue<String> entries;
		
		// Only accessed by handler; a buffer between queued messages and file write
		private final List<String> buffer;
		
		// Current number of messages
		private volatile int lineCount;
		
		// Messages are written here
		private final File traceFile;
		
		// Read the message queue, move to buffer, empty buffer to file
		private final Thread worker;
		
		MsgHandler() {
			this.entries = new MultiQueue<String>( new FifoQueue<String>() );
			this.buffer = new LinkedList<String>();
			
			// Register for shutdown. Close the queue, process existing messages
			App.get().terminateOnShutdown( this );
			
			this.lineCount = 0;
			
			DateFormat fmt = new SimpleDateFormat( "MM.dd.HH.mm.ss" );
			String filename = fmt.format( new Date() );
			LocalDir dir = new LocalDir( true );
			dir.sub( Trace.TRACE_DIR_NAME );
			this.traceFile = dir.getFile( filename,  LocalDir.Type.TEXT );
			if ( this.traceFile.exists() ) {
				Fatal.error( "Trace file already exists: " + this.traceFile );
			}
			
			this.worker = new Thread( this );
			this.worker.setDaemon( true );
			this.worker.start();
		}
		
		/**
		 * @see sundquis.core.App.OnShutdown#terminate()
		 */
		@Override
		public void terminate() {
			this.entries.close();
			try {
				this.worker.join();
			} catch ( InterruptedException e ) {}
		}

		/**
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			if ( Thread.currentThread() != this.worker ) {
				throw new AppException( "Cannot start externally." );
			}

			String msg = null;
			while ( (msg = this.entries.get()) != null ) {
				buffer.add( msg );
				if ( this.buffer.size() > Trace.MAX_BUFFER_SIZE ) {
					this.emptyBuffer();
				}
			}
			this.emptyBuffer();
		}
		
		private void emptyBuffer() {
			if ( this.buffer.size() == 0 ) {
				return;
			}
			
			try ( PrintWriter out = new PrintWriter( new FileWriter( this.traceFile, true ) ) ) {
				for ( String msg : this.buffer ) {
					out.println( msg );
					if ( Trace.ECHO_ON ) {
						System.out.println( msg );
					}
				}
				this.buffer.clear();
			} catch ( IOException e ) {
				Fatal.error( "Unable to write trace file.", e );
			}
		}

		/**
		 * @see java.util.function.Consumer#accept(java.lang.Object)
		 */
		@Override
		public void accept( String msg ) {
			if ( this.lineCount == 0 ) {
				this.entries.put( Trace.HEADER );
				this.lineCount++;
			}
			
			this.entries.put( msg );
			this.lineCount++;
			
			if ( this.lineCount >= Trace.WARN_LIMIT ) {
				Fatal.warning( "Trace file " + this.traceFile + " line count is " + this.lineCount  );
			}
			if ( this.lineCount >= Trace.FAIL_LIMIT ) {
				this.terminate();
				Fatal.error( "Trace file " + this.traceFile + " line count exceeds limit" );
			}
		}
		
		@Override
		public String toString() {
			return this.traceFile.toString();
		}
		
	}
	
	private static final MsgHandler msgHandler = Trace.ENABLED ? new MsgHandler() : null;
	
	
	
	private String topic;
	private int seqNo;

	public Trace( String topic ) {
		this.topic = Assert.boundedString( topic,  3,  20 );
		this.seqNo = 1;
	}
	
	private static final String HEADER = new StringBuffer()
		.append( Strings.rightJustify( "SEQ NO", 6, ' ' ) ).append( " " )
		.append( Strings.leftJustify( "TOPIC", 10, ' ' ) ).append( " " )
		.append( Strings.leftJustify( "THREAD", 10, ' ') ).append( " " )
		.append( Strings.leftJustify( "CLASS NAME", 30, ' ' ) ).append( " " )
		.append( Strings.leftJustify( "METHOD", 25, ' ' ) ).append( " " )
		.append( "MESSAGE" )
		.toString();

	@Test.Decl( "Throws assertion exception for null message" )
	@Test.Decl( "Throws assetrion exception for empty message" )
	@Test.Decl( "Multi thread stress test" )
	public void write( String message ) {
		Assert.nonEmpty( message );
		
		if ( Trace.ENABLED ) {
			StackTraceElement ste = (new Exception()).getStackTrace()[1];
			StringBuffer buf = new StringBuffer();
			buf.append( Strings.rightJustify( "" + seqNo++, 6, ' ' ) ).append( " " );
			buf.append( Strings.leftJustify( this.topic, 10, ' ' ) ).append( " " );
			buf.append( Strings.leftJustify( Thread.currentThread().getName(), 10, ' ') ).append( " " );
			buf.append( Strings.leftJustify( ste.getClassName(), 30, ' ' ) ).append( " " );
			buf.append( Strings.leftJustify( ste.getMethodName(), 25, ' ' ) ).append( " " );
			buf.append( message );
			Trace.msgHandler.accept( buf.toString() );
		}
	}
	
	@Override
	@Test.Decl( "Indicates topic" )
	public String toString() {
		return "Trace(" + this.topic + ", " + Trace.msgHandler + ")";
	}
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return Trace.class;
		}
		
		public Procedure afterAll() {
			return new Procedure() {
				public void call() {
					Trace.msgHandler.terminate();
					Trace.msgHandler.traceFile.delete();
				}
			};
		}
		
		@Test.Impl( 
			src = "public String Trace.toString()", 
			desc = "Indicates topic" )
		public void toString_IndicatesTopic( TestCase tc ) {
			String topic = "Some topic string";
			Trace trace = new Trace( topic );
			tc.assertTrue( trace.toString().contains( topic ) );
		}


		public static class Agent extends Thread {
			
			private Trace trace;
			
			Agent( String topic ) {
				this.trace = new Trace( topic );
			}
			
			@Override
			public void run() {
				for ( int i = 0; i < 101; i++ ) {
					for( int j = 0; j < 9; j++ ) {
						this.trace.write(  "O = " + i + ", I = " + j );
					}
					Thread.yield();
				}
			}
			
			Thread init() {
				this.start();
				return this;
			}
		}
		
		@Test.Impl( 
			src = "public void Trace.write(String)", 
			desc = "Multi thread stress test" )
		public void write_MultiThreadStressTest( TestCase tc ) {
			ArrayList<Thread> agents = new ArrayList<Thread>();
			for ( int i = 0; i < 11; i++ ) {
				agents.add( new Agent( "Thread " + i ).init() );
			}
			
			for ( Thread thread : agents ) {
				try {
					thread.join();
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
			
			tc.pass();
		}

		@Test.Impl( 
			src = "public void Trace.write(String)", 
			desc = "Throws assertion exception for null message" )
		public void write_ThrowsAssertionExceptionForNullMessage( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new Trace( "T" ).write( null );
		}

		@Test.Impl( src = "public void Trace.write(String)", desc = "Throws assetrion exception for empty message" )
		public void write_ThrowsAssetrionExceptionForEmptyMessage( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new Trace( "T" ).write( "" );
		}
		
		
		
		
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();

		System.out.println("\nDone!");
	}	

}
