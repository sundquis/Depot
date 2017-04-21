/*
 * Copyright(c) 2001-3 by Circus Software.
 * All rights reserved.
 *
 * This source code, and its documentation, are the confidential intellectual
 * property of Circus Software and may not be disclosed, reproduced,
 * distributed, or otherwise used for any purpose without the expressed
 * written permission of Circus Software.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/Configuration.java,v $
 * $Id: Configuration.java,v 1.12 2003/06/27 12:45:39 etl Exp $
 * $Log: Configuration.java,v $
 * Revision 1.12  2003/06/27 12:45:39  etl
 * Made serializable.
 *
 * Revision 1.11  2003/06/25 19:51:05  etl
 * Further extended property mechanism. Now also looks for unix style system properties.
 *
 * Revision 1.10  2003/06/24 19:26:55  etl
 * Add "global" property capability.
 *
 * Revision 1.9  2003/06/12 12:39:07  etl
 * Added general name/value mappings.
 *
 * Revision 1.8  2003/06/06 15:54:11  etl
 * Added static getProperty That chacks configuration and system props.
 *
 * Revision 1.7  2003/04/17 21:27:48  etl
 * Remove extraneous empty text lines.
 *
 * Revision 1.6  2003/03/24 01:48:06  johnh
 * Fixed a bug, comments got wrapped twice in unit tests
 *
 * Revision 1.5  2003/03/24 00:45:21  johnh
 * Added some comments
 *
 * Revision 1.4  2003/03/21 14:21:34  etl
 * Fixed comment and text-node printing.
 *
 * Revision 1.3  2002/11/12 00:10:59  toms
 * Debugging deployment.
 *
 * Revision 1.2  2002/10/01 01:11:28  toms
 * Added text content to elements
 *
 * Revision 1.1  2002/09/19 23:26:49  toms
 * Support for reading, writing, and processing xml configuration elements.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * A simple utility for interpolating between xml "configuration" files
 * and a recursive property structure.
 *
 * Configuration instances correspond to xml elements.

 * The element's tag name is represented by the Configuration
 * instance's name.

 * The element's attributes are represented by named properties in the
 * Configuration instance.

 * Nested elements are represented as a list of children Configuration
 * instances.

 * All other xml entities (e.g., text, processing instructions, CDATA)
 * are ignored.

 * Contains supprt for reading xml from a file and writing a
 * Configuration instance as xml to a PrintWriter.  

 * See Configurable for further usage information.

 * @see Configurable
 
 */
