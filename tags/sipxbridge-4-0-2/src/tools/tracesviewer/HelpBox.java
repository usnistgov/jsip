/*
 * HelpBox.java
 *
 * Created on April 15, 2002, 10:55 AM
 */
package tools.tracesviewer;

import java.awt.*;
import java.awt.event.*;

/**
*@version 1.2
*
*@author Olivier Deruelle   <br/>
*
*
*/
public class HelpBox extends Dialog {

    TextArea helpTextArea;
    Button ok;

    /** Creates new HelpBox */
    public HelpBox() {
        super(new Frame(), " Help ", false);
        this.setBackground(Color.white);
        this.setLayout(new BorderLayout());
        helpTextArea = new TextArea();
        helpTextArea.setEditable(false);
        // fill the help box
        helpTextArea.append(
            "National Institute of Standards and Technology\n"
                + "========================================\n"
                + "\n"
                + "NIST-SIP Trace viewer 1.1\n"
                + "========================================\n"
                + "\n"
                + "\n"
                + " Hit refresh  to get new trace data from the proxy. \n"
                + "If no traces appear at all, you should check if the proxy is started.\n"
                + "Once some SIP sessions are available, you can click directly on \n"
                + "an arrow representing a SIP message of your \n"
                + "choice, and see the text of the chosen SIP message. \n"
                + "If any small yellow bubbles appear on the top right of the SIP message, \n"
                + "you can click on it and see some extra informations logged by the proxy.\n"
                + "\n"
                + "\n"
                + "If you experience any problems please contact:\n"
                + "nist-sip-dev@antd.nist.gov\n"
                + "\n");
        ok = new Button(" Ok ");
        ok.setBackground(Color.lightGray);
        ok.setForeground(Color.black);
        this.add(helpTextArea, BorderLayout.CENTER);
        this.add(ok, BorderLayout.SOUTH);
        ok.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                setVisible(false);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });
        // width, height
        this.setSize(400, 400);
    }

}
