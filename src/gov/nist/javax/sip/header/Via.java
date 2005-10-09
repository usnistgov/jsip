/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Via SIPHeader (these are strung together in a ViaList).
 *
 * @see ViaList
 *
 * @version JAIN-SIP-1.1 $Revision: 1.7 $ $Date: 2005-10-09 19:44:19 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Via
	extends ParametersHeader
	implements javax.sip.header.ViaHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 5281728373401351378L;

	/** The branch parameter is included by every forking proxy.
	*/
	public static final String BRANCH = ParameterNames.BRANCH;

	/** The "received" parameter is added only for receiver-added Via Fields.
	 */
	public static final String RECEIVED = ParameterNames.RECEIVED;

	/** The "maddr" paramter is designating the multicast address.
	 */
	public static final String MADDR = ParameterNames.MADDR;

	/** The "TTL" parameter is designating the time-to-live value.
	 */
	public static final String TTL = ParameterNames.TTL;

	/** The RPORT parameter.
	*/
	public static final String RPORT = ParameterNames.RPORT;

	/** sentProtocol field.
	 */
	protected Protocol sentProtocol;

	/** sentBy field.
	 */
	protected HostPort sentBy;

	/** comment field
	 */
	protected String comment;

	/** Default constructor
	*/
	public Via() {
		super(NAME);
		sentProtocol = new Protocol();
	}

	/**
	 * JvB: equals() should look something like this
	 *
	public boolean equals(Object other) {
		
		if (other==this) return true;
		
		if (other instanceof ViaHeader) {
			final ViaHeader o = (ViaHeader) other;
			return getProtocol().equalsIgnoreCase( o.getProtocol() )
				&& getTransport().equalsIgnoreCase( o.getTransport() )
				&& getHost().equalsIgnoreCase( o.getHost() )
				&& getPort() == o.getPort()
				&& equalParameters( o );
		} 
		return false;
	}
	*/
	
	
	/** get the Protocol Version
	 * @return String
	 */
	public String getProtocolVersion() {
		if (sentProtocol == null)
			return null;
		else
			return sentProtocol.getProtocolVersion();
	}

	/**
	 * Accessor for the sentProtocol field.
	 * @return Protocol field
	 */
	public Protocol getSentProtocol() {

		return sentProtocol;
	}

	/**
	 * Accessor for the sentBy field
	 *@return SentBy field
	 */
	public HostPort getSentBy() {
		return sentBy;
	}

	/**
	 * Accessor for the parameters field
	 * @return parameters field
	 */
	public NameValueList getViaParms() {
		return parameters;
	}

	/**
	 * Accessor for the comment field.
	 * @return comment field.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 *  Get the maddr parameter if it exists.
	 * @return maddr parameter.
	 */
	public Host getMaddr() {
		return (Host) parameters.getValue(ParameterNames.MADDR);
	}

	/** port of the Via Header.
	 * @return true if Port exists.
	 */
	public boolean hasPort() {
		return (getSentBy()).hasPort();
	}

	/** comment of the Via Header.
	 * 
	 * @return false if comment does not exist and true otherwise.
	 */
	public boolean hasComment() {
		return comment != null;
	}

	/** remove the port.
	 */
	public void removePort() {
		sentBy.removePort();
	}

	/** remove the comment field.
	 */
	public void removeComment() {
		comment = null;
	}

	/** set the Protocol Version
	 * @param protocolVersion String to set
	 */
	public void setProtocolVersion(String protocolVersion) {
		if (sentProtocol == null)
			sentProtocol = new Protocol();
		sentProtocol.setProtocolVersion(protocolVersion);
	}

	/** set the Host of the Via Header
	     * @param host String to set
	     */
	public void setHost(Host host) {
		if (sentBy == null) {
			sentBy = new HostPort();
		}
		sentBy.setHost(host);
	}

	/**
	 * Set the sentProtocol member  
	 * @param s Protocol to set.
	 */
	public void setSentProtocol(Protocol s) {
		sentProtocol = s;
	}

	/**
	 * Set the sentBy member  
	 * @param s HostPort to set.
	 */
	public void setSentBy(HostPort s) {
		sentBy = s;
	}

	/**
	 * Set the comment member  
	 * @param c String to set.
	 */
	public void setComment(String c) {
		comment = c;
	}

	/** Encode the body of this header (the stuff that follows headerName).
	 * A.K.A headerValue.
	 */
	protected String encodeBody() {
		StringBuffer encoding = new StringBuffer();
		encoding.append(sentProtocol.encode()).append(SP).append(
			sentBy.encode());	
		if (!parameters.isEmpty()) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}
		if (comment != null) {
			encoding.append(SP).append(LPAREN).append(comment).append(RPAREN);
		}
		return encoding.toString();
	}

	/**
	 * Set the host part of this ViaHeader to the newly supplied <code>host</code> 
	 * parameter.
	 *
	 * @return host - the new interger value of the host of this ViaHeader
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the host value.
	 */
	public void setHost(String host) throws ParseException {
		if (sentBy == null)
			sentBy = new HostPort();
		try {
			Host h = new Host(host);
			sentBy.setHost(h);
		} catch (Exception e) {
			throw new NullPointerException(" host parameter is null");
		}
	}

	/**
	* Returns the host part of this ViaHeader.
	*
	* @return  the string value of the host
	*/
	public String getHost() {
		if (sentBy == null)
			return null;
		else {
			Host host = sentBy.getHost();
			if (host == null)
				return null;
			else
				return host.getHostname();
		}
	}

	/**
	 * Set the port part of this ViaHeader to the newly supplied <code>port</code> 
	 * parameter.
	 *
	 * @param port - the new integer value of the port of this ViaHeader
	 */
	public void setPort( int port ) /*throws InvalidArgumentException*/ {

		/*
		 * InvalidArgumentException not in 1.1 API
		 * 
		if ( port!=-1 && (port<1 || port>65535)) {
			throw new InvalidArgumentException( "Port value out of range -1, [1..65535]" );
		}
		*/
				
		if (sentBy == null)
			sentBy = new HostPort();
		sentBy.setPort(port);
	}
	
	/**
	 * Set the RPort parameter.
	 * 
	 * @param rport -- rport parameter to set.
	 */
	public void setRPort(int rport) throws InvalidArgumentException {
	    if ( rport <= 0 ) throw new InvalidArgumentException ("Bad RPort value");
	    this.setParameter(Via.RPORT,rport);
	}

	/**
	 * Returns the port part of this ViaHeader.
	 *
	 * @return the integer value of the port
	 */
	public int getPort() {
		if (sentBy == null)
			return -1;
		return sentBy.getPort();
	}


	/**
	* Return the rport parameter.
	*
	*@return the rport parameter or -1.
	*/
       public int getRPort() {
         String strRport = getParameter(ParameterNames.RPORT);
         if (strRport != null)
            return new Integer(strRport).intValue();
         else
            return -1;
     	}
    

	/**
	 * Returns the value of the transport parameter. 
	 *
	 * @return the string value of the transport paramter of the ViaHeader
	 */
	public String getTransport() {
		if (sentProtocol == null)
			return null;
		return sentProtocol.getTransport();
	}

	/**
	 * Sets the value of the transport. This parameter specifies
	 * which transport protocol to use for sending requests and responses to
	 * this entity. The following values are defined: "udp", "tcp", "sctp",
	 * "tls", but other values may be used also. 
	 *
	 * @param transport - new value for the transport parameter
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the transport value.
	 */
	public void setTransport(String transport) throws ParseException {
		if (transport == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "Via, setTransport(), the transport parameter is null.");
		if (sentProtocol == null)
			sentProtocol = new Protocol();
		sentProtocol.setTransport(transport);
	}

	/**
	 * Returns the value of the protocol used. 
	 *
	 * @return the string value of the protocol paramter of the ViaHeader
	 */
	public String getProtocol() {
		if (sentProtocol == null)
			return null;
		return sentProtocol.getProtocol();	// JvB: Return name ~and~ version
	}

	/**
	 * Sets the value of the protocol parameter. This parameter specifies
	 * which protocol is used, for example "SIP/2.0".
	 *
	 * @param protocol - new value for the protocol parameter
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the protocol value.
	 */
	public void setProtocol(String protocol) throws ParseException {
		if (protocol == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "Via, setProtocol(), the protocol parameter is null.");
		
		if (sentProtocol == null)
			sentProtocol = new Protocol();
		
		sentProtocol.setProtocol(protocol);
	}

	/**
	 * Returns the value of the ttl parameter, or -1 if this is not set.
	 *
	 * @return the integer value of the <code>ttl</code> parameter
	 */
	public int getTTL() {
		int ttl = getParameterAsInt(ParameterNames.TTL);
		return ttl;
	}

	/**
	 * Sets the value of the ttl parameter. The ttl parameter specifies the 
	 * time-to-live value when packets are sent using UDP multicast. 
	 *
	 * @param ttl - new value of the ttl parameter
	 * @throws InvalidArgumentException if supplied value is less than zero or 
	 * greater than 255, excluding -1 the default not set value.
	 */
	public void setTTL(int ttl) throws InvalidArgumentException {
		if (ttl < 0 && ttl != -1)
			throw new InvalidArgumentException(
				"JAIN-SIP Exception"
					+ ", Via, setTTL(), the ttl parameter is < 0");
		setParameter(new NameValue(ParameterNames.TTL, new Integer(ttl)));
	}

	/**
	 * Returns the value of the <code>maddr</code> parameter, or null if this
	 * is not set.
	 *
	 * @return the string value of the maddr parameter
	 */
	public String getMAddr() {
		return getParameter(ParameterNames.MADDR);
	}

	/**
	 * Sets the value of the <code>maddr</code> parameter of this ViaHeader. The
	 * maddr parameter indicates the server address to be contacted for this
	 * user, overriding any address derived from the host field. 
	 *
	 * @param  mAddr new value of the <code>maddr</code> parameter
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the mAddr value.
	 */
	public void setMAddr(String mAddr) throws ParseException {
		if (mAddr == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "Via, setMAddr(), the mAddr parameter is null.");

		Host host = new Host();
		host.setAddress(mAddr);
		NameValue nameValue = new NameValue(ParameterNames.MADDR, host);
		setParameter(nameValue);

	}

	/**
	 * Gets the received paramater of the ViaHeader. Returns null if received
	 * does not exist.
	 *
	 * @return the string received value of ViaHeader
	 */
	public String getReceived() {
		return getParameter(ParameterNames.RECEIVED);
	}

	/**
	 * Sets the received parameter of ViaHeader.
	 *
	 * @param received - the newly supplied received parameter.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the received value.
	 */
	public void setReceived(String received) throws ParseException {
		if (received == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "Via, setReceived(), the received parameter is null.");

		setParameter(ParameterNames.RECEIVED, received);

	}

	/**
	 * Gets the branch paramater of the ViaHeader. Returns null if branch
	 * does not exist.
	 *
	 * @return the string branch value of ViaHeader
	 */
	public String getBranch() {
		return getParameter(ParameterNames.BRANCH);
	}

	/**
	 * Sets the branch parameter of the ViaHeader to the newly supplied
	 * branch value.
	 *
	 * @param branch - the new string branch parmameter of the ViaHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the branch value.
	 */
	public void setBranch(String branch) throws ParseException {
		if (branch == null)
			throw new NullPointerException(
				"JAIN-SIP Exception, "
					+ "Via, setBranch(), the branch parameter is null.");

		setParameter(ParameterNames.BRANCH, branch);
	}

	public Object clone() {
		Via retval = (Via) super.clone();
		if (this.sentProtocol != null)
			retval.sentProtocol = (Protocol) this.sentProtocol.clone();
		if (this.sentBy != null)
			retval.sentBy = (HostPort) this.sentBy.clone();
		return retval;
	}

    
   
}
