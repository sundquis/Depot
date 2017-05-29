/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

/**
 * 
 * Classes that contain test cases for a subject class implement this interface.
 * 
 */
@Test.Skip
public interface TestContainer {
	
	/**
	 * The target class for which the container holds test cases
	 */
	public Class<?> subjectClass();

	
	
	/**
	 * The procedure to be called before any method invocation occurs.
	 */
	default public Procedure beforeAll() {
		return Procedure.NOOP;
	}
	
	
	/**
	 * The procedure to be called before each method invocation
	 */
	default public Procedure beforeEach() {
		return Procedure.NOOP;
	}
	

	
	/**
	 * The procedure to be called after each method invocation
	 */
	default public Procedure afterEach() {
		return Procedure.NOOP;
	}

	
	/**
	 * The procedure to be called after all method invocations have completed
	 */
	default public Procedure afterAll() {
		return Procedure.NOOP;
	}
	
	
	@Test.Skip( "May need testing. Appears correct" )
	public static String location() {
		StackTraceElement[] stes = (new Exception()).getStackTrace();
		int index = 0;
		int line = -1;
		String fileName = null;
		while ( fileName == null && index < stes.length ) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName( stes[index].getClassName() );
			} catch (ClassNotFoundException e) {
				return null;
			}
			if ( TestContainer.class.isAssignableFrom( clazz ) && ! clazz.equals( TestContainer.class ) ) {
				line = stes[index].getLineNumber();
				fileName = stes[index].getFileName();
			} else {
				index++;
			}
		}
		
		return fileName == null ? null : "(" + fileName + ":" + line + ")";
	}

	
	
}