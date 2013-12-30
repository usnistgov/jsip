package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

public class DeliverUnsolicitedNotifyTest extends TestCase implements
        SipListener {



    private boolean notifySeen = false;

    private boolean notifyResponseSeen = false;

    private MessageFactory messageFactory;

    private SipProvider sipProvider;

    private AddressFactory addressFactory;

    private HeaderFactory headerFactory;

    private ListeningPoint listeningPoint;

    private int port;

    private String transport;

    private SipStack sipStack;

    private static Timer timer = new Timer();

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub

    }

    public void processRequest(RequestEvent requestEvent) {
        try {
            if (requestEvent.getRequest().getMethod().equals(Request.NOTIFY)) {
                this.notifySeen = true;
            }
            Response response = this.messageFactory.createResponse(Response.OK,
                    requestEvent.getRequest());
            ServerTransaction st = requestEvent.getServerTransaction();
            if (st == null) {
                st = sipProvider.getNewServerTransaction(requestEvent
                        .getRequest());
            }
            st.sendResponse(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception ");
        }
    }

    public void processResponse(ResponseEvent responseEvent) {
        this.notifyResponseSeen = true;

    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        // TODO Auto-generated method stub

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    public void setUp() {
        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.resetFactory();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "mystack");

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.

        properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "1");

        properties.setProperty("gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY",
                "true");
        if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
        	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
        }
        try {
            this.port = 6050;
            this.transport = "udp";
            this.sipStack = sipFactory.createSipStack(properties);
            this.listeningPoint = sipStack.createListeningPoint("127.0.0.1",
                    port, transport);
            sipProvider = sipStack.createSipProvider(listeningPoint);
            this.addressFactory = sipFactory.createAddressFactory();
            this.headerFactory = sipFactory.createHeaderFactory();
            // Create the request.
            this.messageFactory = sipFactory.createMessageFactory();
            sipProvider.addSipListener(this);

            timer.schedule(new TimerTask() {

                public void run() {
                    if (!notifySeen || !notifyResponseSeen) {
                        fail("Did not see expected event");
                    }
                    sipStack.stop();

                }

            }, 4000);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Could not create sip stack");
        }

    }

    public void testSendRequest() {
        try {
            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = addressFactory
                    .createSipURI(toUser, toSipAddress);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                    null);

            // create Request URI
            SipURI requestURI = addressFactory.createSipURI(toUser,
                    toSipAddress);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();

            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    port, transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            // JvB: Make sure that the implementation matches the messagefactory
            callIdHeader = headerFactory.createCallIdHeader(callIdHeader
                    .getCallId());

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.NOTIFY);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            Request request = messageFactory.createRequest(requestURI,
                    Request.NOTIFY, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = listeningPoint.getIPAddress();

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setTransportParam(transport);
            contactURI.setPort(sipProvider.getListeningPoint(transport)
                    .getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = headerFactory
                    .createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // JvB: To test forked SUBSCRIBEs, send it via the Forker
            // Note: BIG Gotcha: Need to do this before creating the
            // ClientTransaction!

            RouteHeader route = headerFactory.createRouteHeader(addressFactory
                    .createAddress("<sip:127.0.0.1:" + port + ";transport="
                            + transport + ";lr>"));
            request.addHeader(route);
            // JvB end added

            // Create an event header for the subscription.
            EventHeader eventHeader = headerFactory.createEventHeader("foo");
            eventHeader.setEventId("foo");
            request.addHeader(eventHeader);

            SubscriptionStateHeader ssh = headerFactory
                    .createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
            // Create the client transaction.
            request.addHeader(ssh);
            ClientTransaction ct = sipProvider.getNewClientTransaction(request);

            ct.sendRequest();
            Thread.sleep(10000);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception sending request");
        }
    }

}
