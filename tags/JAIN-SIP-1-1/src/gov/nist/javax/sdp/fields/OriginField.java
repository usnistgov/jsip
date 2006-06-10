/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
/**
*   Origin Field SDP header
*
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class OriginField extends SDPField implements javax.sdp.Origin {
	protected String username;
	protected long sessId;
	protected long sessVersion;
	protected String nettype; // IN
	protected String addrtype; // IPV4/6
	protected Host address;

	public OriginField() {
		super(ORIGIN_FIELD);
	}

	/** Returns the name of the session originator.
	* @throws SdpParseException
	* @return the string username.
	*/
	public String getUsername() throws SdpParseException {
		return username;
	}
	/**
	* Get the sessionID member.
	*/
	public long getSessId() {
		return sessId;
	}
	/**
	* Get the sessionVersion member.
	*/
	public long getSessVersion() {
		return sessVersion;
	}
	/**
	* Get the netType member.
	*/
	public String getNettype() {
		return nettype;
	}
	/**
	* Get the address type member.
	*/
	public String getAddrtype() {
		return addrtype;
	}
	/**
	* Get the host member.
	*/
	public Host getHost() {
		return address;
	}
	/**
	* Set the sessId member  
	*/
	public void setSessId(long s) {
		sessId = s;
	}
	/**
	* Set the sessVersion member  
	*/
	public void setSessVersion(long s) {
		sessVersion = s;
	}
	/**
	* Set the nettype member  
	*/
	public void setNettype(String n) {
		nettype = n;
	}
	/**
	* Set the addrtype member  
	*/
	public void setAddrtype(String a) {
		addrtype = a;
	}
	/**
	* Set the address member  
	*/
	public void setAddress(Host a) {
		address = a;
	}

	/** Sets the name of the session originator.
	 * @param user the string username.
	 * @throws SdpException if the parameter is null
	 */
	public void setUsername(String user) throws SdpException {
		if (user == null)
			throw new SdpException("The user parameter is null");
		else {
			this.username = user;
		}
	}

	/** Returns the unique identity of the session.
	 * @throws SdpParseException
	 * @return the session id.
	 */
	public long getSessionId() throws SdpParseException {
		return getSessId();
	}

	/** Sets the unique identity of the session.
	 * @param id  the session id.
	 * @throws SdpException if the id is <0
	 */
	public void setSessionId(long id) throws SdpException {
		if (id < 0)
			throw new SdpException("The is parameter is <0");
		else
			setSessId(id);
	}

	/** Returns the unique version of the session.
	 * @throws SdpException
	 * @return the session version.
	 */
	public long getSessionVersion() throws SdpParseException {
		return getSessVersion();
	}

	/** Sets the unique version of the session.
	 * @param version  the session version.
	 * @throws SdpException if the version is <0
	 */
	public void setSessionVersion(long version) throws SdpException {
		if (version < 0)
			throw new SdpException("The version parameter is <0");
		else
			setSessVersion(version);
	}

	/** Returns the type of the network for this Connection.
	 * @throws SdpParseException
	 * @return the string network type.
	 */
	public String getAddress() throws SdpParseException {
		Host addr = getHost();
		if (addr == null)
			return null;
		else
			return addr.getAddress();
	}

	/** Returns the type of the address for this Connection.
	 * @throws SdpParseException
	 * @return the string address type.
	 */
	public String getAddressType() throws SdpParseException {
		return getAddrtype();
	}

	/** Returns the type of the network for this Connection
	* @throws SdpParseException
	* @return the string network type.
	*/
	public String getNetworkType() throws SdpParseException {
		return getNettype();
	}

	/** Sets the type of the address for this Connection.
	 * @param addr  string address type.
	 * @throws SdpException if the addr is null
	 */
	public void setAddress(String addr) throws SdpException {
		if (addr == null)
			throw new SdpException("The addr parameter is null");
		else {
			Host host = getHost();
			if (host == null)
				host = new Host();
			host.setAddress(addr);
			setAddress(host);
		}
	}

	/** Returns the type of the network for this Connection.
	 * @param type the string network type.
	 * @throws SdpException if the type is null
	 */
	public void setAddressType(String type) throws SdpException {
		if (type == null)
			throw new SdpException("The type parameter is <0");
		else
			setAddrtype(type);
	}

	/** Sets the type of the network for this Connection.
	 * @param type  the string network type.
	 * @throws SdpException if the type is null
	 */
	public void setNetworkType(String type) throws SdpException {
		if (type == null)
			throw new SdpException("The type parameter is <0");
		else
			setNettype(type);
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		return ORIGIN_FIELD
			+ username
			+ Separators.SP
			+ sessId
			+ Separators.SP
			+ sessVersion
			+ Separators.SP
			+ nettype
			+ Separators.SP
			+ addrtype
			+ Separators.SP
			+ address.encode()
			+ Separators.NEWLINE;
	}

	public Object clone() {
		OriginField retval = (OriginField) super.clone();
		if (this.address != null)
			retval.address = (Host) this.address.clone();
		return retval;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:27  sverker
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