public class Configuration implements java.io.Serializable {
	
	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	public static class TEST extends Test {

		public static void main( String[] args ) {
			TEST t = new TEST();
			t.run();
			t.testSummary();
		}

		public void run() {
			try {
				script();
			} catch ( Exception ex ) {
				this.testFail( "Unexpected exception", ex );
				ex.printStackTrace();
			}
		}

		public void script() throws Exception {
			Configuration item = new Configuration( "Test" );
			item.addAttribute( "name1", "value1" ).addAttribute( "name2", "value2" );
			item.setText( "This is a text sample\r\nwith two lines." );
			Configuration child = item.makeChild( "one" );
			child.addAttribute( "name1", "value1" ).addAttribute( "name2", "value2" );
			child = item.makeChild( "two" );
			child.addAttribute( "name1", "value1" ).addAttribute( "name2", "value2" );
			child = child.makeChild( "inner" );
			child.addAttribute( "name", "value" );
			child.makeChild( "A" ).addAttribute( "SEQ", "1" );
			child.makeChild( "A" ).addAttribute( "SEQ", "2" );
			child.makeChild( "A" ).addAttribute( "SEQ", "3" );
			child.makeChild( "A" ).addAttribute( "SEQ", "4" );
			child.makeChild( "A" ).addAttribute( "SEQ", "5" );
			child.makeChild( "A" ).addAttribute( "SEQ", "6" );
			child.makeChild( "A" ).addAttribute( "SEQ", "7" );
			child.makeChild( "B" ).addAttribute( "SEQ", "8" );
			child.makeChild( "B" ).addAttribute( "SEQ", "9" );
			child.makeChild( "B" ).addAttribute( "SEQ", "10" );
			
			Configuration other = new Configuration( "one" );
			other.addAttribute( "name1", "value1" ).addAttribute( "name2", "value2" );
			
			this.testCase( "Can buid Configuration instance", true );
			this.testCase( "Name correct", item.getName(), "Test" );
			this.testCase( "Attribute count correct", item.getAttributes().size() == 2 );
			this.testCase( "Attribute name1 correct", item.getAttribute( "name1" ), "value1" );
			this.testCase( "Attribute name2 correct", item.getAttribute( "name2" ), "value2" );
			this.testCase( "Named child correct, multiple", child.getChild( "A" ), null );
			this.testCase( "Named child correct, none", child.getChild( "FOO" ), null );
			this.testCase( "Named child correct, one", item.getChild( "one" ), other );
			this.testCase( "Child count correct", item.getChildren().size() == 2 );
			this.testCase( "Child name correct", ((Configuration) item.getChildren().get(0)).getName(), "one" );
			this.testCase( "Constrained child count correct", child.getChildren( "B" ).size() == 3 );
			child.addChild( new Configuration( "B" ) );
			this.testCase( "Add child works", child.getChildren( "B" ).size() == 4 );
			child.setChild( new Configuration( "B" ) );
			this.testCase( "set child works", child.getChildren( "B" ).size() == 1 );
			this.testCase( "Equals works", item.getChildren().get(0), other );
			
			item.addComment( "Top level comment" );
			child.addComment( "Inner comment" );
			StringWriter sw = new StringWriter();
			PrintWriter out = new PrintWriter( sw );
			
			Configurable x = new Configurable() {
				List list = new LinkedList();
				public void process( Configuration item ) throws ConfigurationException {
					this.list.add( item );
				}
				public boolean equals( Object obj ) {
					return this.list.equals( ((Configuration) obj).getChildren() );
				}
				public String toString() {
					Iterator c = this.list.iterator();
					while ( c.hasNext() ) {
						((Configuration) c.next()).print();
					}
					return "";
				}
			};
			
			item.writeClassConfiguration( x );
			this.testCase( "write class config works", true );
			
			Configuration.loadClassConfiguration( x );
			this.testCase( "load class config works", x, item );
			
			this.testCase( "get class config works", Configuration.getClassConfiguration(x), item );
			
			item.write( out );
			this.testCase( "write works", true );
			
			other = new Configuration( new StringReader( sw.toString() ) );
			this.testCase( "read works", other, item );
		}

	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { Configuration.TEST.main( args ); }

	/* * * * * * * * * * End of TEST * * * * * * * * * * * * * * */

    static final long serialVersionUID = -2463324337099920624L;
	
	
	/**
	 * If the system configuration file,
	 *		<application.home>/home/<configuration.home>/system.xml
	 * contains a top-level element with tagname equal to
	 * <TT>Configuration.getTagName(obj)</TT>, each of its nested elements
	 * is passed to the configurable object via its process method.
	 *
	 * The system configuration file is maintained through editing.
	 */
	public static void loadSystemConfiguration( Configurable obj ) throws ConfigurationException {
		assert obj != null;
		
		Configuration systemConfiguration = Configuration.getSystemConfiguration( obj );
		Iterator iter = systemConfiguration.getChildren().iterator();
		while ( iter.hasNext() ) {
			obj.process( (Configuration) iter.next() );
		}
	}
	
	
	/**
	 * Returns the value of the named property.
	 *
	 * Property values may be obtained from System.getProperty or from
	 * special configuration elements of the form
	 *		<property name="nnnn" value="vvvv"/>
	 *
	 * These properties are local to a class if found as nested elements
	 * in a configurable class element, or global if found as top-lavel
	 * elements in the system.xml file.
	 *
	 * The value returned is the first non-empty value found by the 
	 * following procedure:
	 *	1.	If the Configurable object is not null and has class configuration
	 *		with a matching property element its value is used.
	 *	2.	If the Configurable object is not null and has system configuration
	 *		with a matching property element its value is used.
	 *	3.	If the system.xml has a matching top-level property element
	 *		its value is used.
	 *	4.	If System.getProperties() has a matching property its value
	 *		is used.
	 *	5.	If applicable, the name is converted to an alternate form by
	 *		replacing all "." with "_" and taking uppercase. If there is
	 *		a corresponding system property its value is used.
	 *	6.	Otherwise, the default is used.
	 *
	 * @param obj
	 *		Possibly null Configurable instance. If not null, its class
	 *		determines class configuration and local system.xml configuration.
	 *		If null, only the global properties are checked.
	 *
	 * @param name
	 *		Non-empty string property name.
	 *
	 * @param dflt
	 *		Default property value.
	 */
	public static String getProperty( Configurable obj, String name, String dflt ) {
		assert Strings.isNotEmpty( name );
		
		String value = null;
		Configuration config = null;
		
		// 1. Look in class configuration for obj
		if ( obj != null && Strings.isEmpty( value ) ) {
			config = Configuration.getClassConfiguration( obj );
			Iterator props = config.getChildren( "property" ).iterator();
			while ( Strings.isEmpty( value ) && props.hasNext() ) {
				config = (Configuration) props.next();
				if ( name.equals( config.getAttribute( "name" ) ) ) {
					value = config.getAttribute( "value" );
				}
			}
		}
		
		// 2. Look in (local) system.xml configuration for obj
		if ( obj != null && Strings.isEmpty( value ) ) {
			config = Configuration.getSystemConfiguration( obj );
			Iterator props = config.getChildren( "property" ).iterator();
			while ( Strings.isEmpty( value ) && props.hasNext() ) {
				config = (Configuration) props.next();
				if ( name.equals( config.getAttribute( "name" ) ) ) {
					value = config.getAttribute( "value" );
				}
			}
		}
		
		// 3. Look in global system.xml configuration
		if ( Strings.isEmpty( value ) ) {
			config = Configuration.getSystemConfiguration();
			Iterator props = config.getChildren( "property" ).iterator();
			while ( Strings.isEmpty( value ) && props.hasNext() ) {
				config = (Configuration) props.next();
				if ( name.equals( config.getAttribute( "name" ) ) ) {
					value = config.getAttribute( "value" );
				}
			}
		}
		
		// 4. Check System
		if ( Strings.isEmpty( value ) ) {
			value = System.getProperty( name );
		}
		
		// 5. Check System for alternate name
		if ( Strings.isEmpty( value ) ) {
			value = System.getProperty( name.toUpperCase().replaceAll( "\\.", "_" ) );
		}

		// 6. Last ditch
		if ( Strings.isEmpty( value ) ) {
			value = dflt;
		}
		
		return value;
	}
	
	public static Object getObject( Configurable obj, String name, Class targetType )
			throws ConfigurationException {
		assert Strings.isNotEmpty( name );
		
		Object result = null;
		try {
			String classname = Configuration.getProperty( obj, name, null );
			if ( Strings.isNotEmpty( classname ) ) {
				result = Class.forName( classname ).newInstance();
			}
		} catch ( Exception ex ) {
			throw new ConfigurationException( ex );
		}
		
		if ( result != null && !targetType.isInstance( result ) ) {
			throw new ConfigurationException( "Result has incorrect type: " + result.getClass() );
		}
		
		return result;
	}
	
	/**
	 * If the class configuration file,
	 *		<application.home>/home/<configuration.home>/<getTagName(obj)>.xml
	 * exists, each of its nested elements
	 * is passed to the configurable object via its process method.
	 *
	 * Class configuration files should not be edited. They are generated by
	 * various build processes.
	 */
	public static void loadClassConfiguration( Configurable obj ) throws ConfigurationException {
		assert obj != null;
		
		String tag = Configuration.getTagName( obj );
		File xml = new LocalFile( Application.getInstance().getConfigDir(),
			tag, LocalFile.Type.XML ).getFile();

		if ( xml.exists() ) {
			Iterator iter = (new Configuration( xml )).getChildren().iterator();
			while ( iter.hasNext() ) {
				obj.process( (Configuration) iter.next() );
			}
		}	
	}
	
	public static Configuration getClassConfiguration( Configurable obj ) {
		assert obj != null;
		
		String tag = Configuration.getTagName( obj );
		File xml = new LocalFile( Application.getInstance().getConfigDir(),
			tag, LocalFile.Type.XML ).getFile();

		Configuration classConfig = null;
		if ( xml.exists() ) {
			classConfig = new Configuration( xml );
		} else {
			classConfig = new Configuration( "classConfiguration" );
		}
		
		assert classConfig != null;
		return classConfig;
	}

	/**
	 * The tag name used for items in the system configuration file
	 * and the filename for configurable class items.
	 */
	public static String getTagName( Configurable obj ) {
		assert obj != null;
		
		return obj.getClass().getName().replaceAll( "\\$", "." );
	}
	
	/**
	 * Cache for system configurations.
	 *
	 *	KEY:		(File)			Configuration directory
	 *	VALUE:		(Configuration)	Corresponding system configuration
	 */
	private static Map configDirToConfiguration = new HashMap();
	
	private static synchronized Configuration getSystemConfiguration() {
		return Configuration.getSystemConfiguration(
			Application.getInstance().getConfigDir() );
	}

	private static Configuration getSystemConfiguration( Configurable obj ) {
		assert obj != null;
		
		String tag = Configuration.getTagName( obj );
		Configuration conf = Configuration.getSystemConfiguration().getChild( tag );
		if ( conf == null ) {
			conf = new Configuration( tag );
		}
		
		assert conf != null;
		return conf;
	}

	private static synchronized Configuration getSystemConfiguration( File configDir ) {
		Configuration config = (Configuration)
			Configuration.configDirToConfiguration.get( configDir );
		
		if ( config == null ) {
			config = Configuration.readSystemConfiguration( configDir );
			Configuration.configDirToConfiguration.put( configDir, config );
		}
		
		assert config != null;
		return config;
	}
	
	/**
	 * Helper to read the system configuration file. Lazy retrieval-on-demand.
	 */
	private static synchronized Configuration readSystemConfiguration( File configDir ) {
		Configuration result = new Configuration( "system" );
		
		File xml = new LocalFile( configDir, "system", LocalFile.Type.XML ).getFile();
		try {
			FileReader reader = new FileReader( xml );
			result = new Configuration( reader );
		} catch ( FileNotFoundException ex ) {}
		
		return result;
	}
	
	/** The tag name of the corresponding xml element */
	private String name;
	
	/** The element's attributes as string name/value associations */
	private Properties attributes;
	
	/** The list of children representing nested elements, in the order read or created */
	private List children;
	
	/** The element's text (if any) in the normalized form. If none, text is empty. */
	private String text;
	
	/** List of strings (without "<!--" and "-->") to include as comments before the document element */
	private List comments;
	
	/**
	 * User maintained map of name value pairs. Independent of the structure
	 * of the xml that is read or written.
	 */
	private Map values;
	
	/** Cached hashCode. -1 means need to re-compute. Mutators re-set this to one. */
	private int hashCode = -1;
	
	/**
	 * Constructs a Configuration instance that mirrors the structure elements in the stream.
	 * The constructed Configuration instance corresponds to the top-level document element.
	 *
	 * @throws ConfigurationException
	 *		If the stream cannot be parsed as an xml document.
	 */
	public Configuration( Reader in ) {
		this( new InputSource( in ) );
	}
	
	/**
	 * Constructs a Configuration instance that mirrors the structure elements in the stream.
	 * The constructed Configuration instance corresponds to the top-level document element.
	 *
	 * @throws ConfigurationException
	 *		If the stream cannot be parsed as an xml document.
	 */
	public Configuration( InputStream in ) {
		this( new InputSource( in ) );
	}
	
	/**
	 * Constructs a Configuration instance that mirrors the structure elements in the file.
	 * The constructed Configuration instance corresponds to the top-level document element.
	 *
	 * @throws ConfigurationException
	 *		If the file cannot be parsed as an xml document.
	 */
	public Configuration( File xml ) {
		FileReader fr = null;
		
		try {
			fr = new FileReader( xml );
		} catch ( FileNotFoundException ex ) {
			throw new ConfigurationException( "Missing configuration file:", ex );
		}
		
		this.readSource( new InputSource( fr ) );
	}
	
	/**
	 * Constructs a Configuration instance that mirrors the structure elements in the source.
	 * The constructed Configuration instance corresponds to the top-level document element.
	 *
	 * @throws ConfigurationException
	 *		If the source cannot be parsed as an xml document.
	 */
	private Configuration( InputSource in ) {
		this.readSource( in );
	}
	
	/**
	 * Constructs a Configuration instance that mirrors the given element 
	 * and its sub-tree of elements.
	 */
	private Configuration( Element element ) {
		this.readElement( element );
	}
	
	/**
	 * Use DOM/SAX to parse the source.
	 *
	 * This constructor is not exposed since one of the purposes of this class is to
	 * encapsulate access to DOM in the framework.
	 */
	private void readSource( InputSource in ) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse( in );
			Element elt = doc.getDocumentElement();
		
			this.readElement( elt );
		} catch ( Exception ex ) {
			ex.printStackTrace();
			throw new ConfigurationException( "Unable to parse configuration information", ex );
		}
	}
	
	/**
	 * Traverse the SAX Element tree
	 *
	 * All constructors go through here so we initiailize state here.
	 */
	private void readElement( Element element ) {
		this.text = "";
		this.comments = new LinkedList();
		this.attributes = new Properties();
		this.children = new LinkedList();
		this.values = new HashMap();
		
		this.name = element.getNodeName();
		
		if ( element.hasAttributes() ) {
			NamedNodeMap attrs = element.getAttributes();
			Node node = null;
			for ( int i = 0; i < attrs.getLength(); i++ ) {
				node = attrs.item( i );
				this.attributes.setProperty( node.getNodeName(), node.getNodeValue() );
			}
		}
		
		if ( element.hasChildNodes() ) {
			NodeList children = element.getChildNodes();
			Node node = null;
			for ( int i = 0; i < children.getLength(); i++ ) {
				node = children.item( i );
				if ( node.getNodeType() == Node.ELEMENT_NODE ) {
					this.children.add( new Configuration( (Element) node ) );
				}
				if ( node.getNodeType() == Node.TEXT_NODE ) {
					this.text += node.getNodeValue();
				}
				if ( node.getNodeType() == Node.COMMENT_NODE ) {
					this.comments.add( node.getNodeValue() );
				}
			}
		}
	}

	/**
	 * Construct an empty top-level named element.
	 */
	public Configuration( String name ) {
		assert Strings.isNotEmpty( name );
		
		this.name = name;
		this.attributes = new Properties();
		this.children = new LinkedList();
		this.text = "";
		this.comments = new LinkedList();
	}
	
	/**
	 * Get the text content (if any) associated with this element.
	 *
	 * @return
	 *		A non-null string. The result may be empty if the node contains
	 *		no text.
	 */
	public String getText() {
		return this.text;
	}
	
	/**
	 * Set the configuration item's text content.
	 *
	 * @param text
	 *		A non-null string.
	 */
	public void setText( String text ) {
		this.text = text;
	}
	
	/**
	 * Add a comment to the configuration item. When the element is printed,
	 * all comment lines are printed before the element.
	 *
	 * @param comment
	 *		The comment to be added, without the xml comment delimiters
	 *		"<!--" and "-->"
	 */
	public void addComment( String comment ) {
		assert comment != null;
		
		this.comments.add( comment );
	}

	/** Get the tag name of the element */
	public String getName() {
		return this.name;
	}
	
	/** Get all attributes. Not null, but may be empty */
	public Properties getAttributes() {
		assert this.attributes != null;
		return this.attributes;
	}
	
	/** Get the value of the named attribute or null if not defined */
	public String getAttribute( String name ) {
		return this.attributes.getProperty( name );
	}
	
	/** Add a new name/value attribute. Returns this Configuration. */
	public Configuration addAttribute( String name, String value ) {
		assert Strings.isNotEmpty( name );
		assert Strings.isNotEmpty( value );
		
		this.hashCode = -1;
		this.attributes.setProperty( name, value );
		return this;
	}
	
	public void setValue( String name, Object value ) {
		this.values.put( name, value );
	}
	
	public Object getValue( String name ) {
		return this.values.get( name );
	}
	
	/**
	 * Returns a list of Configuration instances corresponding to
	 * nested elements.  The order is determined by the order the
	 * elements are read from the file or added to this
	 * instance. Return is not null but may be empty.  
	 */
	public List getChildren() {
		assert this.children != null;
		
		return this.children;
	}

	/**
	 * Get all children with the given tag name. Result is not null
	 * but may be empty.  
	 */
	public List getChildren( String name ) {
		assert Strings.isNotEmpty( name );
		
		List result = new ArrayList();
		
		Configuration child = null;
		Iterator iter = this.children.iterator();
		while ( iter.hasNext() ) {
			child = (Configuration) iter.next();
			if ( child.getName().equals( name ) ) {
				result.add( child );
			}
		}
		
		return result;
	}
	
	/**
	 * If there is exactly one child with the given tag name return
	 * it.  Otherwise returns null.  

	 */
	public Configuration getChild( String name ) {
		Configuration child = null;
		
		List children = this.getChildren( name );
		if ( children.size() == 1 ) {
			child = (Configuration) children.iterator().next();
		}
		
		return child;
	}
	
	/**
	 * Make item a child and delete any other siblings by the same name.
	 */
	public void setChild( Configuration item ) {
		assert item != null;
		
		this.hashCode = -1;
		String name = item.getName();
		Iterator iter = this.children.iterator();
		while ( iter.hasNext() ) {
			if ( ((Configuration) iter.next()).getName().equals( name ) ) {
				iter.remove();
			}
		}
		this.children.add( item );
	}
	
	/**
	 * Add an empty nested Configuration element. The new element is added to
	 * the end of the list of children Configuration elements. The new
	 * element is returned.
	 */
	public Configuration makeChild( String name ) {
		assert Strings.isNotEmpty( name );
		
		this.hashCode = -1;
		Configuration c = new Configuration( name );
		this.children.add( c );
		return c;
	}
	
	/**
	 * Add the given item as a child at the end of the child list.
	 * This (parent) configuration instance is returned.
	 */
	public Configuration addChild( Configuration child ) {
		assert child != null;
		
		this.hashCode = -1;
		this.children.add( child );
		return this;
	}

	/**
	 * Equality based on structure (name, attributes, and children. Not comments)
	 */
	public boolean equals( Object other ) {
		boolean same = this == other;
		
		if ( !same && other instanceof Configuration ) {
			Configuration item = (Configuration) other;
			same = this.name.equals( item.name )
				&& this.attributes.equals( item.attributes )
				&& this.children.equals( item.children );
		}
		
		return same;
	}

	/**
	 * Cache the hashCode
	 */
	public int hashCode() {
		if ( this.hashCode == -1 ) {
			this.hashCode = this.name.hashCode() 
				^ this.attributes.hashCode() 
				^ this.children.hashCode();
		}
		
		return this.hashCode;
	}

	/**
	 * Produce an xml representation of this Configuration instance and print
	 * it on the given PrintWriter
	 */
	public void write( PrintWriter out ) {
		assert out != null;
		
		out.println( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
		out.println( "<!-- DO NOT EDIT -->" );
		out.println( "<!-- GENERATED " + new Date() + " -->" );
		this.writeNode( "", out );
		out.flush();
	}
	
	/**
	 * Write this configuration instance in the class config file for the
	 * given object. Over-writes any existing configuration.
	 */
	public void writeClassConfiguration( Configurable obj ) throws IOException {
		assert obj != null;

		File dir = Application.getInstance().getConfigDir();
		String name = Configuration.getTagName( obj );
		File xml = new LocalFile( dir, name, LocalFile.Type.XML ).getFile();
		FileWriter fw = new FileWriter( xml );
		PrintWriter out = new PrintWriter( fw );
		this.write( out );
	}

	/** Helper for writing one node and its descendents */
	private void writeNode( String prefix, PrintWriter out ) {
		out.print( prefix + "<" + this.getName() );
		this.writeAttrs( out );
		
		if ( this.children.isEmpty() && this.comments.isEmpty() && Strings.isEmpty( this.text ) ) {
			out.println( "/>" );
		} else {
			out.println( ">" );
			
			Iterator iter = this.comments.iterator();
			while ( iter.hasNext() ) {
				out.println( prefix + "    <!--" + iter.next() + "-->" );
			}
			
			if ( Strings.isNotEmpty( this.text ) ) {
				out.println( prefix + "    " + this.text.trim() );
			}
			
			iter = this.children.iterator();
			while ( iter.hasNext() ) {
				((Configuration) iter.next()).writeNode( prefix + "    ", out );
			}
			out.println( prefix + "</" + this.getName() + ">" );
		}
	}

	/** Helper for writing an element's attributes */
	private void writeAttrs( PrintWriter out ) {
		Map.Entry entry = null;
		Iterator attrs = this.attributes.entrySet().iterator();
		while ( attrs.hasNext() ) {
			entry = (Map.Entry) attrs.next();
			out.print( " " + entry.getKey() + "=\"" + entry.getValue() + "\"" );
		}
	}

	/**
	 * A convenience method for writing the xml rep to standard out.
	 */
	public void print() {
		this.write( new PrintWriter( System.out ) );
	}
	
	public String toString() {
		StringWriter sw = new StringWriter();
		
		PrintWriter pw = new PrintWriter( sw );
		this.write( pw );
		
		return sw.toString();
	}
	
}
