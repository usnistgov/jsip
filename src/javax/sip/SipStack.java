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
 * File Name     : SipStack.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import javax.sip.address.Router;
import java.util.*;

/**
 * This interface represents the management interface of a SIP stack 
 * implementing this specification and as such is the interface that defines 
 * the management/architectural view of the SIP stack. It defines the methods 
 * required to represent and provision a proprietary SIP protocol stack.
 * <p>
 * This SipStack interface defines the methods that are be used by an
 * application implementing the {@link javax.sip.SipListener} interface to
 * control the architecture and setup of the SIP stack. These methods include:
 * <ul>
 * <li>Creation/deletion of {@link javax.sip.SipProvider}'s that represent
 * messaging objects that can be used by an application to send
 * {@link javax.sip.message.Request} and {@link javax.sip.message.Response}
 * messages statelessly or statefully via Client and Server transactions.
 * <li>Creation/deletion of {@link javax.sip.ListeningPoint}'s that represent
 * different ports and transports that a SipProvider can use to send and
 * receive messages.
 * </ul>
 * <b>Architecture:</b><br>
 * This specification mandates a single SipStack object per IP Address. There 
 * is a one-to-many relationship between a SipStack and a SipProvider. There is 
 * a one-to-many relationship between a SipStack and a ListeningPoint.
 * <p>
 * <b>SipStack Creation</b><br>
 * An application must create a SipStack by invoking the
 * {@link SipFactory#createSipStack(Properties)} method, ensuring the 
 * {@link SipFactory#setPathName(String)} is set. Following the naming
 * convention defined in {@link javax.sip.SipFactory}, the implementation of 
 * the SipStack interface must be called SipStackImpl. This specification also 
 * defines a stack configuration mechanism using java.util.Properties, 
 * therefore this constructor must also accept a properties argument:
 * <p>
 * <center>public SipStackImpl(Properties properties) {}</center>
 * <p>
 * The following table documents the static configuration properties which can 
 * be set for an implementation of a SipStack. This specification doesn't preclude 
 * additional values within a configuration properties object if understood by 
 * the underlying implementation. In order to change these properties after 
 * a SipStack has been initialized the SipStack must be deleted and recreated:
 * <p>
 * <center>
 * <table border="1" bordercolorlight="#FFFFFF" bordercolordark="#000000" width="98%" cellpadding="3" cellspacing="0">
 * <p class="title"></p>
 * <tr bgcolor="#CCCCCC">
 *	<th align="left" valign="top">
 *		<p class="table"><strong><strong>SipStack Property</strong></strong>
 * 	</th>
 *	<th align="left" valign="top">
 *		</a><p class="table"><strong>Description</strong></p>
 * 	</th>
 * </tr>
 * <tr>
 *	<td align="left" valign="top">
 *		<p class="table">javax.sip.IP_ADDRESS</p>
 *	</td>
 *	<td align="left" valign="top">
 * 		<p class="table">Sets the IP Address of the SipStack to the 
 *              property value i.e 11.1.111.111. This property is mandatory.</p>
 *	</td>
 * </tr>
 * <tr>
 *	<td align="left" valign="top">
 *		<p class="table">javax.sip.STACK_NAME</p>
 *	</td>
 *	<td align="left" valign="top">
 * 		<p class="table">Sets a user friendly name to identify the 
 *              underlying stack implementation to the property value i.e. 
 *              NISTv1.1. The stack name property should contain no spaces. 
 *              This property is mandatory.</p>
 *	</td>
 * </tr>
 * <tr>
 *	<td align="left" valign="top">
 *		<p class="table">javax.sip.OUTBOUND_PROXY</p>
 *	</td>
 *	<td align="left" valign="top">
 * 		<p class="table">Sets the outbound proxy of the SIP Stack.
 *              This property maps to the the outbound proxy parameter of the 
 *              Router interface. 
                The format of the outbound proxy parameter should be 
 *              "ipaddress:port/transport" i.e. 129.1.22.333:5060/UDP. This 
 *              property is optional.</p>
 *	</td>
 * </tr>
 * <tr>
 *	<td align="left" valign="top">
 *		<p class="table">javax.sip.ROUTER_PATH</p>
 *	</td>
 *	<td align="left" valign="top">
 * 		<p class="table">Sets the fully qualified classpath to the 
 *              application supplied Router object that determines how to route 
 *              messages before a dialog is established i.e. com.sun.javax.sip.RouteImpl.
 *              An application defined router object must implement the
 *              javax.sip.Router interface. Different routing policies may be 
 *              based on opertaion mode i.e. User Agent or Proxy. This property is optional.
 *	</td>
 * </tr>
 * <tr>
 *	<td align="left" valign="top">
 *		<p class="table">javax.sip.EXTENSION_METHODS</p>
 *	</td>
 *	<td align="left" valign="top">
 * 		<p class="table">This configuration value informs the underlying
 *              implementation of supported extension methods that create new
 *              dialog's. This configuration flag should only be used for dialog 
 *              creating extension methods, other extension methods that 
 *              don't create dialogs can be used using the method parameter on 
 *              Request assuming the implementation understands the method. If more
 *              than one method is supported in this property each extension 
 *              should be seprated with a colon for example "FOO:BAR". This 
 *              property is optional.</p>
 *	</td>
 * </tr>
 * <tr>
 *	<td align="left" valign="top">
 *		<p class="table">javax.sip.RETRANSMISSON_FILTER</p>
 *	</td>
 *	<td align="left" valign="top">
 * 		<p class="table">The default retransmission behaviour of this 
 *              specification is dependent on the application core and is defined 
 *              as follows:
 *              <ul>
 *              <li>User Agent Client: Retransmissions of ACK Requests are the
 *              responsibility of the application. All other retansmissions are 
 *              handled by the SipProvider.
 *              <li>User Agent Server: Retransmissions of 1XX, 2XX Responses are the
 *              responsibility of the application. All other retansmissions are 
 *              handled by the SipProvider.
 *              <li>Stateful Proxy: As stateful proxies have no Invite 
 *              transactions all retransmissions are handled by the SipProvider.
 *              <li>Stateless Proxy: As stateless proxies are not transactional 
 *              all retransmissions are the responsibility of the application 
 *              and will not be handled the SipProvider.
 *              </ul>
 *              This filter can be viewed as a helper function for User Agents 
 *              that can be set by an application to prevent the application 
 *              from handling retransmission of ACK Requests, 1XX and 2XX 
 *              Responses for INVITE transactions, i.e. the SipProvider will 
 *              handle the retransmissions. This utility is useful for hiding 
 *              protocol retransmission semantics from higher level 
 *              programming environments. The acceptable values are ON/OFF. This 
 *              property is optional, therefore if not supplied the default is 
 *              OFF.
 *	</td>
 * </tr>
 * </table>
 * </center>
 *
 * @see SipFactory
 * @see SipProvider
 *
 * @author Sun Microsystems
 * @version 1.1
 *
 */

