package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.SIPConstants;
import javax.sip.message.*;
import java.util.*;
import gov.nist.javax.sip.address.*;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;

import java.io.IOException;

//ifdef SIMULATION
/*
import sim.java.*;
//endif
*/


/**
 *Represents a client transaction.
 * Implements the following state machines. (From RFC 3261)
 *<pre>
 *
 *
 *                               |INVITE from TU
 *             Timer A fires     |INVITE sent
 *             Reset A,          V                      Timer B fires
 *             INVITE sent +-----------+                or Transport Err.
 *               +---------|           |---------------+inform TU
 *               |         |  Calling  |               |
 *               +-------->|           |-------------->|
 *                         +-----------+ 2xx           |
 *                            |  |       2xx to TU     |
 *                            |  |1xx                  |
 *    300-699 +---------------+  |1xx to TU            |
 *   ACK sent |                  |                     |
 *resp. to TU |  1xx             V                     |
 *            |  1xx to TU  -----------+               |
 *            |  +---------|           |               |
 *            |  |         |Proceeding |-------------->|
 *            |  +-------->|           | 2xx           |
 *            |            +-----------+ 2xx to TU     |
 *            |       300-699    |                     |
 *            |       ACK sent,  |                     |
 *            |       resp. to TU|                     |
 *            |                  |                     |      NOTE:
 *            |  300-699         V                     |
 *            |  ACK sent  +-----------+Transport Err. |  transitions
 *            |  +---------|           |Inform TU      |  labeled with
 *            |  |         | Completed |-------------->|  the event
 *            |  +-------->|           |               |  over the action
 *            |            +-----------+               |  to take
 *            |              ^   |                     |
 *            |              |   | Timer D fires       |
 *            +--------------+   | -                   |
 *                               |                     |
 *                               V                     |
 *                         +-----------+               |
 *                         |           |               |
 *                         | Terminated|<--------------+
 *                         |           |
 *                         +-----------+
 *
 *                 Figure 5: INVITE client transaction
 *
 *
 *                                   |Request from TU
 *                                   |send request
 *               Timer E             V
 *               send request  +-----------+
 *                   +---------|           |-------------------+
 *                   |         |  Trying   |  Timer F          |
 *                   +-------->|           |  or Transport Err.|
 *                             +-----------+  inform TU        |
 *                200-699         |  |                         |
 *                resp. to TU     |  |1xx                      |
 *                +---------------+  |resp. to TU              |
 *                |                  |                         |
 *                |   Timer E        V       Timer F           |
 *                |   send req +-----------+ or Transport Err. |
 *                |  +---------|           | inform TU         |
 *                |  |         |Proceeding |------------------>|
 *                |  +-------->|           |-----+             |
 *                |            +-----------+     |1xx          |
 *                |              |      ^        |resp to TU   |
 *                | 200-699      |      +--------+             |
 *                | resp. to TU  |                             |
 *                |              |                             |
 *                |              V                             |
 *                |            +-----------+                   |
 *                |            |           |                   |
 *                |            | Completed |                   |
 *                |            |           |                   |
 *                |            +-----------+                   |
 *                |              ^   |                         |
 *                |              |   | Timer K                 |
 *                +--------------+   | -                       |
 *                                   |                         |
 *                                   V                         |
 *             NOTE:           +-----------+                   |
 *                             |           |                   |
 *         transitions         | Terminated|<------------------+
 *         labeled with        |           |
 *         the event           +-----------+
 *         over the action
 *         to take
 *
 *                 Figure 6: non-INVITE client transaction
 *
 *
 *</pre>
 *
 *@author Jeff Keyser
 *@author M. Ranganathan <mranga@nist.gov>
 *@author Bug fixes by Emil Ivov.
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *@version  JAIN-SIP-1.1
 */
