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
 * File Name     : SipStack.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Reworked
 *  1.2     19/05/2005  Phelim O'Doherty    Added new new config parameters
 *                                          Added new createListeningPoint
 *                                          method with IP address
 *                                          Added start and stop stack methods
 *                      M. Ranganathan      Fixed documentation.
 *
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import javax.sip.address.Router;
import java.util.*;

/**
 * This interface represents the management interface of a SIP stack
 * implementing this specification and as such is the interface that defines the
 * management/architectural view of the SIP stack. It defines the methods
 * required to represent and provision a proprietary SIP protocol stack.
 * <p>
 * This SipStack interface defines the methods that are be used by an
 * application implementing the {@link javax.sip.SipListener} interface to
 * control the architecture and setup of the SIP stack. These methods include:
 * <ul>
 * <li>Creation/deletion of {@link javax.sip.SipProvider}'s that represent
 * messaging objects that can be used by an application to send
 * {@link javax.sip.message.Request}and {@link javax.sip.message.Response}
 * messages statelessly or statefully via Client and Server transactions.
 * <li>Creation/deletion of {@link javax.sip.ListeningPoint}'s that represent
 * different ports and transports that a SipProvider can use to send and receive
 * messages.
 * </ul>
 * <b>Architecture: </b> <br>
 * This specification mandates a one-to-many relationship between a SipStack
 * and a SipProvider. There is a one-to-many relationship between a SipStack
 * and a ListeningPoint.
 * <p>
 * <b>SipStack Creation </b> <br>
 * An application must create a SipStack by invoking the
 * {@link SipFactory#createSipStack(Properties)}method, ensuring the
 * {@link SipFactory#setPathName(String)}is set. Following the naming
 * convention defined in {@link javax.sip.SipFactory}, the implementation of
 * the SipStack interface must be called SipStackImpl. This specification also
 * defines a stack configuration mechanism using java.util.Properties, therefore
 * this constructor must also accept a properties argument:
 * <p>
 * <center>public SipStackImpl(Properties properties) {} </center>
 * <p>
 * The following table documents the static configuration properties which can
 * be set for an implementation of a SipStack. This specification doesn't
 * preclude additional values within a configuration properties object if
 * understood by the underlying implementation. In order to change these
 * properties after a SipStack has been initialized the SipStack must be deleted
 * and recreated:
 * <p>
 * <center><table border="1" bordercolorlight="#FFFFFF"
 * bordercolordark="#000000" width="98%" cellpadding="3" cellspacing="0">
 * <p class="title">
 * </p>
 * <tr bgcolor="#CCCCCC">
 * <th align="left" valign="top">
 * <p class="table">
 * <strong><strong>SipStack Property </strong> </strong></th>
 * <th align="left" valign="top"></a>
 * <p class="table">
 * <strong>Description </strong>
 * </p>
 * </th>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.IP_ADDRESS
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 * <b>Deprecated v1.2.</b> It is recommended in this specification that the IP
 * Address should be set using the enhanced ListeningPoint architecture,
 * therefore this property is no longer mandatory. When this parameter is
 * specified as null, a singleton stack instance will be created and returned
 * by the SipFactory and the IP Address attributes can be managed via the
 * {@link SipStack#createListeningPoint(String, int, String)} method. For
 * backwards compatability if this flag is set the SipFactory will return a new
 * SipStack instance each time a SipStack is created with a new IP Address.
 * This configuration parameter will become the default IP address of the
 * SipStack. The SIP Factory will return any any existing instance of SipStack
 * that already exist for this IP Address.
 * </td>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.STACK_NAME
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 * Sets a user friendly name to identify the underlying stack implementation to
 * the property value i.e. NISTv1.2. The stack name property should contain no
 * spaces. This property is mandatory.
 * </p>
 * </td>
 * </tr>
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.OUTBOUND_PROXY
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 * Sets the outbound proxy of the SIP Stack.  The  fromat
 * for this string is "ipaddress:port/transport" i.e.
 * 129.1.22.333:5060/UDP. This property is optional.
 * </p>
 * </td>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.ROUTER_PATH
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 * Sets the fully qualified classpath to the application supplied Router object
 * that determines how to route messages when the stack cannot make a routing
 * decision ( ie. non-sip URIs). In version 1.2 of this specification,
 * out of Dialog SIP URIs are routed by the Routing algorithm defined in RFC 3261
 * which is implemented internally by the stack provided that
 * javax.sip.USE_ROUTER_FOR_ALL_URIS is set to false.  In this case,
 * the installed Router object is consulted for routing decisions pertaining to
 * non-SIP URIs. An application defined Router object must implement the javax.sip.Router interface.
 * This property is optional.
 * </td>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.EXTENSION_METHODS
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 * This configuration value informs the underlying implementation of supported
 * extension methods that create new dialog's. This list must not include methods
 * that are natively supported by this specification such as INVITE, SUBSCRIBE and REFER.
 * This configuration flag should
 * only be used for dialog creating extension methods, other extension methods
 * that don't create dialogs can be used using the method parameter on Request
 * assuming the implementation understands the method. If more than one method
 * is supported in this property each extension should be seprated with a colon
 * for example "FOO:BAR". This property is optional.
 * </p>
 * </td>
 * </tr>
 *
 * <tr>
 *  <td align="left" valign="top">
 *      <p class="table">javax.sip.RETRANSMISSION_FILTER</p>
 *  </td>
 *  <td align="left" valign="top">
 *      <p class="table"><b>Deprecated v1.2.</b> Applications can request
 *              retransmission alerts from the
 *              {@link ServerTransaction#enableRetransmissionAlerts()}.
 *              <p>
 *              The default retransmission behaviour of this
 *              specification is dependent on the application core and is defined
 *              as follows:
 *              <ul>
 *              <li>User Agent Client: Retransmissions of ACK Requests are the
 *              responsibility of the application. All other retansmissions are
 *              handled by the SipProvider.
 *              <li>User Agent Server: Retransmissions of 1xx, 2xx Responses are the
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
 *              from handling retransmission of ACK Requests, 1xx and 2xx
 *              Responses for INVITE transactions, i.e. the SipProvider will
 *              handle the retransmissions. This utility is useful for hiding
 *              protocol retransmission semantics from higher level
 *              programming environments. The acceptable values are ON/OFF. This
 *              property is optional, therefore if not supplied the default is
 *              OFF.
 *  </td>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.AUTOMATIC_DIALOG_SUPPORT
 * </p>
 * </td>
 * <td align="left" valign="top">This property specifies the defined values
 * 'ON' and 'OFF'. The default value is 'ON'. The default behavior represents a
 * common mode of stack operation and allows the construction of simple user
 * agents. This property is optional. This is summarized as:</li>
 * <ul>
 * <li>A dialog gets created on a dialog creating transaction.</li>
 * <li> The first respose having both a From and a To tag creates the transaction.
 * <li>The first 2xx response to the transaction will drive the dialog to the
 * CONFIRMED state.</li>
 * </ul>
 * </ul>
 * <blockquote>The ability to turn of dialog support is motivated by dialog free
 * servers (such as proxy servers) that do not want to pay the overhead of the
 * dialog layer and user agents that may want to create multiple dialogs for a
 * single INVITE (as a result of forking by proxy servers). The following
 * behavior is defined when the configuration parameter is set to 'OFF'.
 * <ul type="circle">
 * <li>The application is responsible to create the Dialog if desired.</li>
 * <li>The application may create a Dialog and associate it with a response
 * (provisional or final) of a dialog creating request.&nbsp;</li>
 * </ul>
 * </blockquote>
 * <b>Since v1.2.</b>
 * </td>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.FORKABLE_EVENTS
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 *  Comma separated list of events for which the implementation should expect forked
 *  SUBSCRIBE dialogs. Each element of this list must have the syntax packagename.eventname
 *
 *  This configuration parameter is provided in order to support the following behavior ( defined
 *  in RFC 3265):
 *
 *  Successful SUBSCRIBE requests will normally receive only
 *  one 200-class response; however, due to forking, the subscription may
 *  have been accepted by multiple nodes.  The subscriber MUST therefore
 *  be prepared to receive NOTIFY requests with "From:" tags which differ
 *  from the "To:" tag received in the SUBSCRIBE 200-class response.
 *
 *  If multiple NOTIFY messages are received in different dialogs in
 *  response to a single SUBSCRIBE message, each dialog represents a
 *  different destination to which the SUBSCRIBE request was forked.
 *
 *  Each event package MUST specify whether forked SUBSCRIBE requests are
 *  allowed to install multiple subscriptions.If such behavior is not allowed,
 *  the first potential dialog-establishing message will create a dialog.
 * All subsequent NOTIFY messages which correspond to the SUBSCRIBE message
 * (i.e., match "To","From", "From" header "tag" parameter, "Call-ID", "CSeq", "Event",
 * and "Event" header "id" parameter) but which do not match the dialog
 * would be rejected with a 481 response. This property is optional.
 * <p>
 * <b>Since v1.2</b>
 * </p>
 * </td>
 * </tr>
 *
 * <tr>
 * <td align="left" valign="top">
 * <p class="table">
 * javax.sip.USE_ROUTER_FOR_ALL_URIS
 * </p>
 * </td>
 * <td align="left" valign="top">
 * <p class="table">
 * If set to <it>true</it> then the application installed
 * Router is consulted for ALL routing decisions (ie. both out of dialog SIP and non-SIP request
 * URI's -- identitcal to the behavior supported in v1.1 of this specification). If set to
 * <it>false</it> the user installed router will only be consulted for routing of Non-SIP URIs.
 * Implementations may thus provide support for sophisticated operations such as DNS lookup
 * for SIP URI's  using the proceedures defined in RFC 3263
 * (support for RFC 3263 is not mandatory for this specification). This property is optional.
 * The default value for this parameter is <it>true</it>.
 * <p>
 * <b>Since v1.2.</b>
 * </td>
 * </tr>
 *
 * </table> </center>
 *
 * @see SipFactory
 * @see SipProvider
 *
 * @author BEA Systems, NIST
 * @version 1.2
 *
 */

