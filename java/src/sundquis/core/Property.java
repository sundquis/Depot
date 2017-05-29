/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import sundquis.core.Test.Decl;
import sundquis.core.xml.XMLFile;

/**
 * Usage:
 * 		public T myT = Property.get( name, default, parser );
 * 
 */
public class Property {

	// FOR DEVELOPMENT ONLY
	private static final String DEFAULT_SYSTEM_DIR = "/home/sundquis/book/Dropbox/java/projects/DEV";
	
	private static final String SYSTEM_DIR = System.getProperty( "system.dir", DEFAULT_SYSTEM_DIR );
	
	private static final String SYSTEM_NAME = "system.xml";

	private static final Property INSTANCE = new Property();
	
	
	/** Retrieve a configurable property from the system property file */
	@Decl( "Throws assertion error for null name" )
	@Decl( "Throws assertion error for empty name" )
	@Decl( "throws assertion error for null parser" )
	@Decl( "Retrieves properties for top level classes" )
	@Decl( "Retrieves properties for nested classes" )
	@Decl( "Retrieves properties for double nested classes" )
	@Decl( "Throws assertion error for anonymous classes" )
	@Decl( "Throws assertion error for local classs" )
	@Decl( "Prints declaration for missing property" )
	public static <T> T get( String name, T defaultValue, Function<String, T> parser ) {
		Assert.nonEmpty( name );
		Assert.nonNull( parser );

		// FRAGILE:
		StackTraceElement[] stackTrace = (new Exception()).getStackTrace();
		// Stack:
		//		Property.get
		//		<class declaring a property>
		Assert.isTrue( stackTrace.length > 1 );
		String className = stackTrace[1].getClassName().replaceAll( "\\D*\\d+[_a-zA-Z]*$", "" );
		Assert.nonEmpty( className );
		
		String key = className + "." + name;
		String stringValue = Property.INSTANCE.getStringValue( key );
		
		if ( stringValue == null ) {
			System.err.println( "WARNING: Property not found:" );
			System.err.println( "<class fullname=\"" + className + "\">" );
			System.err.println( "<property name=\"" + name + "\" value=\"" + defaultValue + "\" />" );
			System.err.println( "</class>" );
		}
		
		return stringValue == null ? defaultValue : parser.apply( stringValue );
	}

	// Convenience parsers. Add as needed.
	@Test.Skip
	public static final Function<String, Integer> INTEGER = (s) -> Integer.parseInt(s);

	@Test.Skip
	public static final Function<String, Long> LONG = (s) -> Long.parseLong(s);
	
	@Test.Skip
	public static final Function<String, Boolean> BOOLEAN = (s) -> Boolean.parseBoolean(s);
	
	@Test.Skip
	public static final Function<String, String> STRING = (s) -> s;

	@Decl( "Collection of common cases" )
	@Decl( "Array of length one allowed" )
	@Decl( "Empty array allowed" )
	@Decl( "White space after comman ignored" )
	public static final Function<String, String[]> CSV = (s) -> s.split( ",\\s*" );
	

	
	
	
	private Map<String, String> values = null;

	private Property() {}

	private String getStringValue( String key ) {
		if ( this.values == null ) {
			synchronized ( Property.class ) {
				if ( this.values == null ) {
					this.values = new TreeMap<String, String>();
					this.load();
				}
			}
		}
		
		return this.values.get( key );
	}

	private void load() {
		File file = new File( Property.SYSTEM_DIR, Property.SYSTEM_NAME );
		Assert.readableFile( file );
		XMLFile xfile = new SystemXMLFile( file );

		xfile.parse();
	}
	
	// Tied to the structure defined in system.dtd
	private class SystemXMLFile extends XMLFile {
		
		private String currentClassName = null;

		public SystemXMLFile( File file ) {
			super( file );
		}
		
		@Test.Skip
		@Override
		public void startElement( String name, Map<String, String> attributes ) {
			Assert.nonEmpty( name );
			Assert.nonNull( attributes );

			if ( name.equals( "class" ) ) {
				this.currentClassName = Assert.nonEmpty( attributes.get( "fullname" ) );
			}
			if ( name.equals( "property" ) ) {
				String propName = Assert.nonEmpty( attributes.get( "name" ) );
				String key = this.currentClassName + "." + propName;
				String value = Assert.nonEmpty( attributes.get( "value" ) );
				Property.this.values.put( key,  value );
			}
		}
		
		
	}
	

	
	
	
	public static class Container implements TestContainer {
		@Override
		public Class<?> subjectClass() {
			return Property.class;
		}
		
		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Prints declaration for missing property" )
		public void get_PrintsDeclarationForMissingProperty( TestCase tc ) {
			// SHOULD SEE:
			// WARNING: Property not found:
			// <class fullname="sundquis.core.Property$Container">
			// <property name="FOO" value="Foo" />
			// </class>
			//
			// TOGGLE
			/* */ tc.addMessage( "Manually verified" ).pass(); /*
			Property.get( "FOO",  "Foo",  Property.STRING );
			tc.pass();
			// */
		}

		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Retrieves properties for nested classes" )
		public void get_RetrievesPropertiesForNestedClasses( TestCase tc ) {
			// Should use a mock-up. Instead put a fake entry in system.xml
			tc.assertTrue( Property.get( "nested.test",  false,  Property.BOOLEAN ) );
		}

