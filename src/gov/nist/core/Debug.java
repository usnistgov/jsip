/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.core;
/**
*   A class to do debug printfs
*/

public class Debug {
	
        public static final boolean debug = true;
	public static  final boolean parserDebug = false;
        
	public static void print (String s ) {
		if (debug) LogWriter.logMessage(s);
	}
	
        public static void println (String s ) {
	    if (debug) LogWriter.logMessage(s+"\n");
	}
	public static void printStackTrace(Exception ex) {
		if (debug) {
		    LogWriter.logException(ex);
		}
	}
	
        /*
	protected static void Abort(Exception e) {
	    System.out.println("Fatal error");
	     e.printStackTrace();
	     if (debug) {
		    LogWriter.logException(e);
	     }
	     System.exit(0);
	}

	protected static void Assert(boolean b) {
		if ( ! b) {
		   System.out.println("Assertion failure !");
		    new Exception().printStackTrace();
		    if (debug) LogWriter.logStackTrace();
		    System.exit(0);
		}
	}
         */
}
