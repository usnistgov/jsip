package examples.tpcc;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.text.ParseException;

/**
 * The Click to dial third party call controller application.
 *
 * @author Kathleen McCallum
 *
 * <pre>
 *   main () -&gt; init()
 *  init()
 *    createSipStack
 *    createInvite() -&gt; First
 *  processResponse()
 *    if (OK) first
 *       createInvite() -&gt; second
 *    else if (OK) second
 *       ack() -&gt; second
 *       ack() -&gt; first
 * </pre>
 *
 *
 *
 */

public class Controller implements SipListener {

    private static SipProvider sipProvider;

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private ContactHeader contactHeader;

    private ListeningPoint udpListeningPoint;

    protected ClientTransaction inviteFirst;

    protected ClientTransaction inviteSecond;

    String Secuencia;

    String transport = "udp";

    protected static final String usageString = "java "
            + "examples.ctd.ctdControll \n"
            + ">>>> is your class path set to the root?";

    ResponseEvent responseFirstEvent;

    String Auser = "AGuy";

    String ASipAddressDomain = "Afirst.com";

    String ADisplayName = "The A first";

    String Buser = "BGuy";

    String BSipAddressDomain = "BSecond.com";

    String BDisplayName = "The B second";

    String peerHostPortA = "127.0.0.1:5070";

    String peerHostPortB = "127.0.0.1:5080";

