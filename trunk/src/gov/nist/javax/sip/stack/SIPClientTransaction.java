package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.Event;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPResponse;

import java.io.IOException;

import javax.sip.Dialog;
import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.address.Hop;
import javax.sip.message.Request;

public interface SIPClientTransaction extends ClientTransactionExt, SIPTransaction, ServerResponseInterface {

  /**
   * Sets the real ResponseInterface this transaction encapsulates.
   * 
   * @param newRespondTo ResponseInterface to send messages to.
   */
  public abstract void setResponseInterface(ServerResponseInterface newRespondTo);

  /**
   * Returns this transaction.
   */
  public abstract MessageChannel getRequestChannel();

  /**
   * Deterines if the message is a part of this transaction.
   * 
   * @param messageToTest Message to check if it is part of this transaction.
   * 
   * @return true if the message is part of this transaction, false if not.
   */
  public abstract boolean isMessagePartOfTransaction(SIPMessage messageToTest);

  /**
   * Send a request message through this transaction and onto the client.
   * 
   * @param messageToSend Request to process and send.
   */
  public abstract void sendMessage(SIPMessage messageToSend) throws IOException;

  /**
   * Process a new response message through this transaction. If necessary, this message will
   * also be passed onto the TU.
   * 
   * @param transactionResponse Response to process.
   * @param sourceChannel Channel that received this message.
   */
  public abstract void processResponse(SIPResponse transactionResponse,
                                       MessageChannel sourceChannel,
                                       SIPDialog dialog);

  /*
   * (non-Javadoc)
   * 
   * @see javax.sip.ClientTransaction#sendRequest()
   */
  public abstract void sendRequest() throws SipException;

  /*
   * (non-Javadoc)
   * 
   * @see javax.sip.ClientTransaction#createCancel()
   */
  public abstract Request createCancel() throws SipException;

  /*
   * (non-Javadoc)
   * 
   * @see javax.sip.ClientTransaction#createAck()
   */
  public abstract Request createAck() throws SipException;

  /**
   * Set the port of the recipient.
   */
  public abstract void setViaPort(int port);

  /**
   * Set the port of the recipient.
   */
  public abstract void setViaHost(String host);

  /**
   * Get the port of the recipient.
   */
  public abstract int getViaPort();

  /**
   * Get the host of the recipient.
   */
  public abstract String getViaHost();

  /**
   * get the via header for an outgoing request.
   */
  public abstract Via getOutgoingViaHeader();

  /**
   * This is called by the stack after a non-invite client transaction goes to completed state.
   */
  public abstract void clearState();

  /**
   * Sets a timeout after which the connection is closed (provided the server does not use the
   * connection for outgoing requests in this time period) and calls the superclass to set
   * state.
   */
  public abstract void setState(int newState);

  /*
   * Terminate a transaction. This marks the tx as terminated The tx scanner will run and remove
   * the tx. (non-Javadoc)
   * 
   * @see javax.sip.Transaction#terminate()
   */
  public abstract void terminate() throws ObjectInUseException;

  /**
   * Stop the ExPIRES timer if it is running.
   */
  public abstract void stopExpiresTimer();

  /**
   * Check if the From tag of the response matches the from tag of the original message. A
   * Response with a tag mismatch should be dropped if a Dialog has been created for the
   * original request.
   * 
   * @param sipResponse the response to check.
   * @return true if the check passes.
   */
  public abstract boolean checkFromTag(SIPResponse sipResponse);

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.stack.ServerResponseInterface#processResponse(gov.nist.javax.sip.message.SIPResponse,
   *      gov.nist.javax.sip.stack.MessageChannel)
   */
  public abstract void processResponse(SIPResponse sipResponse, MessageChannel incomingChannel);

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.stack.SIPTransaction#getDialog()
   */
  public abstract Dialog getDialog();

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
   *      gov.nist.javax.sip.message.SIPMessage)
   */
  public abstract SIPDialog getDialog(String dialogId);

  /*
   * (non-Javadoc)
   * 
   * @see gov.nist.javax.sip.stack.SIPTransaction#setDialog(gov.nist.javax.sip.stack.SIPDialog,
   *      gov.nist.javax.sip.message.SIPMessage)
   */
  public abstract void setDialog(SIPDialog sipDialog, String dialogId);

  public abstract SIPDialog getDefaultDialog();

  /**
   * Set the next hop ( if it has already been computed).
   * 
   * @param hop -- the hop that has been previously computed.
   */
  public abstract void setNextHop(Hop hop);

  /**
   * Reeturn the previously computed next hop (avoid computing it twice).
   * 
   * @return -- next hop previously computed.
   */
  public abstract Hop getNextHop();

  /**
   * Set this flag if you want your Listener to get Timeout.RETRANSMIT notifications each time a
   * retransmission occurs.
   * 
   * @param notifyOnRetransmit the notifyOnRetransmit to set
   */
  public abstract void setNotifyOnRetransmit(boolean notifyOnRetransmit);

  /**
   * @return the notifyOnRetransmit
   */
  public abstract boolean isNotifyOnRetransmit();

  public abstract void alertIfStillInCallingStateBy(int count);

  //jeand : cleanup method to clear the state of the tx once it has been removed from the stack
  public abstract void cleanUp();

  /**
   * @return the originalRequestFromTag
   */
  public abstract String getOriginalRequestFromTag();

  /**
   * @return the originalRequestFromTag
   */
  public abstract String getOriginalRequestCallId();

  /**
   * @return the originalRequestFromTag
   */
  public abstract Event getOriginalRequestEvent();

  /**
   * @return the originalRequestFromTag
   */
  public abstract Contact getOriginalRequestContact();

  /**
   * @return the originalRequestFromTag
   */
  public abstract String getOriginalRequestScheme();
}
