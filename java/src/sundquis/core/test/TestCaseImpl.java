/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import sundquis.core.Assert;
import sundquis.core.Objects;
import sundquis.core.Procedure;
import sundquis.core.Strings;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * @author sundquis
 *
 */
@Test.Skip
public class TestCaseImpl implements TestCase, Runnable, Comparable<TestCaseImpl> {
	
	// Meta data for this test case; used to implement Comparable
	private final Test.Impl impl;
	
	// Container instance on which to invoke the corresponding method
	private final TestContainer testContainer;
	
	// The method implementing the test
	private final Method method;
	
	// The result object for reporting
	private final CaseResult cr;
	
	// Type of exception expected to be thrown by the method invocation
	private volatile Class<? extends Throwable> expectedError;
	
	// Optional procedure to call after invocation, before the global TestContainer.afterAll
	// and after the TestContainer.afterEach procedure
	private volatile Procedure afterThis;
	
	public TestCaseImpl( Test.Impl impl, TestContainer testContainer, Method method, CaseResult cr ) {
		this.impl = Assert.nonNull( impl );
		this.testContainer = Assert.nonNull( testContainer );
		this.method = Assert.nonNull( method );
		this.cr = Assert.nonNull( cr );
		this.cr.setWeight( impl.weight() );
		this.afterThis = Procedure.NOOP;
		this.expectedError = null;
	}

	@Override
	public int compareTo( TestCaseImpl other ) {
		Assert.nonNull( other );

		int result = this.impl.priority() - other.impl.priority();
		if ( result == 0 ) {
			result = this.impl.src().compareTo( other.impl.src() );
			if ( result == 0 ) {
				result = this.impl.desc().compareTo( other.impl.desc() );
			}
		}
		return result;
	}
	
	@Override
	public boolean equals( Object other ) {
		if ( other == null ) {
			return false;
		}
		
		if ( other instanceof TestCaseImpl ) {
			return this.compareTo( (TestCaseImpl) other ) == 0;
		}
		
		return false;
	}

	// NOTE: Should not be run by multi-threaded workers as TestContainer implementations generally
	// will not be thread-safe.
	// BUT: Could run in a separate thread and monitor for errors/progress
	// In that case should use Test.Impl.timeout
	@Override
	public void run() {
		long start = System.currentTimeMillis();
		this.testContainer.beforeEach().call();
		try {
			this.method.invoke( this.testContainer, (TestCase) this );
			if ( this.expectedError != null ) {
				this.cr.fail();
				this.cr.addMessage( "No exception thrown, expected " + this.expectedError );
			}
			this.cr.setTime( System.currentTimeMillis() - start );
		} catch ( InvocationTargetException ex ) {
			if ( this.expectedError != null && 
					this.expectedError.equals( ex.getCause().getClass() ) ) {
				this.cr.pass();
				this.cr.setTime( System.currentTimeMillis() - start );
			} else {
				this.cr.fail();
				this.cr.addMessage( "Unexpected exception" );
				this.cr.setError( ex );
				this.cr.setTime( System.currentTimeMillis() - start );
			}
		} catch ( Throwable err ) {
			this.cr.fail();
			this.cr.addMessage( "Unexpected exception" );
			this.cr.setError( err );
			this.cr.setTime( System.currentTimeMillis() - start );
		} finally {
			this.afterThis.call();
			this.testContainer.afterEach().call();
		}
	}
	
	@Override
	public TestCase addMessage( String message ) {
		this.setLocation();
		this.cr.addMessage( Assert.nonEmpty( message ) );
		return this;
	}
	
	@Override
	public TestCase expectError( Class<? extends Throwable> expectedError ) {
		this.setLocation();
		this.expectedError = Assert.nonNull( expectedError );
		return this;
	}
	
	@Override
	public TestCase afterThis( Procedure afterThis ) {
		this.setLocation();
		this.afterThis = Assert.nonNull( afterThis );
		return this;
	}
	
	@Override
	public TestCase pass() {
		this.setLocation();
		this.cr.pass();
		return this;
	}

	@Override
	public TestCase fail() {
		this.setLocation();
		this.cr.fail();
		return this;
	}
	
	@Override
	public TestCase notNull( Object obj ) {
		this.setLocation();
		if ( obj == null ) {
			this.fail();
			this.addMessage( "Expeceted non-null: " + TestContainer.location() );
		} else {
			this.pass();
		}
		return this;
	}

	@Override
	public TestCase isNull( Object obj ) {
		this.setLocation();
		if ( obj == null ) {
			this.pass();
		} else {
			this.fail();
			this.addMessage( "Expeceted non-null: " + TestContainer.location() );
		}
		return this;
	}

	@Override
	public TestCase assertFalse( boolean passIfFalse ) {
		this.setLocation();
		if ( passIfFalse ) {
			this.fail();
			this.addMessage( "Expected false: " + TestContainer.location() );
		} else {
			this.pass();
		}
		return this;
	}
	
	@Override
	public TestCase assertTrue( boolean passIfTrue ) {
		this.setLocation();
		if ( passIfTrue ) {
			this.pass();
		} else {
			this.fail();
			this.addMessage( "Expected true: " + TestContainer.location() );
		}
		return this;
	}

	@Override
	public <T> TestCase assertEqual( T expected, T actual ) {
		this.setLocation();
		if ( Objects.shallowEquals( expected, actual ) ) {
			this.pass();
		} else {
			this.fail();
			this.addMessage( "Expected: " + Strings.toString( expected ) + ", Got: " + Strings.toString( actual ) + ": " + TestContainer.location() );
		}
		
		return this;
	}

	@Override
	public String toString() {
		return "TestCase(" + this.impl + ")";
	}
	
	private void setLocation() {
		this.cr.setLocation( TestContainer.location() );
	}
	
	
	



	
	public static void main(String[] args) {

		System.out.println();
		
		System.out.println("\nDone!");
	}


}
