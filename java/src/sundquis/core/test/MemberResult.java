/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import sundquis.core.Assert;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * Represents the results for all test cases associated with a member of a subject class.
 * Member may be
 * 		The class itself (not really a member)
 * 		A constructor, field, or method of the class
 * 		Recursively, elements of member classes
 */
public class MemberResult extends Result {

	// Each annotation corresponds to a test case and will generate a child CaseResult
	private final Test.Decl[] annotations;
	
	// Policy, as determined by Result, for warning untested elements of this type
	private final boolean memberWarnPolicy;
	
	// Generally warn untested public elements
	private final boolean isPublic;
	
	// Simple name of the target subject
	private final String subject;
	
	// True unless this element has been marked with a Test.Skip annotation
	private final boolean include;
	
	// Each constructor:
	//	Uses a type-specific getSimpleName() method to determine label
	//	Gathers annotations
	//	Records the appropriate warning policy from Results
	//	Records the public access status
	//	Records a simple name to serve as the subject used to generate test method names
	
	public MemberResult( Class<?> clazz ) {
		super( getSimpleName( clazz ) );
		this.annotations = clazz.getDeclaredAnnotationsByType( Test.Decl.class );
		this.include = clazz.getAnnotation( Test.Skip.class ) == null;
		this.memberWarnPolicy = Result.WARN_UNTESTED_CLASS;
		this.isPublic = Modifier.isPublic( clazz.getModifiers() );
		this.subject = clazz.getSimpleName();
	}
	
	public MemberResult( Constructor<?> constructor ) {
		super( getSimpleName( constructor ) );
		this.annotations = constructor.getDeclaredAnnotationsByType( Test.Decl.class );
		this.include = constructor.getAnnotation( Test.Skip.class ) == null;
		this.memberWarnPolicy = Result.WARN_UNTESTED_CONSTRUCTOR;
		this.isPublic = Modifier.isPublic( constructor.getModifiers() );
		this.subject = constructor.getDeclaringClass().getSimpleName();
	}
	
	public MemberResult( Field field ) {
		super( getSimpleName( field ) );
		this.annotations = field.getDeclaredAnnotationsByType( Test.Decl.class );
		this.include = field.getAnnotation( Test.Skip.class ) == null;
		this.memberWarnPolicy = Result.WARN_UNTESTED_FIELD;
		this.isPublic = Modifier.isPublic( field.getModifiers() );
		this.subject = field.getName();
	}

	public MemberResult( Method method ) {
		super( getSimpleName( method ) );
		this.annotations = method.getDeclaredAnnotationsByType( Test.Decl.class );
		this.include = method.getAnnotation( Test.Skip.class ) == null;
		this.memberWarnPolicy = Result.WARN_UNTESTED_METHOD;
		this.isPublic = Modifier.isPublic( method.getModifiers() );
		this.subject = method.getName();
	}
	

	/**
	 * Add the case corresponding to the given declaration.
	 * 
	 * This is not done in the constructor because Test needs to interact with the set of annotations.
	 * Not sure if this could be refactored...
	 * 
	 * @param declaration
	 * @return
	 */
	@Test.Decl( "Throws assertion error for null declaration" )
	public CaseResult addCase( Test.Decl declaration ) {
		Assert.nonNull( declaration );
		Assert.nonEmpty( declaration.value() );
		
		CaseResult result = new CaseResult( this.getLabel(), declaration.value(), this.subject );
		return (CaseResult) this.addChild( result );
	}
	
	/*
	 * Test calls to process each declaration
	 */
	@Test.Skip
	public Test.Decl[] getAnnotations() {
		if ( this.shouldWarn() ) {
			Test.addWarning( "No test cases for: " + this.getLabel() );
		}
		return this.annotations;
	}

	/*
	 * True unless annotated with Test.Skip
	 */
	@Test.Skip
	public boolean include() {
		return this.include;
	}
	
	private boolean shouldWarn() {
		return this.include() && this.annotations.length == 0 && this.memberWarnPolicy && (
			(this.isPublic && Result.WARN_UNTESTED_PUBLIC_MEMBERS) || Result.WARN_UNTESTED_NONPUBLIC_MEMBERS
		);
	}

	// Naming policy for member classes
	private static String getSimpleName( Class<?> clazz ) {
		return (clazz.isMemberClass() ? getSimpleName(clazz.getEnclosingClass()) + "." : "") + clazz.getSimpleName();
	}
	
	// Naming policy for constructors
	private static String getSimpleName( Constructor<?> constructor ) {
		StringBuffer buf = new StringBuffer();
		buf.append( getProtection( constructor ) );
		buf.append( getSimpleName( constructor.getDeclaringClass() ) );
		buf.append( "(" );
		boolean first = true;
		for( Class<?> par : constructor.getParameterTypes() ) {
			buf.append( first ? "" : ", " );
			buf.append( getSimpleName( par ) );
			first = false;
		}
		buf.append( ")" );
		return buf.toString();
	}
	
	// Naming policy for member fields
	private static String getSimpleName( Field field ) {
		StringBuffer buf = new StringBuffer();
		buf.append( getProtection( field ) );
		buf.append( getSimpleName( field.getType() ) );
		buf.append( " " );
		buf.append( getSimpleName( field.getDeclaringClass() ) ).append( "." ); 
		buf.append( field.getName() );
		return buf.toString();
	}
	
	// Naming policy for member methods
	private static String getSimpleName( Method method ) {
		StringBuffer buf = new StringBuffer();
		buf.append( getProtection( method ) );
		buf.append( getSimpleName( method.getReturnType() ) );
		buf.append( " " );
		buf.append( getSimpleName( method.getDeclaringClass() ) ).append( "." ); 
		buf.append( method.getName() );
		buf.append( "(" );
		boolean first = true;
		for( Class<?> par : method.getParameterTypes() ) {
			buf.append( first ? "" : ", " );
			buf.append( getSimpleName( par ) );
			first = false;
		}
		buf.append( ")" );
		return buf.toString();
	}

	// Used to assemble simple names
	private static String getProtection( Member member ) {
		int mod = member.getModifiers();
		String protection = "";
		if ( Modifier.isPublic( mod ) ) {
			protection = "public ";
		} else if ( Modifier.isProtected( mod ) ) {
			protection = "protected ";
		} else if ( Modifier.isPrivate( mod ) ) {
			protection = "private ";
		}
		return protection;
	}
	

	/**
	 * @see sundquis.core.test.Result#showResults()
	 */
	@Override
	@Test.Skip
	public boolean showResults() {
		return this.include() && Result.SHOW_MEMBER_RESULTS && this.getTotalCount() > 0;
	}
	
	
	public static class Container implements TestContainer {
		
		@Override
		public Class<?> subjectClass() {
			return MemberResult.class;
		}
		
		
		private MemberResult instance;;
		
		public Procedure beforeEach() {
			return new Procedure() {
				public void call () {
					instance = new MemberResult( Test.class );
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

		@Test.Impl( src = "public CaseResult MemberResult.addCase(Test.Decl)", desc = "Throws assertion error for null declaration" )
		public void addCase_ThrowsAssertionErrorForNullDeclaration( TestCase tc ) {
			tc.expectError( AssertionError.class );
			instance.addCase( null );
		}
		
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		System.out.println( "Done!" );
	}
	
}
