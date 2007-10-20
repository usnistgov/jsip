/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. 
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : ViaHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;


import java.text.ParseException;
import javax.sip.InvalidArgumentException;

/**
 * The Via header field indicates the transport used for the transaction and
 * identifies the location where the response is to be sent. A Via header
 * field value is added only after the transport that will be used to reach
 * the next hop has been selected. When the UAC creates a request, it MUST
 * insert a Via into that request. The protocol name and protocol version in
 * the header field MUST be SIP and 2.0, respectively. The Via header field
 * value MUST contain a branch parameter. This parameter is used to identify
 * the transaction created by that request. This parameter is used by both the
 * client and the server.
 * <p>
 * <b>Branch Parameter:<br></b>
 * The branch parameter value MUST be unique across space and time for all
 * requests sent by the User Agent. The exceptions to this rule are CANCEL and ACK for
 * non-2xx responses. A CANCEL request will have the same value of the branch
 * parameter as the request it cancels. An ACK for a non-2xx response will also
 * have the same branch ID as the INVITE whose response it acknowledges.
 * <p>
 * The uniqueness property of the branch ID parameter, to facilitate its use as
 * a transaction ID, was not part of RFC 2543. The branch ID inserted by an
 * element compliant with this specification MUST always begin with the
 * characters "z9hG4bK". These 7 characters are used as a magic cookie (7 is
 * deemed sufficient to ensure that an older RFC 2543 implementation would not
 * pick such a value), so that servers receiving the request can determine that
 * the branch ID was constructed in the fashion described by this specification
 * (that is, globally unique). Beyond this requirement, the precise format of
 * the branch token is implementation-defined. JSIP defines a convenience 
 * function to generate unique branch identifiers at 
 * {@link javax.sip.Transaction#getBranchId()}
 * <p>
 * A common way to create the branch value is to compute a cryptographic hash
 * of the To tag, From tag, Call-ID header field, the Request-URI of the
 * request received (before translation), the topmost Via header, and the
 * sequence number from the CSeq header field, in addition to any Proxy-Require
 * and Proxy-Authorization header fields that may be present. The algorithm
 * used to compute the hash is implementation-dependent.
 * <p>
 * <b>Via Processing Rules</b>
 * <ul>
 * <li>Generating Requests (UAC): The client originating the Request must insert
 * into the Request a ViaHeader containing its host name or network address
 * and, if not the default port number, the port number at which it wishes to
 * receive Responses. (Note that this port number can differ from the UDP
 * source port number of the Request.) A fully-qualified domain name is
 * recommended.
 * <li>Request Forwarding by Proxies: The proxy MUST insert a Via header field
 * value into the copy before the existing Via header field values. This
 * implies that the proxy will compute its own branch parameter, which will be
 * globally unique for that branch, and contain the requisite magic cookie. Note
 * that this implies that the branch parameter will be different for different
 * instances of a spiraled or looped request through a proxy. If a proxy server
 * receives a Request which contains its own address in a ViaHeader, it must
 * respond with a 482 (Loop Detected) Response. A proxy server must not forward
 * a Request to a multicast group which already appears in any of the ViaHeaders.
 * This prevents a malfunctioning proxy server from causing loops. Also, it
 * cannot be guaranteed that a proxy server can always detect that the address
 * returned by a location service refers to a host listed in the ViaHeader list,
 * as a single host may have aliases or several network interfaces.
 * <li>Response processing by UAC and proxies:
 * <ul>
 * <li>The first ViaHeader should indicate the proxy or client processing this
 * Response. If it does not, discard the Response. Otherwise, remove this
 * ViaHeader.
 * <li>If there is no second ViaHeader, this Response is destined for this
 * client. Otherwise, the processing depends on whether the ViaHeader contains
 * a maddr parameter or is a receiver-tagged field.
 * <li>If the second ViaHeader contains a maddr parameter, send the Response to
 * the multicast address listed there, using the port indicated in "sent-by",
 * or port 5060 if none is present. The Response should be sent using the TTL
 * indicated in the ttl parameter, or with a TTL of 1 if that parameter is not
 * present. For robustness, Responses must be sent to the address indicated in
 * the maddr parameter even if it is not a multicast address.
 * <li>If the second ViaHeader does not contain a maddr parameter and is a
 * receiver-tagged ViaHeader, send the Response to the address in the received
 * parameter, using the port indicated in the port value, or using port 5060
 * if none is present.
 * <li>If neither of the previous cases apply, send the Response to the address
 * indicated by the host value in the second ViaHeader.
 * </ul>
 * <li>Sending Responses (UAS):
 * <ul>
 * <li>If the first ViaHeader in the Request contains a maddr parameter, send
 * the Response to the multicast address listed there, using the port indicated,
 * or port 5060 if none is present. The Response should be sent using the TTL
 * indicated in the ttl parameter, or with a TTL of 1 if that parameter is not
 * present. For robustness, Responses must be sent to the address indicated in
 * the maddr parameter even if it is not a multicast address.
 * <li>If the address in the first ViaHeader differs from the source address of
 * the packet, send the Response to the actual packet source address, similar
 * to the treatment for receiver-tagged ViaHeaders.
 * <li>If neither of these conditions is true, send the Response to the address
 * contained in the host value. If the Request was sent using TCP, use the
 * existing TCP connection if available.
 * </ul>
 * </ul>
 * <b>Via Parameters:</b>
 * A Via header field value contains the transport protocol used to send the
 * message, the client's host name or network address, and possibly the port
 * number at which it wishes to receive responses. Transport protocols defined
 * here are "UDP", "TCP", "TLS", and "SCTP". "TLS" means TLS over TCP. When a
 * request is sent to a SIPS URI, the protocol still indicates "SIP", and the
 * transport protocol is TLS. A Via header field value can also contain
 * parameters such as "maddr", "ttl", "received", and "branch". A proxy sending
 * a Request to a multicast address must add the maddr parameter to its
 * ViaHeader, and should add the ttl parameter. If a server receives a Request
 * which contained an maddr parameter in the topmost ViaHeader, it should send
 * the Response to the multicast address listed in the maddr parameter. The
 * received parameter is added only for receiver-added ViaHeaders.
 * <p>
 * Two Via header fields are equal if their sent-protocol and sent-by fields
 * (including port) are equal, both have the same set of parameters, and the 
 * values of all parameters are equal.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface ViaHeader extends Parameters, Header {

    /**
     * Set the host part of this ViaHeader to the newly supplied <code>host</code> 
     * parameter.
     *
     * @param host - the new value of the host of this ViaHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the host value.
     */  
    public void setHost(String host) throws ParseException;

     /**
     * Returns the host part of this ViaHeader.
     *
     * @return  the string value of the host
     */     
    public String getHost();
    
    /**
     * Set the port part of this ViaHeader to the newly supplied <code>port</code> 
     * parameter.
     *
     * @param port - the new integer value of the port of this ViaHeader
     * @throws InvalidArgumentException when the port value is not -1 and <1 or >65535
     */
    public void setPort(int port) throws InvalidArgumentException;     

    /**
     * Returns the port part of this ViaHeader.
     *
     * @return the integer value of the port, -1 if not present
     */    
    public int getPort();  

  
    /**
     * Returns the value of the transport parameter. 
     *
     * @return the string value of the transport paramter of the ViaHeader
     */
    public String getTransport();

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
    public void setTransport(String transport) throws ParseException;

    /**
     * Returns the value of the protocol used. 
     *
     * @return the string value of the protocol paramter of the ViaHeader
     */
    public String getProtocol();

    /**
     * Sets the value of the protocol parameter. This parameter specifies
     * which protocol is used, for example "SIP/2.0".
     *
     * @param protocol - new value for the protocol parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the protocol value.
     */
    public void setProtocol(String protocol) throws ParseException;    
    
    /**
     * Returns the value of the ttl parameter, or -1 if this is not set.
     *
     * @return the integer value of the <code>ttl</code> parameter
     */
    public int getTTL();

    /**
     * Sets the value of the ttl parameter. The ttl parameter specifies the 
     * time-to-live value when packets are sent using UDP multicast. 
     *
     * @param ttl - new value of the ttl parameter
     * @throws InvalidArgumentException if supplied value is less than zero or 
     * greater than 255, excluding -1 the default not set value.
     */
    public void setTTL(int ttl) throws InvalidArgumentException;    

    /**
     * Returns the value of the <code>maddr</code> parameter, or null if this
     * is not set.
     *
     * @return the string value of the maddr parameter
     */
    public String getMAddr();

    /**
     * Sets the value of the <code>maddr</code> parameter of this ViaHeader. The
     * maddr parameter indicates the server address to be contacted for this
     * user, overriding any address derived from the host field. 
     *
     * @param  mAddr new value of the <code>mAddr</code> parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the mAddr value.
     */
    public void setMAddr(String mAddr) throws ParseException;

    /**
     * Gets the received paramater of the ViaHeader. Returns null if received
     * does not exist.
     *
     * @return the string received value of ViaHeader
     */
    public String getReceived();

    /**
     * Sets the received parameter of ViaHeader.
     *
     * @param received - the newly supplied received parameter.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the received value.
     */
    public void setReceived(String received) throws ParseException;

    /**
     * Gets the branch paramater of the ViaHeader. Returns null if branch
     * does not exist.
     *
     * @return the string branch value of ViaHeader
     */
    public String getBranch();

    /**
     * Sets the branch parameter of the ViaHeader to the newly supplied
     * branch value. Note that when sending a Request within a transaction, 
     * branch id management will be the responsibility of the SipProvider; 
     * that is the application should not set this value. This method should 
     * only be used by the application when sending Requests outside of a 
     * transaction.
     *
     * @param branch - the new string branch parmameter of the ViaHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the branch value.
     */
    public void setBranch(String branch) throws ParseException;

    /**
     * Set the rport part of this ViaHeader. This method indicates to the 
     * remote party that you want it to use rport. It is the applications 
     * responsisbility to call this method to inform the implementation to set 
     * the value of the rport. This allows a client 
     * to request that the server send the response back to the source IP 
     * address and port from which the request originated. 
     * See <a href = "http://www.ietf.org/rfc/rfc3581.txt">RFC3581</a>
     *
     *
     * @throws InvalidArgumentException if rport value is an illegal integer ( <=0 ).
     * @since v1.2
     */
    public void setRPort() throws InvalidArgumentException;     

    /**
     * Returns the rport part of this ViaHeader.
     *
     * @return the integer value of the rport or -1 if the rport parameter 
     * is not set.
     * @since v1.2
     */    
    public int getRPort();     
    
    /**
     * Compare this ViaHeader for equality with another. This method 
     * overrides the equals method in javax.sip.Header. This method specifies 
     * object equality as outlined by  
     * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>. 
     * Two Via header fields are equal if their sent-protocol and sent-by fields 
     * are equal, both have the same set of parameters, and the values of all 
     * parameters are equal. When comparing header fields, field names are always 
     * case-insensitive. Unless otherwise stated in the definition of a 
     * particular header field, field values, parameter names, and parameter 
     * values are case-insensitive. Tokens are always case-insensitive. Unless 
     * specified otherwise, values expressed as quoted strings are case-sensitive.
     *
     * @param obj the object to compare this ViaHeader with.
     * @return <code>true</code> if <code>obj</code> is an instance of this class
     * representing the same ViaHeader as this, <code>false</code> otherwise.
     * @since v1.2
     */
    public boolean equals(Object obj);   
    
    
    /**
     * Name of ViaHeader
     */
    public final static String NAME = "Via";

}

