/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 */

package sundquis.util;

import sundquis.core.Assert;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * Partial implementation of queue operations. Enforces open/close/terminate semantics.
 * 
 * Concrete implementations address the following details:
 * 		get when empty: block vs exception
 * 		put when full: block vs exception
 * 		retrieval order: FIFO, LIFO, etc
 * 		
 * @see FifoQueue
 * @see MultiQueue
 * @see PriorityQueue
 */
public abstract class AbstractQueue<E> implements Queue<E> {

	/**
	 * The legal states for a Queue:
	 */
	@Test.Skip
	private enum State {

		/** Accepting input and providing output */
		OPEN {
			@Override
			<E> boolean put( AbstractQueue<E> q, E elt ) {
				return q.putImpl( elt );
			}

			@Override
			<E> E get( AbstractQueue<E> q ) {
				return q.getImpl();
			}
		},

		/** Providing output but not accepting input */
		CLOSED {
			@Override
			<E> boolean put( AbstractQueue<E> q, E elt ) {
				return false;
			}

			@Override
			<E> E get( AbstractQueue<E> q ) {
				return q.getImpl();
			}
		},

		/** Not accepting input and not providing output */
		TERMINATED {
			@Override
			<E> boolean put( AbstractQueue<E> q, E elt ) {
				return false;
			}

			@Override
			<E> E get( AbstractQueue<E> q ) {
				return null;
			}
		};
		
		abstract <E> boolean put( AbstractQueue<E> q, E elt );
		
		abstract <E> E get( AbstractQueue<E> q );
		
	}

	/** The current state */
	private volatile State state;
	
	/** All queues are created open */
	@Test.Decl( "Queues are created open" )
	protected AbstractQueue() {
		this.state = State.OPEN;
	}

	/**
	 * Request to add a non-null element to the queue.
	 * The behavior depends on the state of the queue and capacity:
	 *
	 * OPEN and not-full: accept the given element
	 * OPEN and full: unspecified
	 * CLOSED: Ignore the element, return false.
	 * TERMINATED: Ignore the element, return false.
	 *
	 * Two choices in the unspecified case, OPEN and full, include:
	 *   Block, waiting for available space
	 *   Throw appropriate exception
	 *
	 * @param elt
	 * 		The non-null element to add to the queue.
	 * 
	 * @return
	 * 		false if the queue is closed or terminated and the call has been ignored,
	 * 		true if the element has been accepted
	 */
	@Test.Skip( "The beavior depends on putImpl so implementations test this." )
	public boolean put( E elt ) {
		Assert.nonNull( elt );  // Queue cannot accept null elements.
		
		return this.state.put( this,  elt );
	}
	
	/**
	 * Accept the non-null element.
	 *
	 * @param elt
	 * 		The non-null element to add to the queue.
	 * @return
	 * 		true if the element is accepted
	 */
	protected abstract boolean putImpl( E elt );

	/**
	 * Get the next element from the queue. The return value depends on the
	 * state of the queue and on the empty/non-empty status of the queue:
	 *
	 * OPEN and non-empty: The next non-null element
	 * OPEN and empty: unspecified
	 * CLOSED: The next element or null if empty
	 * TERMINATED: null
	 *
	 * Two choices in the unspecified case, OPEN and empty, include:
	 *   Block, waiting for an available element
	 *   Throw NoSuchElementException
	 *
	 * @return
	 *       The next element of the queue or null if the queue is done producing elements.
	 */
	@Test.Skip( "The beavior depends on getImpl so implementations test this." )
	public E get() {
		return this.state.get( this );
	}
	
	/** 
	 * Get the next element, or null if empty
	 * 
	 * @return
	 *       The next element of the queue or null if the queue is done producing elements.
	 */
	protected abstract E getImpl();
	
	/**
	 * Tells if the queue is empty.
	 *
	 * @return
	 *      <tt>true</tt> if the queue contains no elements.
	 */
	@Test.Skip
	public abstract boolean isEmpty();

	/**
	 * Tells if the queue is open.
	 *
	 * @return
	 *      <tt>true</tt> if the queue is open and accepting input.
	 */
	@Test.Skip
	public boolean isOpen() {
		return this.state == State.OPEN;
	}

	/**
	 * Tells if the queue is closed.
	 *
	 * @return
	 *      <tt>true</tt> if the queue is closed and not accepting inputs.
	 */
	@Test.Skip
	public boolean isClosed() {
		return this.state == State.CLOSED;
	}

	/**
	 * Tells if the queue has been terminated.
	 *
	 * @return
	 *      <tt>true</tt> if the queue is no longer accepting inputs or
	 *      producing outputs.
	 */
	@Test.Skip
	public boolean isTerminated() {
		return this.state == State.TERMINATED;
	}

	/**
	 * Request that this queue be closed, blocking further input. This call
	 * has no effect unless the queue is open.
	 */
	@Test.Decl( "Can close if open" )
	@Test.Decl( "Close on terminated ignored" )
	public void close() {
		if ( this.state == State.OPEN ) {
			this.state = State.CLOSED;
		}
	}

	/**
	 * Request that this queue be terminated. This causes the current contents
	 * to be discarded. After this call the queue ignores calls to get, put, and
	 * close.
	 */
	@Test.Decl( "Can terminate if open" )
	@Test.Decl( "Can terminate if closed" )
	public void terminate() {
		this.state = State.TERMINATED;
	}
	
	
	

	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return AbstractQueue.class;
		}
		
		// put() and get() not functional.
		// Used to test the semantics of open, close, terminate
		class ConcreteQueue<T> extends AbstractQueue<T> {
			
			ConcreteQueue() { super(); }

			@Override protected boolean putImpl(Object elt) { return false; }

			@Override protected T getImpl() { return null; }

			@Override public boolean isEmpty() { return false; }
			
		}
		
		private Queue<String> queue;
		
		@Override
		public Procedure beforeEach() {
			return new Procedure() {
				public void call() {
					queue = new ConcreteQueue<String>();
				}
			};
		}
		
		@Override
		public Procedure afterEach() {
			return new Procedure() {
				public void call() {
					queue = null;
				}
			};
		}

		@Test.Impl( src = "protected AbstractQueue()", desc = "Queues are created open" )
		public void AbstractQueue_QueuesAreCreatedOpen( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
		}

		@Test.Impl( src = "public void AbstractQueue.close()", desc = "Can close if open" )
		public void close_CanCloseIfOpen( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
		}

		@Test.Impl( src = "public void AbstractQueue.close()", desc = "Close on terminated ignored" )
		public void close_CloseOnTerminatedIgnored( TestCase tc ) {
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			this.queue.close();
			tc.assertFalse( this.queue.isClosed() );
		}

		@Test.Impl( src = "public void AbstractQueue.terminate()", desc = "Can terminate if closed" )
		public void terminate_CanTerminateIfClosed( TestCase tc ) {
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
		}

		@Test.Impl( src = "public void AbstractQueue.terminate()", desc = "Can terminate if open" )
		public void terminate_CanTerminateIfOpen( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
		}
		
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		System.out.println( "\nDone!" );
	}


}
