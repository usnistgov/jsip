/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip;
import javax.sip.*;
import javax.sip.message.*;
import java.io.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.util.HashSet;
import java.util.Iterator;

/**
 * An adapter class from the JAIN implementation objects to the NIST-SIP stack.
 * This is the class that is instantiated by the NistSipMessageFactory to
 * create a new SIPServerRequest or SIPServerResponse.
 * Note that this is not part of the JAIN-SIP spec (it does not implement
 * a JAIN-SIP interface). This is part of the glue that ties together the
 * NIST-SIP stack and event model with the JAIN-SIP stack. Implementors
 * of JAIN services need not concern themselves with this class.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 * Bug fix Contributions by Lamine Brahimi and  Andreas Bystrom. <br/>
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class NistSipMessageHandlerImpl
implements SIPServerRequestInterface, SIPServerResponseInterface {
    
    protected SIPTransaction transactionChannel;
    protected MessageChannel rawMessageChannel;
    // protected SIPRequest sipRequest;
    // protected SIPResponse sipResponse;
    protected ListeningPointImpl listeningPoint;
    protected SIPTransactionStack sipStack;
    protected SipStackImpl sipStackImpl;
    /**
     * Process a request.
     *@exception SIPServerException is thrown when there is an error
     * processing the request.
     */
    public void processRequest(SIPRequest sipRequest,
    MessageChannel incomingMessageChannel)
    throws SIPServerException {
        // Generate the wrapper JAIN-SIP object.
        if (LogWriter.needsLogging)
            sipStackImpl.logMessage
            ("PROCESSING INCOMING REQUEST " + sipRequest.getFirstLine());
        if (listeningPoint == null) {
            if (LogWriter.needsLogging)
                sipStackImpl.logMessage
                ("Dropping message: No listening point " +
                "registered!");
            return;
        }
        this.rawMessageChannel = incomingMessageChannel;
        
        SIPTransactionStack sipStack = (SIPTransactionStack)
        transactionChannel.getSIPStack();
        gov.nist.javax.sip.SipStackImpl sipStackImpl = (SipStackImpl) sipStack;
        SipProviderImpl sipProvider = listeningPoint.getProvider();
	if (sipProvider == null)  {
                if (LogWriter.needsLogging)
		   sipStackImpl.logMessage("No provider - dropping !!");
		return;
	}
	SipListener sipListener = sipProvider.sipListener;
		
        SIPTransaction transaction = transactionChannel;
        // Look for the registered SIPListener for the message channel.
//ifndef SIMULATION
//
	synchronized(sipProvider) {
//endif
//
            if (sipRequest.getMethod().equalsIgnoreCase(Request.ACK)) {
                // Could not find transaction. Generate an event
                // with a null transaction identifier.
                String dialogId = sipRequest.getDialogId(true);
                DialogImpl dialog = sipStackImpl.getDialog(dialogId) ;
		if (LogWriter.needsLogging)
		    sipStackImpl.logMessage
			("Processing ACK for dialog " +
				dialog);

                if (dialog == null) {
                    if (LogWriter.needsLogging) {
                        sipStackImpl.logMessage(
                        "Dialog does not exist "
                        + sipRequest.getFirstLine() +
                        " isServerTransaction = " + true);
                        
                    }
                    //return;
		    // Bug reported by Antonis Karydas
		    transaction = 
			sipStackImpl.findTransaction(sipRequest,true);
		    //transaction = null;
                } else {

                SIPTransaction tr = dialog.getLastTransaction();
                SIPResponse sipResponse = tr.getLastResponse();
                
                if (sipResponse!=null && sipResponse.getStatusCode()/100 == 2 
			&& sipResponse.getCSeq().getSequenceNumber() 
		    == sipRequest.getCSeq().getSequenceNumber()  )  {
                    
                    if (LogWriter.needsLogging)
                        sipStackImpl.logMessage(
                        "ACK retransmission for 2XX response " +
                        "Sending ACK to the TU");
                    dialog.ackReceived(sipRequest);
		    transaction.setDialog(dialog);
                } else {
                    if (LogWriter.needsLogging)
                        sipStackImpl.logMessage(
                        "ACK retransmission for non 2XX response " +
                        "Discarding ACK");
                   // Could not find a transaction.
                   if (tr == null) {
                      if (LogWriter.needsLogging)
                        sipStackImpl.logMessage
                        ("Could not find transaction ACK dropped");
                      return;
                   }  
		   transaction = tr;
		   if (transaction instanceof SIPClientTransaction)  {
			if (LogWriter.needsLogging)
			   sipStackImpl.logMessage
			   ("Dropping late ACK");
			return;
		   }
                }
		}
            } else if (sipRequest.getMethod().equals(Request.BYE)) {
                transaction = this.transactionChannel;
                
                // Get the dialog identifier for the bye request.
                String dialogId = sipRequest.getDialogId(true);
                if (LogWriter.needsLogging)
                    sipStackImpl.logMessage
                    ("dialogId = " + dialogId);
                // Find the dialog identifier in the SIP stack and
                // mark it for garbage collection.
                DialogImpl dialog =
                sipStackImpl.getDialog(dialogId);
                if (dialog != null) {
                    // Remove dialog marks all
                    // outstanding transactions for
                    // garbage collection. Note that the dialog is alive
                    // until the final response for the BYE is sent out.
                    dialog.addTransaction(transaction);
                    
                } else {
                    dialog = sipStackImpl.getDialog(dialogId);
                    if (dialog != null) {
                        dialog.addTransaction(transaction);
                        // sipStackImpl.removeDialog(dialog); // see provider
                    } else {
                        dialogId = sipRequest.getDialogId(false);
                        if (LogWriter.needsLogging)
                            sipStackImpl.getLogWriter().logMessage
                            ("dialogId = " +
                            dialogId);
                        dialog = sipStackImpl.getDialog(dialogId);
                        if (dialog != null) {
                            dialog.addTransaction(transaction);
                        } else {
                            dialog = sipStackImpl.getDialog(dialogId);
                            if (dialog != null)  {
                                dialog.addTransaction(transaction);
                                
                            }
			    transaction = null; // pass up to provider for
						// stateless handling.
                        }
                    }
                }
            } else if (sipRequest.getRequestLine().getMethod().equals
            (Request.CANCEL)) {
                
                // The ID refers to a previously sent
                // INVITE therefore it refers to the
                // server transaction table.
                // Bug reported by Andreas Byström
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
                
                SIPServerTransaction serverTransaction =
                (SIPServerTransaction)
                sipStack.findCancelTransaction(sipRequest,true);
                
                // Generate an event
                // with a null transaction identifier.
                if (serverTransaction == null) {
                    // Could not find the invite transaction.
                    if (LogWriter.needsLogging) {
                        sipStackImpl.logMessage(
                        "transaction " +
                        " does not exist " + sipRequest.getFirstLine()   +
                        "isServerTransaction = " + true);
                    }
                    transaction = null;
                }  else {
                    transaction = serverTransaction;
                }
            }

        
        if (LogWriter.needsLogging) {
            sipStackImpl.logMessage("-----------------");
            sipStackImpl.logMessage(sipRequest.toString());
        }
	// If the transaction is found then it is already managed so
	// dont call the listener.
	if (sipStack.isDialogCreated(sipRequest.getMethod())) {
		if ( (SIPServerTransaction) 
			sipStack.findTransaction(sipRequest,true) != null)  {
		     return;
		}  // TODO check for whether dialog exists here.
	}
        String dialogId = sipRequest.getDialogId(true);
        DialogImpl dialog = sipStackImpl.getDialog(dialogId) ;
	if (dialog != null && transaction != null   ) {
            // Note that route updates are only effective until
             // Dialog is in the confirmed state.
             dialog.addTransaction(transaction);
             dialog.addRoute(sipRequest);
        }
	

	RequestEvent sipEvent;

	if (dialog == null && sipRequest.getMethod().equals(Request.NOTIFY)) {
	     SIPClientTransaction ct = 
			sipStack.findSubscribeTransaction(sipRequest);
	     // From RFC 3265
	     // If the server transaction cannot be found or if it
	     // aleady has a dialog attached to it then just assign the
	     // notify to this dialog and pass it up. 
	     if (ct != null) {
		  transaction.setDialog((DialogImpl)ct.getDialog());
	          if (ct.getDialog().getState() == null) {
	             sipEvent = new RequestEvent
		     (sipProvider, null, (Request) sipRequest);
		  } else {
	             sipEvent = new RequestEvent
		     ((SipProvider)sipProvider, 
			(ServerTransaction)transaction, (Request) sipRequest);
		  }
	      } else {
		   // Got a notify out of the blue - just pass it up 
		   // for stateless handling by the application.
	           sipEvent = new RequestEvent
		     (sipProvider, null, (Request) sipRequest);
	      }

	} else {
	   // For a dialog creating event - set the transaction to null.
	   // The listener can create the dialog if needed.
	   if ( transaction != null &&
		((SIPServerTransaction) transaction).isTransactionMapped() ) 
              sipEvent = new RequestEvent(sipProvider,
               (ServerTransaction) transaction, (Request) sipRequest);
	   else sipEvent = new RequestEvent
		(sipProvider, null, (Request) sipRequest);
	}
        sipProvider.handleEvent(sipEvent,  transaction); 
//ifndef SIMULATION
//
	}
//endif
//

    }
    
    /**
     *Process the response.
     *@exception SIPServerException is thrown when there is an error
     * processing the response
     *@param incomingMessageChannel -- message channel on which the
     * response is received.
     */
    public void processResponse(SIPResponse sipResponse,
    MessageChannel incomingMessageChannel )
    throws SIPServerException {
        if (LogWriter.needsLogging) {
            sipStackImpl.logMessage("PROCESSING INCOMING RESPONSE" +
            sipResponse.encode());
        }
        if (listeningPoint == null) {
            if (LogWriter.needsLogging)
                sipStackImpl.logMessage
                ("Dropping message: No listening point"
                + " registered!");
            return;
        }
        
        SIPTransaction transaction = (SIPTransaction) this.transactionChannel;
        SipProviderImpl sipProvider = listeningPoint.getProvider();
	if (sipProvider == null) {
	    if (LogWriter.needsLogging)  {
		sipStackImpl.logMessage
                ("Dropping message:  no provider");
	    }
	    return;
	}

	SIPTransactionStack sipStack = (SIPTransactionStack) 
				sipProvider.sipStackImpl;
        SipStackImpl sipStackImpl = (SipStackImpl) sipStack;

        if (LogWriter.needsLogging)
            sipStackImpl.logMessage("Transaction = " + transaction);

        if (this.transactionChannel == null) {
	    String dialogId = sipResponse.getDialogId(false);
	    DialogImpl dialog = sipStack.getDialog(dialogId);
	    //  Have a dialog but could not find transaction.
	    if (sipProvider.sipListener == null) {
		 return;
	    } else if ( dialog != null  ) {
		// Bug report by Emil Ivov
		if ( sipResponse.getStatusCode() != Response.OK ) {
			return;
		} else if (sipStackImpl.isRetransmissionFilterActive()) {
		      // 200  retransmission for the final response.
		      if ( sipResponse.getCSeq().equals(
			   dialog.getFirstTransaction().getRequest().
				getHeader(SIPHeaderNames.CSEQ)) ) {
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
            // long receptionTime = System.currentTimeMillis();
            // Pass the response up to the application layer to handle
            // statelessly.

	    // Dialog is null so this is handled statelessly
            ResponseEvent sipEvent =
                new ResponseEvent(sipProvider,null, (Response)sipResponse);
            sipProvider.handleEvent(sipEvent,transaction);
            //transaction.logResponse(sipResponse,
            //       receptionTime,"Retransmission");
            return;
        }
        
        this.rawMessageChannel = incomingMessageChannel;

        // Retrieve the client transaction for which we are getting
        // this response.
        SIPClientTransaction clientTransaction =
        (SIPClientTransaction) this.transactionChannel;
        
        DialogImpl dialog = null;
        if (transaction != null) {
            dialog = (DialogImpl) transaction.getDialog();
            if (LogWriter.needsLogging && dialog == null) {
                sipStackImpl.logMessage("dialog not found for " +
                sipResponse.getFirstLine());
            }
         }
            
         SipListener sipListener = sipProvider.sipListener;
            
         ResponseEvent responseEvent = new javax.sip.ResponseEvent
                (sipProvider,(ClientTransaction)transaction,
			(Response)sipResponse);
         sipProvider.handleEvent(responseEvent,transaction);
            
        }
        /** Get the sender channel.
         */
        public MessageChannel getRequestChannel() {
            return this.transactionChannel;
        }
        
        /** Get the channel if we want to initiate a new transaction to
         * the sender of  a response.
         *@return a message channel that points to the place from where we got
         * the response.
         */
        public MessageChannel getResponseChannel() {
            if (this.transactionChannel != null)
                return this.transactionChannel;
            else return this.rawMessageChannel;
        }
        
        /** Just a placeholder. This is called from the stack
         * for message logging. Auxiliary processing information can
         * be passed back to be  written into the log file.
         *@return auxiliary information that we may have generated during the
         * message processing which is retrieved by the message logger.
         */
        public String getProcessingInfo() {
            return null;
        }
        
        
        
    }
