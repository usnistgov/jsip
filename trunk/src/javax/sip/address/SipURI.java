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
 * File Name     : SipURI.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;

import java.util.Iterator;
import javax.sip.header.Parameters;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;


/**
 * This class represents SIP URIs, that may have either a <code>sip:</code> or
 * <code>sips:</code> scheme. All SIP implementations MUST support the sip:
 * URI scheme. 
 * <p>
 * SIP and SIPS URIs are used for addressing. They are similar to email 
 * addresses in that they are of the form
 * <code>user@host</code> where user is either a user name or telephone number,
 * and host is a host or domain name, or a numeric IP address. Additionally,
 * SIP and SIPS URIs may contain parameters and headers (although headers are
 * not legal in all contexts). A SipURI can be embedded in web pages, business
 * cards or other hyperlinks to indicate that a particular user or service can
 * be called via SIP.
 * <p>
 * Within a SIP Message, SipURIs are used to indicate the source and intended
 * destination of a Request, redirection addresses and the current destination
 * of a Request. Normally all these Headers will contain SipURIs.
 * <p>
 * Syntactically, SIP and SIPS URIs are identical except for the name of the URI
 * scheme. The semantics differs in that the SIPS scheme implies that the
 * identified resource is to be contacted using TLS. Because SIP and SIPS URIs
 * are syntactically identical and because they're used the same way, they're
 * both represented by the SipURI interface.
 * <p>
 * The SipURI interface extends the generic URI interface and provides
 * additional convenience methods for the following components of a SipURI
 * address, above the generic URI interface:
 * <ul>
 * <li>User - The set of valid telephone-subscriber strings is a subset of
 * valid user strings.  The user URI parameter exists to distinguish telephone
 * numbers from user names that happen to look like telephone numbers.  If the
 * user string contains a telephone number formatted as a telephone-subscriber,
 * the user parameter value "phone" SHOULD be present.  Even without this
 * parameter, recipients of SIP and SIPS URIs MAY interpret the pre-@ part as a
 * telephone number if local restrictions on the name space for user name allow it.
 * <li>UserPassword - A password associated with the user.  While the SIP and
 * SIPS URI syntax allows this field to be present, its use is NOT RECOMMENDED,
 * because the passing of authentication information in clear text (such as
 * URIs) has proven to be a security risk in almost every case where it has
 * been used.  For instance, transporting a PIN number in this field exposes
 * the PIN.
 * <li>URI parameters - Parameters affecting a request constructed from this
 * URI. URI parameters are added after the hostport component and are separated
 * by semi-colons. URI parameters take the form:<br>
 * parameter-name "=" parameter-value<br>
 * Even though an arbitrary number of URI parameters may be included in a URI,
 * any given parameter-name MUST NOT appear more than once. The SipURI
 * interface also provides convenience methods for the manipulation of popular
 * parameters in a SipURI address, namely:
 * <ul>
 * <li>Lr Parameter - the element responsible for this resource implements the
 * routing mechanisms specified in RFC 3261.
 * <li>Method - The method of the SIP request constructed from the URI.
 * <li>MAddr Parameter - the server address to be contacted for this user.
 * <li>TTL Parameter - the time-to-live value when packets are sent using UDP
 * multicast.
 * <li>User Parameter - the set of valid telephone-subscriber strings.
 * <li>Transport Parameter - specifies which transport protocol to use for
 * sending requests and responses to this entity
 * </ul>
 * <li>URI Headers - Header fields to be included in a request constructed from
 * the URI. Headers fields in the SIP request can be specified with the "?"
 * mechanism within a URI.  The header names and values are encoded in
 * ampersand separated 'hname = hvalue' pairs.  The special hname "body"
 * indicates that the associated hvalue is the message-body of the SIP request.
 * <li>Secure - This determines if the scheme of this URI is either
 * <code>sip:</code> or <code>sips:</code>, where <code>sips:</code> is secure.
 * </ul>
 * See section 19.1.2 of <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>
 * for the use of SIP and SIPS URI components based on the context in which the
 * URI appears.
 *
 * @see javax.sip.header.FromHeader
 * @see javax.sip.header.ToHeader
 * @see javax.sip.header.ContactHeader
 * @see URI
 *
 * @author BEA Systems, NIST 
 * @version 1.2
 *
 */

public interface SipURI extends URI, Parameters {

    /**
     * Sets the user of SipURI. The identifier of a particular resource at
     * the host being addressed. The user and the user password including the 
     * '@' sign make up the user-info.
     *
     * @param user - the new String value of the user.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the user value.
     */
    public void setUser(String user) throws ParseException;

    /**
     * Returns the user part of this SipURI.
     *
     * @return  the user part of this SipURI, this value may be null.
     */
    public String getUser();

    /**
     * Sets the user password associated with the user of SipURI. While the SIP and
     * SIPS URI syntax allows this field to be present, its use is NOT
     * RECOMMENDED, because the passing of authentication information in clear
     * text (such as URIs) has proven to be a security risk in almost every
     * case where it has been used. The user password and the user including
     * the @ sign make up the user-info.
     *
     * @param userPassword - the new String value of the user password
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the userPassword value.
     */
    public void setUserPassword(String userPassword) throws ParseException;

    /**
     * Gets user password of SipURI, or null if it is not set.
     *
     * @return the user password of this SipURI
     */
    public String getUserPassword();

    /**
     * Returns true if this SipURI is secure i.e. if this SipURI represents a
     * sips URI. A sip URI returns false.
     *
     * @return  <code>true</code> if this SipURI represents a sips URI, and
     * <code>false</code> if it represents a sip URI.
     */
    public boolean isSecure();

