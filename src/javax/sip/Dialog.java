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
 * File Name     : Dialog.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import javax.sip.address.Address;
import javax.sip.message.Request;
import java.util.Iterator;
import javax.sip.header.CallIdHeader;
import java.io.Serializable;

/** 
 * A dialog represents a peer-to-peer SIP relationship between two user agents 
 * that persists for some time. 
 * The dialog facilitates sequencing of messages between the user agents 
 * and proper routing of requests between both of them. The dialog represents 
 * a context in which to interpret SIP messages. However method independent 
 * User Agent processing for requests and responses outside of a dialog exists, 
 * hence a dialog is not necessary for message processing. 
 * <p> 
 * A dialog is identified at each User Agent with a dialog Id, which consists 
 * of a Call-Id value, a local tag and a remote tag. The dialog Id at each
 * User Agent involved in the dialog is not the same. Specifically, the local
 * tag at one User Agent is identical to the remote tag at the peer User Agent. 
 * The tags are opaque tokens that facilitate the generation of unique dialog Ids.
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
 * <li> Remote target - the address from the Contact header field of the  
 * request or response or refresh request or response.
 * <li> "secure" boolean - determines if the dialog is secure i.e. use the 
 * <var>sips:</var> scheme.
 * <li> Route set - an ordered list of URIs. The route set is the list of 
 * servers that need to be traversed to send a request to the peer. 
 * </ul>
 * A dialog also has its own state machine, the current {@link DialogState} is 
 * determined by the sequence of messages that occur on the initial dialog.
 * <p>
 * <b>Dialog States:</b><br>
 * Early --> Confirmed --> Completed --> Terminated
 *
 * @author Sun Microsystems
 * @since v1.1
 */

public interface Dialog extends Serializable{

    /** 
     * Returns the Address identifying the local party. This is the value of 
     * the From header of locally initiated requests in this dialog when
     * acting as an User Agent Client.  
     * <p>
     * This is the value of the To header of recieved responses in this 
     * dialog when acting as an User Agent Server.           
     *
     * @return the address object of the local party. 
     */
     public Address getLocalParty();

    /** 
     * Returns the Address identifying the remote party. This is the value of 
     * the To header of locally initiated requests in this dialog when
     * acting as an User Agent Client.
     * <p>
     * This is the value of the From header of recieved responses in this 
     * dialog when acting as an User Agent Server.           
     *
     *@return the address object of the remote party. 
     */
     public Address getRemoteParty();

    /** 
     * Returns the Address identifying the remote target. This is the value of 
     * the Contact header of recieved Responses for Requests or refresh Requests 
     * in this dialog when acting as an User Agent Client.  
     * <p>
     * This is the value of the Contact header of recieved Requests or refresh 
     * Requests in this dialog when acting as an User Agent Server.           
     *
     * @return the address object of the remote target. 
     */
     public Address getRemoteTarget();

     
    /** 
     * Get the dialog identifer of this dialog. A dialog Id is  
     * associated with all responses and with any request that contains a tag 
     * in the To field.  The rules for computing the dialog Id of a message 
     * depends on whether the SIP element is a User Agent Client or User Agent
     * Server and applies to both requests and responses. 
     * <ul>
     * <li>User Agent Client - the Call-Id value of the dialog Id is set to the 
     * Call-Id of the message, the remote tag is set to the tag in the To field 
     * of the message, and the local tag is set to the tag in the From field of 
     * the message. 
     * <li>User Agent Server - the Call-Id value of the dialog Id is set to the 
     * Call-Id of the message, the remote tag is set to the tag in the From 
     * field of the message, and the local tag is set to the tag in the To 
     * field of the message.
     * </ul>
     *
     * @return the string identifier for this dialog.
     */
     public String getDialogId();
        
     /**
      * Returns the Call-Id for this SipSession. This is the value of the 
      * Call-Id header for all messages belonging to this session.
      * 
      * @return the Call-Id for this dialog
      */
      public CallIdHeader getCallId();        


    /**
     * The local sequence number is used to order requests from this 
     * User Agent Client to its peer User Agent Server. The local sequence 
     * number MUST be set to the value of the sequence number in the CSeq 
     * header field of the request. The remote sequence number MUST be empty as
     * it is established when the remote User Agent sends a request within the 
     * dialog.
     * <p>
     * Requests within a dialog MUST contain strictly monotonically increasing 
     * and contiguous CSeq sequence numbers (increasing-by-one) in each 
     * direction (excepting ACK and CANCEL, whose numbers equal the requests 
     * being acknowledged or cancelled). Therefore, if the local sequence number 
     * is not empty, the value of the local sequence number MUST be incremented 
     * by one, and this value MUST be placed into the CSeq header field. If the 
     * local sequence number is empty, an initial value MUST be chosen.
     *
     *@return the integer value of the local sequence number, returns zero if
     * not set.
     */
     public int getLocalSequenceNumber();
        