public interface SipStack {

    /**
     * Creates a new peer SipProvider on this SipStack on a specified
     * ListeningPoint and returns a reference to the newly created SipProvider
     * object. The newly created SipProvider is implicitly attached to this
     * SipListener upon execution of this method, by adding the SipProvider to
     * the list of SipProviders of this SipStack once it has been successfully
     * created.
     *
     * @param listeningPoint listening point for this SipProvider.
     * @throws ObjectInUseException if another SipProvider is already associated
     * with this ListeningPoint.
     * @return the newly created SipProvider been started.
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
     * Returns an Iterator of existing SipProviders that have been created by
     * this SipStack. All of the SipProviders of this SipStack will belong to
     * the same stack vendor.
     *
     * @return the list of Providers attached to this Sipstack.
     */
    public Iterator getSipProviders();


    /**
     * Creates a new ListeningPoint on this SipStack on a specified port and
     * transport and the default IP address of this stack as specified by the
     * SipStack IP address configuration parameter, and returns a reference to
     * the newly created ListeningPoint object. The newly created ListeningPoint
     * is implicitly attached to this SipStack upon execution of this method, by
     * adding the ListeningPoint to the List of ListeningPoints of this SipStack
     * once it has been successfully created.
     *
     * @return the ListeningPoint attached to this SipStack.
     * @param port
     *            the port of the new ListeningPoint.
     * @param transport
     *            the transport of the new ListeningPoint.
     * @throws TansportNotSupportedException
     *             if the specified transport is not supported by this SipStack.
     * @throws InvalidArgumentException
     *             if the specified port is invalid.
     *
     * @deprecated Since v1.2. This has been replaced by
     *  {@link SipStack#createListeningPoint(String, int, String)  }
     * For backwards compatibility with v1.1 implementations should support this method.
     * Implementations should throw <it>TransportNotSupportedException</it>
     * if the Properties specified during stack creation do not include an IP Address.
     */
    public ListeningPoint createListeningPoint(int port, String transport)
            throws TransportNotSupportedException, InvalidArgumentException;

