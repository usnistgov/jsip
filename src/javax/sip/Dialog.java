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
 * File Name     : Dialog.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *  1.2     05/04/2005  M. Rangnathan       Added Prack Support
 *                      Phelim O'Doherty    Added createAck method
 *                      M. Ranganathan      Clarified retransmission behavior.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import javax.sip.address.Address;
import javax.sip.message.Request;
import javax.sip.message.Response;

import java.text.ParseException;
import java.util.Iterator;
import javax.sip.header.CallIdHeader;
import java.io.Serializable;

/**
 * A dialog represents a peer-to-peer SIP relationship between two user agents
 * that persists for some time. Dialogs are typically used by user agents to
 * facilitate management of state. Dialogs are typically not relevant to proxy
 * servers. The dialog facilitates sequencing of messages between the user
 * agents and proper routing of requests between both of them. The dialog
 * represents a context in which to interpret SIP Transactions and Messages.
 * However, a Dialog is not necessary for message processing.
 * <p>
 * A dialog is identified at each User Agent with a dialog Id, which consists of
 * a Call-Id value, a local tag and a remote tag. The dialog Id at each User
 * Agent involved in the dialog is not the same. Specifically, the local tag at
 * one User Agent is identical to the remote tag at the peer User Agent. The
 * tags are opaque tokens that facilitate the generation of unique dialog Ids.
 * <p>
 * A dialog contains certain pieces of data needed for further message
 * transmissions within the dialog. This data consists of:
 * <ul>
 * <li> Dialog Id - used to identify the dialog.
 * <li> Local sequence number - used to order requests from the User Agent to
 * its peer.
 * <li> Remote sequence number - used to order requests from its peer to the
 * User Agent.
 * <li> Local URI - the address of the local party.
 * <li> Remote URI - the address of the remote party.
 * <li> Remote target - the address from the Contact header field of the request
 * or response or refresh request or response.
 * <li> "secure" boolean - determines if the dialog is secure i.e. use the
 * <var>sips:</var> scheme.
 * <li> Route set - an ordered list of URIs. The route set is the list of
 * servers that need to be traversed to send a request to the peer.
 * </ul>
 * A dialog also has its own state machine, the current {@link DialogState} is
 * determined by the sequence of messages that occur on the initial dialog.
 * <p>
 * <b>Invite Dialog States:</b><br>
 * Null --> Early --> Confirmed --> Terminated
 * <p>
 * <b>Other Dialog-creating Requests
 * Dialog States (ie. SUBSCRIBE):</b><br>
 * Null --> Confirmed --> Terminated.
 * <p>
 * The Listener MUST ACK the 2xx response associated with an INVITE dialog.
 * Retransmissions of the ACK in response to subsequent INVITE requests are
 * handled by the dialog layer. If a listener does not ACK a 2XX response
 * immediately, the implementation will terminate the Dialog automatically and
 * remove it from the stack when the listener completes its execution.
 * <p>
 * The dialog layer MUST respond with an automatic error response when <a href =
 * "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a> specifies that the User
 * Agent MUST respond with a certain error. This does not apply for successful
 * responses like 200 OK. The error condition relates directly to the dialog
 * layer and the SipListener can add no meaningful information to the response.
 * Such a response can be constructed and sent entirely by the dialog layer on
 * behalf of the SipListener. Under such circumstances, it makes sense to let
 * the dialog support handle the response and not bother the SipListener with
 * it. The SipListener will not be notified with the RequestEvent upon which the
 * Response was sent on its behalf by the Dialog layer. However, if no Dialog is
 * registered for the transaction either automatically or under application
 * control, then the SipListener is expected to generate and send the
 * appropriate (error) response.
 *
 * <p>
 * <b>For example:</b> In <a href =
 * "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a> Chapter 14 a User Agent
 * Server that receives a second INVITE before it sends the final response to a
 * first INVITE with a lower CSeq sequence number on the same dialog MUST return
 * a 500 (Server Internal Error) response to the second INVITE and MUST include
 * a Retry-After header field with a randomly chosen value of between 0 and 10
 * seconds.
 *
 * In this release of this specification, the implementation handles retransmissions of
 * Responses that are specified as being handled by the UA core, if there is an
 * associated Dialog for a given transaction.
 * If there is no associated Dialog for a given transaction, the application
 * will be alerted to perform the retransmissions required by the UA core if
 * desired. Applications should explicitly request such alerts see
 * {@link ServerTransaction#enableRetransmissionAlerts()}. Once enabled the
 * SipProvider will deliver retransmission timer
 * events to the Listener with a {@link Timeout#RETRANSMIT} notification. The
 * SipListener can then retransmit the Response as necessary, see
 * {@link SipListener#processTimeout(TimeoutEvent)}.
 * <p>
 * For INVITE Client Transactions:
 * <ul>
 * <li>UAC ACKs first 2xx response </li>
 * <li>Additional 2xx responses  will be ACKed automatically if a Dialog is
 *  asssociated with the response</li>
 * <li>Additional 2xx responses will be presented to the listener if no dialog
 * is associated with the response. In such a case the listener is expected to
 * ACK the 2xx response.
 * </ul>
 *
 * For INVITE Server Transaction 2xx response:
 * <ul>
 * <li> Application sends first 2xx response {@link ServerTransaction#sendResponse(Response)}.
 * <li> If a Dialog is not associated with a ServerTransaction, and if the
 * application explicitly requests to be notified of such timeouts (see
 * {@link ServerTransaction#enableRetransmissionAlerts()} the SipListener is
 * periodically alerted to retransmit 2xx responses for the ServerTransaction.</li>
 * <li> If a Dialog is associated with a ServerTransaction then the
 * implementation takes care of retransmitting final responses until the ACK is
 * received i.e. the SipListener will receive no notifications to retransmit
 * responses.
 * </ul>
 *
 * For an INVITE server Transaction 300-699 response:
 * <ul>
 * <li> Either the application or the dialog layer sends the first response
 * <li> Retransmissions of 300-699 responses from the INVITE Server Transaction
 * are handled by the transaction layer independent of existence of an
 * associated Dialog.
 * </ul>
 *
 * Because reliable provisional responses are always sent by the Dialog layer,
 * the Dialog layer takes care of retrasmitting these. The application is never
 * alerted for retransmissions of Reliable Provisional responses:
 * <ul>
 * <li> The application sends a reliable provisional Response to an Invite
 * Dialog using {@link javax.sip.Dialog#sendReliableProvisionalResponse(Response) }
 * <li> The Stack takes care of retransmitting the provisional response with
 * exponentially increasing intervals until a PRACK is received or the
 * Transaction times out.
 * </ul>
 *
 * Processing Forking INVITES:
 * <p>
 * Multiple 2xx responses may arrive at the UAC for a single INVITE request due
 * to a forking proxy. Each response is distinguished by the tag parameter in
 * the TO header field, and each represents a distinct Dialog, with a distinct
 * Dialog identifier. In this case the first 2xx terminates the original INVITE
 * additional 2xx responses will be presented to the SipListener as a ResponseEvent
 * with null Client Transaction ID but with a valid and distinct Dialog. The
 * Listener is expected to ACK the 2xx response - otherwise the Dialog is
 * terminated after a timeout period. Note that unless automatic dialog creation
 * is explictly disabled, the 2xx ResponseEvent will always contain a Dialog whether
 * or not the outgoing INVITE was sent through a ClientTransaction or statelessly
 * via a SipProvider.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface Dialog extends Serializable {

    /**
     * Returns the Address identifying the local party. This is the value of the
     * From header of locally initiated requests in this dialog when acting as
     * an User Agent Client.
     * <p>
     * This is the value of the To header of received responses in this dialog
     * when acting as an User Agent Server.
     *
     * @return the address object of the local party.
     */
    public Address getLocalParty();

    /**
     * Returns the Address identifying the remote party. This is the value of
     * the To header of locally initiated requests in this dialog when acting as
     * an User Agent Client.
     * <p>
     * This is the value of the From header of recieved responses in this dialog
     * when acting as an User Agent Server.
     *
     * @return the address object of the remote party.
     */
    public Address getRemoteParty();

    /**
     * Returns the Address identifying the remote target. This is the value of
     * the Contact header of received Responses for target refresh Requests in
     * this dialog when acting as an User Agent Client.
     * <p>
     * This is the value of the Contact header of received target refresh
     * Requests Requests in this dialog when acting as an User Agent Server.
     *
     * @return the address object of the remote target.
     */
    public Address getRemoteTarget();

    /**
     * Get the dialog identifier of this dialog. A dialog Id is associated with
     * all responses and with any request that contains a tag in the To field.
     * The rules for computing the dialog Id of a message depends on whether the
     * SIP element is a User Agent Client or User Agent Server and applies to
     * both requests and responses.
     * <ul>
     * <li>User Agent Client - the Call-Id value of the dialog Id is set to the
     * Call-Id of the message, the remote tag is set to the tag in the To field
     * of the message, and the local tag is set to the tag in the From field of
     * the message.
     * <li>User Agent Server - the Call-Id value of the dialog Id is set to the
     * Call-Id of the message, the remote tag is set to the tag in the From
     * field of the message, and the local tag is set to the tag in the To field
     * of the message.
     * </ul>
     *
     * @return the string identifier for this dialog.
     */
    public String getDialogId();

    /**
     * Returns the Call-Id for this dialog. This is the value of the Call-Id
     * header for all messages belonging to this session.
     *
     * @return the Call-Id for this dialog
     */
    public CallIdHeader getCallId();

    /**
     * The local sequence number is used to order requests from this User Agent
     * Client to its peer User Agent Server. The local sequence number MUST be
     * set to the value of the sequence number in the CSeq header field of the
     * request. The remote sequence number MUST be empty as it is established
     * when the remote User Agent sends a request within the dialog.
     * <p>
     * Requests within a dialog MUST contain strictly monotonically increasing
     * and contiguous CSeq sequence numbers (increasing-by-one) in each
     * direction (excepting ACK and CANCEL, whose numbers equal the requests
     * being acknowledged or cancelled). Therefore, if the local sequence number
     * is not empty, the value of the local sequence number MUST be incremented
     * by one, and this value MUST be placed into the CSeq header field. If the
     * local sequence number is empty, an initial value MUST be chosen.
     *
     * @deprecated This method is replaced with {@link #getLocalSeqNumber()} with
         * type long.
     * @see #getLocalSeqNumber()
     *
     * @return the integer value of the local sequence number, returns zero if
     *         not set.
     */
    public int getLocalSequenceNumber();

    /**
     * The local sequence number is used to order requests from this User Agent
     * Client to its peer User Agent Server. The local sequence number MUST be
     * set to the value of the sequence number in the CSeq header field of the
     * request. The remote sequence number MUST be empty as it is established
     * when the remote User Agent sends a request within the dialog.
     * <p>
     * Requests within a dialog MUST contain strictly monotonically increasing
     * and contiguous CSeq sequence numbers (increasing-by-one) in each
     * direction (excepting ACK and CANCEL, whose numbers equal the requests
     * being acknowledged or cancelled). Therefore, if the local sequence number
     * is not empty, the value of the local sequence number MUST be incremented
     * by one, and this value MUST be placed into the CSeq header field. If the
     * local sequence number is empty, an initial value MUST be chosen.
     *
     * @return the value of the local sequence number, returns zero if
     *         not set.
         * @since v1.2
     */
    public long getLocalSeqNumber();

    /**
     * The remote sequence number is used to order requests from its peer User
     * Agent Client to this User Agent Server. When acting an User Agent Server
     * the remote sequence number MUST be set to the value of the sequence
     * number in the CSeq header field of the request from the User Agent
     * Client. The local sequence number MUST be empty.
     * <p>
     * If the remote sequence number is empty, it MUST be set to the value of
     * the sequence number in the CSeq header field value in the request. If the
     * remote sequence number was not empty, but the sequence number of the
     * request is lower than the remote sequence number, the request is out of
     * order and MUST be rejected with a 500 (Server Internal Error) response.
     * If the remote sequence number was not empty, and the sequence number of
     * the request is greater than the remote sequence number, the request is in
     * order.
     *
     * @deprecated This method is replaced with {@link #getRemoteSeqNumber()} with
         * type long.
     * @see #getRemoteSeqNumber()
     * @return the integer value of the remote sequence number, return zero if
     *         not set.
     */
    public int getRemoteSequenceNumber();

    /**
     * The remote sequence number is used to order requests from its peer User
     * Agent Client to this User Agent Server. When acting an User Agent Server
     * the remote sequence number MUST be set to the value of the sequence
     * number in the CSeq header field of the request from the User Agent
     * Client. The local sequence number MUST be empty.
     * <p>
     * If the remote sequence number is empty, it MUST be set to the value of
     * the sequence number in the CSeq header field value in the request. If the
     * remote sequence number was not empty, but the sequence number of the
     * request is lower than the remote sequence number, the request is out of
     * order and MUST be rejected with a 500 (Server Internal Error) response.
     * If the remote sequence number was not empty, and the sequence number of
     * the request is greater than the remote sequence number, the request is in
     * order.
     *
     *
     * @return the value of the remote sequence number, return zero if
     *         not set.
         * @since v1.2
     */
    public long getRemoteSeqNumber();

    /**
     * When acting as an User Agent Server the routeset MUST be set to the list
     * of URIs in the Record-Route header field from the request, taken in order
     * and preserving all URI parameters. When acting as an User Agent Client
     * the route set MUST be set to the list of URIs in the Record-Route header
     * field from the response, taken in reverse order and preserving all URI
     * parameters. If no Record-Route header field is present in the request or
     * response, the route set MUST be set to the empty set. This route set,
     * even if empty, overrides any pre-existing route set for future requests
     * in this dialog.
     * <p>
     * Requests within a dialog MAY contain Record-Route and Contact header
     * fields. However, these requests do not cause the dialog's route set to be
     * modified.
     * <p>
     * The User Agent Client uses the remote target and route set to build the
     * Request-URI and Route header field of the request.
     *
     * @return an Iterator over a list of route headers to be used for
     *         forwarding.
     */
    public Iterator getRouteSet();

    /**
     * Returns true if this Dialog is secure, for example if the request was sent over a
     * "sips:" scheme, or a "sip:" scheme over TLS.
     *
     * @return <code>true</code> if this dialog was secure, and
     *         <code>false</code> otherwise.
     */
    public boolean isSecure();

    /**
     * Returns whether this Dialog is a server dialog, for example this side
         * was initially acting as a UAS.
     *
     * @return <code>true</code> if this is a server dialog and
     *         <code>false</code> if it is a client dialog.
     */
    public boolean isServer();

    /**
     * This method may be used to increment the local sequence number of the
     * dialog when an application wishes to switch from dialog stateful mode to
     * transaction stateful mode for client transactions and back again to
     * dialog stateful mode. Note, the Dialog layer automatically increments the
     * local sequence number when a request is sent out via the Dialog. However
     * in special circumstances applications may wish to send a request (from a
     * sequence of dialog requests) outside of the Dialog using the
     * {@link ClientTransaction#sendRequest()} method. When sending a request
     * using the Transaction the Dialog state is unaffected.
     */
    public void incrementLocalSequenceNumber();

    /**
     * Creates a new Request message based on the dialog creating request. This
     * method should be used for but not limited to creating Bye's, Refer's and
     * re-Invite's on the Dialog. The returned Request will be correctly
     * formatted that is the Dialog implementation is responsible for assigning
     * the following:
     * <ul>
     * <li> RequestURI
     * <li> FromHeader
     * <li> ToHeader
     * <li> CallIdHeader
     * <li> RouteHeaders
     * </ul>
     * The CSeqHeader will be set when the message is sent. If this method
     * returns a CSeqHeader in the Request it may be overwritten again by the
     * {@link Dialog#sendRequest(ClientTransaction)} method. Therefore any
     * Request created by this method must be sent via the
     * {@link Dialog#sendRequest(ClientTransaction)} method and not via its
     * {@link ClientTransaction#sendRequest()} method.
     * <p>
     * All other headers including any Authentication related headers, and
     * record route headers should be assigned by the application to the
     * generated request. The assignment of the topmost via header for the
     * outgoing request may be deferred until the application creates a
     * ClientTransaction to send the request out. This method does not increment
     * the dialog sequence number. This method may not be used for creating
     * PRACK or ACK. Implementations should throw SipException if this method is
     * called for creating ACK or PRACK.
     *
     * @param method
     *            the string value that determines if the request to be created.
     * @return the newly created Request message on this Dialog.
     * @throws SipException
     *             if the Dialog is not yet established (i.e. dialog state
     *             equals null) or is terminated or if the method is invoked for
     *             ACK or PRACK.
     *
     */
    public Request createRequest(String method) throws SipException;

    /**
     * Creates a new reliable provisional response based on an Invite request
     * that created this Dialog or that is now refreshing this Dialog. This new
     * Response does not contain a body. Only the required headers are copied
     * from the Request. The response will include also the following headers:
     * <ul>
     * <li> RequireHeader
     * <li> RSeqHeader
     * </ul>
     * The RequireHeader will include the 100rel option tag. The Dialog will
     * handle the RSeq count that will be updated and icreased if needed when
     * the reliable provisional response is sent. Any Response created by this
     * method must be sent via the
     * {@link Dialog#sendReliableProvisionalResponse( Response)} method.
     * <p>
     *
     * @since 1.2
     *
     * @param statusCode
     *            the new integer of the statusCode value of this Message.
     * @return the newly created Response object.
     * @throws InvalidArgumentException
     *             when an invalid status code or request method is supplied.
     * @throws SipException when the Dialog is not a server dialog or if this
     *         method is called after a final response is sent to the
     *         ServerTransactin that created the Dialog.
     *
     */
    public Response createReliableProvisionalResponse(int statusCode)
            throws InvalidArgumentException, SipException;

    /**
     * Sends a Request to the remote party of this dialog. When an application
     * wishes to send a Request message on this dialog, it creates a Request and
     * creates a new ClientTransaction to handle this request from
     * {@link SipProvider#getNewClientTransaction(Request)}. This
     * ClientTransaction is passed to this method to send the request. The
     * Request message gets sent via the ListeningPoint information of the
     * SipProvider that is associated to this ClientTransaction.
     * <p>
     * This method implies that the application is functioning as UAC hence the
     * underlying SipProvider acts statefully. This method is useful for sending
     * Bye's to terminate a dialog or Re-Invites/Refers on the Dialog for third
     * party call control, call hold etc.
     * <p>
     * This method will set the From and the To tags for the outgoing request.
     * This method increments the dialog sequence number and sets the correct
     * sequence number to the outgoing Request and associates the client
     * transaction with this dialog. Note that any tags assigned by the user
     * will be over-written by this method. If the caller sets no RouteHeader in
     * the Request to be sent out, the implementation of this method will add
     * the RouteHeader from the routes that are mantained in the dialog. If the
     * caller sets the RouteHeader's, the implementation will leave the route
     * headers unaltered. This allows the application to manage its own route
     * set if so desired.
     * <p>
     * The User Agent traditionally must not send a BYE on a confirmed INVITE
     * until it has received an ACK for its 2xx response or until the server
     * transaction timeout is received.
     * <p>
     * A Dialog may be created by an INVITE request and subsequently
     * SUBSCRIBE/NOTIFY are sent withing that Dialog. In this case the
     * application may call {@link Dialog#terminateOnBye } to prevent the Dialog
     * from Terminating upon reciept of a BYE.
     *
     * @param clientTransaction -
     *            the new ClientTransaction object identifying this transaction,
     *            this clientTransaction should be requested from
     *            {@link SipProvider#getNewClientTransaction(Request)}
     * @throws TransactionDoesNotExistException
     *             if the clientTransaction does not correspond to any existing
     *             client transaction.
     * @throws SipException
     *             if implementation cannot send the Request for any reason.
     */
    public void sendRequest(ClientTransaction clientTransaction)
            throws TransactionDoesNotExistException, SipException;

    /**
     * Sends a reliable provisional response to the remote party of this dialog.
     * When an application wishes to send a reliable provisional response on the
     * Invite transaction that created this dialog or that is now refreshing
     * this dialog, it creates a reliable provisional response message with
     * {@link Dialog#createReliableProvisionalResponse(int)}. This Response is
     * passed to this method that sends it. This method will update the RSeq
     * header of the response if needed and will increase the RSeq count of the
     * Transaction. The application will not be able to send further reliable
     * provisional responses for this Dialog until PRACK is received for a
     * previously sent provisional response. The only response that can be sent
     * in parallel with an active reliable Provisional Response is a final
     * response. The Final response will stop retransmission of the reliable
     * responses.
     * <p>
     *
     * @since 1.2
     *
     * @param relResponse -
     *            the reliable provisional response
     * @throws SipException
     *             if implementation cannot send the Request for any reason
     *             (e.g. because another reliable response is still pending).
     */
    public void sendReliableProvisionalResponse(Response relResponse)
            throws SipException;

    /**
     * Creates a new PRACK Request message based on a reliable provisional
     * response received by this Dialog and the Dialog internal information. The
     * returned Request will be correctly formatted that is the Dialog
     * implementation is responsible for assigning the following:
     * <ul>
     * <li> RequestURI
     * <li> FromHeader
     * <li> ToHeader
     * <li> CallIdHeader
     * <li> RouteHeaders
     * <li> RAckHeader
     * </ul>
     * The CSeqHeader will be set when the message is sent. If this method
     * returns a CSeqHeader in the Request it may be overwritten again by the
     * {@link Dialog#sendRequest(ClientTransaction)} method. Therefore any
     * Request created by this method must be sent via the
     * {@link Dialog#sendRequest(ClientTransaction)} method and not via its
     * {@link ClientTransaction#sendRequest()} method.
     * <p>
     * All other headers including any Authentication related headers, and
     * record route headers should be assigned by the application to the
     * generated request. The assignment of the topmost via header for the
     * outgoing request may be deferred until the application creates a
     * ClientTransaction to send the request out. This method does not increment
     * the dialog sequence number.
     *
     * @param relResponse
     *            the reliable provisional response that should result in a
     *            prack reques.
     * @return the newly created Request message on this Dialog.
     * @throws SipException
     *             if the Method of the transaction that created the Dialog or
     *             Refeshing the Dialog is not an INVITE ( for example
     *             SUBSCRIBE).
     * @throws DialogDoesNotExistException
     *             if the Dialog is not yet established (i.e. dialog state
     *             equals null) or is terminated.
     * @since 1.2
     */
    public Request createPrack(Response relResponse)
            throws DialogDoesNotExistException, SipException;

    /**
     * Creates an ACK request for an Invite that was responded with 2xx
     * response. The cseq number for the invite is supplied to relate the ACK to
     * its original invite request.
     *
     * @param cseq -
     *            the CSeq number to be placed in the ACK request.
     * @return The newly created ACK request message.
     * @throws InvalidArgumentException
     *             if there is a problem with the supplied cseq ( for example <=
     *             0 ).
     * @throws SipException
     *             if the cseq does not relate to a previously sent INVITE or if
     *             the Method that created the Dialog is not an INVITE ( for
     *             example SUBSCRIBE)
     * @since 1.2
     */
    public Request createAck(long cseq) throws InvalidArgumentException,
            SipException;

    /**
     * Sends ACK Request to the remote party of this dialog. This method implies
     * that the application is functioning as User Agent Client hence the
     * underlying SipProvider acts statefully. This method does not increment
     * the local sequence number.
     *
     * @param ackRequest -
     *            the new ACK Request message to send.
     * @throws SipException
     *             if implementation cannot send the ACK Request for any reason
     */
    public void sendAck(Request ackRequest) throws SipException;

    /**
     * Returns the current DialogState of the dialog or null. A dialog that is
     * created but not yet mapped to any state must return null, multiple
     * requests can be generated on the Dialog in a null state. The dialog
     * states for INVITE transaction are:
     * <ul>
     * <li> Early - A dialog is in the "early" state, which occurs when it is
     * created when a provisional response is recieved to the INVITE Request.
     * <li> Confirmed - A dialog transitions to the "confirmed" state when a 2xx
     * final response is received to the INVITE Request.
     * <li> Terminated - A dialog transitions to the "terminated" state for all
     * other reasons or if no response arrives at all on the dialog.
     * </ul>
     *
     * A Subscibe/Refer dialog has the following states:
     * <ul>
     * <li> Null - refers to a state that is not terminated.
     * <li> Terminated - when the Application deletes the Dialog or if no
     * response arrives at all.
     * </ul>
     *
     * Independent of the method, if a request outside of a dialog generates a
     * non-2xx final response, any early dialogs created through provisional
     * responses to that request are "terminated". If no response arrives at all
     * on the early dialog it is also "terminated".
     *
     * @return a DialogState determining the current state of the dialog.
     * @see DialogState
     */
    public DialogState getState();

    /**
     * This method will release all resources associated with this dialog that
     * are tracked by the SipProvider. Further references to the dialog by
     * incoming messages will result in a mismatch. This delete method is
     * provided methods that do not expect a BYE to terminate a dialog. Such is
     * the case with SUBSCRIBE/NOTIFY within a Dialog that is created with an
     * INIVTE.
         *
     */
    public void delete();

    /**
     * This method retrieves the transaction which resulted in the creation of
     * this Dialog. The transaction type either server or client can be
     * determined based on whether this is a server or client Dialog, see
     * {@link Dialog#isServer()}.
     *
     * @deprecated Since v1.2. Reduces the amount of state that
         * the stack needs to keep track of.
     * @return the Transaction that created the Dialog.
     */
    public Transaction getFirstTransaction();

    /**
     * Get the Local Tag of this Dialog. On the client side, this tag is
     * assigned to outgoing From headers for Requests within the dialog and To
     * headers for responses within the dialog. On the server side, this tag is
     * associated with outgoing To headers for responses within the dialog.
     */
    public String getLocalTag();

    /**
     * Gets the Remote Tag of this Dialog. On the client side, this tag is
     * associated with outgoing To headers for Requests within the dialog. On
     * the server side, this tag is associated with incoming From headers for
     * requests within the dialog.
     */
    public String getRemoteTag();

    /**
     * Sets application specific data to this dialog. This specification does
     * not define the format of this data. This is the responsibility of the
     * application and is dependent upon the application. This method can be
     * used to link the call state of this dialog to other state, SIP or
     * otherwise in the system. For example this method could be used by a
     * SIP-to-H323 interworking node that would associate the H323 call state
     * associated with a call on the H323 side with this dialog that represents
     * this call on the SIP side. Or a dialog stateful proxy can associate the
     * UAS dialog to the UAC dialog and vice versa.
     *
     * @param applicationData
     *            the new object containing application specific data.
     */
    public void setApplicationData(Object applicationData);

    /**
     * Gets the application specific data specific to this dialog. This
     * specification does not define the format of this application specific
     * data. This is the responsibility of the application.
     *
     * @return the object representation of the application specific data.
     */
    public Object getApplicationData();

    /**
     * Terminate Dialog on BYE. If this flag is set to true then the stack will
     * Terminate the dialog automatically when BYE is received. This is the
     * default behavior. This method is useful for SUBSCRIBE/NOTIFY processing
     * within a Dialog. In the case that several subscriptions are associated
     * with a single Dialog, the Dialog does not terminate until all the
     * subscriptions in it are destroyed. Hence, if the application is aware of
     * active subscriptions in a dialog it should set this flag to false. In
     * this case when BYE is received the dialog will not be terminated and it
     * will be the application’s responsibility to call the
     * {@link Dialog#delete()} function when all active subscriptions are
     * terminated.
     *
     * @param terminateFlag --
     *            if true then the dialog is terminated when a BYE is received.
     * @throws SipException --
     *            if the dialog is already terminated.
         * @since 1.2
     *
     */
    public void terminateOnBye(boolean terminateFlag) throws SipException;

}
