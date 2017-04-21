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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Switch.java,v $
 * $Id: Switch.java,v 1.6 2002/12/09 14:05:48 toms Exp $
 * $Log: Switch.java,v $
 * Revision 1.6  2002/12/09 14:05:48  toms
 * no message
 *
 * Revision 1.5  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.4  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.3  2001/07/26 15:22:59  toms
 * Improved app config mechanism.
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.util.HashMap;

/**
 * Used to implement switch behavior for arbitrary types, especially
 * enumerated types.
 *
 * <p>
 * A typical use looks like:
 * <pre><tt>
 *  public class SomeClass {
 *      public final static class State extends Enum {
 *          private State() { }
 *          public static State OPEN = new State();
 *          public static State CLOSED = new State();
 *      }
 *
 *      private myState = State.OPEN;
 *
 *      private Switch reactor = new Switch( SomeClass.State.class )
 *          .addCase( State.OPEN, new Case() {
 *              public Object doCase( Object arg ) { ... handle the OPEN case ... }
 *          } )
 *          .addCase( State.CLOSED, new Case() {
 *              public Object doCase( Object arg ) { ... handle the CLOSED case ... }
 *          } );
 *
 *      void someMethod() {
 *          ...
 *          Object result = reactor.doSwitch( myState, myArg );
 *          ...
 *      }
 *  }
 * </tt></pre>
 *
 * @see Case;
 */
public class Switch {

	/** Holds the registered cases */
	private HashMap cases = new HashMap();
	
	/** The default case */
	private Case defaultCase = null;
	
	/** The type handled by this switch */
	private Class type = null;

	/**
	 * Builds a switch object associated with the given type.
	 *
	 * @param type
	 *      A non-null Class object determining the type of the allowed
	 *      switch arguments.
	 */
	public Switch( Class type ) {
		assert type != null;

		this.type = type;
	}

	/**
	 * Add a case to this switch object. Proper behavior depends on the
	 * implementation of equals on the key objects. This case object is
	 * returned to allow chaining of addCase() methods.
	 *
	 * @param key
	 *        A non-null object of appropriate type( as determined at
	 *      construction time ).
	 *
	 * @param handler
	 *        A non-null implementation of the Case interface, determining the
	 *        behavior associated with the key value.
	 */
	public Switch addCase( Object key, Case handler ) {
		assert type.isInstance( key );
		assert handler != null;

		this.cases.put( key, handler );

		return this;
	}

	/**
	 * Add a default case to this switch object. If doSwitch is called with an
	 * object that does not have an associated Case object this default case is
	 * called. The return is this Case object to allow chaining with
	 * addCase() methods.
	 *
	 * @param handler
	 *        The non-null handler for the default case.
	 */
	public Switch addDefault( Case handler ) {
		assert handler != null;

		this.defaultCase = handler;

		return this;
	}

	/**
	 * Called by clients to execute the corresponding "switch" statement.
	 *
	 * @param key
	 *		The switch value, which must be a non-null member of the type
	 *		registered at construction time.
	 *
	 * @param arg
	 *		Passed to the case's doCase() method
	 */
	public Object doSwitch( Object key, Object arg ) {
		assert type.isInstance( key );

		Case handler = (Case)  cases.get( key );
		if ( handler == null ) {
			handler = this.defaultCase;
		}

		if ( handler == null ) {
			throw new ConfigurationException( "No case defined for " + key );
		}

		return handler.doCase( arg );
	}

}
