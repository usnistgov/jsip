package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.DialogExt;
import gov.nist.javax.sip.SipProviderExt;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;

import java.util.ArrayList;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
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
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import test.unit.gov.nist.javax.sip.stack.tls.TlsTest;
import junit.framework.TestCase;

/**
 * Testing complete websocket scenario browser to server HTTP-Upgrade->INVITE->ACK->MESSAGE->BYE
 * 
 * @author vralev
 *
 */
public class WebsocketSelfTest extends TestCase {

	private static String transport;
    private static final String myAddress = "127.0.0.1";
    private String peerHostPort = "127.0.0.1:5070";
    private WebsocketServer websocketServer;
    private WebsocketBrowser websocketBrowser;
    
    public class WebsocketServer implements SipListener {

        private  AddressFactory addressFactory;
        private  MessageFactory messageFactory;
        private  HeaderFactory headerFactory;
        private SipStack sipStack;
        private SipProvider sipProvider;
        private static final int myPort = 5070;
        private DialogExt dialog;
        public static final boolean callerSendsBye = true;
        boolean ackReceived;
        boolean okByeReceived;

        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent
                    .getServerTransaction();

            System.out.println("\n\nRequest " + request.getMethod()
                    + " received at " + sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } else if(request.getMethod().equals(Request.ACK)) {
                processAck(requestEvent, serverTransactionId);
            }
        }

