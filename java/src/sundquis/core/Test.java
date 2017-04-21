/**
 * Copyright  (C) 2017 by TS Sundquist
 * All rights reserved.
 * 
 */

package sundquis.core;

/**
 * Holds core test support. Classes provide their own test code by defining
 * a static nested class called TEST that extends core.Test.
 *
 * <p>
 * For example:
 * <pre><tt>
 *  public class MyClass {
 *      public static class TEST extends Test {
 *          public static void main( String[] args ) {
 *              TEST t = new TEST();
 *              // set up cases...
 *              t.testCase( "Case 1", arg1, target1 );
 *              t.testCase( "Case 2", arg2, target2 );
 *              t.testCase( "Case 3", arg3, target3 );
 *              t.testSummary();
 *          }
 *      }
 *      ...
 *  }
 * </pre></tt>
 * <p>

 * In addition Test keeps cummulative counts of all test results, even
 * between invokations!  As long as the method <tt>totalTestSummary()
 * is invoked between JVM invokations.  See <tt>ExampleUse</tt> for an
 * example of this.

 */
public class Test {
	/**
	 * Runtime property that controls the visual display of feedback. If
	 * <tt>true</tt>, each test case produces detailed description of the
	 * test case. Otherwise, messages are only produced for failures.
	 */
	private static final boolean verbose = Boolean.getBoolean( "test.verbose" );
    /**
	 * Runtime property that controls visual display of feedback.  If
	 * <tt>true</tt>, each failed test case produces detailed descriptions of the test case.
	 **/
	private static final boolean failureVerbose = Boolean.getBoolean( "test.failureVerbose" );
	public static boolean getVerbose() {return verbose;};

	/** How many test cases for this class */
	private int testCaseCount = 0;

	/** number of testCases in the lifetime of the Test run **/
	private static int totalTestCaseCount = 0;

	/** How many test failures for this class */
	private int testCaseFailCount = 0;

	public interface Exceptional {
		void causeException() throws Throwable;
	}

	/** number of failure in the lifetime of the Test Run **/
	private static int totalTestCaseFailCount = 0;

	/**
	 * Constructs a new Test object with counts reset to zero.
	 */
	public Test() {}

	/**
	 * Checks for equality of the given argument and target value. If the
	 * <tt>test.verbose</tt> property is <tt>true</tt>, diagnostic information
	 * is sent to the standard output. If the test case fails( the argument
	 * does not match the target value ) an error message is printed.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 *
	 * @param arg
	 *      The test case argument.
	 *
	 * @param target
	 *      The desired target value against which to compare the argument.
	 */
	public void testCase( String identifier, Object arg,
			Object target ) {
		assert Strings.isNotEmpty( identifier );

		this.testCaseCount++;
		totalTestCaseCount++;

		boolean pass = ( arg == null && target == null )
			|| ( arg != null && arg.equals( target ) );

		if ( verbose || (!pass && failureVerbose)) {
			System.out.println( "Case: " + identifier );
			System.out.println( "  Arg:\t" + arg );
			System.out.println( "  Targ:\t" + target );
			System.out.println( "  " +( pass ? "PASS" : "FAIL" ) );
		}

		if ( !pass ) {
			this.testCaseFailCount++;
			totalTestCaseFailCount++;
			System.out.println( "FAILURE: Case \"" + identifier + "\" failed in "
				+ this.getClass().getName() );
		}
	}

	/**
	 * Checks for true in the given argument. If the
	 * <tt>test.verbose</tt> property is <tt>true</tt>, diagnostic
	 * information is sent to the standard output. If the test
	 * case fails( the argument is false ) an error message is
	 * printed.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 *
	 * @param pass
	 *      The pass/fail status of the test.
	 * */
	public void testCase( String identifier, boolean pass) {
		Contract.cAssert( Strings.isNotEmpty( identifier ) );

		this.testCaseCount++;
		totalTestCaseCount++;

  		if ( verbose || (!pass && failureVerbose) ) {
			System.out.println( "Case: " + identifier );
			System.out.println( "  " +( pass ? "PASS" : "FAIL" ) );
		}

		if ( !pass ) {
			this.testCaseFailCount++;
			totalTestCaseFailCount++;
			System.out.println( "FAILURE: Case \"" + identifier + "\" failed in "
				+ this.getClass().getName() );
		}
	}