    int first = 0, second = 0;

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);
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
            System.out.println("Controller:  got a bye .");
            if (serverTransactionId == null) {
                System.out.println("Controller:  null TID.");
                return;
            }

            System.out.println("Create OK para BYE: ");
            // 1: OK BYE
            Response ok = messageFactory.createResponse(Response.OK, request);
            serverTransactionId.sendResponse(ok);

            // 2do: BYE for the other side (send a new clientTransaction)
            System.out.println("Send BYE in new clientTransaction");

            Dialog secondBye = (Dialog) (serverTransactionId.getDialog()
                    .getApplicationData());
            Request requestBye = secondBye.createRequest(Request.BYE);
            ClientTransaction clientTransaction = null;
            clientTransaction = sipProvider.getNewClientTransaction(requestBye);
            secondBye.sendRequest(clientTransaction);

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction tid = responseReceivedEvent.getClientTransaction();
        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        System.out.println("Response received : Status Code = "
                + response.getStatusCode() + " " + cseq);
        if (tid == null) {
            System.out.println("Stray response -- dropping ");
            return;
        }
        System.out.println("transaction state is " + tid.getState());
        System.out.println("Dialog = " + tid.getDialog());
        System.out.println("Dialog State is " + tid.getDialog().getState());

        try {
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    if (Secuencia.equals("first")) {
                        System.out.println("processResponse FIRST");

                        responseFirstEvent = responseReceivedEvent;
                        // get call-id
                        String callId = ((CallIdHeader) response
                                .getHeader(CallIdHeader.NAME)).getCallId();
                        // Create second Invite
                        second++;
                        Secuencia = "second";
                        Request requestSecond = this.createInvite(Secuencia,
                                String.valueOf(second), callId, null,
                                peerHostPortB);
                        // SDP for second Invite with first response
                        // ContentTypeHeader
                        requestSecond.setContent(response.getContent(),
                                (ContentTypeHeader) (response
                                        .getHeader("Content-Type")));

                        inviteSecond = sipProvider
                                .getNewClientTransaction(requestSecond);

                        inviteSecond.sendRequest();
                        System.out.println("INVITE second sent:\n"
                                + requestSecond);

                    } else if (Secuencia.equals("second")) {
                        System.out.println("processResponse SECOND");
                        // send ACK second
                        Dialog dialogSecond = tid.getDialog();

                        Request ackRequest = dialogSecond.createAck(cseq
                                .getSeqNumber());// dialogSecond.createRequest(Request.ACK);
                        System.out.println("Sending ACK second");
                        dialogSecond.sendAck(ackRequest);// dialogSecond.sendAck(ackRequest);

                        CSeqHeader cseqFirst = (CSeqHeader) responseFirstEvent
                                .getResponse().getHeader(CSeqHeader.NAME);
                        Request ackRequestFirst = responseFirstEvent
                                .getDialog().createAck(
                                        cseqFirst.getSeqNumber());
                        ackRequestFirst.setContent(response.getContent(),
                                (ContentTypeHeader) (response
                                        .getHeader("Content-Type")));

                        System.out.println("Sending ACK first");
                        responseFirstEvent.getDialog().sendAck(ackRequestFirst);

                        // save the dialog of the other side, for the bye...
                        responseFirstEvent.getDialog().setApplicationData(
                                dialogSecond);
                        dialogSecond.setApplicationData(responseFirstEvent
                                .getDialog());

                        Secuencia = "fin";
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public Request createInvite(String headerName, String headerValue,
            String callerId, String tagVal, String peerHostPort)
            throws ParseException, InvalidArgumentException {

        String fromSipAddressDomain = "", toSipAddressDomain = "";
        String fromDisplayName = "";
        String toDisplayName = "";
        String fromVal = "", toVal = "";

        if (headerName.equals("first")) {
            fromVal = Auser;
            fromSipAddressDomain = ASipAddressDomain;
            fromDisplayName = ADisplayName;
            toVal = Buser;
            toSipAddressDomain = BSipAddressDomain;
            toDisplayName = BDisplayName;
        } else if (headerName.equals("second")) {
            fromVal = Buser;
            fromSipAddressDomain = BSipAddressDomain;
            fromDisplayName = BDisplayName;
            toVal = Auser;
            toSipAddressDomain = ASipAddressDomain;
            toDisplayName = ADisplayName;
        }
        System.out.println("CreateInvite ");

        // create >From Header
        SipURI fromAddress = addressFactory.createSipURI(fromVal,
                fromSipAddressDomain);
        Address fromNameAddress = addressFactory.createAddress(fromAddress);
        fromNameAddress.setDisplayName(fromDisplayName);
        FromHeader fromHeader = headerFactory.createFromHeader(fromNameAddress,
                "12345");

        // create To Header
        SipURI toAddress = addressFactory.createSipURI(toVal,
                toSipAddressDomain);
        Address toNameAddress = addressFactory.createAddress(toAddress);
        toNameAddress.setDisplayName(toDisplayName);
        ToHeader toHeader = headerFactory.createToHeader(toNameAddress, null);

        // create Request URI
        SipURI requestURI = addressFactory.createSipURI(toVal, peerHostPort);

        // Create ViaHeaders
        ArrayList viaHeaders = new ArrayList();
        ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                sipProvider.getListeningPoint(transport).getPort(), transport,
                null);
        viaHeaders.add(viaHeader);

        // Create a new CallId header
        CallIdHeader callIdHeader = null;
        if (callerId == null) {
            callIdHeader = sipProvider.getNewCallId();
        } else {
            callIdHeader = headerFactory.createCallIdHeader(callerId);
        }

        // Create a new Cseq header
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(Long
                .parseLong(headerValue), Request.INVITE);

        // Create a new MaxForwardsHeader
        MaxForwardsHeader maxForwards = headerFactory
                .createMaxForwardsHeader(70);

        // Create the request.
        Request request = messageFactory.createRequest(requestURI,
                Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader,
                viaHeaders, maxForwards);
        // Create contact headers
        String host = "127.0.0.1";
        SipURI contactUrl = addressFactory.createSipURI(fromVal, host);
        contactUrl.setPort(udpListeningPoint.getPort());

        // Create the contact name address.
        SipURI contactURI = addressFactory.createSipURI(fromVal, host);
        contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());
        Address contactAddress = addressFactory.createAddress(contactURI);

        // Add the contact address.
        contactAddress.setDisplayName(fromVal);
        contactHeader = headerFactory.createContactHeader(contactAddress);
        request.addHeader(contactHeader);

        // Allow header. With PUBLISH, to indicate that we'd like to have an
        // server-sided PA
        String methods = Request.INVITE + ", " + Request.ACK + ", "
                + Request.OPTIONS + ", " + Request.CANCEL + ", " + Request.BYE
                + ", " + Request.INFO + ", " + Request.REFER + ", "
                + Request.MESSAGE + ", " + Request.NOTIFY + ", "
                + Request.SUBSCRIBE;
        AllowHeader allowHeader = headerFactory.createAllowHeader(methods);
        request.addHeader(allowHeader);

        // Add the extension header. To mantain Flow I
        Header extensionHeader = headerFactory.createHeader(headerName,
                headerValue);
        request.addHeader(extensionHeader);

        return request;
    }

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        // This one is optional so I remove it, since I will call 2 parts
        // properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
        // + transport);
        properties.setProperty("javax.sip.STACK_NAME", "controller");

        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "controllerdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "controllerlog.txt");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");

        try {
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);

            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",
                    5050, "udp");
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            Controller listener = this;
            sipProvider.addSipListener(listener);

        } catch (PeerUnavailableException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Creating Listener Points");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        try {
            System.out.println("ProcessCTD ");
            first++;
            this.Secuencia = "first";
            Request request = this.createInvite(Secuencia, String
                    .valueOf(first), "", null, peerHostPortA);
            // Create the client transaction.
            inviteFirst = sipProvider.getNewClientTransaction(request);
            // send the request out.
            inviteFirst.sendRequest();
            System.out.println("INVITE first sent:\n" + request);

        } catch (Exception e) {
            System.out.println("Creating call CreateInvite()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        new Controller().init();
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        System.out.println("Transaction Time out");
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
}
