package performance.uas;

import java.util.Properties;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This is the UAS application for performance testing
 *
 * @author Vladimir Ralev
 */
public class ShootmeDialogAndTxStateless implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private static final String myAddress = "127.0.0.1";

    private static final int myPort = 5080;


    protected static final String usageString = "java "
            + ShootmeDialogAndTxStateless.class.getCanonicalName() + " \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestEvent) {
        final Request request = requestEvent.getRequest();
	final ServerTransaction serverTransactionId = requestEvent.getServerTransaction();

        if (request.getMethod().equals(Request.INVITE)) {
            processInvite(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.ACK)) {
            processAck(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent, serverTransactionId);
        } else if (request.getMethod().equals(Request.CANCEL)) {
            processCancel(requestEvent, serverTransactionId);
        }

    }

    public void processResponse(ResponseEvent responseEvent) {
    }

    /**
     * Process the ACK request.
     */
    public void processAck(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {

    }

    /**
     * Process the invite request.
     */
    public void processInvite(RequestEvent requestEvent,
            ServerTransaction serverTransaction) {

        final Request request = requestEvent.getRequest();
        final SipProvider sipProvider = (SipProvider) requestEvent.getSource();
//        ServerTransaction st = serverTransaction;        
        try {
//        	if (st == null) {
//        		st = sipProvider.getNewServerTransaction(request);
//            }
        	final String toTag = ""+System.nanoTime();
            Response response = messageFactory.createResponse(Response.RINGING,
                    request);            
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag(toTag); // Application is supposed to set.
            sipProvider.sendResponse(response);

            response = messageFactory.createResponse(Response.OK,
                    request);
            final Address address = addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            final ContactHeader contactHeader = headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag(toTag); // Application is supposed to set.
            response.addHeader(contactHeader);
            sipProvider.sendResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            //System.exit(0);
        }
    }


    /**
     * Process the bye request.
     */
    public void processBye(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        final Request request = requestEvent.getRequest();
        try {
            final Response response = messageFactory.createResponse(200, request);
            ((SipProvider)requestEvent.getSource()).sendResponse(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            //System.exit(0);

        }
    }

    public void processCancel(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "shootme");
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
        properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "false");
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");        
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "./shootmedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "./shootmelog.txt");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
        "./shootmedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
        "./shootmelog.txt");
        properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",
        "true");
        properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "4");
        properties.setProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", "65536");
        properties.setProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", "65536");
        //properties.setProperty("gov.nist.javax.sip.RECEIVE_UDP_BUFFER_SIZE", "120000");
        //properties.setProperty("gov.nist.javax.sip.SEND_UDP_BUFFER_SIZE", "120000");

        properties.setProperty("gov.nist.javax.sip.CONGESTION_CONTROL_ENABLED", "false");
        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
        } catch (PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
            e.printStackTrace();
            System.err.println(e.getMessage());
            if (e.getCause() != null)
                e.getCause().printStackTrace();
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    myPort, "udp");

            ShootmeDialogAndTxStateless listener = this;

            SipProvider sipProvider = sipStack.createSipProvider(lp);
            sipProvider.addSipListener(listener);

        } catch (Exception ex) {
            ex.printStackTrace();
            usage();
        }

    }

    public static void main(String args[]) {
        new ShootmeDialogAndTxStateless().init();
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {

    }

}