	/**
	 * Allows for exercise of methods. If the
	 * <tt>test.verbose</tt> property is <tt>true</tt>, diagnostic information
	 * is sent to the standard output. Each eval is counted as a
	 * successful test case.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 *
	 * @param arg
	 *      The test case argument.
	 */
	public void testEval( String identifier, Object arg ) {
		Contract.cAssert( Strings.isNotEmpty( identifier ) );

		this.testCaseCount++;
		totalTestCaseCount++;
		if ( verbose ) {
			System.out.println( "Eval: " + identifier );
			System.out.println( "  PASS" );
			System.out.println( "  Arg:\t" + arg );
		}
	}

	/**
	 * Allows for testing of exceptional cases. If the
	 * <tt>test.verbose</tt> property is <tt>true</tt>, diagnostic information
	 * is sent to the standard output. Each eval is counted as a
	 * successful test case.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 *
	 * @param th
	 *      The throwable instance.
	 *
	 * @deprecated Use <TT>Exceptional</TT> version
	 */
	public void testException( String identifier, Throwable th ) {
		assert Strings.isNotEmpty( identifier );

		this.testCaseCount++;
		totalTestCaseCount++;
		if ( verbose ) {
			System.out.println( "Exception: " + identifier );
			th.printStackTrace( System.out );
		}
	}


	/**
	 *
	 * Not deprecated, as we don't have a complete solution yet,
	 * although in general the reflection version of this method is
	 * preferred.
	 **/

	public void testException( String identifier, Exceptional object,
		Class exceptionType ) {

		Contract.cAssert( Strings.isNotEmpty( identifier ) );
		Contract.cAssert( object != null );
		Contract.cAssert( exceptionType != null );


		Throwable thrown = null;
		this.testCaseCount++;
		Test.totalTestCaseCount++;
		boolean pass = false;
		try {
			object.causeException();
		} catch ( Throwable th ) {
			thrown = th;
			pass = exceptionType.isAssignableFrom( th.getClass() );
		}

		if ( verbose ) {
			System.out.println( "Case: " + identifier );
			System.out.println( "  Expect type:\t" + exceptionType );
			System.out.println( "  Threw:\t" + thrown );
			System.out.println( "  " +( pass ? "PASS" : "FAIL" ) );
			if ( thrown != null && !pass ) {
				thrown.printStackTrace();
			}
		}

		if ( !pass ) {
			this.testCaseFailCount++;
			Test.totalTestCaseFailCount++;
			if (thrown == null) {
				System.out.println( "FAILURE: Case \"" + identifier + "\" failed.  No exception thrown.  In "
									+ this.getClass().getName() );
			} else {
				System.out.println( "FAILURE: Case \"" + identifier + "\" failed.  Exception " + thrown.getClass().getName() + " thrown.  In "
									+ this.getClass().getName() );
			}
		}
	}



