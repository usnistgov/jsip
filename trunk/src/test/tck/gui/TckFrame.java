package test.tck.gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.border.*;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * <p>Title: </p>
 * <p>Description: JAIN SIP 1.1 TCK GUI </p>
 * <p>Company: NIST</p>
 * @author Emil Ivov
 * This code is in the public domain.
 * @version 1.0
 */

public class TckFrame
    extends JFrame
    implements ActionListener
{

    /**
     * Closes the frame and starts the TckTestSuite if start was pressed or
     * exits if Exit was pressed;
     */
    public void actionPerformed(ActionEvent evt)
    {
        dispose();
        Object source = evt.getSource();
        if(source == startButton)
        {
            System.setProperty(test.tck.Tck.IMPLEMENTATION_PATH, pathNameField.getText().trim());
            System.setProperty(test.tck.Tck.ABORT_ON_FAIL, 	  Boolean.toString(abortCheckBox.isSelected()));
            System.setProperty(test.tck.Tck.LOG_FILE_NAME, 	  logFileField.getText().trim());
            junit.swingui.TestRunner.run( test.tck.TckTestSuite.class );
        }
        else if( source == exitButton)
        {
            System.exit(0 );
        }
    }


    /**
     * Sets the values of the path name field and the checkbox
     */
    protected void initFields()
    {
        //path name
        String pathName = System.getProperty(test.tck.Tck.IMPLEMENTATION_PATH);
        if(pathName == null || pathName.trim().length() == 0)
            pathName = "gov.nist";
        pathNameField.setText(pathName);

        //log file name
        String logFileName = System.getProperty(test.tck.Tck.LOG_FILE_NAME);
        if(logFileName == null || logFileName.trim().length() == 0)
            logFileName = "tcklog.txt";
        logFileField.setText(logFileName);


        //checkBox
        boolean abortOnFail =
                Boolean.valueOf(
                    System.getProperty(
                        test.tck.Tck.ABORT_ON_FAIL)).booleanValue();
        abortCheckBox.setSelected(abortOnFail);
    }

    protected void processWindowEvent(java.awt.event.WindowEvent evt)
    {
        super.processWindowEvent(evt);
        if(evt.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING)
            System.exit(0);
    }

    private void center()
    {
        int x = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()
                 - getWidth())/2;
        int y = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()
                 - getHeight())/2;
        setLocation(x, y);
    }


    //generated Stuff

    JPanel rootPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel northLabel = new JLabel();
    Icon northIcon = null;
    Icon pathNameIcon = null;
    Icon logFileIcon = null;
    Icon abortIcon = null;
    Border border3;
    JPanel centerPane = new JPanel();
    JPanel buttonsPanel = new JPanel();
    JButton exitButton = new JButton();
    JButton startButton = new JButton();
    Border border4;
    JLabel pathNameLabel = new JLabel();
    JLabel logFileLabel = new JLabel();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel pathNamePanel = new JPanel();
    JPanel logFilePanel = new JPanel();
    JPanel abortPanel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    JLabel abortLabel = new JLabel();
    JCheckBox abortCheckBox = new JCheckBox();
    JPanel fitFieldPanel = new JPanel();
    JPanel fitLogFieldPanel = new JPanel();
    JTextField pathNameField = new JTextField();
    JTextField logFileField = new JTextField();
    BorderLayout borderLayout4 = new BorderLayout();
    BorderLayout fitBorderLayout = new BorderLayout();
    BorderLayout logFileBorderLayout = new BorderLayout();
    Border border5;
    Border border6;

    public TckFrame() throws HeadlessException
    {
        northIcon = new ImageIcon(getClass().getResource("images/northbanner.jpg"));
        northLabel.setIcon(northIcon);

        pathNameIcon = new ImageIcon(getClass().getResource("images/pathname.jpg"));
        pathNameLabel.setIcon(pathNameIcon);

        logFileIcon = new ImageIcon(getClass().getResource("images/logfile.jpg"));
        logFileLabel.setIcon(logFileIcon);

        abortIcon = new ImageIcon(getClass().getResource("images/abort.jpg"));
        abortLabel.setIcon(abortIcon);

        try
        {
            jbInit();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        startButton.addActionListener(this);
        exitButton.addActionListener(this);
        initFields();

        pack();
        center();
        show();
    }

    private void jbInit() throws Exception
    {
        border3 = BorderFactory.createEmptyBorder(10,20,27,20);
        border4 = BorderFactory.createEmptyBorder();
        border5 = BorderFactory.createEmptyBorder();
        border6 = BorderFactory.createEmptyBorder(0,0,5,0);
        rootPanel.setBackground(Color.white);
        rootPanel.setBorder(border3);
        rootPanel.setMinimumSize(new Dimension(450, 340));
        rootPanel.setPreferredSize(new Dimension(450, 340));
        rootPanel.setLayout(borderLayout1);
        northLabel.setIcon(northIcon);
        centerPane.setBackground(Color.white);
        centerPane.setPreferredSize(new Dimension(342, 108));
        centerPane.setLayout(null);
        exitButton.setBackground(Color.white);
        exitButton.setMnemonic('X');
        exitButton.setText("Exit");
        startButton.setBackground(Color.white);
        startButton.setActionCommand("jButton2");
        startButton.setMnemonic('S');
        startButton.setText("Start");
        buttonsPanel.setBackground(Color.white);
        buttonsPanel.setBorder(border4);
        this.setEnabled(true);
        pathNameLabel.setRequestFocusEnabled(true);
        pathNameLabel.setText("");
        pathNamePanel.setLayout(borderLayout3);
        logFileLabel.setRequestFocusEnabled(true);
        logFileLabel.setText("");
        logFilePanel.setLayout(logFileBorderLayout);
        abortPanel.setLayout(borderLayout2);
        abortLabel.setText("");
        abortCheckBox.setBackground(Color.white);
        abortCheckBox.setBorder(border5);
        pathNameField.setPreferredSize(new Dimension(150, 20));
        pathNameField.setText("");
        logFileField.setPreferredSize(new Dimension(150, 20));
        logFileField.setText("");
        fitFieldPanel.setLayout(borderLayout4);
        fitFieldPanel.setBackground(Color.white);
        fitFieldPanel.setBorder(border6);
        fitLogFieldPanel.setLayout(fitBorderLayout);
        fitLogFieldPanel.setBackground(Color.white);
        fitLogFieldPanel.setBorder(border6);
        pathNamePanel.setBackground(Color.white);
        pathNamePanel.setBounds(new Rectangle(0, 0, 342, 68));
        logFilePanel.setBackground(Color.white);
        logFilePanel.setBounds(new Rectangle(0, 68, 342, 40));
        abortPanel.setBackground(Color.white);
        abortPanel.setBounds(new Rectangle(0, 106, 342, 40));
        this.getContentPane().add(rootPanel, BorderLayout.CENTER);
        rootPanel.add(northLabel,  BorderLayout.NORTH);
        rootPanel.add(centerPane, BorderLayout.WEST);
        rootPanel.add(buttonsPanel,  BorderLayout.SOUTH);
        buttonsPanel.add(startButton, null);
        buttonsPanel.add(exitButton, null);
        centerPane.add(pathNamePanel, null);
        centerPane.add(logFilePanel, null);
        pathNamePanel.add(pathNameLabel, BorderLayout.WEST);
        pathNamePanel.add(fitFieldPanel,  BorderLayout.CENTER);
        logFilePanel.add(logFileLabel, BorderLayout.WEST);
        logFilePanel.add(fitLogFieldPanel,  BorderLayout.CENTER);
        fitLogFieldPanel.add(logFileField,  BorderLayout.SOUTH);
        fitFieldPanel.add(pathNameField,  BorderLayout.SOUTH);
        centerPane.add(abortPanel, null);
        abortPanel.add(abortLabel, BorderLayout.WEST);
        abortPanel.add(abortCheckBox, BorderLayout.CENTER);
        this.setTitle("JAIN SIP 1.1 Technology Compatibility Kit");
    }

    public static void main(String[] args)
    {
        TckFrame tckFrame = new TckFrame();

    }

}
