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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Ordinal.java,v $
 * $Id: Ordinal.java,v 1.9 2003/06/27 15:08:50 joeyr Exp $
 * $Log: Ordinal.java,v $
 * Revision 1.9  2003/06/27 15:08:50  joeyr
 * changed some CAssert to assert
 *
 * Revision 1.8  2003/06/16 18:27:14  etl
 * Added a public "list" service.
 *
 * Revision 1.7  2002/08/04 19:59:33  johnh
 * Changes supporting initial rollout of gui client
 *
 * Revision 1.6  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.5  2001/09/25 10:04:21  toms
 * Added lookup by int value.
 *
 * Revision 1.4  2001/08/22 15:01:01  toms
 * Enhanced Ordinal to enforce well-ordering.
 *
 * Revision 1.3  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.util.*;
import java.io.*;

/**
 * Extends the enumerated class and adds ordering on the enumertaed instances.
 * Instances should be constructed with distinct values (so that the total
 * order requirement of <tt>Comparable</tt> can be met.)
 */
public abstract class Ordinal extends Enum implements Comparable {

	public static class TEST extends Test {

		public static final class Level extends Ordinal {
			private Level( String label, int value ) {
				super( label, value );
			}

			private Level( int value ) {
				super( value );
			}

			public static Level HIGHEST = new Level( 5 );

			public static Level HIGH = new Level( "High", 4 );

			public static Level LOW = new Level( "Low", 3 );

			public static Level LOWEST = new Level( 0 );
		}

		public static void main( String[] args ) {
			Test t = new TEST();

			Set set = new TreeSet();
			set.add( Level.LOW );
			set.add( Level.HIGHEST );
			set.add( Level.LOWEST );
			set.add( Level.HIGH );
			set.add( Level.HIGHEST );
			Iterator i = set.iterator();
			while ( i.hasNext() ) {
				t.testEval( "Order", i.next() );
			}

			i = Ordinal.getInstances( Level.class );
			while ( i.hasNext() ) {
				t.testEval( "Value", new Integer(((Ordinal) i.next()).getValue()) );
			}

			t.testCase("numInstances works", numInstances(Level.class) == 4);

			t.testSummary();
		}
	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { Ordinal.TEST.main( args ); }

	/** Determines the order among instances */
	private int value;

	/**
	 * Construct an instance with the associated label. Its position in the
	 * order is determined by the supplied integer value.
	 *
	 * @param label
	 *      The non-empty label associated with this instance.
	 *
	 * @param value
	 *      The integer determining the instance's position in the order.
	 */
	protected Ordinal( String label, int value ) {
		super( label );
		this.value = value;
	}

	/**
	 * Construct an instance with no associated label. Its position in the
	 * order is determined by the supplied integer value.
	 *
	 * @param value
	 *      The integer determining the instance's position in the order.
	 */
	protected Ordinal( int value ) {
		super();
		this.value = value;
	}

	/**
	 * Compare with another Ordinal instance using the value field.
	 *
	 * @param other
	 *      The object to compare with.
	 */
	public int compareTo( Object other ) {
		assert( other != null );
		assert( this.getClass().equals( other.getClass() ) );

		Ordinal ord = (Ordinal)  other;
		return this.value - ord.value;
	}

	/**
	 * Gets the value that determines this instance's position in the order.
	 *
	 * @return
	 *      The integer value that represents this instance's relative position.
	 */
	public int getValue() {
		return this.value;
	}

	/**
	 * Looks up Enum instances by type and value.
	 *
	 * @param ordinalClass
	 *      A non-null class object that represents a sub-class of Ordinal. The
	 *      returned object will have this type.
	 *
	 * @param value
	 *      The value of the instance.
	 *
	 * @return
	 *      A non-null instance of the given class with the given value.
	 *      If no such instance exists the return is null.
	 */
	public static Ordinal find( Class ordinalClass, int value ) {
		assert( ordinalClass != null );
		assert Ordinal.class.isAssignableFrom( ordinalClass ) : "class: " + ordinalClass;

		Ordinal result = null;

		Iterator instance = Enum.getInstances( ordinalClass );
		while ( result == null && instance.hasNext() ) {
			result = (Ordinal) instance.next();
			if ( result.getValue() != value ) {
				result = null;
			}
		}

		return result;
	}

	/**
	 * Gets an iterator containing the instances of the given enumerated type,
	 * in the order represented by this type. This method also checks the
	 * assertion that the ordinal type is totally ordered.
	 *
	 * @param ennumClass
	 *      The non-null <tt>Class</tt> object representing an ordinal type.
	 *
	 * @return
	 *       The non-null iterator of instances of the ordinal type in
	 *       the defined order.
	 */
	public static Iterator getInstances( final Class ordinalClass ) {
		assert ordinalClass != null;
		assert Ordinal.class.isAssignableFrom( ordinalClass );


		Set set = new TreeSet();
		int size = 0;

		// We need to rebuild the iterator, because the Enum iterator does not
		// return things in the proper order.
		Iterator iter = Enum.getInstances( ordinalClass );
		while ( iter.hasNext() ) {
			set.add( iter.next() );
			assert set.size() == ++size : "Ordinal set not totally ordered: " + ordinalClass ;
		}

		Iterator result = set.iterator();
		assert( result != null );
		return result;
	}

	/**
	 * Returns the number of instances of ordinalClass that are defined.
	 *
	 * @param ordinalClass
	 *      The non-null <tt>Class</tt> object representing an ordinal type.
	 *
	 * @return
	 *       The non-null iterator of instances of the ordinal type in
	 *       the defined order.
	 */

	public static int numInstances(Class ordinalClass) {
		return ordinalClass.getFields().length;
	}
	

	public static void list( Class ordinalClass, PrintWriter out ) {
		assert ordinalClass != null;
		assert Ordinal.class.isAssignableFrom( ordinalClass );

		Iterator iter = Enum.getInstances( ordinalClass );
		while ( iter.hasNext() ) {
			Ordinal ord = (Ordinal) iter.next();
			out.println( ord.getName() + "\t" + ord.getLabel() + "\t" + ord.getValue() );
		}
	}


}
