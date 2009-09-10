package examples.refer;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.text.ParseException;
import java.util.*;

import org.apache.log4j.*;



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
 */
public class Referee implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;


    private int port;

    protected SipProvider udpProvider;

    protected Dialog dialog;

    private static Logger logger = Logger.getLogger(Referee.class) ;

    private EventHeader referEvent;

    protected static final String usageString = "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        logger.info(usageString);
        System.exit(0);

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
                e.printStackTrace();
            }
        }

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

            System.out.println("referee: got an REFER sending Accepted");
            System.out.println("referee:  " + refer.getMethod() );
            System.out.println("referee : dialog = " + requestEvent.getDialog());

            // Check that it has a Refer-To, if not bad request
            ReferToHeader refTo = (ReferToHeader) refer.getHeader( ReferToHeader.NAME );
            if (refTo==null) {
                Response bad = messageFactory.createResponse(Response.BAD_REQUEST, refer);
                bad.setReasonPhrase( "Missing Refer-To" );
                sipProvider.sendResponse( bad );
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
            if (toHeader.getTag()!=null) {
                System.err.println( "####ERROR: To-tag!=null but no dialog match! My dialog=" + dialog.getState() );
            }
            toHeader.setTag(toTag); // Application is supposed to set.

            this.dialog = st.getDialog();
            // REFER dialogs do not terminate on bye.
            this.dialog.terminateOnBye(false);
            if (dialog != null) {
                System.out.println("Dialog " + dialog);
                System.out.println("Dialog state " + dialog.getState());
                System.out.println( "local tag=" + dialog.getLocalTag() );
                System.out.println( "remote tag=" + dialog.getRemoteTag() );
            }

            // Both 2xx response to SUBSCRIBE and NOTIFY need a Contact
            Address address = addressFactory.createAddress("Referee <sip:127.0.0.1>");
            ((SipURI)address.getURI()).setPort( udpProvider.getListeningPoint("udp").getPort() );
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);

            // Expires header is mandatory in 2xx responses to REFER
            ExpiresHeader expires = (ExpiresHeader) refer.getHeader( ExpiresHeader.NAME );
            if (expires==null) {
                expires = headerFactory.createExpiresHeader(30);// rather short
            }
            response.addHeader( expires );

            /*
             * JvB: The REFER MUST be answered first.
             */
            st.sendResponse(response);

            // NOTIFY MUST have "refer" event, possibly with id
            referEvent = headerFactory.createEventHeader("refer");

            // Not necessary, but allowed: id == cseq of REFER
            long id = ((CSeqHeader) refer.getHeader("CSeq")).getSeqNumber();
            referEvent.setEventId( Long.toString(id) );

            sendNotify( Response.TRYING, "Trying" );

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
            ((SipURI)address.getURI()).setPort( udpProvider.getListeningPoint("udp").getPort() );
            ContactHeader contactHeader = headerFactory.createContactHeader(address);
            notifyRequest.setHeader(contactHeader);
            // notifyRequest.setHeader(routeHeader);
            ClientTransaction ct2 = udpProvider.getNewClientTransaction(notifyRequest);

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

        System.out.println("Response received with client transaction id "
                + tid + ":\n" + response );

        CSeqHeader cseq = (CSeqHeader) response.getHeader( CSeqHeader.NAME );
        if (cseq.getMethod().equals(Request.INVITE)) {

            try {
                sendNotify( response.getStatusCode(), response.getReasonPhrase() );
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                try {
                    Request ack = tid.getDialog().createAck( cseq.getSeqNumber() );
                    tid.getDialog().sendAck( ack );

                    // kill it right away
                    Request bye = tid.getDialog().createRequest( Request.BYE );
                    tid.getDialog().sendRequest( udpProvider.getNewClientTransaction(bye) );
                } catch (Exception e) {
                    e.printStackTrace();
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

            // Get transport
            String transport = requestURI.getTransportParam();
            if (transport==null) transport = "udp";

            ListeningPoint lp = udpProvider.getListeningPoint(transport);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    lp.getPort(), transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = udpProvider.getNewCallId();

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
            ClientTransaction inviteTid = udpProvider.getNewClientTransaction(request);

            System.out.println("Invite Dialog = " + inviteTid.getDialog());

            // send the request out.
            inviteTid.sendRequest();

        } catch (Throwable ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    private static void initFactories () throws Exception {

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        logger.addAppender(new FileAppender
            ( new SimpleLayout(),"refereeoutputlog.txt" ));

        properties.setProperty("javax.sip.STACK_NAME", "referee" );
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        // JvB note: debug level may impact order of messages!
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "refereedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "refereelog.txt");


        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            logger.info("sipStack = " + sipStack);
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
        } catch  (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void createProvider() {

        try {

            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
                    this.port, "udp");

            this.udpProvider = sipStack.createSipProvider(lp);
            logger.info("udp provider " + udpProvider);

        } catch (Exception ex) {
            logger.info(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public Referee( int port ) {
        this.port = port;
    }


    public static void main(String args[]) throws Exception {
        initFactories();
        Referee notifier = new Referee( 5065 );
        notifier.createProvider( );
        notifier.udpProvider.addSipListener(notifier);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println( "processIOEx:" + exceptionEvent );
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
