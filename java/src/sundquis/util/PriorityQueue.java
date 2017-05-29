/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 */

package sundquis.util;

import java.util.SortedSet;
import java.util.TreeSet;

import sundquis.core.Assert;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;


/**
 * Implements Queue behavior. The element returned by <code>get</code>
 * is determined by the priority ranking of elements currently in the queue,
 * which in turn is derived from the <code>Comparable</code> order of
 * elements.
 *
 * <p>
 * <b>Note:</b> This implies that all elements added to a priority queue
 * must implement <code>Comparable</code> and in fact must all be
 * mutually comparable.
 *
 * <p>
 * <b>Important:</b> When implementing <code>Comparable</code> it is required
 * that the order be <i>total</i> and consistent with <tt>equals</tt>.
 * 
 * 
 * Queue Implementation:
 * 	1. Determine behavior for put( E elt ) when the queue is open and full
 * 		No full property.
 * 
 * 	2. Determine behavior for E get() when the queue is open and empty
 * 		Return null if open and empty.
 * 
 * 	3. Determine the order retrieval policy
 * 		Smallest element with respect to the Comparable property
 * 
 */
@Test.Decl( "Elements retrieved in comparable order" )
public class PriorityQueue<E extends Comparable<E>> extends AbstractQueue<E> {


	private SortedSet<E> elements;

	/**
	 * Constructs an empty priority queue. The queue is open and
	 * accepting input.
	 */
	@Test.Decl( "Can be created empty" )
	public PriorityQueue() {
		this.elements = new TreeSet<E>();
	}

	/**
	 * Constructs a priority queue containing the elements of the given
	 * TreeSet. Note: Operations on the set will affect
	 * the behavior of the queue. The queue is open and accepting input.
	 *
	 * @param elements
	 *      A sorted set of elements. The minimal element( with respect to
	 *      the implementation of <code>Comparable</code> ) is considered the
	 *      "head" of the queue and will be the first to be produced by a
	 *      call to <code>get</code>.
	 */
	@Test.Decl( "Can be created non empty" )
	public PriorityQueue( SortedSet<E> elements ) {
		this.elements = Assert.nonNull( elements );
	}

	@Override
	@Test.Decl( "Put on open is accepted" )
	@Test.Decl( "Put on closed is ignored" )
	@Test.Decl( "Put on terminated is ignored" )
	protected boolean putImpl( E elt ) {
		return this.elements.add( elt );
	}

	@Override
	@Test.Decl( "Get on open non empty returns non null" )
	@Test.Decl( "Get on open empty returns null" )
	@Test.Decl( "Get on closed non empty returns non null" )
	@Test.Decl( "Get on closed empty returns null" )
	@Test.Decl( "Get on terminated non empty returns null" )
	@Test.Decl( "Get on terminated empty returns null" )
	protected E getImpl() {
		if ( this.isEmpty() ) {
			return null;
		}
		
		E result = this.elements.first();
		this.elements.remove( result );
		return result;
	}