public interface SipStack {

    /**
     * Creates a new peer SipProvider on this SipStack on a specified
     * ListeningPoint and returns a reference to the newly created SipProvider
     * object. The newly created SipProvider is implicitly attached to this
     * SipStack upon execution of this method, by adding the SipProvider to the 
     * list of SipProviders of this SipStack once it has been successfully 
     * created.
     *
     * @return the SipProvider attached to this SipStack on the specified
     * ListeningPoint.
     * @param listeningPoint the ListeningPoint the SipProvider is to be 
     * attached to in order to send and receive messages.
     * @throws ObjectInUseException if another SipProvider is 
     * already using the ListeningPoint.
     */
    public SipProvider createSipProvider(ListeningPoint listeningPoint)
                                throws ObjectInUseException;

    /**
     * Deletes the specified peer SipProvider attached to this SipStack. The
     * specified SipProvider is implicitly detached from this SipStack upon
     * execution of this method, by removing the SipProvider from the
     * SipProviders list of this SipStack. Deletion of a SipProvider does not 
     * automatically delete the SipProvider's ListeningPoint from the SipStack.
     *
     * @param sipProvider the peer SipProvider to be deleted from this 
     * SipStack.
     * @throws ObjectInUseException if the specified SipProvider cannot be 
     * deleted because the SipProvider is currently in use.
     *
     */
    public void deleteSipProvider(SipProvider sipProvider)
                                            throws ObjectInUseException;

