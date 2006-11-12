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
 * File Name     : TransactionTerminatedEvent.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.2     03/05/2005  M. Ranganathan      New class
 *                      
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

import java.util.EventObject;

/**
 * 
 * TransactionTerminatedEvent is delivered to the Listener when the
 * transaction transitions to the terminated state. An implementation
 * is expected to deliver this event to the listener when it discards
 * all internal book keeping records for a given transaction - thereby
 * allowing the Listener to unmap its own data structures.
 * 
 * @author BEA Systems, NIST
 * @since v1.2
 *
 */
public class TransactionTerminatedEvent extends EventObject {
    
    /**
     * Constructs a TransactionTerminatedEvent to indicate a server retransmission 
     * or transaction timeout.
     *
     * @param source - the source of TransactionTerminatedEvent (the SipProvider associated with the transaction). 
     * @param serverTransaction - the server transaction that timed out.
     */
     public TransactionTerminatedEvent(Object source, 
             ServerTransaction serverTransaction) {
         super(source);
         m_serverTransaction = serverTransaction;
         m_isServerTransaction = true;
      
    }

     /**
     * Constructs a TransactionTerminatedEvent to indicate a client 
     * retransmission or transaction timeout.
     *
     * @param source - source of TransactionTerminatedEvent (the SipProvider associated with the transaction).
     * @param clientTransaction - the client transaction that timed out.
     */
     public TransactionTerminatedEvent(Object source, 
             ClientTransaction clientTransaction) {
         super(source);
         m_clientTransaction = clientTransaction;
         m_isServerTransaction = false;
     }    

     /**
     * Gets the server transaction associated with this TransactionTerminatedEvent.
     *
     * @return server transaction associated with this TransactionTerminatedEvent, 
     * or null if this event is specific to a client transaction.
     */
     public ServerTransaction getServerTransaction(){
         return m_serverTransaction;
     }


     /**
     * Gets the client transaction associated with this TransactionTerminatedEvent.
     *
     * @return client transaction associated with this TransactionTerminatedEvent, 
     * or null if this event is specific to a server transaction.
     */
     public ClientTransaction getClientTransaction(){
         return m_clientTransaction;
     }   

     /**
     * Indicates if the transaction associated with this 
     * TransactionTerminatedEvent is a server transaction.
     *
     * @return returns true if a server transaction or false if a client 
     * transaction.
     */
     public boolean isServerTransaction() {
         return m_isServerTransaction;
     }
   

     // internal variables
     private boolean m_isServerTransaction;
     private ServerTransaction m_serverTransaction = null;
     private ClientTransaction m_clientTransaction = null;

}

