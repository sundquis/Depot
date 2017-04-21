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
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Enum.java,v $
 * $Id: Enum.java,v 1.15 2003/06/19 22:03:21 etl Exp $
 * $Log: Enum.java,v $
 * Revision 1.15  2003/06/19 22:03:21  etl
 * no message
 *
 * Revision 1.14  2003/05/27 18:54:00  etl
 * Fixed incorrect recursion base case.
 *
 * Revision 1.13  2002/11/21 23:17:13  toms
 * Altered toString
 *
 * Revision 1.12  2002/10/09 08:52:29  toms
 * Fixes to allow anonymous iunner classes
 *
 * Revision 1.11  2002/10/02 21:11:29  toms
 * no message
 *
 * Revision 1.10  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.9  2002/05/04 16:11:39  toms
 * Made toString() final. Serialization depends on it.
 *
 * Revision 1.8  2002/04/26 21:04:49  toms
 * Added method for finding Enum instance from the full name.
 *
 * Revision 1.7  2002/04/23 09:07:47  toms
 * Finalized the hashCode method
 *
 * Revision 1.6  2001/11/14 12:04:30  toms
 * Synchronized setNames() and added comment about usage.
 *
 * Revision 1.5  2001/08/22 15:01:01  toms
 * Enhanced Ordinal to enforce well-ordering.
 *
 * Revision 1.4  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.3  2001/07/27 13:47:36  toms
 * ControllableResource mod
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import com.circus.util.*;

/**
 * This class serves as the base class for all classes representing enumerated
 * types. It guarantees consistent instances across Serialization.
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
 *  }
 * </pre></tt>
 * In this case the only instances of State in the system will be
 * <tt>State.OPEN</tt> and <tt>State.CLOSED</tt>( even through
 * serialization/deserialization ).
 *
 * <p>
 * <b>Important:</b> The static instances of the nested class must be
 * public in order for the introspetive methods to work correctly.
 *
 * @see Switch
 */
public abstract class Enum implements Serializable {

	public static class TEST extends Test {

		public static class State extends Enum {
			public static State OPEN = new State( "Open" ){};
			public static State CLOSED = new State();
			private State( String label ) { super( label ); }
			private State() { super(); }
		}

		public static void main( String[] args ) {
			Test t = new TEST();

			State st = State.OPEN;
			t.testEval( "OPEN string", st.toString() );
			t.testEval( "OPEN name", st.getName() );
			t.testEval( "OPEN label", st.getLabel() );

			st = State.CLOSED;
			t.testEval( "CLOSED string", st.toString() );
			t.testEval( "CLOSED name", st.getName() );
			t.testEval( "CLOSED label", st.getLabel() );

			State open = (State)  Enum.find( State.class, "OPEN" );
			t.testCase( "Found open", open, State.OPEN );
			t.testEval( "Found == OPEN", new Boolean( open == State.OPEN ) );

			Iterator i = Enum.getLabels( State.class );
			while ( i.hasNext() ) {
				t.testEval( "  ITER", i.next() );
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos = new ObjectOutputStream( baos );
				oos.writeObject( State.OPEN );
				oos.close();
			} catch ( Exception ex ) {
				t.testFail( "Serialization", ex );
			}
			ByteArrayInputStream bais = new ByteArrayInputStream(
				baos.toByteArray() );
			State deserialized = null;
			try {
				ObjectInputStream ois = new ObjectInputStream( bais );
				deserialized = (State)  ois.readObject();
				ois.close();
			} catch ( Exception ex ) {
				t.testFail( "Serialization", ex );
			}
			t.testEval( "deserialized == OPEN",
				new Boolean( deserialized == State.OPEN ) );

			t.testSummary();
		}
	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { Enum.TEST.main( args ); }

	/**
	 * The declared name of a static instance of some concrete Enum class.
	 * This is found through introspection. It is used by toString()
	 * and in Serialization to control instances.
	 */
	private String instanceName = null;

	/**
	 * An optional label. If not supplied the instanceName serves as label.
	 */
	private String label = null;

