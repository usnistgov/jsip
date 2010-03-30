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
 * File Name     : DialogState.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *  1.2     07/07/2005  Phelim O'Doherty    Deprecated the completed state.
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

import java.io.*;

/**
 * This class contains the enumerations that define the underlying state of an 
 * existing dialog. 
 *
 * There are three explicit states for a dialog, namely:
 * <ul>
 * <li> Early - A dialog is in the "early" state, which occurs when it is 
 * created when a provisional response is recieved to the INVITE Request.
 * <li> Confirmed - A dialog transitions to the "confirmed" state when a 2xx 
 * final response is received to the INVITE Request.
 * <li> Terminated - A dialog transitions to the "terminated" state for all 
 * other reasons or if no response arrives at all on the dialog.
 * </ul>
 *
 * @author BEA Systems, NIST 
 * @version 1.2
 */
public final class DialogState implements Serializable{

    /**
     * Constructor for the DialogState
     *
     * @param dialogState  The integer value for the DialogueState
     */
    private DialogState(int dialogState) {
        m_dialogState = dialogState;
        m_dialogStateArray[m_dialogState] = this;
    }

    /**
     * This method returns the object value of the DialogState
     *
     * @return  The DialogState Object
     * @param dialogState The integer value of the DialogState
     */
    public static DialogState getObject(int dialogState){
        if (dialogState >= 0 && dialogState < m_size) {
            return m_dialogStateArray[dialogState];
        } else {
            throw new IllegalArgumentException("Invalid dialogState value");
        }
    }

    /**
     * This method returns the integer value of the DialogState
     *
     * @return The integer value of the DialogState
     */
    public int getValue() {
        return m_dialogState;
    }


    /**
     * Returns the designated type as an alternative object to be used when
     * writing an object to a stream.
     *
     * This method would be used when for example serializing DialogState.EARLY
     * and deserializing it afterwards results again in DialogState.EARLY.
     * If you do not implement readResolve(), you would not get
     * DialogState.EARLY but an instance with similar content.
     *
     * @return the DialogState
     * @exception ObjectStreamException
     */
    private Object readResolve() throws ObjectStreamException {
        return m_dialogStateArray[m_dialogState];
    }

    
    /**
     * Compare this dialog state for equality with another.
     * 
     * @since 1.2
     * @param obj the object to compare this with.
     * @return <code>true</code> if <code>obj</code> is an instance of this class
     * representing the same dialog state as this, <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == this) return true;

        return (obj instanceof DialogState) && ((DialogState)obj).m_dialogState == m_dialogState;
    }

    /**
     * Get a hash code value for this dialog state.
     * 
     * @since 1.2
     * @return a hash code value.
     */
    public int hashCode() {
        return m_dialogState;
    }        
    
    
    /**
     * This method returns a string version of this class.
     * @return The string version of the DialogState
     */
    public String toString() {
        String text = "";
        switch (m_dialogState) {
            case _EARLY:
                text = "Early Dialog";
                break;
            case _CONFIRMED:
                text = "Confirmed Dialog";
                break;
            case _COMPLETED:
                text = "Completed Dialog";
                break;    
            case _TERMINATED:
                text = "Terminated Dialog";
                break;                  
            default:
                text = "Error while printing Dialog State";
                break;
        }
        return text;
    }

    // internal variables
    private int m_dialogState;
    private static int m_size = 4;
    private static DialogState[] m_dialogStateArray = new DialogState[m_size];
        
    /**
     * This constant value indicates the internal value of the "Early" 
     * constant.
     * <br>This constant has an integer value of 0.
     */    
    public static final int _EARLY = 0;

    /**
     * This constant value indicates that the dialog state is "Early".
     */        
    public final static DialogState EARLY = new DialogState(_EARLY);
    
    /**
     * This constant value indicates the internal value of the "Confirmed" 
     * constant.
     * <br>This constant has an integer value of 1.
     */    
    public static final int _CONFIRMED = 1;

    /**
     * This constant value indicates that the dialog state is "Confirmed".
     */        
    public final static DialogState CONFIRMED = new DialogState(_CONFIRMED);
    
    /**
     * This constant value indicates the internal value of the "Completed" 
     * constant.
     * <br>This constant has an integer value of 2.
     * @deprecated Since v1.2. This state does not exist in a dialog.
     */    
    public static final int _COMPLETED = 2;
    
    /**
     * This constant value indicates that the dialog state is "Completed".
     * @deprecated Since v1.2. This state does not exist in a dialog.
     */        
    public final static DialogState COMPLETED = new DialogState(_COMPLETED);

    /**
     * This constant value indicates the internal value of the "Terminated" 
     * constant.
     * <br>This constant has an integer value of 3.
     */    
    public static final int _TERMINATED = 3;
    
    /**
     * This constant value indicates that the dialog state is "Terminated".
     */        
    public final static DialogState TERMINATED = new DialogState(_TERMINATED);    
    

}





















