/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.core;

/**
 *   A class to do debug printfs
 */
public class Debug {

	public static final boolean debug = false;
	public static final boolean parserDebug = false;

	public static void println(String s) {
		if (debug)
			System.out.println(s + "\n");
	}
	public static void printStackTrace(Exception ex) {
		if (debug) {
			ex.printStackTrace();
		}
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
