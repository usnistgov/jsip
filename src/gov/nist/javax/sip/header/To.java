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
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

import javax.sip.header.ToHeader;

/**  
*To SIP Header.
*
*@version 1.2 $Revision: 1.6 $ $Date: 2006-07-02 09:50:46 $
*
*@author M. Ranganathan   <br/>
*@author Olivier Deruelle <br/>
*
*
*
*/

public final class To
	extends AddressParametersHeader
	implements javax.sip.header.ToHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -4057413800584586316L;

	/** default Constructor.
	 */
	public To() {
		super(TO);
	}

	/** Generate a TO header from a FROM header
	*/
	public To(From from) {
		super(TO);
		setAddress(from.address);
		setParameters(from.parameters);
	}

	/**
	 * Encode the header into a String.
	 * @since 1.0
	 * @return String
	 */
	public String encode() {
		return headerName + COLON + SP + encodeBody() + NEWLINE;
	}

	/**
	 * Encode the header content into a String.
	 * @return String
	 */
	protected String encodeBody() {
		if (address == null)
			return null;
		String retval = "";
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += LESS_THAN;
		}
		retval += address.encode();
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += GREATER_THAN;
		}

		if (!parameters.isEmpty()) {
		  synchronized(parameters) {
			  retval += SEMICOLON + parameters.encode();
			}
		}
		return retval;
	}

	/**
	 * Conveniance accessor function to get the hostPort field from the address.
	 * Warning -- this assumes that the embedded URI is a SipURL.
	 *
	 * @return hostport field
	 */
	public HostPort getHostPort() {
		if (address == null)
			return null;
		return address.getHostPort();
	}

	/**
	 * Get the display name from the address.
	 * @return Display name
	 */
	public String getDisplayName() {
		if (address == null)
			return null;
		return address.getDisplayName();
	}

	/**
	 * Get the tag parameter from the address parm list.
	 * @return tag field
	 */
	public String getTag() {
		if (parameters == null)
			return null;
		synchronized (this.parameters) {
			return getParameter(ParameterNames.TAG);
		}
	}

	/** Boolean function
	 * @return true if the Tag exist
	 */
	public boolean hasTag() {
		if (parameters == null)
			return false;
	        synchronized (this.parameters) {
		   return hasParameter(ParameterNames.TAG);
		}
	}

	/** remove Tag member
	 */
	public void removeTag() {
	      synchronized (this.parameters) {
		if (parameters != null)
			parameters.delete(ParameterNames.TAG);
	      }
	}

	/**
	 * Set the tag member. This should be set to null for the initial request
	 * in a dialog.
	 * @param t tag String to set.
	 */
	public void setTag(String t) throws ParseException {
		synchronized (this.parameters) {
		   if (t == null)
			throw new NullPointerException("null tag ");
		   else if (t.trim().equals(""))
			throw new ParseException("bad tag", 0);
		   this.setParameter(ParameterNames.TAG, t);
		}
	}

	/** Get the user@host port string.
	 */
	public String getUserAtHostPort() {
		if (address == null)
			return null;
		return address.getUserAtHostPort();
	}

	public boolean equals(Object other) {
		return (other instanceof ToHeader) && super.equals(other);
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.4  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.3  2006/01/31 06:57:04  jeroen
 * it should not be needed to synchronize on the parameters, but for consistency it's better to do it everywhere then
 * Someone reported a concurrent modification exception in encodeBody
 *
 * Revision 1.2  2005/10/09 18:47:53  jeroen
 * defined equals() in terms of API calls
 *
 * Revision 1.1.1.1  2005/10/04 17:12:35  mranga
 *
 * Import
 *
 *
 * Revision 1.4  2004/06/17 15:22:29  mranga
 * Reviewed by:   mranga
 *
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 *
 * Revision 1.3  2004/01/22 13:26:30  sverker
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
