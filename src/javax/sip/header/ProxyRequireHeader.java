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
 * File Name     : ProxyRequireHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;



/**

 * The Proxy-Require header field is used to indicate proxy-sensitive features

 * that must be supported by the proxy. The Proxy-Require header field contains

 * a list of option tags. Each option tag defines a SIP extension that MUST be

 * understood by the proxy to process the request. Any ProxyRequireHeader

 * features that are not supported by the proxy must be negatively acknowledged

 * by the proxy to the client if not supported. Proxy servers treat this field

 * identically to the RequireHeader.

 * <p>

 * For Example:<br>

 * <code>Proxy-Require: foo</code>

 *

 * @see RequireHeader
 * @author BEA Systems, NIST
 * @version 1.2

 */



public interface ProxyRequireHeader extends RequireHeader {



    /**

     * Name of ProxyRequireHeader

     */

    public final static String NAME = "Proxy-Require";

}

