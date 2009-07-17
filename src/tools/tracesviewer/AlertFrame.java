/*
 * AlertFrame.java
 *
 * Created on April 10, 2002, 12:11 PM
 */
package tools.tracesviewer;

import javax.swing.*;

/**
*
*@version 1.2
*
*@author Olivier Deruelle   <br/>
*
*
*
*/
public class AlertFrame extends JOptionPane {

    public String finalInputValue;

    /** Creates new AlertFrame */
    public AlertFrame(String text, int messageType) {
        super("Alert");
        if (messageType == JOptionPane.ERROR_MESSAGE)
            showMessageDialog(this, text, "Error", messageType);
        else if (messageType == JOptionPane.WARNING_MESSAGE)
            showMessageDialog(this, text, "Warning", messageType);
        else
            showMessageDialog(this, "Unknown alert message");

    }

    /** Creates new AlertFrame */
    public AlertFrame(String text, int messageType, String initialInputValue) {
        super("Alert");
        finalInputValue = null;
        if (messageType == JOptionPane.ERROR_MESSAGE)
            showMessageDialog(this, text, "Error", messageType);
        else if (messageType == JOptionPane.WARNING_MESSAGE)
            showMessageDialog(this, text, "Warning", messageType);
        else if (messageType == JOptionPane.INFORMATION_MESSAGE) {
            finalInputValue =
                (String) showInputDialog(this,
                    text,
                    "Information",
                    messageType,
                    null,
                    null,
                    initialInputValue);
        } else
            showMessageDialog(this, "Unknown alert message");

    }

    /** Creates new AlertFrame */
    public AlertFrame(String text) {
        super("Alert");
        // information message by default
        showMessageDialog(this, text);
    }

}