        public void processResponse(ResponseEvent responseEvent) {
        	if(responseEvent.getResponse().getStatusCode() != 200) return;
        	String messageMethod = ((CSeqHeader)responseEvent.getResponse().getHeader(CSeqHeader.NAME)).getMethod();
        	if(messageMethod.equals("INVITE")) {
                try {
                    System.out.println("Got an OK response! ");
                    System.out.println("Dialog State = " + dialog.getState());
                    SipProvider provider = (SipProvider) responseEvent.getSource();

                    Request messageRequest = dialog.createRequest(Request.MESSAGE);
                    CSeqHeader cseq = (CSeqHeader)messageRequest.getHeader(CSeqHeader.NAME);

                    // We will test if the CSEq validation is off by sending CSeq 1 again
                    cseq.setSeqNumber(1);
                    ClientTransaction ct = provider
                    .getNewClientTransaction(messageRequest);
                    dialog.sendRequest(ct);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if(messageMethod.equals("MESSAGE")) {
                try {
                    System.out.println("Got an OK response for MESSAGE! ");
                    System.out.println("Dialog State = " + dialog.getState());
                    SipProvider provider = (SipProvider) responseEvent.getSource();

                    Request messageRequest = dialog.createRequest(Request.BYE);
                    CSeqHeader cseq = (CSeqHeader) messageRequest.getHeader(CSeqHeader.NAME);
                    cseq.setSeqNumber(3);
                    messageRequest.setHeader(cseq);

                    ClientTransaction ct = provider
                    .getNewClientTransaction(messageRequest);
                    dialog.sendRequest(ct);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            else if(messageMethod.equals("BYE")) {
                try {
                    this.okByeReceived = true;

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if(responseEvent.getResponse().getStatusCode() == 500) {
                fail("We received some error. It should not happen with loose dialog validation. We should not receive error on cseq out of order");
            }
        }

        /**
         * Process the ACK request. Send the bye and complete the call flow.
         */
        public void processAck(RequestEvent requestEvent,
        		ServerTransaction serverTransaction) {
        	try {
        		this.ackReceived = true;
        		System.out.println("Got an ACK! ");
        		System.out.println("Dialog State = " + dialog.getState());
        		SipProvider provider = (SipProvider) requestEvent.getSource();

        		Request messageRequest = dialog.createRequest(Request.MESSAGE);
        		CSeqHeader cseq = (CSeqHeader)messageRequest.getHeader(CSeqHeader.NAME);

        		// We will test if the CSEq validation is off by sending CSeq 1 again

        		ClientTransaction ct = provider
        				.getNewClientTransaction(messageRequest);
        		cseq.setSeqNumber(1);
        		ct.sendRequest();

        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
        }

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
        
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
  
            Request request = requestEvent.getRequest();
            try {
                serverTransaction = sipProvider.getNewServerTransaction(request);
                dialog = (DialogExt) sipProvider.getNewDialog(serverTransaction);
                dialog.disableSequenceNumberValidation();
            } catch (Exception e) {
            	// TODO Auto-generated catch block
            	e.printStackTrace();
            }

            try {
            	Response okResponse = messageFactory.createResponse(180,
            			request);
            	FromHeader from = (FromHeader) okResponse.getHeader(FromHeader.NAME);
            	from.removeParameter("tag");
            	Address address = addressFactory.createAddress("UAS <sip:"
            			+ myAddress + ":" + myPort + ">");
            	ContactHeader contactHeader = headerFactory
            			.createContactHeader(address);
            	ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
            	toHeader.setTag("4321");

            	FromHeader fromHeader = (FromHeader)okResponse.getHeader(FromHeader.NAME);
            	fromHeader.setTag("12345");
            	okResponse.addHeader(contactHeader);
            	serverTransaction.sendResponse(okResponse);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }

            try {
                Response okResponse = messageFactory.createResponse(200,
                        request);
                FromHeader from = (FromHeader) okResponse.getHeader(FromHeader.NAME);
                from.removeParameter("tag");
                Address address = addressFactory.createAddress("UAS <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = headerFactory
                        .createContactHeader(address);
                ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
                toHeader.setTag("4321"); 

                FromHeader fromHeader = (FromHeader)okResponse.getHeader(FromHeader.NAME);
                fromHeader.setTag("12345");
                okResponse.addHeader(contactHeader);
                serverTransaction.sendResponse(okResponse);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            Transaction transaction;
            if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
            } else {
                transaction = timeoutEvent.getClientTransaction();
            }
            System.out.println("state = " + transaction.getState());
            System.out.println("dialog = " + transaction.getDialog());
            System.out.println("dialogState = "
                    + transaction.getDialog().getState());
            System.out.println("Transaction Time out");
        }

        public void init() {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();
            properties.setProperty("javax.sip.STACK_NAME", "server");
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
           
            properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING", "false");
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
            	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            
            try {
                sipStack = sipFactory.createSipStack(properties);
                System.out.println("sipStack = " + sipStack);
            } catch (PeerUnavailableException e) {
               
                e.printStackTrace();
                System.err.println(e.getMessage());
                if (e.getCause() != null)
                    e.getCause().printStackTrace();
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                ListeningPoint lp = sipStack.createListeningPoint(myAddress,
                        myPort, transport);

                WebsocketServer listener = this;

                sipProvider = sipStack.createSipProvider(lp);
                System.out.println("udp provider " + sipProvider);
                sipProvider.addSipListener(listener);

            } catch (Exception ex) {
                ex.printStackTrace();
                fail("Unexpected exception");
            }

        }

        public void processIOException(IOExceptionEvent exceptionEvent) {
            fail("IOException");

        }

        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            if (transactionTerminatedEvent.isServerTransaction())
                System.out.println("Transaction terminated event recieved"
                        + transactionTerminatedEvent.getServerTransaction());
            else
                System.out.println("Transaction terminated "
                        + transactionTerminatedEvent.getClientTransaction());
        }

        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            Dialog d = dialogTerminatedEvent.getDialog();
            System.out.println("Local Party = " + d.getLocalParty());

        }

        public void terminate() {
            this.sipStack.stop();
        }

    }

    public class WebsocketBrowser implements SipListener {

        private SipProvider sipProvider;
        private AddressFactory addressFactory;
        private MessageFactory messageFactory;
        private HeaderFactory headerFactory;
        private SipStack sipStack;
        private ContactHeader contactHeader;
        private ListeningPoint udpListeningPoint;
        private Dialog dialog;
        public boolean okByeReceived;
        
        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            try {
            	Response response = messageFactory.createResponse(200, request);
            	requestReceivedEvent.getServerTransaction().sendResponse(response);
            } catch (Exception e) {
            	e.printStackTrace();fail("Error");
            }
        }

        public void processResponse(ResponseEvent responseReceivedEvent) {
        	CSeqHeader cseq = (CSeqHeader) responseReceivedEvent.getResponse().getHeader(CSeqHeader.NAME);
        	String method = cseq.getMethod();
        	if(method.equals(Request.INVITE)) {
        		if ( responseReceivedEvent.getResponse().getStatusCode() == Response.OK) {
        			Dialog d = responseReceivedEvent.getDialog();
        			try {
        				Request ack = d.createAck(1);
        				sipProvider.sendRequest(ack);
        			} catch (Exception e) {
        				e.printStackTrace();
        				fail("Error sending ACK");
        			}
        		}
        	} else if(method.equals(Request.BYE)){
        		okByeReceived = true;
        	}
        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            System.out.println("Got a timeout " + timeoutEvent.getClientTransaction());
        }



        public void init() {
            SipFactory sipFactory = null;
            sipStack = null;
            sipFactory = SipFactory.getInstance();
            sipFactory.setPathName("gov.nist");
            Properties properties = new Properties();

            properties.setProperty("javax.sip.STACK_NAME", "browserphone");

            // Drop the client connection after we are done with the transaction.
            properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                    "false");
            //properties.setProperty("javax.net.ssl.trustStore", "/Users/vladimirralev/dev.nist.gov.jks");
            //properties.setProperty("javax.net.ssl.keyStore", "/Users/vladimirralev/dev.nist.gov.jks");
            //properties.setProperty("javax.net.ssl.keyStorePassword", "passphrase");
            //properties.setProperty("javax.net.ssl.trustStorePassword", "passphrase");
            
            //properties.setProperty("gov.nist.javax.sip.USE_TLS_ACCELERATOR", "true");
            
            properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
            properties.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", "1");
            properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");
            properties.setProperty("gov.nist.javax.sip.AUTOMATIC_DIALOG_ERROR_HANDLING","false");
            properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
            
            try {
                sipStack = sipFactory.createSipStack(properties);
                System.out.println("createSipStack " + sipStack);
            } catch (PeerUnavailableException e) {

                e.printStackTrace();
                System.err.println(e.getMessage());
                fail("Problem with setup");
            }

            try {
                headerFactory = sipFactory.createHeaderFactory();
                addressFactory = sipFactory.createAddressFactory();
                messageFactory = sipFactory.createMessageFactory();
                udpListeningPoint = sipStack.createListeningPoint(myAddress, 5060, transport);
                sipProvider = sipStack.createSipProvider(udpListeningPoint);
                WebsocketBrowser listener = this;
                sipProvider.addSipListener(listener);

                String fromName = "anonymous";
                String fromSipAddress = "anonymous.invalid";
                String fromDisplayName = "User";

                String toSipAddress = "nist.gov";
                String toUser = "user";
                String toDisplayName = "user";

                SipURI fromAddress = addressFactory.createSipURI(fromName,
                        fromSipAddress);

                Address fromNameAddress = addressFactory.createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = headerFactory.createFromHeader(
                        fromNameAddress, "12345");

                SipURI toAddress = addressFactory
                        .createSipURI(toUser, toSipAddress);
                Address toNameAddress = addressFactory.createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
                        null);

                // Set proper websocket SIP uri
                SipURI requestURI = addressFactory.createSipURI(toUser,
                        peerHostPort);
                requestURI.setMethodParam("GET");
        		requestURI.setHeader("Host", "dev.nist.gov");
        		requestURI.setHeader("Location", "/sip");

                ArrayList viaHeaders = new ArrayList();
                String ipAddress = udpListeningPoint.getIPAddress();
                ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
                        sipProvider.getListeningPoint(transport).getPort(),
                        transport, null);

                viaHeaders.add(viaHeader);

                ContentTypeHeader contentTypeHeader = headerFactory
                        .createContentTypeHeader("application", "sdp");

                CallIdHeader callIdHeader = sipProvider.getNewCallId();

                CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
                        Request.INVITE);

                MaxForwardsHeader maxForwards = headerFactory
                        .createMaxForwardsHeader(70);

                Request request = messageFactory.createRequest(requestURI,
                        Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                        toHeader, viaHeaders, maxForwards);

                String host = myAddress;

                SipURI contactUrl = addressFactory.createSipURI(fromName, host);
                contactUrl.setPort(udpListeningPoint.getPort());
                contactUrl.setLrParam();

                SipURI contactURI = addressFactory.createSipURI(fromName, host);
                contactURI.setPort(sipProvider.getListeningPoint(transport)
                        .getPort());

                Address contactAddress = addressFactory.createAddress(contactURI);

                contactAddress.setDisplayName(fromName);

                contactHeader = headerFactory.createContactHeader(contactAddress);
                request.addHeader(contactHeader);

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
                byte[] contents = sdpData.getBytes();

                request.setContent(contents, contentTypeHeader);
              
                ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);
            	Dialog d = null;
				try {
					d = sipProvider.getNewDialog(inviteTid);
				} catch (SipException e1) {
					e1.printStackTrace();
				}

                inviteTid.sendRequest();

                dialog = inviteTid.getDialog();

            } catch (Exception ex) {
            	ex.printStackTrace();
                fail("cannot create or send initial invite");
            }
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
        
