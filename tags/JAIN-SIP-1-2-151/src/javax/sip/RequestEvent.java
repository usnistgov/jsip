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
 * File Name     : RequestEvent.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *  1.2     02/15/2005  M. Ranganathan      getDialog get the current
 *                                          dialog.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

import java.util.*;
import javax.sip.message.Request;

/**
 * This class represents an Request event that is passed from a SipProvider to
 * its SipListener. This specification handles the passing of request messages to the
 * application use the event model. An application (SipListener) will register
 * with the SIP protocol stack (SipProvider) and listen for Request events
 * from the SipProvider.
 * <p>
 * This specification defines a single Request event object to handle all Request
 * messages. The Request event encapsulates the Request message that can be
 * retrieved from {@link RequestEvent#getRequest()}. Therefore the event type
 * of a Request event can be determined as follows:
 * <p>
 * <i>eventType == RequestEvent.getRequest().getMethod();</i>
 * <p>
 * A Request event also encapsulates the server transaction which handles the
 * Request.
 * <p>
 * RequestEvent contains the following elements:
 * <ul>
 * <li>source - the source of the event i.e. the SipProvider sending the
 * RequestEvent
 * <li>serverTransaction - the server transaction this RequestEvent is
 * associated with.
 * <li>Request - the Request message received on the SipProvider
 * that needs passed to the application encapsulated in a RequestEvent.
 * </ul>
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public class RequestEvent extends EventObject {

    /**
    * Constructs a RequestEvent encapsulating the Request that has been received
    * by the underlying SipProvider. This RequestEvent once created is passed to
    * {@link javax.sip.SipListener#processRequest(RequestEvent)} method of the SipListener
    * for application processing.
    *
    * @param source - the source of ResponseEvent i.e. the SipProvider
    * @param serverTransaction - server transaction upon which
    * this Request was sent
    * @param request - the Request message received by the SipProvider
    */
    public RequestEvent(Object source, ServerTransaction serverTransaction, Dialog dialog, Request request) {
        super(source);
        m_transaction = serverTransaction;
        m_request = request;
        m_dialog  = dialog;
   }

    /**
    * Gets the server transaction associated with this RequestEvent
    *
    * @return the server transaction associated with this RequestEvent
    */
    public ServerTransaction getServerTransaction(){
        return m_transaction;
    }

    /**
    * Gets the Request message associated with this RequestEvent.
    *
    * @return the message associated with this RequestEvent.
    */
    public Request getRequest() {
        return m_request;
    }

    /**
     * Gets the dialog with which this Event is associated.
     * This method separates transaction support from dialog support. This
     * enables application developers to access the dialog associated to this event
     * without having to query the transaction associated to the event.
     *
     * @return the dialog with which the RequestEvent is associated or null if
     * no dialog exists.
     * @since v1.2
     */
    public Dialog getDialog() {
        return m_dialog;
    }


    // internal variables
    private Request m_request;
    private ServerTransaction m_transaction;
    private Dialog  m_dialog;
}