		public static class Inner {
			public static boolean getTestProp() {
				return Property.get( "double.nested.test",  false,  Property.BOOLEAN );
			}
		}
		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Retrieves properties for double nested classes" )
		public void get_RetrievesPropertiesForDoubleNestedClasses( TestCase tc ) {
			tc.assertTrue( Container.Inner.getTestProp() );
		}

		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Retrieves properties for top level classes" )
		public void get_RetrievesPropertiesForTopLevelClasses( TestCase tc ) {
			tc.pass();
		}

		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Throws assertion error for anonymous classes" )
		public void get_ThrowsAssertionErrorForAnonymousClasses( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Object anon = new Object() {
				@Override public String toString() {
					return Property.get( "foo",  "foo",  Property.STRING );
				}
			};
			tc.addMessage( "Shouldn't get here" ).assertEqual( "bar",  anon.toString() ); 
		}

		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Throws assertion error for empty name" )
		public void get_ThrowsAssertionErrorForEmptyName( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Property.get( "",  "foo",  Property.STRING );
		}

		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Throws assertion error for local classs" )
		public void get_ThrowsAssertionErrorForLocalClasss( TestCase tc ) {
			tc.expectError( AssertionError.class );
			class Local {
				boolean getProp() {
					return Property.get( "foo",  false,  Property.BOOLEAN );
				}
			}
			Local local = new Local();
			tc.assertEqual( "bar",  local.getProp() );
			tc.addMessage( "Shouldn't get here" );
		}

		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "Throws assertion error for null name" )
		public void get_ThrowsAssertionErrorForNullName( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Property.get( null,  "foo",  Property.STRING );
		}
		
		@Test.Impl( src = "public Object Property.get(String, Object, Function)", desc = "throws assertion error for null parser" )
		public void get_ThrowsAssertionErrorForNullParser( TestCase tc ) {
			tc.expectError( AssertionError.class );
			Property.get( "foo",  "foo",  null );
		}
		
		@Test.Impl( src = "public Function Property.CSV", desc = "Array of length one allowed" )
		public void CSV_ArrayOfLengthOneAllowed( TestCase tc ) {
			String arg = "A single string.";
			String[] array = { "A single string." };
			tc.assertEqual( array, Property.CSV.apply( arg ) );
		}

		private static String[][] ARRAYS = {
			{ "a", "b", "c" }, 
			{ "This time I mean it", "You know what that means", "Somewhat", "longer", "array", "here" },
			{ "Rememeber", "to", "add", "cases", "here", "for", "newly", "discovered", "failure", "modes." },
			{ "" },
			{ "Singleton" }
		};
		private static String[] ARGS = {
			"a, b, c",
			"This time I mean it, You know what that means, Somewhat, longer, array, here",
			"Rememeber, to, add, cases, here, for, newly, discovered, failure, modes.",
			"",
			"Singleton"
		};
		@Test.Impl( src = "public Function Property.CSV", desc = "Collection of common cases", weight = 5 )
		public void CSV_CollectionOfCommonCases( TestCase tc ) {
			for ( int i = 0; i < ARRAYS.length; i++ ) {
				tc.assertEqual( ARRAYS[i],  Property.CSV.apply( ARGS[i] ) );
			}
		}

		@Test.Impl( src = "public Function Property.CSV", desc = "Empty array allowed" )
		public void CSV_EmptyArrayAllowed( TestCase tc ) {
			String arg = "";
			String[] array = { "" };
			tc.assertEqual( array, Property.CSV.apply( arg ) );
		}
		
		@Test.Impl( src = "public Function Property.CSV", desc = "White space after comman ignored" )
		public void CSV_WhiteSpaceAfterCommanIgnored( TestCase tc ) {
			String arg = "Spaces ignored,        Tabs ignored,\t\tNewlines ignored,\n\n Done";
			String[] array = { "Spaces ignored", "Tabs ignored", "Newlines ignored", "Done" };
			tc.assertEqual( array, Property.CSV.apply( arg ) );
		}

		
		
	}

	
	public static void main( String[] args ) {
		System.out.println();
		
		//Test.noWarnings();
		new Test( Container.class ).eval();
		Test.printResults();

		System.out.println("\nDone!");
	}

}
