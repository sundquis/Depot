/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 */

package sundquis.util;

import java.util.LinkedList;

import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * Implements the Queue behavior and specifies a FIFO property on elements.
 * 
 * Queue Implementation:
 * 	1. Determine behavior for put( E elt ) when the queue is open and full
 * 		No full property.
 * 
 * 	2. Determine behavior for E get() when the queue is open and empty
 * 		Return null if open and empty.
 * 
 * 	3. Determine the order retrieval policy
 * 		"First in first out"
 * 
 */
@Test.Decl( "Elements retrieved in FIFO order" )
public class FifoQueue<E> extends AbstractQueue<E> {


	private LinkedList<E> elements;

	/**
	 * Constructs an empty FIFO queue. The queue is open and accepting input.
	 */
	@Test.Decl( "FifoQueues are created empty" )
	public FifoQueue() {
		this.elements = new LinkedList<E>();
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
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}

	@Override
	@Test.Decl( "Put on open is accepted" )
	@Test.Decl( "Put on closed is ignored" )
	@Test.Decl( "Put on terminated is ignored" )
	protected boolean putImpl( E elt ) {
		this.elements.addLast( elt );
		return true;
	}


	@Override
	@Test.Decl( "Get on open non empty returns non null" )
	@Test.Decl( "Get on open empty returns null" )
	@Test.Decl( "Get on closed non empty returns non null" )
	@Test.Decl( "Get on closed empty returns null" )
	@Test.Decl( "Get on terminated non empty returns null" )
	@Test.Decl( "Get on terminated empty returns null" )
	protected E getImpl() {
		return this.isEmpty() ? null : this.elements.removeFirst();
	}

	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return FifoQueue.class;
		}
		
		private Queue<String> queue;

		@Override
		public Procedure beforeEach() {
			return new Procedure() {
				public void call() {
					queue = new FifoQueue<String>();
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
		
		

		@Test.Impl( src = "FifoQueue", desc = "Elements retrieved in FIFO order" )
		public void FifoQueue_ElementsRetrievedInFifoOrder( TestCase tc ) {
			this.queue.put( "A" );
			this.queue.put( "B" );
			this.queue.put( "C" );
			tc.assertEqual( "A",  this.queue.get() );
			tc.assertEqual( "B",  this.queue.get() );
			tc.assertEqual( "C",  this.queue.get() );
		}

		@Test.Impl( src = "protected Object FifoQueue.getImpl()", desc = "Get on closed empty returns null" )
		public void getImpl_GetOnClosedEmptyReturnsNull( TestCase tc ) {
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			tc.assertTrue( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Object FifoQueue.getImpl()", desc = "Get on closed non empty returns non null" )
		public void getImpl_GetOnClosedNonEmptyReturnsNonNull( TestCase tc ) {
			this.queue.put( "A" );
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			tc.assertFalse( this.queue.isEmpty() );
			tc.notNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Object FifoQueue.getImpl()", desc = "Get on open empty returns null" )
		public void getImpl_GetOnOpenEmptyReturnsNull( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
			tc.assertTrue( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Object FifoQueue.getImpl()", desc = "Get on open non empty returns non null" )
		public void getImpl_GetOnOpenNonEmptyReturnsNonNull( TestCase tc ) {
			this.queue.put( "A" );
			tc.assertTrue( this.queue.isOpen() );
			tc.assertFalse( this.queue.isEmpty() );
			tc.notNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Object FifoQueue.getImpl()", desc = "Get on terminated empty returns null" )
		public void getImpl_GetOnTerminatedEmptyReturnsNull( TestCase tc ) {
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			tc.assertTrue( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Object FifoQueue.getImpl()", desc = "Get on terminated non empty returns null" )
		public void getImpl_GetOnTerminatedNonEmptyReturnsNull( TestCase tc ) {
			this.queue.put( "A" );
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			tc.assertFalse( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected boolean FifoQueue.putImpl(Object)", desc = "Put on closed is ignored" )
		public void putImpl_PutOnClosedIsIgnored( TestCase tc ) {
			this.queue.put( "A" );
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			tc.assertFalse( this.queue.put( "B" ) );
			tc.assertEqual( "A", this.queue.get() );
		}

		@Test.Impl( src = "protected boolean FifoQueue.putImpl(Object)", desc = "Put on open is accepted" )
		public void putImpl_PutOnOpenIsAccepted( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
			tc.assertTrue( this.queue.put( "B" ) );
			tc.assertEqual( "B", this.queue.get() );
		}

		@Test.Impl( src = "protected boolean FifoQueue.putImpl(Object)", desc = "Put on terminated is ignored" )
		public void putImpl_PutOnTerminatedIsIgnored( TestCase tc ) {
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			tc.assertFalse( this.queue.put( "A" ) );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "public FifoQueue()", desc = "FifoQueues are created empty" )
		public void FifoQueue_FifoQueuesAreCreatedEmpty( TestCase tc ) {
			tc.assertTrue ( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean FifoQueue.isEmpty()", desc = "Put on empty is not empty" )
		public void isEmpty_PutOnEmptyIsNotEmpty( TestCase tc ) {
			tc.assertTrue( this.queue.isEmpty() );
			this.queue.put( "A" );
			tc.assertFalse( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean FifoQueue.isEmpty()", desc = "Put on non empty is not empty" )
		public void isEmpty_PutOnNonEmptyIsNotEmpty( TestCase tc ) {
			this.queue.put( "A" );
			tc.assertFalse( this.queue.isEmpty() );
			this.queue.put( "B" );
			tc.assertFalse( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean FifoQueue.isEmpty()", desc = "Put then get on empty is empty" )
		public void isEmpty_PutThenGetOnEmptyIsEmpty( TestCase tc ) {
			tc.assertTrue( this.queue.isEmpty() );
			this.queue.put( "A" );
			this.queue.get();
			tc.assertTrue( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean FifoQueue.isEmpty()", desc = "Put then put then get is not empty" )
		public void isEmpty_PutThenPutThenGetIsNotEmpty( TestCase tc ) {
			tc.assertTrue( this.queue.isEmpty() );
			this.queue.put( "A" );
			this.queue.put( "A" );
			this.queue.get();
			tc.assertFalse( this.queue.isEmpty() );
		}
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		System.out.println( "\nDone!" );
	}
	
}
