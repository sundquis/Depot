/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import sundquis.core.Assert;
import sundquis.core.Test;

/**
 * @author sundquis
 *
 */
@Test.Skip
public class TestResult extends Result {
	
	// TODO Store results for historical comparison
	
	public TestResult() {
		super( "TESTS: " + new SimpleDateFormat( "YYYY-MM-dd HH:mm:ss" ).format( new Date() ) );
	}
	
	public ClassResult addClass( Class<?> clazz ) {
		Assert.nonNull( clazz );
		ClassResult result = new ClassResult( clazz );
		return (ClassResult) this.addChild( result );
	}
	
	/**
	 * @see sundquis.core.test.Result#showResults()
	 */
	@Override
	public boolean showResults() {
		return Result.SHOW_GLOBAL_RESULTS;
	}
	
}
