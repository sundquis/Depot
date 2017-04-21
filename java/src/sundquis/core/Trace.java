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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Trace.java,v $
 * $Id: Trace.java,v 1.18 2002/11/30 14:32:55 johnh Exp $
 * $Log: Trace.java,v $
 * Revision 1.18  2002/11/30 14:32:55  johnh
 * Backed out some printlines I had forgotten to remove
 *
 * Revision 1.17  2002/11/25 04:48:51  johnh
 * Fixed(removed) detab, and added methods to add TraceWriters to TraceManager.
 *
 * Revision 1.16  2002/08/05 20:23:02  toms
 * Simplified TraceEntry for Throwable. 1.4 Throwable prints its cause.
 *
 * Revision 1.15  2002/08/04 19:59:34  johnh
 * Changes supporting initial rollout of gui client
 *
 * Revision 1.14  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.13  2002/05/20 20:33:41  toms
 * Added documentation and overhauled trace message generation.
 *
 * Revision 1.12  2002/05/09 04:08:01  toms
 * Removed trim
 *
 * Revision 1.11  2002/05/04 16:02:06  toms
 * Enhanced toString and added support for tracing iterations
 *
 * Revision 1.10  2002/04/26 21:04:00  toms
 * Refactored and improved tracing interface.
 *
 * Revision 1.9  2002/04/23 09:06:29  toms
 * Added Traceable interface so that objects may specify their own list of trace messages.
 *
 * Revision 1.8  2001/11/14 12:05:37  toms
 * Multiline objects have abbreviated output.
 *
 * Revision 1.7  2001/11/13 15:37:33  toms
 * Minor modification.
 *
 * Revision 1.6  2001/11/02 11:48:34  toms
 * Remove support for old caching mechanism.
 *
 * Revision 1.5  2001/10/18 15:35:40  toms
 * New Trace facility.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.io.*;
import java.text.*;
import java.util.*;
import com.circus.util.*;

/**
 * A thread-safe tracing utility.
 * <P>
 * <TT>Trace</TT> instances are constructed with a topic and a
 * <A HREF="Trace.Severity.html">severity level</A>. Messages
 * of varying severity can be sent to the trace instance and depending
 * on current level of the instance the message will conditionally be
 * sent to the <A HREF="TraceManager.html">trace manager</A> which has
 * responsibility for directing the message to some destination.
 */