    /**
     * Returns an Iterator of existing SipProviders that have been
     * created by this SipStack. All of the SipProviders of this SipStack will 
     * belong to the same stack vendor.
     *
     * @return an Iterator containing all existing SipProviders created
     * by this SipStack. Returns an empty Iterator if no SipProviders exist.
     */
    public Iterator getSipProviders();

    /**
     * Creates a new ListeningPoint on this SipStack on a specified
     * port and transport, and returns a reference to the newly created
     * ListeningPoint object. The newly created ListeningPoint is implicitly
     * attached to this SipStack upon execution of this method, by adding the
     * ListeningPoint to the List of ListeningPoints of this SipStack once it 
     * has been successfully created.
     *
     * @return the ListeningPoint attached to this SipStack.
     * @param port the port of the new ListeningPoint.
     * @param transport the transport of the new ListeningPoint.
     * @throws TansportNotSupportedException if the specified  
     * transport is not supported by this SipStack.
     * @throws InvalidArgumentException if the specified port is invalid.
     * @since v1.1
     */
    public ListeningPoint createListeningPoint(int port, String transport)
             throws TransportNotSupportedException, InvalidArgumentException;

    /**
     * Deletes the specified ListeningPoint attached to this SipStack. The
     * specified ListeningPoint is implicitly detached from this SipStack upon
     * execution of this method, by removing the ListeningPoint from the
     * ListeningPoints list of this SipStack.
     *
     * @param listeningPoint the SipProvider to be deleted from this SipStack.
     * @throws ObjectInUseException if the specified ListeningPoint cannot be 
     * deleted because the ListeningPoint is currently in use.
     *
     * @since v1.1
     */
    public void deleteListeningPoint(ListeningPoint listeningPoint)
                                throws ObjectInUseException;

    /**
     * Returns an Iterator of existing ListeningPoints created by this
     * SipStack. All of the ListeningPoints of this SipStack belong to the 
     * same stack vendor.
     *
     * @return an Iterator containing all existing ListeningPoints created
     * by this SipStack. Returns an empty Iterator if no ListeningPoints exist.
     */
    public Iterator getListeningPoints();

// Configuration methods    
    
    /**
     * Gets the user friendly name that identifies this SipStack instance. This 
     * value is set using the Properties object passed to the 
     * {@link SipFactory#createSipStack(Properties)} method upon creation of 
     * the SipStack object.
     *
     * @return a string identifing the stack instance
     */
    public String getStackName();

    /**
     * Gets the IP Address that identifies this SipStack instance. Every 
     * SipStack object must have an IP Address and only one SipStack object 
     * can service an IP Address. This value is set using the Properties 
     * object passed to the {@link SipFactory#createSipStack(Properties)} method upon 
     * creation of the SipStack object.
     *
     * @return a string identifing the IP Address
     * @since v1.1
     */    
    public String getIPAddress();

    /**
     * Gets the Router object that identifies the default Router information 
     * of this SipStack, including the outbound proxy. This value is set using 
     * the Properties object passed to the 
     * {@link SipFactory#createSipStack(Properties)} method upon creation of 
     * the SipStack object.
     *
     * @return the Router object identifying the Router information.
     * @since v1.1
     */    
    public Router getRouter();    
    
    /**
     * This method returns the value of the retransmission filter helper
     * function for User Agent applications. This value is set using the 
     * Properties object passed to the 
     * {@link SipFactory#createSipStack(Properties)} method upon creation of 
     * the SipStack object.
     * <p>
     * The default value of the retransmission filter boolean is <var>false</var>.
     * When this value is set to <code>true</code>, retransmissions of ACK's and
     * 2XX responses to an INVITE transaction are handled 
     * by the SipProvider, hence the application will not receive 
     * {@link Timeout#RETRANSMIT} notifications encapsulated in
     * {@link javax.sip.TimeoutEvent}'s, however an application will be
     * notified if the underlying transaction expires with a
     * {@link Timeout#TRANSACTION} notification encapsulated in a TimeoutEvent.
     *
     * @return the value of the retransmission filter, <code>true</code> if the 
     * filter is set, <code>false</code> otherwise.
     * @since v1.1
     */
    public boolean isRetransmissionFilterActive();

}

