package examples.wssclient;
import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.TlsSecurityPolicy;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransaction;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.*;



/**
 * This class is a WSS UAC template.
 * It will establish websocket connection towards the destination, register and invite.
 *
 *@author Vladimir Ralev
 */
public class WSSClient implements SipListener, TlsSecurityPolicy {

    private static SipProvider tlsProvider;
    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory headerFactory;
    private static SipStack sipStack;
    private int reInviteCount;
    private ContactHeader contactHeader;
    private ListeningPoint tlsListeningPoint;
    private int counter;
    private String transport="wss";
    private int port = 5061;
    private String peerHostPort;
    private String hostname;
    private String httpUrl;

    protected ClientTransaction inviteTid;

    protected static final String usageString =
        "java "
            + "examples.shootistTLS.Shootist \n"
            + ">>>> is your class path set to the root?";

    private static void usage() {
        System.out.println(usageString);
        //System.exit(0);

    }
    private void shutDown() {
        try {
                try {
                Thread.sleep(2000);
                 } catch (InterruptedException e) {
                 }
            System.out.println("nulling reference");
            sipStack.deleteListeningPoint(tlsListeningPoint);
            // This will close down the stack and exit all threads
            tlsProvider.removeSipListener(this);
            while (true) {
              try {
                  sipStack.deleteSipProvider(tlsProvider);
                  break;
                } catch (ObjectInUseException  ex)  {
                    try {
                    Thread.sleep(2000);
                     } catch (InterruptedException e) {
                    continue;
                     }
               }
            }
            sipStack = null;
            tlsProvider = null;
            this.inviteTid = null;
            this.contactHeader = null;
            addressFactory = null;
            headerFactory = null;
            messageFactory = null;
            this.tlsListeningPoint = null;
            this.reInviteCount = 0;
            System.gc();
            //Redo this from the start.
           //  if (counter < 10 )
           //     this.init();
           //  else counter ++;
        } catch (Exception ex) { ex.printStackTrace(); }
    }


    public void processRequest(RequestEvent requestReceivedEvent) {
        Request request = requestReceivedEvent.getRequest();
        ServerTransaction serverTransactionId =
            requestReceivedEvent.getServerTransaction();

        System.out.println(
            "\n\nRequest "
                + request.getMethod()
                + " received at "
                + sipStack.getStackName()
                + " with server transaction id "
                + serverTransactionId);

        // We are the UAC so the only request we get is the BYE.
        if (request.getMethod().equals(Request.BYE))
            processBye(request, serverTransactionId);

    }

