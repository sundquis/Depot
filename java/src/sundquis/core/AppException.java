/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

// The base application exception type
@Test.Skip
public class AppException extends RuntimeException {

	private static final long serialVersionUID = -2314875945481995828L;

	/** Constructs an exception with empty detail message. */
	public AppException() {
		super();
	}

	/** Constructs an exception with specified detail message. */
	public AppException( String msg ) {
		super( msg );
	}

	/** Constructs an exception with specified cause. */
	public AppException( Throwable cause ) {
		super( cause );
	}

	/** Constructs an exception with specified detail message and cause. */
	public AppException( String msg, Throwable cause ) {
		super( msg, cause );
	}
	
}