        public void terminate() {
            this.sipStack.stop();
        }
    }

    public void setUp() {
    	System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
		System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
		System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
		System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
    	ConsoleAppender console = new ConsoleAppender();
    	console.setName("Console app");
    	String PATTERN = "%d [%p|%c|%C{1}] %m%n";
    	console.setLayout(new PatternLayout(PATTERN)); 
    	console.setThreshold(Level.DEBUG);
    	console.activateOptions();
    	Logger.getRootLogger().addAppender(console);
    	this.websocketServer = new WebsocketServer();
    	this.websocketBrowser = new WebsocketBrowser();
    }
    
    public void tearDown() {
    	websocketBrowser.terminate();
        websocketServer.terminate();
    }

    public void testWebsocketBrowserServer() {
    	transport = "ws";
        this.websocketServer.init();
        this.websocketBrowser.init();
        try {
            Thread.sleep(8000);
        } catch (Exception ex) {

        }
        assertTrue(this.websocketServer.okByeReceived);
        assertTrue(this.websocketServer.ackReceived);
    }
    
    public void testTlsWebsocketBrowserServer() {
    	transport = "wss";
        this.websocketServer.init();
        this.websocketBrowser.init();
        try {
            Thread.sleep(8000);
        } catch (Exception ex) {

        }
        assertTrue(this.websocketServer.okByeReceived);
        assertTrue(this.websocketServer.ackReceived);
    }



}
