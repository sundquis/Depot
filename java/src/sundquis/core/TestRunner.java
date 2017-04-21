/*

 * $Author: johnh $
 * Last Check in $Date: 2002/07/28 21:01:53 $
 * Symbolic $Name:  $ of this version (if any used to check out the file)
 * $Locker:  $ (if any)
 * $Revision: 1.6 $
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/TestRunner.java,v $
 * $State: Exp $
 * 
 * 
 * $Log: TestRunner.java,v $
 * Revision 1.6  2002/07/28 21:01:53  johnh
 * Caught an additional exception, and forced it to be a failure
 *
 * Revision 1.5  2002/06/03 13:18:11  johnh
 * Added test to test that asserts are turned on
 *
 * Revision 1.4  2002/05/05 22:17:53  johnh
 * Cleaned up how classes get dynamically created from class files
 *
 * Revision 1.3  2002/05/01 02:57:25  johnh
 * Minor fixes to get TestRunner to successfully test
 *
 * Revision 1.2  2002/04/19 18:03:10  johnh
 * Replaced JClassLoader
 *
 * Revision 1.1  2002/02/24 20:36:46  johnh
 * Initial Version
 *
 * $$
 *
 *
 *
 *
 **/

package com.circus.core;

import java.io.*;
import java.lang.reflect.*;
//import org.apache.bcel.classfile.ClassParser;
//import org.apache.bcel.classfile.JavaClass;

/** Searches all directories (and sub directories) running all the
 *  main methods in every class ending in $TEST.  Prints a summary at
 *  the end.  
 *
 * <p>

 * Things to do: Add a security manager, that only let's this class
 * call "System.exit()".  Otherwise, the tests get aborted, and the
 * summary never gets printed.

 * @author john hoffman
 **/

public class TestRunner extends Test { 

    public TestRunner () {

    }

    public void checkDir(File dir) {
	if (!dir.isDirectory()) {
	    if (dir.getName().endsWith("TEST.class")) {
		runTestsForFile(dir.getAbsolutePath());
	    } else {
		testFail("a non test class file entered ",dir);
	    }
	} else {
	    File[] files = dir.listFiles();
	    for (int i = 0; i < files.length; i++) {
		if (files[i].isDirectory()) {
		    checkDir(files[i]);
		} else if (files[i].getName().endsWith("TEST.class")) {
		    runTestsForFile(files[i].getAbsolutePath());
		}
	    }
	}
    }


    /** A version using BCEL.  We use BCEL to read in the class file,
        and to tell us the packagename for the class.  This allows us
        to avoid having to specify packages in the interface or
        anything.  Just pass in the class filename for a Test, and the
        rest is done automagically.
    **/
    public void runTestsForFile(String filename) {
	Class cl = null;

	try {
            // FIXME
	    //ClassParser cp = new ClassParser(filename);
	    //JavaClass jc = cp.parse();

	    // the fully qualified class name associated with filename.
            // FIXME
	    //String name = jc.getClassName();
	    String name = null;
		
	    cl = Class.forName(name);

	    // This is a valid test, but the code is yucky, need to revisit.
//  	    String clStartName = cl.getProtectionDomain().getCodeSource().getLocation().getFile();
//  	    // If this happens, there's likely a problem with the classpath.
//  	    if (!filename.replace('/','\\').startsWith(clStartName.replace('/','\\'))) {
//  		testFail("Testclass start name not what was expected.  Error in classpath?" + clStartName + "!= " + filename, null);
//  	    }


	    // Invoke the main method with NO arguments.
	    Method m = cl.getMethod("main", new Class[] { new String[] {}.getClass()});
	    m.invoke(null, new Object[]{ new String[] {} } );
	} catch (ClassNotFoundException e) {
	    testFail("Failure in creating Class from file " + filename,null);
	    e.printStackTrace();
	} catch (NoSuchMethodException e) {
	    testFail("Test Class contained in file " + filename + " Does not have a main method!", null);
	} catch (InvocationTargetException e) {
	    testFail("Exception thrown by test : " + filename, e);
	    if (getVerbose()) {
		e.printStackTrace();
	    }
	} catch (IllegalAccessException e) {
	    testFail("Illegal Access Exception thrown by test : " + filename, e);
//	} catch (IOException e) {
//	    testFail("Caught IOException in class : " + filename, e);
	} catch (Exception e) {
	    testFail("Caught Exception in class : " + filename,e);
	    e.printStackTrace();
	}
    }






    public static void main(String[] args) {
	if (args == null || args.length == 0) {
	    // If no arguments, get current working directory.
	    args = new String[] {
		System.getProperties().getProperty("user.dir")
	    };
	}

	TestRunner tr = new TestRunner();

	// run through all the arguments.
	for (int i = 0; i < args.length; i++) {
	    File base = new File(args[i]);
	    tr.checkDir(base);
	}

	// tell us what happened
	Test.totalTestSummary();
    }


    /** A fake class, used to make sure we actually run tests.
     */
    public static class TestRunnerTestClass extends Test {
	public static void main(String[] args) {
	    TEST t = new TEST();
	    t.testCase("TestRunnerTest is Invoked", true);
	}	
    }

    /** We can test ourselves!
     */
    public static class TEST extends Test {
	public static void main(String[] args) {
	    TEST t = new TEST();
	    t.run();
	}
	public void run() {

	    try {
		TestRunner tr = new TestRunner();
		String fileForTestClass = 
		    this.getClass().getResource("TestRunner$TestRunnerTestClass.class").getFile();

		tr.runTestsForFile(fileForTestClass);

//"c:\\CircusSoftware\\circus\\java\\src\\com\\circus\\core\\TestRunner$TestRunnerTestClass.class");

		testCase("runTestsFor test 1", true);
	    } catch (Exception e) {
		testFail("runTestsFor test 1", e);
	    }

	    // Make sure Assertions are getting caught
	    boolean pass = true;
	    try {
		failAssertion();
		pass = false;
	    } catch (AssertionError e) {
	    }
	    testCase("Assertions are getting caught", pass);
	}

	public void failAssertion() {
	    assert false;
	}
    }

}
