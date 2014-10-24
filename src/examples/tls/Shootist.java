package examples.tls;
import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.TlsSecurityPolicy;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransaction;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;

import test.unit.gov.nist.javax.sip.stack.tls.TlsTest;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.*;



/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme
 * is the guy that gets shot.
 *
 *@author Daniel Martinez
 *@author Ivelin Ivanov
 */

public class Shootist implements SipListener, TlsSecurityPolicy {

    private static SipProvider tlsProvider;
    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory headerFactory;
    private static SipStack sipStack;
    private int reInviteCount;
    private ContactHeader contactHeader;
    private ListeningPoint tlsListeningPoint;
    private int counter;

    protected ClientTransaction inviteTid;

    protected static final String usageString =
        "java "
            + "examples.shootistTLS.Shootist \n"
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

    public void init() {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        String transport = "tls";
        int port = 5061;
        String peerHostPort = "127.0.0.1:5071";
        properties.setProperty(
            "javax.sip.OUTBOUND_PROXY",
            peerHostPort + "/" + transport);
        // If you want to use UDP then uncomment this.
        //properties.setProperty(
        //  "javax.sip.ROUTER_PATH",
        //  "examples.shootistTLS.MyRouter");
        properties.setProperty("javax.sip.STACK_NAME", "shootist");
   
        // The following properties are specific to nist-sip
        // and are not necessarily part of any other jain-sip
        // implementation.
        // You can set a max message size for tcp transport to
        // guard against denial of service attack.
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE",
                    "1048576");
        properties.setProperty(
            "gov.nist.javax.sip.DEBUG_LOG",
            "shootistdebug.txt");
        properties.setProperty(
            "gov.nist.javax.sip.SERVER_LOG",
            "shootistlog.txt");

        properties.setProperty("gov.nist.javax.sip.TLS_SECURITY_POLICY",
                this.getClass().getName());
//        properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());

        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
        // Set to 0 in your production code for max speed.
        // You need  16 for logging traces. 32 for debug + traces.
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
            Shootist listener = this;

            tlsListeningPoint = sipStack.createListeningPoint
                                ("127.0.0.1", port, "tls");
            tlsProvider = sipStack.createSipProvider(tlsListeningPoint);
            tlsProvider.addSipListener(listener);

            SipProvider sipProvider = tlsProvider;

            String fromName = "BigGuy";
            String fromSipAddress = "here.com";
            String fromDisplayName = "The Master Blaster";

            String toSipAddress = "there.com";
            String toUser = "LittleGuy";
            String toDisplayName = "The Little Blister";

            // create >From Header
            SipURI fromAddress =
                addressFactory.createSipURI(fromName, fromSipAddress);
            //fromAddress.setSecure(true);

            Address fromNameAddress = addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader =
                headerFactory.createFromHeader(fromNameAddress, "12345");

            // create To Header
            SipURI toAddress =
                addressFactory.createSipURI(toUser, toSipAddress);
            //toAddress.setSecure(true);
            Address toNameAddress = addressFactory.createAddress(toAddress);
            toNameAddress.setDisplayName(toDisplayName);
            ToHeader toHeader =
                headerFactory.createToHeader(toNameAddress, null);

            // create Request URI
            SipURI requestURI =
                addressFactory.createSipURI(toUser, peerHostPort);
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

            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader =
                headerFactory.createContentTypeHeader("application", "sdp");

            // Create a new CallId header
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // Create a new Cseq header
            CSeqHeader cSeqHeader =
                headerFactory.createCSeqHeader(1L, Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards =
                headerFactory.createMaxForwardsHeader(70);

            // Create the request.
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
            // Create contact headers
            String host = "127.0.0.1";

            //SipURI contactUrl = addressFactory.createSipURI(fromName, host);
            //contactUrl.setPort(tlsListeningPoint.getPort());

            // Create the contact name address.
            SipURI contactURI = addressFactory.createSipURI(fromName, host);
            //contactURI.setSecure( true );
            contactURI.setPort(port);
            contactURI.setTransportParam("tls");

            Address contactAddress = addressFactory.createAddress(contactURI);

            // Add the contact address.
            contactAddress.setDisplayName(fromName);

            contactHeader =
                headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Add the extension header.
            Header extensionHeader =
                headerFactory.createHeader("My-Header", "my header value");
            request.addHeader(extensionHeader);

            String sdpData =
                "v=0\r\n"
                    + "o=4855 13760799956958020 13760799956958020"
                    + " IN IP4  129.6.55.78\r\n"
                    + "s=mysession session\r\n"
                    + "p=+46 8 52018010\r\n"
                    + "c=IN IP4  129.6.55.78\r\n"
                    + "t=0 0\r\n"
                    + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                    + "a=rtpmap:0 PCMU/8000\r\n"
                    + "a=rtpmap:4 G723/8000\r\n"
                    + "a=rtpmap:18 G729A/8000\r\n"
                    + "a=ptime:20\r\n";
            byte[]  contents = sdpData.getBytes();
            //byte[]  contents = sdpBuff.toString().getBytes();

            request.setContent(contents, contentTypeHeader);

            Header callInfoHeader =
                headerFactory.createHeader(
                    "Call-Info",
                    "<http://www.antd.nist.gov>");
            request.addHeader(callInfoHeader);
            
            SipURI routeUri = (SipURI) requestURI.clone();
            routeUri.setLrParam();
            routeUri.setTransportParam("tls");
            Address peerAddress = addressFactory.createAddress(requestURI);
           
            
            RouteHeader routeHeader = headerFactory.createRouteHeader(peerAddress);
            request.setHeader(routeHeader);


            // Create the client transaction.
            listener.inviteTid = sipProvider.getNewClientTransaction(request);

            // send the request out.
            listener.inviteTid.sendRequest();
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }
    }

    public static void main(String args[]) {
    	System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
        System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
        new Shootist().init();

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
        System.out.println("enforceTlsPolicy");
        List<String> certIdentities = null;
        try {
            certIdentities = transaction.extractCertIdentities();
        } catch (SSLPeerUnverifiedException e) {
        }
        if ((certIdentities == null) || certIdentities.isEmpty()) {
            System.out.println("Could not find any identities in the TLS certificate");
        }
        else {
            System.out.println("found identities: " + certIdentities);
        }

        // the destination IP address should match one of the certIdentities
        boolean foundPeerIdentity = false;
        String expectedIpAddress = ((SipURI)transaction.getRequest().getRequestURI()).getHost();
        for (String identity : certIdentities) {
            // identities must be resolved to dotted quads before comparing: this is faked here
            String peerIpAddress = "10.10.10.0";
            if (identity.equals("localhost")) {
                peerIpAddress = "127.0.0.1";
            }
            if (expectedIpAddress.equals(peerIpAddress)) {
                foundPeerIdentity = true;
            }
        }
        if (!foundPeerIdentity) {
            throw new SecurityException("Certificate identity does not match requested domain");
        }
        System.out.println("isSecure = " + transaction.isSecure());
        if (transaction.isSecure() ) {
            System.out.println("cipherSuite = " + transaction.getCipherSuite());
            if(transaction.getLocalCertificates() != null) {
                for ( Certificate cert : transaction.getLocalCertificates()) {
                    System.out.println("localCert =" + cert);
                }
            }
            try {
	            if(transaction.getPeerCertificates() != null) {
	                for ( Certificate cert : transaction.getPeerCertificates()) {
	                    System.out.println("remoteCerts = " + cert);
	                }
	            }
            } catch (SSLPeerUnverifiedException e) {
            	throw new SecurityException("problem with peers", e);
            }
        }
    }
}
