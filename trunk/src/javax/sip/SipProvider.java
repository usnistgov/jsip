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
 * File Name     : SipProvider.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import java.util.*;
import javax.sip.message.*;
import javax.sip.header.*;

/**
 * This interface represents the messaging entity of a SIP stack and as
 * such is the interface that defines the messaging and transactional
 * component view of the SIP stack. It must be implemented by any object
 * representing a SIP stack compliant to this specification that interacts 
 * directly with a proprietary implementation of a SIP stack.
 * This interface defines the methods that enable any registered
 * application implementing the {@link javax.sip.SipListener} interface to:
 * <ul>
 *    <li> Register a {@link javax.sip.SipListener} to the SipProvider. Once 
 *    the SipListener is registered with the SipProvider it will get notified 
 *    of Events representing either Request, Responce or Timeout messages.
 *    <li> De-register a {@link javax.sip.SipListener} from the SipProvider. 
 *    Once a SipListener is de-registered, it will no longer receive any Events 
 *    from that SipProvider.
 *    <li> Send {@link javax.sip.message.Request}'s statelessly.
 *    <li> Send {@link javax.sip.message.Response}'s statelessly.
 *    <li> Client and Server Transaction creation methods. 
 *    <li> Listening Point manipulation methods.
 *    <li> New CallIdHeader accessor method.
 *    <li> SipStack object accessor method.
 * </ul>
 * <p>
 * <b>Architecture:</b><br>
 * This specification defines a many-to-one relationship between a SipProvider 
 * and a SipStack, a one-to-one relationship between a SipProvider and a 
 * ListeningPoint and a many-to-one relationship between a SipProvider 
 * and a SipListener.
 * <p>
 * A SipProvider has the capability to behave transaction statefully, dialog
 * statefully and statelessly. The transaction stateful methods are defined 
 * on the ClientTransaction and ServerTransaction respectfully. The transaction 
 * stateful method defined specifically for UAC and stateful proxy 
 * applications is:
 * <ul>
 * <li> {@link ClientTransaction#sendRequest()}
 * </ul>
 * <p>
 * The stateful (transactional) convenience method defined specifically for 
 * UAS and stateful proxy applications is:
 * <ul>
 * <li> {@link ServerTransaction#sendResponse(Response)}
 * </ul>
 * <p>
 * The dialog stateful methods defined specifically for UAC and stateful 
 * proxy applications are:
 * <ul>
 * <li> {@link Dialog#sendRequest(ClientTransaction)}
 * <li> {@link Dialog#sendAck(Request)}
 * </ul>
 * <p>
 * The stateless methods (non-transactional) defined on the SipProvider 
 * are:
 * <ul>
 * <li> {@link SipProvider#sendResponse(Response)}
 * <li> {@link SipProvider#sendRequest(Request)}
 * </ul>
 * <p>
 * <b>Transaction Model:</b><br>
 * This specification supports stateful and stateless applications on a per 
 * message basis, hence transactional semantics are not mandated for all 
 * messages. This specification defines two types of transactions, server 
 * transactions and client transactions. A stateless proxy does not contain a 
 * client or server transaction, stateless proxies are effectively transparent 
 * with respect to transactions. 
 * <p>
 * Client Transaction:<br>
 * A client transaction exists between a UAC and a UAS specific to Request 
 * messages and a server transaction exists between a UAS and a UAC specific 
 * to Response messages. A transaction either server or client identifies 
 * messages sent between two SIP entities. The purpose of a client transaction 
 * is to identify a Request sent by an application that will reliably deliver 
 * the Request to a server transaction on 
 * the responding SIP entity. The purpose of a server transaction is to 
 * identify a Response sent by an application that will reliably deliver the 
 * Response to the request initiator.
 * <p> 
 * Server Transaction:<br>
 * A new server transaction is required for each response that an application 
 * decides to respond to statefully, as follows: 
 * <ul>
 * <li>Dialog-Creating Requests: A server transaction is not automatically 
 * generated by a SipProvider implementation upon receipt of every 
 * Dialog-Creating Request i.e. INVITE. Instead the 
 * server transaction is set to <code>null</code> in the RequestEvent and 
 * the RequestEvent also containing the Request is passed to the application. 
 * It is then the responsibility of the application to decide to handle the 
 * Dialog-Creating Request statefully or statelessly, using the appropriate 
 * send methods on the SipProvider and the ServerTransaction. If a retransmission of the initial Request 
 * request is recieved by the SipProvider the following procedures should be 
 * adhered to: 
 * <ul>
 * <li>Determine if an exisiting transaction is already handling this Request.
 * <li>If a transaction exists do not pass the Request to the application via a 
 * RequestEvent.
 * <li>If a transaction doesn't exist pass the retransmitted request to the 
 * application as a RequestEvent.
 * </ul>
 * <li>Non-Dialog-Creating Requests - When the SipProvider receives a 
 * Non-Dialog-Creating Request upon which this application has already 
 * responded to the Dialog-Creating Request of the same dialogue the server 
 * transaction is automatically placed to the RequestEvent and passed up to 
 * the application upon which it can respond. Note that the server transaction 
 * may be null in a stateful implementation if the incoming request does not 
 * match any dialog but must be part of one. That is for requests that must 
 * have state but for which the stack cannot find that state, the application 
 * can still handle these requests statelessly. The application cannot create 
 * a new server transaction for such requests. 
 * </ul>
 * <p>
 * <b>Sending Requests:</b><br>
 * The client side of the transport layer is responsible for sending the
 * request. The application passes the the Request to the ClientTransaction 
 * Dialog or the SipProvider that will send the Request over the ListeningPoint's
 * port and transport. See section 18.1.1 of
 * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>.
 * <p>
 * <b>Sending Responses:</b><br>
 * The server side of the transport layer is responsible for sending the
 * responses. The application passes the Response to the ServerTransaction 
 * or the SipProvider that will send the Response over the ListeningPoint's port and transport. 
 * See section 18.2.2 of
 * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a>.
 * <p>
 * <b>Receiving Requests:</b><br>
 * A SipProvider should be prepared to receive requests on any IP address,
 * port and transport combination encapsulated in a ListeningPoint that can be
 * the result of a DNS lookup on a SIP or SIPS URI that is handed out for the
 * purposes of communicating with that server. It is also recommended that a
 * SipProvider listen for requests on the default SIP ports on all public 
 * interfaces. When the SipProvider receives a request over any transport, it
 * must examine the value of the "sent-by" parameter in the top Via
 * header.  If the host portion of the "sent-by" parameter contains a 
 * domain name, or if it contains an IP address that differs
 * from the packet source address, the server must add a "received"
 * parameter to that Via header field value.  This parameter must
 * contain the source address from which the packet was received.  This
 * is to assist the SipProvider in sending the response, since it must 
 * be sent to the source IP address from which the request came.
 * Next, the SipProvider attempts to match the request to a server
 * transaction.  If there are any server transactions in existence, the server 
 * transport uses the matching procedures of Chapter 17 of 
 * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a> to attempt to
 * match the response to an existing transaction.  If a matching server
 * transaction is found, the request is passed to that transaction, encapsulated
 * into a RequestEvent and fired to the application for processing. If no match
 * is found, the request is passed to the application, which may decide to 
 * construct a new server transaction for that request.
 * <p>
 * <b>Receiving Responses</b><br>
 * Responses are first processed by the transport layer and then passed
 * up to the transaction layer.  The transaction layer performs its
 * processing and then passes the response up to the application.
 * When a response is received, the SipProvider examines the top
 * Via header. If the value of the "sent-by" parameter in that header field 
 * value does not correspond to a value that the client transport is configured 
 * to insert into requests, the response MUST be silently discarded. If there 
 * are any client transactions in existence, the client transport uses the 
 * matching procedures of Chapter 17 of 
 * <a href = "http://www.ietf.org/rfc/rfc3261.txt">RFC3261</a> to attempt to 
 * match the response to an existing transaction.  If there is a
 * match, the response must be passed to that transaction, encapsulated into a
 * ResponseEvent and fired to the application.  Otherwise, the response is stray 
 * and must be passed to the application to determine its outcome i.e. a proxy 
 * will forward them, while a User Agent will discard.
 *
 * @see SipListener
 * @see SipStack
 *
 * @author Sun Microsystems
 * @version 1.1
 */
