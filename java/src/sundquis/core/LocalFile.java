/*
 * Copyright( c ) 2001 by Circus Software.
 * All rights reserved.
 *
 * This source code, and its documentation, are the confidential intellectual
 * property of Circus Software and may not be disclosed, reproduced,
 * distributed, or otherwise used for any purpose without the expressed
 * written permission of Circus Software.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * $Source: /usr/local/cvsroot/circus/java/src/com/circus/core/LocalFile.java,v $
 * $Id: LocalFile.java,v 1.14 2002/10/02 21:11:29 toms Exp $
 * $Log: LocalFile.java,v $
 * Revision 1.14  2002/10/02 21:11:29  toms
 * no message
 *
 * Revision 1.13  2002/09/19 23:23:41  toms
 * Added xml type.
 *
 * Revision 1.12  2002/07/16 02:48:58  toms
 * Filr type for serailized indices.
 *
 * Revision 1.11  2002/05/31 17:17:50  toms
 * Fixed assert keyword conflicts, renamed to omAssert
 *
 * Revision 1.10  2002/05/20 20:36:41  toms
 * Added new file type: HTML
 *
 * Revision 1.9  2002/05/06 16:17:30  toms
 * Improved constructor selection
 *
 * Revision 1.8  2002/04/16 20:03:56  toms
 * Added hsql type.
 *
 * Revision 1.7  2001/12/14 21:15:38  toms
 * Added constructor that accepts path components in a string array.
 *
 * Revision 1.6  2001/11/26 21:20:42  toms
 * Added new file type for Spf data files.
 *
 * Revision 1.5  2001/10/03 16:12:49  toms
 * Changed suffix for property files to agree with Sun convention.
 *
 * Revision 1.4  2001/09/25 10:03:47  toms
 * Added new file type btr for BTree indices.
 *
 * Revision 1.3  2001/08/15 09:35:56  toms
 * Integrated Scott's changes.
 *
 * Revision 1.2  2001/07/26 13:51:21  toms
 * Added RCS fields.
 *
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 */

package com.circus.core;

import java.io.File;

/**
 * A class for modelling files relative to the root directory. When a
 * LocalFile instance is first created it is mutable in that subdirectories
 * may be appended to the current directory. Once the instance is used in
 * any other way it becomes immutable.
 *
 * <p>
 * The <tt>addDir</tt> method returns an instance reference so that successive
 * calls may be chained. Thus a typical usage might look like:
 *
 * <pre><tt>
 * LocalFile local = new LocalFile( "cache", LocalFile.DATA );
 * File file = local.addDir( "data" ).addDir( "ui" ).getFile();
 * </pre></tt>
 *
 * This results in a file instance that represents the path
 * data/ui/cache.ser relative to the environmental root directory.
 */
public class LocalFile {

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	public static class TEST extends Test {

		public static void main( String[] args ) {
			Test t = new TEST();

			LocalFile file = new LocalFile( new String[] { "a", "b" }, LocalFile.Type.DIR );
			t.testEval( "DIR a/b", file.getFile() );

			t.testSummary();
		}

	}

	// Some IDEs do not allow nested classes to be started.
	// This line not allowed in final production code.
	public static void main( String[] args ) { LocalFile.TEST.main( args ); }

	/* * * * * * * * * * * * * * * TEST * * * * * * * * * * * * * * */

	/**
	 * The enumerated type for possible extensions.
	 */
	public static final class Type extends Enum {

		private Type( String label ) {
			super( label );
		}

		/** For local directories */
		public static Type DIR = new Type( "dir" );

		/** No system type defined. */
		public static Type PLAIN = new Type( "plain" );

		/** File used to store configuration properties */
		public static Type PROPERTY = new Type( "properties" );

		/** Temporary file*/
		public static Type TEMPORARY = new Type( "tmp" );

		/** File contains comma separated values */
		public static Type CSV = new Type( "csv" );

		/** File contains generic binary data */
		public static Type DATA = new Type( "dat" );

		/** File contains serialized objects */
		public static Type SERIAL = new Type( "ser" );

		/** Text file */
		public static Type TEXT = new Type( "txt" );

		/** Data supporting a BTree index */
		public static Type BTREE = new Type( "btr" );