    /**
     * Creates a ListeningPoint a given IP address, port and transport. If this
     * method is used, the IP address of the stack is ignored and a listening
     * point is created with the given parameters. This support is useful for
     * multi-homed hosts which may have to listen at multiple IP addresses and
     * have different dialogs for each IP address.
     *
     * @return ListeningPoint that uses the IP address port and transport.
     * @throws SipException if the Listening point cannot be created for any reason or if the
     * stack has specified a default IP address that differs from the IP address specified
     * for this method.
     * @throws InvalidArgumentException
     * @since 1.2
     */
    public ListeningPoint createListeningPoint(String ipAddress, int port,
            String transport) throws TransportNotSupportedException, InvalidArgumentException;

    /**
     * Deletes the specified ListeningPoint attached to this SipStack. The
     * specified ListeningPoint is implicitly detached from this SipStack upon
     * execution of this method, by removing the ListeningPoint from the
     * ListeningPoints list of this SipStack.
     *
     * @param listeningPoint
     *            the SipProvider to be deleted from this SipStack.
     * @throws ObjectInUseException
     *             if the specified ListeningPoint cannot be deleted because the
     *             ListeningPoint is currently in use.
     */
    public void deleteListeningPoint(ListeningPoint listeningPoint)
            throws ObjectInUseException;

