/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import sundquis.core.Assert;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * Represents the results for all tested components of a subject class
 *
 */
public class ClassResult extends Result {

	
	public ClassResult( Class<?> clazz ) {
		super( clazz.getCanonicalName() );
	}
	

	@Test.Decl( "Throws assertion error for null class" )
	public MemberResult addMember( Class<?> c ) {
		Assert.nonNull( c );
		MemberResult result = new MemberResult( c );
		return (MemberResult) this.addChild( result );
	}

	@Test.Decl( "Throws assertion error for null constructor" )
	public MemberResult addMember( Constructor<?> c ) {
		Assert.nonNull( c );
		MemberResult result = new MemberResult( c );
		return (MemberResult) this.addChild( result );
	}

	@Test.Decl( "Throws assertion error for null method" )
	public MemberResult addMember( Method m ) {
		Assert.nonNull( m );
		MemberResult result = new MemberResult( m );
		return (MemberResult) this.addChild( result );
	}

	@Test.Decl( "Throws assertion error for null field" )
	public MemberResult addMember( Field f ) {
		Assert.nonNull( f );
		MemberResult result = new MemberResult( f );
		return (MemberResult) this.addChild( result );
	}


	
	@Override
	@Test.Skip
	public boolean showResults() {
		return Result.SHOW_CLASS_RESULTS && this.getTotalCount() > 0;
	}
	
	
	
	
	public static class Container implements TestContainer {
		
		@Override
		public Class<?> subjectClass() {
			return ClassResult.class;
		}
		
		
		private ClassResult instance;;
		
		public Procedure beforeEach() {
			return new Procedure() {
				public void call () {
					instance = new ClassResult( Test.class );
				}
			};
		}
		
		public Procedure afterEach() {
			return new Procedure() {
				public void call () {
					instance = null;
				}
			};
		}

		
		@Test.Impl( src = "public MemberResult ClassResult.addMember(Class)", desc = "Throws assertion error for null class" )
		public void addMember_ThrowsAssertionErrorForNullClass( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Class<?> clazz = null;
			instance.addMember( clazz );
		}

		@Test.Impl( src = "public MemberResult ClassResult.addMember(Constructor)", desc = "Throws assertion error for null constructor" )
		public void addMember_ThrowsAssertionErrorForNullConstructor( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Constructor<?> constructor = null;
			instance.addMember( constructor );
		}

		@Test.Impl( src = "public MemberResult ClassResult.addMember(Field)", desc = "Throws assertion error for null field" )
		public void addMember_ThrowsAssertionErrorForNullField( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Field field = null;
			instance.addMember( field );
		}

		@Test.Impl( src = "public MemberResult ClassResult.addMember(Method)", desc = "Throws assertion error for null method" )
		public void addMember_ThrowsAssertionErrorForNullMethod( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Method method = null;
			instance.addMember( method );
		}
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		System.out.println( "Done!" );
	}
	
	
}
