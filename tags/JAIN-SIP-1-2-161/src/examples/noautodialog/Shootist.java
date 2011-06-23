package examples.noautodialog;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Shootist implements SipListener {

    private static SipProvider tcpProvider;

    private static SipProvider udpProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;


    private ContactHeader contactHeader;

    private ListeningPoint tcpListeningPoint;

    private ListeningPoint udpListeningPoint;


    private static String PEER_ADDRESS = Shootme.myAddress;

    // To run on two machines change these to suit.
    public static final String myAddress = "127.0.0.1";

    private static final int myPort = 5060;

    protected ClientTransaction inviteTid;

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

            sipStack.stop();
            tcpProvider = null;
            udpProvider = null;
            this.inviteTid = null;
            this.contactHeader = null;
            addressFactory = null;
            headerFactory = null;
            messageFactory = null;
            this.udpListeningPoint = null;
            this.tcpListeningPoint = null;
            this.init();

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

        if (request.getMethod().equals(Request.BYE))
            processBye((SipProvider) requestReceivedEvent.getSource(), request, serverTransactionId);
        else if (request.getMethod().equals(Request.INVITE))
            processInvite(request, serverTransactionId);

    }

    public void processInvite(Request request, ServerTransaction st) {
        try {
            Response response = messageFactory.createResponse(Response.OK,
                    request);
            ((ToHeader) response.getHeader(ToHeader.NAME))
                    .setTag(((ToHeader) request.getHeader(ToHeader.NAME))
                            .getTag());

            Address address = addressFactory.createAddress("Shootme <sip:"
                    + myAddress + ":" + myPort + ">");
            ContactHeader contactHeader = headerFactory
                    .createContactHeader(address);
            response.addHeader(contactHeader);
            st.sendResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void sendBye(Dialog dialog) {
        try {

            Request bye = dialog.createRequest(Request.BYE);
            SipProvider provider = udpProvider;
            ClientTransaction ct = provider.getNewClientTransaction(bye);
            dialog.sendRequest(ct);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void processBye(SipProvider sipProvider, Request request,
            ServerTransaction serverTransactionId) {
        try {
            System.out.println("shootist:  got a bye .");
            if (serverTransactionId == null) {
                System.out.println("shootist:  null TID.");
                sipProvider.sendResponse(messageFactory.createResponse(200,request));
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
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

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
                Dialog dialog = tid.getDialog();
                Request ackRequest = dialog.createAck( cseq.getSeqNumber() );
                System.out.println("Sending ACK");
                dialog.sendAck(ackRequest);
                try {
                    Thread.sleep(100);
                } catch (Exception ex) {
                }
                this.sendBye(dialog);
            } else if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.BYE)) {
                this.shutDown();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        System.out.println("Transaction Time out");
        System.out.println("TimeoutEvent " + timeoutEvent.getTimeout());
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // If you want to try TCP transport change the following to
        String transport = "udp";
        String peerHostPort = PEER_ADDRESS + ":5070";

        // Turn off automatic dialog support.
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
        properties.setProperty("javax.sip.STACK_NAME", "shootist");
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
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

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
            udpListeningPoint = sipStack.createListeningPoint(myAddress,myPort, "udp");
            udpProvider = sipStack.createSipProvider(udpListeningPoint);
            Shootist listener = this;
            udpProvider.addSipListener(listener);

            tcpListeningPoint = sipStack.createListeningPoint(myAddress,myPort, "tcp");
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
            ViaHeader viaHeader = headerFactory.createViaHeader(myAddress, sipProvider.getListeningPoint("udp").getPort(),
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
            String host = myAddress;

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(tcpListeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint("udp").getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

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

            request.setContent(sdpData, contentTypeHeader);

            extensionHeader = headerFactory.createHeader("My-Other-Header",
                    "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            listener.inviteTid = sipProvider.getNewClientTransaction(request);
            // Need to create a dialog if you need one.
            Dialog d = sipProvider.getNewDialog(inviteTid);

            System.out.println("Created a dialog " + d);

            // send the request out.
            listener.inviteTid.sendRequest();

            d = inviteTid.getDialog();
            System.out.println("dialog = " + d);


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) {
        new Shootist().init();

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
     */
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IO Exception Event!");

    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
     */
    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {

        System.out.println("Transaction Terminated Event!");
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
     */
    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog Terminated Event!");

    }
}
