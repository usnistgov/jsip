/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip;

import gov.nist.javax.sip.header.*;

/**
 * Default constants for SIP.
 * @version 1.2 $Revision: 1.6 $ $Date: 2006-07-02 09:54:22 $
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
	
	public static final String BRANCH_MAGIC_COOKIE_LOWER_CASE = "z9hg4bk";

	/**
	 * constant SIP_VERSION_STRING 
	 */
	public static final String SIP_VERSION_STRING = "SIP/2.0";
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.3  2006/06/16 15:26:29  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.2  2005/11/14 22:36:02  mranga
 * Interim update of source code
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.5  2004/10/28 19:02:48  mranga
 * Submitted by:  Daniel Martinez
 * Reviewed by:   M. Ranganathan
 *
 * Added changes for TLS support contributed by Daniel Martinez
 *
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