	@Override
	@Test.Decl( "Put on empty is not empty" )
	@Test.Decl( "Put on non empty is not empty" )
	@Test.Decl( "Put then get on empty is empty" )
	@Test.Decl( "Put then put then get is not empty" )
	public boolean isEmpty() {
		return this.elements.isEmpty();
	}
	
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return PriorityQueue.class;
		}
		
		private Queue<String> queue;

		@Override
		public Procedure beforeEach() {
			return new Procedure() {
				public void call() {
					queue = new PriorityQueue<String>();
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
		

		@Test.Impl( src = "PriorityQueue", desc = "Elements retrieved in comparable order" )
		public void PriorityQueue_ElementsRetrievedInComparableOrder( TestCase tc ) {
			this.queue.put( "C" );
			this.queue.put( "B2" );
			this.queue.put( "A" );
			this.queue.put( "B1" );
			tc.assertEqual( "A",  this.queue.get() );
			tc.assertEqual( "B1",  this.queue.get() );
			tc.assertEqual( "B2",  this.queue.get() );
			tc.assertEqual( "C",  this.queue.get() );
		}

		@Test.Impl( src = "protected Comparable PriorityQueue.getImpl()", desc = "Get on closed empty returns null" )
		public void getImpl_GetOnClosedEmptyReturnsNull( TestCase tc ) {
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			tc.assertTrue( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Comparable PriorityQueue.getImpl()", desc = "Get on closed non empty returns non null" )
		public void getImpl_GetOnClosedNonEmptyReturnsNonNull( TestCase tc ) {
			this.queue.put( "A" );
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			tc.assertFalse( this.queue.isEmpty() );
			tc.notNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Comparable PriorityQueue.getImpl()", desc = "Get on open empty returns null" )
		public void getImpl_GetOnOpenEmptyReturnsNull( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
			tc.assertTrue( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Comparable PriorityQueue.getImpl()", desc = "Get on open non empty returns non null" )
		public void getImpl_GetOnOpenNonEmptyReturnsNonNull( TestCase tc ) {
			this.queue.put( "A" );
			tc.assertTrue( this.queue.isOpen() );
			tc.assertFalse( this.queue.isEmpty() );
			tc.notNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Comparable PriorityQueue.getImpl()", desc = "Get on terminated empty returns null" )
		public void getImpl_GetOnTerminatedEmptyReturnsNull( TestCase tc ) {
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			tc.assertTrue( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected Comparable PriorityQueue.getImpl()", desc = "Get on terminated non empty returns null" )
		public void getImpl_GetOnTerminatedNonEmptyReturnsNull( TestCase tc ) {
			this.queue.put( "A" );
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			tc.assertFalse( this.queue.isEmpty() );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "protected boolean PriorityQueue.putImpl(Comparable)", desc = "Put on closed is ignored" )
		public void putImpl_PutOnClosedIsIgnored( TestCase tc ) {
			this.queue.put( "B" );
			this.queue.close();
			tc.assertTrue( this.queue.isClosed() );
			tc.assertFalse( this.queue.put( "A" ) );
			tc.assertEqual( "B", this.queue.get() );
		}

		@Test.Impl( src = "protected boolean PriorityQueue.putImpl(Comparable)", desc = "Put on open is accepted" )
		public void putImpl_PutOnOpenIsAccepted( TestCase tc ) {
			tc.assertTrue( this.queue.isOpen() );
			tc.assertTrue( this.queue.put( "B" ) );
			tc.assertEqual( "B", this.queue.get() );
		}

		@Test.Impl( src = "protected boolean PriorityQueue.putImpl(Comparable)", desc = "Put on terminated is ignored" )
		public void putImpl_PutOnTerminatedIsIgnored( TestCase tc ) {
			this.queue.terminate();
			tc.assertTrue( this.queue.isTerminated() );
			tc.assertFalse( this.queue.put( "A" ) );
			tc.isNull( this.queue.get() );
		}

		@Test.Impl( src = "public PriorityQueue()", desc = "Can be created empty" )
		public void PriorityQueue_CanBeCreatedEmpty( TestCase tc ) {
			tc.assertTrue ( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public PriorityQueue(SortedSet)", desc = "Can be created non empty" )
		public void PriorityQueue_CanBeCreatedNonEmpty( TestCase tc ) {
			TreeSet<String> set = new TreeSet<String>();
			set.add( "B" );
			set.add( "A" );
			queue = new PriorityQueue<String>( set );
			tc.assertFalse( queue.isEmpty() );
			tc.assertEqual( "A",  queue.get() );
		}

		@Test.Impl( src = "public boolean PriorityQueue.isEmpty()", desc = "Put on empty is not empty" )
		public void isEmpty_PutOnEmptyIsNotEmpty( TestCase tc ) {
			tc.assertTrue( this.queue.isEmpty() );
			this.queue.put( "A" );
			tc.assertFalse( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean PriorityQueue.isEmpty()", desc = "Put on non empty is not empty" )
		public void isEmpty_PutOnNonEmptyIsNotEmpty( TestCase tc ) {
			this.queue.put( "A" );
			tc.assertFalse( this.queue.isEmpty() );
			this.queue.put( "B" );
			tc.assertFalse( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean PriorityQueue.isEmpty()", desc = "Put then get on empty is empty" )
		public void isEmpty_PutThenGetOnEmptyIsEmpty( TestCase tc ) {
			tc.assertTrue( this.queue.isEmpty() );
			this.queue.put( "A" );
			this.queue.get();
			tc.assertTrue( this.queue.isEmpty() );
		}

		@Test.Impl( src = "public boolean PriorityQueue.isEmpty()", desc = "Put then put then get is not empty" )
		public void isEmpty_PutThenPutThenGetIsNotEmpty( TestCase tc ) {
			tc.assertTrue( this.queue.isEmpty() );
			this.queue.put( "A" );
			this.queue.put( "B" );
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