	/**
	 * Allows for testing of exceptional cases. If the
	 * <tt>test.verbose</tt> property is <tt>true</tt>, diagnostic information
	 * is sent to the standard output. Each eval is counted as a
	 * successful test case.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 * @param obj
	 *      The object on which a method that throws an exception is to
	 *      be invoked.
	 * @param method
	 *      The name of the method, as a string
	 * @param args
	 *      The arguments of the method, if no arguments, this can be null.
	 *      However, all the elements of the array must be non null.
	 * @param exceptionType[]
	 *      an array of exceptions that are expected to be thrown.
	 *      The exceptions are assumed to be nested/wrapped
	 *      exceptions.  
	 */
	public void testException( String identifier,
							   Object obj,
							   String method,
							   Object[] args,
							   Class[] exceptionType ) {

		assert ( Strings.isNotEmpty( identifier ) );
		assert ( Strings.isNotEmpty( method ) );
		assert ( obj != null );
		assert ( exceptionType != null );
		assert ( exceptionType.length > 0 );

		Throwable thrown = null;
		this.testCaseCount++;
		Test.totalTestCaseCount++;
		boolean pass = true;

		try {
                    // FIXME
                    //org.apache.commons.beanutils.MethodUtils.invokeMethod(obj, method, args);
			pass = false;
//		} catch ( InvocationTargetException ex ) {
//			thrown = ex.getTargetException();
		} catch ( Throwable th ) {
			thrown = th;
			pass = false;
		}

		if (pass) {
			Throwable thrownObject = thrown;
			try {
				for (int i = 0; i < exceptionType.length && pass; i++) {
					pass = exceptionType[i].isAssignableFrom( thrownObject.getClass() );
					thrownObject = thrownObject.getCause();
				}
			} catch (Throwable t) {
				pass = false;
			}
		}


		// print out and compute results.
		if ( verbose || (!pass && failureVerbose) ) {
			System.out.println( "Case: " + identifier );
			Throwable thrownObject = thrown;
			int i = 0;
			for (; i < exceptionType.length && thrownObject != null; i++) {
				System.out.println( "  Expect type[" + i + "]:\t" + exceptionType[i] );
				System.out.println( "  Threw  type[" + i + "]:\t" + thrownObject.getClass().getName() );
				thrownObject = thrownObject.getCause();
			}
			if (thrownObject == null && i < exceptionType.length) {
				System.out.println("   Expect type[" + i + "]:\t" + exceptionType[i]);
				System.out.println("   Threw  type[" + i + "]:\t" + thrownObject);
			}
			System.out.println( "  " +( pass ? "PASS" : "FAIL" ) );
			System.out.flush();
			if ( thrown != null && !pass ) {
				thrown.printStackTrace();
			}
		}

		if ( !pass ) {
			this.testCaseFailCount++;
			Test.totalTestCaseFailCount++;
			System.out.println( "FAILURE: Case \"" + identifier + "\" failed in "
				+ this.getClass().getName() );
		}
	}


	/**
	 * Allows for testing of exceptional cases. If the
	 * <tt>test.verbose</tt> property is <tt>true</tt>, diagnostic information
	 * is sent to the standard output. Each eval is counted as a
	 * successful test case.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 * @param obj
	 *      The object on which a method that throws an exception is to
	 *      be invoked.
	 * @param method
	 *      The name of the method, as a string
	 * @param args
	 *      The arguments of the method, if no arguments, this can be null.
	 *      However, all the elements of the array must be non null.
	 * @param exceptionType
	 *      The type of the exception that is expected to be thrown.
	 */
	public void testException( String identifier,
							   Object obj,
							   String method,
							   Object[] args,
							   Class exceptionType ) {
		testException(identifier, obj, method, args, new Class[] {exceptionType});
	}


	/**
	 * Reports a test failure. If the <tt>test.verbose</tt> property is
	 * <tt>true</tt>, diagnostic information is sent to the standard output.
	 * Each eval is counted as a test failure.
	 *
	 * @param identifier
	 *      The unique non-null and non-empty test case identifier.
	 *
	 * @param arg
	 *      The test case argument.
	 */
	public void testFail( String identifier, Object arg ) {
		assert Strings.isNotEmpty( identifier );

		this.testCaseCount++;
		totalTestCaseCount++;
		if ( verbose || failureVerbose ) {
			System.out.println( "Failure: " + identifier );
			System.out.println( "  Arg:\t" + arg );
			if (arg instanceof Throwable) {
				((Throwable) arg).printStackTrace();
			}
		}
		this.testCaseFailCount++;
		totalTestCaseFailCount++;
		System.out.println( "FAILURE: Case \"" + identifier + "\" failed in "
			+ this.getClass().getName() );
	}

	/**
	 * Prints a brief summary of the test cases for this class. The output
	 * contains the name of the class being tested, the number of test cases
	 * and the number of test failures.
	 *
	 * <p><b>Note:</b> The implementation assumes that the test class is
	 * an inner class of the class being tested.
	 */
	public void testSummary() {
		String fullName = this.getClass().getName();
		int index = fullName.indexOf( "$" );
		if (index < 0) {
			index = fullName.lastIndexOf(".");
		}
		String name = fullName.substring( 0, index );
		System.out.println( name + ", " + this.testCaseCount + " trials, "
			+ this.testCaseFailCount + " failures." );
	}

