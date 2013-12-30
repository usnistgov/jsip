package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.ServerTransactionExt;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;

import java.io.IOException;

import javax.sip.Dialog;
import javax.sip.ObjectInUseException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.TransactionState;
import javax.sip.message.Response;

public interface SIPServerTransaction extends SIPTransaction, ServerTransaction, ServerTransactionExt, ServerRequestInterface {

  public static final String CONTENT_TYPE_APPLICATION = "application";
  public static final String CONTENT_SUBTYPE_SDP = "sdp";

  // force the listener to see transaction

  /**
   * Sets the real RequestInterface this transaction encapsulates.
   *
   * @param newRequestOf RequestInterface to send messages to.
   */
  public void setRequestInterface(ServerRequestInterface newRequestOf);

  /**
   * Returns this transaction.
   */
  public  MessageChannel getResponseChannel();

  /**
   * Determines if the message is a part of this transaction.
   *
   * @param messageToTest Message to check if it is part of this transaction.
   *
   * @return True if the message is part of this transaction, false if not.
   */
  public  boolean isMessagePartOfTransaction(SIPMessage messageToTest);
  
  /**
   * Return true if the transaction is known to stack.
   */
  public  boolean isTransactionMapped();
  

  /**
   * Set if the transaction is known to stack.
   * @param transactionMapped
   */
  public void setTransactionMapped(boolean transactionMapped);

  /**
   * Process a new request message through this transaction. If necessary, this message will
   * also be passed onto the TU.
   *
   * @param transactionRequest Request to process.
   * @param sourceChannel Channel that received this message.
   */
  public  void processRequest(SIPRequest transactionRequest, MessageChannel sourceChannel);

  /**
   * Send a response message through this transaction and onto the client. The response drives
   * the state machine.
   *
   * @param messageToSend Response to process and send.
   */
  public  void sendMessage(SIPMessage messageToSend) throws IOException;

  public  String getViaHost();

  public  int getViaPort();

  // jeand we nullify the last response very fast to save on mem and help GC but we keep it as byte array
  // so this method is used to resend the last response either as a response or byte array depending on if it has been nullified
  public  void resendLastResponseAsBytes() throws IOException;

  /**
   * Get the last response status code.
   */
  public  int getLastResponseStatusCode();

  /**
   * Set the original request.
   */
  public  void setOriginalRequest(SIPRequest originalRequest);

  /*
   * (non-Javadoc)
   *
   * @see javax.sip.ServerTransaction#sendResponse(javax.sip.message.Response)
   */
  public  void sendResponse(Response response) throws SipException;

  /**
   * Return the current transaction state according to the RFC 3261 transaction state machine.
   * Invite transactions do not have a trying state. We just use this as a pseudo state for
   * processing requests.
   *
   * @return the state of the transaction.
   */
  public  TransactionState getState();

  /**
   * Sets a timeout after which the connection is closed (provided the server does not use the
   * connection for outgoing requests in this time period) and calls the superclass to set
   * state.
   */
  public  void setState(int newState);

  public  boolean equals(Object other);

  /*
   * (non-Javadoc)
   *
   * @see gov.nist.javax.sip.stack.SIPTransaction#getDialog()
   */
  public  Dialog getDialog();

  /*
   * (non-Javadoc)
   *
   * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
   *      gov.nist.javax.sip.message.SIPMessage)
   */
  public  void setDialog(SIPDialog sipDialog, String dialogId);

  /*
   * (non-Javadoc)
   *
   * @see javax.sip.Transaction#terminate()
   */
  public  void terminate() throws ObjectInUseException;

  public  byte[] getReliableProvisionalResponse();

  /**
   * Cancel the retransmit timer for the provisional response task.
   *
   * @return true if the tx has seen the prack for the first time and false otherwise.
   *
   */
  public  boolean prackRecieved();

  public  void enableRetransmissionAlerts() throws SipException;

  public  boolean isRetransmissionAlertEnabled();

  /**
   * Disable retransmission Alerts and cancel associated timers.
   *
   */
  public  void disableRetransmissionAlerts();

  /**
   * This is book-keeping for retransmission filter management.
   */
  public  void setAckSeen();

  /**
   * This is book-keeping for retransmission filter management.
   */
  public  boolean ackSeen();

  public  void setMapped(boolean b);

  public  void setPendingSubscribe(SIPClientTransaction pendingSubscribeClientTx);

  public  void releaseSem();

  /**
   * The INVITE Server Transaction corresponding to a CANCEL Server Transaction.
   *
   * @param st -- the invite server tx corresponding to the cancel server transaction.
   */
  public  void setInviteTransaction(SIPServerTransaction st);

  /**
   * TODO -- this method has to be added to the api.
   *
   * @return
   */
  public  SIPServerTransaction getCanceledInviteTransaction();

  public  void scheduleAckRemoval() throws IllegalStateException;

  // jeand cleanup the state of the stx to help GC
  public  void cleanUp();

  /**
   * @return the pendingReliableResponseMethod
   */
  public  String getPendingReliableResponseMethod();

  /**
   * @return the pendingReliableCSeqNumber
   */
  public  long getPendingReliableCSeqNumber();

  /**
   * @return the pendingReliableRSeqNumber
   */
  public  long getPendingReliableRSeqNumber();

  public  void waitForTermination();

  void sendReliableProvisionalResponse(Response relResponse) throws SipException;

  /**
   * Send out a trying response (only happens when the transaction is mapped). Otherwise the
   * transaction is not known to the stack.
   */
  void map();

}
