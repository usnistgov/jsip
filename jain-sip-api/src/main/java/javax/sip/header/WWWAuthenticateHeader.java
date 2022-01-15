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
 * File Name     : WWWAuthenticateHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *  1.2     13/06/2005  Phelim O'Doherty    Deprecated get/set URI parameter
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;
import javax.sip.address.URI;

/**
 * This interface represents the WWW-Authenticate response-header. A
 * WWW-Authenticate header field value contains an authentication challenge.
 * When a UAS receives a request from a UAC, the UAS MAY authenticate
 * the originator before the request is processed.  If no credentials
 * (in the Authorization header field) are provided in the request, the
 * UAS can challenge the originator to provide credentials by rejecting
 * the request with a 401 (Unauthorized) status code. The WWW-Authenticate
 * response-header field MUST be included in 401 (Unauthorized) response
 * messages.  The field value consists of at least one challenge that indicates
 * the authentication scheme(s) and parameters applicable to the realm.
 * <p>
 * For Example:<br>
 * <code>WWW-Authenticate: Digest realm="atlanta.com", domain="sip:boxesbybob.com",
 * qop="auth", nonce="f84f1cec41e6cbe5aea9c8e88d359", opaque="", stale=FALSE,
 * algorithm=MD5</code>
 *
 * @see Parameters
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface WWWAuthenticateHeader extends Parameters, Header {

    /**
     * Sets the scheme of the challenge information for this WWWAuthenticateHeader.
     * For example, Digest.
     *
     * @param scheme - the new string value that identifies the challenge 
     * information scheme.
     */
    public void setScheme(String scheme);


    /**
     * Returns the scheme of the challenge information for this WWWAuthenticateHeader.
     *
     * @return the string value of the challenge information.
     */
    public String getScheme();    
    
    /**
     * Sets the Realm of the WWWAuthenicateHeader to the realm
     * parameter value. Realm strings MUST be globally unique.  It is
     * RECOMMENDED that a realm string contain a hostname or domain name.
     * Realm strings SHOULD present a human-readable identifier that can be
     * rendered to a user.
     *
     * @param realm the new Realm String of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the realm.
     */
    public void setRealm(String realm) throws ParseException;

    /**
     * Returns the Realm value of this WWWAuthenicateHeader. This convenience
     * method returns only the realm of the complete Challenge.
     *
     * @return the String representing the Realm information, null if value is
     * not set.
     */
    public String getRealm();

    /**
     * Sets the Nonce of the WWWAuthenicateHeader to the nonce
     * parameter value. 
     *
     * @param nonce - the new nonce String of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the nonce value.
     */
    public void setNonce(String nonce) throws ParseException;

    /**
     * Returns the Nonce value of this WWWAuthenicateHeader. 
     *
     * @return the String representing the nonce information, null if value is
     * not set.
     */
    public String getNonce();    
    
    /**
     * Sets the URI of the WWWAuthenicateHeader to the URI parameter value.
     *
     * @param uri - the new URI of this WWWAuthenicateHeader.
     * @deprecated Since v1.2. URI is not a supported parameter for this header.
     */
    public void setURI(URI uri);

    /**
     * Returns the URI value of this WWWAuthenicateHeader, for example DigestURI. 
     *
     * @return the URI representing the URI information, null if value is
     * not set.
     * @deprecated Since v1.2. URI is not a supported parameter for this 
     * header. This method should return null.
     */
    public URI getURI();            
    
    /**
     * Sets the Algorithm of the WWWAuthenicateHeader to the new 
     * algorithm parameter value.
     *
     * @param algorithm - the new algorithm String of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the algorithm value.
     */
    public void setAlgorithm(String algorithm) throws ParseException;

    /**
     * Returns the Algorithm value of this WWWAuthenicateHeader.
     *
     * @return the String representing the Algorithm information, null if the 
     * value is not set.
     */
    public String getAlgorithm();        
    
    /**
     * Sets the Qop value of the WWWAuthenicateHeader to the new 
     * qop parameter value.
     *
     * @param qop - the new Qop string of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the Qop value.
     */
    public void setQop(String qop) throws ParseException;

    /**
     * Returns the Qop value of this WWWAuthenicateHeader.
     *
     * @return the string representing the Qop information, null if the 
     * value is not set.
     */
    public String getQop();        
    
    /**
     * Sets the Opaque value of the WWWAuthenicateHeader to the new 
     * opaque parameter value.
     *
     * @param opaque - the new Opaque string of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the opaque value.
     */
    public void setOpaque(String opaque) throws ParseException;

    /**
     * Returns the Opaque value of this WWWAuthenicateHeader.
     *
     * @return the String representing the Opaque information, null if the 
     * value is not set.
     */
    public String getOpaque();    

    /**
     * Sets the Domain of the WWWAuthenicateHeader to the domain
     * parameter value. 
     *
     * @param domain - the new Domain string of this WWWAuthenicateHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the domain.
     */
    public void setDomain(String domain) throws ParseException;


    /**
     * Returns the Domain value of this WWWAuthenicateHeader. 
     *
     * @return the String representing the Domain information, null if value is
     * not set.
     */
    public String getDomain();       
    
    /**
     * Sets the value of the stale parameter of the WWWAuthenicateHeader to the 
     * stale parameter value.
     *
     * @param stale - the new boolean value of the stale parameter.
     */
    public void setStale(boolean stale);

    /**
     * Returns the boolean value of the state paramater of this 
     * WWWAuthenicateHeader. 
     *
     * @return the boolean representing if the challenge is stale.
     */
    public boolean isStale();      
 
    

    /**
     * Name of WWWAuthenticateHeader
     */
    public final static String NAME = "WWW-Authenticate";
}