public interface SipProvider {

    /**
     * This method registers the SipListener object to this SipProvider, once
     * registered the SIP Listener recieve events emitted from the SipProvider. 
     * This specification restricts a unicast Listener model, that is only one 
     * Listener may be registered on the SipProvider concurrently. If an 
     * attempt is made to re-register the existing registered SipListener this
     * method returns silently, however a SipListener must be removed from the
     * SipProvider before a different SipListener can be registered to the 
     * SipProvider.
     *
     * @param sipListener the SipListener to be registered with the SipProvider.
     * @throws TooManyListenersException when a new SipListener attempts to 
     * register with the SipProvider when another SipListener is already 
     * registered with this SipProvider.
     */
    public void addSipListener(SipListener sipListener) throws TooManyListenersException;

    /**
     * Removes the specified SipListener from this SipProvider. This method 
     * returns silently if the SipListener is not registered with the SipProvider.
     *
     * @param sipListener the SipListener to be removed from this SipProvider.
     */
    public void removeSipListener(SipListener sipListener);

    /**
     * Returns the SipStack that created this SipProvider. A SipProvider
     * can only be attached to a single SipStack object that belongs to the same
     * implementation as this SipProvider.
     *
     * @see SipStack
     * @return the SipStack that created this SipProvider.
     */
    public SipStack getSipStack();

