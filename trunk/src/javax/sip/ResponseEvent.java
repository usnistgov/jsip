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
 * File Name     : ResponseEvent.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import java.util.*;
import javax.sip.message.Response;

/**
 * This class represents a Response event that is passed from a SipProvider to
 * its SipListener. This specification handles the passing of Response messages 
 * to the application with the event model. An application (SipListener) 
 * registers with the SIP protocol stack (SipProvider) and listens for Response 
 * events from the SipProvider.
 * <p>
 * This specification defines a single Response event object to handle all 
 * Response messages. The Response event encapsulates the Response message 
 * that can be retrieved from {@link javax.sip.ResponseEvent#getResponse()}.
 * Therefore the event type of a Response event can be determined as follows:
 * <p>
 * <i>eventType == ResponseEvent.getResponse().getStatusCode();</i>
 * <p>
 * A Response event also encapsulates the client transaction upon which the
 * Response is correlated, i.e. the client transaction of the Request
 * message upon which this is a Response.
 * <p>
 * ResponseEvent contains the following elements:
 * <ul>
 * <li>source - the source of the event i.e. the SipProvider sending the
 * ResponseEvent.
 * <li>clientTransaction - the client transaction this ResponseEvent is 
 * associated with.
 * <li>Response - the Response message received on the SipProvider
 * that needs passed to the application encapsulated in a ResponseEvent.
 * </ul>
 *
 * @author Sun Microsystems
 * @since v1.1
 */
public class ResponseEvent extends EventObject {

    /**
    * Constructs a ResponseEvent encapsulating the Response that has been received
    * by the underlying SipProvider. This ResponseEvent once created is passed to
    * {@link SipListener#processResponse(ResponseEvent)} method of the SipListener
    * for application processing.
    *
    * @param source - the source of ResponseEvent i.e. the SipProvider
    * @param clientTransaction - client transaction upon which
    * this Response was sent
    * @param response - the Response message received by the SipProvider
    */
    public ResponseEvent(Object source, ClientTransaction clientTransaction, Response response) {
        super(source);
        m_response = response;
        m_transaction = clientTransaction;
    }

    /**
    * Gets the client transaction associated with this ResponseEvent
    *
    * @return client transaction associated with this ResponseEvent
    */
    public ClientTransaction getClientTransaction(){
        return m_transaction;
    }

    /**
    * Gets the Response message encapsulated in this ResponseEvent.
    *
    * @return the response associated with this ResponseEvent.
    */
    public Response getResponse() {
        return m_response;
    }

    // internal variables
    private Response m_response;
    private ClientTransaction m_transaction;
}

