package tools.tracesviewer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class TracesSessionsDisplayer extends javax.swing.JFrame {

    public TracesSessions tracesSessions;
    public TextArea allmessagesTextArea;
    public List sessionsList;

    public JPanel mainPanel;
    public JButton okButton;

    public TracesSessionsDisplayer() {
        super("Sessions Displayer");
        this.tracesSessions = null;
        initComponents();
    }

    public void initComponents() {
        /********************** The main container ****************************/

        Container container = this.getContentPane();
        container.setLayout(new BorderLayout());
        container.setBackground(Color.black);
        this.setSize(650, 600);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

            }
        });

        /*************************** MAIN PANEL ********************************/

        mainPanel = new JPanel();
        // If put to False: we see the container's background
        mainPanel.setOpaque(false);
        mainPanel.setLayout(new BorderLayout());
        container.add(mainPanel, BorderLayout.CENTER);

        allmessagesTextArea = new TextArea();
        allmessagesTextArea.setEditable(false);
        allmessagesTextArea.setFont(new Font("Dialog", 1, 12));
        allmessagesTextArea.setForeground(Color.black);
        allmessagesTextArea.append(
            "Select a session in the list to view its messages");
        mainPanel.add(allmessagesTextArea, BorderLayout.CENTER);

        sessionsList = new List();
        sessionsList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                showMessages(e);
            }
        });
        sessionsList.setForeground(Color.black);
        sessionsList.setFont(new Font("Dialog", 1, 14));
        mainPanel.add(sessionsList, BorderLayout.WEST);

        okButton = new JButton("  OK  ");
        okButton.setToolTipText("Returns to the main frame");
        okButton.setFont(new Font("Dialog", 1, 16));
        okButton.setFocusPainted(false);
        okButton.setBackground(Color.lightGray);
        okButton.setBorder(new BevelBorder(BevelBorder.RAISED));
        okButton.setVerticalAlignment(AbstractButton.CENTER);
        okButton.setHorizontalAlignment(AbstractButton.CENTER);
        container.add(okButton, BorderLayout.SOUTH);
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                hide();
            }
        });
    }

    public static String getTrueName(String name) {
        try {
            int firstIndex = name.indexOf("//");
            int secondIndex = name.indexOf("/", 2);
            String fakeName = name.substring(2, secondIndex);
            if (fakeName.equals(TracesViewer.stackId))
                return "the proxy";
            else
                return "a user agent (" + fakeName + ")";
        } catch (Exception e) {
            return "unknown";
        }
    }

    public void setTracesSessions(TracesSessions tracesSessions) {
        sessionsList.removeAll();
        this.tracesSessions = tracesSessions;
        for (int i = 0; i < tracesSessions.size(); i++) {
            TracesSession tracesSession =
                (TracesSession) tracesSessions.elementAt(i);
            String name = tracesSession.getName();
            //System.out.println("name:"+name);
            if (name.equals("No available session yet, click on refresh"))
                sessionsList.add(name);
            else {
                String trueName = getTrueName(name);
                sessionsList.add("Trace " + (i + 1) + " from " + trueName);
            }
        }
        if (tracesSessions.size() != 0)
            sessionsList.select(0);
    }

    // Item Listener stuff
    public void showMessages(ItemEvent e) {
        int index = 0;
        if (e != null)
            index = ((Integer) e.getItem()).intValue();
        allmessagesTextArea.setText("");
        TracesSession tS = (TracesSession) tracesSessions.elementAt(index);
        for (int i = 0; i < tS.size(); i++) {
            TracesMessage tM = (TracesMessage) tS.elementAt(i);
            //allmessagesTextArea.setForeground(Color.red);
            //allmessagesTextArea.setFont(new Font ("Dialog", 1, 18));
            allmessagesTextArea.append(
                "Message "
                    + (i + 1)
                    + " from "
                    + tM.getFrom()
                    + " to "
                    + tM.getTo());
            allmessagesTextArea.append("\n\n");
            //allmessagesTextArea.setForeground(Color.black);
            //allmessagesTextArea.setFont(new Font ("Dialog", 1, 14));
            allmessagesTextArea.append(tM.getMessageString());
            allmessagesTextArea.append("\n");
        }
        allmessagesTextArea.select(0, 0);
    }

    public void show(TracesSessions tracesSessions) {
        //System.out.println("tracesSessions:"+tracesSessions);
        this.tracesSessions = tracesSessions;
        setTracesSessions(tracesSessions);
        showMessages(null);
        this.setVisible(true);
    }

}
