/*

 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.util.Map;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Used to implement switch behavior for arbitrary types, especially
 * enumerated types.
 *
 * <p>
 * A typical use looks like:
 * <pre><tt>
 *  public class MyClass {
 *      public final enum State {
 *          OPEN,
 *          CLOSED
 *      }
 *
 *      private myState = State.OPEN;
 *
 *      private Switch reactor = new Switch()
 *          .addCase( State.OPEN, (x) -> f(x) )
 *          .addCase( State.CLOSED, (x) -> g(x) )
 *          .addDefault( (x) -> h(x) );
 *
 *      void someMethod() {
 *          ...
 *          Object result = reactor.apply( myState, myArg );
 *          ...
 *      }
 *  }
 * </tt></pre>
 * 
 * Three type parameters:
 * 	T	Type to switch on
 * 	A	Argument type for Function.apply
 * 	R	Result type for Function.apply
 * 
 * Uses equals for comparisons
 */
public class Switch<T, A, R> {

	/** Holds the registered handlers */
	private Map<T, Function<A, R>> handlers;
	
	/** The default case */
	private Function<A, R> defaultHandler;
	
	/** Builds a switch object associated with the given type T. */
	public Switch() {
		this.handlers = new HashMap<T, Function<A, R>>();
		this.defaultHandler = null;
	}

	/**
	 * Add a case to this switch object. Proper behavior depends on the
	 * implementation of equals on the key objects. This case object is
	 * returned to allow chaining of addCase() methods.
	 *
	 * @param key
	 *        A non-null object of type T
	 *
	 * @param handler
	 *        A non-null implementation of the apply method
	 */
	@Test.Decl( "Throws assertion error for null key" )
	@Test.Decl( "Throws assertion error for null handler" )
	@Test.Decl( "Returns non null" )
	@Test.Decl( "Returns this Switch instance" )
	public Switch<T, A, R> addCase( T key, Function<A, R> handler ) {
		Assert.nonNull( key );
		Assert.nonNull( handler );

		this.handlers.put( key, handler );

		return this;
	}

	/**
	 * Add a default handler to this switch object. If apply is called with an
	 * object that does not have an associated handler function this default handler is
	 * called. The return is this Case object to allow chaining with
	 * addCase() methods.
	 *
	 * @param handler
	 *        The non-null handler for the default case.
	 */
	@Test.Decl( "Throws assertion error for null handler" )
	@Test.Decl( "Returns non null" )
	@Test.Decl( "Returns this Switch instance" )
	public Switch<T, A, R> addDefault( Function<A, R> handler ) {
		this.defaultHandler = Assert.nonNull( handler );

		return this;
	}