public class SIPClientTransaction
extends SIPTransaction
implements SIPServerResponseInterface, javax.sip.ClientTransaction {
    
    private SIPRequest lastRequest;
    
    
    private int viaPort;
    
    private String viaHost;
    
    // Real ResponseInterface to pass messages to
    private SIPServerResponseInterface	respondTo;
    
    
    /**
     *	Creates a new client transaction.
     *
     *	@param newSIPStack Transaction stack this transaction
     *      belongs to.
     *	@param newChannelToUse Channel to encapsulate.
     */
    protected SIPClientTransaction(
    SIPTransactionStack	newSIPStack,
    MessageChannel		newChannelToUse
    ) {
        super( newSIPStack, newChannelToUse );
        // Create a random branch parameter for this transaction
        // setBranch( SIPConstants.BRANCH_MAGIC_COOKIE +
        // Integer.toHexString( hashCode( ) ) );
        setBranch( Utils.generateBranchId());
        if (parentStack.logWriter.needsLogging) {
            parentStack.logWriter.logMessage("Creating clientTransaction " + this);
            parentStack.logWriter.logStackTrace();
        }
        
    }
    
    
    /**
     *	Sets the real ResponseInterface this transaction encapsulates.
     *
     *	@param newRespondTo ResponseInterface to send messages to.
     */
    public void setResponseInterface(
    SIPServerResponseInterface	newRespondTo
    ) {
        
        respondTo = newRespondTo;
        
    }
    
    
    public String getProcessingInfo(
    ) {
        
        return respondTo.getProcessingInfo( );
        
    }
    
    
    /**
     *	Returns this transaction.
     */
    public MessageChannel getRequestChannel(
    ) {
        
        return this;
        
    }
    
    
    /**
     *	Deterines if the message is a part of this transaction.
     *
     *	@param messageToTest Message to check if it is part of this
     *		transaction.
     *
     *	@return True if the message is part of this transaction,
     * 		false if not.
     */
    public  boolean isMessagePartOfTransaction(
    SIPMessage	messageToTest
    ) {
        
        // List of Via headers in the message to test
        ViaList	viaHeaders = messageToTest.getViaHeaders( );
        // Flags whether the select message is part of this transaction
        boolean transactionMatches;
        String messageBranch = ((Via)viaHeaders.getFirst()).getBranch();
        boolean rfc3261Compliant =
        getBranch() != null &&
        messageBranch != null &&
        getBranch().startsWith(SIPConstants.BRANCH_MAGIC_COOKIE) &&
        messageBranch.startsWith(SIPConstants.BRANCH_MAGIC_COOKIE);

	/**
	if (parentStack.logWriter.needsLogging)  {
		parentStack.logWriter.logMessage("--------- TEST ------------");
		parentStack.logWriter.logMessage(" testing " + this.getOriginalRequest());
		parentStack.logWriter.logMessage("Against " + messageToTest);
		parentStack.logWriter.logMessage("isTerminated = " + isTerminated());
		parentStack.logWriter.logMessage("messageBranch = " + messageBranch);
		parentStack.logWriter.logMessage("viaList = " + messageToTest.getViaHeaders());
		parentStack.logWriter.logMessage("myBranch = " + getBranch());
	}
	**/
        
        transactionMatches = false;
        if( !isTerminated( ) ) {
            if (rfc3261Compliant) {
                if( viaHeaders != null ) {
                    // If the branch parameter is the
                    //same as this transaction and the method is the same,
                    if( getBranch().equals
                    (((Via)viaHeaders.getFirst()).
                    getBranch())) {
                        transactionMatches =
                        getOriginalRequest().getCSeq().
                        getMethod().equals
                        ( messageToTest.getCSeq().
                        getMethod());
                        
                    }
                }
            } else {
                transactionMatches =
                getOriginalRequest().getTransactionId().equals
                (messageToTest.getTransactionId());
                
            }
            
        }
        return transactionMatches;
        
    }
    
    
    /**
     *  Send a request message through this transaction and
     *  onto the client.
     *
     *	@param messageToSend Request to process and send.
     */
    public void sendMessage(
    SIPMessage	messageToSend
    ) throws IOException {
        
        // Message typecast as a request
        SIPRequest	transactionRequest;

//ifdef SIMULATION
/*
	SimSystem.hold(getSIPStack().stackProcessingTime);
//endif
*/

	

        
        
        transactionRequest = (SIPRequest)messageToSend;
        
        
        // Set the branch id for the top via header.
        Via topVia =
        (Via) transactionRequest.getViaHeaders().getFirst();
        // Tack on a branch identifier  to match responses.
        try {
            topVia.setBranch(getBranch());
        } catch (java.text.ParseException ex) {}
        
        // If this is the first request for this transaction,
        if( getState( ) != null  &&
         (getState().getValue() == PROCEEDING_STATE ||
        getState().getValue() == CALLING_STATE )  ){
            
            // If this is a TU-generated ACK request,
            if( transactionRequest.getMethod().equals( Request.ACK ) ) {
                // Send directly to the underlying
                // transport and close this transaction
		// Bug fix by Emil Ivov
		if (isReliable()) {
                   setState( TERMINATED_STATE );
		} else  {
		   setState(COMPLETED_STATE);
		}
                getMessageChannel().sendMessage
                (transactionRequest );
                return;
                
            }
            
        }
        try {
            
            // Send the message to the server
            lastRequest = transactionRequest;
            getMessageChannel( ).sendMessage( transactionRequest );
	    if (getState() == null) {
               // Save this request as the one this transaction
               // is handling
               setOriginalRequest( transactionRequest );
               // Change to trying/calling state
               if (transactionRequest.getMethod().equals(Request.INVITE)) {
                   setState(CALLING_STATE);
               } else if (transactionRequest.getMethod().equals(Request.ACK)) {
		  // Acks are never retransmitted.
                  setState(TERMINATED_STATE);
	       } else {
                   setState( TRYING_STATE );
               }
              if( !isReliable( ) ) {
                enableRetransmissionTimer( );
              }
              if (isInviteTransaction()) {
                 enableTimeoutTimer( TIMER_B );
              }  else {
                enableTimeoutTimer( TIMER_F );
              }
	    }
            
        } catch( IOException e ) {
            
            setState( TERMINATED_STATE );
            throw e;
            
        }
        
    }
    
    
    /**
     *	Process a new response message through this transaction.
     * If necessary, this message will also be passed onto the TU.
     *
     *	@param transactionResponse Response to process.
     *	@param sourceChannel Channel that received this message.
     */
    public synchronized  void processResponse(
    SIPResponse		transactionResponse,
    MessageChannel	sourceChannel
    ) throws SIPServerException {
        // Log the incoming response in our log file.
        if (parentStack.serverLog.needsLogging
	   (parentStack.serverLog.TRACE_MESSAGES))
            this.logResponse(transactionResponse,

//ifdef SIMULATION
/*
            SimSystem.currentTimeMillis(),
//else
*/
            System.currentTimeMillis(),
//endif
//

	    "normal processing");
	// Ignore 1xx 
	if (getState().getValue() == COMPLETED_STATE  && 
		transactionResponse.getStatusCode()/100 == 1) return;

	if (parentStack.logWriter.needsLogging) 
	    parentStack.logWriter.logMessage("processing " + 
	    transactionResponse.getFirstLine() + "current state = " 
	   + getState().getValue());

        this.lastResponse = transactionResponse;

	if (dialog != null) {
	    // add the route before you process the response.
	    // Bug noticed by Brad Templeton.
            dialog.addRoute(transactionResponse);
	}
        String method = transactionResponse.getCSeq().getMethod();
        if (dialog != null) {
            boolean added = false;
	    SIPTransactionStack sipStackImpl
                = (SIPTransactionStack) getSIPStack();
            
            // A tag just got assigned  or changed.
            if (dialog.getRemoteTag() == null &&
            transactionResponse.getTo().getTag() != null ) {
                
                // Dont assign tag on provisional response
                if (transactionResponse.getStatusCode() != 100) {
                    dialog.setRemoteTag(transactionResponse.getToTag());
                }
                String dialogId = transactionResponse.getDialogId(false);
                dialog.setDialogId(dialogId);
                if (sipStackImpl.isDialogCreated(method) && 
		    transactionResponse.getStatusCode() != 100 ) {
                    sipStackImpl.putDialog(dialog);
		    if (transactionResponse.getStatusCode()/100 == 1) 
                        dialog.setState(DialogImpl.EARLY_STATE);
		    else if ( transactionResponse.getStatusCode()/100 == 2)
                        dialog.setState(DialogImpl.CONFIRMED_STATE);
                    added = true;
                }
                
            } else if ( dialog.getRemoteTag() 		!= null      &&
            		transactionResponse.getToTag()  != null      &&
            		! dialog.getRemoteTag().equals
            		(transactionResponse.getToTag())) {
                String dialogId = transactionResponse.getDialogId(false);
		dialog.setRemoteTag(transactionResponse.getToTag());
                dialog.setDialogId(dialogId);
                if (sipStackImpl.isDialogCreated(method)) {
                    sipStackImpl.putDialog(dialog);
                    added = true;
                }
            }
            
            if (sipStackImpl.isDialogCreated(method) ) {
                // Make  a final tag assignment.
                if (transactionResponse.getToTag() != null &&
                  transactionResponse.getStatusCode()/100 == 2)  {
                   // This is a dialog creating method (such as INVITE).
                   // 2xx response -- set the state to the confirmed
                  // state.
                    dialog.setRemoteTag(transactionResponse.getToTag());
                    dialog.setState(DialogImpl.CONFIRMED_STATE);
                }  else if ( (
			 transactionResponse.getStatusCode() == 487 ||
                         transactionResponse.getStatusCode()/100 == 5 ||
                         transactionResponse.getStatusCode()/100 == 6) &&
                        ( dialog.getState() == null  ||
                          dialog.getState().getValue() ==
                          DialogImpl.EARLY_STATE)) {
                        // Invite transaction generated an error.
                        dialog.setState(DialogImpl.TERMINATED_STATE);
                }
            }
	}
        try {
            if (isInviteTransaction())
                inviteClientTransaction(transactionResponse,sourceChannel);
            else nonInviteClientTransaction(transactionResponse,sourceChannel);
        } catch (IOException ex) {
            setState(TERMINATED_STATE);
            raiseErrorEvent
            ( SIPTransactionErrorEvent.TRANSPORT_ERROR );
        }
    }
    
    /** Implements the state machine for invite client transactions.
     *<pre>
     *
     *                                   |Request from TU
     *                                   |send request
     *               Timer E             V
     *               send request  +-----------+
     *                   +---------|           |-------------------+
     *                   |         |  Trying   |  Timer F          |
     *                   +-------->|           |  or Transport Err.|
     *                             +-----------+  inform TU        |
     *                200-699         |  |                         |
     *                resp. to TU     |  |1xx                      |
     *                +---------------+  |resp. to TU              |
     *                |                  |                         |
     *                |   Timer E        V       Timer F           |
     *                |   send req +-----------+ or Transport Err. |
     *                |  +---------|           | inform TU         |
     *                |  |         |Proceeding |------------------>|
     *                |  +-------->|           |-----+             |
     *                |            +-----------+     |1xx          |
     *                |              |      ^        |resp to TU   |
     *                | 200-699      |      +--------+             |
     *                | resp. to TU  |                             |
     *                |              |                             |
     *                |              V                             |
     *                |            +-----------+                   |
     *                |            |           |                   |
     *                |            | Completed |                   |
     *                |            |           |                   |
     *                |            +-----------+                   |
     *                |              ^   |                         |
     *                |              |   | Timer K                 |
     *                +--------------+   | -                       |
     *                                   |                         |
     *                                   V                         |
     *             NOTE:           +-----------+                   |
     *                             |           |                   |
     *         transitions         | Terminated|<------------------+
     *         labeled with        |           |
     *         the event           +-----------+
     *         over the action
     *         to take
     *
     *                 Figure 6: non-INVITE client transaction
     *</pre>
     * @param transactionResponse -- transaction response received.
     * @param sourceChannel - source channel on which the response was received.
     */
    private void nonInviteClientTransaction(
    SIPResponse transactionResponse,
    MessageChannel sourceChannel)
    throws IOException, SIPServerException  {
        int currentState = getState().getValue();
        int statusCode = transactionResponse.getStatusCode();
        if (currentState == TRYING_STATE) {
            if (statusCode / 100 == 1) {
                setState(PROCEEDING_STATE);
                enableRetransmissionTimer
                (MAXIMUM_RETRANSMISSION_TICK_COUNT);
                enableTimeoutTimer(TIMER_F);
		// According to RFC, the TU has to be informed on 
		// this transition.  Bug report by Emil Ivov
	        respondTo.processResponse(transactionResponse,this);
            } else if (200 <= statusCode && statusCode <= 699) {
                // Send the response up to the TU.
                respondTo.processResponse( transactionResponse, this );
                if (! isReliable() ) {
                    setState(COMPLETED_STATE);
                    enableTimeoutTimer(TIMER_K);
                } else {
                    setState(TERMINATED_STATE);
                }
            }
        } else  if (currentState == PROCEEDING_STATE ) { 
	  // Bug fixes by Emil Ivov
	  if ( statusCode / 100 == 1) {
	    respondTo.processResponse(transactionResponse,this);
          } else if (200 <= statusCode && statusCode <= 699) {
            respondTo.processResponse( transactionResponse, this );
            disableRetransmissionTimer( );
            disableTimeoutTimer( );
            if (! isReliable()) {
                setState(COMPLETED_STATE);
                enableTimeoutTimer(TIMER_K);
            } else {
                setState(TERMINATED_STATE);
            }
	   }
        }
    }
    
    
    
    /** Implements the state machine for invite client transactions.
     *<pre>
     *
     *                               |INVITE from TU
     *             Timer A fires     |INVITE sent
     *             Reset A,          V                      Timer B fires
     *             INVITE sent +-----------+                or Transport Err.
     *               +---------|           |---------------+inform TU
     *               |         |  Calling  |               |
     *               +-------->|           |-------------->|
     *                         +-----------+ 2xx           |
     *                            |  |       2xx to TU     |
     *                            |  |1xx                  |
     *    300-699 +---------------+  |1xx to TU            |
     *   ACK sent |                  |                     |
     *resp. to TU |  1xx             V                     |
     *            |  1xx to TU  -----------+               |
     *            |  +---------|           |               |
     *            |  |         |Proceeding |-------------->|
     *            |  +-------->|           | 2xx           |
     *            |            +-----------+ 2xx to TU     |
     *            |       300-699    |                     |
     *            |       ACK sent,  |                     |
     *            |       resp. to TU|                     |
     *            |                  |                     |      NOTE:
     *            |  300-699         V                     |
     *            |  ACK sent  +-----------+Transport Err. |  transitions
     *            |  +---------|           |Inform TU      |  labeled with
     *            |  |         | Completed |-------------->|  the event
     *            |  +-------->|           |               |  over the action
     *            |            +-----------+               |  to take
     *            |              ^   |                     |
     *            |              |   | Timer D fires       |
     *            +--------------+   | -                   |
     *                               |                     |
     *                               V                     |
     *                         +-----------+               |
     *                         |           |               |
     *                         | Terminated|<--------------+
     *                         |           |
     *                         +-----------+
     *</pre>
     * @param transactionResponse -- transaction response received.
     * @param sourceChannel - source channel on which the response was received.
     */
    
    private void inviteClientTransaction(
    SIPResponse transactionResponse,
    MessageChannel sourceChannel) throws IOException, SIPServerException {
        int statusCode = transactionResponse.getStatusCode();
        int currentState = getState().getValue();
        if (currentState == TERMINATED_STATE) {
            // Do nothing in the terminated state.
            return;
        } else if (currentState == CALLING_STATE )  {
            if (statusCode/100 == 2) {
	        // 200 responses are always seen by TU.
                respondTo.processResponse( transactionResponse, this );
                disableRetransmissionTimer( );
                disableTimeoutTimer( );
                setState(TERMINATED_STATE);
            } else if (statusCode/100 == 1) {
                disableRetransmissionTimer( );
                disableTimeoutTimer( );
                respondTo.processResponse( transactionResponse, this );
                setState(PROCEEDING_STATE);
            } else if (300 <= statusCode  &&  statusCode <= 699) {
                // Send back an ACK request (do this before calling the
		// application (bug noticed by Andreas Bystrom).
		try {
                  sendMessage((SIPRequest) createAck());
		} catch (SipException ex) { 
			InternalErrorHandler.handleException(ex);
		}
                // When in either the "Calling" or "Proceeding" states,
                // reception of response with status code from 300-699
                // MUST cause the client transaction to 
		// transition to "Completed".
                // The client transaction MUST pass the received response up to
                // the TU, and the client transaction MUST generate an 
		// ACK request.

                 respondTo.processResponse( transactionResponse, this);

                if ( ! isReliable())  {
                    setState(COMPLETED_STATE);
                    enableTimeoutTimer(TIMER_D);
                } else {
                    //Proceed immediately to the TERMINATED state.
                    setState(TERMINATED_STATE);
                }
            }
        } else if (currentState == PROCEEDING_STATE) {
	    if (statusCode / 100 == 1) {
                respondTo.processResponse( transactionResponse, this);
            } else if (statusCode / 100 == 2) {
                setState(TERMINATED_STATE);
                respondTo.processResponse( transactionResponse, this);
            } else if (300 <= statusCode  &&  statusCode <= 699) {
                // Send back an ACK request
		try {
                   sendMessage((SIPRequest)createAck());
		} catch (SipException ex) {
		   InternalErrorHandler.handleException(ex);
		}
		// Pass up to the TU for processing.
                respondTo.processResponse( transactionResponse, this);
                if ( ! isReliable())  {
                    setState(COMPLETED_STATE);
                    enableTimeoutTimer(TIMER_D);
                } else {
                    setState(TERMINATED_STATE);
                }
            }
        } else if (currentState == COMPLETED_STATE) {
            if (300 <= statusCode  &&  statusCode <= 699) {
                // Send back an ACK request
		try {
                   sendMessage((SIPRequest)createAck());
		} catch (SipException ex) { 
			InternalErrorHandler.handleException(ex);
		}
            }
            
        }
        
    }
    
    /** Sends specified {@link javax.sip.message.Request} on a unique
     * client transaction identifier. This method implies that the application
     * is functioning as either a User Agent Client or a Stateful proxy, hence
     * the underlying SipProvider acts statefully.
     * <p>
     * JAIN SIP defines a retransmission utility specific to user agent
     * behaviour and the default retransmission behaviour for each method.
     * <p>
     * When an application wishes to send a message, it creates a Request
     * message passes that Request to this method, this method returns the
     * cleintTransactionId generated by the SipProvider. The Request message
     * gets sent via the ListeningPoint that this SipProvider is attached to.
     * <ul>
     * <li>User Agent Client - must not send a BYE on a confirmed INVITE until
     * it has received an ACK for its 2xx response or until the server
     * transaction times out.
     * </ul>
     *
     * @throws SipException if implementation cannot send request for any reason
     */
    public void sendRequest()
    throws SipException {
        SIPRequest sipRequest =  this.getOriginalRequest();
        try {
	    // Only map this after the fist request is sent out.
	    this.isMapped = true;
            this.sendMessage(sipRequest);
        } catch (IOException ex) {
            throw new SipException(ex.getMessage());
        }
        
    }
    
    
    /**
     * Called by the transaction stack when a retransmission timer
     * fires.
     */
    protected void fireRetransmissionTimer(
    ) {
        
        try {
             // Resend the last request sent
	     if ( this.getState() == null || ! this.isMapped ) return;
	     if ( this.getState().getValue()    == CALLING_STATE  ||
		 this.getState().getValue()    == TRYING_STATE ) {
		 // If the retransmission filter is disabled then
		 // retransmission of the INVITE is the application
		 // responsibility.
	         if ((!(((SIPTransactionStack)getSIPStack()).
		    retransmissionFilter)) && this.isInviteTransaction() ) {
                    raiseErrorEvent
			(SIPTransactionErrorEvent.TIMEOUT_RETRANSMIT);
		 } else {
		   // Could have allocated the transaction but not yet
		   // sent out a request (Bug report by Dave Stuart).
		   if (lastRequest != null) 
                   	getMessageChannel( ).sendMessage( lastRequest );
		 }
	       }
        } catch( IOException e ) {
            raiseErrorEvent(SIPTransactionErrorEvent.TRANSPORT_ERROR );
        }
        
    }
    
    
    /**
     *	Called by the transaction stack when a timeout timer fires.
     */
    protected void fireTimeoutTimer(
    ) {
        
         DialogImpl dialogImpl = (DialogImpl) this.getDialog();
         if( getState().getValue() == CALLING_STATE ||
            getState().getValue() == TRYING_STATE   ||
            getState().getValue() == PROCEEDING_STATE ) {
            // Timeout occured. If this is asociated with a transaction
            // creation then kill the dialog.
	    if (dialogImpl != null) {
              if (((SIPTransactionStack)getSIPStack()).isDialogCreated
                (this.getOriginalRequest().getMethod())) {
                // terminate the enclosing dialog.
                dialogImpl.setState(DialogImpl.TERMINATED_STATE);
              } else if (getOriginalRequest().getMethod().equalsIgnoreCase
                (Request.BYE)){
                // Terminate the associated dialog on BYE Timeout.
                dialogImpl.setState(DialogImpl.TERMINATED_STATE);
              }
	    }
        }
        if ( getState().getValue() != COMPLETED_STATE ) {
            raiseErrorEvent(SIPTransactionErrorEvent.TIMEOUT_ERROR);
        } else {
            setState(TERMINATED_STATE);
        }
        
        
        
    }

    /**
     * Creates a new Cancel message from the Request associated with this client
     * transaction. The CANCEL request, is used to cancel the previous request 
     * sent by this client transaction. Specifically, it asks the UAS to cease 
     * processing the request and to generate an error response to that request.
     * 
     *@return a cancel request generated from the original request.
     */    
    public Request createCancel() throws SipException {
	SIPRequest originalRequest = this.getOriginalRequest();
	if (originalRequest.getMethod().equalsIgnoreCase(Request.ACK))
		throw new SipException("Cannot Cancel ACK!");
	else return originalRequest.createCancelRequest();
    }


    /**
     * Creates an ACK request for this transaction
     * 
     *@return an ack request generated from the original request.
     *
     *@throws SipException if transaction is in the wrong state to be acked.
     */    
    public Request createAck() throws SipException {
	SIPRequest originalRequest = this.getOriginalRequest();
	if (originalRequest.getMethod().equalsIgnoreCase(Request.ACK))
		throw new SipException("Cannot ACK an ACK!");
	else if (  lastResponse == null) 
		throw new SipException ("bad Transaction state");
	else if (  lastResponse.getStatusCode() < 200 )  {
		if (parentStack.logWriter.needsLogging ) {
			parentStack.logWriter.logMessage("lastResponse = " + 
				lastResponse);
		}
		throw new SipException("Cannot ACK a provisional response!");
	}
	SIPRequest ackRequest =  
		originalRequest.createAckRequest((To)lastResponse.getTo());
	// Pull the record route headers from the last reesponse.
	 RecordRouteList recordRouteList = 
		lastResponse.getRecordRouteHeaders();
	 if (recordRouteList == null) return ackRequest;
	 ackRequest.removeHeader(RouteHeader.NAME);
         RouteList routeList =  new RouteList();
          // start at the end of the list and walk backwards
         ListIterator li = recordRouteList.listIterator
            (recordRouteList.size());
         while (li.hasPrevious()) {
                RecordRoute rr = (RecordRoute) li.previous();
                AddressImpl addr = (AddressImpl) rr.getAddress();
                Route route = new Route();
                route.setAddress
                  ((AddressImpl)((AddressImpl)rr.getAddress()).clone());
                  route.setParameters
			((NameValueList)rr.getParameters().clone());
                routeList.add(route);
          }

	  Contact contact = null;
	  if (lastResponse.getContactHeaders() != null)  {
	     contact = (Contact) lastResponse.
				getContactHeaders().getFirst();
	  }

	  if( ! ((SipURI)((Route)routeList.getFirst()).getAddress().getURI()).
			hasLrParam() ) {

		// Contact may not yet be there (bug reported by Andreas B).

		Route route = null;
		if (contact != null) {
		   route = new Route();
                   route.setAddress
                   ((AddressImpl)((AddressImpl)(contact.getAddress())).clone());
		}

		Route firstRoute = (Route) routeList.getFirst();
		routeList.removeFirst();
	  	javax.sip.address.URI uri = firstRoute.getAddress().getURI();
		ackRequest.setRequestURI(uri);

		if (route != null) routeList.add(route);	

		ackRequest.addHeader(routeList);
	 } else {
	      javax.sip.address.URI uri = (javax.sip.address.URI) 
			contact.getAddress().getURI().clone();
	      ackRequest.setRequestURI(uri);
	      ackRequest.addHeader(routeList);
	 }
	 return ackRequest;
		
	
    }

    

    
    
    /** Set the port of the recipient.
     */
    protected void setViaPort(int port) { this.viaPort = port; }
    
    /** Set the port of the recipient.
     */
    protected void setViaHost(String host) { this.viaHost = host; }
    
    /** Get the port of the recipient.
     */
    public int getViaPort() { return this.viaPort; }
    
    /** Get the host of the recipient.
     */
    public String getViaHost() { return this.viaHost; }

    /** get the via header for an outgoing request.
     */
    public Via getOutgoingViaHeader() {
		return this.getMessageProcessor().getViaHeader();
    }

     public boolean isSecure() { return encapsulatedChannel.isSecure(); }
    
}