	/**
	 * Constructs an instance with the no explicit label (the label will be
	 * the instance name). Subclasses should override with a private
	 * constructor and supply public static instances.
	 */
	protected Enum() {}

	/**
	 * Constructs an instance with the specified label. Subclasses should
	 * override with a private constructor and supply public static instances.
	 *
	 * @param label
	 *      The label associated with this instance.
	 */
	protected Enum( String label ) {
		assert Strings.isNotEmpty( label );

		this.label = label;
	}

	/**
	 * Cloning is strictly dis-allowed.
	 */
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException( "Cannot clone an enumerated "
			+ "instance." );
	}

	/**
	 * Determines equality based on identity. This is made possible through
	 * careful management of instances.
	 *
	 * @param other
	 *      The other object to test for equality.
	 *
	 * @return
	 *      True if this instance and the other are the same instance.
	 */
	public final boolean equals( Object other ) {
		return this == other;
	}

	public final int hashCode() {
		return super.hashCode();
	}

	/**
	 * Gets the name of this instance.
	 *
	 * @return
	 *      The non-null string name of this instance.
	 */
	public String getName() {
		setName();

		assert Strings.isNotEmpty( this.instanceName );
		return this.instanceName;
	}

	/**
	 * Gets the label associated with this enumerated instance. The label is
	 * the string supplied when constructed, if supplied, otherwise the
	 * instnace name is used.
	 *
	 * @return
	 *      The non-null string label associated with this instance.
	 */
	public String getLabel() {
		String result = this.label;

		if ( result == null ) {
			result = this.getName();
		}

		assert Strings.isNotEmpty( result );
		return result;
	}

	/**
	 * Provides a string representation. The representation consists of the
	 * fully qualified class name together with the instance name.
	 *
	 * @return
	 *      The non-null string representation for this enumerated instance.
	 */
	public String toString() {
		return this.getLabel();
	}

	/**
	 * Used in serialization and persistence operations.
	 */
	public String toFullName() {
		return this.getClass().getName() + "." + this.getName();
	}
	
	/**
	 * Looks up Enum instances by fully qualified instance name.
	 * This is used in the de-serialization process and can also be used by
	 * other persistence mechanisms.
	 *
	 * @param fullName
	 *      A non-empty string representing the full qualified instance name
	 *      as returned by toFullName()
	 *
	 * @return
	 *      A non-null instance of the given class with the given name.
	 *      If no such instance exists the return is null.
	 */
	public static Enum find( String fullName ) {
		assert Strings.isNotEmpty( fullName );

		int index = fullName.lastIndexOf( "." );
		assert index > 0;
		String className = fullName.substring( 0, index );
		String name = fullName.substring( index + 1 );

		Class enumClass = null;
		try {
			enumClass = Class.forName( className );
		} catch ( Exception ex ) {}

		Enum result = null;
		if ( Strings.isNotEmpty( name ) && enumClass != null
				&& Enum.class.isAssignableFrom( enumClass ) ) {
			result = Enum.find( enumClass, name );
		}

		return result;
	}

	/**
	 * Looks up Enum instances by type and instance name. This is used in the
	 * de-serialization process and can also be used by other persistence
	 * mechanisms.
	 *
	 * @param enumClass
	 *      A non-null class object that represents a sub-class of Enum. The
	 *      returned object will have this type.
	 *
	 * @param name
	 *      A non-empty string representing the name of the instance.
	 *
	 * @return
	 *      A non-null instance of the given class with the given name.
	 *      If no such instance exists the return is null.
	 */
	public static Enum find( Class enumClass, String name ) {
		if ( enumClass == null || !Enum.class.isAssignableFrom( enumClass ) ) {
			return null;
		}
		
		assert Enum.class.isAssignableFrom( enumClass );
		assert Strings.isNotEmpty( name );

		Enum result = null;

		Field[] fields = enumClass.getFields();
		int i = 0;
		while ( result == null && i < fields.length ) {
			if ( fields[i].getType().equals( enumClass ) && fields[i].getName().equals( name ) ) {
				try {
					result = (Enum)  fields[i].get( null );
				} catch ( IllegalAccessException ex ) {
					Fatal.impossible( ex );
				}
			}
			i++;
		}
		
		if ( result == null ) {
			result = Enum.find( enumClass.getSuperclass(), name );
		}

		return result;
	}

