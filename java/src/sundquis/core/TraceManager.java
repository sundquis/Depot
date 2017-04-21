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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/TraceManager.java,v $
 * $Id: TraceManager.java,v 1.12 2002/11/30 14:34:23 johnh Exp $
 * $Log: TraceManager.java,v $
 * Revision 1.12  2002/11/30 14:34:23  johnh
 * Synchronized access to traceWriters, it was not multi-thread safe
 *
 * Revision 1.11  2002/11/25 04:49:33  johnh
 * Made multiplexing, fixed bugs in TraceFileWriter
 *
 * Revision 1.10  2002/08/05 20:21:50  toms
 * Removed previous change: emptyBuffer(0) causes a concurrency error.
 *
 * Revision 1.9  2002/08/04 19:59:34  johnh
 * Changes supporting initial rollout of gui client
 *
 * Revision 1.8  2002/07/28 21:02:11  johnh
 * Added nl to EOF
 *
 * Revision 1.7  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.6  2002/05/20 20:32:52  toms
 * Modified Entry interface to provide List instead of iterator.
 *
 * Revision 1.5  2002/05/04 16:01:25  toms
 * Enhanced toString so that TM can explain where trace info is going
 *
 * Revision 1.4  2002/04/26 21:04:00  toms
 * Refactored and improved tracing interface.
 *
 * Revision 1.3  2001/12/14 21:14:58  toms
 * Added comment on future development.
 *
 * Revision 1.2  2001/11/14 12:04:57  toms
 * Added comment about behavior.
 *
 * Revision 1.1  2001/10/18 15:35:40  toms
 * New Trace facility.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import com.circus.util.*;
import java.util.*;
import java.io.*;

/**
 * A singleton, active object that buffers <tt>Trace.Entry</tt> instances
 * and writes them to the appropriate stream.
 */