	/**
	 * Called by clients to execute the corresponding apply operation.
	 *
	 * @param key
	 *		The switch value, which must be a non-null member of type T
	 *
	 * @param arg
	 *		Passed to the handler's apply method
	 *
	 * @return
	 * 		The result of the corresponding apply method
	 */
	@Test.Decl( "Throws assertion error for null key" )
	@Test.Decl( "Throws App exception when no handler found" )
	@Test.Decl( "Throws App exception when handler raises exception" )
	public R apply( T key, A arg ) {
		Assert.nonNull( key );
		
		Function<A, R> handler = this.handlers.get( key );
		if ( handler == null ) {
			handler = this.defaultHandler;
		}
		
		if ( handler == null ) {
			Fatal.error( "No handler found" );
		}
		
		R result = null;
		try {
			result = handler.apply( arg );
		} catch ( Throwable e ) {
			throw new AppException( "Exception in case handler for Switch on " + key.getClass(), e );
		}

		return result;
	}
	
	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return Switch.class;
		}
		
		public static enum State {
			OPEN,
			CLOSED,
			MURKY
		}
		
		private static Function<String, String> ID = (s) -> s;
		
		private Switch<State, String, String> sw;
		
		public Procedure beforeEach() {
			return new Procedure() {
				public void call() {
					sw = new Switch<State, String, String>();
				}
			};
		}
		
		public Procedure afterEach() {
			return new Procedure() {
				public void call() {
					sw = null;
				}
			};
		}
		
		
		// apply
		@Test.Impl( 
			src = "public Object Switch.apply(Object, Object)", 
			desc = "Throws App exception when handler raises exception" )
		public void apply_ThrowsAppExceptionWhenHandlerRaisesException( TestCase tc ) {
			tc.expectError( AppException.class );
			sw.addCase( State.OPEN, new Function<String, String> () {
				public String apply(String t) {
					return "" + 1/0;
				}
				
			});
			sw.apply( State.OPEN, "Foo" );
		}
			

		@Test.Impl( 
			src = "public Object Switch.apply(Object, Object)", 
			desc = "Throws App exception when no handler found" )
		public void apply_ThrowsAppExceptionWhenNoHandlerFound( TestCase tc ) {
			tc.expectError( AppException.class );
			sw.addCase( State.OPEN, ID ).addCase( State.CLOSED, ID );
			tc.assertEqual( "Foo",  sw.apply( State.OPEN, "Foo" ) );
			tc.assertEqual( "Foo",  sw.apply( State.CLOSED, "Foo" ) );
			sw.apply( State.MURKY, "Foo" );
		}

		@Test.Impl( 
			src = "public Object Switch.apply(Object, Object)", 
			desc = "Throws assertion error for null key" )
		public void apply_ThrowsAssertionErrorForNullKey( TestCase tc ) {
			tc.expectError( AssertionError.class );
			sw.addCase( State.OPEN, ID ).addCase( State.CLOSED, ID );
			sw.apply( null, "Foo" );
		}

		
		// addCase
		@Test.Impl( 
			src = "public Switch Switch.addCase(Object, Function)", 
			desc = "Returns non null" )
		public void addCase_ReturnsNonNull( TestCase tc ) {
			tc.notNull( sw.addCase( State.OPEN, ID ) );
		}

		@Test.Impl( 
			src = "public Switch Switch.addCase(Object, Function)", 
			desc = "Returns this Switch instance" )
		public void addCase_ReturnsThisSwitchInstance( TestCase tc ) {
			tc.assertEqual( sw, sw.addCase( State.OPEN, ID ) );
		}

		@Test.Impl( 
			src = "public Switch Switch.addCase(Object, Function)", 
			desc = "Throws assertion error for null handler" )
		public void addCase_ThrowsAssertionErrorForNullHandler( TestCase tc ) {
			tc.expectError( AssertionError.class );
			sw.addCase( State.OPEN, null );
		}

		@Test.Impl( 
			src = "public Switch Switch.addCase(Object, Function)", 
			desc = "Throws assertion error for null key" )
		public void addCase_ThrowsAssertionErrorForNullKey( TestCase tc ) {
			tc.expectError( AssertionError.class );
			sw.addCase( null, ID );
		}

		
		// addDefault
		@Test.Impl( 
			src = "public Switch Switch.addDefault(Function)", 
			desc = "Returns non null" )
		public void addDefault_ReturnsNonNull( TestCase tc ) {
			tc.notNull( sw.addDefault( ID ) );
		}

		@Test.Impl( 
			src = "public Switch Switch.addDefault(Function)", 
			desc = "Returns this Switch instance" )
		public void addDefault_ReturnsThisSwitchInstance( TestCase tc ) {
			tc.assertEqual( sw, sw.addDefault( ID ) );
		}

		@Test.Impl( 
			src = "public Switch Switch.addDefault(Function)", 
			desc = "Throws assertion error for null handler" )
		public void addDefault_ThrowsAssertionErrorForNullHandler( TestCase tc ) {
			tc.expectError( AssertionError.class );
			sw.addDefault( null );
		}
		
		
	}

	public static void main(String[] args) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();

		System.out.println("\nDone!");
	}
	
	
}
