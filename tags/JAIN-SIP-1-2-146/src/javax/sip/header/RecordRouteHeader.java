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
 * File Name     : RecordRouteHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;


/**
 * The Record-Route header field is inserted by proxies in a request to force
 * future requests in the dialog to be routed through the proxy. The 
 * SipProvider (as opposed to the application) should attach Record-Route 
 * headers to messages explicitly when forwarding them if necessary.
 * <p>
 * The RecordRouteHeader is added to a Request by any proxy that insists on being
 * in the path of subsequent Requests for the same call leg. It contains
 * a globally reachable RequestURI that identifies the proxy server. Each proxy
 * server adds its Address URI to the beginning of the list.
 * <p>
 * The calling user agent client copies the RecordRouteHeaders into
 * RouteHeaders of subsequent Requests within the same call leg, reversing the
 * order, so that the first entry is closest to the user agent client. If the
 * Response contained a ContactHeader field, the calling user agent adds its
 * content as the last RouteHeader. Unless this would cause a loop, a client
 * must send subsequent Requests for this call leg to the  Address URI in the
 * first RouteHeader and remove that entry.
 * <p>
 * Some proxies, such as those controlling firewalls or in an automatic call
 * distribution (ACD) system, need to maintain call state and thus need to
 * receive any BYE and ACK Requests for the call. 
 * <p>
 * For Example:<br>
 * <code>Record-Route: sip:server10.jcp.org;lr,
 * sip:bigbox3.duke.jcp.org;lr</code>
 *
 * @see RouteHeader
 * @see HeaderAddress
 * @see Parameters
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface RecordRouteHeader extends HeaderAddress, Parameters, Header {

    /**
     * Name of RecordRouteHeader
     */
    public final static String NAME = "Record-Route";
}

