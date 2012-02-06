package test.unit.gov.nist.javax.sip.stack.forkedinvitedialogtimeout;

import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.ResponseExt;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.helpers.NullEnumeration;


/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 *
 * @author M. Ranganathan
 */

public class Shootist implements SipListenerExt {

    private ContactHeader contactHeader;

    private ClientTransaction inviteTid;


    private SipProvider sipProvider;

    private String host = "127.0.0.1";

    private int port;

    private String peerHost = "127.0.0.1";

    private int peerPort;

    private ListeningPoint listeningPoint;

    private static String unexpectedException = "Unexpected exception ";

    private static Logger logger = Logger.getLogger(Shootist.class);

    static {
        if (logger.getAllAppenders().equals(NullEnumeration.getInstance())) {

            logger.addAppender(new ConsoleAppender(new SimpleLayout()));

        }
    }
    
    private Dialog originalDialog;
    
    private Dialog earlyDialog;

    private HashSet<Dialog> forkedDialogs = new HashSet<Dialog>();
    
    private HashSet<Dialog> forkedEarlyDialogs = new HashSet<Dialog>();

    private Dialog ackedDialog;

    private SipStack sipStack;

    private HashSet<Dialog> timedOutDialog = new HashSet<Dialog>();
    
  
    private boolean byeResponseSeen;

    private int counter;

    private static HeaderFactory headerFactory;

    private static MessageFactory messageFactory;

    private static AddressFactory addressFactory;

    private static final String transport = "udp";

    static boolean callerSendsBye  = true;

    private boolean byeSent;

    private Timer timer = new Timer();

    private ClientTransaction originalTransaction;
    
    boolean isAutomaticDialogSupportEnabled = true;

    private boolean createDialogAfterRequest = false;

    private boolean terminatedDialogWasOneOfCancelled;

    private boolean forkFirst;

    private boolean timeoutEventSeen;

    private HashSet<Dialog> dialogTerminatedEvents = new HashSet<Dialog>();       

    class SendBye extends TimerTask {

        private Dialog dialog;
        public SendBye(Dialog dialog ) {
            this.dialog = dialog;
        }
        @Override
        public void run() {
           try {
               TestCase.assertEquals ("Dialog state must be confirmed",
                        DialogState.CONFIRMED,dialog.getState());

               Request byeRequest = dialog.createRequest(Request.BYE);
               ClientTransaction ctx = sipProvider.getNewClientTransaction(byeRequest);
               dialog.sendRequest(ctx);
           } catch (Exception ex) {
               TestCase.fail("Unexpected exception");
           }

        }
    }

