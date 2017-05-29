/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.util.LinkedList;
import java.util.List;

import sundquis.core.Assert;
import sundquis.core.Fatal;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 * Represents the result status of a single test case
 *
 */
public class CaseResult extends Result {

	/*
	 * Case created in state UNEXECUTED
	 * Allowed transitions:
	 * 	U -> P
	 * 	U -> F
	 * 	P -> F	If the case is a compound case with multiple conditions
	 */
	@Test.Skip
	private static enum Status {
		UNEXECUTED,	// Test case has been declared but not executed
		PASSED,		// Test case passed
		FAILED		// Test case failed
	}

	
	private Status status;
	
	private long time;	
	
	private List<String> messages;
	
	private Throwable error;
	
	private int weight;
	
	private final String member;
	
	private final String subject;
	
	private String location;
	
	public CaseResult( String member, String description, String subject ) {
		super( description );
		this.status = Status.UNEXECUTED;
		this.time = 0L;
		this.messages = new LinkedList<String>();
		this.error = null;
		this.weight = 1;
		this.member = Assert.nonEmpty( member );
		this.subject = Assert.nonEmpty( subject );
		this.location = null;
	}
	

	/**
	 * Pretty-print with indented format
	 * 
	 * @see sundquis.core.test.Result#print(java.lang.String)
	 */
	@Override
	@Test.Decl( "Throws assertion error for null prefix" )
	@Test.Decl( "Adds warnings for unexecuted cases" )
	@Test.Decl( "Prints messages for non pass cases" )
	@Test.Decl( "Shows stack trace for unexpected errors" )
	public void print( String prefix ) {
		Assert.nonNull( prefix );
		
		if ( this.status == Status.UNEXECUTED ) {
			Test.addStub( this.member, this.getLabel(), this.subject );
		}
		
		if ( this.showResults() ) {
			System.err.print( prefix + this );
			if ( this.location != null && this.status != Status.PASSED ) {
				System.err.print( this.location );
			}
			System.err.println();

			if ( this.status != Status.PASSED ) {
				for ( String s : this.messages ) {
					System.err.println( "\t" + prefix + s );
				}
			}
			
			if ( this.error != null ) {
				this.error.printStackTrace();
			}
		}
	}

	/**
	 * Shows status and test description
	 * @see sundquis.core.test.Result#toString()
	 */
	@Override
	@Test.Decl( "Is not empty" )
	public String toString() {
		return this.status + " [" + this.getLabel() + "] ";
	}

	/**
	 * Unique identifier among test cases for the subject class
	 * 
	 * @return
	 */
	@Test.Decl( "Is not empty" )
	public String getKey() {
		return this.member + "." + this.getLabel();
	}
	

	/**
	 * Set the execution time for this test case
	 * @param time
	 */
	@Test.Skip
	public void setTime ( long time ) {
		this.time = time;
	}

	/**
	 * If an unexpected error occurs in TestCaseImpl
	 * @param err
	 */
	@Test.Decl( "Throws assertion error for null error" )
	public void setError( Throwable err ) {
		this.error = Assert.nonNull( err );
	}

	/**
	 * Set the location (file and line number) of the test method.
	 * This is best-effort based on StackTraceElement
	 * @param location
	 */
	@Test.Skip
	public void setLocation( String location ) {
		if ( this.location == null && location != null ) {
			this.location = location;
		}
	}

	/**
	 * Set the relative weight. Default weight is 1.
	 * @param weight
	 */
	@Test.Decl( "Throws assertion eror for non-positve weight" )
	public void setWeight( int weight ) {
		this.weight = Assert.positive( weight );
	}

	@Override
	@Test.Skip
	public int getPassCount() {
		return this.status == Status.PASSED ? this.weight : 0;
	}
	
	@Override
	@Test.Skip
	public int getFailCount() {
		return this.status == Status.FAILED ? this.weight : 0;
	}
	
	@Override
	@Test.Skip
	public int getTotalCount() {
		return this.weight;
	}
	
