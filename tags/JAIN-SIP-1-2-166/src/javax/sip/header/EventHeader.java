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
 * File Name     : EventHeader.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     13/12/2002  Phelim O'Doherty    Initial version, extension header to 
 *                                          support RFC3265
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip.header;

import java.text.ParseException;

/**
 * This interface represents the Event SIP header, as defined by 
 * <a href = "http://www.ietf.org/rfc/rfc3265.txt">RFC3265</a>, this header is 
 * not part of RFC3261.
 * <p> 
 * For the purposes of matching responses and NOTIFY messages with SUBSCRIBE 
 * messages, the event-type portion of the "Event" header is compared 
 * byte-by-byte, and the "id" parameter token (if present) is compared 
 * byte-by-byte. An "Event" header containing an "id" parameter never matches 
 * an "Event" header without an "id" parameter. No other parameters are 
 * considered when performing a comparison, i.e. "Event: foo; id=1234" would
 * match "Event: foo; param=abcd; id=1234", but not "Event: Foo; id=1234".
 * <p>
 * There MUST be exactly one event type listed per event header. Multiple events 
 * per message are disallowed i.e Subscribers MUST include exactly one "Event" 
 * header in SUBSCRIBE requests, indicating to which event or class of events 
 * they are subscribing. The "Event" header will contain a token which indicates
 * the type of state for which a subscription is being requested. This token 
 * will correspond to an event package which further describes the semantics of 
 * the event or event class. The "Event" header MAY also contain an "id" parameter. 
 * When a subscription is created in 
 * the notifier, it stores the event package name and the "Event" header "id" 
 * parameter (if present) as part of the subscription information.
 * <p> 
 * This "id" parameter, if present:
 * <ul>
 * <li>contains an opaque token which identifies the specific subscription 
 * within a dialog. 
 * <li>is only valid within the scope of a single dialog.
 * <li>if contained in the initial SUBSCRIBE message on the "Event" header, then 
 * refreshes of the subscription must also contain an identical "id" parameter, 
 * they will otherwise be considered new subscriptions in an existing dialog. 
 * <li>if present in the SUBSCRIBE message, that "id" parameter MUST also be 
 * present in the corresponding NOTIFY messages.
 * </ul>
 * If the event package to which the event token corresponds defines behavior 
 * associated with the body of its SUBSCRIBE requests or parameters for the 
 * Event header, those semantics apply.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public interface EventHeader extends Parameters, Header {

    /**
     * Sets the eventType to the newly supplied eventType string.
     *
     * @param eventType - the  new string defining the eventType supported
     * in this EventHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the eventType value.
     */
    public void setEventType(String eventType) throws ParseException;

    /**
     * Gets the eventType of the EventHeader. 
     *
     * @return the string object identifing the eventType of EventHeader.
     */
    public String getEventType();        
    
    /**
     * Sets the id to the newly supplied eventId string.
     *
     * @param eventId - the new string defining the eventId of this EventHeader
     * @throws ParseException which signals that an error has been reached
     * unexpectedly while parsing the eventId value.
     */
    public void setEventId(String eventId) throws ParseException;

    /**
     * Gets the id of the EventHeader. This method may return null if the 
     * eventId is not set.
     *
     * @return the string object identifing the eventId of EventHeader.
     */
    public String getEventId();
    
    
    /**
     * Name of EventHeader
     */
    public final static String NAME = "Event";

}

