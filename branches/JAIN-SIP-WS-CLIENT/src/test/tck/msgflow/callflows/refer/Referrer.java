package test.tck.msgflow.callflows.refer;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
 * @author Jeroen van Bemmel
 * @author Ivelin Ivanov
 */

public class Referrer implements SipListener {

    private SipProvider sipProvider;

    private AddressFactory addressFactory;

    private MessageFactory messageFactory;

    private HeaderFactory headerFactory;

    private SipStack sipStack;

    private ContactHeader contactHeader;

    private String transport;

    public static final int myPort = 5080;

    public int count;//< Number of NOTIFYs

    private static Logger logger = Logger.getLogger(Referrer.class);

    static {
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    "logs/refereroutputlog.txt"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private ClientTransaction subscribeTid;

    private ListeningPoint listeningPoint;

    public Referrer(ProtocolObjects protObjects) {
        addressFactory = protObjects.addressFactory;
        messageFactory = protObjects.messageFactory;
        headerFactory = protObjects.headerFactory;
        sipStack = protObjects.sipStack;
        transport = protObjects.transport;
    }

    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId = requestReceivedEvent
                .getServerTransaction();
        String viaBranch = ((ViaHeader)(request.getHeaders(ViaHeader.NAME).next())).getParameter("branch");

        logger.info("\n\nRequest " + request.getMethod() + " received at "
                + sipStack.getStackName() + " with server transaction id "
                + serverTransactionId +
                " branch ID = " + viaBranch);
        //logger.info( request );
        if (request.getMethod().equals(Request.NOTIFY)) {
            processNotify(requestReceivedEvent, serverTransactionId);
        } else if ( request.getMethod().equals(Request.INVITE)) {
            processInvite( requestReceivedEvent );
        } else if ( request.getMethod().equals(Request.ACK)) {
            processAck( requestReceivedEvent );
        } else if ( request.getMethod().equals(Request.BYE)) {
            processBye( requestReceivedEvent );
        } else {
            TestHarness.fail( "Unexpected request type:" + request.getMethod() );
        }

    }

    private void processNotify(RequestEvent requestEvent,
            ServerTransaction serverTransactionId) {
        SipProvider provider = (SipProvider) requestEvent.getSource();
        Request notify = requestEvent.getRequest();
        if ( notify.getMethod().equals("NOTIFY") ) try {
            logger.info("referer:  got a NOTIFY count  " + ++this.count + ":\n" + notify );
            if (serverTransactionId == null) {
                logger.info("referer:  null TID.");
                serverTransactionId = provider.getNewServerTransaction(notify);
            }
            Dialog dialog = serverTransactionId.getDialog();
            logger.info("Dialog = " + dialog);

            TestHarness.assertTrue("Dialog should not be null", dialog != null);
            logger.info("Dialog State = " + dialog.getState());

            Response response = messageFactory.createResponse(200, notify);
            // SHOULD add a Contact
            ContactHeader contact = (ContactHeader) contactHeader.clone();
            ((SipURI)contact.getAddress().getURI()).setParameter( "id", "sub" );
            response.addHeader( contact );
            logger.info("Transaction State = " + serverTransactionId.getState());
            serverTransactionId.sendResponse(response);
            logger.info("Dialog State = " + dialog.getState());
            SubscriptionStateHeader subscriptionState = (SubscriptionStateHeader) notify
                    .getHeader(SubscriptionStateHeader.NAME);

            // Subscription is terminated?
            String state = subscriptionState.getState();
            if (state.equalsIgnoreCase(SubscriptionStateHeader.TERMINATED)) {
                dialog.delete();
            } else {
                logger.info("Referer: state now " + state);
            }
        } catch (Exception ex) {
            TestHarness.fail("Failed processing notify, because of " + ex);

        } else {
            TestHarness.fail( "Unexpected request type" );
        }
    }

