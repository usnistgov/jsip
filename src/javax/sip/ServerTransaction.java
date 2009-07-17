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
 * File Name     : ServerTransaction.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import javax.sip.message.Response;

/**
 * A server transaction is used by a SipProvider to handle incoming Request
 * messages to fire Request events to the SipListener on a specific server
 * transaction and by a User Agent Server application to send Response
 * messages to a User Agent Client application. This interfaces enables an
 * application to send a {@link javax.sip.message.Response} to a recently
 * received Request in a transaction stateful way.
 * <p>
 * A new server transaction is generated in the following ways:
 * <ul>
 * <li> By the application by invoking the
 * {@link SipProvider#getNewServerTransaction(Request)} for Requests that the
 * application wishes to handle.
 * <li> By the SipProvider by automatically populating the server transaction
 * of a RequestEvent for Incoming Requests that match an existing Dialog. Note
 * that a dialog-stateful application is automatically transaction
 * stateful too
 * </ul>
 * A server transaction of the transaction layer is represented by a finite
 * state machine that is constructed to process a particular request under
 * the covers of a stateful SipProvider. The transaction layer handles
 * application-layer retransmissions, matching of responses to requests, and
 * application-layer timeouts.
 * <p>
 * The server transaction Id must be unique within the underlying
 * implementation. This Id is commonly taken from the branch parameter in the
 * topmost Via header (for RFC3261 compliant clients), but may also be computed as a
 * cryptographic hash of the To tag, From tag, Call-ID header field, the
 * Request-URI of the request received (before translation), the topmost Via
 * header, and the sequence number from the CSeq header field, in addition to
 * any Proxy-Require and Proxy-Authorization header fields that may be present.
 * The algorithm used to determine the id is implementation-dependent.
 * <p>
 * For the detailed server transaction state machines refer to Chapter
 * 17 of <a href="http://www.ietf.org/rfc/rfc3261.txt">RFC 3261</a>, the
 * allowable transitions are summarized below:
 * <p>
 * <b>Invite Transaction:</b><br>
 * Proceeding --> Completed --> Confirmed --> Terminated
 * <p>
 * <b>Non-Invite Transaction:</b><br>
 * Trying --> Proceeding --> Completed --> Terminated
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public interface ServerTransaction extends Transaction {

    /**
     * Sends the Response to a Request which is associated with this
     * ServerTransaction. When an application wishes to send a Response, it
     * creates a Response using the {@link javax.sip.message.MessageFactory} and
     * then passes that Response to this method. The Response message gets sent out on
     * the network via the ListeningPoint information that is associated with
     * the SipProvider of this ServerTransaction.
     * <p>
     * This method implies that the application is functioning as either a UAS
     * or a stateful proxy, hence the underlying implementation acts statefully.
     * When a UAS sends a 2xx response to an INVITE, the server transaction is
     * transitions to the TerminatedState. The implementation may delay physically
     * removing ServerTransaction record from memory to catch retransmissions
     * of the INVITE in accordance with the reccomendation of
     * <a href="http://bugs.sipit.net/show_bug.cgi?id=769"> http://bugs.sipit.net/show_bug.cgi?id=769 </a>.
     *
     *
     * <p><b>ACK Processing and final response retransmission:</b> <br/>
     * If a Dialog is associated
     * with the ServerTransaction then when the UAC sends the ACK ( the typical case for User Agents),
     * the Application ( i.e. Listener )
     * will see a ServerTransaction corresponding to the ACK and the corresponding
     * {@link Dialog} presented to it. The ACK will  be presented to the Listener only
     * once in this case. Retransmissions of the OK and filtering of ACK retransmission
     * are the responsibility of the Dialog layer of this specification. However
     * if no {@link Dialog} is associated with the INVITE Transaction, the ACK will be presented
     * to the Application with a null Dialog in the {@link RequestEvent} and there will be
     * no Dialog associated with the ACK Transaction
     * (i.e. {@link Transaction#getDialog()} returns null).
     * In this case (when there is no Dialog associated with the original INVITE or ACK)
     * the Application is responsible for retransmission
     * of the OK for the INVITE if necessary (i.e. if it wants to manage its own dialog layer and
     * function as a User Agent) and for dealing with retransmissions of the ACK. This
     * requires that the three way handshake of an INVITE is managed by the UAS
     * application and not the implementation of this specification.
     *
     * <p>
     * Note that Responses created via {@link Dialog#createReliableProvisionalResponse(int)}
     * should be sent using {@link Dialog#sendReliableProvisionalResponse(Response)}
     *
     * @param response the Response to send to the Request.
     * @throws SipException if the SipProvider cannot send the Response for any
     * other reason.
     * @throws InvalidArgumentException if the Response is created by
     *  {@link Dialog#createReliableProvisionalResponse(int)} and
     *  the application attempts to use this method to send the response.
     * @see Response
     */
    public void sendResponse(Response response) throws SipException, InvalidArgumentException;

    /**
     * Enable the timeout retransmit notifications for the ServerTransaction. This method is
     * invoked by UAs that do want to be alerted by the
     * stack to retransmit 2XX responses but that do NOT want to associate a Dialog.
     * The Default operation is to disable retransmission alerts for the Server Transaction
     * when no Dialog is associated with the Server Transaction, as is common
     * for a Proxy server.
     * When this method is called, the stack will continue to generate {@link Timeout#RETRANSMIT}
     * until the application calls {@link Transaction#terminate()} or a
     * the listener receives a {@link SipListener#processTransactionTerminated(TransactionTerminatedEvent) } callback.
     *  Note that the stack calls
     * {@link SipListener#processTransactionTerminated(TransactionTerminatedEvent)}asynchronously
     * after it removes the transaction some time after the Transaction state is set to
     * {@link TransactionState#TERMINATED } ;
     * after which, it maintains no record of the Transaction.
     *
     * @throws SipException if a Dialog is already associated with the ServerTransaction
     * when the method is called.
     *
     * @since 1.2
     */
    public void enableRetransmissionAlerts() throws SipException ;



}

