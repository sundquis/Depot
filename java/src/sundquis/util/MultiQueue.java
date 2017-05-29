/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 */

package sundquis.util;

import java.util.ArrayList;

import sundquis.core.Assert;
import sundquis.core.Fatal;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * Decorates a queue by adding thread-safe synchronization and blocking
 * semantics to the accessors.
 */
@Test.Decl( "If backed by priority queue elements retrieved in priority order" )
@Test.Decl( "If backed by FIFO queue elements retrieved in FIFO order" )
@Test.Decl( "Multi thread stress test" )
public class MultiQueue<E> implements Queue<E> {


	// The backing Queue
	private Queue<E> q;

	/**
	 * Constructs a queue containing the elements of the given Queue.
	 * The queue has the same state as the wrapped queue.
	 *
	 * <p>
	 * <b>Note:</b> Direct operations on the underlying <tt>Queue</tt> are
	 * not thread-safe. Safe access is available through the <tt>MultiQueue</tt>
	 * wrapper only.
	 *
	 * @param elements
	 *      A queue of elements.
	 */
	@Test.Decl( "is empty if queue is empty" )
	@Test.Decl( "is non empty if queue is non empty" )
	@Test.Decl( "is open if queue is open" )
	@Test.Decl( "is closed if queue is closed" )
	@Test.Decl( "is terminated if queue is terminated" )
	public MultiQueue( Queue<E> q ) {
		this.q = Assert.nonNull( q );
	}

	/**
	 * Tells if the queue is open.
	 *
	 * @return
	 *      <tt>true</tt> if the queue is open and accepting inputs and producing outputs.
	 */
	@Override
	@Test.Decl( "is open if queue is open" )
	public synchronized boolean isOpen() {
		return this.q.isOpen();
	}

	/**
	 * Tells if the queue is closed.
	 *
	 * @return
	 *      <tt>true</tt> if the queue is closed and not accepting inputs.
	 */
	@Override
	@Test.Decl( "is closed if queue is closed" )
	public synchronized boolean isClosed() {
		return this.q.isClosed();
	}

	/**
	 * Tells if the queue has been terminated.
	 *
	 * @return
	 *      <tt>true</tt> if the queue is no longer accepting inputs or
	 *      producing outputs.
	 */
	@Override
	@Test.Decl( "is terminated if queue is terminated" )
	public synchronized boolean isTerminated() {
		return this.q.isTerminated();
	}

	/**
	 * Request that this queue be closed, blocking further input.
	 */
	@Override
	@Test.Decl( "Can close if open" )
	@Test.Decl( "Close on terminated ignored" )
	public synchronized void close() {
		this.q.close();
		this.notifyAll();
	}

	/**
	 * Request that this queue be terminated. After this call the queue ignores calls 
	 * to get, put, and close.
	 */
	@Override
	@Test.Decl( "Can terminate if open" )
	@Test.Decl( "Can terminate if closed" )
	public synchronized void terminate() {
		this.q.terminate();
		this.notifyAll();
	}
	
	/**
	 * Request to add a non-null element to the queue.
	 *
	 * @param elt
	 * 		The non-null element to add to the queue.
	 * 
	 * @return
	 * 		false if the queue is closed or terminated and the call has been ignored,
	 * 		true if the element has been accepted
	 */
	@Override
	@Test.Decl( "Put on open is accepted" )
	@Test.Decl( "Put on closed is ignored" )
	@Test.Decl( "Put on terminated is ignored" )
	public synchronized boolean put( E elt ) {
		boolean result = this.q.put( elt );
		this.notifyAll();
		return result;
	}
	