public final class TraceManager implements Runnable, Finalizable {

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	public static class TEST extends Test {

		public static void main( String[] args ) {
			Test t = new TEST();

			t.testSummary();
		}

	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { TraceManager.TEST.main( args ); }

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	/**
	 * The <tt>TraceManager</tt> uses a <tt>TraceWriter</tt> implementation
	 * to record trace entries.
	 */
	public static interface TraceWriter {

		/** Prepare for writing a batch of entries. */
		public void open();

		/** Write a single entry.  It is assumed in the code that this
			is a "quick" function call.  If it is possible that the
			call hangs (for example, a TraceWriter that is passing
			data over a network) then that TraceWriter should
			implement a "worker" thread, and queue up this string.
		*/
		public void println( String s );

		/** Close the writer. */
		public void close();

		/** Inform the writer to shutdown.  (Typically, either close()
            or shutdown() is a no-op.)  This is guaranteed to be
            called from the finalizer of TraceManager, *AFTER* it has
            completed it's flushing of its queues.
		*/
		public void shutdown();

		/** User friendly message describing location of log contents */
		public String getDestination();
	}

	/** The singleton instance. */
	private static TraceManager instance = null;

	/** The maximum number of entries to accumulate before writing. */
	private static final int maxBufferSize = 20;

	/** Returns the singleton instance. Uses lazy instantiation. */
	static TraceManager getInstance() {
		if ( TraceManager.instance == null ) {
			makeInstance();
		}
		return TraceManager.instance;
	}

	/** Builds the static instance using double-checked synchronization */
	private static synchronized void makeInstance() {
		if ( TraceManager.instance == null ) {
			TraceManager.instance = new TraceManager();
		}
	}

	/** A FIFO queue that holds all outstanding trace requests. */
	private Queue entries = new MultiQueue( new FifoQueue() );

	/**
	 * The worker thread. Reads the entry queue, converts entries to string
	 * messages, buffers the string messages, and writes the buffer to a stream.
	 */
	private Thread traceThread;

	/** Holds the current buffer of string records */
	private List buffer = new LinkedList();

	/**
	 * All TraceWriters go here.
	 **/
	private List traceWriters; // Starts out as a single list with TraceFileWriter();

	/**
	 * Constructs the singleton instance. The manager registers with the
	 * application for shutdown and starts the singleton wotker thread.
	 */
	private TraceManager() {
		traceWriters = Collections.synchronizedList(new ArrayList());
		traceWriters.add(new TraceFileWriter());

		DevApplication.getInstance().finalizeOnShutdown( this );
		this.traceThread = new Thread( this );
		this.traceThread.setDaemon( true );
		this.traceThread.start();
	}

	public String toString() {
		// we assume this is not called often.
		String result = ((TraceWriter)traceWriters.get(0)).getDestination();
		for (int i = 1; i < traceWriters.size(); i++) {
			result = result + ", " + ((TraceWriter)traceWriters.get(i)).getDestination();
		}
		return result;
	}

	/** Add a trace writer to the list of trace writers, to which
        messages are multiplexed.  For entities outside this package,
        use Trace.addTraceWriter.**/
	public void addTraceWriter(TraceWriter tw) {
		synchronized (traceWriters) {
			traceWriters.add(tw);
		}
	}

	/** Remove a <tt>TraceWriter</tt> from the list of tracewriters.
	 * Returns true iff the trace writer was in the list of
	 * tracewriters.  
	 **/
	public boolean removeTraceWriter(TraceWriter tw) {
		synchronized (traceWriters) {
			return traceWriters.remove(tw);
		}
	}

	/**
	 * <tt>Trace</tt> instances may submit entries. Entries are put on a queue
	 * for subsequent processing by the worker thread.
	 */
	void submit( Trace.Entry entry ) {
		this.entries.put( entry );
	}

	/**
	 * The body of the worker thread.
	 */
	public void run() {
		if ( Thread.currentThread() != this.traceThread ) {
			throw new ApplicationException( "Cannot start externally." );
		}

		Trace.Entry entry = null;
		while ( (entry = (Trace.Entry) this.entries.get()) != null ) {
			buffer.addAll( entry.getMessages() );
			emptyBuffer( maxBufferSize );
		}
		emptyBuffer( 0 );
	}

	/** If the buffer has more than count elements empty it.
	 */
	private void emptyBuffer( int count ) {
		if ( this.buffer.size() > count ) {
			ListIterator iter = this.buffer.listIterator();


			synchronized (traceWriters) {

				// open up all the trace writers....
				Iterator twIter = traceWriters.iterator();
				while (twIter.hasNext()) {
					((TraceWriter)twIter.next()).open();
				}

				// print out the message to each of the trace writers.
				while ( iter.hasNext() ) {
					String message = iter.next().toString();
				
					twIter = traceWriters.iterator();
					while (twIter.hasNext()) {
						((TraceWriter)twIter.next()).println(message);
					}
					iter.remove();
				}

				// close all the trace writers.
				twIter = traceWriters.iterator();
				while (twIter.hasNext()) {
					((TraceWriter)twIter.next()).close();
				}
			}
		}
	}


	/**
	 * When the JVM is shutting down this method will be called. We close the
	 * queue and wait for elements to be processed.
	 */
	public void finalize() {
		this.entries.close();
		try {
			// push out all the messages.
			this.traceThread.join();
		} catch ( InterruptedException ex ) {}
		/*
		 * NOTE: Not sure if the JVM can kill traceThread prematurely (it
		 * is a daemon thread). If yes, we may need to make additional effort
		 * to empty the queue; uncomment the following:
		 */
		// this.run();

		ListIterator iter = traceWriters.listIterator();
		while (iter.hasNext()) {
			((TraceWriter)iter.next()).shutdown();
			iter.remove();
		}
	}

	/**
	 * This implementation writes trace entries to a file in the directory
	 * <home>/trace. Files are sequential, ranging from 0000.txt to 9999.txt
	 * When all files have been used, usage "wraps around".
	 *
	 * <p>
	 * <b>Note:</b> If the directory currently contains the maximum number of
	 * files the directory is emptied.
	 */
	private class TraceFileWriter implements TraceWriter {

		/** The (approx.) maximum number of entries to be written to a file. */
		private static final int MAX_LINE_COUNT = 10000;

		/** The number of lines that have been written to the current file. */
		private int lineCount = 0;

		/** The current file number. */
		private int fileNo = -1;

		/** The directory that contains trace files. */
		private File traceDir;

		/** The path of the current file, or null if we need to recompute the file name. */
		private String filePath = null;

		/**
		 * If the TraceFileWriter has been opened, this will be a non-null
		 * reference to the destination PrintWriter. Otherwise it is null.
		 */
		private PrintWriter out = null;

		/**
		 * Constructs a TraceFileWriter. If the target directory does not
		 * exist and cannot be created a fatal error is generated.
		 */
		private TraceFileWriter() {
			LocalFile lf = new LocalFile( "trace", LocalFile.Type.DIR );
			this.traceDir = lf.getFile();
			if ( !this.traceDir.exists() && !this.traceDir.mkdir() ) {
				Fatal.error( "Failed to create trace directory." );
			}
			// Create the file now.  If there is a name conflict,
			// create a new one.  This is an attempt to make
			// TraceFileWriter multi-application safe.  (It currently
			// is not) While this is not a guaranteed solution the
			// chances of the race condition getting hit, are much
			// smaller than before.  (note createNewFile is atomic
			// only within a single jvm)
			try {
				File file;
				file = getFile();
				while (!file.createNewFile()) {
					fileNo++;
					file = getFile();
				}
			} catch (IOException ex) {
				assert false : "TraceFileWriter.<init>" + ex.getMessage();
			}
		}

		/**
		 * Opens the writer.
		 */
		public void open() {
			try {
				this.out = new PrintWriter( new FileWriter( getPath(), true ) );
			} catch ( IOException ex ) {
				assert false : "TraceFileWriter.open" + ex.getMessage();
			}
		}

		public String getDestination() {
			return this.getPath();
		}

		/**
		 * Get the path to the current trace file.
		 */
		private String getPath() {
			if ( this.filePath == null ) {
				try {
					this.filePath = getFile().getCanonicalPath();
				} catch ( IOException ex ) {
					assert false : "TraceFileWriter.getPath" + ex.getMessage();
				}
			}
			return this.filePath;
		}

		/**
		 * Find the next trace file.
		 */
		private File getFile() {
			if ( this.fileNo < 0 ) {
				String[] n = this.traceDir.list();
				if ( n == null ) {
					this.fileNo = 0;
				} else if ( n.length >= 10000 ) {
					for ( int i = 0; i < n.length; i++ ) {
						(new File( this.traceDir, pad(i) + ".txt" )).delete();
					}
					this.fileNo = 0;
				} else {
					this.fileNo = n.length;
				}
			}
			return new File( this.traceDir, pad( this.fileNo ) + ".txt" );
		}

		/**
		 * Convenience method to left pad the given number with zeros.
		 */
		private String pad( int num ) {
			Contract.cAssert( num >= 0 );

			String s = "000" + num;
			return s.substring( s.length() - 4 );
		}

		/**
		 * Write a single string trace message.
		 */
		public void println( String s ) {
			Contract.cAssert( this.out != null );

			this.lineCount++;
			this.out.println( s );
		}

		/**
		 * Close the writer and check to see if the current file is full.
		 */
		public void close() {
			this.out.close();
			if ( this.lineCount >= MAX_LINE_COUNT ) {
				this.lineCount = 0;
				this.fileNo = -1;
				this.filePath = null;
			}
		}

		/** Shutdown, since the file is already closed, this is a no-op **/
		public void shutdown(){}

	}
}
