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
 * File Name     : TimeoutEvent.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import java.util.*;

/**
 * This class represents an Timeout event that is passed from a SipProvider to
 * its SipListener. A specific message may need retransmitted on a specific
 * transaction numerous times before it is acknowledged by the receiver. If the
 * message is not acknowledged after a specified period in the underlying
 * implementation the transaction will expire, this occurs usually
 * after seven retransmissions. The mechanism to alert an application that a
 * message for a an underlying transaction needs retransmitted (i.e. 200OK) or 
 * an underlying transaction has expired is a Timeout Event.
 * <p>
 * A Timeout Event can be of two different types, namely:
 * <ul>
 * <li>{@link Timeout#RETRANSMIT}
 * <li>{@link Timeout#TRANSACTION}
 * </ul>
 * A TimeoutEvent contains the following information:
 * <ul>
 * <li>source - the SipProvider that sent the TimeoutEvent.
 * <li>transaction - the transaction that this Timeout applies to.
 * <li>isServerTransaction - boolean indicating whether the transaction refers to
 * a client or server transaction.
 * <li>timeout - indicates what type of {@link Timeout} occurred.
 * </ul>
 *
 * @see Timeout
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public class TimeoutEvent extends EventObject {

    /**
    * Constructs a TimeoutEvent to indicate a server retransmission or transaction
    * timeout.
    *
    * @param source - the source of TimeoutEvent. 
    * @param serverTransaction - the server transaction that timed out.
    * @param timeout - indicates if this is a retranmission or transaction 
    * timeout event.
    */
    public TimeoutEvent(Object source, ServerTransaction serverTransaction, Timeout timeout) {
        super(source);
        m_serverTransaction = serverTransaction;
        m_isServerTransaction = true;
        m_timeout = timeout;
   }


    /**
    * Constructs a TimeoutEvent to indicate a client retransmission or transaction
    * timeout.
    *
    * @param source - source of TimeoutEvent.
    * @param clientTransaction - the client transaction that timed out.
    * @param timeout - indicates if this is a retranmission or transaction 
    * timeout event.
    */
    public TimeoutEvent(Object source, ClientTransaction clientTransaction, Timeout timeout) {
        super(source);
        m_clientTransaction = clientTransaction;
        m_isServerTransaction = false;
        m_timeout = timeout;
   }    

    /**
    * Gets the server transaction associated with this TimeoutEvent.
    *
    * @return server transaction associated with this TimeoutEvent, or null if this 
    * event is specific to a client transaction.
    */
    public ServerTransaction getServerTransaction(){
        return m_serverTransaction;
    }


    /**
    * Gets the client transaction associated with this TimeoutEvent.
    *
    * @return client transaction associated with this TimeoutEvent, or null if 
    * this event is specific to a server transaction.
    */
    public ClientTransaction getClientTransaction(){
        return m_clientTransaction;
    }   

    /**
    * Indicates if the transaction associated with this TimeoutEvent is a server
    * transaction.
    *
    * @return returns true if a server transaction or false if a client 
    * transaction.
    */
    public boolean isServerTransaction() {
        return m_isServerTransaction;
    }

    /**
    * Gets the event type of this TimeoutEvent. The event type can be used to 
    * determine if this Timeout Event is one of the following types:
    * <ul>
    * <li>{@link Timeout#TRANSACTION}
    * <li>{@link Timeout#RETRANSMIT}
    * </ul>
    *
    * @return the event type of this TimeoutEvent
    */
    public Timeout getTimeout() {
        return m_timeout;
    }


    // internal variables
    private Timeout m_timeout;
    private boolean m_isServerTransaction;
    private ServerTransaction m_serverTransaction = null;
    private ClientTransaction m_clientTransaction = null;

}

