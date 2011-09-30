package tools.tracesviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DebugWindow extends javax.swing.JFrame {

    public JPanel mainPanel;
    public JButton okButton;

    public DebugWindow(String beforeDebug, String afterDebug, String title) {
        super("Debug window: Debug Log Line " + title);

        initComponents(beforeDebug, afterDebug);
    }

    public void initComponents(String beforeDebug, String afterDebug) {
        /********************** The main container ****************************/

        Container container = this.getContentPane();

        container.setBackground(Color.black);
        this.setSize(700, 550);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

            }
        });

        /*************************** MAIN PANEL ********************************/

        mainPanel = new JPanel();
        // If put to False: we see the container's background
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
        container.add(mainPanel);

        JTextArea beforeDebugTextArea = new JTextArea();
        beforeDebugTextArea.setEditable(false);
        beforeDebugTextArea.setFont(new Font("Dialog", 1, 12));
        beforeDebugTextArea.setForeground(Color.black);
        beforeDebugTextArea.setText(
            "***********************************************\n"
                + "STACK TRACE BEFORE \n"
                + " THE SIP MESSAGE\n"
                + "***********************************************\n\n"
                + beforeDebug);
        ScrollPane scrollBefore = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollBefore.add(beforeDebugTextArea);
        mainPanel.add(scrollBefore);

        JTextArea afterDebugTextArea = new JTextArea();
        afterDebugTextArea.setEditable(false);
        afterDebugTextArea.setFont(new Font("Dialog", 1, 12));
        afterDebugTextArea.setForeground(Color.black);
        afterDebugTextArea.setText(
            "************************************************\n"
                + "STACK TRACE AFTER \n"
                + " THE SIP MESSAGE\n"
                + "************************************************\n\n"
                + afterDebug);
        ScrollPane scrollAfter = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollAfter.add(afterDebugTextArea);
        mainPanel.add(scrollAfter);

    }

}
