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
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.address;
import gov.nist.core.*;

/**
 * Authority part of a URI structure. Section 3.2.2 RFC2396
 *
 * @version 1.2 $Revision: 1.8 $ $Date: 2007-10-22 03:38:22 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 *
 */
public class Authority extends NetObject {

	private static final long serialVersionUID = -3570349777347017894L;

	/** hostport field
	 */
	protected HostPort hostPort;

	/** userInfo field
	 */
	protected UserInfo userInfo;

	/**
	 * Return the host name in encoded form.
	 * @return encoded string (does the same thing as toString)
	 */
	public String encode() {
		return encode(new StringBuffer()).toString();
	}

	public StringBuffer encode(StringBuffer buffer) {
		if (userInfo != null) {
			userInfo.encode(buffer);
			buffer.append(AT);
			hostPort.encode(buffer);
		} else {
			hostPort.encode(buffer);
		}
		return buffer;
	}

	/** retruns true if the two Objects are equals , false otherwise.
	 * @param other Object to test.
	 * @return boolean
	 */
	public boolean equals(Object other) {
		if (other.getClass() != getClass()) {
			return false;
		}
		Authority otherAuth = (Authority) other;
		if (!this.hostPort.equals(otherAuth.hostPort)) {
			return false;
		}
		if (this.userInfo != null && otherAuth.userInfo != null) {
			if (!this.userInfo.equals(otherAuth.userInfo)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * get the hostPort member.
	 * @return HostPort
	 */
	public HostPort getHostPort() {
		return hostPort;
	}

	/**
	 * get the userInfo memnber.
	 * @return UserInfo
	 */
	public UserInfo getUserInfo() {
		return userInfo;
	}

	/**
	     * Get password from the user info.
	     * @return String
	     */
	public String getPassword() {
		if (userInfo == null)
			return null;
		else
			return userInfo.password;
	}

	/**
	 * Get the user name if it exists.
	 * @return String user or null if not set.
	 */
	public String getUser() {
		return userInfo != null ? userInfo.user : null;
	}

	/**
	 * Get the host name.
	 * @return Host (null if not set)
	 */
	public Host getHost() {
		if (hostPort == null)
			return null;
		else
			return hostPort.getHost();
	}

	/**
	 * Get the port.
	 * @return int port (-1) if port is not set.
	 */
	public int getPort() {
		if (hostPort == null)
			return -1;
		else
			return hostPort.getPort();
	}

	/** remove the port.
	 */
	public void removePort() {
		if (hostPort != null)
			hostPort.removePort();
	}

	/**
	 * set the password.
	 * @param passwd String to set
	 */
	public void setPassword(String passwd) {
		if (userInfo == null)
			userInfo = new UserInfo();
		userInfo.setPassword(passwd);
	}

	/**
	 * Set the user name of the userInfo member.
	 * @param user String to set
	 */
	public void setUser(String user) {
		if (userInfo == null)
			userInfo = new UserInfo();
		this.userInfo.setUser(user);
	}

	/**
	 * set the host.
	 * @param host Host to set
	 */
	public void setHost(Host host) {
		if (hostPort == null)
			hostPort = new HostPort();
		hostPort.setHost(host);
	}

	/**
	 * Set the port.
	 * @param port int to set
	 */
	public void setPort(int port) {
		if (hostPort == null)
			hostPort = new HostPort();
		hostPort.setPort(port);
	}

	/**
	     * Set the hostPort member
	     * @param h HostPort to set
	     */
	public void setHostPort(HostPort h) {
		hostPort = h;
	}

	/**
	     * Set the userInfo member
	     * @param u UserInfo to set
	     */
	public void setUserInfo(UserInfo u) {
		userInfo = u;
	}

	/** Remove the user Infor.
	*
	*/
	public void removeUserInfo() {
		this.userInfo = null;
	}

	public Object clone() {
		Authority retval = (Authority) super.clone();
		if (this.hostPort != null)
			retval.hostPort = (HostPort) this.hostPort.clone();
		if (this.userInfo != null)
			retval.userInfo = (UserInfo) this.userInfo.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2007/02/12 15:19:18  belangery
 * Changed the encode() and encodeBody() methods of SIP headers and basic classes to make them use the same StringBuffer instance during the encoding phase.
 *
 * Revision 1.6  2006/07/13 09:02:27  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
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
 * Revision 1.3  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:29  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:34  mranga
 *
 * Import
 *
 *
 * Revision 1.4  2005/04/16 20:39:31  dmuresan
 * Eliminated 'c1.getClass().getName().equals(c2.getClass().getName())' idiom (c1 == c2 suffices)
 *
 * Revision 1.3  2005/04/16 20:38:46  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
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
