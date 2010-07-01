package test.load.leakcheck.busy;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.util.*;

/**
 * This class is a UAC template.
 * @author M. Ranganathan
 */

public class Shootist implements SipListener {

    private static SipProvider tcpProvider;

    private static SipProvider udpProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private int reInviteCount;

    private ContactHeader contactHeader;

    private ListeningPoint tcpListeningPoint;

    private ListeningPoint udpListeningPoint;

    private int counter;

    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    private void shutDown() {
        try {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.out.println("nulling reference");
            sipStack.deleteListeningPoint(tcpListeningPoint);
            sipStack.deleteListeningPoint(udpListeningPoint);
            // This will close down the stack and exit all threads
            tcpProvider.removeSipListener(this);
            udpProvider.removeSipListener(this);
            while (true) {
                try {
                    sipStack.deleteSipProvider(udpProvider);
                    sipStack.deleteSipProvider(tcpProvider);
                    break;
                } catch (ObjectInUseException ex) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        continue;
                    }
                }
            }
            sipStack = null;
            tcpProvider = null;
            udpProvider = null;
            this.contactHeader = null;
            addressFactory = null;
            headerFactory = null;
            messageFactory = null;
            this.udpListeningPoint = null;
            this.tcpListeningPoint = null;
            this.reInviteCount = 0;
            System.gc();
            //Redo this from the start.
            if (counter < 10)
                this.init();
            else
                counter++;
        } catch (Exception ex) {
            ex.printStackTrace();
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

            this.shutDown();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        System.out.println("Response received with client transaction id "
                + tid + ":\n" + response.getStatusCode());
        if (tid == null) {
            System.out.println("Stray response -- dropping ");
            return;
        }
        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        System.out.println("Dialog State is " + tid.getDialog().getState());

        try {
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {
                // Request cancel = inviteTid.createCancel();
                // ClientTransaction ct =
                //  sipProvider.getNewClientTransaction(cancel);
                // ct.sendRequest();
                Dialog dialog = tid.getDialog();
                CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                System.out.println("Sending ACK");
                dialog.sendAck(ackRequest);

                // Send a Re INVITE but this time force it
                // to use UDP as the transport. Else, it will
                // Use whatever transport was used to create
                // the dialog.
                if (reInviteCount == 0) {
                    Request inviteRequest = dialog
                            .createRequest(Request.INVITE);
                    ((SipURI) inviteRequest.getRequestURI())
                            .removeParameter("transport");
                    ((ViaHeader) inviteRequest.getHeader(ViaHeader.NAME))
                            .setTransport("udp");
                    inviteRequest.addHeader(contactHeader);
                    try {
                        Thread.sleep(100);
                    } catch (Exception ex) {
                    }
                    ClientTransaction ct = udpProvider
                            .getNewClientTransaction(inviteRequest);
                    dialog.sendRequest(ct);
                    reInviteCount++;
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

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // If you want to try TCP transport change the following to
        String transport = "udp";
        String peerHostPort = "127.0.0.1:5070";
        properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
        properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
                + transport);
        properties.setProperty("javax.sip.STACK_NAME", "shootist");
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "on");

        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties
                .setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "shootistdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "shootistlog.txt");

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");
        // Set to 0 in your production code for max speed.
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");

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
            udpListeningPoint = sipStack.createListeningPoint(sipStack.getIPAddress(), 5060, "udp");
            udpProvider = sipStack.createSipProvider(udpListeningPoint);
            Shootist listener = this;
            udpProvider.addSipListener(listener);

            tcpListeningPoint = sipStack.createListeningPoint(sipStack.getIPAddress(), 5060, "tcp");
            tcpProvider = sipStack.createSipProvider(tcpListeningPoint);
            tcpProvider.addSipListener(listener);

            SipProvider sipProvider = transport.equalsIgnoreCase("udp") ? udpProvider
                    : tcpProvider;

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // send the request out.
            for (int i = 0; i < 100; i++) {
                // create >From Header
                SipURI fromAddress = addressFactory.createSipURI(fromName,
                        fromSipAddress);

                Address fromNameAddress = addressFactory
                        .createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = headerFactory.createFromHeader(
                        fromNameAddress, "12345" + i);

                // create To Header
                SipURI toAddress = addressFactory.createSipURI(toUser,
                        toSipAddress);
                Address toNameAddress = addressFactory.createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                        null);

                // create Request URI
                SipURI requestURI = addressFactory.createSipURI(toUser,
                        peerHostPort);

                // Create ViaHeaders

                ArrayList viaHeaders = new ArrayList();
                int port = sipProvider.getListeningPoint(transport).getPort();
                ViaHeader viaHeader = headerFactory.createViaHeader(sipStack
                        .getIPAddress(), port, transport, null);

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
                String host = sipStack.getIPAddress();

                SipURI contactUrl = addressFactory.createSipURI(fromName, host);
                contactUrl.setPort(tcpListeningPoint.getPort());

                // Create the contact name address.
                SipURI contactURI = addressFactory.createSipURI(fromName, host);
                contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

                Address contactAddress = addressFactory
                        .createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = headerFactory
                        .createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                // Add the extension header.
                Header extensionHeader = headerFactory.createHeader(
                        "My-Header", "my header value");
                request.addHeader(extensionHeader);

                String sdpData = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n"
                        + "s=mysession session\r\n" + "p=+46 8 52018010\r\n"
                        + "c=IN IP4  129.6.55.78\r\n" + "t=0 0\r\n"
                        + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n"
                        + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
                byte[] contents = sdpData.getBytes();

                request.setContent(contents, contentTypeHeader);

                extensionHeader = headerFactory.createHeader("My-Other-Header",
                        "my new header value ");
                request.addHeader(extensionHeader);

                Header callInfoHeader = headerFactory.createHeader("Call-Info",
                        "<http://www.antd.nist.gov>");
                request.addHeader(callInfoHeader);

                // Create the client transaction.
                ClientTransaction inviteTid = sipProvider
                        .getNewClientTransaction(request);
                inviteTid.sendRequest();
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException occured while retransmitting requests:" + exceptionEvent);
    }
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction Terminated event: " + transactionTerminatedEvent );
    }
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog Terminated event: " + dialogTerminatedEvent);
    }

    public static void main(String args[]) {
        new Shootist().init();

    }
}
