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
 * File Name     : ServerTransaction.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;


import javax.sip.message.Response;

/**
 * A server transaction is used by a SipProvider to handle incoming Request 
 * messages to fire Request events to the SipListener on a specific server 
 * transaction and by a User Agent Server application to send Response 
 * messages to a User Agent Client application. This interfaces enables an 
 * application to send a {@link javax.sip.message.Response} to a recently 
 * received Request statefully.
 * <p>
 * A new server transaction is generated in the following ways:
 * <ul>
 * <li> By the application by invoking the 
 * {@link SipProvider#getNewServerTransaction(Request)} for Dialog-Creating 
 * Requests that the application wishes to handle.
 * <li> By the SipProvider by automatically populating the server transaction 
 * of a RequestEvent for Incoming Requests in an existing Dialog handled by 
 * the application.
 * </ul>
 * A server transaction of the transaction layer is represented by a finite 
 * state machine that is constructed to process a particular request under 
 * the covers of a stateful SipProvider. The transaction layer handles 
 * application-layer retransmissions, matching of responses to requests, and 
 * application-layer timeouts. 
 * <p>
 * The server transaction must be unique within the underlying 
 * implementation. A common way to create this value is to compute a
 * cryptographic hash of the To tag, From tag, Call-ID header field, the 
 * Request-URI of the request received (before translation), the topmost Via 
 * header, and the sequence number from the CSeq header field, in addition to 
 * any Proxy-Require and Proxy-Authorization header fields that may be present.  
 * The algorithm used to compute the hash is implementation-dependent.
 * <p>
 * For the detailed server transaction state machines refer to Chapter 
 * 17 of <a href="http://www.ietf.org/rfc/rfc3261.txt">RFC 3261</a>, the 
 * allowable transitions are summarized below:
 * <p>
 * <b>Invite Transaction:</b><br>
 * Proceeding --> Completed --> Confirmed --> Terminated
 * <p>
 * <b>Non-Invite Transaction:</b><br>
 * Trying --> Proceeding --> Completed --> Terminated
 * 
 * @author  Sun Microsystems
 * @since v1.1
 */
public interface ServerTransaction extends Transaction {

    /**
     * Sends the Response to a Request which is identified by this 
     * ServerTransaction. When an application wishes to send a Response, it 
     * creates a Response from the {@link javax.sip.message.MessageFactory} and 
     * then passes that Response to this method. The Response message gets sent out on 
     * the network via the ListeningPoint information that is associated to 
     * the SipProvider of this ServerTransaction.
     * <p>
     * This method implies that the application is functioning as either a UAS 
     * or a stateful proxy, hence the underlying implementation acts statefully.
     * When a UAS sends a 2xx response to an INVITE, the server transaction is 
     * destroyed, by the underlying implementation. This means that 
     * when the ACK sent by the corresponding UAC arrives at the UAS, there will 
     * be no matching server transaction for the ACK, and based on this rule, 
     * the ACK is passed to the UAS application, where it is processed. This 
     * ensures that the three way handsake of an INVITE is managed by the UAS 
     * application and not the implementation. However when the 
     * {@link SipStack#isRetransmissionFilterActive()} is turned ON for User Agent's 
     * the implementation will take care of this behaviour for the application and 
     * notify the application of an error with a {@link Timeout#TRANSACTION} 
     * Event.
     *
     * @param response the Response to send to the Request.
     * @throws SipException if the SipProvider cannot send the Response for any
     * other reason.
     * @see Response
     */
    public void sendResponse(Response response) throws SipException;    
    
}

