/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and applicable 
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. Sun, 
 * Sun Microsystems, the Sun logo, Java, Jini and JAIN are trademarks or 
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other 
 * countries.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JAIN SIP Specification
 * File Name     : Router.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     19/12/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.address;

import javax.sip.message.Request;
import java.util.ListIterator;

/**
 * The Router interface defines accessor methods to retrieve the default Route
 * and Outbound Proxy of this SipStack. The Outbound Proxy and default Route are
 * made up one or more {@link Hop}'s. This Router information is user-defined in
 * an object that implements this interface. The location of the user-defined
 * Router object is supplied in the Properties object passed to the 
 * {@link javax.sip.SipFactory#createSipStack(Properties)} method upon creation 
 * of the SIP Stack object. 
 * The Router object must accept a SipStack as an argument to the constructor in 
 * order for the Router to access attributes of the SipStack such as IP Address. 
 * The constructor of an object implementing the Router interface must be 
 * <code>RouterImpl(SipStack sipStack, String outboundProxy) {}</code> 
 * <p>
 * The user may define a routing policy dependent on the operation of the 
 * SipStack i.e. user agent or proxy, however this routing policy can not be 
 * changed dynamically, i.e. the SipStack needs to be deleted and re-created. 
 *
 * @author Sun Microsystems
 * @since 1.1
 */

public interface Router {


    /**
     * Gets the Outbound Proxy parameter of this Router, this method may return
     * null if no outbound proxy is defined. 
     *
     * @return the Outbound Proxy of this Router.
     * @see Hop
     */
    public Hop getOutboundProxy();


    /**
     * Gets the ListIterator of the hops of the default Route. This method may 
     * return null if a default route is not defined. 
     *
     * @param request - the Request message that determines the default route.
     * @return the ListIterator over all the hops of this Router.
     * @see Hop
     */
    public ListIterator getNextHops(Request request);    
    
}

