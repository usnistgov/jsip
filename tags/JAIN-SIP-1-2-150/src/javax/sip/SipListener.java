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
 * File Name     : SipListener.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty
 *  1.2     02/15/2005  M. Ranganathan  Added method for IO Exception
 *  1.2     02/15/2005  M. Ranganathan  Added method for TransactionTerminated
 *                      propagation.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import java.util.EventListener;

/**
 * This interface represents the application view to a SIP stack therefore
 * defines the application's communication channel to the SIP stack. This
 * interface defines the methods required by an applications to receive and
 * process Events that are emitted by an object implementing the
 * {@link javax.sip.SipProvider}interface.
 * <p>
 * The Events accepted by a SipListener may be one of four types:
 * <ul>
 * <li>{@link RequestEvent}- these are request messages emitted as events by
 * the SipProvider. Request events represent request messages i.e. INVITE, that
 * are received from the network to the application via the underlying stack
 * implementation.
 * <li>{@link ResponseEvent}- these are response messages emitted as events by
 * the SipProvider. Response events represent Response messages i.e. 2xx's, that
 * are received from the network to the application via the underlying stack
 * implementation.
 * <li>{@link TimeoutEvent}- these are timeout notifications emitted as events
 * by the SipProvider. Timeout events represent timers expiring in the
 * underlying SipProvider transaction state machine. These timeout's events
 * notify the application that a retranmission is required or a transaction has
 * timed out.
 * <li>{@link IOExceptionEvent}- these are IO Exception notifications emitted as
 * events by the SipProvider. IOException events represent failure in the
 * underlying SipProvider IO Layer. These IO Exception events notify the
 * application that a failure has occured while accessing a socket.
 * <li>{@link TransactionTerminatedEvent}- these are Transaction Terminated
 * notifications emitted as events by the SipProvider. TransactionTerminated
 * events represent a transaction termination and notify the
 * application of the termination.
 * <li>{@link DialogTerminatedEvent}- these are Dialog Terminated
 * notifications emitted as events by the SipProvider. DialogTerminated
 * events represent a Dialog termination and notify the
 * application of the termination.
 * </ul>
 * <p>
 * An application will only receive Request, Response, Timeout,
 * TransactionTerminated, DialogTerminated and IOException
 * events once it has registered as an EventListener of a SipProvider. The
 * application registers with the SipProvider by invoking the
 * {@link SipProvider#addSipListener(SipListener)}passing itself as an
 * argument.
 * <p>
 * <b>Architecture: </b> <br>
 * This specification mandates a single SipListener per SipStack,
 * and a unicast event model i.e. a SipProvider can only have one SipListener
 * registered with it. This specification allows multiple SipProviders per
 * SipStack and as such a SipListener can register with multiple SipProviders
 * i.e there is a one-to-many relationship between a SipListener and a SipProvider.
 * <p>
 * Note: An application that implements the SipListener interface, may act as a
 * proxy object and pass all events to higher level core application programming
 * logic that is outside the scope of this specification. For example a SIP
 * Servlet, or a JSLEE implementation can implement a back to back UA or
 * Proxy core application respectively in there respective container
 * environments utilizing this specification to talk the SIP protocol.
 * <p>
 * <b>Messaging Model: </b> <br>
 * An application can send messages by passing {@link javax.sip.message.Request}
 * and {@link javax.sip.message.Response}messages to that the following object:
 * <ul>
 * <li>Request and response messages can be sent statelessly via the
 * SipProvider using the send methods on the {@link javax.sip.SipProvider}.
 * <li>Request messages can be sent transaction stateful via the
 * ClientTransaction using the {@link ClientTransaction#sendRequest()}method.
 * <li>Response messages can be sent transaction stateful via the
 * ServerTransaction using the {@link ServerTransaction#sendResponse(Response)}
 * method.
 * <li>Request messages can be sent dialog stateful via the Dialog using the
 * {@link Dialog#sendRequest(ClientTransaction)}method.
 * </ul>
 * Although this specification provides the capabilities to send messages both
 * statelessly and statefully it is mandated that an application will not send
 * the same message both statefully and statelessly.
 * The messages sent by the application are not Event's as the event model is
 * uni-directional from the SipProvider to the SipListener, i.e. the SipListener
 * listens for Events from the SipProvider, but the SipProvider does not listen
 * for Events on the SipListener. The rationale is the application knows when to
 * initiate requests and responses i.e setup a call or respond to a network
 * event, however an application doesn't know when it will receive a network
 * event, hence the application must listen for these network events.
 * <p>
 * <b>Session Negotiation </b> <br>
 * There are special rules for message bodies of Request and Responses that
 * contain a session description. SIP uses an offer/answer model where one User
 * Agent sends a session description, called the offer, which contains a
 * proposed description of the session. The other User Agent responds with
 * another session description, called the answer, which indicates which
 * communications means are accepted. In this specification, offers and answers
 * can only appear in INVITE requests and Responses, and ACK. The Session
 * Description Protocol (SDP) <a href =
 * "http://www.ietf.org/rfc/rfc2327.txt">RFC2327 </a> MUST be supported by all
 * user agents as a means to describe sessions, and its usage for constructing
 * offers and answers MUST follow the procedures defined in <a href =
 * "http://www.ietf.org/rfc/rfc3261.txt">RFC3261 </a>. The SDP protocol is
 * described in Java by <a href = "http://www.jcp.org/en/jsr/detail?id=141">JSR
 * 141 </a>
 *
 * @see SipProvider
 * @see RequestEvent
 * @see ResponseEvent
 * @see TimeoutEvent
 * @see IOExceptionEvent
 * @see TransactionTerminatedEvent
 * @see DialogTerminatedEvent
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface SipListener extends EventListener {

    /**
     * Processes a Request received on a SipProvider upon which this SipListener
     * is registered.
     * <p>
     * <b>Handling Requests: </b> <br>
     * When the application receives a RequestEvent from the SipProvider the
     * RequestEvent may or may not belong to an existing dialog of the
     * application. The application can be determine if the RequestEvent belongs
     * to an existing dialog by checking the server transaction of the
     * RequestEvent.
     * <ul>
     * <li>If the server transaction equals <code>null</code> the
     * RequestEvent does not belong to an existing dialog and the application
     * must determine how to handle the RequestEvent. If the application decides
     * to forward the Request statelessly no transactional support is required
     * and it can simply pass the Request of the RequestEvent as an argument to
     * the {@link SipProvider#sendRequest(Request)}method. However if the
     * application determines to respond to a Request statefully it must request
     * a new server transaction from the
     * {@link SipProvider#getNewServerTransaction(Request)}method and use this
     * server transaction to send the Response based on the content of the
     * Request. If the SipProvider throws TransactionAlreadyExistsException when
     * the application requests a new server transaction to handle a Request the
     * current RequestEvent is a retransmission of the initial request from
     * which the application hadn't requested a server transaction to handle it,
     * i.e. this exception handles the race condition of an application
     * informing the SipProvider that it will handle a Request and the receipt
     * of a retransmission of the Request from the network to the SipProvider.
     * <li>If the server transaction <b>does NOT </b> equal <code>null</code>
     * the application determines its action to the RequestEvent based on the
     * content of the Request information.
     * </ul>
     * <p>
     * <b>User Agent Server (UAS) Behaviour: </b> <br>
     * A UAS application decides whether to accept the an invitation from a UAC.
     * The UAS application can accept the invitation by sending a 2xx response
     * to the UAC, a 2xx response to an INVITE transaction establishes a
     * session. For 2xx responses, the processing is done by the UAS
     * application, to guarantee the three way handshake of an INVITE
     * transaction. This specification defines a utility thats enables the
     * SipProvider to handle the 2xx processing for an INVITE transaction, see
     * the {@link SipStack#isRetransmissionFilterActive()}method. If the
     * invitation is not accepted, a 3xx, 4xx, 5xx or 6xx response is sent by
     * the application, depending on the reason for the rejection. Alternatively
     * before sending a final response, the UAS can also send provisional
     * responses (1xx) to advise the UAC of progress in contacting the called
     * user. A UAS that receives a CANCEL request for an INVITE, but has not yet
     * sent a final response, would "stop ringing" and then respond to the
     * INVITE with a specific 487 Error response.
     * <p>
     * <b>General Proxy behaviour: </b> <br>
     * In some circumstances, a proxy application MAY forward requests using
     * stateful transports without being transaction stateful, i.e. using the
     * {@link SipProvider#sendRequest(Request)}method, but using TCP as a
     * transport. For example, a proxy application MAY forward a request from
     * one TCP connection to another transaction statelessly as long as it
     * places enough information in the message to be able to forward the
     * response down the same connection the request arrived on. This is the
     * responsibility of the application and not the SipProvider. Requests
     * forwarded between different types of transports where the proxy
     * application takes an active role in ensuring reliable delivery on one of
     * the transports must be forwarded using the stateful send methods on the
     * SipProvider.
     * <p>
     * <b>Stateful Proxies: </b> <br>
     * A stateful proxy MUST create a new server transaction for each new
     * request received, either automatically generated by the SipProvider, if
     * the request matches an existing dialog or by the an application call on
     * the SipProvider if it decides to respond to the request statefully. The
     * proxy application determines where to route the request, choosing one or
     * more next-hop locations. An outgoing request for each next-hop location
     * is processed by its own associated client transaction. The proxy
     * application collects the responses from the client transactions and uses
     * them to send responses to the server transaction. When an application
     * receives a CANCEL request that matches a server transaction, a stateful
     * proxy cancels any pending client transactions associated with a response
     * context. A stateful proxy responds to the CANCEL rather than simply
     * forwarding a response it would receive from a downstream element.
     * <p>
     * For all new Requests, including any with unknown methods, an element
     * intending to stateful proxy the Request determines the target(s) of the
     * request. A stateful proxy MAY process the targets in any order. A
     * stateful proxy must have a mechanism to maintain the target set as
     * responses are received and associate the responses to each forwarded
     * request with the original request. For each target, the proxy forwards
     * the request following these steps:
     * <ul>
     * <li>Make a copy of the received request.
     * <li>Update the Request-URI.
     * <li>Update the Max-Forwards header.
     * <li>Optionally add a Record-route header.
     * <li>Optionally add additional headers.
     * <li>Postprocess routing information.
     * <li>Determine the next-hop address, port, and transport.
     * <li>Add a Via header.
     * <li>Add a Content-Length header if necessary.
     * <li>Forward the new request using the
     * {@link ClientTransaction#sendRequest()}method.
     * <li>Process all responses recieved on the
     * {@link SipListener#processResponse(ResponseEvent)}method.
     * <li>NOT generate 100 (Trying) responses to non-INVITE requests.
     * </ul>
     * <p>
     * A stateful proxy MAY transition to stateless operation at any time during
     * the processing of a request, as long as it did nothing that would prevent
     * it from being stateless initially i.e. forking or generation of a 100
     * response. When performing such a transition, any state already stored is
     * simply discarded.
     * <p>
     * <b>Forking Requests: </b> <br>
     * A stateful proxy application MAY choose to "fork" a request, routing it
     * to multiple destinations. Any request that is forwarded to more than one
     * location MUST be forwarded using the stateful send methods on the
     * SipProvider.
     * <p>
     * <b>Stateless Proxies: </b> <br>
     * As a stateless proxy does not have any notion of a transaction, or of the
     * response context used to describe stateful proxy behavior,
     * <code>requestEvent.getServerTransaction() == null;</code> always return
     * <var>true </var>. The transaction layer of the SipProvider implementation
     * is by-passed. For all requests including any with unknown methods, an
     * application intending to stateless proxy the request MUST:
     * <ul>
     * <li>Validate the request.
     * <li>Preprocess routing information.
     * <li>Determine a single target(s) for the request.
     * <li>Forward the request to the target using the
     * {@link SipProvider#sendRequest(Request)}method.
     * <li>NOT perform special processing for CANCEL requests.
     * </ul>
     *
     * @param requestEvent -
     *            requestEvent fired from the SipProvider to the SipListener
     *            representing a Request received from the network.
     */
    public void processRequest(RequestEvent requestEvent);

    /**
     * Processes a Response received on a SipProvider upon which this
     * SipListener is registered.
     * <p>
     * <b>Handling Responses: </b> <br>
     * When the application receives a ResponseEvent from the SipProvider the
     * ResponseEvent may or may not correlate to an existing Request of the
     * application. The application can be determine if the ResponseEvent
     * belongs to an existing Request by checking the client transaction of the
     * ResponseEvent.
     * <ul>
     * <li>If the the client transaction equals <code>null</code> the
     * ResponseEvent does not belong to an existing Request and the Response is
     * considered stray, i.e. stray response can be identitied, if
     * <code>responseEvent.getClientTransaction() == null;</code>. Handling
     * of these "stray" responses is dependent on the application i.e. a proxy
     * will forward them statelessly using the
     * {@link SipProvider#sendResponse(Response)}method, while a User Agent
     * will discard them.
     * <li>If the client transaction <b>does NOT </b> equal <code>null</code>
     * the application determines it action to the ResponseEvent based on the
     * content of the Response information.
     * </ul>
     * <p>
     * <b>User Agent Client (UAC) behaviour: </b> <br>
     * After possibly receiving one or more provisional responses (1xx) to a
     * Request, the UAC will get one or more 2xx responses or one non-2xx final
     * response. Because of the protracted amount of time it can take to receive
     * final responses to an INVITE, the reliability mechanisms for INVITE
     * transactions differ from those of other requests. A UAC needs to send an
     * ACK for every final Response it receives, however the procedure for
     * sending the ACK depends on the type of Response. For final responses
     * between 300 and 699, the ACK processing is done by the transaction layer
     * i.e. handled by the implementation. For 2xx responses, the ACK processing
     * is done by the UAC application, to guarantee the three way handshake of
     * an INVITE transaction. This specification defines a utility thats enables
     * the SipProvider to handle the ACK processing for an INVITE transaction,
     * see the {@link SipStack#isRetransmissionFilterActive()}method. <br>
     * A 2xx response to an INVITE establishes a session, and it also creates a
     * dialog between the UAC that issued the INVITE and the UAS that generated
     * the 2xx response. Therefore, when multiple 2xx responses are received
     * from different remote User Agents, i.e. the INVITE forked, each 2xx
     * establishes a different dialog and all these dialogs are part of the same
     * call. If an INVITE client transaction returns a {@link TimeoutEvent}
     * rather than a response the UAC acts as if a 408 (Request Timeout)
     * response had been received from the UAS.
     * <p>
     * <b>Stateful Proxies: </b> <br>
     * A proxy application that handles a response statefully must do the
     * following processing:
     * <ul>
     * <li>Find the appropriate response context.
     * <li>Remove the topmost Via header.
     * <li>Add the response to the response context.
     * <li>Check to determine if this response should be forwarded immediately.
     * <li>When necessary, choose the best final response from the response
     * context. If no final response has been forwarded after every client
     * transaction associated with the response context has been terminated, the
     * proxy must choose and forward the "best" response from those it has seen
     * so far.
     * </ul>
     * <p>
     * Additionally the following processing MUST be performed on each response
     * that is forwarded.
     * <ul>
     * <li>Aggregate authorization header values if necessary.
     * <li>Optionally rewrite Record-Route header values.
     * <li>Forward the response using the
     * {@link ServerTransaction#sendResponse(Response)}method.
     * <li>Generate any necessary CANCEL requests.
     * </ul>
     * <p>
     * <b>Stateless Proxies: </b> <br>
     * As a stateless proxy does not have any notion of transactions, or of the
     * response context used to describe stateful proxy behavior,
     * <code>responseEvent.getClientTransaction == null;</code> always return
     * <var>true </var>. Response processing does not apply, the transaction
     * layer of the SipProvider implementation is by-passed. An application
     * intending to stateless proxy the Response MUST:
     * <ul>
     * <li>Inspect the sent-by value in the first Via header.
     * <li>If that address matches the proxy, the proxy MUST remove that header
     * from the response.
     * <li>Forward the resulting response to the location indicated in the next
     * Via header using the {@link SipProvider#sendResponse(Response)}method.
     * </ul>
     *
     * @param responseEvent -
     *            the responseEvent fired from the SipProvider to the
     *            SipListener representing a Response received from the network.
     */
    public void processResponse(ResponseEvent responseEvent);

    /**
     * Processes a retransmit or expiration Timeout of an underlying
     * {@link Transaction}handled by this SipListener. This Event notifies the
     * application that a retransmission or transaction Timer expired in the
     * SipProvider's transaction state machine. The TimeoutEvent encapsulates
     * the specific timeout type and the transaction identifier either client or
     * server upon which the timeout occured. The type of Timeout can by
     * determined by:
     * <code>timeoutType = timeoutEvent.getTimeout().getValue();</code>
     *
     * @param timeoutEvent -
     *            the timeoutEvent received indicating either the message
     *            retransmit or transaction timed out.
     */
    public void processTimeout(TimeoutEvent timeoutEvent);

    /**
     * Process an asynchronously reported IO Exception. Asynchronous IO
     * Exceptions may occur as a result of errors during retransmission of
     * requests. The transaction state machine requires to report IO Exceptions
     * to the application immediately (according to RFC 3261). This method
     * enables an implementation to propagate the asynchronous handling of IO
     * Exceptions to the application.
     *
     * @since v1.2
     * @param exceptionEvent --
     *          The Exception event that is reported to the application.
     */
    public void processIOException(IOExceptionEvent exceptionEvent);


    /**
     * Process an asynchronously reported TransactionTerminatedEvent.
     * When a transaction transitions to the Terminated state, the stack
     * keeps no further records of the transaction. This notification can be used by
     * applications to clean up any auxiliary data that is being maintained
     * for the given transaction.
     *
     * @param transactionTerminatedEvent -- an event that indicates that the
     *       transaction has transitioned into the terminated state.
     * @since v1.2
     */
    public void processTransactionTerminated(TransactionTerminatedEvent
            transactionTerminatedEvent);


    /**
     * Process an asynchronously reported DialogTerminatedEvent.
     * When a dialog transitions to the Terminated state, the stack
     * keeps no further records of the dialog. This notification can be used by
     * applications to clean up any auxiliary data that is being maintained
     * for the given dialog.
     *
     * @param dialogTerminatedEvent -- an event that indicates that the
     *       dialog has transitioned into the terminated state.
     * @since v1.2
     */
    public void processDialogTerminated(DialogTerminatedEvent
                                        dialogTerminatedEvent);

}

