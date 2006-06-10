/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;
import java.net.*;

/**
* Holds the hostname:port.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public final class HostPort extends GenericObject {

	// host / ipv4/ ipv6/ 
	/** host field
	 */
	protected Host host;

	/** port field
	 *
	 */
	protected int port;

	/** Default constructor
	 */
	public HostPort() {

		host = null;
		port = -1; // marker for not set.
	}

	/**
	 * Encode this hostport into its string representation.
	 * Note that this could be different from the string that has
	 * been parsed if something has been edited.
	 * @return String
	 */
	public String encode() {
		StringBuffer retval = new StringBuffer();
		retval.append(host.encode());
		if (port != -1)
			retval.append(COLON).append(port);
		return retval.toString();
	}

	/** returns true if the two objects are equals, false otherwise.
	 * @param other Object to set
	 * @return boolean
	 */
	public boolean equals(Object other) {
		if (getClass () != other.getClass ()) {
			return false;
		}
		HostPort that = (HostPort) other;
		return port == that.port && host.equals(that.host);
	}

	/** get the Host field
	 * @return host field
	 */
	public Host getHost() {
		return host;
	}

	/** get the port field
	 * @return int
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns boolean value indicating if Header has port
	 * @return boolean value indicating if Header has port
	 */
	public boolean hasPort() {
		return port != -1;
	}

	/** remove port.
	 */
	public void removePort() {
		port = -1;
	}

	/**
	     * Set the host member
	     * @param h Host to set
	     */
	public void setHost(Host h) {
		host = h;
	}

	/**
	     * Set the port member
	     * @param p int to set
	     */
	public void setPort(int p) {
		port = p;
	}

	/** Return the internet address corresponding to the host.
	 *@throws java.net.UnkownHostException if host name cannot be resolved.
	 *@return the inet address for the host.
	 */
	public InetAddress getInetAddress() throws java.net.UnknownHostException {
		if (host == null)
			return null;
		else
			return host.getInetAddress();
	}

	public void merge(Object mergeObject) {
		super.merge (mergeObject);
		if (port == -1)
			port = ((HostPort) mergeObject).port;
	}

	public Object clone() {
		HostPort retval = (HostPort) super.clone();
		if (this.host != null)
			retval.host = (Host) this.host.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2005/04/11 09:36:28  dmuresan
 * Fixed HostPort.merge(), though merge() is never used.
 *
 * Revision 1.5  2005/04/04 17:20:11  dmuresan
 * Simplified gov.nist.core.HostPort implementation.
 *
 * Revision 1.4  2004/01/22 13:26:27  sverker
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