    /**
     * Sets the scheme of this URI to sip or sips depending on whether the
     * argument is true or false. The default value is false.
     *
     * @param secure - the boolean value indicating if the SipURI is secure.
     */
    public void setSecure(boolean secure);

    /**
     * Set the host part of this SipURI to the newly supplied <code>host</code> 
     * parameter.
     *
     * @param host - the new interger value of the host of this SipURI
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the host value.
     */  
    public void setHost(String host) throws ParseException;

     /**
     * Returns the host part of this SipURI.
     *
     * @return  the host part of this SipURI
     */     
    public String getHost();
    
    /**
     * Set the port part of this SipURI to the newly supplied port 
     * parameter.
     *
     * @param port - the new interger value of the port of this SipURI
     */
    public void setPort(int port);     

    /**
     * Returns the port part of this SipURI.
     *
     * @return  the port part of this SipURI
     */    
    public int getPort();    

    /**
     * Removes the port part of this SipURI. If no port is specified the 
     * stack will assume the default port.
     *
     */    
    public void removePort();     

// header manipulation methods

    /**
     * Returns the value of the named header, or null if it is not set.
     * SIP/SIPS URIs may specify headers. As an example, the URI
     * sip:jimmy@jcp.org?priority=urgent has a header "priority" whose
     * value is "urgent".
     *
     * @param name name of header to retrieve
     * @return the value of specified header
     */
    public String getHeader(String name);

    /**
     * Sets the value of the specified header fields to be included in a
     * request constructed from the URI. If the header already had a value it
     * will be overwritten.
     *
     * @param name - a String specifying the header name
     * @param value - a String specifying the header value
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the name or value parameters.
     */
    public void setHeader(String name, String value) throws ParseException;


    /**
     * Returns an Iterator over the String names of all headers present
     * in this SipURI.
     *
     * @return an Iterator over all the header names
     */
    public Iterator getHeaderNames();

//Param Covenience methods


    /**
     * Returns the value of the "transport" parameter, or null if this is not
     * set. This is equivalent to getParameter("transport").
     *
     * @return the transport paramter of the SipURI
     */
    public String getTransportParam();

    /**
     * Sets the value of the "transport" parameter. This parameter specifies
     * which transport protocol to use for sending requests and responses to
     * this entity. The following values are defined: "udp", "tcp", "sctp",
     * "tls", but other values may be used also. This method is equivalent to
     * setParameter("transport", transport). Transport parameter constants
     * are defined in the {@link javax.sip.ListeningPoint}.
     *
     * @param transport - new value for the "transport" parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the transport value.
     */
    public void setTransportParam(String transport) throws ParseException;

    /**
     * Returns the value of the "ttl" parameter, or -1 if this is not set.
     * This method is equivalent to getParameter("ttl").
     *
     * @return the value of the <code>ttl</code> parameter
     */
    public int getTTLParam();

    /**
     * Sets the value of the <code>ttl</code> parameter. The ttl parameter
     * specifies the time-to-live value when packets are sent using UDP
     * multicast. This is equivalent to setParameter("ttl", ttl).
     *
     * @param ttl - new value of the <code>ttl</code> parameter
     * @throws InvalidArgumentException if supplied value is less than zero, 
     * excluding -1 the default not set value.
     */
    public void setTTLParam(int ttl) throws InvalidArgumentException;

    /**
     * Returns the value of the <code>method</code> parameter, or null if this
     * is not set. This is equivalent to getParameter("method").
     *
     * @return  the value of the <code>method</code> parameter
     */
    public String getMethodParam();

    /**
     * Sets the value of the <code>method</code> parameter. This specifies
     * which SIP method to use in requests directed at this URI. This is
     * equivalent to setParameter("method", method).
     *
     * @param  method - new value String value of the method parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     */
    public void setMethodParam(String method) throws ParseException;

    /**
     * Sets the value of the user parameter. The user URI parameter exists to
     * distinguish telephone numbers from user names that happen to look like
     * telephone numbers.  This is equivalent to setParameter("user", user).
     *
     * @param  userParam - new value String value of the method parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the userParam value.
     */
    public void setUserParam(String userParam) throws ParseException;

    /**
     * Returns the value of the <code>userParam</code>, or null if this is not
     * set.
     * <p>
     * This is equivalent to getParameter("user").
     *
     * @return the value of the <code>userParam</code> of the SipURI
     */
    public String getUserParam();


    /**
     * Returns the value of the <code>maddr</code> parameter, or null if this
     * is not set. This is equivalent to getParameter("maddr").
     *
     * @return the value of the <code>maddr</code> parameter
     */
    public String getMAddrParam();

    /**
     * Sets the value of the <code>maddr</code> parameter of this SipURI. The
     * maddr parameter indicates the server address to be contacted for this
     * user, overriding any address derived from the host field. This is
     * equivalent to setParameter("maddr", maddr).
     *
     * @param mAddr new value of the <code>maddr</code> parameter
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the mAddr value.
     */
    public void setMAddrParam(String mAddr) throws ParseException;

    /**
     * Returns whether the the <code>lr</code> parameter is set. This is 
     * equivalent to hasParameter("lr"). This interface has no getLrParam as 
     * RFC3261 does not specify any values for the "lr" paramater.
     *
     * @return true if the "lr" parameter is set, false otherwise.
     */
    public boolean hasLrParam();

    /**
     * Sets the value of the <code>lr</code> parameter of this SipURI. The lr
     * parameter, when present, indicates that the element responsible for
     * this resource implements the routing mechanisms specified in RFC 3261.
     * This parameter will be used in the URIs proxies place in the
     * Record-Route header field values, and may appear in the URIs in a
     * pre-existing route set.
     */
    public void setLrParam();

    
    
    /**
     * This method returns the URI as a string. 
     *
     * @return the stringified version of the URI
     */    
    public String toString();
    
}

