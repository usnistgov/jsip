/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * CallInfo SIPHeader.
 *
 * @author "M. Ranganathan" <mranga@nist.gov> <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:48 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class CallInfo
	extends ParametersHeader
	implements javax.sip.header.CallInfoHeader {

	protected GenericURI info;

	/**
	 * Default constructor
	 */
	public CallInfo() {
		super(CALL_INFO);
	}

	/**
	 * Return canonical representation.
	 * @return String 
	 */
	public String encodeBody() {
		StringBuffer encoding = new StringBuffer();

		encoding.append(LESS_THAN).append(info.toString()).append(GREATER_THAN);

		if (parameters != null && !parameters.isEmpty())
			encoding.append(SEMICOLON).append(parameters.encode());

		return encoding.toString();
	}

	/**
	 * get the purpose field
	 * @return String
	 */
	public String getPurpose() {
		return this.getParameter("purpose");
	}

	/**
	 * get the URI field
	 * @return URI
	 */
	public javax.sip.address.URI getInfo() {
		return info;
	}

	/**
	 * set the purpose field
	 * @param purpose is the purpose field.
	 */
	public void setPurpose(String purpose) {
		if (purpose == null)
			throw new NullPointerException("null arg");
		try {
			this.setParameter("purpose", purpose);
		} catch (ParseException ex) {
		}
	}

	/**
	 * set the URI field
	 * @param info is the URI to set.
	 */
	public void setInfo(javax.sip.address.URI info) {
		this.info = (GenericURI) info;
	}

	public Object clone() {
		CallInfo retval = (CallInfo) super.clone();
		if (this.info != null)
			retval.info = (GenericURI) this.info.clone();
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
