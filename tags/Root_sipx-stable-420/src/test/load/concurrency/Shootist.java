package test.load.concurrency;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
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

/**
 * This class is a UAC template.
 *
 * @author M. Ranganathan
 */

public class Shootist extends TestCase implements SipListener {

    static AddressFactory addressFactory;

    static MessageFactory messageFactory;

    static HeaderFactory headerFactory;

    static SipStack sipStack;

    ContactHeader contactHeader;

    SipProvider[] sipProviders;

    static String transport;

    // Starts at -2000, a ramp-up period is required for performance testing.
    int byeCount = -2000;

    int ackCount;

    long start;

    static int MAXCONCURRENTINVITE = 200;
    static int NDIALOGS = 1000;
    static int NBPROVIDERS = 1;

    AtomicInteger nbConcurrentInvite = new AtomicInteger(0);

    // Keeps track of successful dialog completion.
    private static Timer timer;

    static {
        timer = new Timer();
    }

    class TTask extends TimerTask {
        Dialog dialog;

        public TTask(Dialog dialog) {
            this.dialog = dialog;
        }

        public void run() {
            if (dialog.getState() != DialogState.TERMINATED) {
                System.out.println("BYE not received for " + this.dialog);
                System.out.println("State " + this.dialog.getState());
                System.out.println("dialogId " + this.dialog.getDialogId());
                Appdata appData = (Appdata) dialog.getApplicationData();

                System.out.println("ackCount " + appData.ackCount);
                ((gov.nist.javax.sip.stack.SIPDialog) dialog).printDebugInfo();
                System.exit(0);
            } else {
                this.dialog = null;
            }
        }
    }

    class Appdata {

        protected TTask ttask;

        protected long startTime;

        protected long endTime;

        protected int ackCount;

        Appdata(Dialog dialog) {
            ttask = new TTask(dialog);
            timer.schedule(ttask, 20 * 1000 * NDIALOGS / 100);
            startTime = System.currentTimeMillis();
        }

        public void cancelTimer() {
            this.ttask.dialog = null;
            this.ttask.cancel();
            endTime = System.currentTimeMillis();
        }
    }

    class TestTimer extends TimerTask {

        private Shootist shootist;

        public TestTimer(Shootist shootist) {
            this.shootist = shootist;
        }

