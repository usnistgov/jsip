package examples.reinvite;

import java.util.Properties;

import javax.sip.PeerUnavailableException;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipStack;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

/**
 * @author M. Ranganathan
 *
 */
public class ProtocolObjects {
    static  AddressFactory addressFactory;

    static MessageFactory messageFactory;

    static HeaderFactory headerFactory;

    static SipStack sipStack;

    static int logLevel  = 32;

    public static String logFileDirectory = "";

    public static String transport = "udp";



    static void init(String stackname, boolean autoDialog)
    {
        SipFactory sipFactory = null;

        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // If you want to try TCP transport change the following to



        // If you want to use UDP then uncomment this.
        properties.setProperty("javax.sip.STACK_NAME", stackname);

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
            logFileDirectory + stackname + "debuglog.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                logFileDirectory + stackname + "log.txt");

        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
                    (autoDialog? "on": "off"));

        // Set to 0 in your production code for max speed.
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", new Integer(logLevel).toString());

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);

            System.out.println("createSipStack " + sipStack);
        } catch (Exception e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            throw new RuntimeException("Stack failed to initialize");
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
        } catch (SipException ex) {
            ex.printStackTrace();
            throw new RuntimeException ( ex);
        }
    }

    public static void destroy() {
        sipStack.stop();
    }

    public static void start() throws Exception  {
        sipStack.start();

    }
}
