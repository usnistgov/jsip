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
 * File Name     : Request.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.message;

import javax.sip.address.URI;
import java.text.ParseException;

/**
 * A SIP Request is a request from a client to a server. The following Requests 
 * method names are defined by <a href = "http://www.ietf.org/rfc/rfc3261.txt">
 * RFC3261</a>:
 * <ul>
 * <li>ACK - confirms that client has received a final Response to an INVITE 
 * Request.
 * <li>BYE - indicates to the server that client wishes to release the
 * call leg.
 * <li>CANCEL - cancels a pending User Agent Client Request.
 * <li>INVITE - indicates that user or service is being invited to participate 
 * in a session.
 * <li>OPTIONS - queries a server with regards to its capabilities.
 * <li>REGISTER - registers contact information with a SIP server.
 * </ul>
 *
 * This specifications also supports the following method name extensions, documented in
 * the following standards track RFCs:
 * <ul>
 * <li> INFO - used to carry session related control information that is 
 * generated during a session. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc2976.txt">RFC2976</a>.
 * <li> PRACK - similiar in operation to ACK, however specific to the 
 * reliability of provisional Responses. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3262.txt">RFC3262</a>.
 * <li> UPDATE - allows a client to update parameters of a session without
 * impacting the state of a dialog. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3311.txt">RFC3311</a>.
 * <li> SUBSCRIBE - provides an extensible framework by which SIP nodes can 
 * request notification from remote nodes indicating that certain events have 
 * occurred. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>.
 * <li> NOTIFY - provides an extensible framework by which SIP nodes can 
 * get notification from remote nodes indicating that certain events have 
 * occurred. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>. 
 * <li> MESSAGE - for sending instant messages using a metaphor similar to that 
 * of a two-way pager or SMS enabled handset. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3428.txt">RFC3428</a>.
 * <li> REFER - requests that the recipient REFER to a resource provided in 
 * the request. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3515.txt">RFC3515</a>.
 * <li> PUBLISH - for publishing event state. This functionality is defined in 
 * <a href = "http://www.ietf.org/rfc/rfc3903.txt">RFC3903</a>. 
 * </ul>
 * A valid SIP request formulated by a User Agent Client MUST, at a minimum, contain the 
 * following header fields: To, From, CSeq, Call-ID, Max-Forwards, and Via; all 
 * of these header fields are mandatory in all SIP requests. These six header 
 * fields are the fundamental building blocks of a SIP message, as they jointly 
 * provide for most of the critical message routing services including the 
 * addressing of messages, the routing of responses, limiting message 
 * propagation, ordering of messages, and the unique identification of 
 * transactions. These header fields are in addition to the mandatory request 
 * line, which contains the method, Request-URI, and SIP version.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface Request extends Message {
   
    /**
    * Gets method string of this Request message.
    *
    * @return the method of this Request message.
    */
    public String getMethod();

    /**
     * Sets the method of Request to the newly supplied value. The standard 
     * RFC3261 methods are REGISTER for registering contact information, INVITE, 
     * ACK, and CANCEL for setting up sessions, BYE for terminating sessions, and
     * OPTIONS for querying servers about their capabilities.   
     *
     * @param method - the new string value of the method of Request
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the method value.
     */
    public void setMethod(String method) throws ParseException;    
    
    /**
     * Gets the URI Object identifying the request URI of this Request, which 
     * indicates the user or service to which this request is addressed.
     *
     * @return Request URI of Request
     */
    public URI getRequestURI();

    /**
     * Sets the RequestURI of Request. The Request-URI is a SIP or SIPS URI 
     * or a general URI. It indicates the user or service to which this request 
     * is being addressed. SIP elements MAY support Request-URIs with schemes 
     * other than "sip" and "sips", for example the "tel" URI scheme. SIP 
     * elements MAY translate non-SIP URIs using any mechanism at their disposal, 
     * resulting in SIP URI, SIPS URI, or some other scheme.
     *
     * @param requestURI - the new Request URI of this request message
     */
    public void setRequestURI(URI requestURI);

    
    
