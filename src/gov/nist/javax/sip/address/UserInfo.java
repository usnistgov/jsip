/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.address;

/**
 * User information part of a URL. 
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:39:31 $
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * Acknowledgement -- Lamine Brahimi 
 * Submitted a bug fix for a this class.
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class UserInfo extends NetObject {

	/** user field
	 */
	protected String user;

	/** password field
	 */
	protected String password;

	/** userType field
	     */
	protected int userType;

	/** Constant field
	 */
	public final static int TELEPHONE_SUBSCRIBER = 1;

	/** constant field
	 */
	public final static int USER = 2;

	/** Default constructor
	 */
	public UserInfo() {
		super();
	}

	/**
	 * Compare for equality.
	 * @param obj Object to set
	 * @return true if the two headers are equals, false otherwise.
	 */
	public boolean equals(Object obj) {
		if (getClass() != obj.getClass()) {
			return false;
		}
		UserInfo other = (UserInfo) obj;
		if (this.userType != other.userType) {
			return false;
		}
		if (!this.user.equalsIgnoreCase(other.user)) {
			return false;
		}
		if (this.password != null && other.password == null)
			return false;

		if (other.password != null && this.password == null)
			return false;

		if (this.password == other.password)
			return true;

		return (this.password.equals(other.password));
	}

	/**
	 * Encode the user information as a string.
	 * @return String
	 */
	public String encode() {
		if (password != null)
			return new StringBuffer()
				.append(user)
				.append(COLON)
				.append(password)
				.toString();
		else
			return user;
	}

	/** Clear the password field.
	*/
	public void clearPassword() {
		this.password = null;
	}

	/**
	 * Gets the user type (which can be set to TELEPHONE_SUBSCRIBER or USER)
	 * @return the type of user.
	 */
	public int getUserType() {
		return userType;
	}

	/** get the user field.
	 * @return String
	 */
	public String getUser() {
		return user;
	}

	/** get the password field.
	 * @return String
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the user member
	 * @param user String to set
	 */
	public void setUser(String user) {
		this.user = user;
		// BUG Fix submitted by Lamine Brahimi 
		// add this (taken form sip_messageParser)
		// otherwise comparison of two SipUrl will fail because this
		// parameter is not set (whereas it is set in sip_messageParser).
		if (user != null
			&& (user.indexOf(POUND) >= 0 || user.indexOf(SEMICOLON) >= 0)) {
			setUserType(TELEPHONE_SUBSCRIBER);
		} else {
			setUserType(USER);
		}
	}

	/**
	 * Set the password member
	 * @param p String to set
	 */
	public void setPassword(String p) {
		password = p;
	}

	/**
	 * Set the user type (to TELEPHONE_SUBSCRIBER or USER).
	 * @param type int to set
	 * @throws IllegalArgumentException if type is not in range.
	 */
	public void setUserType(int type) throws IllegalArgumentException {
		if (type != TELEPHONE_SUBSCRIBER && type != USER) {
			throw new IllegalArgumentException("Parameter not in range");
		}
		userType = type;
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