    /**
     * The remote sequence number is used to order requests from its 
     * peer User Agent Client to this User Agent Server. When acting an User 
     * Agent Server the remote sequence number MUST be set to the value of the 
     * sequence number in the CSeq header field of the request from the User 
     * Agent Client. The local sequence number MUST be empty.  
     * <p>
     * If the remote sequence number is empty, it MUST be set to the value of 
     * the sequence number in the CSeq header field value in the request. If 
     * the remote sequence number was not empty, but the sequence number of the 
     * request is lower than the remote sequence number, the request is out of 
     * order and MUST be rejected with a 500 (Server Internal Error) response. 
     * If the remote sequence number was not empty, and the sequence number of 
     * the request is greater than the remote sequence number, the request is 
     * in order.
     *
     *@return the integer value of the remote sequence number, return zero if 
     * not set.
     */
     public int getRemoteSequenceNumber();
 
    /**
     * When acting as an User Agent Server
     * the routeset MUST be set to the list of URIs in the Record-Route header 
     * field from the request, taken in order and preserving all URI parameters.  
     * When acting as an User Agent Client the route set MUST be set to the list 
     * of URIs in the Record-Route header field from the response, taken in 
     * reverse order and preserving all URI parameters. If no Record-Route 
     * header field is present in the request or response, the route set MUST 
     * be set to the empty set. This route set, even if empty, overrides any 
     * pre-existing route set for future requests in this dialog.
     * <p>
     * Requests within a dialog MAY contain Record-Route and Contact header
     * fields. However, these requests do not cause the dialog's route set to be 
     * modified.
     * <p>
     * The User Agent Client uses the remote target and route set to build the 
     * Request-URI and Route header field of the request.
     *
     * @return an Iterator containing a list of route headers to be used for 
     * forwarding.
     */
     public Iterator getRouteSet();        

    /**
     * Returns true if this Dialog is secure i.e. if the request was sent over 
     * a "sips:" scheme, or a "sip:" scheme over TLS.
     *
     * @return <code>true</code> if this dialog was secure, and 
     * <code>false</code> otherwise.
     */     
     public boolean isSecure();

    /**
     * Returns whether this Dialog is a server dialog. A proxy may wish to keep 
     * an associated sets of dialogs for forking, i.e. a single server dialog is 
     * associated with multiple client dialogs.
     *
     * @return <code>true</code> if this is a server dialog and <code>false</code> 
     * if it is a client dialog.
     */     
     public boolean isServer();     
  
     /**
      * This method may be used to increment the local sequence number of the 
      * dialog when an application wishes to switch from dialog stateful mode 
      * to transaction stateful mode for client transactions and back again 
      * to dialog stateful mode. Note, the Dialog layer automatically 
      * increments the local sequence number when a request is sent out via 
      * the Dialog. However in special circumstances applications may wish to 
      * send a request (from a sequence of dialog requests) outside of the 
      * Dialog using the {@link ClientTransaction#sendRequest()} method. When sending
      * a request using the Transaction the Dialog state is unaffected.
      */
     public void incrementLocalSequenceNumber();
     