	@Override
	@Test.Skip
	public long getTime() {
		return this.time;
	}
	
	@Override
	@Test.Skip
	public Result addChild( Result child ) {
		Fatal.error( "Case elements do not have children" );
		return null;
	}

	@Override
	@Test.Skip
	public boolean showResults() {
		return Result.SHOW_CASE_RESULTS && (
			(this.status == Status.PASSED && Result.SHOW_PASS_CASES)
			|| (this.status == Status.FAILED && Result.SHOW_FAIL_CASES)
			|| (this.status == Status.UNEXECUTED && Result.SHOW_UNEXECUTED_CASES)
		);
	}

	/**
	 * Add a message to be displayed for non-pass cases
	 * @param message
	 */
	@Test.Decl( "Throws assertion eror for empty message" )
	public void addMessage( String message ) {
		this.messages.add( Assert.nonEmpty( message ) );
	}

	/**
	 * Mark the case passed. Can later fail if this is a compound case.
	 */
	@Test.Skip
	public void pass() {
		if ( this.status == Status.UNEXECUTED ) {
			this.status = Status.PASSED;
		}
	}
	
	/**
	 * Once a case fails it cannot change status
	 */
	@Test.Skip
	public void fail() {
		this.status = Status.FAILED;
	}
	
	
	
	

	public static class Container implements TestContainer {
		
		@Override
		public Class<?> subjectClass() {
			return CaseResult.class;
		}
		
		private CaseResult cr;
		
		public Procedure beforeEach() {
			return new Procedure() {
				public void call () {
					cr = new CaseResult( "member signature", "description", "subject" );
				}
			};
		}
		
		public Procedure afterEach() {
			return new Procedure() {
				public void call () {
					cr = null;
				}
			};
		}
		
		
		
		@Test.Impl( src = "public String CaseResult.getKey()", desc = "Is not empty" )
		public void getKey_IsNotEmpty( TestCase tc ) {
			tc.assertFalse( cr.getKey().isEmpty() );
		}

		@Test.Impl( src = "public String CaseResult.toString()", desc = "Is not empty" )
		public void toString_IsNotEmpty( TestCase tc ) {
			tc.assertFalse( cr.toString().isEmpty() );
		}

		@Test.Impl( src = "public void CaseResult.addMessage(String)", desc = "Throws assertion eror for empty message" )
		public void addMessage_ThrowsAssertionErorForEmptyMessage( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cr.addMessage( "" );
		}

		@Test.Impl( src = "public void CaseResult.print(String)", desc = "Adds warnings for unexecuted cases" )
		public void print_AddsWarningsForUnexecutedCases( TestCase tc ) {
			tc.addMessage( "Manually verified" ).pass();
		}

		@Test.Impl( src = "public void CaseResult.print(String)", desc = "Prints messages for non pass cases" )
		public void print_PrintsMessagesForNonPassCases( TestCase tc ) {
			tc.addMessage( "Manually verified" ).pass();
		}

		@Test.Impl( src = "public void CaseResult.print(String)", desc = "Shows stack trace for unexpected errors" )
		public void print_ShowsStackTraceForUnexpectedErrors( TestCase tc ) {
			tc.addMessage( "Manually verified" ).pass();
		}

		@Test.Impl( src = "public void CaseResult.print(String)", desc = "Throws assertion error for null prefix" )
		public void print_ThrowsAssertionErrorForNullPrefix( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cr.print( null );
		}

		@Test.Impl( src = "public void CaseResult.setError(Throwable)", desc = "Throws assertion error for null error" )
		public void setError_ThrowsAssertionErrorForNullError( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cr.setError( null );
		}

		@Test.Impl( src = "public void CaseResult.setWeight(int)", desc = "Throws assertion eror for non-positve weight" )
		public void setWeight_ThrowsAssertionErorForNonPositveWeight( TestCase tc ) {
			tc.expectError( AssertionError.class );
			cr.setWeight( 0 );
		}
		
		
	}

	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();

		System.out.println("\nDone!");
	}	

	
}