    /**
     * Returns an Iterator of existing ListeningPoints created by this SipStack.
     * All of the ListeningPoints of this SipStack belong to the same stack
     * vendor.
     *
     * @return an Iterator containing all existing ListeningPoints created by
     *         this SipStack. Returns an empty Iterator if no ListeningPoints
     *         exist.
     */
    public Iterator getListeningPoints();

    // Configuration methods

    /**
     * Gets the user friendly name that identifies this SipStack instance. This
     * value is set using the Properties object passed to the
     * {@link SipFactory#createSipStack(Properties)}method upon creation of the
     * SipStack object.
     *
     * @return a string identifing the stack instance
     */
    public String getStackName();

    /**
     * Gets the IP Address that identifies this SipStack instance. Every
     * SipStack object may have an IP Address. This specification recommends
     * associating the IP address with the {@link ListeningPoint}.
     * For backwards compatability if an IP address is specified in the
     * Properties object passed to the
     * {@link SipFactory#createSipStack(Properties)} method upon creation of
     * the SipStack object then this becomes the default IP address of the
     * SipStack object, ListeningPoints can then be created on this IP Address.
     *
     * @return a string identifing the IP Address. Null if there is no default
     * IP address associated with the stack instance.
     */
    public String getIPAddress();

    /**
     * Gets the Router object that identifies the default Router information of
     * this SipStack. This value is set using the Properties object passed to
     * the {@link SipFactory#createSipStack(Properties)}method upon creation of
     * the SipStack object.
     *
     * @return the Router object identifying the Router information.
     */
    public Router getRouter();

    /**
     * This methods initiates the shutdown of the stack. The stack will
     * terminate all ongoing transactions, without providing
     * notificatin to the listener, close all listening points and release all
     * resources associated with this stack. Note that this is a hard stop and
     * should be used with care. The application may build graceful stop
     * measures if needed, however the implementation is expected to
     * immediately release any resources such as threads sockets and buffers
     * that are allocated to this stack.
     *
     * @since 1.2
     */
    public void stop();

    /**
     * This method initiates the active processing of the stack. This method is
     * used to start the stack after the necessary SipProviders have been
     * created.   After calling this method, the stack can handle incoming
     * requests and responses on the ListeningPoints associated to the
     * SipProviders.
     *
     * @throws SipException if the stack cannot be started due to some system
     * level failure.
     *
     * @since 1.2
     */
    public void start() throws SipException;

    /**
     * This method returns the value of the retransmission filter helper
     * function for User Agent applications. This value is set using the
     * Properties object passed to the
     * {@link SipFactory#createSipStack(Properties)}method upon creation of the
     * SipStack object.
     * <p>
     * The default value of the retransmission filter boolean is <var>false
     * </var>. When this value is set to <code>true</code>, retransmissions
     * of ACK's and 2xx responses to an INVITE transaction are handled by the
     * SipProvider, hence the application will not receive
     * {@link Timeout#RETRANSMIT}notifications encapsulated in
     * {@link javax.sip.TimeoutEvent}'s, however an application will be
     * notified if the underlying transaction expires with a
     * {@link Timeout#TRANSACTION}notification encapsulated in a TimeoutEvent.
     *
     * @deprecated Since v1.2. This retransmission filter property has been deprecated
     * as a SIP Stack property. Applications can enable retransmission alerts by using the
     * {@link ServerTransaction#enableRetransmissionAlerts()} method.
     *
     * @return the value of the retransmission filter, <code>true</code> if
     *         the filter is set, <code>false</code> otherwise.
     */
    public boolean isRetransmissionFilterActive();

}