    /**
     * Creates a new Request message based on the dialog creating request. 
     * This method should be used for but not limited to creating Bye's, 
     * Refer's and re-Invite's on the Dialog. The returned Request will be 
     * correctly formatted that is the Dialog implementation is responsible 
     * for assigning the following:
     * <ul> 
     * <li> RequestURI
     * <li> FromHeader 
     * <li> ToHeader 
     * <li> CallIdHeader 
     * <li> RouteHeaders
     * </ul>
     * The CSeqHeader will be set when the message is sent. If this method
     * returns a CSeqHeader in the Request it may be overwritten again by
     * the {@link Dialog#sendRequest(ClientTransaction)} method. Therefore
     * any Request created by this method must be sent via the 
     * {@link Dialog#sendRequest(ClientTransaction)} method and not via its
     * {@link ClientTransaction#sendRequest()} method.
     * <p> 
     * All other headers including any Authentication related headers, and 
     * record route headers should be assigned by the application to the 
     * generated request. The  assignment of the topmost via header for the 
     * outgoing request may be deferred until the application creates a 
     * ClientTransaction to send the request out. This method does not 
     * increment the dialog sequence number.
     *
     * @param method the string value that determines if the request to be  
     * created.
     * @return the newly created Request message on this Dialog.
     * @throws SipException if the Dialog is not yet established (i.e. 
     * dialog state equals null) or is terminated.
     */
    public Request createRequest(String method) throws SipException;     
     
    
    /**
     * Sends a Request to the remote party of this dialog. When an application 
     * wishes to send a Request message on this dialog, it creates a Request 
     * and creates a new ClientTransaction to handle this request from 
     * {@link SipProvider#getNewClientTransaction(Request)}. This 
     * ClientTransaction is passed to this method to send the request. The Request 
     * message gets sent via the ListeningPoint information of the SipProvider 
     * that is associated to this ClientTransaction.
     *<p>
     * This method implies that the application is functioning as UAC hence the 
     * underlying SipProvider acts statefully. This method is useful for 
     * sending Bye's to terminate a dialog or Re-Invites/Refers on the 
     * Dialog for third party call control, call hold etc.
     * <p>
     * This methods will set the From and the To tags for the outgoing 
     * request. This method increments the dialog sequence number and sets 
     * the correct sequence number to the outgoing Request and associates 
     * the client transaction with this dialog. 
     * Note that any tags assigned by the user will be over-written by this 
     * method. If the caller sets no RouteHeader in the Request to be sent out, 
     * the implementation of this method will add the RouteHeader from the 
     * routes that are mantained in the dialog. If the caller sets the 
     * RouteHeader's, the implementation will leave the route headers 
     * unaltered. This allows the application to manage its own route set if 
     * so desired.
     * <p>
     * The User Agent traditionally must not send a BYE on a confirmed INVITE until it has 
     * received an ACK for its 2xx response or until the server transaction 
     * timeout is received.
     * <p>
     * When the {@link SipStack#isRetransmissionFilterActive()} is <code>true</code>, 
     * that is the SipProvider takes care of all retransmissions for the 
     * application, and the SipProvider can not deliver the Request after 
     * multiple retransmits the SipListener will be notified with a 
     * {@link TimeoutEvent} when the transaction expires.
     *
     * @param clientTransaction - the new ClientTransaction object identifying 
     * this transaction, this clientTransaction should be requested from 
     * {@link SipProvider#getNewClientTransaction(Request)}
     * @throws TransactionDoesNotExistException if the clientTransaction does
     * not correspond to any existing client transaction.
     * @throws SipException if implementation cannot send the Request for 
     * any reason.
     */
    public void sendRequest(ClientTransaction clientTransaction) 
                                    throws TransactionDoesNotExistException, 
                                           SipException;

    /**
     * Sends ACK Request to the remote party of this dialog. This method 
     * implies that the application is functioning as User Agent Client hence 
     * the underlying SipProvider acts statefully. This method does not 
     * increment the local sequence number.
     *
     * @param ackRequest - the new ACK Request message to send. 
     * @throws SipException if implementation cannot send the ACK Request for 
     * any reason
     */
    public void sendAck(Request ackRequest) throws SipException;
    
    /**
     * Returns the current state of the dialog. A dialog that is created but 
     * not yet mapped to any state must return null, multiple requests can be
     * generated on the Dialog in a null state, e.g. pseudo dialog's. The 
     * dialog states are:
     * <ul>
     * <li> Early - A dialog is in the "early" state, which occurs when it is 
     * created when a provisional response is recieved to the INVITE Request.
     * <li> Confirmed - A dialog transitions to the "confirmed" state when a 2xx 
     * final response is received to the INVITE Request.
     * <li> Completed - A dialog transitions to the "completed" state when a BYE 
     * request is sent or received by the User Agent Client.
     * <li> Terminated - A dialog transitions to the "terminated" state when it 
     * can be garbage collection.
     * </ul>
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
     * This method will release all resources associated with this dialog 
     * that are tracked by the SipProvider. Further references to the dialog by 
     * incoming messages will result in a mismatch. This delete method is provided 
     * for future use and extension methods that do not require a BYE to 
     * terminate a dialog. The basic case of the INVITE and all dialogs 
     * that we are aware of today it is expected that BYE requests will end the 
     * dialog.
     */     
     public void delete();
     

    /**
     * This method retrieves the transaction which resulted in the creation of 
     * this Dialog. The transaction type either server or client can be determined 
     * based on whether this is a server or client Dialog, see 
     * {@link Dialog#isServer()}. 
     *
     * @return the Transaction that created the Dialog.
     */
     public Transaction getFirstTransaction();
     
     
    /** 
     * Get the Local Tag of this Dialog. On the client side, this tag is 
     * assigned to outgoing From headers for Requests within the dialog and 
     * To headers for responses within the dialog. On the server side, this 
     * tag is associated with outgoing To headers for responses within the 
     * dialog.
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
     * Sets application specific data to this dialog. This specification 
     * does not define the format of this data. This is the responsibility
     * of the application and is dependent upon the application. This method 
     * can be used to link the call state of this dialog to other state, SIP 
     * or otherwise in the system. For example this method could be used by 
     * a SIP-to-H323 interworking node that would associate the H323 call state 
     * associated with a call on the H323 side with this dialog that 
     * represents this call on the SIP side. Or a dialog stateful proxy can 
     * associate the UAS dialog to the UAC dialog and vice versa.
     *
     * @param applicationData the new object containing application specific
     * data. 
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
     

}

