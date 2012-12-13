package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
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
import javax.sip.TransactionState;
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
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

public class DialogIdentityTest extends TestCase {
    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private

    class Shootme implements SipListener {


        private SipStack sipStack;

        private static final String myAddress = "127.0.0.1";

        private static final int myPort = 5070;

        protected ServerTransaction inviteTid;

        private Response okResponse;

        private Request inviteRequest;

        private Dialog dialog;

        private boolean setToTagOn180;

        public static final boolean callerSendsBye = true;

        class ByeTimer extends TimerTask {
            Shootme shootme;

            public ByeTimer(Shootme shootme) {
                this.shootme = shootme;

            }

            public void run() {
                shootme.sendBye();
            }

        }

        protected static final String usageString = "java "
                + "examples.shootist.Shootist \n"
                + ">>>> is your class path set to the root?";



        public Shootme(boolean b) {
            this.setToTagOn180 = b;
        }

        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent
                    .getServerTransaction();

            System.out.println("\n\nRequest " + request.getMethod()
                    + " received at " + sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.ACK)) {
                processAck(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.BYE)) {
                processBye(requestEvent, serverTransactionId);
            } else if (request.getMethod().equals(Request.CANCEL)) {
                processCancel(requestEvent, serverTransactionId);
            } else {
                try {
                    serverTransactionId.sendResponse( messageFactory.createResponse( 202, request ) );

                    // send one back
                    SipProvider prov = (SipProvider) requestEvent.getSource();
                    Request refer = requestEvent.getDialog().createRequest("REFER");
                    requestEvent.getDialog().sendRequest( prov.getNewClientTransaction(refer) );

                } catch (SipException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvalidArgumentException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        public void processResponse(ResponseEvent responseEvent) {
        }

        /**
         * Process the ACK request. Send the bye and complete the call flow.
         */
        public void processAck(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            try {
                System.out.println("shootme: got an ACK! ");
                System.out.println("Dialog State = " + dialog.getState());
                SipProvider provider = (SipProvider) requestEvent.getSource();
                if (!callerSendsBye) {
                    Request byeRequest = dialog.createRequest(Request.BYE);
                    ClientTransaction ct = provider
                            .getNewClientTransaction(byeRequest);
                    dialog.sendRequest(ct);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
    		ListIterator li = request.getHeaders("Route");
    		if(li == null || !li.hasNext()) {
    			//logger.info("No route headers in that invite. It must be means for someone else");
    			return;
    		}
    		li.next(); // skip the first Route which is pointing to us here, we need the second
    		if(!li.hasNext()) {
    			//logger.info("No route headers in that invite. It must be means for someone else");
    			return;
    		}
    		RouteHeader route = (RouteHeader) li.next();
    		Shootist shootist = new Shootist();
    		
    		shootist.makeCall("mockserver", "testingSystem", route);
            try {
                System.out.println("shootme: got an Invite sending Trying");
                // System.out.println("shootme: " + request);
                Response response = messageFactory.createResponse(Response.RINGING,
                        request);
                if(setToTagOn180) {
                    ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                    toHeader.setTag("4321"); // Application is supposed to set.
                }
                ServerTransaction st = requestEvent.getServerTransaction();

                if (st == null) {
                    st = sipProvider.getNewServerTransaction(request);
                }
                dialog = st.getDialog();

                st.sendResponse(response);

                this.okResponse = messageFactory.createResponse(Response.OK,
                        request);
                Address address = addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
                toHeader.setTag("4321"); // Application is supposed to set.
                okResponse.addHeader(contactHeader);
                this.inviteTid = st;
                // Defer sending the OK to simulate the phone ringing.
                // Answered in 1 second ( this guy is fast at taking calls)
                this.inviteRequest = request;
                sendInviteOK();
                new Timer().schedule(new ByeTimer(this), 5000);
            } catch (Exception ex) {
                ex.printStackTrace();
                //System.exit(0);
            }
        }

        private void sendInviteOK() {
            try {
                if (inviteTid.getState() != TransactionState.COMPLETED) {
                    System.out.println("shootme: Dialog state before 200: "
                            + inviteTid.getDialog().getState());
                    inviteTid.sendResponse(okResponse);
                    System.out.println("shootme: Dialog state after 200: "
                            + inviteTid.getDialog().getState());
                }
            } catch (SipException ex) {
                ex.printStackTrace();
            } catch (InvalidArgumentException ex) {
                ex.printStackTrace();
            }
        }
        
        private void sendBye() {
            try {
                Request request = dialog.createRequest("BYE");
                SipProvider provider = (SipProvider) sipStack.getSipProviders().next();
                ClientTransaction ct = provider.getNewClientTransaction(request);
                dialog.sendRequest(ct);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        /**
         * Process the bye request.
         */
        public void processBye(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            Dialog dialog = requestEvent.getDialog();
            System.out.println("local party = " + dialog.getLocalParty());
            try {
                System.out.println("shootme:  got a bye sending OK.");
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                System.out.println("Dialog State is "
                        + serverTransactionId.getDialog().getState());

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);

            }
        }

        public void processCancel(RequestEvent requestEvent,
                ServerTransaction serverTransactionId) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            try {
                System.out.println("shootme:  got a cancel.");
                if (serverTransactionId == null) {
                    System.out.println("shootme:  null tid.");
                    return;
                }
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                if (dialog.getState() != DialogState.CONFIRMED) {
                    response = messageFactory.createResponse(
                            Response.REQUEST_TERMINATED, inviteRequest);
                    inviteTid.sendResponse(response);
                }

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

                SipProvider sipProvider = sipStack.createSipProvider(lp);
                System.out.println("udp provider " + sipProvider);
                sipProvider.addSipListener(listener);

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                fail("Unexpected exception");
            }

        }



        public void processIOException(IOExceptionEvent exceptionEvent) {
            System.out.println("IOException");

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
            System.out.println("Dialog terminated event recieved");
            Dialog d = dialogTerminatedEvent.getDialog();
            System.out.println("Local Party = " + d.getLocalParty());

        }

        public void stop() {
            this.sipStack.stop();
        }

    }
    class Shootist implements SipListener {

        private  SipProvider sipProvider;

        private SipStack sipStack;

        private ContactHeader contactHeader;

        private ListeningPoint udpListeningPoint;

        private ClientTransaction inviteTid;

        private Dialog dialog;

        private boolean byeTaskRunning;

        class ByeTask  extends TimerTask {
            Dialog dialog;
            public ByeTask(Dialog dialog)  {
                this.dialog = dialog;
            }
            public void run () {
                try {
                   Request byeRequest = this.dialog.createRequest(Request.BYE);
                   ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
                   dialog.sendRequest(ct);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    fail("Unexpected exception ");
                }

            }

        }



        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            ServerTransaction serverTransactionId = requestReceivedEvent
                    .getServerTransaction();

            System.out.println("\n\nRequest " + request.getMethod()
                    + " received at " + sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            // We are the UAC so the only request we get is the BYE.
            if (request.getMethod().equals(Request.BYE))
                processBye(request, serverTransactionId);
            else {
                try {
                    serverTransactionId.sendResponse( messageFactory.createResponse(202,request) );
                } catch (Exception e) {
                    e.printStackTrace();
                    fail("Unxepcted exception ");
                }
            }

        }

        public void processBye(Request request,
                ServerTransaction serverTransactionId) {
            try {
                System.out.println("shootist:  got a bye .");
                if (serverTransactionId == null) {
                    System.out.println("shootist:  null TID.");
                    return;
                }
                Dialog dialog = serverTransactionId.getDialog();
                System.out.println("Dialog State = " + dialog.getState());
                Response response = messageFactory.createResponse(200, request);
                serverTransactionId.sendResponse(response);
                System.out.println("shootist:  Sending OK.");
                System.out.println("Dialog State = " + dialog.getState());

            } catch (Exception ex) {
                fail("Unexpected exception");

            }
        }

           // Save the created ACK request, to respond to retransmitted 2xx
           private Request ackRequest;

        public void processResponse(ResponseEvent responseReceivedEvent) {
            System.out.println("Got a response");
            Response response = (Response) responseReceivedEvent.getResponse();
            ClientTransaction tid = responseReceivedEvent.getClientTransaction();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

            System.out.println("Response received : Status Code = "
                    + response.getStatusCode() + " " + cseq);


            if (tid == null) {

                // RFC3261: MUST respond to every 2xx
                if (ackRequest!=null && dialog!=null) {
                   System.out.println("re-sending ACK");
                   try {
                      dialog.sendAck(ackRequest);
                   } catch (SipException se) {
                      se.printStackTrace();
                      fail("Unxpected exception ");
                   }
                }
                return;
            }
            // If the caller is supposed to send the bye
            if (!byeTaskRunning) {
                byeTaskRunning = true;
                new Timer().schedule(new ByeTask(dialog), 5000) ;
            }
            System.out.println("transaction state is " + tid.getState());
            System.out.println("Dialog = " + tid.getDialog());
            System.out.println("Dialog State is " + tid.getDialog().getState());

            assertSame("Checking dialog identity",tid.getDialog(), this.dialog);

            try {
                if (response.getStatusCode() == Response.OK) {
                    if (cseq.getMethod().equals(Request.INVITE)) {
                        System.out.println("Dialog after 200 OK  " + dialog);
                        System.out.println("Dialog State after 200 OK  " + dialog.getState());
                        Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                        System.out.println("Sending ACK");
                        dialog.sendAck(ackRequest);

                    } else if (cseq.getMethod().equals(Request.CANCEL)) {
                        if (dialog.getState() == DialogState.CONFIRMED) {
                            // oops cancel went in too late. Need to hang up the
                            // dialog.
                            System.out
                                    .println("Sending BYE -- cancel went in too late !!");
                            Request byeRequest = dialog.createRequest(Request.BYE);
                            ClientTransaction ct = sipProvider
                                    .getNewClientTransaction(byeRequest);
                            dialog.sendRequest(ct);

                        }

                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(0);
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

            System.out.println("Transaction Time out");
        }

        public void sendCancel() {
            try {
                System.out.println("Sending cancel");
                Request cancelRequest = inviteTid.createCancel();
                ClientTransaction cancelTid = sipProvider
                        .getNewClientTransaction(cancelRequest);
                cancelTid.sendRequest();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        public void init() {}

        public void makeCall(String from, String to, RouteHeader route) {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            // If you want to try TCP transport change the following to
            String transport = "udp";
            String peerHostPort = to;
            //properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
            //        + transport);
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
                System.exit(0);
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                udpListeningPoint = sipStack.createListeningPoint("127.0.0.1", 5099, "udp");
                sipProvider = sipStack.createSipProvider(udpListeningPoint);
                Shootist listener = this;
                sipProvider.addSipListener(listener);

                String fromName = from;
                String fromSipAddress = "here.com";
                String fromDisplayName = "The Master Blaster";

                String toSipAddress = "there.com";
                String toUser = to;
                String toDisplayName = "The Little Blister";

                // create >From Header
                SipURI fromAddress = addressFactory.createSipURI(fromName,
                        fromSipAddress);

                Address fromNameAddress = addressFactory.createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = headerFactory.createFromHeader(
                        fromNameAddress, "473722");

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
                
                request.addLast(route);

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
                inviteTid = sipProvider.getNewClientTransaction(request);

                // send the request out.
                inviteTid.sendRequest();

                dialog = inviteTid.getDialog();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                ex.printStackTrace();
                fail("Unxpected exception ");
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

        public void stop() {
        	if(sipStack != null) {
        		this.sipStack.stop();
        	}
        }
    }

    public void testDialogIdentity() throws Exception {

            Shootme shootme = new Shootme(false);
            shootme.init();


            Thread.sleep(10000);

            shootme.stop();
    }
    
    public void testDialogIdentity180HasToTag() throws Exception {

        Shootist shootist = new Shootist();

        Shootme shootme = new Shootme(true);
        shootme.init();

        shootist.init();

        Thread.sleep(10000);

        shootist.stop();
        shootme.stop();
    }
}
