/*
 * Copyright (C) 2017 by TS Sundquist
 * 
 * All rights reserved.
 * 
 */

package sundquis.core.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Supplier;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import sundquis.core.Assert;
import sundquis.core.Fatal;
import sundquis.core.LocalDir;
import sundquis.core.Procedure;
import sundquis.core.Test;
import sundquis.core.TestCase;
import sundquis.core.TestContainer;

/**
 *
 */
public class XMLFile implements ContentHandler, ErrorHandler, Locator, LexicalHandler {

	private final File file;
	
	private Locator locator;

	@Test.Decl( "Throws assertion error if file does not exist" )
	public XMLFile( File file ) {
		this.file = Assert.readableFile( file );
		this.locator = null;
	}

	@Test.Skip
	public void parse() {
		try ( FileInputStream fis = new FileInputStream( this.file ) ) {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler( this );
			reader.setErrorHandler( this );
			reader.setProperty( "http://xml.org/sax/properties/lexical-handler", this );
			InputSource src = new InputSource( fis );
			reader.parse( src );
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

	
	
	// CONTENT HANDLER
	
	/**
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	@Override
	@Test.Decl( "Throws assertion error for null locator" )
	@Test.Decl( "Parser sets locator before start document" )
	public void setDocumentLocator( Locator locator ) {
		this.locator = Assert.nonNull( locator );
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	@Override
	@Test.Decl( "Called before end document" )
	public void startDocument() {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	@Override
	@Test.Skip
	public void endDocument() {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	@Override
	@Test.Skip
	public void startPrefixMapping( String prefix, String uri ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	@Override
	@Test.Skip
	public void endPrefixMapping( String prefix ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	@Override
	@Test.Skip
	public void startElement( String uri, String localName, String qName, Attributes atts ) {
		Map<String, String> attributes = new TreeMap<String, String>();
		for ( int i = 0; i < atts.getLength(); i++ ) {
			attributes.put( atts.getLocalName(i), atts.getValue(i) );
		}
		this.startElement( localName,  attributes );
	}
	
	// Convenience to reduce contact with sax
	@Test.Decl( "Called with attribute map" )
	public void startElement( String name, Map<String, String> attributes ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Test.Decl( "Nested elements close in l i f o order" )
	public void endElement( String uri, String localName, String qName ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	@Override
	@Test.Decl( "Characters are read" )
	public void characters( char[] ch, int start, int length ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	@Override
	@Test.Skip
	public void ignorableWhitespace( char[] ch, int start, int length ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	@Override
	@Test.Skip
	public void processingInstruction( String target, String data ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	@Override
	@Test.Skip
	public void skippedEntity( String name ) {
		// NOOP
	}


	
	
	// ERROR HANDLER
	
	/**
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	@Override
	@Test.Skip
	public void warning( SAXParseException exception ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	@Override
	@Test.Skip
	public void error( SAXParseException exception ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	@Override
	@Test.Skip
	public void fatalError( SAXParseException exception ) {
		// NOOP
	}


	
	
	// LOCATOR
	// Reader sets this in parse. We defer to the given locator.
	
	/**
	 * @see org.xml.sax.Locator#getPublicId()
	 */
	@Override
	@Test.Skip
	public String getPublicId() {
		return Assert.nonNull( this.locator ).getPublicId();
	}


	/**
	 * @see org.xml.sax.Locator#getSystemId()
	 */
	@Override
	@Test.Skip
	public String getSystemId() {
		return Assert.nonNull( this.locator ).getSystemId();
	}


	/**
	 * @see org.xml.sax.Locator#getLineNumber()
	 */
	@Override
	@Test.Decl( "Returns correct line number" )
	public int getLineNumber() {
		return Assert.nonNull( this.locator ).getLineNumber();
	}


	/**
	 * @see org.xml.sax.Locator#getColumnNumber()
	 */
	@Override
	@Test.Decl( "Returns correct column number" )
	public int getColumnNumber() {
		return Assert.nonNull( this.locator ).getColumnNumber();
	}

	
	
	
	// LexicalHandler

	/**
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	@Test.Skip
	public void startDTD( String name, String publicId, String systemId ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	@Override
	@Test.Skip
	public void endDTD() {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	@Override
	@Test.Skip
	public void startEntity( String name ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	@Override
	@Test.Skip
	public void endEntity( String name ) {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	@Override
	@Test.Decl( "Called before end cdata" )
	public void startCDATA() {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	@Override
	@Test.Skip
	public void endCDATA() {
		// NOOP
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	@Override
	@Test.Decl( "Reads comments" )
	public void comment( char[] ch, int start, int length ) {
		// NOOP
	}

	
	
	
	public static class Container implements TestContainer {

		@Override
		public Class<?> subjectClass() {
			return XMLFile.class;
		}
		
		private File file;
		
		public Procedure beforeAll() {
			return new Procedure() {
				public void call() {
					// Write a simple sample xml file to parse.
					// FRAGILE: Cases below tied to the structure.
					file = new LocalDir().sub( "tmp" ).getTmpFile( "XMLFile" );
					try ( PrintWriter out = new PrintWriter( new FileWriter( file ) ) ) {
						out.println ( XML.DECLARATION );
						out.println( "<root>" );
						out.println( "Some character data" );
						out.println( "<!-- This is a comment -->" );
						out.println( "<![CDATA[Some unparsed data]]>" );
						out.println( "<nested>" );
						out.println( "</nested>" );
						out.println( "<nested>" );
						out.println( "<inner key1=\"value1\" key2=\"value2\">" );
						out.println( "</inner>" );
						out.println( "</nested>" );
						out.println( "</root>" );
					} catch ( IOException e ) {
						Fatal.error( "Failed to write tmp file", e );
					}
				}
			};
		}
		
		static abstract class XMLFileAdapter<T> extends XMLFile implements Supplier<T> {
			XMLFileAdapter( File file ) { super( file ); }
		}
		
		
		@Test.Impl( 
			src = "public XMLFile(File)", 
			desc = "Throws assertion error if file does not exist" )
		public void XMLFile_ThrowsAssertionErrorIfFileDoesNotExist( TestCase tc ) {
			tc.expectError( AssertionError.class );
			File file = new File( "Bogus" );
			tc.assertFalse( file.exists() );
			new XMLFile( file );
		}

		@Test.Impl( 
			src = "public int XMLFile.getColumnNumber()", 
			desc = "Returns correct column number" )
		public void getColumnNumber_ReturnsCorrectColumnNumber( TestCase tc ) {
			XMLFileAdapter<Integer> x = new XMLFileAdapter<Integer>( this.file ) {
				int column = -1;
				@Override public void startDocument() {
					column = this.getColumnNumber();
				}
				@Override public Integer get() { return column; }
			};
			x.parse();
			tc.assertEqual( 1,  x.get() );
		}

		@Test.Impl( 
			src = "public int XMLFile.getLineNumber()", 
			desc = "Returns correct line number" )
		public void getLineNumber_ReturnsCorrectLineNumber( TestCase tc ) {
			XMLFileAdapter<Integer> x = new XMLFileAdapter<Integer>( this.file ) {
				int line = -1;
				@Override public void startDocument() {
					line = this.getLineNumber();
				}
				@Override public Integer get() { return line; }
			};
			x.parse();
			tc.assertEqual( 1,  x.get() );
		}

		@Test.Impl( 
			src = "public void XMLFile.characters(char[], int, int)", 
			desc = "Characters are read" )
		public void characters_CharactersAreRead( TestCase tc ) {
			String testResult = "Some character data";
			XMLFileAdapter<String> x = new XMLFileAdapter<String>( this.file ) {
				String data = null;
				@Override public void characters( char[] buf, int start, int length ) {
					if ( data == null ) { data = new String( buf, start, length ); }
				}
				@Override public String get() { return data.trim(); }
			};
			x.parse();
			tc.assertEqual( testResult,  x.get() );
		}

		@Test.Impl( 
			src = "public void XMLFile.comment(char[], int, int)", 
			desc = "Reads comments" )
		public void comment_ReadsComments( TestCase tc ) {
			String testResult = "This is a comment";
			XMLFileAdapter<String> x = new XMLFileAdapter<String>( this.file ) {
				String data = null;
				@Override public void comment( char[] buf, int start, int length ) {
					if ( data == null ) { data = new String( buf, start, length ); }
				}
				@Override public String get() { return data.trim(); }
			};
			x.parse();
			tc.assertEqual( testResult,  x.get() );
		}

		@Test.Impl( 
			src = "public void XMLFile.endElement(String, String, String)", 
			desc = "Nested elements close in l i f o order" )
		public void endElement_NestedElementsCloseInLIFOOrder( TestCase tc ) {
			XMLFileAdapter<Boolean> x = new XMLFileAdapter<Boolean>( this.file ) {
				boolean testResult = true;
				Stack<String> stack = new Stack<String>();
				@Override public void startElement( String uri, String localName, String qName, Attributes atts ) {
					stack.push( localName );
				}
				@Override public void endElement( String uri, String localName, String qName ) {
					testResult &= stack.pop().equals( localName );
				}
				@Override public Boolean get() { return testResult && stack.isEmpty(); }
			};
			x.parse();
			tc.assertTrue( x.get() );
		}

		@Test.Impl( 
			src = "public void XMLFile.setDocumentLocator(Locator)", 
			desc = "Parser sets locator before start document" )
		public void setDocumentLocator_ParserSetsLocatorBeforeStartDocument( TestCase tc ) {
			XMLFileAdapter<Boolean> x = new XMLFileAdapter<Boolean>( this.file ) {
				boolean testResult = false;
				boolean set = false;
				@Override public void setDocumentLocator( Locator locator ) {
					set = true;
				}
				@Override public void startDocument() {
					testResult = set;
				}
				@Override public Boolean get() { return testResult; }
			};
			x.parse();
			tc.assertTrue( x.get() );
		}

		@Test.Impl( 
			src = "public void XMLFile.setDocumentLocator(Locator)", 
			desc = "Throws assertion error for null locator" )
		public void setDocumentLocator_ThrowsAssertionErrorForNullLocator( TestCase tc ) {
			tc.expectError( AssertionError.class );
			new XMLFile( this.file ).setDocumentLocator( null );
		}

		@Test.Impl( 
			src = "public void XMLFile.startCDATA()", 
			desc = "Called before end cdata" )
		public void startCDATA_CalledBeforeEndCdata( TestCase tc ) {
			XMLFileAdapter<Boolean> x = new XMLFileAdapter<Boolean>( this.file ) {
				boolean testResult = false;
				boolean end = false;
				@Override public void startCDATA() {
					testResult = !end;
				}
				@Override public void endCDATA() {
					end = true;
				}
				@Override public Boolean get() { return testResult; }
			};
			x.parse();
			tc.assertTrue( x.get() );
		}

		@Test.Impl( 
			src = "public void XMLFile.startDocument()", 
			desc = "Called before end document" )
		public void startDocument_CalledBeforeEndDocument( TestCase tc ) {
			XMLFileAdapter<Boolean> x = new XMLFileAdapter<Boolean>( this.file ) {
				boolean testResult = false;
				boolean end = false;
				@Override public void startDocument() {
					testResult = !end;
				}
				@Override public void endDocument() {
					end = true;
				}
				@Override public Boolean get() { return testResult; }
			};
			x.parse();
			tc.assertTrue( x.get() );
		}

		@Test.Impl( src = "public void XMLFile.startElement(String, Map)", desc = "Called with attribute map" )
		public void startElement_CalledWithAttributeMap( TestCase tc ) {
			XMLFileAdapter<Map<String, String>> x = new XMLFileAdapter<Map<String, String>>( this.file ) {
				Map<String, String> testResults;
				@Override public void startElement( String name, Map<String, String> atts ) {
					if ( "inner".equals( name ) ) {
						testResults = atts;
					}
				}
				@Override public Map<String, String> get() { return testResults; }
			};
			x.parse();
			Map<String, String> map = x.get();
			tc.assertEqual( "value1",  map.get( "key1" ) );
			tc.assertEqual( "value2",  map.get( "key2" ) );
		}

		
		
		
	}
	
	public static void main( String[] args ) {
		System.out.println();
		
		new Test( Container.class ).eval();
		Test.printResults();
		
		System.out.println( "\nDone!" );
	}

}
