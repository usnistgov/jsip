package test.tck.msgflow.callflows.refer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

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
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ReferToHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import test.tck.TestHarness;
import test.tck.msgflow.callflows.ProtocolObjects;



/**
 * This example shows an out-of-dialog REFER scenario:
 *
 * referer sends REFER to referee, with Refer-To set to Shootme
 * referee sends INVITE to Shootme, and NOTIFYs to referer about call progress
 *
 * This is the referee
 *
 * @see RFC3515 http://www.ietf.org/rfc/rfc3515.txt
 *
 * @author Jeroen van Bemmel
 * @author Ivelin Ivanov
 *
 */
public class Referee implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    public static final int myPort = 5070;

    protected SipProvider mySipProvider;

    protected Dialog dialog;

    private static Logger logger = Logger.getLogger(Referee.class) ;

    private boolean tryingSent;

    private EventHeader referEvent;

    private String transport;

    static {
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    "logs/refereeoutputlog.txt"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public Referee(ProtocolObjects protObjects) {
        addressFactory = protObjects.addressFactory;
        messageFactory = protObjects.messageFactory;
        headerFactory = protObjects.headerFactory;
        sipStack = protObjects.sipStack;
        transport = protObjects.transport;
    }

    public void processRequest(RequestEvent requestEvent) {

        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId = requestEvent
                .getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + serverTransactionId
                + " and dialog id " + requestEvent.getDialog() );
        logger.info( request.toString() );
        if (request.getMethod().equals(Request.REFER)) {
            try {
                processRefer(requestEvent, serverTransactionId);
            } catch (Exception e) {
                logger.info("Referee failed processing REFER, because of " + e.getMessage(), e);
                TestHarness.fail("Referee failed processing REFER, because of " + e.getMessage());
            }
        } else TestHarness.fail( "Not a REFER request but:" + request.getMethod() );

    }

    /**
     * Process the REFER request.
     * @throws ParseException
     * @throws SipException
     * @throws InvalidArgumentException
     */
    public void processRefer(RequestEvent requestEvent,
        ServerTransaction serverTransaction) throws ParseException, SipException, InvalidArgumentException {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        Request refer = requestEvent.getRequest();

            logger.info("referee: got an REFER sending Accepted");
            logger.info("referee:  " + refer.getMethod() );
            dialog = requestEvent.getDialog();
            logger.info("referee : dialog = " + requestEvent.getDialog());

            // Check that it has a Refer-To, if not bad request
            ReferToHeader refTo = (ReferToHeader) refer.getHeader( ReferToHeader.NAME );
            if (refTo==null) {
                Response bad = messageFactory.createResponse(Response.BAD_REQUEST, refer);
                bad.setReasonPhrase( "Missing Refer-To" );
                sipProvider.sendResponse( bad );
                TestHarness.fail("Bad REFER request. Missing Refer-To.");
            }

            // New test: first time, only send 100 Trying, to test that retransmission
            // continues for non-INVITE requests (using UDP)
            // before(!) creating a ServerTransaction! Else retransmissions are filtered
            if (!tryingSent && "udp".equalsIgnoreCase(transport)) {
                tryingSent = true;
                sipProvider.sendResponse( messageFactory.createResponse(100, refer) );
                return;
            }

            // Always create a ServerTransaction, best as early as possible in the code
            Response response = null;
            ServerTransaction st = requestEvent.getServerTransaction();
            if (st == null) {
                st = sipProvider.getNewServerTransaction(refer);
            }

            // Check if it is an initial SUBSCRIBE or a refresh / unsubscribe
            String toTag = Integer.toHexString( (int) (Math.random() * Integer.MAX_VALUE) );
            response = messageFactory.createResponse(202, refer);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);

            // Sanity check: to header should not have a tag. Else the dialog
            // should have matched
            TestHarness.assertNull("To-tag!=null but no dialog match! My dialog=" + dialog, toHeader.getTag());
            toHeader.setTag(toTag); // Application is supposed to set.

            this.dialog = st.getDialog();
            // REFER dialogs do not terminate on bye.
            this.dialog.terminateOnBye(false);
            if (dialog != null) {
                logger.info("Dialog " + dialog);
                logger.info("Dialog state " + dialog.getState());
                logger.info( "local tag=" + dialog.getLocalTag() );
                logger.info( "remote tag=" + dialog.getRemoteTag() );
            }

            // Both 2xx response to SUBSCRIBE and NOTIFY need a Contact
            Address address = addressFactory.createAddress("Referee <sip:127.0.0.1>");
            ((SipURI)address.getURI()).setPort( mySipProvider.getListeningPoint(transport).getPort() );
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);

            // Expires header is mandatory in 2xx responses to REFER
            ExpiresHeader expires = (ExpiresHeader) refer.getHeader( ExpiresHeader.NAME );
            if (expires==null) {
                expires = headerFactory.createExpiresHeader(30);// rather short
            }
            response.addHeader( expires );

            /*
             * The REFER MUST be answered first.
             */
            TestHarness.assertNull( dialog.getState() );
            st.sendResponse(response);
            TestHarness.assertEquals( DialogState.CONFIRMED, dialog.getState() );

            // NOTIFY MUST have "refer" event, possibly with id
            referEvent = headerFactory.createEventHeader("refer");

            // Not necessary, but allowed: id == cseq of REFER
            long id = ((CSeqHeader) refer.getHeader("CSeq")).getSeqNumber();
            referEvent.setEventId( Long.toString(id) );

            // JvB: do this after receiving 100 response
            // sendNotify( Response.TRYING, "Trying" );

            // Then call the refer-to
            sendInvite( refTo );
        }

        private void sendNotify( int code, String reason )
            throws SipException, ParseException
        {
            /*
             * NOTIFY requests MUST contain a "Subscription-State" header with a
             * value of "active", "pending", or "terminated". The "active" value
             * indicates that the subscription has been accepted and has been
             * authorized (in most cases; see section 5.2.). The "pending" value
             * indicates that the subscription has been received, but that
             * policy information is insufficient to accept or deny the
             * subscription at this time. The "terminated" value indicates that
             * the subscription is not active.
             */

            Request notifyRequest = dialog.createRequest( "NOTIFY" );

            // Initial state is pending, second time we assume terminated (Expires==0)
            String state = SubscriptionStateHeader.PENDING;
            if (code>100 && code<200) {
                state = SubscriptionStateHeader.ACTIVE;
            } else if (code>=200) {
                state = SubscriptionStateHeader.TERMINATED;
            }

            SubscriptionStateHeader sstate = headerFactory.createSubscriptionStateHeader( state );
            if (state == SubscriptionStateHeader.TERMINATED) {
                sstate.setReasonCode("noresource");
            }
            notifyRequest.addHeader(sstate);
            notifyRequest.setHeader(referEvent);

            Address address = addressFactory.createAddress("Referee <sip:127.0.0.1>");
            ((SipURI)address.getURI()).setPort( mySipProvider.getListeningPoint(transport).getPort() );
            ((SipURI)address.getURI()).setTransportParam(transport);
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            notifyRequest.setHeader(contactHeader);
            // notifyRequest.setHeader(routeHeader);
            ClientTransaction ct2 = mySipProvider.getNewClientTransaction(notifyRequest);

            ContentTypeHeader ct = headerFactory.createContentTypeHeader("message","sipfrag");
            ct.setParameter( "version", "2.0" );

            notifyRequest.setContent( "SIP/2.0 " + code + ' ' + reason, ct );

            // Let the other side know that the tx is pending acceptance
            //
            dialog.sendRequest(ct2);
            logger.info("NOTIFY Branch ID " +
                ((ViaHeader)notifyRequest.getHeader(ViaHeader.NAME)).getParameter("branch"));
            logger.info("Dialog " + dialog);
            logger.info("Dialog state after NOTIFY: " + dialog.getState());
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        logger.info("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        if(tid != null) {
	        logger.info("Response received with client transaction id "
	                + tid + ":\n" + response.getStatusCode() +
	                " cseq = " + response.getHeader(CSeqHeader.NAME) + 
	                " dialog " + tid.getDialog());
        } else {
        	logger.info("Response received with client transaction id "
	                + tid + ":\n" + response.getStatusCode() +
	                " cseq = " + response.getHeader(CSeqHeader.NAME) + 
	                " dialog " + responseReceivedEvent.getDialog());
        }
        
        // Filter retransmissions for slow machines
        if(tid == null) return;

        CSeqHeader cseq = (CSeqHeader) response.getHeader( CSeqHeader.NAME );
        if (cseq.getMethod().equals(Request.INVITE)) {

            try {
                sendNotify( response.getStatusCode(), response.getReasonPhrase() );
            } catch (Exception e1) {
                TestHarness.fail("Failed to send notify, because of " + e1.getMessage());
            }

            if (response.getStatusCode() == 200 ) {
                try {
                    Request ack = tid.getDialog().createAck( cseq.getSeqNumber() );
                    tid.getDialog().sendAck( ack );

                    // kill it right away
                    if ( tid.getDialog().getState() != DialogState.TERMINATED ) {
                    	Request bye = tid.getDialog()
								.createRequest(Request.BYE);
						tid.getDialog().sendRequest(
								mySipProvider.getNewClientTransaction(bye));
                    }
                } catch (Exception e) {
                	logger.error("Caught exception",e);
                    TestHarness.fail("Failed to send BYE request, because of " + e.getMessage());
                }
            }
        }


    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        Transaction transaction;
        if (timeoutEvent.isServerTransaction()) {
            transaction = timeoutEvent.getServerTransaction();
        } else {
            transaction = timeoutEvent.getClientTransaction();
        }
        logger.info("state = " + transaction.getState());
        logger.info("dialog = " + transaction.getDialog());
        logger.info("dialogState = "
                + transaction.getDialog().getState());
        logger.info("Transaction Time out");

        TestHarness.fail( "Transaction timeout" );
    }

    public void sendInvite( ReferToHeader to ) {

        try {

            String fromName = "Referee";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            // create >From Header
            SipURI fromAddress = addressFactory.createSipURI(fromName,
                    fromSipAddress);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = headerFactory.createFromHeader(
                    fromNameAddress, "12345");

            // create To Header
            ToHeader toHeader = headerFactory.createToHeader( to.getAddress(),
                    null);

            // get Request URI
            SipURI requestURI = (SipURI) to.getAddress().getURI();

            ListeningPoint lp = mySipProvider.getListeningPoint(transport);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    lp.getPort(), transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = mySipProvider.getNewCallId();
            // JvB: Make sure that the implementation matches the messagefactory
            callIdHeader = headerFactory.createCallIdHeader( callIdHeader.getCallId() );


            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request. (TODO should read request type from Refer-To)
            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = lp.getIPAddress();

            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setPort(lp.getPort());
            contactURI.setTransportParam( transport );

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Create the client transaction.
            ClientTransaction inviteTid = mySipProvider.getNewClientTransaction(request);

            logger.info("Invite Dialog = " + inviteTid.getDialog());

            // send the request out.
            inviteTid.sendRequest();

        } catch (Throwable ex) {
            TestHarness.fail("Failed to send INVITE, because of " + ex);
        }
    }



    public SipProvider createProvider() throws Exception {
        ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                myPort, transport);

        this.mySipProvider = sipStack.createSipProvider(lp);
        logger.info("provider " + mySipProvider);

        return mySipProvider;
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error( "processIOEx:" + exceptionEvent );
        TestHarness.fail("unexpected event");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent tte) {

        logger.info("transaction terminated:" + tte );
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {

        logger.info("dialog terminated:" + dialogTerminatedEvent );
    }

}
