package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.SipProviderExt;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.MessageChannel;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPMessageValve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

/**
 * Test for SIP_MESSAGE_VALVE callback
 * 
 * @author Vladimir Ralev <vralev@redhat.com>
 *
 */
public class SIPMessageValveTest extends TestCase {

    public class Shootme implements SipListener {

        private  AddressFactory addressFactory;

        private  MessageFactory messageFactory;

        private  HeaderFactory headerFactory;

        private SipStack sipStack;

        private SipProvider sipProvider;

        private static final String myAddress = "127.0.0.1";

        private static final int myPort = 5070;



        private DialogExt dialog;

        public static final boolean callerSendsBye = true;




        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent
                    .getServerTransaction();

            System.out.println("\n\nRequest " + request.getMethod()
                    + " received at " + sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } else if(request.getMethod().equals(Request.ACK)) {
                processAck(requestEvent, serverTransactionId);
            }

        }

        private int num = 0;

        public void processResponse(ResponseEvent responseEvent) {
            num++;
            if(num<5) {
                try {
                    System.out.println("shootme: got an OK response! ");
                    System.out.println("Dialog State = " + dialog.getState());
                    SipProvider provider = (SipProvider) responseEvent.getSource();

                    Request messageRequest = dialog.createRequest(Request.MESSAGE);
                    CSeqHeader cseq = (CSeqHeader)messageRequest.getHeader(CSeqHeader.NAME);

                    // We will test if the CSEq validation is off by sending CSeq 1 again
                    cseq.setSeqNumber(1);
                    ClientTransaction ct = provider
                    .getNewClientTransaction(messageRequest);
                    dialog.sendRequest(ct);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (num == 5){
                try {
                    System.out.println("shootme: got an OK response! ");
                    System.out.println("Dialog State = " + dialog.getState());
                    SipProvider provider = (SipProvider) responseEvent.getSource();

                    Request messageRequest = dialog.createRequest(Request.BYE);

                    ClientTransaction ct = provider
                    .getNewClientTransaction(messageRequest);
                    dialog.sendRequest(ct);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if(responseEvent.getResponse().getStatusCode() == 500) {
                fail("We received some error. It should not happen with loose dialog validation. We should not receive error on cseq out of order");
            }
        }

        int acks = 0;
        /**
         * Process the ACK request. Send the bye and complete the call flow.
         */
        public void processAck(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            acks++;
            // We will wait for 5 acks to test if retransmissions are filtered. With loose dialog
            // validation the ACK retransmissions are not filtered by the stack.
            if(acks == 5)
            {
                try {
                    System.out.println("shootme: got an ACK! ");
                    System.out.println("Dialog State = " + dialog.getState());
                    SipProvider provider = (SipProvider) requestEvent.getSource();

                    Request messageRequest = dialog.createRequest(Request.MESSAGE);
                    CSeqHeader cseq = (CSeqHeader)messageRequest.getHeader(CSeqHeader.NAME);

                    // We will test if the CSEq validation is off by sending CSeq 1 again

                    ClientTransaction ct = provider
                    .getNewClientTransaction(messageRequest);
                    cseq.setSeqNumber(1);
                    ct.sendRequest();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
        
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
  
            Request request = requestEvent.getRequest();
            try {
                serverTransaction = sipProvider.getNewServerTransaction(request);
                dialog = (DialogExt) sipProvider.getNewDialog(serverTransaction);
                dialog.disableSequenceNumberValidation();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            try {


                Response okResponse = messageFactory.createResponse(Response.OK,
                        request);
                FromHeader from = (FromHeader) okResponse.getHeader(FromHeader.NAME);
                from.removeParameter("tag");
                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
                toHeader.setTag("4321"); // Application is supposed to set.

                FromHeader fromHeader = (FromHeader)okResponse.getHeader(FromHeader.NAME);
                fromHeader.setTag("12345");
                okResponse.addHeader(contactHeader);
                serverTransaction.sendResponse(okResponse);


            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }
        }






        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            Transaction transaction;
            if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
            } else {
                transaction = timeoutEvent.getClientTransaction();
            }
            System.out.println("state = " + transaction.getState());
            System.out.println("dialog = " + transaction.getDialog());
            System.out.println("dialogState = "
                    + transaction.getDialog().getState());
            System.out.println("Transaction Time out");
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
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "shootmedebug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "shootmelog.txt");
            properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
            properties.setProperty("gov.nist.javax.sip.SIP_MESSAGE_VALVE", SIPMessageValveImpl.class.getCanonicalName());
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
                System.out.println("sipStack = " + sipStack);
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

                Shootme listener = this;

                sipProvider = sipStack.createSipProvider(lp);
                System.out.println("udp provider " + sipProvider);
                sipProvider.addSipListener(listener);

            } catch (Exception ex) {
                ex.printStackTrace();
                fail("Unexpected exception");
            }

        }



        public void processIOException(IOExceptionEvent exceptionEvent) {
            fail("IOException");

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            if (transactionTerminatedEvent.isServerTransaction())
                System.out.println("Transaction terminated event recieved"
                        + transactionTerminatedEvent.getServerTransaction());
            else
                System.out.println("Transaction terminated "
                        + transactionTerminatedEvent.getClientTransaction());

        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            Dialog d = dialogTerminatedEvent.getDialog();
            System.out.println("Local Party = " + d.getLocalParty());

        }

        public void terminate() {
            this.sipStack.stop();
        }

    }



    public class Shootist implements SipListener {

        private  SipProvider sipProvider;

        private AddressFactory addressFactory;

        private MessageFactory messageFactory;

        private  HeaderFactory headerFactory;

        private SipStack sipStack;

        private ContactHeader contactHeader;

        private ListeningPoint udpListeningPoint;


        private Dialog dialog;


        private boolean timeoutRecieved;

        boolean messageSeen = false;





        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            if(request.getMethod().equalsIgnoreCase("message")) {
                messageSeen = true;
            }
            try {
                Response response = messageFactory.createResponse(200, request);
                requestReceivedEvent.getServerTransaction().sendResponse(response);
            } catch (Exception e) {
                e.printStackTrace();fail("Error");
            }


        }

        public int lastResponseCode;
        
        public void processResponse(ResponseEvent responseReceivedEvent) {
        	lastResponseCode = responseReceivedEvent.getResponse().getStatusCode();
            if ( responseReceivedEvent.getResponse().getStatusCode() != 603) {

                fail("Expected 603 here from the valve. Got " + responseReceivedEvent.getResponse());
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

            System.out.println("Got a timeout " + timeoutEvent.getClientTransaction());

            this.timeoutRecieved = true;
        }



        public void init() {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            // If you want to try TCP transport change the following to
            String transport = "udp";
            String peerHostPort = "127.0.0.1:5070";
            properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                    + transport);
            // If you want to use UDP then uncomment this.
            properties.setProperty("javax.sip.STACK_NAME", "shootist");

            // The following properties are specific to nist-sip
            // and are not necessarily part of any other jain-sip
            // implementation.
            // You can set a max message size for tcp transport to
            // guard against denial of service attack.
            properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                    "shootistdebug.txt");
            properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                    "shootistlog.txt");

            // Drop the client connection after we are done with the transaction.
            properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                    "false");
            // Set to 0 (or NONE) in your production code for max speed.
            // You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug + traces.
            // Your code will limp at 32 but it is best for debugging.
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
            properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING","false");
            properties.setProperty("gov.nist.javax.sip.SIP_MESSAGE_VALVE", SIPMessageValveImpl.class.getCanonicalName());
            if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            }
            try {
                // Create SipStack object
                sipStack = sipFactory.createSipStack(properties);
                System.out.println("createSipStack " + sipStack);
            } catch (PeerUnavailableException e) {
                // could not find
                // gov.nist.jain.protocol.ip.sip.SipStackImpl
                // in the classpath
                e.printStackTrace();
                System.err.println(e.getMessage());
                fail("Problem with setup");
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                udpListeningPoint = sipStack.createListeningPoint("127.0.0.1", 5060, "udp");
                sipProvider = sipStack.createSipProvider(udpListeningPoint);
                Shootist listener = this;
                sipProvider.addSipListener(listener);

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
                        peerHostPort);