	/**
	 * Gets an iterator containing the string labels of each instance.
	 *
	 * @param ennumClass
	 *      The non-null <tt>Class</tt> object representing an enumerated type.
	 *
	 * @return
	 *       The non-null iterator of instance names.
	 */
	public static Iterator getLabels( final Class enumClass ) {
		assert enumClass != null;
		assert Enum.class.isAssignableFrom( enumClass );

		return new AbstractIterator() {
			private Field[] fields = enumClass.getFields();
			private int index = 0;

			public Object getNext() {
				Object result = null;
				Enum enum = null;
				while ( result == null && fields != null
						&& index < fields.length ) {
					if ( fields[index].getType().equals( enumClass ) ) {
						try {
							enum = (Enum)  fields[index].get( null );
						} catch ( IllegalAccessException ex ) {
							Fatal.impossible( ex );
						}
						result = enum.getLabel();
					}
					index++;
				}
				return result;
			}
		};
	}

	/**
	 * Gets an iterator containing the instances of the given enumerated type.
	 *
	 * @param ennumClass
	 *      The non-null <tt>Class</tt> object representing an enumerated type.
	 *
	 * @return
	 *       The non-null iterator of instances of the enumerated type.
	 */
	public static Iterator getInstances( final Class enumClass ) {
		assert enumClass != null;
		assert Enum.class.isAssignableFrom( enumClass );

		return new AbstractIterator() {
			private Field[] fields = enumClass.getFields();
			private int index = 0;

			public Object getNext() {
				Enum enum = null;
				while ( enum == null && fields != null
						&& index < fields.length ) {
					if ( fields[index].getType().equals( enumClass ) ) {
						try {
							enum = (Enum)  fields[index].get( null );
						} catch ( IllegalAccessException ex ) {
							Fatal.impossible( ex );
						}
					}
					index++;
				}
				return enum;
			}
		};
	}

	/**
	 * Gets the enumerated instance by the same name. (Used in the
	 * de-serialization process.) This object has been de-serialized and
	 * we need to replace the de-serialized object with the correct
	 * enumerated instance. During serialization we ensure that the name
	 * is correclty written to the stream so that we can use it to look
	 * up the correct instance.
	 *
	 * @return
	 *      The enumerated instance with the same name as this instance.
	 */
	protected Object readResolve() {
		Object result = find( this.getClass(), this.instanceName );

		assert result != null;
		return result;
	}

	/**
	 * Before serialization occurs we need to make sure that the name field
	 * has been set so that it can be re-read in de-serialization.
	 *
	 * @param oos
	 *      The ObjectOutputStream provided bby the serialization mechanism.
	 */
	private void writeObject( ObjectOutputStream oos ) throws IOException {
		setName();
		oos.defaultWriteObject();
	}

	/**
	 * Ensures that the name on this instance has been set. This can only
	 * be determined from an instance context (after the class has been loaded)
	 */
	private synchronized void setName() {
		if ( this.instanceName == null ) {
			setNames();
		}

		assert this.instanceName != null;
	}

	/**
	 * Sets the name for this instance and all other static instances of
	 * this instance's class.
	 */
	private void setNames() {
		Class myClass = this.getClass();
		
		while ( this.instanceName == null && Enum.class.isAssignableFrom( myClass ) ) {
			Field[] fields = myClass.getFields();
			Enum enum = null;
			for ( int i = 0; i < fields.length; i++ ) {
				if ( Enum.class.isAssignableFrom(fields[i].getType()) ) {
					try {
						enum = (Enum)  fields[i].get( null );
					} catch ( IllegalAccessException ex ) {
						//Fatal.impossible( ex );
					}
					if ( enum != null ) {
						enum.instanceName = fields[i].getName();
					}
				}
			}
			
			myClass = myClass.getSuperclass();
		}

		assert this.instanceName != null;
	}

}
