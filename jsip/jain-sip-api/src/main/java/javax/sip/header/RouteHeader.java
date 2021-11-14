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
 * File Name     : RouteHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

/**
 * The Route header field is used to force routing for a request through the
 * listed set of proxies. Each host removes the first entry and then proxies
 * the Request to the host listed in that entry using it as the RequestURI.
 * <p>
 * Explicit Route assignment (if needed) for the initial dialog establishment 
 * is the applications responsibility, but once established Routes are 
 * maintained by the dialog layer and should not be manupulated by the 
 * application. For example the SipProvider queries the dialog for Route 
 * assignment and adds these to the outgoing message as needed. The 
 * {@link javax.sip.address.Router} may be used by the application to determine 
 * the initial Route of the message.
 *
 * @see RecordRouteHeader
 * @see HeaderAddress
 * @see Parameters
 *
 * @author BEA Systems, NIST
 * @version 1.2
 *
 */
public interface RouteHeader extends HeaderAddress, Parameters, Header {

    /**
     * Name of RouteHeader
     */
    public final static String NAME = "Route";
}

