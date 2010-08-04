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
 * File Name     : AuthenticationInfoHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

/**
 * The Authentication-Info header field provides for mutual
 * authentication with HTTP Digest. A UAS MAY include this header field
 * in a 2xx response to a request that was successfully authenticated
 * using digest based on the Authorization header field.
 * <p>
 * For Example:<br>
 * <code>Authentication-Info: nextnonce="47364c23432d2e131a5fb210812c"</code>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface AuthenticationInfoHeader extends Parameters, Header {

    /**
     * Sets the NextNonce of the AuthenticationInfoHeader to the <var>nextNonce</var>
     * parameter value. 
     *
     * @param nextNonce - the new nextNonce String of this AuthenticationInfoHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the nextNonce value.
     */
    public void setNextNonce(String nextNonce) throws ParseException;

    /**
     * Returns the nextNonce value of this AuthenticationInfoHeader. 
     *
     * @return the String representing the nextNonce information, null if value is
     * not set.
     */
    public String getNextNonce();   
    
    /**
     * Sets the Qop value of the AuthenticationInfoHeader to the new 
     * <var>qop</var> parameter value.
     *
     * @param qop - the new Qop string of this AuthenticationInfoHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the Qop value.
     */
    public void setQop(String qop) throws ParseException;

    /**
     * Returns the messageQop value of this AuthenticationInfoHeader.
     *
     * @return the string representing the messageQop information, null if the 
     * value is not set.
     */
    public String getQop();        

    /**
     * Sets the CNonce of the AuthenticationInfoHeader to the <var>cNonce</var>
     * parameter value. 
     *
     * @param cNonce - the new cNonce String of this AuthenticationInfoHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the cNonce value.
     */
    public void setCNonce(String cNonce) throws ParseException;

    /**
     * Returns the CNonce value of this AuthenticationInfoHeader. 
     *
     * @return the String representing the cNonce information, null if value is
     * not set.
     */
    public String getCNonce();  
    
    /**
     * Sets the Nonce Count of the AuthenticationInfoHeader to the <var>nonceCount</var>
     * parameter value. 
     *
     * @param nonceCount - the new nonceCount integer of this AuthenticationInfoHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the nonceCount value.
     */
    public void setNonceCount(int nonceCount) throws ParseException;

    /**
     * Returns the Nonce Count value of this AuthenticationInfoHeader. 
     *
     * @return the integer representing the nonceCount information, -1 if value is
     * not set.
     */
    public int getNonceCount();    
    
    /**
     * Sets the Response of the AuthenticationInfoHeader to the new <var>response</var> 
     * parameter value.
     *
     * @param response - the new response String of this AuthenticationInfoHeader.
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the Response.
     */
    public void setResponse(String response) throws ParseException;

    /**
     * Returns the Response value of this AuthenticationInfoHeader.
     *
     * @return the String representing the Response information.
     */
    public String getResponse();    
    
    /**
     * Name of the AlertInfoHeader
     */
    public final static String NAME = "Authentication-Info";

}