                // Create ViaHeaders

                ArrayList viaHeaders = new ArrayList();
                String ipAddress = udpListeningPoint.getIPAddress();
                ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                        sipProvider.getListeningPoint(transport).getPort(),
                        transport, null);

                // add via headers
                viaHeaders.add(viaHeader);

                // Create ContentTypeHeader
                ContentTypeHeader contentTypeHeader = headerFactory
                        .createContentTypeHeader("application", "sdp");

                // Create a new CallId header
                CallIdHeader callIdHeader = sipProvider.getNewCallId();

                // Create a new Cseq header
                CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                        Request.INVITE);

                // Create a new MaxForwardsHeader
                MaxForwardsHeader maxForwards = headerFactory
                        .createMaxForwardsHeader(70);

                // Create the request.
                Request request = messageFactory.createRequest(requestURI,
                        Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                        toHeader, viaHeaders, maxForwards);
                // Create contact headers
                String host = "127.0.0.1";

                SipURI contactUrl = addressFactory.createSipURI(fromName, host);
                contactUrl.setPort(udpListeningPoint.getPort());
                contactUrl.setLrParam();

                // Create the contact name address.
                SipURI contactURI = addressFactory.createSipURI(fromName, host);
                contactURI.setPort(sipProvider.getListeningPoint(transport)
                        .getPort());

                Address contactAddress = addressFactory.createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = headerFactory.createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                // You can add extension headers of your own making
                // to the outgoing SIP request.
                // Add the extension header.
                Header extensionHeader = headerFactory.createHeader("My-Header",
                        "my header value");
                request.addHeader(extensionHeader);

                String sdpData = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                        + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                        + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
                byte[] contents = sdpData.getBytes();

                request.setContent(contents, contentTypeHeader);
                // You can add as many extension headers as you
                // want.

                extensionHeader = headerFactory.createHeader("My-Other-Header",
                        "my new header value ");
                request.addHeader(extensionHeader);

                Header callInfoHeader = headerFactory.createHeader("Call-Info",
                        "<http://www.antd.nist.gov>");
                request.addHeader(callInfoHeader);

                // Create the client transaction.
                ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);
            	Dialog d = null;
				try {
					d = sipProvider.getNewDialog(inviteTid);
				} catch (SipException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

                // send the request out.
                inviteTid.sendRequest();

                dialog = inviteTid.getDialog();

            } catch (Exception ex) {
                fail("cannot create or send initial invite");
            }
        }



        public void processIOException(IOExceptionEvent exceptionEvent) {
            System.out.println("IOException happened for "
                    + exceptionEvent.getHost() + " port = "
                    + exceptionEvent.getPort());

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            System.out.println("Transaction terminated event recieved");
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            System.out.println("dialogTerminatedEvent");

        }
        public void terminate() {
            this.sipStack.stop();
        }
    }

    private test.unit.gov.nist.javax.sip.stack.SIPMessageValveTest.Shootme shootme;
    private test.unit.gov.nist.javax.sip.stack.SIPMessageValveTest.Shootist shootist;

    public void setUp() {
        this.shootme = new Shootme();
        this.shootist = new Shootist();


    }
    public void tearDown() {
        shootist.terminate();
        shootme.terminate();
    }

    public void testSipMessageValve() {
        this.shootme.init();
        this.shootist.init();
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {

        }
        if(this.shootist.lastResponseCode != 603) fail("We expected 603");
        if(SIPMessageValveImpl.lastResponseCode != 603) fail("We expected 603");
        assertTrue(SIPMessageValveImpl.inited);
    }


}