		/** Spf data file */
		public static Type SDF = new Type( "sdf" );

		/** Java source file */
		public static Type SRC = new Type( "java" );

		/** HSQL data file */
		public static Type HSQL = new Type( "hsql" );

		/** HTML file */
		public static Type HTML = new Type( "html" );
		
		/** File used to store a database index */
		public static Type INDEX = new Type( "idx" );
		
		/** An xml file */
		public static Type XML = new Type( "xml" );
		
		/** A password file used for local authentication */
		public static Type PWD = new Type( "pwd" );
	}

	private File file;
	private String name;
	private Type type;
	private boolean isSet = false;

	/**
	 * To construct a local file must specify the directory, name, and type.
	 *
	 * @param root
	 *	    The root directory. The desired file will not necessarily reside
	 *	    in the root. The addDir() method can be used to specify the path
	 *	    from the root to the named file.
	 *
	 * @param name
	 *	    The non-empty string name.
	 *
	 * @param type
	 *	    A non-null instance of the enumerated type LocalFile.Type
	 */
	public LocalFile( File root, String name, Type type ) {
		Contract.cAssert( root != null );
		Contract.cAssert( Strings.isNotEmpty( name ) );
		Contract.cAssert( type != null );

		this.file = root;
		this.name = name;
		this.type = type;
	}

	/**
	 * If no directory is given we assume a path relative to the environment's
	 * root directory.
	 *
	 * @param name
	 *	    The non-empty string name.
	 *
	 * @param type
	 *	    A non-null instance of the enumerated type LocalFile.Type
	 */
	public LocalFile( String name, Type type ) {
		this( Application.getInstance().getRootDir(), name, type );
	}

	/**
	 * Constructs a local file with path determined by the application root
	 * and given path components and type.
	 *
	 * @param pathComponents
	 *	    The non-null and non-empty array of path component names.
	 *
	 * @param type
	 *	    A non-null instance of the enumerated type LocalFile.Type
	 */
	public LocalFile( String[] pathComponents, Type type ) {
		this( pathComponents[ pathComponents.length - 1 ], type );

		for ( int i = 0; i < pathComponents.length - 1; i++ ) {
			this.addDir( pathComponents[i] );
		}
	}

	/**
	 * Constructs a local file with path determined by the given root
	 * and given path components and type.
	 *
	 * @param root
	 *	    The root directory. The path components are taken to be
	 *      relative to this directory.
	 *
	 * @param pathComponents
	 *	    The non-null and non-empty array of path component names.
	 *
	 * @param type
	 *	    A non-null instance of the enumerated type LocalFile.Type
	 */
	public LocalFile( File root, String[] pathComponents, Type type ) {
		this( root, pathComponents[ pathComponents.length - 1 ], type );

		for ( int i = 0; i < pathComponents.length - 1; i++ ) {
			this.addDir( pathComponents[i] );
		}
	}

	/**
	 * Add a subdirectory( relative to the current root directory ) to this
	 * local file. This can only be done before the final file has been set.
	 *
	 * @param subDir
	 *	    The non-empty name of the sub-directory to be appended.
	 *
	 * @return
	 *      This LocalFile instance.
	 */
	public LocalFile addDir( String subDir ) {
		Contract.cAssert( Strings.isNotEmpty( subDir ) );
		Contract.cAssert( !isSet );

		this.file = new File( this.file, subDir );

		return this;
	}

	/**
	 * Retrieves the file represented by this instance. After this call the
	 * local file instance can no longer be altered( no additional sub-
	 * directories can be added. )
	 *
	 * @return
	 *      The non-null file instance representing the local file.
	 */
	public File getFile() {
		setFile();

		Contract.cAssert( this.file != null );
		return this.file;
	}

	/**
	 * Builds the final file instance from the current parent directory
	 *( given by <tt>file</tt> ), the name, and the type. If the file
	 * has already been set this call has no affect.
	 */
	private synchronized void setFile() {
		if ( !isSet ) {
			String fullName = this.name;
			if ( this.type != Type.DIR && this.type != Type.PLAIN ) {
				fullName += "." + this.type.getLabel();
			}
			this.file = new File( this.file, fullName );
			this.isSet = true;
		}
	}

}
