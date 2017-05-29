/*


 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;


import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

import sundquis.core.Test.Decl;

public final class Cache<K extends Comparable<K>, V> {
	

	/**
	 * When the value corresponding to a key is not found in the cache
	 * we use a Builder to make a value instance. The key must
	 * contain all the information needed by the builder
	 * to create the object. A Builder must never make null objects.
	 */
	@Test.Skip
	@FunctionalInterface
	public interface Builder<K, V> {
		public V make( K key ) throws AppException;
	}

	/** A SoftReference that also holds the key */
	private static final class SoftRef<K, V> extends SoftReference<V> {

		private final K key;

		SoftRef( K key, V value, ReferenceQueue<V> rq ) {
			super( value, rq );
			this.key = Assert.nonNull( key );
		}
	}


	/** The builder that creates objects for this cache. **/
	private final Builder<K, V> builder;

	/** Maintains hard references to SoftReferece objects whose referent has been collected */
	private final ReferenceQueue<V> rq;

	/** The contents of the cache are stored here. **/
	private final SortedMap<K, SoftRef<K, V>> map;

	/** Construct */
	@Decl( "Null Builder throws Assertion Error" )
	public Cache( Builder<K, V> builder ) {
		this.builder = Assert.nonNull( builder );
		this.rq = new ReferenceQueue<V>();
		this.map = new TreeMap<K, SoftRef<K, V>>();
	}

	/**
	 * Return the value corresponding to the given key. If the value is not currently
	 * held the associated builder is used to construct an instance.
	 * 
	 * @param key
	 * @return
	 * @throws AppException		If the builder is unable to construct the value.
	 */
	@Decl( "Null key throws Assertion Error" )
	@Decl( "Values are not null" )
	@Decl( "From empty cache returns valid object" )
	@Decl( "Stored uncolllectable object returns same object" )
	@Decl( "Put Get stress test" )
	@Decl( "Multi thread stress test" )
	public V get( K key ) throws AppException {
		Assert.nonNull( key );
		V value = null;

		synchronized ( this.map ) {
			SoftRef<K, V> sr = this.map.get( key );
			value = (sr == null) ? null : sr.get();
			
			this.flushQueue();

			if ( value == null ) {
				value = this.builder.make( key );
				this.map.put( key, new SoftRef<K, V>( key, value, this.rq ) );
			}
		}

		return Assert.nonNull( value );
	}

	/**
	 * Store a key-value pair. Values are held via a soft reference and may be collected.
	 * 
	 * @param key
	 * @param value
	 */
	@Decl( "Null key throws Assertion Error" )
	@Decl( "Null value throws Assertion Error" )
	public void put( K key, V value ) {
		Assert.nonNull( key );
		Assert.nonNull( value );

		synchronized ( this.map ) {
			this.flushQueue();
			this.map.put( key, new SoftRef<K, V>( key, value, this.rq ) );
		}
	}
	
	/**
	 * Remove all associations.
	 */
	// How important is this? To test need a custom value that has some non-reconstructed state
	//@Decl( "Then get() retrieves distinct instance" )
	@Decl( "Then get() retrieves equivalent value" )
	@Decl( "Cache empty after" )
	public void flush() {
		synchronized ( this.map ) {
			this.map.clear();
		}
		
	}

	/**
	 * Queue contains references whose referents have been collected.
	 * Remove these keys from the map.
	 * 
	 * Only called when lock on map is held.
	 */
	@SuppressWarnings("unchecked")
	private void flushQueue() {
		SoftRef<K, V> sr = null;
		while ( (sr = (SoftRef<K, V>) this.rq.poll()) != null ) {
			this.map.remove( sr.key );
		}
	}

	@Override
	@Test.Decl( "Result is not null" )
	@Test.Decl( "Result is not empty" )
	public String toString() {
		return "Cache(" + this.map.size() + " keys)";
	}

	
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return Cache.class;
		}

		private static class MyBuilder implements Builder<Integer, String> {
			@Override
			public String make( Integer key ) throws AppException {
				return Strings.rightJustify( key.toString(), 50, '_' );
			}
		}
		
		private static class Agent extends Thread {
			private static int ITERATIONS = 100;		// Stress: 1000
			private static int MAXIMUM = 1000;			// Stress: 10000
			private static Cache<Integer, String> cache = new Cache<Integer, String>( new MyBuilder() );
			private static void dispose() {
				Agent.cache.flush();
				Agent.cache = null;
			}
			
			@Override
			public void run() {
				for ( int i = 0; i < ITERATIONS; i++ ) {
					cache.get( (int) (Math.random() * MAXIMUM) );
					cache.put( i,  "[" + i + "]" );
					Thread.yield();
				}
			}
			
			public Thread init() {
				this.start();
				return this;
			}
		}
		
		private Cache<Integer, String> cache;
		
		@Override
		public Procedure beforeEach() {
			return new Procedure() {
				@Override
				public void call() {
					cache = new Cache<Integer, String>( new MyBuilder() );
				}
				
			};
		}
		
		@Override
		public Procedure afterEach() {
			return new Procedure() {
				@Override
				public void call() {
					cache.flush();
					cache = null;
				}
			};
		}
		
		@Test.Impl( src = "public Cache(Cache.Builder)", desc = "Null Builder throws Assertion Error" )
		public void Cache_NullBuilderThrowsAssertionError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new Cache<String, String>( null );
		}

		@Test.Impl( src = "public Object Cache.get(Comparable)", desc = "From empty cache returns valid object" )
		public void get_FromEmptyCacheReturnsValidObject( TestCase tc ) {
			tc.assertEqual( "_________________________________________________0", 
				cache.get(0)
			);
		}

		@Test.Impl( src = "public Object Cache.get(Comparable)", desc = "Multi thread stress test", weight = 10 )
		public void get_MultiThreadStressTest( TestCase tc ) {
			tc.afterThis( new Procedure() {
				public void call() { Agent.dispose(); }
			});
			ArrayList<Thread> agents = new ArrayList<Thread>();
			for ( int i = 0; i < 5; i++ ) {
				agents.add( new Agent().init() );
			}
			for ( Thread thread : agents ) {
				try {
					thread.join();
				} catch ( InterruptedException e ) {
					e.printStackTrace();
				}
			}
			// Very simplistic test of concurrency. Should add consistency checking?
			tc.assertTrue( true );
		}

		@Test.Impl( src = "public Object Cache.get(Comparable)", desc = "Null key throws Assertion Error" )
		public void get_NullKeyThrowsAssertionError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cache.get( null );
		}

		@Test.Impl( src = "public Object Cache.get(Comparable)", desc = "Put Get stress test", weight = 10 )
		public void get_PutGetStressTest( TestCase tc ) {
			LinkedList<String> values = new LinkedList<String>();
			int i;
			for ( i = 0; i < 10000; i++ ) {
				values.add( cache.get( 713 * i -12345 ) );
			}
			cache.flush();
			i = 0;
			for ( String s : values ) {
				cache.put( i++, s );
			}
			boolean getIsConsistent = true;
			i = 0;
			for ( String s : values ) {
				getIsConsistent &= s.equals( cache.get(i++) );
			}			
			tc.assertTrue( getIsConsistent );
		}

		@Test.Impl( src = "public Object Cache.get(Comparable)", desc = "Stored uncolllectable object returns same object", weight = 10 )
		public void get_StoredUncolllectableObjectReturnsSameObject( TestCase tc ) {
			cache = new Cache<Integer, String>( (x) -> Strings.rightJustify( x.toString(), 100000, '_' ) );
			int curSize = cache.map.size();  // Should be 0
			int i = 42;
			String strongReference = cache.get(i++); // Can't be collected; size() now 1
			while ( cache.map.size() > curSize ) {  // Size decreases after GC
				curSize = cache.map.size();
				for ( int j = 0; j < 100; j++ ) {  // Fill cache
					cache.get( i++ );
				}
			}
			// Some objects have been GC'd, but not our strongly held value
			tc.assertTrue( strongReference == cache.get(42) );
		}

		@Test.Impl( src = "public void Cache.put(Comparable, Object)", desc = "Null key throws Assertion Error" )
		public void put_NullKeyThrowsAssertionError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cache.put( null,  "Foo" );
		}

		@Test.Impl( src = "public void Cache.put(Comparable, Object)", desc = "Null value throws Assertion Error" )
		public void put_NullValueThrowsAssertionError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cache.put( 42,  null );
		}
		
		@Test.Impl( src = "public Object Cache.get(Comparable)", desc = "Values are not null" )
		public void get_ValuesAreNotNull( TestCase tc ) {
			tc.assertTrue( cache.get(42) != null );
		}

		@Test.Impl( src = "public void Cache.flush()", desc = "Cache empty after" )
		public void flush_CacheEmptyAfter( TestCase tc ) {
			for ( int i = 0; i < 1000; i++ ) {
				cache.get(i);
			}
			cache.flush();
			tc.assertEqual( cache.map.size(), 0 );
		}

		@Test.Impl( src = "public void Cache.flush()", desc = "Then get() retrieves equivalent value" )
		public void flush_ThenGetRetrievesEquivalentValue( TestCase tc ) {
			String orig = cache.get( 42 );
			cache.flush();
			tc.assertEqual( orig ,  cache.get(42) );
		}

		@Test.Impl( src = "public String Cache.toString()", desc = "Result is not empty" )
		public void toString_ResultIsNotEmpty( TestCase tc ) {
			tc.assertFalse( cache.toString().isEmpty() );
		}

		@Test.Impl( src = "public String Cache.toString()", desc = "Result is not null" )
		public void toString_ResultIsNotNull( TestCase tc ) {
			tc.notNull( cache.toString() );
		}

		
		
		
	}
	

	public static void main(String[] args) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();

		System.out.println("\nDone!");
	}
	
	
	
}