    /**
     * Returns the ListeningPoint of this SipProvider. A SipProvider has a single
     * Listening Point at any specific point in time.
     *
     * @see ListeningPoint
     * @return the ListeningPoint of this SipProvider.
     */
    public ListeningPoint getListeningPoint();

    /**
     * This method sets the ListeningPoint of the SipProvider. A SipProvider can
     * only have a single ListeningPoint at any specific time, i.e. multiple 
     * SipProviders are prohibited to listen on the same ListeningPoint. This 
     * method returns silently if the same ListeningPoint argument is re-set on 
     * the SipProvider.
     *
     * @param listeningPoint the ListeningPoint of this SipProvider.
     * @throws ObjectInUseException when an application invokes this method 
     * with a ListeningPoint that is being used by another SipProvider in the 
     * system.
     * @see ListeningPoint
     * @since v1.1
     */
    public void setListeningPoint(ListeningPoint listeningPoint)
                                                   throws ObjectInUseException;

    /**
     * Returns a unique CallIdHeader for identifying dialogues between two
     * SIP applications. 
     *
     * @return the new CallIdHeader unique within the SipProvider.
     */
    public CallIdHeader getNewCallId();

    /**
     * Before an application can send a new request it must first request 
     * a new client transaction to handle that Request. This method is called 
     * by the application to create the new client transaction befores it sends 
     * the Request on that transaction. This methods returns 
     * a new unique client transaction that can be passed to send Requests 
     * statefully.
     *
     * @param request the new Request message that is to handled statefully by 
     * the ClientTransaction.
     * @return a new unique client transaction.
     * @throws TransactionUnavailableException if a new transaction can not be created, for example
     * the next hop of the request can not be determined.
     * @see ClientTransaction
     * @since v1.1
     */
    public ClientTransaction getNewClientTransaction(Request request) 
                                        throws TransactionUnavailableException;        
    
    /**
     * An application has the responsibility of deciding to respond to a
     * Request that does not match an existing server transaction. This method
     * is called by an application that decides to respond to an unmatched
     * Request statefully. This methods return a new unique server transaction
     * that can be used to respond to the request statefully.
     *
     * @param request the Request message that the doesn't match an existing 
     * transaction that the application decides to handle statefully.
     * @return a new unique server transaction.
     * @throws TransactionAlreadyExistsException if a transaction already exists
     * that is already handling this Request. This may happen if the application
     * gets retransmits of the same request before the initial transaction is 
     * allocated.
     * @throws TransactionUnavailableException if a new transaction can not be created, for example
     * the next hop of the request can not be determined.
     * @see ServerTransaction
     * @since v1.1
     */
    public ServerTransaction getNewServerTransaction(Request request) 
                       throws TransactionAlreadyExistsException, TransactionUnavailableException;


    /**
     * Sends the Request statelessly, that is no transaction record is 
     * associated with this action. This method implies that the application is 
     * functioning as a stateless proxy, hence the underlying SipProvider acts 
     * statelessly. A stateless proxy simply forwards every request it receives 
     * downstream and discards information about the Request message once the 
     * message has been forwarded. A stateless proxy does not have any notion 
     * of a transaction.
     * <p>
     * Once the Request message has been passed to this method, the SipProvider
     * will forget about this Request. No transaction semantics will be
     * associated with the Request and the SipProvider will not handle
     * retranmissions for the Request. If these semantics are required it is the
     * responsibility of the application not the SipProvider.
     *
     * @since v1.1
     * @see Request
     * @param request the Request message to send statelessly
     * @throws SipException if the SipProvider cannot send the Request for any 
     * reason.
     */    
    public void sendRequest(Request request) throws SipException;
    
    /**
     * Sends the Response statelessly, that is no transaction record is 
     * associated with this action. This method implies that the application is 
     * functioning as either a stateless proxy or a stateless UAS.
     * <ul>
     *  <li> Stateless proxy - A stateless proxy simply forwards every response
     *  it receives upstream and discards information about the response message
     *  once the message has been forwarded. A stateless proxy does not
     *  have any notion of a transaction.
     *  <li>Stateless UAS - A stateless UAS does not maintain
     *  transaction state. It replies to requests normally, but discards
     *  any state that would ordinarily be retained by a UAS after a response
     *  has been sent.  If a stateless UAS receives a retransmission of a
     *  request, it regenerates the response and resends it, just as if it
     *  were replying to the first instance of the request. A UAS cannot be
     *  stateless unless the request processing for that method would always
     *  result in the same response if the requests are identical. Stateless
     *  UAS's do not use a transaction layer; they receive requests directly
     *  from the transport layer and send responses directly to the transport
     *  layer.
     * </ul>
     * 
     * @see Response
     * @param response the Response to send statelessly.
     * @throws SipException if the SipProvider cannot send the Response for any 
     * reason.
     * @see Response
     * @since v1.1
     */
    public void sendResponse(Response response) throws SipException;

  
}