    public void processBye(
        Request request,
        ServerTransaction serverTransactionId) {
        try {
            System.out.println("shootist:  got a bye .");
            if (serverTransactionId == null) {
                System.out.println("shootist:  null TID.");
                return;
            }
            Dialog dialog = serverTransactionId.getDialog();
            System.out.println("Dialog State = " + dialog.getState());
            Response response = messageFactory.createResponse
                        (200, request);
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

        System.out.println(
            "Response received with client transaction id "
                + tid
                + ":\n"
                + response.getStatusCode());
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
                    .getMethod()
                    .equals(
                    Request.INVITE)) {
                Dialog dialog = tid.getDialog();
                Request ackRequest = dialog.createAck( cseq.getSeqNumber() );
                System.out.println("Sending ACK");
                dialog.sendAck(ackRequest);

                // Send a Re INVITE
                if (reInviteCount == 0) {
                    Request inviteRequest = dialog.createRequest(Request.INVITE);
                    //((SipURI)inviteRequest.getRequestURI()).removeParameter("transport");
                    //((ViaHeader)inviteRequest.getHeader(ViaHeader.NAME)).setTransport("tls");
                    // inviteRequest.addHeader(contactHeader);

                    try {Thread.sleep(100); } catch (Exception ex) {}
                    ClientTransaction ct =
                    tlsProvider.getNewClientTransaction(inviteRequest);
                    dialog.sendRequest(ct);
                    reInviteCount ++;
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

    }

    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        System.out.println("Transaction Time out" );
    }

    public void init(String peerHostPort, String hostname, String httpUrl) {
    	this.peerHostPort = peerHostPort;
    	this.hostname = hostname;
    	this.httpUrl = httpUrl;
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        
        
        properties.setProperty(
            "javax.sip.OUTBOUND_PROXY",
            peerHostPort + "/" + transport);

        properties.setProperty("javax.sip.STACK_NAME", "shootist");
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
                    "1048576");
        properties.setProperty(
            "gov.nist.javax.sip.DEBUG_LOG",
            "shootistdebug.txt");
        properties.setProperty(
            "gov.nist.javax.sip.SERVER_LOG",
            "shootistlog.txt");
        properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY",
        		NioMessageProcessorFactory.class.getName());
        properties.setProperty("gov.nist.javax.sip.TLS_SECURITY_POLICY",
                this.getClass().getName());
        properties.setProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE",
                "Disabled");
        //properties.setProperty("javax.net.ssl.trustStore", "/Users/vladimirralev/cert.jks");
        //properties.setProperty("javax.net.ssl.keyStore", "/Users/vladimirralev/cert.jks");
        properties.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
        properties.setProperty("javax.net.ssl.trustStorePassword", "passphrase");
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");

        try {
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
        }

        try {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            WSSClient listener = this;

            tlsListeningPoint = sipStack.createListeningPoint
                                ("127.0.0.1", port, transport);
            tlsProvider = sipStack.createSipProvider(tlsListeningPoint);
            tlsProvider.addSipListener(listener);

            sendRegister();
            Thread.sleep(1000);
            sendInvite();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public void sendInvite() {
    	try {
    		String fromName = "anonymous";
    		String fromSipAddress = "anonymous.invalid";
    		String fromDisplayName = "";

    		String toSipAddress = "company1";
    		String toUser = "user1";
    		String toDisplayName = "";

    		// create >From Header
    		SipURI fromAddress =
    				addressFactory.createSipURI(fromName, fromSipAddress);
    		//fromAddress.setSecure(true);

    		Address fromNameAddress = addressFactory.createAddress(fromAddress);
    		//fromNameAddress.setDisplayName(fromDisplayName);
    		FromHeader fromHeader =
    				headerFactory.createFromHeader(fromNameAddress, "12345");

    		// create To Header
    		SipURI toAddress =
    				addressFactory.createSipURI(toUser, toSipAddress);
    		//toAddress.setSecure(true);
    		Address toNameAddress = addressFactory.createAddress(toAddress);
    		//toNameAddress.setDisplayName(toDisplayName);
    		ToHeader toHeader =
    				headerFactory.createToHeader(toNameAddress, null);

    		// create Request URI
    		SipURI requestURI =
    				addressFactory.createSipURI(toUser, peerHostPort);
    		requestURI.setMethodParam("GET");
    		requestURI.setHeader("Host", this.hostname);
    		requestURI.setHeader("Location", this.httpUrl);
    		//requestURI.setSecure( true );

    		// Create ViaHeaders

    		ArrayList viaHeaders = new ArrayList();
    		ViaHeader viaHeader =
    				headerFactory.createViaHeader(
    						"127.0.0.1",
    						port,
    						transport,
    						null);


    		viaHeaders.add(viaHeader);

    		ContentTypeHeader contentTypeHeader =
    				headerFactory.createContentTypeHeader("application", "sdp");

    		CallIdHeader callIdHeader = this.tlsProvider.getNewCallId();

    		CSeqHeader cSeqHeader =
    				headerFactory.createCSeqHeader(1L, Request.INVITE);

    		MaxForwardsHeader maxForwards =
    				headerFactory.createMaxForwardsHeader(70);

    		Request request =
    				messageFactory.createRequest(
    						requestURI,
    						Request.INVITE,
    						callIdHeader,
    						cSeqHeader,
    						fromHeader,
    						toHeader,
    						viaHeaders,
    						maxForwards);
    		String host = "127.0.0.1";  

    		SipURI contactURI = addressFactory.createSipURI(fromName, host);
    		contactURI.setPort(port);
    		contactURI.setTransportParam(transport);

    		Address contactAddress = addressFactory.createAddress(contactURI);

    		contactAddress.setDisplayName(fromName);

    		contactHeader =
    				headerFactory.createContactHeader(contactAddress);
    		request.addHeader(contactHeader);


    		String sdpData =
    				"test\r\n";
    		byte[]  contents = sdpData.getBytes();

    		request.setContent(contents, contentTypeHeader);

    		SipURI routeUri = (SipURI) requestURI.clone();
    		routeUri.setLrParam();
    		routeUri.setTransportParam(transport);
    		Address peerAddress = addressFactory.createAddress(requestURI);


    		RouteHeader routeHeader = headerFactory.createRouteHeader(peerAddress);
    		request.setHeader(routeHeader);


    		// Create the client transaction.
    		this.inviteTid = tlsProvider.getNewClientTransaction(request);

    		// send the request out.
    		inviteTid.sendRequest();


    	} catch (Exception ex) {
    		System.out.println(ex.getMessage());
    		ex.printStackTrace();
    		usage();
    	}
    }

    public void sendRegister() {
    	try {

    		SipProvider sipProvider = tlsProvider;

            String fromName = "anonymous";
            String fromSipAddress = "anonymous.invalid";
            String fromDisplayName = "";

            String toSipAddress = "anonymous.invalid";
            String toUser = "anonymous";
            String toDisplayName = "";

            // create >From Header
            SipURI fromAddress =
                addressFactory.createSipURI(fromName, fromSipAddress);
            //fromAddress.setSecure(true);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            //fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader =
                headerFactory.createFromHeader(fromNameAddress, "12345");

            // create To Header
            SipURI toAddress =
                addressFactory.createSipURI(toUser, toSipAddress);
            //toAddress.setSecure(true);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            //toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader =
                headerFactory.createToHeader(toNameAddress, null);

            // create Request URI
            SipURI requestURI =
                addressFactory.createSipURI(toUser, peerHostPort);
            requestURI.setMethodParam("GET");
            requestURI.setHeader("Host", this.hostname);
            requestURI.setHeader("Location", this.httpUrl);
            //requestURI.setSecure( true );

            // Create ViaHeaders

           ArrayList viaHeaders = new ArrayList();
           ViaHeader viaHeader =
                headerFactory.createViaHeader(
                    "127.0.0.1",
                    port,
                    transport,
                    null);


            // add via headers
            viaHeaders.add(viaHeader);

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader =
                headerFactory.createCSeqHeader(1L, Request.REGISTER);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards =
                headerFactory.createMaxForwardsHeader(70);

            // Create the request.
            Request request =
                messageFactory.createRequest(
                    requestURI,
                    Request.REGISTER,
                    callIdHeader,
                    cSeqHeader,
                    fromHeader,
                    toHeader,
                    viaHeaders,
                    maxForwards);
            // Create contact headers
            String host = "127.0.0.1";  

            //SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            //contactUrl.setPort(tlsListeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            //contactURI.setSecure( true );
            contactURI.setPort(port);
            contactURI.setTransportParam(transport);

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader =
                headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            
            SipURI routeUri = (SipURI) requestURI.clone();
            routeUri.setLrParam();
            routeUri.setTransportParam("tls");
            Address peerAddress = addressFactory.createAddress(requestURI);
           
            
            RouteHeader routeHeader = headerFactory.createRouteHeader(peerAddress);
            request.setHeader(routeHeader);


            // Create the client transaction.
            ClientTransaction registerCtx = sipProvider.getNewClientTransaction(request);

            // send the request out.
            registerCtx.sendRequest();
            
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) {
        new WSSClient().init("1.2.3.4:443","company.com", "/sip");

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

    public void enforceTlsPolicy(ClientTransactionExt transaction) throws SecurityException {
        
    }
}