public final class Trace {

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	public static class TEST extends Test {

		public static void main( String[] args ) throws Exception {
			Test t = new TEST();

			final Trace t1 = new Trace( "Topic One" );
			final Trace t2 = new Trace( "Topic Two" );
			t1.setLevel( Severity.LOW );
			t2.setLevel( Severity.LOW );

			ArrayList list = new ArrayList();
			list.add( "A" );
			list.add( "B" );

			final Object[] arg = new Object[] {
				"Hello World",
				new Date().toString(),
				new String[] { "1", "2", "3" },
				t,
				new Date(),
				new RuntimeException( "runtime" ),
				new ApplicationException( "app exc" ),
				new BaseRuntime( "base runtime", new Exception( "cause" ) ),
				"multi\nstring with\ttab",
				list,
			};

			Thread thd1 = new Thread() {
				public void run() {
					for ( int i = 0; i < 10; i++ ) {
						t1.writeMed( arg[ i % arg.length ] );
						Thread.yield();
					}
				}
			};
			Thread thd2 = new Thread() {
				public void run() {
					for ( int i = 0; i < 10; i++ ) {
						t2.writeMed( arg[ i % arg.length ] );
						Thread.yield();
					}
				}
			};
			thd1.start();
			thd2.start();

			try {
				thd1.join();
				thd2.join();
			} catch ( InterruptedException ex ) {}

			t.testSummary();
		}

	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
//  	public static void main( String[] args ) { Trace.TEST.main( args ); }

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	/**
	 * <A NAME="LEVELS">An enumerated type that models fixed severity levels.</A>
	 * <P>
	 * Allows graded severity of trace entries. Additional levels may be
	 * specified by adding a new instance to the list of static instances
	 * below. It must be assigned a unique integer value (values of existing
	 * instances may be re-assigned but their order should not be altered).
	 * New levels should also be exposed in the trace API by providing an
	 * appropriate <TT>write</TT> method.
	 */
	public static class Severity extends Ordinal {

		private Severity( int value ) {
			super( value );
		}

		/**
		 * A low level message, only sent to the log when the trace level
		 * is set to maximum verbosity.
		 */
		public static Severity LOW = new Severity( 0 );

		/** An intermediate level message. */
		public static Severity MED = new Severity( 100 );

		/**
		 * An important message, written to the log unless tracing has been
		 * turned off.
		 */
		public static Severity HIGH = new Severity( 200 );

		/**
		 * A message that cannot be ignored, even if tracing is set
		 * to minimum verbosity.
		 */
		public static Severity MAX = new Severity( Integer.MAX_VALUE );

	}

	/**
	 * The current trace level. Entries are only logged if their level is
	 * at or exceeds this level. By default, tracing is disabled.
	 */
	private volatile Severity level;

	/**
	 * The topic associated with this trace instance. The topic is included
	 * in every entry made by this trace instance.
	 */
	private final String topic;

	/**
	 * Constructs a trace instance for the given topic. By default tracing is
	 * disabled.
	 *
	 * @param topic
	 *      A non-null, non-empty <TT>String</TT> designating the trace topic.
	 */
	public Trace( String topic ) {
		this( topic, Severity.MAX );
	}

	/**
	 * Constructs a trace instance for the given topic and severity level.
	 *
	 * @param topic
	 *      A non-null, non-empty <TT>String</TT> designating the trace topic.
	 *
	 * @param level
	 *      A non-null <A HREF="Trace.Severity.html"><TT>Severity</TT></A>
	 *      instance designating the severity level of this
	 *      trace instance.
	 */
	public Trace( String topic, Severity level ) {
		Contract.cAssert( Strings.isNotEmpty( topic ) );
		Contract.cAssert( level != null );

		this.topic = topic;
		this.level = level;
	}

	/**
	 * All subsequent trace calls will be logged if their level is at or
	 * exceeds this level.
	 *
	 * @param level
	 *      A non-null <A HREF="Trace.Severity.html"><TT>Severity</TT></A>
	 *      instance designating the new severity level of this
	 *      trace instance.
	 */
	public void setLevel( Severity level ) {
		Contract.cAssert( level != null );

		this.level = level;
	}

	/**
	 * Write a low severity object into the trace record.
	 *
	 * @param object
	 *      A non-null <TT>Object</TT>. The displayable messages for this
	 *      object are preparred
	 *      by a <A HREF="Trace.Entry.html"><TT>Trace.Entry</TT></A> instance
	 *      which uses the object type to determine what kind of message is
	 *      appropriate.
	 */
	public void writeLow( Object object ) {
		Contract.cAssert( object != null );

		this.write( object, Severity.LOW );
	}

	/**
	 * Write a medium severity object into the trace record.
	 *
	 * @param object
	 *      A non-null <TT>Object</TT>. The displayable messages for this
	 *      object are preparred
	 *      by a <A HREF="Trace.Entry.html"><TT>Trace.Entry</TT></A> instance
	 *      which uses the object type to determine what kind of message is
	 *      appropriate.
	 */
	public void writeMed( Object object ) {
		Contract.cAssert( object != null );

		this.write( object, Severity.MED );
	}

	/**
	 * Write a high severity object into the trace record.
	 *
	 * @param object
	 *      A non-null <TT>Object</TT>. The displayable messages for this
	 *      object are preparred
	 *      by a <A HREF="Trace.Entry.html"><TT>Trace.Entry</TT></A> instance
	 *      which uses the object type to determine what kind of message is
	 *      appropriate.
	 */
	public void writeHigh( Object object ) {
		Contract.cAssert( object != null );

		this.write( object, Severity.HIGH );
	}

	/**
	 * Force the object to be written to the trace log, regardless of level.
	 *
	 * @param object
	 *      A non-null <TT>Object</TT>. The displayable messages for this
	 *      object are preparred
	 *      by a <A HREF="Trace.Entry.html"><TT>Trace.Entry</TT></A> instance
	 *      which uses the object type to determine what kind of message is
	 *      appropriate.
	 */
	public void force( Object object ) {
		Contract.cAssert( object != null );

		this.write( object, Severity.MAX );
	}

	/**
	 * If the current trace level is at or below the given level an entry is
	 * added to the entry queue.
	 *
	 * @param object
	 *      A non-null <TT>Object</TT>. The displayable messages for this
	 *      object are preparred
	 *      by a <A HREF="Trace.Entry.html"><TT>Trace.Entry</TT></A> instance
	 *      which uses the object type to determine what kind of message is
	 *      appropriate.
	 *
	 * @param level
	 *      A non-null <A HREF="Trace.Severity.html"><TT>Severity</TT></A>
	 *      instance designating the severity level of this object's message.
	 */
	public void write( Object object, Severity level ) {
		Contract.cAssert( object != null );
		Contract.cAssert( level != null );

		if ( this.level.compareTo( level ) <= 0 ) {
			TraceManager.getInstance().submit( new Entry( object, level ) );
		}
	}

	/** Add a traceWriter to the multiplexing TraceManager.  Put here,
		to keep TraceManager a package protected class, as it should
		be. **/
	public static void addTraceWriter(TraceManager.TraceWriter tw) {
		TraceManager.getInstance().addTraceWriter(tw);
	}

	/**
	 * Prepare a simple <TT>String</TT> representation.
	 * <P>
	 * <TT>Trace</TT> defers to the
	 * <A HREF="TraceManager.html"><TT>trace manager</TT></A>, which should
	 * provide useful information as to the destination of trace messages.  */
	public String toString() {
		return TraceManager.getInstance().toString();
	}

	/**
	 * Objects implement this interface to control their displayed messages.
	 */
	public static interface Traceable {

		/**
		 * Return the list of displayable trace messages suitable for this
		 * object.
		 * <P>
		 * Elements of the returned list must be non-null
		 * strings. It is guaranteed that this list will appear as a
		 * contiguous block of messages in the log.
		 *
		 * @return
		 *      A non-null, possibly empty, list of non-null string messages.
		 */
		public List getTraceMessages();

	}

	// These three really belong to Entry but, being static, cannot be declared there.

	/**
	 * Trace output is tab-delimited to enable "spread-sheet" style viewing
	 * of the log.
	 * When a trace entry is sent to the manager we check to see if we should
	 * first print the header information.
	 */
	private static volatile boolean needToPrintHeader = true;

	/** Used to generate timestamps for messages. */
	private static DateFormat dateFmt = new SimpleDateFormat( "yyyy-MMM-dd HH:mm:ss" );

	/**
	 * The header to include in the log before the first message.
	 * <P>
	 * Each line  of the trace log contains these elements:
	 * <OL>
	 *   <LI><B>Timestamp.</B> A date sting prepared by the static
	 *     <A HREF="Trace.html#dateFmt">formatter</A>.
	 *   <LI><B>Severity.</B> The name of the severity level assigned to this message.
	 *   <LI><B>Topic.</B> The topic associated with the trace instance.
	 *   <LI><B>Thread.</B> The name of the thread generating the trace message.
	 *   <LI><B>N.</B> A sequence number, starting at 1, used to mark multi-line
	 *     messages.
	 *   <LI><B>Message.</B> The string message.
	 * </OL>
	 * For multi-line messages, only the first line contains all elements;
	 * subsequent lines contain only the sequence numebr and message fields.
	 */
	private static String headerString = "TIMESTAMP\tSEVERITY\tTOPIC\tTHREAD\tN\tMESSAGE";

	/**
	 * An inner class for modelling trace log entries.
	 * <P>
	 * The entry contains a list of strings to be entered in the trace log.
	 * The strings contain no new-line characters, and each line contains
	 * a tab-delimited set of <A HREF="Trace.html#headerString">fields</A>.
	 * The <TT>message</TT> field of the entry is obtained in a type-specifc
	 * way. The supported types and their custom messages are:
	 * <OL>
	 *   <LI><A HREF="Trace.Entry.html#getTraceMessages(java.lang.String)"><B>String</B></A>
	 *   <LI><A HREF="Trace.Entry.html#getTraceMessages(com.circus.core.Trace.Traceable)"><B>Traceable</B></A>
	 *   <LI><A HREF="Trace.Entry.html#getTraceMessages(java.util.Collection)"><B>Collection</B></A>
	 *   <LI><A HREF="Trace.Entry.html#getTraceMessages(java.lang.Object[])"><B>Object[]</B></A>
	 *   <LI><A HREF="Trace.Entry.html#getTraceMessages(java.lang.Throwable)"><B>Throwable</B></A>
	 *   <LI><B>Chainable exception types.</B>
	 *     First the exception is processed as a throwable; if the exception
	 *     contains a non-null chained exception it is submitted for further
	 *     processing.
	 * </OL>
	 * The <TT>Entry</TT> class implements <TT>getTraceMessages()</TT>
	 * methods for each of these types. The appropriate method is
	 * determined through <A HREF="Reflect.html">reflection</A>. To provide
	 * custom tracing for additional types implement a new <TT>getTraceMessages(X)</TT>
	 * method that processes the new type <TT>X</TT>. The implementation should
	 * resolve <TT>X</TT> messages to one of the existing types and submit
	 * them for further processing (this ensures consistency and proper
	 * string format).
	 */
	class Entry {

		/**
		 * The list of string messages.
		 * <P>
		 * The strings in this list contains no tab or new-line characters.
		 */
		private List messages;

		/**
		 * Construct a trace entry corresponding to a target object.
		 * <P>
		 *
		 * @param value
		 *      A non-null object to trace.
		 *
		 * @param severity
		 *      The non-null severity level of this trace entry.
		 */
		private Entry( Object value, Severity severity ) {
			this.messages = new LinkedList();

			// There is a harmless race condition here that might result
			// in the header not appearing as the first line of the log.
			if ( Trace.needToPrintHeader ) {
				Trace.needToPrintHeader = false;
				messages.add( Trace.headerString );
			}

			String first =
				Trace.dateFmt.format( new Date() )
				+ "\t" + severity.getName()
				+ "\t" + Trace.this.topic
				+ "\t" + Thread.currentThread();
			String subsequent = "\t\t\t";

			int seqNo = 1;
			Iterator iter = this.getMessagesByType( value ).iterator();
			while ( iter.hasNext() ) {
				this.messages.add(
					(seqNo == 1 ? first : subsequent) + "\t" + (seqNo++) + "\t" + iter.next()
				);
			}
		}

		/**
		 * The <A HREF="TraceManager.html"><TT>TraceManager</TT></A> calls
		 * this method to get the entries formatted messages.
		 */
		List getMessages() {
			return this.messages;
		}

		/**
		 * Helper method that invokes the method <TT>getTraceMessages</TT>
		 * on this entry instance with the given object as argument. This
		 * allows type-specific processing of the argument.
		 *
		 * @param value
		 *      A non-null object to be converted into a list of messages.
		 */
		private List getMessagesByType( Object value ) {
			List result = null;

			if ( value instanceof String ) {
				result = this.getTraceMessages( (String) value );
			} else if ( value instanceof Traceable ) {
				result = this.getTraceMessages( (Traceable) value );
			} else if ( value instanceof Collection ) {
				result = this.getTraceMessages( (Collection) value );
			} else if ( value instanceof Object[] ) {
				result = this.getTraceMessages( (Object[]) value );
			} else if ( value instanceof BaseException ) {
				result = this.getTraceMessages( (BaseException) value );
			} else if ( value instanceof BaseRuntime ) {
				result = this.getTraceMessages( (BaseRuntime) value );
			} else if ( value instanceof Throwable ) {
				result = this.getTraceMessages( (Throwable) value );
			} else {
				result = this.getTraceMessages( value.toString() );
			}

			return result;
		}

		/**
		 * Get the list of messages associated with a string.
		 * <P>
		 * If the string contains any embedded newlines it is
		 * split into a list of strings. Any contained tab
		 * characters are replaced by a single space.
		 *
		 * @param s
		 *      A non-null <TT>Sting</TT>.
		 */
		public List getTraceMessages( String s ) {
			List msgs = new LinkedList();

			StringReader sr = new StringReader( s );
			BufferedReader br = new BufferedReader( sr );
			String msg = null;
			try {
				while ( (msg = br.readLine()) != null ) {
					msgs.add( msg.replace('\t',' '));
				}
			} catch ( IOException ex ) {
				assert false : "Trace.Entry.getTraceMessages(String)) : " + ex.getMessage();
			}

			return msgs;
		}

		/**
		 * Get the messages associated with a <TT>Traceable</TT> instance.
		 * <P>
		 * The list obtained from the object's
		 * <A HREF="Trace.Traceable.html#getMessages()"><TT>getTraceMessages()</TT></A>
		 * method is submitted for further processing as a <TT>Collection</TT>.
		 */
		public List getTraceMessages( Trace.Traceable obj ) {
			return this.getTraceMessages( obj.getTraceMessages() );
		}

		/**
		 * Get the messages associated with a <TT>Collection</TT> instance.
		 * <P>
		 * The collection is converted
		 * <A HREF="java.util.Collection#toArray()">to an object array</A>
		 * and submitted for further processing.
		 */
		public List getTraceMessages( Collection c ) {
			return this.getTraceMessages( c.toArray() );
		}

		/**
		 * Get the messages associated with a <TT>Object[]</TT> instance.
		 * <P>
		 * Each element of the array is sequentially submitted for further
		 * processing.
		 */
		public List getTraceMessages( Object[] objects ) {
			List messages = new LinkedList();

			for ( int i = 0; i < objects.length; i++ ) {
				messages.addAll( this.getMessagesByType( objects[i] ) );
			}

			return messages;
		}

		/**
		 * Get the message associated with a <TT>Throwable</TT> instance.
		 * <P>
		 * The lines contained in the
		 * <A HREF="java.lang.Throwable#printStackTrace()">stack trace</A>
		 * are read into a string and submitted for further processing.
		 */
		public List getTraceMessages( Throwable th ) {
			StringWriter sw = new StringWriter();
			th.printStackTrace( new PrintWriter( sw ) );
			return getTraceMessages( sw.toString() );
		}

	}

}
