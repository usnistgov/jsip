/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip;

import gov.nist.javax.sip.header.*;

/**
 * Default constants for SIP.
 * @version JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-10-28 19:02:48 $
 */
public interface SIPConstants
	extends
		SIPHeaderNames,
		gov.nist.javax.sip.address.ParameterNames,
		gov.nist.javax.sip.header.ParameterNames {
	public static final int DEFAULT_PORT = 5060;

	// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
	public static final int DEFAULT_TLS_PORT = 5061;

	/**
	 * Prefix for the branch parameter that identifies 
	 * BIS 09 compatible branch strings. This indicates
	 * that the branch may be as a global identifier for
	 * identifying transactions.
	 */
	public static final String BRANCH_MAGIC_COOKIE = "z9hG4bK";

	/**
	 * constant SIP_VERSION_STRING 
	 */
	public static final String SIP_VERSION_STRING = "SIP/2.0";
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/22 13:26:28  sverker
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