	/**
	 * Prints a brief summary of the test cases run for the entire
	 * test run.  Any test that is not called TEST MUST invoke this
	 * method.  e.g. ExampleUse
	 **/
	public static void totalTestSummary() {
		// fraction of successfully executed tests.
		double perc = 1.0 * (totalTestCaseCount - totalTestCaseFailCount)
			/ totalTestCaseCount;

		System.out.println(totalTestCaseCount + " trials, "
		   + totalTestCaseFailCount + " failures "
		   + ((int) Math.floor(100 * perc)) + " % success");

		try {
			File results = new File("testResults");
			if (results.exists()) {
				BufferedReader is = new BufferedReader(new FileReader(results));
				totalTestCaseCount += Integer.parseInt(is.readLine());
				totalTestCaseFailCount += Integer.parseInt(is.readLine());
				is.close();
			}
			perc = 1.0 * (totalTestCaseCount - totalTestCaseFailCount)
				/ totalTestCaseCount;
			System.out.println("Total results for series of runs are: " + totalTestCaseCount + " trials, "
							   + totalTestCaseFailCount + " failures "
							   + ((int) Math.floor(100 * perc)) + " % success");
			PrintWriter os = new PrintWriter(new FileOutputStream(results));
			os.println(totalTestCaseCount);
			os.println(totalTestCaseFailCount);
			os.close();
		} catch (Exception ex) {
			System.out.println("Caught an exception while attempting to accumulate test runs results");
			ex.printStackTrace();
		}
	}

	public static void printUsage(PrintStream out) {
		out.println("usage: java com.circus.core.Test [-d] [-u] [-h]");
		out.println("        -d[etails] prints out the details of the current total results for the run");
		out.println("        -u[sage] usage");
		out.println("        -h[elp]  help");
	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length == 0) {
			printUsage(System.out);
			return;
		}
		if (args[0].startsWith("-d")) {
			totalTestSummary();
			return;
		}
		if (args[0].startsWith("-u") || args[0].startsWith("-h")) {
			printUsage(System.out);
			return;
		}
		printUsage(System.out);
	}



	/** It has proven difficult to get "testException" right.
	 * Thus, the creation of this seemingly complicated test, to
	 * get at the nature of the difficulty.  And to make sure we
	 * fully understand what is going on here.
	**/
	/*
	public static class TEST extends Test {
		public static void main(String[] args) {
			TEST t = new TEST();
			t.run();
			t.testSummary();
		}
		public void throwException(Exception e) throws Exception {
			throw e;
		}
		public void getInfo(java.util.List l) throws Exception {
			throw new Exception("Exception");
		}
		public void getInfo2(java.util.ArrayList l) throws Exception {
			throw new Exception("Exception");
		}
		public void getInfo3(java.util.List l) throws Exception {
			throw new Exception("Exception");
		}
		public void getInfo3(java.util.ArrayList l) throws Exception {
			throw new java.io.IOException("Exception");
		}
		public void getInfo4(Object l) throws Exception {
			throw new java.io.IOException("Exception getInfo4.Object");
		}
		public void getInfo4(java.util.AbstractCollection l) throws Exception {
			throw new java.io.IOException("Exception getInfo4.AbstractCollection");
		}
		public void getInfo4(java.util.AbstractList l) throws Exception {
			throw new java.io.IOException("Exception getInfo4.AbstractArray");
		}

		public void run() {
			// this test fails.  Should be commented out in production.
			testException("Test1 of testException",
						  this,

						  "getInfo",
						  new Object[] {new java.util.ArrayList()},
						  Exception.class);
			testException("Test2 of testException",
						  this,
						  "getInfo2",
						  new Object[] {new java.util.ArrayList()},
						  Exception.class);
			// Test 3 shows why we cannot in general use the
			// "assignable to" algorithm to determine the method.
			// (find all methods with the given name, and examine the
			// class[] arguements, and if each element is assignable
			// to another, then call it.
			testException("Test3 of testException",
						  this,
						  "getInfo3",
						  new Object[] {new java.util.ArrayList()},
						  java.io.IOException.class);

			testException("Test 4 of testException",
						  this,
						  "throwException",
						  new Object[] {new Exception(new SecurityException("Hello!"))},
						  new Class[] {Exception.class, SecurityException.class});

			// To test correct operation, this test must FAIL.  We comment it out here.
//  			testException("Test5 of testException",
//  						  this,
//  						  "throwException",
//  						  new Object[] {new Exception(new SecurityException("Hello!"))},
//  						  new Class[] {Exception.class, java.io.IOException.class});


		}
   }
	*/


}
