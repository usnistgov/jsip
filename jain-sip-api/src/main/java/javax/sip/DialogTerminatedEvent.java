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
 * File Name     : DialogTerminatedEvent.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.2     17/06/2005  Pnelim O'Doherty      New class
 *                      
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

import java.util.EventObject;

/**
 * 
 * DialogTerminatedEvent is delivered to the Listener when the
 * dialog transitions to the terminated state. An implementation
 * is expected to deliver this event to the listener when it discards
 * all internal book keeping records for a given dialog, allowing the 
 * Listener to unmap its own data structures.
 * 
 * @author BEA Systems, NIST
 * @since v1.2
 *
 */
public class DialogTerminatedEvent extends EventObject {
    
    /**
     * Constructs a DialogTerminatedEvent to indicate a dialog
     * timeout.
     *
     * @param source - the source of TimeoutEvent. 
     * @param dialog - the dialog that timed out.
     */
     public DialogTerminatedEvent(Object source, Dialog dialog) {
         super(source);
         m_dialog = dialog;
      
    }

    /**
     * Gets the Dialog associated with the event. This 
     * enables application developers to access the dialog associated to this 
     * event. 
     * 
     * @return the dialog associated with the response event or null if there is no dialog.
     * @since v1.2
     */
    public Dialog getDialog() {
        return m_dialog;
    }     
     
     // internal variables
     private Dialog m_dialog = null;

}

