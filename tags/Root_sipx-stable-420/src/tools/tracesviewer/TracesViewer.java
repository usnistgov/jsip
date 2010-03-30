/*
 * TraceViewer.java
 *
 * Created on April 16, 2002, 2:41 PM
 */

package tools.tracesviewer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class TracesViewer extends javax.swing.JFrame {

    private boolean standaloneViewer = false;

    private String logFile;

    // Menus
    protected JMenuBar menuBar;
    protected JMenu displayAllSessionsMenu;
    protected JMenu optionsMenu;
    protected JMenu refreshMenu;
    protected JMenu aboutMenu;
    protected JMenu helpMenu;
    protected JMenu quitMenu;

    protected JMenuItem animationMenuItem;
    protected JMenuItem stackIdMenuItem;

    // The container: contain panels (one left and one center)
    protected JPanel firstPanel;
    protected JPanel secondPanel;
    protected JPanel firstSubPanel;
    protected JPanel secondSubPanel;
    protected JPanel thirdSubPanel;

    protected JButton scriptButton;
    //protected JButton refreshButton;
    protected Choice choice;

    protected ListenerTracesViewer listenerTracesViewer;

    // All the components inside the second panel
    protected JLabel sessionsLabel;
    protected TracesSessionsList tracesSessionsList;
    protected JButton messageContentButton;
    protected TextArea messageContentTextArea;

    protected TracesSessions tracesSessions;
    protected TracesCanvas tracesCanvas;

    protected static String rmiHost;
    protected static String rmiPort;
    protected static String stackId;

    protected static Image actorsImage = null;
    protected static Image backgroundImage = null;
    protected static Image facesImage = null;
    protected static Image logoNist = null;

    protected TracesAnimationThread animationThread = null;

    /**
    * Call this constructor when you want to construct a visualizer
    * from traces that you have already read.
    *
    *@param titles is an array containing the titles for the traces.
    *@param traces is an array containing an array of XML Formatted traces.
    *
    */
    public TracesViewer(
        Hashtable traces,
        String logName,
        String logDescription,
        String auxInfo) {
        //System.out.println("**** TRACE ******:\n"+trace+"\n");
        TracesSessions tss = new TracesSessions();
        Enumeration elements = traces.elements();
        tss.setName(logName);
        logFile = logName;
        while (elements.hasMoreElements()) {
            MessageLogList mll = (MessageLogList) elements.nextElement();
            TracesSession ts = new TracesSession(mll);
            ts.setName(logName);
            ts.setInfo(auxInfo);
            ts.setLogDescription(logDescription);
            tss.add(ts);
        }

        listenerTracesViewer = new ListenerTracesViewer(this);

        // The order is important!!!!!
        // Look at the rmi registry for new traces
        // tracesSessions=refreshTracesSessions();
        this.tracesSessions = tss;
        this.standaloneViewer = true;
        initComponents();
        // Initialisation of the tracesSessionsList:
        tracesSessionsList.setTracesSessions(this.tracesSessions);
        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

        // width, height
        this.setSize(670, 620);
        this.setLocation(0, 0);
    }

    // Constructor for the GUILauncher only!!!!!
    public TracesViewer(
        String title,
        String aRmiHost,
        String aRmiPort,
        String aStackId) {
        super(title);
        try {
            /*
                Toolkit toolkit=Toolkit.getDefaultToolkit();
                backgroundImage=toolkit.getImage("./images/back.gif");
                actorsImage=toolkit.getImage("./images/comp.gif");
                facesImage=toolkit.getImage("./images/faces.jpg");
                logoNist=toolkit.getImage("./images/nistBanner.jpg");
             */

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            // this.backgroundImage=toolkit.getImage(back);
            URL url = TracesViewer.class.getResource("images/back.gif");
            // System.out.println("url:"+url.toString());
            backgroundImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource("images/comp.gif");
            actorsImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource("images/faces.jpg");
            facesImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource("images/nistBanner.jpg");
            logoNist = toolkit.getImage(url);

        } catch (Exception e) {
            backgroundImage = null;
            actorsImage = null;
            facesImage = null;
            logoNist = null;
            System.out.println("Problem with the Toolkit: no loaded images!!!");
            e.printStackTrace();
        }

        rmiHost = aRmiHost;
        rmiPort = aRmiPort;
        stackId = aStackId;

        listenerTracesViewer = new ListenerTracesViewer(this);

        // The order is important!!!!!
        // Look at the rmi registry for new traces
        tracesSessions = refreshTracesSessions();
        initComponents();
        // Initialisation of the tracesSessionsList:
        tracesSessionsList.setTracesSessions(tracesSessions);
        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

        // width, height
        this.setSize(670, 620);
        // this.setLocation(0,0);
        this.show();
    }

    // Constructor for the Application only!!!!!
    public TracesViewer(
        String title,
        String aRmiHost,
        String aRmiPort,
        String aStackId,
        String back,
        String faces,
        String actors,
        String aLogoNist) {
        super(title);
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            //System.out.println("back:"+back);
            // this.backgroundImage=toolkit.getImage(back);
            URL url = TracesViewer.class.getResource(back);
            //  System.out.println("url:"+url.toString());
            backgroundImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(actors);
            actorsImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(faces);
            facesImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(aLogoNist);
            logoNist = toolkit.getImage(url);

        } catch (Exception e) {
            backgroundImage = null;
            actorsImage = null;
            facesImage = null;
            logoNist = null;
            System.out.println("Images are not loaded.");

            e.printStackTrace();
        }

        rmiHost = aRmiHost;
        rmiPort = aRmiPort;
        stackId = aStackId;

        listenerTracesViewer = new ListenerTracesViewer(this);

        // The order is important!!!!!
        // Look at the rmi registry for new traces
        tracesSessions = refreshTracesSessions();
        initComponents();
        // Initialisation of the tracesSessionsList:
        tracesSessionsList.setTracesSessions(tracesSessions);
        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

        // width, height
        this.setSize(670, 620);
        this.setLocation(0, 0);
        this.show();
    }

    /*********** constructor for the proxy ****************************************/

    // Constructor for the Application only!!!!!
    public TracesViewer(
        String logFile,
        Hashtable traces,
        String logName,
        String logDescription,
        String auxInfo,
        String title,
        String back,
        String faces,
        String actors,
        String aLogoNist) {
        super(title);
        this.logFile = logFile;
        //System.out.println("back:"+back);
        try {

            Toolkit toolkit = Toolkit.getDefaultToolkit();
            //System.out.println("back:"+back);
            // this.backgroundImage=toolkit.getImage(back);
            URL url = TracesViewer.class.getResource(back);
            //  System.out.println("url:"+url.toString());
            backgroundImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(actors);
            actorsImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(faces);
            facesImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(aLogoNist);
            logoNist = toolkit.getImage(url);

            /*
               Toolkit toolkit=Toolkit.getDefaultToolkit();
               backgroundImage=toolkit.getImage(back);
               actorsImage=toolkit.getImage(actors);
               facesImage=toolkit.getImage(faces);
               this.logoNist=logoNist;
             */
        } catch (Exception e) {
            backgroundImage = null;
            actorsImage = null;
            facesImage = null;
            logoNist = null;
            System.out.println("Images are not loaded.");

            e.printStackTrace();
        }

        TracesSessions tss = new TracesSessions();
        tss.setName(logName);
        if (traces != null) {
            Enumeration elements = traces.elements();
            while (elements.hasMoreElements()) {
                MessageLogList mll = (MessageLogList) elements.nextElement();
                TracesSession ts = new TracesSession(mll);
                ts.setName(logName);
                ts.setInfo(auxInfo);
                ts.setLogDescription(logDescription);
                tss.add(ts);
            }
        }
        if (tss.isEmpty()) {
            TracesSession ts = new TracesSession();
            ts.setName("No available session, refresh");
            tss.add(ts);
        }

        listenerTracesViewer = new ListenerTracesViewer(this);

        this.tracesSessions = tss;
        initComponents();
        // Initialisation of the tracesSessionsList:
        tracesSessionsList.setTracesSessions(this.tracesSessions);
        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

        // width, height
        this.setSize(670, 620);
        this.setLocation(0, 0);
    }

    /** Use this constructor when retrieving a trace from  a log file.
     */
    public TracesViewer(
        String title,
        String logFile,
        String back,
        String faces,
        String actors,
        String logoNist) {
        super(title);
        this.logFile = logFile;
        listenerTracesViewer = new ListenerTracesViewer(this);

        // The order is important!!!!!
        // Look at the rmi registry for new traces
        tracesSessions = refreshTracesSessions();
        initComponents();
        // Initialisation of the tracesSessionsList:
        tracesSessionsList.setTracesSessions(tracesSessions);
        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

        // width, height
        this.setSize(670, 620);
        this.setLocation(0, 0);
        this.show();

    }

    /*********************************************************************************/

    // Constructor for the daemon only!!!!!
    public TracesViewer(
        String port,
        String logFile,
        Hashtable traces,
        String logName,
        String logDescription,
        String auxInfo,
        String title,
        String back,
        String faces,
        String actors,
        String aLogoNist) {
        super(title);
        this.logFile = logFile;

        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            URL url = TracesViewer.class.getResource(back);
            backgroundImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(actors);
            actorsImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(faces);
            facesImage = toolkit.getImage(url);
            url = TracesViewer.class.getResource(aLogoNist);
            logoNist = toolkit.getImage(url);
        } catch (Exception e) {
            backgroundImage = null;
            actorsImage = null;
            facesImage = null;
            logoNist = null;
            System.out.println("Images are not loaded.");

            e.printStackTrace();
        }

        TracesSessions tss = new TracesSessions();
        tss.setName(logName);
        if (traces != null) {
            Enumeration elements = traces.elements();
            while (elements.hasMoreElements()) {
                MessageLogList mll = (MessageLogList) elements.nextElement();
                TracesSession ts = new TracesSession(mll);
                ts.setName(logName);
                ts.setInfo(auxInfo);
                ts.setLogDescription(logDescription);
                tss.add(ts);
            }
        }
        if (tss.isEmpty()) {
            TracesSession ts = new TracesSession();
            ts.setName("No available session, refresh");
            tss.add(ts);
        }

        listenerTracesViewer = new ListenerTracesViewer(this);

        this.tracesSessions = tss;
        initComponents();
        // Initialisation of the tracesSessionsList:
        tracesSessionsList.setTracesSessions(this.tracesSessions);
        // Initialisation of the Thread for the animations:
        animationThread = new TracesAnimationThread(tracesCanvas);

        // width, height
        this.setSize(670, 620);
        this.setLocation(0, 0);

        try {
            // Try to open a connection:
            TracesSocket ts = new TracesSocket(logFile, port);
            ts.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.show();
    }

    /*****************************************************************************/

    public TracesSessions refreshTracesSessions() {
        TracesSessions retval = null;
        TracesSessions tss = new TracesSessions();
        String trace = null;
        try {
            if (this.logFile != null) {
                File file = new File(this.logFile);
                char[] cbuf = new char[(int) file.length()];
                FileReader fr = new FileReader(file);
                fr.read(cbuf);
                fr.close();
                trace = new String(cbuf);

            }
            //System.out.println("**** TRACE ******:\n"+trace+"\n");
            if (trace != null && !trace.equals("")) {
                LogFileParser parser = new LogFileParser();
                Hashtable traces = parser.parseLogsFromString(trace);
                Enumeration elements = traces.elements();
                while (elements.hasMoreElements()) {
                    MessageLogList mll =
                        (MessageLogList) elements.nextElement();
                    TracesSession ts = new TracesSession(mll);
                    ts.setName(parser.logName);
                    ts.setLogDescription(parser.logDescription);
                    ts.setInfo(parser.auxInfo);
                    tss.add(ts);
                }
                tss.setName(parser.logName);

                retval = tss;
            } else {
                TracesSession ts = new TracesSession();
                ts.setName("No available session, refresh");
                tss.add(ts);
                retval = tss;
            }
        } catch (Exception e) {
            System.out.println("*** Exception retrieving trace ***");
            e.printStackTrace();
            TracesSession ts = new TracesSession();
            ts.setName("No available session, refresh");
            tss.add(ts);
            retval = tss;
        }

        tracesSessions = retval;
        return tracesSessions;
    }

    public TracesSessions getTracesSessions() {
        return tracesSessions;
    }

    /**************************************************************************/

    private void initComponents() {

        /********************** The main container ****************************/

        Container container = this.getContentPane();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        //container.setLayout( new PercentLayout() );
        container.setBackground(Color.black);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.out.println("Trace viewer closed!");
                // System.exit(0);
            }
        });

        /********************** Menu bar **************************************/
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        //Build a second menu in the menu bar.
        optionsMenu = new JMenu("      Options      ");
        optionsMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        optionsMenu.setToolTipText("Some options related to the GUI");
        animationMenuItem = new JMenuItem("    Animation    ");
        animationMenuItem.setToolTipText(
            "Animation of the entire selected session");
        animationMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                listenerTracesViewer.animationActionPerformed(evt);
            }
        });
        optionsMenu.add(animationMenuItem);
        menuBar.add(optionsMenu);

        // create a menu and add it to the menubar
        displayAllSessionsMenu = new JMenu("    Display Sessions    ");
        displayAllSessionsMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        displayAllSessionsMenu.setToolTipText(
            "Display all the retrieved sessions in a separate windows");
        displayAllSessionsMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                listenerTracesViewer.displayAllSessionsMouseEvent(evt);
            }
        });
        // add the Controls menu to the menu bar
        menuBar.add(displayAllSessionsMenu);

        // create a menu and add it to the menubar
        refreshMenu = new JMenu("   Refresh   ");
        refreshMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        refreshMenu.setBackground(new Color(51, 153, 255));
        refreshMenu.setToolTipText("Get the new traces");
        refreshMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                listenerTracesViewer.refreshActionPerformed(evt);
            }
        });
        // add the Controls menu to the menu bar
        menuBar.add(refreshMenu);

        //...create and add some menus...
        menuBar.add(Box.createHorizontalGlue());

        helpMenu = new JMenu("    Help    ");
        helpMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        helpMenu.setToolTipText("Some useful notes about this tool");
        helpMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                listenerTracesViewer.helpMenuMouseEvent(evt);
            }
        });
        menuBar.add(helpMenu);

        aboutMenu = new JMenu("    About    ");
        aboutMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        aboutMenu.setToolTipText("Some advertises about the creators!");
        aboutMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                listenerTracesViewer.aboutMenuMouseEvent(evt);
            }
        });
        menuBar.add(aboutMenu);

        quitMenu = new JMenu("    Quit    ");
        quitMenu.setBorder(new BevelBorder(BevelBorder.RAISED));
        quitMenu.setToolTipText("Quit the traces viewer");
        quitMenu.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                close();
            }
        });
        menuBar.add(quitMenu);

        /********************    FIRST PANEL         ********************************/

        firstPanel = new JPanel();
        // If put to False: we see the container's background
        firstPanel.setOpaque(false);
        firstPanel.setLayout(new PercentLayout());
        //firstPanel.setLayout(  new BorderLayout() );
        container.add(firstPanel);

        // Sub right panel:
        // topx %, topy %, width %, height % 73, 100-> 65, 95
        PercentLayoutConstraint firstPanelConstraint =
            new PercentLayoutConstraint(30, 0, 70, 100);
        tracesSessionsList = new TracesSessionsList();
        tracesSessionsList.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                listenerTracesViewer.tracesSessionsListStateChanged(e);
            }
        });
        tracesSessionsList.setForeground(Color.black);
        tracesSessionsList.setFont(new Font("Dialog", 1, 14));

        ScrollPane scroll = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        TracesSession tracesSession =
            (TracesSession) tracesSessions.firstElement();
        String name = tracesSession.getName();
        String logDescription = tracesSession.getLogDescription();
        String callId = tracesSessionsList.getCallId(name);
        String origin = tracesSessionsList.getOrigin(name);

        // Warning: to put before for the canvas!!!!
        TextArea messageContentTextArea = new TextArea();
        messageContentButton = new JButton("SIP Message:");
        if (name.equals("No available session, refresh")) {
            tracesCanvas =
                new TracesCanvas(
                    tracesSession,
                    messageContentTextArea,
                    "unknown",
                    this);
        } else if (
            logDescription == null || logDescription.trim().equals("")) {
            tracesCanvas =
                new TracesCanvas(
                    tracesSession,
                    messageContentTextArea,
                    origin,
                    this);
        } else {
            //  System.out.println("logDesc44:"+logDescription);
            tracesCanvas =
                new TracesCanvas(
                    tracesSession,
                    messageContentTextArea,
                    logDescription,
                    this);
        }

        tracesSessionsList.setTracesCanvas(tracesCanvas);
        // The ScrollPane for the Canvas
        scroll.add(tracesCanvas);
        firstPanel.add(scroll, firstPanelConstraint);

        /***************************    SECOND PANEL         ********************************/

        //  left panel:
        secondPanel = new JPanel();
        secondPanel.setBackground(Color.black);
        // rows, columns
        //  secondPanel.setLayout(new GridLayout(3,1,0,0) );
        secondPanel.setLayout(new BorderLayout());
        // topx %, topy %, width %, height %
        PercentLayoutConstraint secondPanelConstraint =
            new PercentLayoutConstraint(0, 0, 30, 100);
        firstPanel.add(secondPanel, secondPanelConstraint);

        /****************************** FIRST SUB PANEL **********************************/

        // Sub left panel:
        firstSubPanel = new JPanel();
        firstSubPanel.setBackground(Color.black);
        // Top, left, bottom, right
        firstSubPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 7, 5));

        if (!standaloneViewer) {
            // rows, columns, gap, gap
            firstSubPanel.setLayout(new GridLayout(2, 1, 3, 6));
            secondPanel.add(firstSubPanel, BorderLayout.NORTH);

            JPanel panelGrid = new JPanel();
            panelGrid.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panelGrid.setLayout(new GridLayout(2, 1, 0, 0));

            JPanel panelBox = new JPanel();
            panelBox.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            panelBox.setLayout(new BorderLayout());

            JLabel scriptLabel = new JLabel("Display the event script:");
            scriptLabel.setToolTipText(
                "Display the content of the selected script");

            scriptLabel.setHorizontalAlignment(AbstractButton.CENTER);
            scriptLabel.setForeground(Color.black);
            scriptLabel.setFont(new Font("Dialog", 1, 14));
            // If put to true: we see the label's background
            scriptLabel.setOpaque(true);
            panelGrid.add(scriptLabel);

            choice = new Choice();
            panelBox.add(choice, BorderLayout.CENTER);

            scriptButton = new JButton("Open");
            scriptButton.setToolTipText(
                "Get the script controlling the current session");
            scriptButton.setFont(new Font("Dialog", 1, 14));
            scriptButton.setFocusPainted(false);
            scriptButton.setBackground(new Color(186, 175, 175));
            scriptButton.setBorder(new BevelBorder(BevelBorder.RAISED));
            scriptButton.setVerticalAlignment(AbstractButton.CENTER);
            scriptButton.setHorizontalAlignment(AbstractButton.CENTER);
            panelBox.add(scriptButton, BorderLayout.EAST);
            scriptButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    listenerTracesViewer.scriptActionPerformed(evt);
                }
            });
            panelGrid.add(panelBox);
            firstSubPanel.add(panelGrid);
            //initComboBox();

            /*
            refreshButton=new JButton("Refresh");
            refreshButton.setToolTipText("Refresh all the sessions");
            refreshButton.setFont(new Font ("Dialog", 1, 14));
            refreshButton.setFocusPainted(false);
            //refreshButton.setBackground(new Color(186,175,175));
            refreshButton.setBackground( new Color(51,153,255));
            refreshButton.setBorder(new BevelBorder(BevelBorder.RAISED));
            refreshButton.setVerticalAlignment(AbstractButton.CENTER);
            refreshButton.setHorizontalAlignment(AbstractButton.CENTER);
            firstSubPanel.add(refreshButton);
            refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                     listenerTracesViewer.refreshActionPerformed(evt);
               }
            }
            );
             */
            ImageIcon icon;
            if (logoNist != null) {
                icon = new ImageIcon(logoNist);

                JLabel label = new JLabel(icon);
                label.setVisible(true);
                label.setToolTipText("The NIST logo!!!");
                //label.setHorizontalAlignment(AbstractButton.CENTER);
                label.setForeground(Color.black);
                //label.setFont(new Font ("Dialog", 1, 14));
                label.setOpaque(false);
                firstSubPanel.add(label);
            }
        } else {
            // rows, columns, gap, gap
            firstSubPanel.setLayout(new GridLayout(1, 1, 3, 6));
            secondPanel.add(firstSubPanel, BorderLayout.NORTH);

            ImageIcon icon;
            if (logoNist != null) {
                icon = new ImageIcon(logoNist);
                JLabel label = new JLabel(icon);
                label.setVisible(true);
                label.setToolTipText("The NIST logo!!!");
                label.setHorizontalAlignment(AbstractButton.CENTER);
                label.setForeground(Color.black);
                label.setFont(new Font("Dialog", 1, 14));
                label.setOpaque(false);
                firstSubPanel.add(label);
            }
        }

        /****************** SECOND SUB PANEL ****************************************/
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 0, 0));
        secondPanel.add(panel, BorderLayout.CENTER);
        secondSubPanel = new JPanel();
        secondSubPanel.setBackground(Color.black);
        secondSubPanel.setLayout(new BorderLayout());
        //secondPanel.add(secondSubPanel);
        panel.add(secondSubPanel);

        sessionsLabel = new JLabel("Sessions available:");
        sessionsLabel.setToolTipText("All the sessions currently available");
        // Alignment of the text
        sessionsLabel.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        sessionsLabel.setForeground(Color.black);
        // Size of the text
        sessionsLabel.setFont(new Font("Dialog", 1, 14));
        // If put to true: we see the label's background
        sessionsLabel.setOpaque(true);
        sessionsLabel.setBackground(Color.lightGray);
        sessionsLabel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
        secondSubPanel.add(sessionsLabel, BorderLayout.NORTH);

        ScrollPane scrollList = new ScrollPane(ScrollPane.SCROLLBARS_ALWAYS);
        scrollList.add(tracesSessionsList);
        //secondSubPanel.add(scrollList,BorderLayout.CENTER);
        secondSubPanel.add(tracesSessionsList, BorderLayout.CENTER);

        /******************** THIRD SUB PANEL ****************************************/

        thirdSubPanel = new JPanel();
        thirdSubPanel.setBackground(Color.black);
        thirdSubPanel.setLayout(new BorderLayout());
        //secondPanel.add(thirdSubPanel);
        panel.add(thirdSubPanel);

        messageContentButton.setToolTipText(
            "Display all the content of the current SIP message");
        // Alignment of the text
        messageContentButton.setHorizontalAlignment(AbstractButton.CENTER);
        // Color of the text
        messageContentButton.setForeground(Color.black);
        // Size of the text
        messageContentButton.setFont(new Font("Dialog", 1, 14));
        // If put to true: we see the label's background
        messageContentButton.setOpaque(true);
        messageContentButton.setBackground(Color.lightGray);
        messageContentButton.setBorder(
            BorderFactory.createLineBorder(Color.darkGray));
        messageContentButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                listenerTracesViewer.debugActionPerformed(evt);
            }
        });
        messageContentTextArea.setBackground(Color.white);
        messageContentTextArea.setEditable(false);
        messageContentTextArea.setFont(new Font("Dialog", 1, 12));
        messageContentTextArea.setForeground(Color.black);
        thirdSubPanel.add(messageContentButton, BorderLayout.NORTH);
        thirdSubPanel.add(messageContentTextArea, BorderLayout.CENTER);

        validateTree();
    }

    public void close() {
        System.out.println("Trace viewer closed!");
        this.dispose();
        //  System.exit(0);

    }

    public static void usage() {
        System.out.println("***************************************");
        System.out.println("*** missing or incorrect parameters ***");
        System.out.println("*************************************\n");
        System.out.println("When you create your SIP Stack specify");
        System.out.println("gov.nist.javax.sip.stack.SERVER_LOG = fileName\n");
        System.out.println("gov.nist.javax.sip.stack.DEBUG_LOG = debugFileName\n");
        System.out.println("*************************************");
        System.out.println(
        "Use this tool to view the signaling trace as a sequence Diagram");
        System.out.println( "Usage (if classpath correctly set):\n\n");
        System.out.println("When viewing from a server file:\n" +
        " --> java tools.tracesviewer.tracesViewer -debug_file debugFileName\n");
        System.out.println("When viewing from a debug file:\n" +
            " --> java tools.tracesviewer.tracesViewer -server_file fileName");
        System.out.println("*************************************\n" );
        System.exit(0);
    }

    // This method is only used by the GUI proxy!!!!!
    // We do not need to use the prompt!!!
    public static void main(String args[]) {
        try {
            if (args.length == 0) {
                System.out.println("Using default parameters!");
                System.out.println(
                    "Everything is Ok ... Launching the Traces viewer");
                TracesViewer tracesViewer =
                    new TracesViewer(
                        "Traces viewer",
                        "127.0.0.1",
                        "1099",
                        "127.0.0.1:5060");

            } else if (args[0].equals("-debug_file")) {
                String fileName = args[1];
                LogFileParser parser = new LogFileParser();
                Hashtable traces = parser.parseLogsFromDebugFile(fileName);

                new TracesViewer(
                    fileName,
                    traces,
                    parser.logName,
                    parser.logDescription,
                    parser.auxInfo,
                    "traces viewer",
                    "images/back.gif",
                    "images/faces.jpg",
                    "images/comp.gif",
                    "images/nistBanner.jpg")
                    .show();

                return;

            } else if (args[0].equals("-server_file")) {
                String fileName = args[1];
                LogFileParser parser = new LogFileParser();
                Hashtable traces = parser.parseLogsFromFile(fileName);

                new TracesViewer(
                    fileName,
                    traces,
                    parser.logName,
                    parser.logDescription,
                    parser.auxInfo,
                    "traces viewer",
                    "images/back.gif",
                    "images/faces.jpg",
                    "images/comp.gif",
                    "images/nistBanner.jpg")
                    .show();

                return;

            } else if (args[0].equals("-daemon")) {

                int length = args.length;

                String port = "10000";
                String fileName = "NOT SET";
                for (int k = 0; k < length; k++) {

                    if (args[k].equals("-server_file")) {
                        fileName = args[k + 1];
                        k++;
                    } else if (args[k].equals("-port")) {
                        port = args[k + 1];
                        k++;
                    }
                }
                LogFileParser parser = new LogFileParser();
                Hashtable traces = parser.parseLogsFromFile(fileName);

                new TracesViewer(
                    port,
                    fileName,
                    traces,
                    parser.logName,
                    parser.logDescription,
                    parser.auxInfo,
                    "traces viewer daemon",
                    "images/back.gif",
                    "images/faces.jpg",
                    "images/comp.gif",
                    "images/nistBanner.jpg")
                    .show();

                return;

            } else {
                int length = args.length;

                String rmiHost = "127.0.0.1";
                String rmiPort = "1099";
                String stackId = null;
                String back = null;
                String faces = null;
                String actors = null;
                String logoNist = null;
                boolean launcher = false;
                for (int k = 0; k < length; k++) {
                    if (args[k].equals("-rmihost")) {
                        rmiHost = args[k + 1];
                        k++;
                    } else if (args[k].equals("-rmiport")) {
                        rmiPort = args[k + 1];
                        k++;
                    } else if (args[k].equals("-stackId")) {
                        stackId = args[k + 1];
                        k++;
                    } else if (args[k].equals("-back")) {
                        launcher = true;
                        back = args[k + 1];
                        k++;
                    } else if (args[k].equals("-faces")) {
                        faces = args[k + 1];
                        k++;
                    } else if (args[k].equals("-actors")) {
                        actors = args[k + 1];
                        k++;
                    } else if (args[k].equals("-logoNist")) {
                        logoNist = args[k + 1];
                        k++;
                    } else
                        usage();
                }
                TracesViewer tracesViewer;
                if (rmiHost == null) {
                    System.out.println("Assuming RMI host = 127.0.0.1");
                }
                if (stackId == null) {
                    System.out.println(
                        "Stack Id (name) not specified Bailing!"
                            + " Please specify stackId (JAIN stack name) "
                            + " using -stackId flag");
                    System.exit(0);
                }

                if (launcher)
                    tracesViewer =
                        new TracesViewer(
                            "Traces viewer",
                            rmiHost,
                            rmiPort,
                            stackId,
                            back,
                            faces,
                            actors,
                            logoNist);
                else
                    tracesViewer =
                        new TracesViewer(
                            "Traces viewer",
                            rmiHost,
                            rmiPort,
                            stackId);
                System.out.println(
                    "Everything is Ok ... Launching the Traces viewer");
            }
        } catch (Exception e) {
            System.out.println("Problem starting viewer. I give up :)");
            e.printStackTrace();
        }
    }

}
