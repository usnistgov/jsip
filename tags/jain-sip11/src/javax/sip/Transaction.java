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
 * File Name     : Transaction.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
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
 * @author Sun Microsystems
 * @since v1.1
 */

public interface Transaction extends Serializable{

    /**
     * Gets the dialog object of this transaction object. A dialog only 
     * exists for a transaction when a session is setup between a User Agent 
     * Client and a User Agent Server, either by a 1xx Provisional Response 
     * for an early dialog or a 200OK Response for a committed dialog.
     * <p>
     * An implementation must always associate a dialog with a transaction 
     * which may result in the creation of a 'dummy' dialog so that the 
     * application may always query the dialog  from the Transaction. However 
     * if a dialog is not yet initialized, the Dialog.getState() must return 
     * null to indicate that the dialog has been created but is not yet 
     * mapped by the stack to any specific state.
     *
     * @return the dialog object of this transaction object.
     * @see Dialog
     */    
    public Dialog getDialog();    

    /**
     * Returns the current state of the transaction. The allowable states for 
     * client and server transactions are defined in their respective objects.
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
     */
    public Request getRequest(); 
 
    
}

