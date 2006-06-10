/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
* See ../../../../doc/uncopyright.html for conditions of use.                  *
* Author: M. Ranganathan (mranga@nist.gov)                                     *
* Modified By:  O. Deruelle (deruelle@nist.gov), added JAVADOC                 *                                                                                 
* Questions/Comments: nist-sip-dev@antd.nist.gov                               *
*******************************************************************************/
package gov.nist.javax.sip.header;

/**
 * Internal utility class for pretty printing and header formatting.
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-04 09:51:38 $
 */
class Indentation {

	private int indentation;

	/** Default constructor
	 */
	protected Indentation() {
		indentation = 0;
	}

	/** Constructor
	 * @param initval int to set
	 */
	protected Indentation(int initval) {
		indentation = initval;
	}

	/** set the indentation field
	 * @param initval int to set
	 */
	protected void setIndentation(int initval) {
		indentation = initval;
	}

	/** get the number of indentation.
	 * @return int
	 */
	protected int getCount() {
		return indentation;
	}

	/** increment the indentation field
	 */
	protected void increment() {
		indentation++;
	}

	/** decrement the indentation field
	 */
	protected void decrement() {
		indentation--;
	}

	/** get the indentation
	 * @return String
	 */
	protected String getIndentation() {
    char [] chars = new char [indentation];
    java.util.Arrays.fill (chars, ' ');
    return new String (chars);
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:29  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