    private void processInvite( RequestEvent re )
    {
        SipProvider provider = (SipProvider) re.getSource();
        ServerTransaction st = re.getServerTransaction();
        try {
            if (st==null) st = provider.getNewServerTransaction( re.getRequest() );
            Response r = messageFactory.createResponse( 100 , re.getRequest());
            st.sendResponse( r );
            r = messageFactory.createResponse( 180 , re.getRequest());
            r.addHeader( (ContactHeader) contactHeader.clone() );
            ((ToHeader) r.getHeader("To")).setTag( "inv_res" );
            st.sendResponse( r );
            Thread.sleep( 500 );
            r = messageFactory.createResponse( 200, re.getRequest() );
            r.addHeader( (ContactHeader) contactHeader.clone() );
            ((ToHeader) r.getHeader("To")).setTag( "inv_res" );
            st.sendResponse( r );
        } catch (Throwable t) {
            t.printStackTrace();
            TestHarness.fail( "Throwable:" + t.getLocalizedMessage() );
        }
    }

    private void processAck( RequestEvent re )
    {
        // ignore it, Referee sends BYE right after
    }

    private void processBye( RequestEvent re )
    {
        try {
            re.getServerTransaction().sendResponse(
                    messageFactory.createResponse(200, re.getRequest()));
        } catch (Throwable t) {
            t.printStackTrace();
            TestHarness.fail( "Throwable:" + t.getLocalizedMessage() );
        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();

        logger.info("Got a response:" + response.getStatusCode()
                + ':' + response.getHeader( CSeqHeader.NAME ) );

        logger.info("Response received with client transaction id " + tid
                + ": " + response.getStatusCode()  );
        if (tid == null) {
            logger.info("Stray response -- dropping ");
            return;
        }
        logger.info("transaction state is " + tid.getState());
        logger.info("Dialog = " + tid.getDialog());
        if ( tid.getDialog () != null )
            logger.info("Dialog State is " + tid.getDialog().getState());

    }

    public SipProvider createProvider() throws Exception {

        this.listeningPoint = sipStack.createListeningPoint("127.0.0.1", myPort,
                transport);
        sipProvider = sipStack.createSipProvider(listeningPoint);
        return sipProvider;

    }

    public void sendRefer() {

        try {

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "127.0.0.1";
            String toUser = "referee";
            String toDisplayName = "Referee";

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
            requestURI.setPort( Referee.myPort );// referee
            requestURI.setTransportParam(transport);

            // Create ViaHeaders

            ArrayList viaHeaders = new ArrayList();
            int port = sipProvider.getListeningPoint(transport).getPort();
            ViaHeader viaHeader = headerFactory.createViaHeader("127.0.0.1",
                    port, transport, null);

            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();
            // JvB: Make sure that the implementation matches the messagefactory
            callIdHeader = headerFactory.createCallIdHeader( callIdHeader.getCallId() );


            // Create a new Cseq header
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                    Request.REFER);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            Request request = messageFactory.createRequest(requestURI,
                    Request.REFER, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            // Create contact headers
            String host = listeningPoint.getIPAddress();

            SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            contactUrl.setPort(listeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            contactURI.setTransportParam(transport);
            contactURI.setPort(sipProvider.getListeningPoint(transport).getPort());

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Create the client transaction.
            subscribeTid = sipProvider.getNewClientTransaction(request);

            // REFER has an implicit "refer" event
            // Create an event header for the subscription.
            // EventHeader eventHeader = headerFactory.createEventHeader("foo");
            // eventHeader.setEventId("foo");
            // request.addHeader(eventHeader);

            // Make the INVITE come back to this listener!
            ReferToHeader referTo = headerFactory.createReferToHeader(
                    addressFactory.createAddress( "<sip:127.0.0.1:" + myPort + ";transport=" + transport + ">" )
            );
            request.addHeader( referTo );

            logger.info("Refer Dialog = " + subscribeTid.getDialog());

            // send the request out.
            subscribeTid.sendRequest();
        } catch (Throwable ex) {
            TestHarness.fail("Referrer failed sending Subscribe request, because of " + ex);
        }
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("io exception event received");
        TestHarness.fail("IOException unexpected");
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent tte) {
        logger.info("transaction terminated:" + tte );

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("dialog terminated event received");
    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        logger.info("Transaction Time out");
    }
}
