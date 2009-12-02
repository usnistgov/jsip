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
 * File Name     : ProxyAuthenticateHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

/**
 * A Proxy-Authenticate header field value contains an authentication
 * challenge. When a UAC sends a request to a proxy server, the proxy server
 * MAY authenticate the originator before the request is processed. If no
 * credentials (in the Proxy-Authorization header field) are provided in the
 * request, the proxy can challenge the originator to provide credentials by
 * rejecting the request with a 407 (Proxy Authentication Required) status
 * code.  The proxy MUST populate the 407 (Proxy Authentication Required)
 * message with a Proxy-Authenticate header field value applicable to the
 * proxy for the requested resource.  The field value consists of a challenge
 * that indicates the authentication and parameters applicable to the proxy
 * for this RequestURI.
 * <p>
 * Note - Unlike its usage within HTTP, the ProxyAuthenticateHeader must be
 * passed upstream in the Response to the UAC. In SIP, only UAC's can
 * authenticate themselves to proxies.
 * <p>
 * Proxies MUST NOT add values to the Proxy-Authorization header field. All
 * 407 (Proxy Authentication Required) responses MUST be forwarded upstream
 * toward the UAC following the procedures for any other response. It is the
 * UAC's responsibility to add the Proxy-Authorization header field value
 * containing credentials for the realm of the proxy that has asked for
 * authentication.
 * <p>
 * When the originating UAC receives the 407 (Proxy Authentication Required)
 * it SHOULD, if it is able, re-originate the request with the proper
 * credentials. It should follow the same procedures for the display of the
 * "realm" parameter that are given above for responding to 401. If no
 * credentials for a realm can be located, UACs MAY attempt to retry the
 * request with a username of "anonymous" and no password (a password of "").
 * The UAC SHOULD also cache the credentials used in the re-originated request.
 * <p>
 * For Example:<br>
 * <code>Proxy-Authenticate: Digest realm="jcp.org",
 * domain="sip:ss1.duke.com", qop="auth",
 * nonce="f84f1cec41e6cbe5aea9c8e88d359", opaque="", stale=FALSE,
 * algorithm=MD5</code>
 *
 * @see Parameters
 * @see ProxyAuthorizationHeader
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface ProxyAuthenticateHeader extends WWWAuthenticateHeader {


    /**
     * Name of ProxyAuthenticateHeader
     */
    public final static String NAME = "Proxy-Authenticate";

}

