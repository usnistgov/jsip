package gov.nist.javax.sip.address;

import java.util.Iterator;

/**
 * Implementation of the TelURL interface.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:47 $
 *
 * @author M. Ranganathan <mranga@nist.gov> 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class TelURLImpl
	extends GenericURI
	implements javax.sip.address.TelURL {

	protected String scheme;

	protected TelephoneNumber telephoneNumber;

	/** Creates a new instance of TelURLImpl */
	public TelURLImpl() {
		this.scheme = "tel";
	}

	/** Set the telephone number.
	 *@param telephoneNumber -- telephone number to set.
	 */

	public void setTelephoneNumber(TelephoneNumber telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	/** Returns the value of the <code>isdnSubAddress</code> parameter, or null
	 * if it is not set.
	 *
	 * @return  the value of the <code>isdnSubAddress</code> parameter
	 */
	public String getIsdnSubAddress() {
		return telephoneNumber.getIsdnSubaddress();
	}

	/** Returns the value of the <code>postDial</code> parameter, or null if it
	 * is not set.
	 *
	 * @return  the value of the <code>postDial</code> parameter
	 */
	public String getPostDial() {
		return telephoneNumber.getPostDial();
	}

	/** Returns the value of the "scheme" of this URI, for example "sip", "sips"
	 * or "tel".
	 *
	 * @return the scheme paramter of the URI
	 */
	public String getScheme() {
		return this.scheme;
	}

	/** Returns <code>true</code> if this TelURL is global i.e. if the TelURI
	 * has a global phone user.
	 *
	 * @return <code>true</code> if this TelURL represents a global phone user,
	 * and <code>false</code> otherwise.
	 */
	public boolean isGlobal() {
		return telephoneNumber.isGlobal();
	}

	/** This method determines if this is a URI with a scheme of "sip" or "sips".
	 *
	 * @return true if the scheme is "sip" or "sips", false otherwise.
	 */
	public boolean isSipURI() {
		return false;
	}

	/** Sets phone user of this TelURL to be either global or local. The default
	 * value is false, hence the TelURL is defaulted to local.
	 *
	 * @param global - the boolean value indicating if the TelURL has a global
	 * phone user.
	 */
	public void setGlobal(boolean global) {
		this.telephoneNumber.setGlobal(global);
	}

	/** Sets ISDN subaddress of this TelURL. If a subaddress is present, it is
	 * appended to the phone number after ";isub=".
	 *
	 * @param isdnSubAddress - new value of the <code>isdnSubAddress</code>
	 * parameter
	 */
	public void setIsdnSubAddress(String isdnSubAddress) {
		this.telephoneNumber.setIsdnSubaddress(isdnSubAddress);
	}

	/** Sets post dial of this TelURL. The post-dial sequence describes what and
	 * when the local entity should send to the phone line.
	 *
	 * @param postDial - new value of the <code>postDial</code> parameter
	 */
	public void setPostDial(String postDial) {
		this.telephoneNumber.setPostDial(postDial);
	}

	/** 
	 * Set the telephone number.
	 * @param telephoneNumber long phone number to set.
	 */
	public void setPhoneNumber(String telephoneNumber) {
		this.telephoneNumber.setPhoneNumber(telephoneNumber);
	}

	/** Get the telephone number. 
	 *
	 *@return -- the telephone number.
	 */
	public String getPhoneNumber() {
		return this.telephoneNumber.getPhoneNumber();
	}

	/** Return the string encoding.
	 *
	 *@return -- the string encoding.
	 */
	public String toString() {
		return this.scheme + ":" + telephoneNumber.encode();
	}

	public String encode() {
		return this.scheme + ":" + telephoneNumber.encode();
	}

	/** Deep copy clone operation.
	*
	*@return -- a cloned version of this telephone number.
	*/
	public Object clone() {
		TelURLImpl retval = (TelURLImpl) super.clone();
		if (this.telephoneNumber != null)
			retval.telephoneNumber = (TelephoneNumber) this.telephoneNumber.clone();
		return retval;
	}

	public String getParameter(String parameterName) {
		return telephoneNumber.getParameter(parameterName);
	}

	public void setParameter(String name, String value) {
		telephoneNumber.setParameter(name, value);
	}

	public Iterator getParameterNames() {
		return telephoneNumber.getParameterNames();
	}

	public void removeParameter(String name) {
		telephoneNumber.removeParameter(name);
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
