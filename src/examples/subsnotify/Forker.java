package examples.subsnotify;

import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import java.text.ParseException;

import java.util.*;

/**
 * This implements a simple forking proxy to test proper handling of multiple
 * NOTIFYs. An initial SUBSCRIBE request (i.e. without to-tag) is forked two
 * times to the same destination. Each response should have a different to-tag;
 * this proxy only passes the first 2xx response through, and discards the
 * second
 *
 * NOTIFYs should go directly to the Contact announced in the SUBSCRIBE, hence
 * this proxy won't see them
 *
 * @author Jeroen van Bemmel
 */

public class Forker implements SipListener {

    private static AddressFactory addressFactory;

    private static MessageFactory messageFactory;

    private static HeaderFactory headerFactory;

    private static SipStack sipStack;

    private SipProvider udpProvider;

    /**
     * Flag to test UAC behavior for non-RFC3261 proxies. In particular, they dont
     * set the 'lr' flag and perform strict routing, ie replace the request URI
     * with the topmost Route header
     */
    private static boolean nonRFC3261Proxy;

    private static Logger logger = Logger.getLogger(Forker.class);
    static {
        try {
            logger.setLevel(Level.INFO);
            logger.addAppender(new ConsoleAppender(new SimpleLayout()));
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    "forkeroutputlog.txt"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static final String usageString = "java "
            + "examples.subsnotify.Forker \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        logger.info(usageString);
        System.exit(0);

    }

    /**
     * Adds a suitable Record-Route header to the given request or response
     *
     * @param r
     * @throws ParseException
     * @throws SipException
     * @throws
     */
    private void recordRoute( Message m, String uniqueId ) throws ParseException, SipException {
        Address me = addressFactory.createAddress( "<sip:127.0.0.1:5065;id=" + uniqueId + '>' );
        if (!nonRFC3261Proxy) ((SipURI) me.getURI()).setLrParam();
        RecordRouteHeader rr = headerFactory.createRecordRouteHeader(me);
        m.addFirst( rr );
    }

    public void processRequest(RequestEvent re) {
        Request request = re.getRequest();
        ServerTransaction st = re.getServerTransaction();

        logger.info("\n\nRequest " + request.getMethod()
                + " received at " + sipStack.getStackName()
                + " with server transaction id " + st);

        try {
            if (request.getMethod().equals(Request.SUBSCRIBE)) {
                processSubscribe(re, st);
            } else if (request.getMethod().equals(Request.NOTIFY)) {    // because
                                                                        // of
                                                                        // Record-Routing

                logger.info( "Got NOTIFY, forwarding statelessly...");

                // Forward it without creating a transaction

                // RFC3265 says: "proxy MUST record-route the initial SUBSCRIBE
                // and
                // any dialog-establishing NOTIFY requests
                // Use from tag as unique id, for debugging
                FromHeader from = (FromHeader) request.getHeader(FromHeader.NAME);
                recordRoute( request, from.getTag() );

                doForwardStateless( request, st );
            } else {
                Response notImplemented = messageFactory.createResponse( Response.NOT_IMPLEMENTED, request );
                ((SipProvider)re.getSource()).sendResponse( notImplemented );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the invite request.
     */
    public void processSubscribe( RequestEvent re, ServerTransaction st) {
        Request request = re.getRequest();
        try {
            logger.info("forker: got an Subscribe -> forking or forwarding");

            // Check if it is in-dialog or not
            ToHeader to = (ToHeader) request.getHeader(ToHeader.NAME);
            if (to.getTag()==null) {
                logger.info("forker: got a dialog-creating Subscribe forking twice");

                if (st==null) {
                    st = ((SipProvider)re.getSource()).getNewServerTransaction(request);
                }

              // Subscriber added a Route to us; remove it could check its 'id' here)
              request.removeFirst( RouteHeader.NAME );

                doFork( request, st, 5070 );
                doFork( request, st, 5071 );
            } else {
                System.out.println("forker: got a mid-dialog Subscribe, forwarding statelessly...");

                // Forward it statelessly
                doForwardStateless( request, st );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Mapping of Via branch IDs to the corresponding ServerTransaction, used
     * for forwarding responses
     */
    private final Map CTtoST = new HashMap();

    private void doFork( Request orig, ServerTransaction st, int port ) throws Exception {
        ViaHeader myVia = headerFactory.createViaHeader( "127.0.0.1",
                udpProvider.getListeningPoint("udp").getPort(), "udp", null );
        Request forked = (Request) orig.clone();
        forked.addHeader( myVia );

        // Note: BIG Gotcha: Need to do this before creating the
        // ClientTransaction!
        if (nonRFC3261Proxy) {
            SipURI suri = addressFactory.createSipURI( null, "127.0.0.1" );
            suri.setPort( port );
            forked.setRequestURI( suri );
        } else {
            RouteHeader route = headerFactory.createRouteHeader(
                addressFactory.createAddress( "<sip:127.0.0.1;lr>" )
            );
            ((SipURI)route.getAddress().getURI()).setPort( port );
            forked.addHeader( route );
        }

        // Add a Record-Route header, to test that separate dialog instances are
        // correctly created at the subscriber
        // This causes us to receive NOTIFYs too
        recordRoute( forked, Integer.toString(port) );

        ClientTransaction ct = udpProvider.getNewClientTransaction( forked );
        CTtoST.put( ct, st );
        ct.sendRequest();// gets sent to the outbound proxy == Notifier
    }

    private void doForwardStateless( Request orig, ServerTransaction st ) throws ParseException,
        InvalidArgumentException, SipException {
        // To forward statelessly, we need to keep the stack from
        // creating a ST for us.
        // Internally a dialog is created for the SUBSCRIBE, unless
        // dialog support
        // XXX bug: if I use branch==null here, the stack assigns a random int
        // without magic cookie
        //
        // Latest wisdom from RFC3261 says to simply copy branch from current top via
        // when forwarding statelessly
        //
        ViaHeader top = (ViaHeader) orig.getHeader( ViaHeader.NAME );
        ViaHeader myVia = headerFactory.createViaHeader("127.0.0.1",
                5065, "udp", top.getBranch() );
        orig.addFirst( myVia );

        if (nonRFC3261Proxy) {
            RouteHeader route = (RouteHeader) orig.getHeader( "Route" );
            if (route!=null) {
                orig.removeFirst( "Route" );
                orig.setRequestURI( route.getAddress().getURI() );
            }
        } else {
            orig.removeFirst( RouteHeader.NAME );// points at us
        }

        // To forward statelessly, we need to keep the stack from creating a ST for us.
        // Internally a dialog is created for the SUBSCRIBE, unless dialog support
        // is switched off (see initialization)
        if (st!=null) {
            logger.info( "Would like to forward statelessly, but ST!=null! Problem...");
            logger.info("st == " + st);

        }
        udpProvider.sendRequest( orig );
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        logger.info("Got a response");
        Response response = (Response) responseReceivedEvent.getResponse();
        ClientTransaction ct = responseReceivedEvent.getClientTransaction();

        logger.info("Dialog = " + responseReceivedEvent.getDialog());

        logger.info("Response received with client transaction id "
                + ct + ": " + response.getStatusCode() );

        if (ct==null) {
            logger.info( "Assuming NOTIFY response, forwarding...");
            // NOTIFYs are forwarded without transaction, do the same for their
            // responses
            response.removeFirst( ViaHeader.NAME );
            try {
                udpProvider.sendResponse( response );
            } catch (SipException e) {
                e.printStackTrace();
            }
        } else {
            ServerTransaction st = (ServerTransaction) CTtoST.get(ct);
            if (st!=null) {
                // only forward the first response
                synchronized (st) {
                    if (st.getState() == TransactionState.TRYING) {
                        response.removeFirst( ViaHeader.NAME );
                        try {
                            st.sendResponse( response );
                        } catch (SipException e) {
                            e.printStackTrace();
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        logger.info( "Discarding second response" );
                    }
                    CTtoST.remove( ct );
                }
            } else {
                logger.info( "No ST found");
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
        logger.info("Transaction Time out");
    }

    private static void initFactories () {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();

        String name = "forker";
        if (nonRFC3261Proxy) name += "_nonRFC3261";

        properties.setProperty("javax.sip.STACK_NAME", name );
        // You need 16 for logging traces. 32 for debug + traces.
        // Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "forkerdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
                "forkerlog.txt");

        // Switch OFF automatic dialog support. We dont want dialogs in a proxy!
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
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
        }
    }

    public void init() {

        try {

            ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1", 5065, "udp");

            this.udpProvider = sipStack.createSipProvider(lp);
            logger.info("udp provider " + udpProvider);

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }

    public static void main(String args[]) throws Exception {

        nonRFC3261Proxy = args.length > 0;

        initFactories();
        Forker f = new Forker();
        f.init();
        f.udpProvider.addSipListener(f);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        // TODO Auto-generated method stub

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        // TODO Auto-generated method stub

    }

}