        public void run() {
            assertTrue("Missed BYE " + shootist.byeCount,
                    shootist.byeCount >= NDIALOGS);
            ProtocolObjects.destroy();
            System.exit(0);
        }

    }

    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        System.exit(0);

    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);

    }

    public void processBye(Request request,
            ServerTransaction serverTransactionId) {
        try {
            if (serverTransactionId == null) {
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            Response response = messageFactory.createResponse(200, request);
            serverTransactionId.sendResponse(response);

            Appdata appdata = (Appdata) dialog.getApplicationData();
            appdata.cancelTimer();
            int ndialogs = nbConcurrentInvite.decrementAndGet();
            // System.out.println(nbConcurrentInvite);
            if ( ndialogs > MAXCONCURRENTINVITE ) System.out.println("Concurrent invites = " + ndialogs);
            synchronized( this) {
                if ( ndialogs < MAXCONCURRENTINVITE/2 ) this.notify();
            }
            // Synchronization necessary for Multiprocessor machine
            // noted by Matt Porter.
            this.byeCount++;
            // System.out.println("bye count = " + byeCount);
            if (byeCount == NDIALOGS) {
                long current = System.currentTimeMillis();
                float sec = (float) (current - start) / 1000f;
                System.out.println("Thrupt = " + (float) (NDIALOGS / sec));
            }

            // dialog.delete();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        if (tid == null) {
            return;
        }

        try {
            if (response.getStatusCode() == Response.OK
                    && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                            .getMethod().equals(Request.INVITE)) {
                // Request cancel = inviteTid.createCancel();
                // ClientTransaction ct =
                // sipProvider.getNewClientTransaction(cancel);
                // ct.sendRequest();
                Dialog dialog = tid.getDialog();
                if (dialog.getState() != DialogState.TERMINATED) {
                    CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                    Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                    dialog.sendAck(ackRequest);

                    Appdata appData = (Appdata) dialog.getApplicationData();
                    if (appData != null)
                        appData.ackCount++;
                }

            }
        } catch (Exception ex) {
            Dialog dialog = tid.getDialog();
            System.out.println("Dialog state is " + dialog.getState());
            ex.printStackTrace();
            System.exit(0);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        System.out.println("Transaction Time out");
        Request request = null;
        Transaction transaction = null;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
            request = ((ServerTransaction) transaction).getRequest();
        } else {
            transaction = timeoutEvent.getClientTransaction();
            request = ((ClientTransaction) transaction).getRequest();
        }
        System.out.println("state = " + transaction.getState());
        System.out.println("dialog = " + transaction.getDialog());
        System.out.println("dialogState = "
                + transaction.getDialog().getState());
        System.out.println("Transaction Time out");
        System.out.println("Transaction " + transaction);
        System.out.println("request " + request);

        fail("Unexpected event: TimeoutEvent ");

    }

    public void sendInvite() {
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

            String tag = new Integer((int) (Math.random() * 10000)).toString();
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, tag);

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
            SipProvider sipProvider = getNextProvider();
            int port = sipProvider.getListeningPoint(transport).getPort();
            ViaHeader viaHeader = headerFactory.createViaHeader(sipProvider
                    .getListeningPoint(transport).getIPAddress(), port,
                    transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

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
            String host = sipProvider.getListeningPoint(transport)
                    .getIPAddress();

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(port);
            contactURI.setTransportParam(transport);

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Add the extension header.
            Header extensionHeader = headerFactory.createHeader("My-Header",
                    "my header value");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader("Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            SipURI myUri = addressFactory.createSipURI(null, host);
            myUri.setLrParam();
            myUri.setTransportParam(transport);
            myUri.setPort(5070);
            Address address = addressFactory.createAddress(null, myUri);
            RouteHeader routeHeader = headerFactory.createRouteHeader(address);
            request.setHeader(routeHeader);

            // Create the client transaction.
            ClientTransaction inviteTid = sipProvider
                    .getNewClientTransaction(request);

            Dialog dialog = inviteTid.getDialog();

            // Set a pointer to our application data
            Appdata appdata = new Appdata(dialog);
            dialog.setApplicationData(appdata);
            // send the request out.
            inviteTid.sendRequest();

            nbConcurrentInvite.incrementAndGet();
                // System.out.println(nbConcurrentInvite);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    int currentProvider = 0;

    private SipProvider getNextProvider() {
        synchronized (this) {
            currentProvider++;
            if (currentProvider >= NBPROVIDERS) {
                currentProvider = 0;
            }
            return sipProviders[currentProvider];
        }
    }

    public void createProvider(SipListener listener) throws Exception {
        sipProviders = new SipProvider[NBPROVIDERS];
        for (int i = 0; i < NBPROVIDERS; i++) {
            ListeningPoint listeningPoint = sipStack.createListeningPoint(
                    "127.0.0.1", 15060 + i, transport);
            sipProviders[i] = sipStack.createSipProvider(listeningPoint);
            sipProviders[i].addSipListener(listener);
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException occured while retransmitting requests:"
                + exceptionEvent);
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("Transaction Terminated event: " +
        // transactionTerminatedEvent);
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // System.out.println("Dialog Terminated event: " +
        // dialogTerminatedEvent);
    }

    public Shootist() {
        TimerTask testTimer = new TestTimer(this);
        Shootist.timer.schedule(testTimer, 20000 * NDIALOGS / 100);

    }

    public static int getNdialogs() {
        return NDIALOGS;
    }

    public static void main(String args[]) throws Exception {
        ProtocolObjects.init("shootist", true);
        NDIALOGS = Integer.parseInt(args[0]);
        Shootist.addressFactory = ProtocolObjects.addressFactory;
        Shootist.messageFactory = ProtocolObjects.messageFactory;
        Shootist.headerFactory = ProtocolObjects.headerFactory;
        Shootist.sipStack = ProtocolObjects.sipStack;
        Shootist.transport = ProtocolObjects.transport;
        final Shootist shootist = new Shootist();
        shootist.createProvider(shootist);

        shootist.start = System.currentTimeMillis();
        while (shootist.byeCount < NDIALOGS) {

                while (shootist.nbConcurrentInvite.intValue() >= MAXCONCURRENTINVITE) {
                    System.out.println("Waiting for max invite count to go down!");
                    synchronized(shootist) {
                     try {
                        shootist.wait();
                     } catch (Exception ex) {
                     }
                    }
                }

            if (shootist.byeCount == 0) {
                shootist.start = System.currentTimeMillis();
            }

            if (transport.equalsIgnoreCase("udp")) {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }

            shootist.sendInvite();
        }
    }
}