// Request Constants
    
    /**
     * An ACK is used to acknowledge the successful receipt
     * of a message in a transaction. It is also used to illustrate the 
     * successful setup of a dialog via the a three-way handshake between an 
     * UAC and an UAS for an Invite transaction.
     */
    public static final String ACK = "ACK";

    /**
     * The BYE request is used to terminate a specific 
     * session or attempted session. When a BYE is received on a dialog, any 
     * session associated with that dialog SHOULD terminate. A User Agent MUST 
     * NOT send a BYE outside of a dialog. The caller's User Agent MAY send a 
     * BYE for either confirmed or early dialogs, and the callee's User Agent 
     * MAY send a BYE on confirmed dialogs, but MUST NOT send a BYE on early 
     * dialogs. However, the callee's User Agent MUST NOT send a BYE on a 
     * confirmed dialog until it has received an ACK for its 2xx response or 
     * until the server transaction times out. If no SIP extensions have defined 
     * other application layer states associated with the dialog, the BYE also
     * terminates the dialog.
     */
    public static final String BYE = "BYE";

    /**
     * The CANCEL request is used to cancel a previous
     * request sent by a client. Specifically, it asks the UAS to cease 
     * processing the request and to generate an error response to that request.  
     * CANCEL has no effect on a request to which a UAS has already given a 
     * final response. Because of this, it is most useful to CANCEL requests to 
     * which it can take a server long time to respond. For this reason, CANCEL 
     * is best for INVITE requests, which can take a long time to generate a 
     * response.  
     */
    public static final String CANCEL = "CANCEL";

    /**
     * The INVITE method is used by an user agent client that desires to 
     * initiate a session, session examples include, audio, video, or a game. The
     * INVITE request asks a server to establish a session. This request may be 
     * forwarded by proxies, eventually arriving at one or more UAS's that can 
     * potentially accept the invitation. These UAS's will frequently need to 
     * query the user about whether to accept the invitation. After some time, 
     * those UAS's can accept the invitation (meaning the session is to be 
     * established) by sending a 2xx response. If the invitation is not 
     * accepted, a 3xx, 4xx, 5xx or 6xx response is sent, depending on the 
     * reason for the rejection. Before sending a final response, the UAS can 
     * also send provisional responses (1xx) to advise the UAC of progress in 
     * contacting the called user.
     */
    public static final String INVITE = "INVITE";    
    
    /**
     * The OPTIONS method allows a User Agent to query 
     * another User Agent or a proxy server as to its capabilities. This allows 
     * a client to discover information about the supported methods, content 
     * types, extensions, codecs, etc. without "ringing" the other party. For 
     * example, before a client inserts a Require header field into an INVITE 
     * listing an option that it is not certain the destination UAS supports, 
     * the client can query the destination UAS with an OPTIONS to see if this
     * option is returned in a Supported header field. All User Agents MUST 
     * support the OPTIONS method.
     */
    public static final String OPTIONS = "OPTIONS";

    /**
     * The REGISTER method requests the addition, 
     * removal, and query of bindings. A REGISTER request can add a new binding 
     * between an address-of-record and one or more contact addresses. 
     * Registration on behalf of a particular address-of-record can be performed 
     * by a suitably authorized third party. A client can also remove previous 
     * bindings or query to determine which bindings are currently in place for 
     * an address-of-record. A REGISTER request does not establish a dialog.  
     * Registration entails sending a REGISTER request to a special type of UAS 
     * known as a registrar. A registrar acts as the front end to the location 
     * service for a domain, reading and writing mappings based on the contents 
     * of REGISTER requests. This location service is then typically consulted 
     * by a proxy server that is responsible for routing requests for that domain.
     */
    public static final String REGISTER = "REGISTER";


    
   /**    
    * Notify is an extension method that informs subscribers of changes in state 
    * to which the subscriber has a subscription. Subscriptions are typically
    * put in place using the SUBSCRIBE method; however, it is possible that 
    * other means have been used.
    * <p>
    * When a SUBSCRIBE request is answered with a 200-class response, the 
    * notifier MUST immediately construct and send a NOTIFY request to the 
    * subscriber. When a change in the subscribed state occurs, the notifier 
    * SHOULD immediately construct and send a NOTIFY request, subject to 
    * authorization, local policy, and throttling considerations.
    * <p>
    * A NOTIFY does not terminate its corresponding subscription. i.e. a single 
    * SUBSCRIBE request may trigger several NOTIFY requests. NOTIFY requests 
    * MUST contain a "Subscription-State" header with a value of "active", 
    * "pending", or "terminated". As in SUBSCRIBE requests, NOTIFY "Event" 
    * headers will contain a single event package name for which a notification 
    * is being generated. The package name in the "Event" header MUST match 
    * the "Event" header in the corresponding SUBSCRIBE message. If an "id" 
    * parameter was present in the SUBSCRIBE message, that "id" parameter MUST 
    * also be present in the corresponding NOTIFY messages.
    * <p>
    * Event packages may define semantics associated with the body of their
    * NOTIFY requests; if they do so, those semantics apply. NOTIFY bodies
    * are expected to provide additional details about the nature of the event 
    * which has occurred and the resultant resource state. When present, the 
    * body of the NOTIFY request MUST be formatted into one of the body formats 
    * specified in the "Accept" header of the corresponding SUBSCRIBE request. 
    * This body will contain either the state of the subscribed resource or a 
    * pointer to such state in the form of a URI 
    * <p>
    * A NOTIFY request is considered failed if the response times out, or a
    * non-200 class response code is received which has no "Retry-After"
    * header and no implied further action which can be taken to retry the
    * request. If a NOTIFY request receives a 481 response, the notifier MUST 
    * remove the corresponding subscription even if such subscription was
    * installed by non-SUBSCRIBE means. 
    * <p>
    * If necessary, clients may probe for the support of NOTIFY using the 
    * OPTIONS. The presence of the "Allow-Events" header in a message is 
    * sufficient to indicate support for NOTIFY. The "methods" parameter for 
    * Contact may also be used to specifically announce support for NOTIFY 
    * messages when registering. 
    *
    */    
    public static final String NOTIFY = "NOTIFY";          


    
   /**    
    * Subscribe is an extension method that is used to request current state 
    * and state updates from a remote node. SUBSCRIBE requests SHOULD contain 
    * an "Expires" header, which indicates the duration of the subscription.
    * In order to keep subscriptions effective beyond the duration communicated 
    * in the "Expires" header, subscribers need to refresh subscriptions on a 
    * periodic basis using a new SUBSCRIBE message on the same dialog. If no 
    * "Expires" header is present in a SUBSCRIBE request, the implied default 
    * is defined by the event package being used. 
    * <p>
    * 200-class responses to a SUBSCRIBE request indicate that the subscription 
    * has been accepted, and that a NOTIFY will be sent immediately. If the 
    * subscription resource has no meaningful state at the time that the SUBSCRIBE 
    * message is processed, this NOTIFY message MAY contain an empty or neutral body. 
    * 200-class responses to SUBSCRIBE requests also MUST contain an "Expires" 
    * header. The period of time in the response MAY be shorter but MUST NOT be 
    * longer than specified in the request. The period of time in the response 
    * is the one which defines the duration of the subscription. An "expires" 
    * parameter on the "Contact" header has no semantics for SUBSCRIBE and is 
    * explicitly not equivalent to an "Expires" header in a SUBSCRIBE request 
    * or response.
    * <p>
    * The Request URI of a SUBSCRIBE request, contains enough information to 
    * route the request to the appropriate entity. It also contains enough 
    * information to identify the resource for which event notification is 
    * desired, but not necessarily enough information to uniquely identify the 
    * nature of the event. Therefore Subscribers MUST include exactly one 
    * "Event" header in SUBSCRIBE requests, indicating to which event or class 
    * of events they are subscribing. The "Event" header will contain a token 
    * which indicates the type of state for which a subscription is being 
    * requested. 
    * <p>
    * As SUBSCRIBE requests create a dialog, they MAY contain an "Accept" 
    * header. This header, if present, indicates the body formats allowed in 
    * subsequent NOTIFY requests. Event packages MUST define the behavior for 
    * SUBSCRIBE requests without "Accept" headers. If an initial SUBSCRIBE is 
    * sent on a pre-existing dialog, a matching 200-class response or successful 
    * NOTIFY request merely creates a new subscription associated with that 
    * dialog. Multiple subscriptions can be associated with a single dialog.
    * <p>
    * Unsubscribing is handled in the same way as refreshing of a subscription, 
    * with the "Expires" header set to "0". Note that a successful unsubscription 
    * will also trigger a final NOTIFY message.
    * <p>
    * If necessary, clients may probe for the support of SUBSCRIBE using the 
    * OPTIONS. The presence of the "Allow-Events" header in a message is 
    * sufficient to indicate support for SUBSCRIBE. The "methods" parameter for 
    * Contact may also be used to specifically announce support for SUBSCRIBE 
    * messages when registering. 
    *
    */
    public static final String SUBSCRIBE = "SUBSCRIBE";    
    
   /**    
    * Message is an extension method that allows the transfer of Instant Messages. 
    * The MESSAGE request inherits all the request routing and security 
    * features of SIP. MESSAGE requests carry the content in the form of MIME 
    * body parts. The actual communication between participants happens in the 
    * media sessions, not in the SIP requests themselves. The MESSAGE method 
    * changes this assumption.
    * <p>
    * MESSAGE requests do not themselves initiate a SIP dialog; under 
    * normal usage each Instant Message stands alone, much like pager 
    * messages, that is there are no explicit association between messages. 
    * MESSAGE requests may be sent in the context of a dialog initiated by some 
    * other SIP request. If a MESSAGE request is sent within a dialog, it is 
    * "associated" with any media session or sessions associated with that dialog. 
    * <p>
    * When a user wishes to send an instant message to another, the sender 
    * formulates and issues a Message request. The Request-URI of this request 
    * will normally be the "address of record" for the recipient of the instant 
    * message, but it may be a device address in situations where the client 
    * has current information about the recipient's location. The body of the 
    * request will contain the message to be delivered.
    * <p>
    * Provisional and final responses to the request will be returned to the 
    * sender as with any other SIP request. Normally, a 200 OK response will be 
    * generated by the user agent of the request's final recipient. Note that 
    * this indicates that the user agent accepted the message, not that the 
    * user has seen it. 
    * <p>
    * The UAC MAY add an Expires header field to limit the validity of the message 
    * content. If the UAC adds an Expires header field with a non-zero value, it 
    * SHOULD also add a Date header field containing the time the message is sent.
    * Most SIP requests are used to setup and modify communication sessions. 
    *
    */
    public static final String MESSAGE = "MESSAGE";

   /**    
    * Refer is an extension method that requests that the recipient REFER to a 
    * resource provided in the request, this can be used to enable many 
    * applications such as Call Transfer. The REFER method indicates that 
    * the recipient (identified by the Request-URI) should contact a third 
    * party using the contact information provided in the request. A REFER 
    * request MUST contain exactly one Refer-To header field value and MAY 
    * contain a body. A receiving agent may choose to process the body 
    * according to its Content-Type.
    * <p>
    * A User Agent accepting a well-formed REFER request SHOULD request 
    * approval from the user to proceed. If approval is granted, the User 
    * Agent MUST contact the resource identified by the URI. SIP proxies do 
    * not require modification to support the REFER method. A proxy should 
    * process a REFER request the same way it processes an OPTIONS request.
    * <p>
    * A REFER request implicitly establishes a subscription to the "refer" 
    * event. The agent issuing the REFER can terminate this subscription 
    * prematurely by unsubscribing. A REFER request MAY be placed outside 
    * the scope of a dialog created with an INVITE. REFER creates a dialog, 
    * and MAY be Record-Routed, hence MUST contain a single Contact header 
    * field value. REFERs occurring inside an existing dialog MUST follow 
    * the Route/Record-Route logic of that dialog. The NOTIFY mechanism MUST 
    * be used to inform the agent sending the REFER of the status of the 
    * reference. The dialog identifiers of each NOTIFY must match those of 
    * the REFER as they would if the REFER had been a SUBSCRIBE request. If 
    * more than one REFER is issued in the same dialog, the dialog 
    * identifiers do not provide enough information to associate the 
    * resulting NOTIFYs with the proper REFER. Therefore it MUST include an 
    * "id" parameter in the Event header field of each NOTIFY containing the 
    * sequence number of the REFER this NOTIFY is associated with. A REFER 
    * sent within the scope of an existing dialog will not fork. A REFER 
    * sent outside the context of a dialog MAY fork, and if it is accepted 
    * by multiple agents, MAY create multiple subscriptions.
    *
    */    
    public static final String REFER = "REFER";    

   /**    
    * INFO is an extension method which allows for the carrying of session 
    * related control information that is generated during a session. One 
    * example of such session control information is ISUP and ISDN signaling 
    * messages used to control telephony call services. The purpose of the INFO 
    * message is to carry application level information along the SIP signaling 
    * path. The signaling path for the INFO method is the signaling path 
    * established as a result of the call setup. This can be either direct
    * signaling between the calling and called user agents or a signaling path 
    * involving SIP proxy servers that were involved in the call setup and added 
    * themselves to the Record-Route header on the initial INVITE message.
    * <p>
    * The INFO method is used for communicating mid-session signaling 
    * information, it is not used to change the state of SIP calls, nor does it 
    * change the state of sessions initiated by SIP. Rather, it provides 
    * additional optional information which can further enhance the application 
    * using SIP. The mid-session information can be communicated in either an 
    * INFO message header or as part of a message body. There are no specific 
    * semantics associated with INFO. The semantics are derived from the body 
    * or new headers defined for usage in INFO. JAIN SIP provides the 
    * facility to send {@link javax.sip.header.ExtensionHeader} in messages. 
    * The INFO request MAY contain a message body. Bodies which imply a change 
    * in the SIP call state or the sessions initiated by SIP MUST NOT be sent 
    * in an INFO message. 
    *
    */    
    public static final String INFO = "INFO";    
    
   /**    
    * PRACK is an extension method that plays the same role as ACK, but for 
    * provisional responses. PRACK is a normal SIP message, like BYE. As such, 
    * its own reliability is ensured hop-by-hop through each stateful 
    * proxy. Also like BYE, but unlike ACK, PRACK has its own response. 
    * In order to achieve reliability of provisional responses, in a similiar 
    * manner to 2xx final responses to INVITE, reliable provisional responses 
    * are retransmitted with an exponential backoff, which cease when a PRACK 
    * message is received. The PRACK messages contain an RAck header field, 
    * which indicates the sequence number of the provisional response that is 
    * being acknowledged. 
    * <p>
    * PRACK is like any other request within a dialog, and is treated likewise. 
    * In particular, a UAC SHOULD NOT retransmit the PRACK request when it 
    * receives a retransmission of the provisional response being acknowledged, 
    * although doing so does not create a protocol error. A matching PRACK is 
    * defined as one within the same dialog as the response, and whose 
    * method, CSeq-num, and RSeq-num in the RAck header field match, 
    * respectively, the method and sequence number from the CSeq and the 
    * sequence number from the RSeq header of the reliable provisional response. 
    * PRACK requests MAY contain bodies, which are interpreted according to 
    * their type and disposition.
    *
    */
    public static final String PRACK = "PRACK";    

   /** 
    * UPDATE is an extension method that allows a client to update parameters 
    * of a session (such as the set of media streams and their codecs) but has 
    * no impact on the state of a dialog. In that sense, it is like a re-INVITE, 
    * but unlike re-INVITE, it can be sent before the initial INVITE has been 
    * completed. This makes it very useful for updating session parameters 
    * within early dialogs. Operation of this extension is straightforward, the 
    * caller begins with an INVITE transaction, which proceeds normally. Once a 
    * dialog is established, the caller can generate an UPDATE method that 
    * contains an SDP offer for the purposes of updating the session. The 
    * response to the UPDATE method contains the answer. The Allow header 
    * field is used to indicate support for the UPDATE method. There are 
    * additional constraints on when UPDATE can be used, based on the 
    * restrictions of the offer/answer model. Although UPDATE can be used on 
    * confirmed dialogs, it is RECOMMENDED that a re-INVITE be used instead. 
    * This is because an UPDATE needs to be answered immediately, ruling out 
    * the possibility of user approval. Such approval will frequently be needed, 
    * and is possible with a re-INVITE.
    *
    */
    public static final String UPDATE = "UPDATE";
    
    /**
     * PUBLISH is an extension method that allows a client to publish event state
     * (such as presence information). It is sent outside of any dialog, and is not
     * dialog creating. PUBLISH is similar to REGISTER in that it allows a user 
     * to create, modify, and remove state in another entity which manages this
     * state on behalf of the user.  Addressing a PUBLISH request is identical 
     * to addressing a SUBSCRIBE request.  The Request-URI of a PUBLISH request 
     * is populated with the address of the resource for which the user wishes 
     * to publish event state.  The user may in turn have multiple User Agents 
     * or endpoints that publish event state. Each endpoint may publish its own 
     * unique state, out of which the event state compositor generates the 
     * composite event state of the resource.  In addition to a particular 
     * resource, all published event state is associated with a specific event 
     * package. Through a subscription to that event package, the user is able 
     * to discover the composite event state of all of the active publications.
     *<p>
     * PUBLISH requests create soft state in the event state compositor. This 
     * event soft state has a defined lifetime and will expire after a 
     * negotiated amount of time, requiring the publication to be refreshed 
     * by subsequent PUBLISH requests. There may also be event hard state 
     * provisioned for each resource for a particular event package. This event 
     * state represents the resource state that is present at all times, and 
     * does not expire.The event state compositor may use event hard state in 
     * the absence of, or in addition to, event soft state provided through 
     * the PUBLISH mechanism.
     * <p>
     * Clients may probe for the support of PUBLISH using theOPTIONS request. 
     * The presence of "PUBLISH" in the "Allow" header field in a response to 
     * an OPTIONS request indicates support for the PUBLISH method. In addition, 
     * the "Allow-Events" header field indicates the supported event packages.
     * <p>
     * A PUBLISH request does not establish a dialog. A UAC MAY include a
     * Route header field in a PUBLISH request based on a pre-existing route set.
     * The Record-Route header field has no meaning in PUBLISH requests or 
     * responses, and MUST be ignored if present. In particular, the UAC MUST 
     * NOT create a new route set based on the presence or absence of a 
     * Record-Route header field in any response to a PUBLISH request. The 
     * PUBLISH request MAY contain a Contact header field, but including
     * one in a PUBLISH request has no meaning in the event publication
     * context and will be ignored. A PUBLISH request may be sent within an 
     * existing dialog.  In that case, the request is received in the context 
     * of any media session or sessions associated with that dialog. A new 
     * PUBLISH should not be sent (not a re-transmission) for the same 
     * Request-URI, until they have received a final response for the previous 
     * one or the previous PUBLISH request has timed out.
     *
     * @since v1.2
     */
    public static final String PUBLISH = "PUBLISH";    
}

