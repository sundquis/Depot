/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;


/**
 * @author sundquis
 *
 */
@Test.Skip
public interface TestCase {


	/**
	 * Only displayed for non-passing cases
	 * 
	 * @param message
	 * 		Failure message
	 * @return
	 * 		this TestCase
	 */
	public TestCase addMessage( String message );


	/**
	 * Type of Throwable that subsequent code is expected to throw
	 * 
	 * @param expectedError
	 * 		Throwable tpye
	 * @return
	 * 		this TestCase
	 */
	public TestCase expectError( Class<? extends Throwable> expectedError );


	/**
	 * Procedure to call after the current method (in a TestContainer) completes
	 * Will be called even if the method throws an exception.
	 * 
	 * @param callafter
	 * @return
	 * 		this TestCase
	 */
	public TestCase afterThis( Procedure callafter );

	
	/**
	 * Mark the current case as successful.
	 * 
	 * @return
	 * 		this TestCase
	 */
	public TestCase pass();


	/**
	 * Mark the current case as failed.
	 * 
	 * @return
	 * 		this TestCase
	 */
	public TestCase fail();
	

	/**
	 * Assert that the given object is not null.
	 * 
	 * @param obj
	 * @return
	 * 		this TestCase
	 */
	public TestCase notNull( Object obj );
	
	
	/**
	 * Assert that the given object is not null.
	 * 
	 * @param obj
	 * @return
	 * 		this TestCase
	 */
	public TestCase isNull( Object obj );
	
	
	/**
	 * Conditionally mark the case as passed/failed.
	 *  
	 * @param passIfTrue
	 * @return
	 * 		this TestCase
	 */
	public TestCase assertTrue( boolean passIfTrue );
	
	
	/**
	 * Conditionally mark the case as passed/failed.
	 *  
	 * @param passIfFalse
	 * @return
	 * 		this TestCase
	 */
	public TestCase assertFalse( boolean passIfFalse);
	
	
	/**
	 * Test for equality using Object.equals().
	 * If T is a compound type (array or collection) then the components are shallowly tested.
	 * 
	 * @param expected
	 * @param actual
	 * @return
	 * 		this TestCase
	 */
	public <T> TestCase assertEqual( T expected, T actual );
	
	
}