    public Shootist(int myPort, int proxyPort, String createDialogAuto, boolean forkFirst) {
        this.port = myPort;
        this.peerPort = proxyPort;
        this.forkFirst = forkFirst;
        
        SipObjects sipObjects = new SipObjects(myPort, "shootist", createDialogAuto);
        addressFactory = sipObjects.addressFactory;
        messageFactory = sipObjects.messageFactory;
        headerFactory = sipObjects.headerFactory;
        this.sipStack = sipObjects.sipStack;
        
        if(!createDialogAuto.equalsIgnoreCase("on")) {
            isAutomaticDialogSupportEnabled = false;
        }
    }   

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod() + " received at "
                +  sipStack.getStackName()
                + " with server transaction id " + serverTransactionId);

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);
        else
            TestCase.fail("Unexpected request ! : " + request);

    }

    public void processBye(Request request,
            ServerTransaction serverTransactionId) {
        try {
            logger.info("shootist:  got a bye .");
            if (serverTransactionId == null) {
                logger.info("shootist:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            logger.info("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse(
                    200, request);
            serverTransactionId.sendResponse(response);
            logger.info("shootist:  Sending OK.");
            logger.info("Dialog State = " + dialog.getState());

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);

        }
    }

    public synchronized void processResponse(ResponseEvent responseReceived) {
        try {            
            ResponseEventExt responseReceivedEvent = (ResponseEventExt) responseReceived;
            Response response = (Response) responseReceivedEvent.getResponse();
            ClientTransaction tid = responseReceivedEvent.getClientTransaction();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
    
            logger.info("Response received : Status Code = "
                    + response.getStatusCode() + " " + cseq);
            logger.info("Response = " + response + " class=" + response.getClass() );            
    
            Dialog dialog = responseReceivedEvent.getDialog();     
            this.forkedDialogs.add(dialog);
            if(createDialogAfterRequest) {
                TestCase.assertNull( dialog );
                return;
            } else {
                TestCase.assertNotNull( dialog );
            }
            System.out.println("original Tx " + responseReceivedEvent.getOriginalTransaction());            
            if (tid != null)
                logger.info("transaction state is " + tid.getState());
            else
                logger.info("transaction = " + tid);
    
            logger.info("Dialog = " + dialog);
            logger.info("Dialog state is " + dialog.getState());

            String toTag = ((ResponseExt)response).getToHeader().getTag();
            boolean isFromFork = false;
            if(forkFirst) {
                isFromFork = toTag != null && !toTag.contains("shootme-5080");
            } else {
                isFromFork = toTag != null && !toTag.contains("shootme-5081");
            }
            logger.info("isRetransmission = " + responseReceivedEvent.isRetransmission() + " isFromForked = " + isFromFork + " isForked = " + responseReceivedEvent.isForkedResponse() + " response "+ response);
            
            if (response.getStatusCode() == Response.OK) {
                if (cseq.getMethod().equals(Request.INVITE)) {
                    // Proxy will fork. I will accept the first dialog.

                    if (ackedDialog == null) {
                        TestCase.assertFalse("retransmission flag should be false", responseReceivedEvent
                                .isRetransmission());
                        TestCase.assertTrue("Dialog state should be CONFIRMED",
                                dialog.getState() == DialogState.CONFIRMED);

                        TestCase.assertTrue("Identity check" , this.ackedDialog == null || this.ackedDialog == dialog);
                        this.ackedDialog = dialog;

                        Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                        dialog.sendAck(ackRequest);
                        this.timer.schedule( new SendBye(dialog),500);
                      } else {
                        TestCase.assertTrue("retransmission flag should be true", responseReceivedEvent
                                .isRetransmission());
                        if (timedOutDialog.isEmpty()) {
                            this.timedOutDialog.add(dialog);
                            // Did not send ACK here so should get DialogTmeoutEvent
                        }
                    }

                } else if (cseq.getMethod().equals(Request.BYE)) {
                    TestCase.assertFalse("retransmission flag should be false", responseReceivedEvent
                            .isRetransmission());
                    if (dialog == this.ackedDialog) {
                        this.byeResponseSeen = true;
                    }
                } else {
                    logger.info("Response method = " + cseq.getMethod());
                }
            } else if ( response.getStatusCode() == Response.RINGING ) {
                TestCase.assertEquals( DialogState.EARLY, dialog.getState() );     
                this.forkedEarlyDialogs.add(dialog);
                // Proxy will fork. This make sure the forked early dialog is not the same as the first one.
                if(isFromFork) {
                    logger.info("forked early dialog= " + dialog + " for response " + response);
                    ResponseEventExt responseEventExt  = (ResponseEventExt) responseReceivedEvent;
                    TestCase.assertTrue("forked event must set flag",responseEventExt.isForkedResponse());
                    TestCase.assertNotSame(dialog, earlyDialog);                        
                    TestCase.assertSame(earlyDialog, responseEventExt.getOriginalTransaction().getDefaultDialog());
                } else {
                    earlyDialog = dialog;
                    logger.info("original early dialog= " + dialog + " for response " + response);
                    ResponseEventExt responseEventExt  = (ResponseEventExt) responseReceivedEvent;
                    TestCase.assertFalse("non forked event must not set flag",responseEventExt.isForkedResponse());
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
            // System.exit(0);
        }

    }

    public SipProvider createSipProvider() {
        try {
            listeningPoint = sipStack.createListeningPoint(
                    host, port, "udp");

            logger.info("listening point = " + host + " port = " + port);
            logger.info("listening point = " + listeningPoint);
            sipProvider = sipStack
                    .createSipProvider(listeningPoint);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            TestCase.fail(unexpectedException);
            return null;
        }

    }

    public void checkStateForDialogTimeoutEvent() {
        TestCase.assertEquals("Should see " + this.counter + " distinct early dialogs",
                        counter, this.forkedEarlyDialogs.size());
        TestCase.assertEquals("Should see " + this.counter + " distinct dialogs",
                2,this.forkedDialogs.size());
        if(!createDialogAfterRequest) {
            TestCase.assertTrue(
                "Should see the original (default) dialog in the forked set",
                this.forkedDialogs.contains(this.originalDialog));
        } 
        TestCase.assertTrue("Timeout Event must be seen", this.timeoutEventSeen); 
        TestCase.assertEquals("Need ONE dialog terminated events", this.dialogTerminatedEvents.size(), 1);
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

        logger.info("Transaction Time out");
    }

    public void sendInvite(int forkCount) {
        try {

            this.counter = forkCount;

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(
                    fromName, fromSipAddress);

            Address fromNameAddress = addressFactory
                    .createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory
                    .createFromHeader(fromNameAddress, "12345");

            // create To Header
            SipURI toAddress = addressFactory.createSipURI(
                    toUser, toSipAddress);
            Address toNameAddress = addressFactory
                    .createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader = headerFactory.createToHeader(
                    toNameAddress, null);

            // create Request URI
            String peerHostPort = peerHost + ":" + peerPort;
            SipURI requestURI = addressFactory.createSipURI(
                    toUser, peerHostPort);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory
                    .createViaHeader(host, sipProvider.getListeningPoint(
                            transport).getPort(),
                            transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            SipURI sipuri = addressFactory.createSipURI(null,
                    host);
            sipuri.setPort(peerPort);
            sipuri.setLrParam();

            RouteHeader routeHeader = headerFactory
                    .createRouteHeader(addressFactory
                            .createAddress(sipuri));

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = headerFactory
                    .createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            // JvB: Make sure that the implementation matches the messagefactory
            callIdHeader = headerFactory
                    .createCallIdHeader(callIdHeader.getCallId());

            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory
                    .createCSeqHeader(1L, Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(
                    requestURI, Request.INVITE, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwards);
            // Create contact headers

            SipURI contactUrl = addressFactory.createSipURI(
                    fromName, host);
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(
                    fromName, host);
            contactURI.setPort(sipProvider.getListeningPoint(
                    transport).getPort());
            contactURI.setTransportParam(transport);

            Address contactAddress = addressFactory
                    .createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory
                    .createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Dont use the Outbound Proxy. Use Lr instead.
            request.setHeader(routeHeader);

            // Add the extension header.
            Header extensionHeader = headerFactory
                    .createHeader("My-Header", "my header value");
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

            extensionHeader = headerFactory.createHeader(
                    "My-Other-Header", "my new header value ");
            request.addHeader(extensionHeader);

            Header callInfoHeader = headerFactory.createHeader(
                    "Call-Info", "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);

            // Create the client transaction.
            inviteTid = sipProvider.getNewClientTransaction(request);
            this.originalTransaction = inviteTid;
            Dialog dialog = null;
            if(isAutomaticDialogSupportEnabled) {
                dialog = inviteTid.getDialog();
                TestCase.assertTrue("Initial dialog state should be null",
                                dialog.getState() == null);
            } else {
                if(!createDialogAfterRequest) {
                    dialog = sipProvider.getNewDialog(inviteTid);
                    TestCase.assertTrue("Initial dialog state should be null",
                                    dialog.getState() == null);
                }                
            }           
            
            this.originalDialog = dialog;
            // send the request out.
            inviteTid.sendRequest();

            

        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            TestCase.fail(unexpectedException);

        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("IOException happened for " + exceptionEvent.getHost()
                + " port = " + exceptionEvent.getPort());
        TestCase.fail("Unexpected exception");

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event recieved");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("dialog Id " + dialogTerminatedEvent.getDialog().getDialogId());
        TestCase.assertTrue("Only want one DTE per dialog",!this.dialogTerminatedEvents.contains(dialogTerminatedEvent.getDialog()));
        this.dialogTerminatedEvents.add(dialogTerminatedEvent.getDialog());
        if(this.timedOutDialog.contains((Dialog)dialogTerminatedEvent.getDialog() )) {
            terminatedDialogWasOneOfCancelled= true;
        }

    }

    public void stop() {
      this.sipStack.stop();
    }

    /**
     * @param createDialogAfterRequest the createDialogAfterRequest to set
     */
    public void setCreateDialogAfterRequest(boolean createDialogAfterRequest) {
        this.createDialogAfterRequest = createDialogAfterRequest;
    }

    /**
     * @return the createDialogAfterRequest
     */
    public boolean isCreateDialogAfterRequest() {
        return createDialogAfterRequest;
    }
    
    class DialogCreationDelayTask extends TimerTask {       

        @Override
        public void run() {
            try {
                originalDialog = sipProvider.getNewDialog(inviteTid);
                TestCase.fail("Should not be able to create Dialog after request sent");
            } catch (SipException e) {
                logger.info("Got the expected SIP Exception");
            }
        }
    }

    public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {

        logger.info("Got a DialogTimeoutEvent " + timeoutEvent.getDialog());
        this.timeoutEventSeen = true;
        if (timedOutDialog.size() > 0 ) {
        TestCase.assertTrue ("Should find timeout dialog in cancelSet", 
                this.timedOutDialog.contains(timeoutEvent.getDialog()));
        } else {
            TestCase.assertTrue ("Should find timeout dialog in cancelSet", 
                    this.forkedEarlyDialogs.contains(timeoutEvent.getDialog()));
       
        }
    }
}
