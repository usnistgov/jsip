/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.javax.sip.address.*;
import javax.sip.address.*;

/**
 * AlertInfo SIP Header.
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:48 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AlertInfo
	extends ParametersHeader
	implements javax.sip.header.AlertInfoHeader {

	/** URI field
	 */
	protected GenericURI uri;

	/** Constructor
	 */
	public AlertInfo() {
		super(NAME);
	}

	/**
	 * Return value encoding in canonical form.
	 * @return The value of the header in canonical encoding.
	 */
	protected String encodeBody() {
		StringBuffer encoding = new StringBuffer();
		encoding.append(LESS_THAN).append(uri.encode()).append(GREATER_THAN);
		if (!parameters.isEmpty()) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}
		return encoding.toString();
	}

	/**
	 * Set the uri member
	 * @param uri URI to set
	 */
	public void setAlertInfo(URI uri) {
		this.uri = (GenericURI) uri;
	}

	/**
	 * Returns the AlertInfo value of this AlertInfoHeader.
	 * @return the URI representing the AlertInfo.
	 *
	 * @since JAIN SIP v1.1
	 *
	 */
	public URI getAlertInfo() {
		return (URI) this.uri;
	}

	public Object clone() {
		AlertInfo retval = (AlertInfo) super.clone();
		if (this.uri != null)
			retval.uri = (GenericURI) this.uri.clone();
		return retval;
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
