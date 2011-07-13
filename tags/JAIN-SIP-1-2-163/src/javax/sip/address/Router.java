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
 * File Name     : Router.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     19/12/2002  Phelim O'Doherty    Initial version
 *  1.2     16/06/2005  Phelim O'Doherty    Deprecated getNextHops and replaced
 *                                          with getNextHop.
 *              M. Ranganathan      Worked out details and clarified behavior.
 *              Sarit Galanos
 *              Jeroen van Bemmel
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;

import javax.sip.SipException;
import javax.sip.message.Request;
import java.util.ListIterator;

/**
 * The Router interface may be implemented by the application to provide custom
 * routing logic. It is used to determine the next hop for a given request.
 *
 * <p>For backwards compatibility reasons, the default behavior of the stack is
 * to consult the application provided Router implementation for all requests
 * outside of a dialog. This is controlled through the stack property
 * <code>javax.sip.USE_ROUTER_FOR_ALL_URIS</code> which defaults to <code>true</code>
 * when not set.
 *
 * <p>This specification recommends to set the stack property
 * <code>javax.sip.USE_ROUTER_FOR_ALL_URIS</code> to
 * <code>false</code>. This will cause the stack to only consult the application
 * provided Router implementation for requests with a non-SIP URI as request URI
 * (such as tel: or pres:) and without Route headers. This enables an application
 * to implement DNS lookups and other resolution algorithms
 *
 * <p>When <code>javax.sip.USE_ROUTER_FOR_ALL_URIS</code> is set to
 * <code>false</code>, the next hop is determined according to the following algorithm:
 * <ul>
 * <li> If the request contains one or more Route headers, use the URI of the
 *      topmost Route header as next hop, possibly modifying the request in
 *      the process if the topmost Route header contains no lr parameter(See Note below))
 * <li> Else, if the property <code>javax.sip.OUTBOUND_PROXY</code> is set, use its
 *      value as the next hop
 * <li> Otherwise, use the request URI as next hop. If the request URI is not a SIP
 *      URI, call {@link javax.sip.address.Router#getNextHop(Request)} provided by the application.
 * </ul>
 *
 * <p><b>Note:</b> In case the topmost Route header contains no 'lr' parameter
 * (which means the next hop is a strict router), the implementation will perform
 * 'Route Information Postprocessing' as described in RFC3261 section 16.6 step 6
 * (also known as "Route header popping"). That is, the following modifications will be
 * made to the request:
 * <ol>
 * <li>The implementation places the Request-URI into the Route header
            field as the last value.
   <li>The implementation then places the first Route header field value
            into the Request-URI and removes that value from the Route
            header field.
 * </ol>
 * Subsequently, the request URI will be used as next hop target.
 *
 * <p>The location (classname) of the user-defined Router object is supplied in the
 * Properties object passed to the
 * {@link javax.sip.SipFactory#createSipStack(Properties)} method upon creation
 * of the SIP Stack object.
 * The Router object must accept a SipStack as an argument to the constructor in
 * order for the Router to access attributes of the SipStack
 * The constructor of an object implementing the Router interface must be
 * <code>RouterImpl(SipStack sipStack, String outboundProxy) {}</code>
 * <p>
 * The routing policy can not be changed dynamically, i.e. the SipStack needs to be
 * deleted and re-created.
 * Outbound proxy should be passed to the
 * {@link javax.sip.SipFactory#createSipStack(Properties)} method upon creation
 * of the SIP Stack object.
 *
 * <p><b>Application Notes</b><br/>
 *
 * <p>A UAC application which desires to use a particular outbound
 * proxy should prepend a Route header with the URI of that proxy (and 'lr' flag if
 * appropriate).
 * Alternatively, it may achieve the same result by setting the OUTBOUND_PROXY
 * property (although the Route header approach is more flexible and therefore RECOMMENDED)
 *
 * <p>A proxy application may either rewrite the request URI (if the proxy is
 * responsible for the domain), or prepend a Route header.
 *
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface Router {


    /**
     * Gets the Outbound Proxy parameter of this Router, this method may return
     * null if no outbound proxy is defined.
     *
     *
     * @return the Outbound Proxy of this Router.
     * @see Hop
     */
    public Hop getOutboundProxy();


    /**
     * Gets the ListIterator of the hops of the default Route.
     * This method may return null if a default route is not defined.
     *
     * @deprecated Since v1.2. This method is replaced with
     * {@link Router#getNextHop(Request)} method which returns the next
     * Hop for this request.
     *
     */
    public ListIterator getNextHops(Request request);

    /**
     * Gets the next Hop from this Router for the specified request, this
     * method may return <code>null</code> if a default route is not defined.
     *
     * @return the next Hop from this Router for the Request.
     * @see Hop
     * @since v1.2
     *
     * @throws SipException when there is something wrong with the request
     */
    public Hop getNextHop(Request request) throws SipException;



}

