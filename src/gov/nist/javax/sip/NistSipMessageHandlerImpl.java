/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip;

import javax.sip.*;
import javax.sip.message.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.io.IOException;

/**
 * An adapter class from the JAIN implementation objects to the NIST-SIP stack.
 * The primary purpose of this class is to do early rejection of bad messages
 * and deliver meaningful messages to the transaction layer. This is the class
 * that is instantiated by the NistSipMessageFactory to create a new
 * SIPServerRequest or SIPServerResponse. Note that this is not part of the
 * JAIN-SIP spec (it does not implement a JAIN-SIP interface). This is part of
 * the glue that ties together the NIST-SIP stack and event model with the
 * JAIN-SIP stack. Implementors of JAIN services need not concern themselves
 * with this class.
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.41 $ $Date: 2004-09-28 04:07:03 $
 * 
 * @author M. Ranganathan <mranga@nist.gov><br/>Bug fix Contributions by
 *         Lamine Brahimi and Andreas Bystrom. <br/><a href=" {@docRoot}
 *         /uncopyright.html">This code is in the public domain. </a>
 */
public class NistSipMessageHandlerImpl implements ServerRequestInterface,
        ServerResponseInterface {

    protected SIPTransaction transactionChannel;

    protected ListeningPointImpl listeningPoint;

    protected SipStackImpl sipStackImpl;

    private SIPRequest pendingRequest;

    private MessageChannel pendingMessageChannel;

    /**
     * Gets called from the dialog layer.
     */
    public void processPending() {
        processRequest(pendingRequest, pendingMessageChannel);
    }

    /**
     * Process a request.
     * 
     * @exception SIPServerException
     *                is thrown when there is an error processing the request.
     */
    public void processRequest(SIPRequest sipRequest,
            MessageChannel incomingMessageChannel) {
        // Generate the wrapper JAIN-SIP object.
        if (LogWriter.needsLogging)
            sipStackImpl.logMessage("PROCESSING INCOMING REQUEST "
                    + sipRequest.getFirstLine());
        if (listeningPoint == null) {
            if (LogWriter.needsLogging)
                sipStackImpl
                        .logMessage("Dropping message: No listening point registered!");
            return;
        }

        SIPTransactionStack sipStack = (SIPTransactionStack) transactionChannel
                .getSIPStack();
        gov.nist.javax.sip.SipStackImpl sipStackImpl = (SipStackImpl) sipStack;
        SipProviderImpl sipProvider = listeningPoint.getProvider();
        if (sipProvider == null) {
            if (LogWriter.needsLogging)
                sipStackImpl.logMessage("No provider - dropping !!");
            return;
        }
        SipListener sipListener = sipProvider.sipListener;

        // Section 16.4 If the first value in the Route header
        // field indicates this proxy,the proxy MUST remove that
        // value from the request . Note: Bill Roome from AT&T
        // noted that it may be necessary for the route header
        // to be passed up to the application as the application
        // may wish to extract some information from it. Thus
        // a new configuration parameter was added.

        if (sipRequest.getHeader(Route.NAME) != null
                && sipStackImpl.stripRouteHeader) {
            RouteList routes = sipRequest.getRouteHeaders();
            Route route = (Route) routes.getFirst();
            SipUri uri = (SipUri) route.getAddress().getURI();
            int port;
            if (uri.getHostPort().hasPort()) {
                port = uri.getHostPort().getPort();
            } else {
                port = 5060;
            }
            String host = uri.getHost();
            if (host.equals(listeningPoint.getHost())
                    && port == listeningPoint.getPort()) {
                if (routes.size() == 1)
                    sipRequest.removeHeader(Route.NAME);
                else
                    routes.removeFirst();
            }
        }

        SIPTransaction transaction = transactionChannel;
        // Look for the registered SIPListener for the message channel.

        // Removed unnecessary synchronization
        // bug reported by John Martin.
        if (sipRequest.getMethod().equalsIgnoreCase(Request.ACK)) {
            // Could not find transaction. Generate an event
            // with a null transaction identifier.
            String dialogId = sipRequest.getDialogId(true);
            SIPDialog dialog = sipStackImpl.getDialog(dialogId);
            if (LogWriter.needsLogging)
                sipStackImpl.logMessage("Processing ACK for dialog " + dialog);

            if (dialog == null) {
                if (LogWriter.needsLogging) {
                    sipStackImpl.logMessage("Dialog does not exist "
                            + sipRequest.getFirstLine()
                            + " isServerTransaction = " + true);

                }
                // Bug reported by Antonis Karydas
                transaction = sipStackImpl.findTransaction(sipRequest, true);
            } else if (dialog.getLastAck() != null
                    && dialog.getLastAck().getCSeq().getSequenceNumber() == sipRequest
                            .getCSeq().getSequenceNumber()) {
                if (sipStackImpl.isRetransmissionFilterActive()) {
                    if (LogWriter.needsLogging) {
                        sipStackImpl
                                .logMessage("Retransmission Filter enabled - dropping Ack"
                                        + " retransmission");
                    }
                    return;
                }
                if (LogWriter.needsLogging)
                    sipStackImpl
                            .logMessage("ACK retransmission for 2XX response "
                                    + "Sending ACK to the TU");
                transaction.setDialog(dialog);
            } else {

                // This could be a re-invite processing.
                // check to see if the ack matches with the last
                // transaction.

                SIPServerTransaction tr = dialog.getInviteTransaction();
                SIPResponse sipResponse = (tr != null ? tr.getLastResponse()
                        : null);

                // Idiot check for sending ACK from the wrong side!
                if (tr != null
                        && sipResponse != null
                        && sipResponse.getStatusCode() / 100 == 2
                        && sipResponse.getCSeq().getMethod().equals(
                                Request.INVITE)
                        && sipResponse.getCSeq().getSequenceNumber() == sipRequest
                                .getCSeq().getSequenceNumber()) {

                    transaction.setDialog(dialog);
                    // record that we already saw an ACK for
                    // this dialog.
                    dialog.ackReceived(sipRequest);
                    sipStackImpl
                            .logMessage("ACK for 2XX response --- sending to TU ");

                } else {
                    // This happens when the ACK is re-transmitted and arrives
                    // too late
                    // to be processed.
                    if (LogWriter.needsLogging)
                        sipStackImpl
                                .logMessage(" INVITE transaction not found  -- Discarding ACK");
                    return;
                }
            }
        } else if (sipRequest.getMethod().equals(Request.BYE)) {
            transaction = this.transactionChannel;
            // If the stack has not mapped this transaction because
            // of sequence number problem then just drop the BYE
            if (transaction != null
                    && ((SIPServerTransaction) transaction)
                            .isTransactionMapped()) {
                // Get the dialog identifier for the bye request.
                String dialogId = sipRequest.getDialogId(true);
                if (LogWriter.needsLogging)
                    sipStackImpl.logMessage("dialogId = " + dialogId);
                SIPDialog dialog = sipStackImpl.getDialog(dialogId);
                if (dialog != null) {
                    // check if request is in dialog sequence.
                    if (dialog.isRequestConsumable(sipRequest)) {
                        dialog.addTransaction(transaction);
                    } else {
                        if (LogWriter.needsLogging)
                            sipStackImpl.logMessage("Dropping bye  for "
                                    + dialogId);
                        return;
                    }
                }

            } else if (transaction != null) {
                // This is an out of sequence BYE
                // transaction was allocated but
                // not mapped to the stack so
                // just discard it.
                String dialogId = sipRequest.getDialogId(true);
                SIPDialog dialog = sipStack.getDialog(dialogId);
                if (dialog != null) {
                    if (LogWriter.needsLogging)
                        sipStackImpl.logMessage("Dropping out of sequence BYE");
                    return;
                } else if (sipStackImpl.dialogSupport) {
                    // If dialog support is enabled then
                    // there must be a dialog associated with the bye
                    transaction = null;
                }

            }
            // note that the transaction may be null (which
            // happens when no dialog for the bye was fund.
        } else if (sipRequest.getRequestLine().getMethod().equals(
                Request.CANCEL)) {

            // The ID refers to a previously sent
            // INVITE therefore it refers to the
            // server transaction table.
            // Bug reported by Andreas Bystr�m
            // Find the transaction to cancel.
            // Send a 487 for the cancel to inform the
            // other side that we've seen it but do not send the
            // request up to the application layer.

            // Get rid of the CANCEL transaction -- we pass the
            // transaciton we are trying to cancel up to the TU.

            // Antonis Karydas: Suggestion
            // 'transaction' here refers to the transaction to
            // be cancelled. Do not change
            // it's state because of the CANCEL.
            // Wait, instead for the 487 from TU.
            // transaction.setState(SIPTransaction.TERMINATED_STATE);

            SIPServerTransaction serverTransaction = (SIPServerTransaction) sipStack
                    .findCancelTransaction(sipRequest, true);

            // Generate an event
            // with a null transaction identifier.
            if (serverTransaction == null) {
                // Could not find the invite transaction.
                if (LogWriter.needsLogging) {
                    sipStackImpl.logMessage("transaction " + " does not exist "
                            + sipRequest.getFirstLine()
                            + "isServerTransaction = " + true);
                }
                transaction = null;
            } else {
                transaction = serverTransaction;
            }
        } else if (sipRequest.getMethod().equals(Request.INVITE)) {
            String dialogId = sipRequest.getDialogId(true);
            SIPDialog dialog = sipStack.getDialog(dialogId);
            SIPTransaction lastTransaction = dialog == null ? null : dialog
                    .getInviteTransaction();

            // RFC 3261 Chapter 14.
            // A UAS that receives a second INVITE before it sends the final
            // response to a first INVITE with a lower CSeq sequence number on
            // the
            // same dialog MUST return a 500 (Server Internal Error) response to
            // the
            // second INVITE and MUST include a Retry-After header field with a
            // randomly chosen value of between 0 and 10 seconds.

            if (dialog != null
                    && transaction != null
                    && lastTransaction != null
                    && sipRequest.getCSeq().getSequenceNumber() > dialog
                            .getRemoteSequenceNumber()
                    && lastTransaction instanceof SIPServerTransaction
                    && lastTransaction.isInviteTransaction()
                    && lastTransaction.getState() != TransactionState.COMPLETED
                    && lastTransaction.getState() != TransactionState.TERMINATED
                    && lastTransaction.getState() != TransactionState.CONFIRMED) {

                if (LogWriter.needsLogging)
                    sipStackImpl
                            .logMessage("Sending 500 response for out of sequence message");
                SIPResponse sipResponse = sipRequest
                        .createResponse(Response.SERVER_INTERNAL_ERROR);
                RetryAfter retryAfter = new RetryAfter();
                try {
                    retryAfter.setRetryAfter((int) (10 * Math.random()));
                } catch (InvalidArgumentException ex) {
                    ex.printStackTrace();
                }
                sipResponse.addHeader(retryAfter);
                try {
                    transaction.sendMessage(sipResponse);
                } catch (IOException ex) {
                    // Ignore.
                }
                return;
            }

            // RFC 3261 Chapter 14.
            // A UAS that receives an INVITE on a dialog while an INVITE it had
            // sent
            // on that dialog is in progress MUST return a 491 (Request Pending)
            // response to the received INVITE.
            lastTransaction = (dialog == null ? null : dialog
                    .getLastTransaction());

            if (dialog != null
                    && dialog.getLastTransaction() != null
                    && lastTransaction.isInviteTransaction()
                    && lastTransaction instanceof SIPClientTransaction
                    && lastTransaction.getState() != TransactionState.TERMINATED) {
                if (LogWriter.needsLogging)
                    sipStackImpl
                            .logMessage("Sending 491 response for out of sequence message");
                SIPResponse sipResponse = sipRequest
                        .createResponse(Response.REQUEST_PENDING);
                try {
                    transaction.sendMessage(sipResponse);
                } catch (IOException ex) {
                    // Ignore.
                }
                return;
            }
        }

        if (LogWriter.needsLogging) {
            sipStackImpl.logMessage("-----------------");
            sipStackImpl.logMessage(sipRequest.encodeMessage());
        }
        // If the transaction is found then it is already managed so
        // dont call the listener.
        String dialogId = sipRequest.getDialogId(true);
        SIPDialog dialog = sipStackImpl.getDialog(dialogId);

        // Sequence numbers are supposed to be incremented
        // sequentially within a dialog for RFC 3261
        // Note BYE is handled above - so no check here.

        if (dialog != null && transaction != null
                && !sipRequest.getMethod().equals(Request.BYE)
                && !sipRequest.getMethod().equals(Request.CANCEL)
                && !sipRequest.getMethod().equals(Request.ACK)) {
            if (dialog.getRemoteSequenceNumber() >= sipRequest.getCSeq()
                    .getSequenceNumber()) {
                if (LogWriter.needsLogging) {
                    sipStackImpl.logMessage("Dropping out of sequence message "
                            + dialog.getRemoteSequenceNumber() + " "
                            + sipRequest.getCSeq());
                }
                // "UAS Behavior" section (12.2.2):
                // If the remote sequence number was not empty, but the sequence
                // number
                // of the request is lower than the remote sequence number, the
                // request
                // is out of order and MUST be rejected with a 500 (Server
                // Internal
                // Error) response.
                // This is rather strange because the error is on the side of
                // the
                // client and not the server but thats what the spec says....
                if (dialog.getRemoteSequenceNumber() > sipRequest.getCSeq()
                        .getSequenceNumber()) {
                    if (LogWriter.needsLogging)
                        sipStackImpl
                                .logMessage("Sending 500 response for out of sequence message");
                    SIPResponse sipResponse = sipRequest
                            .createResponse(Response.SERVER_INTERNAL_ERROR);
                    try {
                        transaction.sendMessage(sipResponse);
                    } catch (IOException ex) {
                        // Ignore.
                    }
                }
                return;
            } else if (!dialog.isRequestConsumable(sipRequest)) {
                // Bug noticed by Bruce Evangelder. Spec Section 12.2.1
                // Check if sequence number of the request allows it to be
                // consumed.
                // Requests within a dialog MUST contain strictly monotonically
                // increasing and contiguous CSeq sequence numbers
                // (increasing-by-one)
                // in each direction (excepting ACK and CANCEL of course, whose
                // numbers
                // equal the requests being acknowledged or cancelled).
                // For efficient processing put in the pending queue
                // and re-process later. It would be equally valid to drop here.
                if (LogWriter.needsLogging)
                    sipStackImpl
                            .logMessage("sequence number is too large - putting pending!");
                this.pendingRequest = sipRequest;
                this.pendingMessageChannel = incomingMessageChannel;
                dialog.putPending(this, sipRequest.getCSeq()
                        .getSequenceNumber());
                return;

            }
            // This will set the remote sequence number.
            dialog.addTransaction(transaction);
            dialog.addRoute(sipRequest);
        }

        RequestEvent sipEvent;

        if (dialog == null && sipRequest.getMethod().equals(Request.NOTIFY)) {
            SIPClientTransaction ct = sipStack
                    .findSubscribeTransaction(sipRequest);
            // From RFC 3265
            // If the server transaction cannot be found or if it
            // aleady has a dialog attached to it then just assign the
            // notify to this dialog and pass it up.
            if (ct != null) {
                transaction.setDialog((SIPDialog) ct.getDialog());
                if (ct.getDialog().getState() == null) {
                    sipEvent = new RequestEvent(sipProvider, null,
                            (Request) sipRequest);
                } else {
                    sipEvent = new RequestEvent((SipProvider) sipProvider,
                            (ServerTransaction) transaction,
                            (Request) sipRequest);
                }
            } else {
                // Got a notify out of the blue - just pass it up
                // for stateless handling by the application.
                sipEvent = new RequestEvent(sipProvider, null,
                        (Request) sipRequest);
            }

        } else {
            // For a dialog creating event - set the transaction to null.
            // The listener can create the dialog if needed.
            if (transaction != null
                    && ((SIPServerTransaction) transaction)
                            .isTransactionMapped())
                sipEvent = new RequestEvent(sipProvider,
                        (ServerTransaction) transaction, (Request) sipRequest);
            else
                sipEvent = new RequestEvent(sipProvider, null,
                        (Request) sipRequest);
        }
        sipProvider.handleEvent(sipEvent, transaction);

    }

    /**
     * Process the response.
     * 
     * @exception SIPServerException
     *                is thrown when there is an error processing the response
     * @param incomingMessageChannel --
     *            message channel on which the response is received.
     */
    public void processResponse(SIPResponse sipResponse,
            MessageChannel incomingMessageChannel) {
        if (LogWriter.needsLogging) {
            sipStackImpl.logMessage("PROCESSING INCOMING RESPONSE"
                    + sipResponse.encodeMessage());
        }
        if (listeningPoint == null) {
            if (LogWriter.needsLogging)
                sipStackImpl.logMessage("Dropping message: No listening point"
                        + " registered!");
            return;
        }

        SIPTransaction transaction = (SIPTransaction) this.transactionChannel;
        SipProviderImpl sipProvider = listeningPoint.getProvider();
        if (sipProvider == null) {
            if (LogWriter.needsLogging) {
                sipStackImpl.logMessage("Dropping message:  no provider");
            }
            return;
        }

        SipStackImpl sipStackImpl = sipProvider.sipStack;

        if (LogWriter.needsLogging)
            sipStackImpl.logMessage("Transaction = " + transaction);

        if (this.transactionChannel == null) {
            String dialogId = sipResponse.getDialogId(false);
            SIPDialog dialog = sipStackImpl.getDialog(dialogId);
            //  Have a dialog but could not find transaction.
            if (sipProvider.sipListener == null) {
                return;
            } else if (dialog != null) {
                // Bug report by Emil Ivov
                if (sipResponse.getStatusCode() != Response.OK) {
                    return;
                } else if (sipStackImpl.isRetransmissionFilterActive()) {
                    // 200 retransmission for the final response.
                    if (sipResponse.getCSeq().getSequenceNumber() == ((SIPTransaction) dialog
                            .getFirstTransaction()).getCSeq()
                            && sipResponse.getCSeq().getMethod()
                                    .equals(
                                            ((SIPTransaction) dialog
                                                    .getFirstTransaction())
                                                    .getMethod())) {
                        try {
                            // Found the dialog - resend the ACK and
                            // dont pass up the null transaction
                            // bug noticed by Joe Provino.
                            dialog.resendAck();
                            return;
                        } catch (SipException ex) {
                            // What to do here ?? kill the dialog?
                        }
                    }
                }
            }
            // Pass the response up to the application layer to handle
            // statelessly.
            // Dialog is null so this is handled statelessly

            ResponseEvent sipEvent = new ResponseEvent(sipProvider, null,
                    (Response) sipResponse);
            sipProvider.handleEvent(sipEvent, transaction);
            return;
        }

        SipListener sipListener = sipProvider.sipListener;

        ResponseEvent responseEvent = new javax.sip.ResponseEvent(sipProvider,
                (ClientTransaction) transaction, (Response) sipResponse);
        sipProvider.handleEvent(responseEvent, transaction);

    }

    /**
     * Just a placeholder. This is called from the stack for message logging.
     * Auxiliary processing information can be passed back to be written into
     * the log file.
     * 
     * @return auxiliary information that we may have generated during the
     *         message processing which is retrieved by the message logger.
     */
    public String getProcessingInfo() {
        return null;
    }
}
/*
 * $Log: not supported by cvs2svn $ Revision 1.40 2004/09/26 14:48:02
 * mranga Submitted by: John Martin Reviewed by: mranga
 * 
 * Remove unnecssary synchronization.
 * 
 * Revision 1.39 2004/09/01 18:09:05 mranga Reviewed by: mranga Allow
 * application to see route header on incoming request though use of a
 * configuration parameter.
 * 
 * Revision 1.38 2004/06/21 05:43:16 mranga Reviewed by: mranga
 * 
 * code smithing
 * 
 * Revision 1.37 2004/06/21 04:59:48 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.36 2004/06/17 17:27:00 mranga Reviewed by: mranga null ptr fix
 * 
 * Revision 1.35 2004/06/17 15:22:29 mranga Reviewed by: mranga
 * 
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 * 
 * Revision 1.34 2004/06/16 19:04:27 mranga Check for out of sequence bye
 * processing.
 * 
 * Revision 1.33 2004/06/16 16:31:07 mranga Sequence number checking for
 * in-dialog messages
 * 
 * Revision 1.32 2004/06/16 02:53:17 mranga Submitted by: mranga Reviewed by:
 * implement re-entrant multithreaded listener model.
 * 
 * Revision 1.31 2004/06/15 09:54:40 mranga Reviewed by: mranga re-entrant
 * listener model added. (see configuration property
 * gov.nist.javax.sip.REENTRANT_LISTENER)
 * 
 * Revision 1.30 2004/05/18 15:26:42 mranga Reviewed by: mranga Attempted fix at
 * race condition bug. Remove redundant exception (never thrown). Clean up some
 * extraneous junk.
 * 
 * Revision 1.29 2004/05/12 20:48:54 mranga Reviewed by: mranga
 * 
 * 
 * When request is sent. The receiver is supposed to strip the route header not
 * sender. Previously sender was stripping it.
 * 
 * Revision 1.28 2004/05/10 21:11:19 mranga Submitted by: alex rootham Reviewed
 * by: mranga Add check for out of sequence requests and return server internal
 * error response.
 * 
 * Revision 1.27 2004/04/22 22:51:16 mranga Submitted by: Thomas Froment
 * Reviewed by: mranga
 * 
 * Fixed corner cases.
 * 
 * Revision 1.26 2004/04/07 00:19:22 mranga Reviewed by: mranga Fixes a
 * potential race condition for client transactions. Handle re-invites
 * statefully within an established dialog.
 * 
 * Revision 1.25 2004/04/06 12:28:23 mranga Reviewed by: mranga changed locale
 * to Locale.getDefault().getCountry() moved check for valid transaction state
 * up in the stack so unfruitful responses are pruned early.
 * 
 * Revision 1.24 2004/03/31 20:30:46 deruelle_jean Fix bug on dropping out of
 * sequence BYE
 * 
 * Issue number: Obtained from: Jean Deruelle Submitted by: Ranganathan Reviewed
 * by:
 * 
 * Revision 1.23 2004/03/25 15:15:03 mranga Reviewed by: mranga option to log
 * message content added.
 * 
 * Revision 1.22 2004/03/05 20:36:55 mranga Reviewed by: mranga put in some
 * debug printfs and cleaned some things up.
 * 
 * Revision 1.21 2004/02/26 14:28:50 mranga Reviewed by: mranga Moved some code
 * around (no functional change) so that dialog state is set when the
 * transaction is added to the dialog. Cleaned up the Shootist example a bit.
 * 
 * Revision 1.20 2004/02/25 23:02:13 mranga Submitted by: jeand Reviewed by:
 * mranga Remove pointless redundant code
 * 
 * Revision 1.19 2004/02/25 22:15:43 mranga Submitted by: jeand Reviewed by:
 * mranga Dialog state should be set to completed state and not confirmed state
 * on bye.
 * 
 * Revision 1.18 2004/02/20 20:22:55 mranga Reviewed by: mranga More hacks to
 * supress OK retransmission on re-invite when retransmission filter is enabled.
 * 
 * Revision 1.17 2004/02/19 16:21:16 mranga Reviewed by: mranga added idiot
 * check to guard against servers who like to send acks from the wrong side.
 * 
 * Revision 1.16 2004/02/19 16:01:40 mranga Reviewed by: mranga tighten up
 * retransmission filter to deal with ack retransmissions.
 * 
 * Revision 1.15 2004/02/19 15:20:27 mranga Reviewed by: mranga allow ack to go
 * through for re-invite processing
 * 
 * Revision 1.14 2004/02/13 13:55:31 mranga Reviewed by: mranga per the spec,
 * Transactions must always have a valid dialog pointer. Assigned a dummy dialog
 * for transactions that are not assigned to any dialog (such as Message).
 * 
 * Revision 1.13 2004/02/11 20:22:30 mranga Reviewed by: mranga tighten up the
 * sequence number checks for BYE processing.
 * 
 * Revision 1.12 2004/02/05 10:58:12 mranga Reviewed by: mranga Fixed a another
 * bug caused by previous fix that restricted request consumption in a dialog
 * based on increasing sequence numbers.
 * 
 * Revision 1.11 2004/02/04 22:07:24 mranga Reviewed by: mranga Fix for handling
 * of out of order sequence numbers in the dialog layer.
 * 
 * Revision 1.10 2004/02/04 18:44:18 mranga Reviewed by: mranga check sequence
 * number before delivering event to application.
 * 
 * Revision 1.9 2004/01/27 15:11:06 mranga Submitted by: jeand Reviewed by:
 * mranga If retrans filter enabled then ack should be seen only once by
 * application. Else each retransmitted ack is seen by application.
 * 
 * Revision 1.8 2004/01/26 19:12:49 mranga Reviewed by: mranga moved SIMULATION
 * tag to first columnm
 * 
 * Revision 1.7 2004/01/22 13:26:28 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *  
 */
