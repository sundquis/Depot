/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.test;

import java.util.Map;
import java.util.TreeMap;

import sundquis.core.Assert;
import sundquis.core.Property;
import sundquis.core.Test;

/**
 * @author sundquis
 *
 */
@Test.Skip( "How to test abstract?" )
public abstract class Result {

	
	protected static boolean SHOW_GLOBAL_RESULTS =
		Property.get( "show.global.results",  true,  Property.BOOLEAN );
	
	protected static boolean SHOW_CLASS_RESULTS =
		Property.get( "show.class.results",  true,  Property.BOOLEAN );
	
	protected static boolean SHOW_MEMBER_RESULTS =
		Property.get( "show.member.results",  true,  Property.BOOLEAN );
		
	protected static boolean SHOW_CASE_RESULTS =
		Property.get( "show.case.results",  true,  Property.BOOLEAN );
	
	protected static boolean SHOW_FAIL_CASES =
		Property.get( "show.fail.cases",  true,  Property.BOOLEAN );
		
	protected static boolean SHOW_PASS_CASES =
			Property.get( "show.pass.cases",  false,  Property.BOOLEAN );
			
	protected static boolean SHOW_UNEXECUTED_CASES =
			Property.get( "show.unexecuted.cases",  true,  Property.BOOLEAN );
	
	protected static boolean WARN_UNTESTED_CLASS =
			Property.get( "warn.untested.class",  false,  Property.BOOLEAN );

	protected static boolean WARN_UNTESTED_CONSTRUCTOR =
		Property.get( "warn.untested.constructor",  false,  Property.BOOLEAN );

	protected static boolean WARN_UNTESTED_METHOD =
		Property.get( "warn.untested.method",  true,  Property.BOOLEAN );

	protected static boolean WARN_UNTESTED_FIELD =
		Property.get( "warn.untested.field",  false,  Property.BOOLEAN );
		
	protected static boolean WARN_UNTESTED_NONPUBLIC_MEMBERS =
		Property.get( "warn.untested.nonpublic.members",  false,  Property.BOOLEAN );

	protected static boolean WARN_UNTESTED_PUBLIC_MEMBERS =
		Property.get( "warn.untested.public.members",  true,  Property.BOOLEAN );


	
	
	private final String label;
	
	private final Map<String, Result> children;
	
	private final Object statMutex;
	
	private volatile long time;
	
	private volatile int passCount;
	
	private volatile int failCount;
	
	private volatile int totalCount;
	
	
	protected Result( String label ) {
		this.label = Assert.nonEmpty( label );
		this.children = new TreeMap<String, Result>();
		this.statMutex = new Object(){};
		
		this.time = -1L;
		this.passCount = -1;
		this.failCount = -1;
		this.totalCount = -1;
	}

	public String getLabel() {
		return Assert.nonEmpty( this.label );
	}
	
	public static void summary() {
		SHOW_GLOBAL_RESULTS = true;
		SHOW_CLASS_RESULTS = true;
		SHOW_MEMBER_RESULTS = false;
		SHOW_CASE_RESULTS = true;
		SHOW_FAIL_CASES = true;
		SHOW_PASS_CASES = false;
		SHOW_UNEXECUTED_CASES = false;
	}

	
	public int getPassCount() {
		if ( this.time < 0L ) {
			this.setStats();
		}
		return this.passCount;
	}
	
	public int getFailCount() {
		if ( this.time < 0L ) {
			this.setStats();
		}
		return this.failCount;
	}
	
	public int getTotalCount() {
		if ( this.time < 0L ) {
			this.setStats();
		}
		return this.totalCount;
	}
	
	public long getTime() {
		if ( this.time < 0L ) {
			this.setStats();
		}
		return this.time;
	}
	
	private void setStats() {
		synchronized ( this.statMutex ) {
			if ( this.time < 0L ) {
				int p = 0;
				int f = 0;
				int t = 0;
				long time = 0L;
				for ( Result r : this.children.values() ) {
					p += r.getPassCount();
					f += r.getFailCount();
					t += r.getTotalCount();
					time += r.getTime();
				}
				this.passCount = p;
				this.failCount = f;
				this.totalCount = t;
				this.time = time;
			}
		}
	}
	
	public Result addChild( Result child ) {
		Result result = Assert.nonNull( child );
		
		synchronized ( this.children ) {
			Result current = this.children.get( result.getLabel() );
			if ( current == null ) {
				this.children.put( result.getLabel(),  result );
			} else {
				result = current;
			}
		}
		
		return Assert.nonNull( result );
	}
	
	public void print( String prefix ) {
		if ( this.showResults() ) {
			System.err.println( prefix + this );
		}
		
		for ( Result r : this.children.values() ) {
			r.print( "\t" + prefix );
		}
	}
	
	@Override
	public String toString() {
		int tot = this.getTotalCount();
		int pass = this.getPassCount();
		int fail = this.getFailCount();
		int unx = tot - pass - fail;
		double success = (double) 100 * pass / (tot == 0 ? 1 : tot);
		double seconds = (double) this.time / 1000.0;
		return String.format( "[%s] Success = %.1f%%, Time = %.1fs, Count = %d (P = %d, F = %d, U = %d)", this.label, success, seconds, tot, pass, fail, unx );
	}
	
	
	public abstract boolean showResults();

	
	
	
	public static void main( String[] args ) {
		System.out.println( "\n\n" );
		
		
		System.out.println( "Done!" );
	}

}
