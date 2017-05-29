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
@FunctionalInterface
@Test.Skip
public interface Procedure {

	/**
	 * No arguments, no return, no Exception.
	 */
	public void call();
	
	public static Procedure NOOP = new Procedure() { public void call() {} };
	
}
