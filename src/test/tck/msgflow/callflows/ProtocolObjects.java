package test.tck.msgflow.callflows;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.sip.ObjectInUseException;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.apache.log4j.Logger;

/**
 * @author M. Ranganathan
 *
 */
public class ProtocolObjects {
	public static Logger logger = Logger.getLogger(ProtocolObjects.class);
    public final AddressFactory addressFactory;

    public final MessageFactory messageFactory;

    public final HeaderFactory headerFactory;

    public final SipStack sipStack;

    public int logLevel = 32;

    String logFileDirectory = "logs/";

    public final String transport;

    private boolean isStarted;

	public boolean autoDialog;
	

    public ProtocolObjects(String stackname, String pathname, String transport,
            boolean autoDialog, boolean isBackToBackUserAgent, boolean isReentrant) {

    	this.autoDialog = autoDialog;
        this.transport = transport;
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName(pathname);
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", stackname);

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", logFileDirectory
                + stackname + "debuglog.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                logFileDirectory + stackname + "log.txt");

        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
                (autoDialog ? "on" : "off"));

        // For the forked subscribe notify test
        properties.setProperty("javax.sip.FORKABLE_EVENTS", "foo");

        //For the TelUrlRouter test.
        properties.setProperty("javax.sip.ROUTER_PATH", NonSipUriRouter.class.getName());

        // Dont use the router for all requests.
        properties.setProperty("javax.sip.USE_ROUTER_FOR_ALL_URIS", "false");


        properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "1");
        
        properties.setProperty("gov.nist.javax.sip.IS_BACK_TO_BACK_USER_AGENT", Boolean.toString(isBackToBackUserAgent));
        
        properties.setProperty("gov.nist.javax.sip.DELIVER_RETRANSMITTED_ACK_TO_LISTENER", "true");
        properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "" + isReentrant);
        if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
        	logger.info("\nNIO Enabled\n");
        	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
        }
        // Set to 0 in your production code for max speed.
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", new Integer(
                logLevel).toString());

        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);

            NonSipUriRouter router = (NonSipUriRouter) sipStack.getRouter();

            router.setMyPort(5080);

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
            throw new RuntimeException(ex);
        }
    }

    public synchronized void destroy() {

        HashSet hashSet = new HashSet();

        for (Iterator it = sipStack.getSipProviders(); it.hasNext();) {

            SipProvider sipProvider = (SipProvider) it.next();
            hashSet.add(sipProvider);
        }

        for ( Iterator it = hashSet.iterator(); it.hasNext();) {
            SipProvider sipProvider = (SipProvider) it.next();

            for (int j = 0; j < 5; j++) {
                try {
                    sipStack.deleteSipProvider(sipProvider);
                } catch (ObjectInUseException ex) {
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }

                }
            }
        }

        sipStack.stop();
    }

    public void start() throws Exception {
        if (this.isStarted)
            return;
        sipStack.start();
        this.isStarted = true;

    }
}
