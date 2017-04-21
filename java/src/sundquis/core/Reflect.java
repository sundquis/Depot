/*
 * Copyright( c ) 2001 by Circus Software.
 * All rights reserved.
 *
 * This source code, and its documentation, are the confidential intellectual
 * property of Circus Software and may not be disclosed, reproduced,
 * distributed, or otherwise used for any purpose without the expressed
 * written permission of Circus Software.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Reflect.java,v $
 * $Id: Reflect.java,v 1.9 2003/02/02 19:53:48 johnh Exp $
 * $Log: Reflect.java,v $
 * Revision 1.9  2003/02/02 19:53:48  johnh
 * Changed some Contract.cAsserts to assert
 *
 * Revision 1.8  2002/10/01 01:10:03  toms
 * Added printStackTrace on invocation failure.
 *
 * Revision 1.7  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.6  2002/05/20 20:35:53  toms
 * Altered reflection mechanism to find interfaces.
 *
 * Revision 1.5  2002/05/04 16:09:57  toms
 * Added support for finding interface types
 *
 * Revision 1.4  2001/11/14 12:03:36  toms
 * Refined exception handling.
 *
 * Revision 1.3  2001/10/18 15:36:04  toms
 * New Trace facility.
 *
 * Revision 1.2  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.1  2001/08/02 20:55:47  toms
 * Support for reflective polymorphism.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.lang.reflect.*;

/**
 *
 */
public final class Reflect {

	private Reflect() {}

	/**
	 * Simulate type-specific callbacks for situations where we do not have
	 * access to the source code. The drawback to this approach is that
	 * certain errors normally found at compile time are delayed until runtime.
	 * Any use of this mechanism should be accompanied with carefully
	 * constructed test cases.
	 *
	 * <p>
	 * For example, suppose we have a base class
	 * A, extended by two subclasses B and C, and suppose we would like
	 * a polymorphic response to the "foo" message. If we have control over
	 * the source for A, B, and C we would simply implement A.foo(), B.foo(),
	 * and C.foo(). But there are cases where we do not have access to sources
	 *( for example, A, B, and C might be generated classes ). In such a case
	 * we can use <tt>Reflect.invoke</tt> to simulate the polymorphic
	 * response.
	 *
	 * <p>
	 * For example, the previous scenario could be handled by defining a class
	 * that implememnts type-specific methods:
	 * <pre><tt>
	 *  public class SomeClass {
	 *      public Object foo( A arg ) {
	 *          // Do something with arg
	 *      }
	 *      public Object foo( B arg ) {
	 *          // Do something with arg
	 *      }
	 *      public Object foo( C arg ) {
	 *          // Do something with arg
	 *      }
	 *  }
	 * </tt></pre>
	 * Then some client can get a type-specific repsonse using:
	 * <tt><pre>
	 *  SomeClass handler = new SomeClass();
	 *  A arg = </tt>... get an instance of unknown actual type<tt>
	 *  Reflect.invoke( handler, "foo", arg );
	 * </pre></tt>
	 *
	 * This class properly handles the case where the desired method is
	 * defined for a parent type. For example, in the previous example it
	 * is possible to use a handler such as
	 * <pre><tt>
	 *  public class OtherClass {
	 *      public Object foo( A arg ) {
	 *          // Do something with arg
	 *      }
	 *      public Object foo( C arg ) {
	 *          // Do something with arg
	 *      }
	 *  }
	 * </tt></pre>
	 * Invocations for arguments of types <tt>A</tt> or <tt>C</tt> are handled
	 * as expected, and invocations for type <tt>B</tt> are handled by
	 * <tt>foo( A arg )</tt> as desired.
	 *
	 * @param target
	 *      The non-null object that holds type-specific implementations for
	 *      the given method.
	 *
	 * @param methodName
	 *      The non-empty method name.
	 *
	 * @param arg
	 *      The non-null argument to be passed to the target method.
	 *
	 * @return
	 *      The return value( possibly null ) of the target method.
	 */
	public static Object invoke( Object target, String methodName, Object arg ) {
		assert ( target != null );
		assert ( Strings.isNotEmpty( methodName ) );
		assert ( arg != null );

		Method method = null;

		Class targetClass = target.getClass();
		Class argClass = arg.getClass();
		Class[] interfaces = null;
		int index = 0;

		while ( method == null && argClass != null ) {
			interfaces = argClass.getInterfaces();
			index = 0;
			while ( method == null && index < interfaces.length ) {
				try {
					method = targetClass.getDeclaredMethod( methodName,
						new Class[] { interfaces[index] } );
				} catch ( NoSuchMethodException ex ) {
					index++;
				}
			}
			if ( method == null ) {
				argClass = argClass.getSuperclass();
			}
		}

		argClass = arg.getClass();
		while ( method == null && argClass != null ) {
			try {
				method = targetClass.getDeclaredMethod( methodName,
					new Class[] { argClass } );
			} catch ( NoSuchMethodException ex ) {
				argClass = argClass.getSuperclass();
			}
		}

		if ( method == null ) {
			throw new ConfigurationException( "Reflect: No suitable method" );
		}

		Object result = null;
		method.setAccessible( true );
		try {
			result = method.invoke( target, new Object[] { arg } );
		} catch ( IllegalAccessException ex ) {
			throw new ConfigurationException( ex );
		} catch ( IllegalArgumentException ex ) {
			throw new ConfigurationException( ex );
		} catch ( InvocationTargetException ex ) {
			ex.printStackTrace();
			throw new ConfigurationException( "Invocation error", ex.getTargetException() );
		}

		return result;
	}

}
