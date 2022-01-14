/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright � 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright � 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. 
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : Transaction.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *  1.2     12/15/2004  M. Ranganathan      Clarified behavior of getDialog when 
 *                      Phelim O'Doherty    AUTOMATIC_DIALOG_SUPPORT is set to off.
 *                                          Added two methods - set/getApplicationData
 *                                          Added terminate method.    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import javax.sip.Dialog;
import javax.sip.message.Request;
import java.io.Serializable;

/**
 * Transactions are a fundamental component of SIP. A transaction is a request 
 * sent by a client transaction to a server transaction, along with all 
 * responses to that request sent from the server transaction back to the client
 * transactions. User agents contain a transaction layer, as do stateful proxies. 
 * Stateless proxies do not contain a transaction layer. This specification 
 * provides the capabilities to allow either the SipProvider or SipListener to 
 * handle transactional functionality.
 * <p>
 * This interface represents a generic transaction interface defining the methods
 * common between client and server transactions.
 *
 * @see TransactionState
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface Transaction extends Serializable{

    /**
     * Gets the dialog object of this transaction object. A dialog only 
     * exists for a transaction when a session is setup between a User Agent 
     * Client and a User Agent Server, either by a 1xx Provisional Response 
     * for an early dialog or a 200OK Response for a committed dialog.
     * 
     * <ul>
     * <li>If the stack is configured with the AUTOMATIC_DIALOG_SUPPORT property set to
     * </it>ON</it> ( default behavior ) then the following behavior is defined:
     * <ul>
     * <li>If the transaction is associated with an existing Dialog or could result
     * in a Dialog being created in the future (ie. the stack is configured
     * to recognize the method as a Dialog creating method or is one of the
     * natively supported dialog creating methods such as INVITE, SUBSCRIBE or
     * REFER), then the implementation must either associate the transaction
     * with the existing Dialog or create a Dialog with null state. 
     * <li>If the Transaction is neither dialog creating nor can be associated with
     * an existing dialog, then the implementation must return null when the
     * application issues getDialog on the transaction.
     * </ul>
     * <li>If the stack is configured with AUTOMATIC_DIALOG property set to </it>OFF</it>
     * then the stack does not automatically create a Dialog for a transaction nor does 
     * it maintain an association between dialog and transaction on behalf of the
     * application. Hence this method will return null.
     * It is the responsibility of the application to create a Dialog and associate
     * it with the transaction when the response is sent. 
     * </ul>
     *
     * @return the dialog object of this transaction object or null if no 
     * dialog exists.
     * @see Dialog
     */    
    public Dialog getDialog();    

    /**
     * Returns the current state of the transaction. Returns the current 
     * TransactionState of this Transaction or null if a ClientTransaction has 
     * yet been used to send a message.
     *
     * @return a TransactionState object determining the current state of the 
     * transaction.
     */    
    public TransactionState getState();
 
    
    /**
     * Returns the current value of the retransmit timer in milliseconds used 
     * to retransmit messages over unreliable transports for this transaction.
     *
     * @return the integer value of the retransmit timer in milliseconds.
     * @throws UnsupportedOperationException if this method is not supported
     * by the underlying implementation.
     */    
    public int getRetransmitTimer() throws UnsupportedOperationException;  
    
    /**
     * Sets the value of the retransmit timer to the newly supplied timer value. 
     * The retransmit timer is expressed in milliseconds and its default value 
     * is 500ms. This method allows the application to change the transaction 
     * retransmit behavior for different networks. For example the gateway proxy,  
     * the internal intranet is likely to be relatively uncongested 
     * and the endpoints will be relatively close. The external network is the 
     * general Internet. This functionality allows different retransmit times 
     * for either side. 
     *
     * @param retransmitTimer - the new integer value of the retransmit timer 
     * in milliseconds.
     * @throws UnsupportedOperationException if this method is not supported
     * by the underlying implementation. 
     */    
    public void setRetransmitTimer(int retransmitTimer) 
                                throws UnsupportedOperationException;    
    
    /**
     * Returns a unique branch identifer that identifies this transaction. The
     * branch identifier is used in the ViaHeader. The uniqueness property of 
     * the branch ID parameter to facilitate its use as a transaction ID, was 
     * not part of RFC 2543. The branch ID inserted by an element compliant 
     * with the RFC3261 specification MUST always begin with the characters 
     * "z9hG4bK". These 7 characters are used as a magic cookie, so that 
     * servers receiving the request can determine that the branch ID was 
     * constructed to be globally unique. The precise format of the branch 
     * token is implementation-defined. This method should always return the 
     * same branch identifier for the same transaction.
     *
     * @return the new branch that uniquely identifies this transaction.
     */
    public String getBranchId(); 
 
    
    /**
     * Returns the request that created this transaction. The transaction state 
     * machine needs to keep the Request that resulted in the creation of this 
     * transaction while the transaction is still alive. Applications also need 
     * to access this information, e.g. a forking proxy server may wish to 
     * retrieve the original Invite request to cancel branches of a fork when 
     * a final Response has been received by one branch.
     *
     * @return the Request message that created this transaction. 
     * 
     */
    public Request getRequest(); 
    
    
    /**
     * This method allows applications to associate application context with 
     * the transaction. This specification does not define the format of this 
     * data, this the responsibility of the application and is dependent 
     * on the application. This capability may be useful for proxy servers 
     * to associate the transaction to some application state. The context of 
     * this application data is un-interpreted by the stack.
     * 
     * @param applicationData - un-interpreted application data.
     * @since v1.2
     *
     */
    
    public void setApplicationData (Object applicationData);
    
    
    /**
     * Returns the application data associated with the transaction.This
     * specification does not define the format of this application specific
     * data. This is the responsibility of the application. 
     * 
     * @return application data associated with the transaction by the application.
     * @since v1.2
     *
     */
    public Object getApplicationData();
    
    /**
     * Terminate this transaction and immediately release all stack resources 
     * associated with it. When a transaction is terminated using this method, 
     * a transaction terminated event is sent to the listener. If the 
     * transaction is already associated with a dialog, it cannot be terminated 
     * using this method. Instead, the dialog should be deleted to remove the 
     * transaction.
     * 
     * @throws ObjectInUseException if the transaction cannot be terminated as 
     * it is associated to a dialog.
     * @since v1.2
     */
    public void terminate() throws ObjectInUseException;
 
    
}

