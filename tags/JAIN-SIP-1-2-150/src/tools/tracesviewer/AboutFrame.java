/*
 * AboutFrame.java
 *
 * Created on April 16, 2002, 10:47 AM
 */

package tools.tracesviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
*@version 1.2
*
*
*@author Olivier Deruelle   <br/>
*
*
*
*/
public class AboutFrame extends JFrame {

    public TracesAnimationThread animationThread;

    /** Creates new AboutFrame */
    public AboutFrame() {
        super("About the NIST SIP traces viewer");
        initComponents();
    }

    public void initComponents() {
        // width, height
        this.setSize(700, 450);
        Container container = this.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(Color.black);
        this.setResizable(false);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                animationThread.stop();
                hide();
            }
        });

        TracesSession tS = new TracesSession();

        TracesMessage tM1 = new TracesMessage();
        tM1.setFrom("NIST");
        tM1.setTo("SIP");
        tM1.setFirstLine("Ranganathan MUDUMBAI");
        tM1.setTime("SIP project leader");
        tM1.setTransactionId("About1");
        tM1.setStatusInfo("Our boss!!!");
        tS.addElement(tM1);

        TracesMessage tM2 = new TracesMessage();
        tM2.setFrom("SIP");
        tM2.setTo("TRACESVIEWER");
        tM2.setFirstLine("Marc BEDNAREK");
        tM2.setStatusInfo("A french coder!!!");
        tM2.setTime("Coder");
        tM2.setTransactionId("About2");
        tS.addElement(tM2);

        TracesMessage tM3 = new TracesMessage();
        tM3.setFrom("TRACESVIEWER");
        tM3.setTo("SIP");
        tM3.setFirstLine("Olivier DERUELLE");
        tM3.setStatusInfo("Myself: french coder !!!");
        tM3.setTime("Coder");
        tM3.setTransactionId("About3");
        tS.addElement(tM3);

        TracesMessage tM4 = new TracesMessage();
        tM4.setFrom("SIP");
        tM4.setTo("NIST");
        tM4.setFirstLine("Christophe CHAZEAU");
        tM4.setStatusInfo("Still a french coder: what a french team!!!");
        tM4.setTime("Coder");
        tM4.setTransactionId("About4");
        tS.addElement(tM4);

        TracesCanvas tracesCanvas =
            new TracesCanvas(
                tS,
                TracesViewer.facesImage,
                "the NIST-SIP team",
                250,
                null);
        container.add(tracesCanvas);

        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

    }

}
