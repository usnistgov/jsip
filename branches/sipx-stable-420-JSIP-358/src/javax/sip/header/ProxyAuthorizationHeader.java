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
 * File Name     : ProxyAuthorizationHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



/**

 * The Proxy-Authorization header field allows the client to identify

 * itself (or its user) to a proxy that requires authentication.  A

 * Proxy-Authorization field value consists of credentials containing

 * the authentication information of the user agent for the proxy and/or

 * realm of the resource being requested.

 * <p>

 * This header field, along with Authorization, breaks the general rules

 * about multiple header field names.  Although not a comma-separated

 * list, this header field name may be present multiple times, and MUST

 * NOT be combined into a single header line.

 * <p>

 * An UAC sends a request to a proxy server containing a Proxy-Authorization

 * header field, so that the proxy can authenticate the UAC before processing

 * the request. A proxy can challenge for credentials by rejecting a request

 * with a 407 (Proxy Authentication Required) status code upon which a UAC may

 * provide credentials for the requested resource in the Proxy-Authorization

 * header.

 * <p>

 * A Proxy-Authorization header field value applies only to the proxy

 * whose realm is identified in the "realm" parameter. When multiple proxies

 * are used in a chain, a Proxy-Authorization header field value MUST NOT be

 * consumed by any proxy whose realm does not match the "realm" parameter

 * specified in that value. Note that if an authentication scheme that does not

 * support realms is used in the Proxy-Authorization header field, a proxy

 * server MUST attempt to parse all Proxy-Authorization header field values to

 * determine whether one of them has what the proxy server considers to be

 * valid credentials.

 * <p>

 * Example:<br>

 * Proxy-Authorization: Digest username="Alice", realm="atlanta.com",

 * nonce="c60f3082ee1212b402a21831ae", response="245f23415f11432b3434341c022"

 *

 * @see Parameters

 * @see ProxyAuthenticateHeader
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface ProxyAuthorizationHeader extends AuthorizationHeader {



    /**

     * Name of ProxyAuthorizationHeader

     */

    public final static String NAME = "Proxy-Authorization";

}

