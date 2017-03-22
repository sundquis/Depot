package sundquis.junk;

import jxl.Workbook;
import jxl.Sheet;
import jxl.Cell;

import java.io.File;

public class Gradebook {
	
	public static void display( String path ) throws Exception {
		File file = new File( path );
		Workbook wb = Workbook.getWorkbook( file );
		Sheet sheet = wb.getSheet( 0 );
		Cell[] cells = sheet.getRow( 5 );
		int numCols = cells.length;
		for ( int j = 0; j < numCols; j++ ){
			cells = sheet.getColumn( j );
			System.out.printf( "%4.3s", String.valueOf(j+1) );
			for ( int i = 0; i < Math.min( cells.length, 10); i++ ) {
				System.out.printf( "%8.7s", cells[i].getContents() );
			}
			System.out.println();;
		}
		
		wb.close();
	}
	
	public static String[] GB = {
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.1.FALL/1500/gradebook.xls",
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.1.FALL/1400/gradebook.xls",
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.1.FALL/700/grades.xls",
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.3.SUMMER/1510/grades2.xls",
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.3.SUMMER/1510/grades.xls",
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.2.SPRING/1100/Grades.xls",
		"/home/sundquis/book/Dropbox/tmp/03-04/03-04.2.SPRING/1500/Grades.xls",
		"/home/sundquis/book/Dropbox/tmp/07-08/07-08.1.FALL/MC14.16/Grades.xls",
		"/home/sundquis/book/Dropbox/tmp/07-08/07-08.1.FALL/1500/Grades.xls",
		"/home/sundquis/book/Dropbox/tmp/07-08/07-08.2.SPRING/1510/Grades.xlsx",
		"/home/sundquis/book/Dropbox/tmp/07-08/07-08.3.SUMMER/1500/Grades.xlsx",
		"/home/sundquis/book/Dropbox/tmp/07-08/07-08.3.SUMMER/2700/Grades.xlsx",
		"/home/sundquis/book/Dropbox/tmp/16-17/16-17.1.FALL/1520/Grades.1520.30.xlsx",
		"/home/sundquis/book/Dropbox/tmp/16-17/16-17.1.FALL/1500/Grades.1500.90.xlsx",
		"/home/sundquis/book/Dropbox/tmp/16-17/16-17.1.FALL/2011/Grades.2011.10.xlsx"
	};
	
	public static void main(String[] args) {
		System.out.println( "MAIN" );
		
		try {
			display( args[0] );
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