	/**
	 * Get the next element from the queue. The return value depends on the
	 * state of the queue and on the empty/non-empty status of the queue:
	 *
	 * OPEN and non-empty: The next non-null element
	 * OPEN and empty: block, awaiting an element
	 * CLOSED: The next element or null if empty
	 * TERMINATED: null
	 *
	 * @return
	 *       The next element of the queue or null if the queue is done
	 *       producing elements.
	 */
	@Override
	@Test.Decl( "Get on open non empty returns non null" )
	@Test.Decl( "Get on open empty blocks awaiting notification" )
	@Test.Decl( "Get on closed non empty returns non null" )
	@Test.Decl( "Get on closed empty returns null" )
	@Test.Decl( "Get on terminated non empty returns null" )
	@Test.Decl( "Get on terminated empty returns null" )
	public synchronized E get() {
		while ( true ) {
			if ( this.q.isTerminated() ) {
				return null;
			}
			
			if ( this.q.isClosed() ) {
				return this.q.get();
			}
			
			if ( ! this.q.isEmpty() ) {
				return this.q.get();
			}
			
			try {
				this.wait();
			} catch ( InterruptedException ex ) {}
		}
	}

	
	/**
	 * Tells if the queue is empty.
	 *
	 * @return
	 *      <tt>true</tt> if the queue contains no elements.
	 */
	@Override
	@Test.Decl( "Put on empty is not empty" )
	@Test.Decl( "Put on non empty is not empty" )
	@Test.Decl( "Put then get on empty is empty" )
	@Test.Decl( "Put then put then get is not empty" )
	public synchronized boolean isEmpty() {
		return this.q.isEmpty();
	}
	
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return MultiQueue.class;
		}
		
		
		private Queue<String> fm;
		private Queue<String> pm;
		private Queue<String> fifo;
		private Queue<String> priority;

		@Override
		public Procedure beforeEach() {
			return new Procedure() {
				public void call() {
					fifo = new FifoQueue<String>();
					priority = new PriorityQueue<String>();
					fm = new MultiQueue<String>( fifo );
					pm = new MultiQueue<String>( priority );
				}
			};
		}
		
		@Override
		public Procedure afterEach() {
			return new Procedure() {
				public void call() {
					fifo = null;
					priority = null;
					fm = null;
					pm = null;
				}
			};
		}
		
		private static class Agent extends Thread {
			private Queue<String> queue;
			private String results;
			Agent( Queue<String> queue ) { this.queue = queue; this.results = ""; }
			String getResults() { return this.results; }
			@Override public void run() {
				String r;
				while ( (r = this.queue.get()) != null ) {
					this.results = r;
				}
				this.results = "Z";
			}
		}

		private static class Record {
			int counter = 0;
			void inc() { this.counter++; }
			int get() { return this.counter; }
		}
		
		private static class Agent2 extends Thread {
			private Queue<Record> in;
			private Queue<Record> out;
			private int index;
			private int count = 0;
			Agent2( int index, Queue<Record> in, Queue<Record> out ) {
				this.index = index;
				this.in = in; 
				this.out = out; 
			}
			@Override public void run() {
				Record r;
				while ( (r = this.in.get()) != null ) {
					r.inc();
					count = r.get();
					Thread.yield();
					if ( count != r.get() ) {
						Fatal.error( "Concurrency error" );
					}
					out.put( r );
				}
				out.close();
			}
			Agent2 init() { this.start(); return this; }
			@Override public String toString() {
				return this.index + " [" + count + "]" ; 
			}
		}
		
		

		@Test.Impl( src = "MultiQueue", desc = "If backed by FIFO queue elements retrieved in FIFO order" )
		public void MultiQueue_IfBackedByFifoQueueElementsRetrievedInFifoOrder( TestCase tc ) {
			this.fm.put( "B" );
			this.fm.put( "A" );
			tc.assertEqual( "B",  this.fm.get() );
			tc.assertEqual( "A",  this.fm.get() );
		}

		@Test.Impl( src = "MultiQueue", desc = "If backed by priority queue elements retrieved in priority order" )
		public void MultiQueue_IfBackedByPriorityQueueElementsRetrievedInPriorityOrder( TestCase tc ) {
			this.pm.put( "B" );
			this.pm.put( "A" );
			tc.assertEqual( "A",  this.pm.get() );
			tc.assertEqual( "B",  this.pm.get() );
		}

		@Test.Impl( src = "MultiQueue", desc = "Multi thread stress test" )
		public void MultiQueue_MultiThreadStressTest( TestCase tc ) throws InterruptedException {
			ArrayList<Agent2> threads = new ArrayList<Agent2>();

			Queue<Record> first = new MultiQueue<Record>( new FifoQueue<Record>() );
			Queue<Record> prev = first;
			Queue<Record> next;
			for ( int i = 0; i < 5; i++ ) {
				next = new MultiQueue<Record>( new FifoQueue<Record>() );
				threads.add( new Agent2( i, prev, next ).init() );
				prev = next;
			}
			threads.add( new Agent2( 5, prev, first ).init() );
			
			for ( int i = 0; i < 100; i++ ) {
				first.put( new Record() );
			}
			Thread.sleep( 1000 );
			first.close();
			
			for ( Thread t : threads ) {
				t.join();
			}
			
			tc.pass();
		}

		@Test.Impl( src = "public MultiQueue(Queue)", desc = "is closed if queue is closed" )
		public void MultiQueue_IsClosedIfQueueIsClosed( TestCase tc ) {
			fifo.close();
			priority.close();
			tc.assertTrue( fm.isClosed() );
			tc.assertTrue( pm.isClosed() );
		}

		@Test.Impl( src = "public MultiQueue(Queue)", desc = "is empty if queue is empty" )
		public void MultiQueue_IsEmptyIfQueueIsEmpty( TestCase tc ) {
			tc.assertTrue( fifo.isEmpty() );
			tc.assertTrue( priority.isEmpty() );
			tc.assertTrue( fm.isEmpty() );
			tc.assertTrue( pm.isEmpty() );
		}

		@Test.Impl( src = "public MultiQueue(Queue)", desc = "is non empty if queue is non empty" )
		public void MultiQueue_IsNonEmptyIfQueueIsNonEmpty( TestCase tc ) {
			fifo.put( "A" );
			priority.put( "A" );
			tc.assertFalse( fifo.isEmpty() );
			tc.assertFalse( priority.isEmpty() );
			tc.assertFalse( fm.isEmpty() );
			tc.assertFalse( pm.isEmpty() );
		}

		@Test.Impl( src = "public MultiQueue(Queue)", desc = "is open if queue is open" )
		public void MultiQueue_IsOpenIfQueueIsOpen( TestCase tc ) {
			tc.assertTrue( fifo.isOpen() );
			tc.assertTrue( priority.isOpen() );
			tc.assertTrue( fm.isOpen() );
			tc.assertTrue( pm.isOpen() );
		}

		@Test.Impl( src = "public MultiQueue(Queue)", desc = "is terminated if queue is terminated" )
		public void MultiQueue_IsTerminatedIfQueueIsTerminated( TestCase tc ) {
			this.fifo.terminate();
			this.priority.terminate();
			tc.assertTrue( fifo.isTerminated() );
			tc.assertTrue( priority.isTerminated() );
			tc.assertTrue( fm.isTerminated() );
			tc.assertTrue( pm.isTerminated() );
		}

		@Test.Impl( src = "public Object MultiQueue.get()", desc = "Get on closed empty returns null" )
		public void get_GetOnClosedEmptyReturnsNull( TestCase tc ) {
			this.fifo.close();
			this.priority.close();
			tc.assertTrue( fifo.isEmpty() );
			tc.assertTrue( priority.isEmpty() );
			tc.assertTrue( fifo.isClosed() );
			tc.assertTrue( priority.isClosed() );
			tc.isNull( fm.get() );
			tc.isNull( pm.get() );
		}

		@Test.Impl( src = "public Object MultiQueue.get()", desc = "Get on closed non empty returns non null" )
		public void get_GetOnClosedNonEmptyReturnsNonNull( TestCase tc ) {
			this.fifo.put( "A" );
			this.priority.put( "B" );
			this.fifo.close();
			this.priority.close();
			tc.assertFalse( fifo.isEmpty() );
			tc.assertFalse( priority.isEmpty() );
			tc.assertTrue( fifo.isClosed() );
			tc.assertTrue( priority.isClosed() );
			tc.notNull( fm.get() );
			tc.notNull( pm.get() );
		}

		@Test.Impl( src = "public Object MultiQueue.get()", desc = "Get on open empty blocks awaiting notification" )
		public void get_GetOnOpenEmptyBlocksAwaitingNotification( TestCase tc ) throws InterruptedException {
			tc.assertTrue( this.fm.isEmpty() );
			tc.assertTrue( this.pm.isEmpty() );
			Agent fma = new Agent( this.fm );
			Agent pma = new Agent( this.pm );
			fma.start();
			pma.start();
			tc.assertEqual( "",  fma.getResults() );
			tc.assertEqual( "",  pma.getResults() );

			this.fm.put( "A" );
			this.pm.put( "A" );
			Thread.sleep( 20 );
			tc.assertEqual( "A",  fma.getResults() );
			tc.assertEqual( "A",  pma.getResults() );
			
			this.fm.put( "B" );
			this.pm.put( "B" );
			Thread.sleep( 20 );
			tc.assertEqual( "B",  fma.getResults() );
			tc.assertEqual( "B",  pma.getResults() );
			
			this.fm.close();
			this.pm.close();
			Thread.sleep( 20 );
			tc.assertEqual( "Z",  fma.getResults() );
			tc.assertEqual( "Z",  pma.getResults() );
			
			tc.assertFalse( fma.isAlive() );
			tc.assertFalse( pma.isAlive() );
		}

		@Test.Impl( src = "public Object MultiQueue.get()", desc = "Get on open non empty returns non null" )
		public void get_GetOnOpenNonEmptyReturnsNonNull( TestCase tc ) {
			this.fifo.put( "A" );
			this.priority.put( "B" );
			tc.assertFalse( fifo.isEmpty() );
			tc.assertFalse( priority.isEmpty() );
			tc.assertTrue( fifo.isOpen() );
			tc.assertTrue( priority.isOpen() );
			tc.notNull( fm.get() );
			tc.notNull( pm.get() );
		}

		@Test.Impl( src = "public Object MultiQueue.get()", desc = "Get on terminated empty returns null" )
		public void get_GetOnTerminatedEmptyReturnsNull( TestCase tc ) {
			this.fifo.terminate();
			this.priority.terminate();
			tc.assertTrue( fifo.isEmpty() );
			tc.assertTrue( priority.isEmpty() );
			tc.assertTrue( fifo.isTerminated() );
			tc.assertTrue( priority.isTerminated() );
			tc.isNull( fm.get() );
			tc.isNull( pm.get() );
		}

		@Test.Impl( src = "public Object MultiQueue.get()", desc = "Get on terminated non empty returns null" )
		public void get_GetOnTerminatedNonEmptyReturnsNull( TestCase tc ) {
			this.fifo.put( "A" );
			this.priority.put( "B" );
			this.fifo.terminate();
			this.priority.terminate();
			tc.assertFalse( fifo.isEmpty() );
			tc.assertFalse( priority.isEmpty() );
			tc.assertTrue( fifo.isTerminated() );
			tc.assertTrue( priority.isTerminated() );
			tc.isNull( fm.get() );
			tc.isNull( pm.get() );
		}

		@Test.Impl( src = "public boolean MultiQueue.isClosed()", desc = "is closed if queue is closed" )
		public void isClosed_IsClosedIfQueueIsClosed( TestCase tc ) {
			this.fifo.close();
			this.priority.close();
			tc.assertTrue( fifo.isClosed() );
			tc.assertTrue( priority.isClosed() );
			tc.assertTrue( fm.isClosed() );
			tc.assertTrue( pm.isClosed() );
		}

		@Test.Impl( src = "public boolean MultiQueue.isEmpty()", desc = "Put on empty is not empty" )
		public void isEmpty_PutOnEmptyIsNotEmpty( TestCase tc ) {
			tc.assertTrue( fm.isEmpty() );
			tc.assertTrue( pm.isEmpty() );
			this.fm.put( "A" );
			this.pm.put( "A" );
			tc.assertFalse( fm.isEmpty() );
			tc.assertFalse( pm.isEmpty() );			
		}

		@Test.Impl( src = "public boolean MultiQueue.isEmpty()", desc = "Put on non empty is not empty" )
		public void isEmpty_PutOnNonEmptyIsNotEmpty( TestCase tc ) {
			this.fm.put( "A" );
			this.pm.put( "A" );
			tc.assertFalse( fm.isEmpty() );
			tc.assertFalse( pm.isEmpty() );			
			this.fm.put( "B" );
			this.pm.put( "B" );
			tc.assertFalse( fm.isEmpty() );
			tc.assertFalse( pm.isEmpty() );			
		}

		@Test.Impl( src = "public boolean MultiQueue.isEmpty()", desc = "Put then get on empty is empty" )
		public void isEmpty_PutThenGetOnEmptyIsEmpty( TestCase tc ) {
			tc.assertTrue( fm.isEmpty() );
			tc.assertTrue( pm.isEmpty() );
			this.fm.put( "A" );
			this.pm.put( "A" );
			this.fm.get();
			this.pm.get();
			tc.assertTrue( fm.isEmpty() );
			tc.assertTrue( pm.isEmpty() );
		}

		@Test.Impl( src = "public boolean MultiQueue.isEmpty()", desc = "Put then put then get is not empty" )
		public void isEmpty_PutThenPutThenGetIsNotEmpty( TestCase tc ) {
			tc.assertTrue( fm.isEmpty() );
			tc.assertTrue( pm.isEmpty() );
			this.fm.put( "A" );
			this.pm.put( "A" );
			this.fm.put( "B" );
			this.pm.put( "B" );
			this.fm.get();
			this.pm.get();
			tc.assertFalse( fm.isEmpty() );
			tc.assertFalse( pm.isEmpty() );
		}

		@Test.Impl( src = "public boolean MultiQueue.isOpen()", desc = "is open if queue is open" )
		public void isOpen_IsOpenIfQueueIsOpen( TestCase tc ) {
			tc.assertTrue( this.fifo.isOpen() );
			tc.assertTrue( this.pm.isOpen() );
			tc.assertTrue( this.fm.isOpen() );
			tc.assertTrue( this.pm.isOpen() );
		}

		@Test.Impl( src = "public boolean MultiQueue.isTerminated()", desc = "is terminated if queue is terminated" )
		public void isTerminated_IsTerminatedIfQueueIsTerminated( TestCase tc ) {
			this.fifo.terminate();
			this.pm.terminate();
			tc.assertTrue( this.fifo.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
			tc.assertTrue( this.fm.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
		}

		@Test.Impl( src = "public boolean MultiQueue.put(Object)", desc = "Put on closed is ignored" )
		public void put_PutOnClosedIsIgnored( TestCase tc ) {
			this.fm.put( "A" );
			this.pm.put( "B" );
			this.fm.close();
			this.pm.close();
			tc.assertTrue( this.fm.isClosed() );
			tc.assertTrue( this.pm.isClosed() );
			tc.assertFalse( this.fm.put( "B" ) );
			tc.assertFalse( this.pm.put( "A" ) );
			tc.assertEqual( "A",  this.fm.get() );
			tc.assertEqual( "B",  this.pm.get() );
		}

		@Test.Impl( src = "public boolean MultiQueue.put(Object)", desc = "Put on open is accepted" )
		public void put_PutOnOpenIsAccepted( TestCase tc ) {
			tc.assertTrue( this.fm.isOpen() );
			tc.assertTrue( this.pm.isOpen() );
			tc.assertTrue( this.fm.put( "B" ) );
			tc.assertTrue( this.pm.put( "A" ) );
			tc.assertEqual( "B",  this.fm.get() );
			tc.assertEqual( "A",  this.pm.get() );
		}

		@Test.Impl( src = "public boolean MultiQueue.put(Object)", desc = "Put on terminated is ignored" )
		public void put_PutOnTerminatedIsIgnored( TestCase tc ) {
			this.fm.put( "A" );
			this.pm.put( "B" );
			this.fm.terminate();
			this.pm.terminate();
			tc.assertTrue( this.fm.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
			tc.assertFalse( this.fm.put( "B" ) );
			tc.assertFalse( this.pm.put( "A" ) );
			tc.isNull( this.fm.get() );
			tc.isNull( this.pm.get() );
		}

		@Test.Impl( src = "public void MultiQueue.close()", desc = "Can close if open" )
		public void close_CanCloseIfOpen( TestCase tc ) {
			tc.assertTrue( this.fm.isOpen() );
			tc.assertTrue( this.pm.isOpen() );
			this.fm.close();
			this.pm.close();
			tc.assertTrue( this.fm.isClosed() );
			tc.assertTrue( this.pm.isClosed() );
		}

		@Test.Impl( src = "public void MultiQueue.close()", desc = "Close on terminated ignored" )
		public void close_CloseOnTerminatedIgnored( TestCase tc ) {
			this.fm.terminate();
			this.pm.terminate();
			tc.assertTrue( this.fm.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
			this.fm.close();
			this.pm.close();
			tc.assertTrue( this.fm.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
		}

		@Test.Impl( src = "public void MultiQueue.terminate()", desc = "Can terminate if closed" )
		public void terminate_CanTerminateIfClosed( TestCase tc ) {
			this.fm.close();
			this.pm.close();
			tc.assertTrue( this.fm.isClosed() );
			tc.assertTrue( this.pm.isClosed() );
			this.fm.terminate();
			this.pm.terminate();
			tc.assertTrue( this.fm.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
		}

		@Test.Impl( src = "public void MultiQueue.terminate()", desc = "Can terminate if open" )
		public void terminate_CanTerminateIfOpen( TestCase tc ) {
			tc.assertTrue( this.fm.isOpen() );
			tc.assertTrue( this.pm.isOpen() );
			this.fm.terminate();
			this.pm.terminate();
			tc.assertTrue( this.fm.isTerminated() );
			tc.assertTrue( this.pm.isTerminated() );
		}
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		System.out.println( "\nDone!" );
	}
	

}
