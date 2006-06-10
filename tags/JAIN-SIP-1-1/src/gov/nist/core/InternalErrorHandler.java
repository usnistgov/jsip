/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;
/**
*  Handle Internal error failures and print a stack trace (for debugging).
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class InternalErrorHandler {
	/**
	* Handle an unexpected exception.
	*/
	public static void handleException(Exception ex) {
		try {
			throw ex;
		} catch (Exception e) {
			System.err.println("Unexpected exception : " + e);
			System.err.println("Error message is " + ex.getMessage());
			System.err.println("*************Stack Trace ************");
			e.printStackTrace(System.err);
			System.exit(0);
		}
	}
	/**
	* Handle an unexpected condition (and print the error code).
	*/

	public static void handleException(String emsg) {
		try {
			throw new Exception(emsg);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
