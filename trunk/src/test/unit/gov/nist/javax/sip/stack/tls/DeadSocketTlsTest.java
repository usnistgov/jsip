package test.unit.gov.nist.javax.sip.stack.tls;

import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.TlsSecurityPolicy;
import gov.nist.javax.sip.TransactionExt;
import gov.nist.javax.sip.stack.NioMessageProcessorFactory;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
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
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


public class DeadSocketTlsTest extends TestCase {


	private BadShootist shootist;
	private BadShootme shootme;


	public void setUp() {

		Logger root = Logger.getRootLogger();
		root.setLevel(Level.DEBUG);
		root.addAppender(new ConsoleAppender(
				new PatternLayout(PatternLayout.TTCC_CONVERSION_PATTERN)));
		// setup TLS properties
		System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
		System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
		System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
		System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
		this.shootist = new BadShootist();
		this.shootme = new BadShootme();
		shootist.setSocketDisconnectWorstCase(true);
		shootme.setSocketDisconnectWorstCase(true);
		this.shootme.init();
	}

	public void testTls() {
		this.shootist.init();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void tearDown() {
		try {
			Thread.sleep(2000);
			this.shootme.stop();
			this.shootist.stop();


			System.clearProperty( "javax.net.ssl.keyStore" );
			System.clearProperty( "javax.net.ssl.trustStore" );
			System.clearProperty( "javax.net.ssl.keyStorePassword" );
			System.clearProperty( "javax.net.ssl.keyStoreType" );


		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}














	/**
	 * This class is a UAC template. Shootist is the guy that shoots and shootme
	 * is the guy that gets shot. The BadShootist class will disconnect sockets every time it has a chance
	 * and it will help testing the socket reconnect capability for the stack. Unlike the other class this
	 * one doesnt check the certs because they are in undefined state after disconnect.
	 *
	 *@author vladimir ralev
	 */

	static public class BadShootist implements SipListener, TlsSecurityPolicy {

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
		private boolean byeSeen;
		private boolean enforceTlsPolicyCalled;

		protected static final String usageString =
				"java "
						+ "examples.shootistTLS.Shootist \n"
						+ ">>>> is your class path set to the root?";



		private boolean socketDisconnectWorstCase = false;
		public void setSocketDisconnectWorstCase(boolean disconnect) {
			this.socketDisconnectWorstCase = disconnect;
		}
		public void processRequest(RequestEvent requestReceivedEvent) {
			Request request = requestReceivedEvent.getRequest();
			ServerTransaction serverTransactionId =
					requestReceivedEvent.getServerTransaction();
			if(socketDisconnectWorstCase) {
				((SIPTransactionStack)sipStack).closeAllSockets();
			}
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

				this.byeSeen = true;

			} catch (Exception ex) {
				ex.printStackTrace();
				TlsTest.fail("unepxected exception");

			}
		}

		public void processResponse(ResponseEvent responseReceivedEvent) {
			System.out.println("Got a response");
			Response response = (Response) responseReceivedEvent.getResponse();
			Transaction tid = responseReceivedEvent.getClientTransaction();
			CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
			if(socketDisconnectWorstCase)  {
				((SIPTransactionStack)sipStack).closeAllSockets();
			}
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
				TlsTest.fail("unexpected exception");
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
					"logs/shootistdebug.txt");
			properties.setProperty(
					"gov.nist.javax.sip.SERVER_LOG",
					"logs/shootistlog.txt");
			properties.setProperty(
					"gov.nist.javax.sip.SSL_HANDSHAKE_TIMEOUT", "10000");
			properties.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", "20");
			properties.setProperty("gov.nist.javax.sip.TLS_SECURITY_POLICY",
					this.getClass().getName());

			// Drop the client connection after we are done with the transaction.
			properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");
			// Set to 0 in your production code for max speed.
			// You need  16 for logging traces. 32 for debug + traces.
			// Your code will limp at 32 but it is best for debugging.
			properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
			if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
	        	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
	        }
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
				TlsTest.fail("unexpected Exception");
			}

			try {
				headerFactory = sipFactory.createHeaderFactory();
				addressFactory = sipFactory.createAddressFactory();
				messageFactory = sipFactory.createMessageFactory();
				BadShootist listener = this;

				tlsListeningPoint = sipStack.createListeningPoint
						("127.0.0.1", port, transport);
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
				contactURI.setTransportParam(transport);

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
				routeUri.setTransportParam(transport);
				Address peerAddress = addressFactory.createAddress(routeUri);


				RouteHeader routeHeader = headerFactory.createRouteHeader(peerAddress);
				request.setHeader(routeHeader);


				// Create the client transaction.
				listener.inviteTid = sipProvider.getNewClientTransaction(request);

				Thread.sleep(100);
				// send the request out.
				listener.inviteTid.sendRequest();

				Thread.sleep(100);

				System.out.println("isSecure = " + ((ClientTransactionExt)listener.inviteTid).isSecure());
				if (((ClientTransactionExt)listener.inviteTid).getPeerCertificates()!=null && ((ClientTransactionExt)listener.inviteTid).isSecure() ) {
					System.out.println("cipherSuite = " + ((ClientTransactionExt)listener.inviteTid).getCipherSuite());
					for ( Certificate cert : ((ClientTransactionExt)listener.inviteTid).getLocalCertificates()) {
						System.out.println("localCert =" + cert);
					}
					for ( Certificate cert : ((ClientTransactionExt)listener.inviteTid).getPeerCertificates()) {
						System.out.println("remoteCerts = " + cert);
					}
				}
			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				ex.printStackTrace();
				TlsTest.fail("unexpected exception ");
			}
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
			this.enforceTlsPolicyCalled = true;
			List<String> certIdentities;
			try {
				certIdentities = transaction.extractCertIdentities();
			} catch (SSLPeerUnverifiedException e) {
				throw new SecurityException(e);
			}
			if (certIdentities.isEmpty()) {
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
		}

		public void checkState() {
			TlsTest.assertTrue("enforceTlsPolicy should be called ", this.enforceTlsPolicyCalled);
		}

		public void stop() {
			this.sipStack.stop();
		}

		public static void main(String args[]) throws Exception {
			// setup TLS properties
			System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
			System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
			System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
			System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
			BadShootist shootist = new BadShootist();
			shootist.init();
		}

	}

	/**
	 * This class is a UAC template. Shootist is the guy that shoots and shootme
	 * is the guy that gets shot. The BadShootme class will disconnect sockets every time it has a chance
	 * and it will help testing the socket reconnect capability for the stack. Unlike the other class this
	 * one doesnt check the certs because they are in undefined state after disconnect.
	 *
	 *@author vladimir ralev
	 */

	static public class BadShootme implements SipListener {

		private static AddressFactory addressFactory;
		private static MessageFactory messageFactory;
		private static HeaderFactory headerFactory;
		private static SipStack sipStack;
		private static final String myAddress = "127.0.0.1";
		private static final int myPort    = 5071;

		protected ServerTransaction inviteTid;

		Dialog dialog;
		private boolean inviteSeen;

		private boolean socketDisconnectWorstCase = false;
		public void setSocketDisconnectWorstCase(boolean disconnect) {
			this.socketDisconnectWorstCase = disconnect;
		}

		class ApplicationData {
			protected int ackCount;
		}

		protected static final String usageString =
				"java "
						+ "examples.shootistTLS.Shootist \n"
						+ ">>>> is your class path set to the root?";

		private static void usage() {
			System.out.println(usageString);
			System.exit(0);

		}

		public void processRequest(RequestEvent requestEvent) {
			Request request = requestEvent.getRequest();
			ServerTransaction serverTransactionId =
					requestEvent.getServerTransaction();

			System.out.println(
					"\n\nRequest "
							+ request.getMethod()
							+ " received at "
							+ sipStack.getStackName()
							+ " with server transaction id "
							+ serverTransactionId);

			if (request.getMethod().equals(Request.INVITE)) {
				processInvite(requestEvent, serverTransactionId);
			} else if (request.getMethod().equals(Request.ACK)) {
				processAck(requestEvent, serverTransactionId);
			} else if (request.getMethod().equals(Request.BYE)) {
				processBye(requestEvent, serverTransactionId);
			}

		}

		/** Process the ACK request. Send the bye and complete the call flow.
		 */
		public void processAck(
				RequestEvent requestEvent,
				ServerTransaction serverTransaction) {
			SipProvider sipProvider = (SipProvider) requestEvent.getSource();
			try {
				System.out.println("shootme: got an ACK "
						+ requestEvent.getRequest());
				int ackCount =
						((ApplicationData ) dialog.getApplicationData()).ackCount;
				if (ackCount == 1) {
					dialog = inviteTid.getDialog();
					Request byeRequest = dialog.createRequest(Request.BYE);
					ClientTransaction tr =
							sipProvider.getNewClientTransaction(byeRequest);
					System.out.println("shootme: got an ACK -- sending bye! ");
					dialog.sendRequest(tr);
					System.out.println("Dialog State = " + dialog.getState());
				} else ((ApplicationData) dialog.getApplicationData()).ackCount ++;
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}
		}

		/** Process the invite request.
		 */
		public void processInvite(
				RequestEvent requestEvent,
				ServerTransaction serverTransaction) {
			SipProvider sipProvider = (SipProvider) requestEvent.getSource();
			Request request = requestEvent.getRequest();
			System.out.println("Got an INVITE  " + request);
			if(socketDisconnectWorstCase) {
				((SIPTransactionStack)sipStack).closeAllSockets();
			}
			this.inviteSeen = true;
			try {
				System.out.println("shootme: got an Invite sending OK");
				//System.out.println("shootme:  " + request);
				Response response = messageFactory.createResponse(180, request);
				ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				toHeader.setTag("4321"); // Application is supposed to set.
				Address address =
						addressFactory.createAddress("Shootme <sip:" + myAddress+ ":" + myPort 
								+ ";transport=tls>" );
				ContactHeader contactHeader =
						headerFactory.createContactHeader(address);

				response.addHeader(contactHeader);
				ServerTransaction st = requestEvent.getServerTransaction();

				if (st == null) {
					st = sipProvider.getNewServerTransaction(request);
					if (st.getDialog().getApplicationData() == null) {
						st.getDialog().setApplicationData(new ApplicationData());
					}
				} else {
					System.out.println("This is a RE INVITE ");
					if (st.getDialog() != dialog) {
						System.out.println("Whoopsa Daisy Dialog Mismatch");
						TlsTest.fail("Whoopsa Daisy Dialog Mismatch");

					}
				}

				// Thread.sleep(5000);
				System.out.println("got a server tranasaction " + st);
				byte[] content = request.getRawContent();
				if (content != null) {
					ContentTypeHeader contentTypeHeader =
							headerFactory.createContentTypeHeader("application", "sdp");
					System.out.println("response = " + response);
					response.setContent(content, contentTypeHeader);
				}
				dialog = st.getDialog();
				if (dialog != null) {
					System.out.println("Dialog " + dialog);
					System.out.println("Dialog state " + dialog.getState());
				}
				st.sendResponse(response);
				TransactionExt stExt = ( TransactionExt)st  ;
				Certificate[] certs = stExt.getPeerCertificates();
				System.out.println("Certs = " + certs);
				if(certs != null) {
					for (Certificate cert: certs ) {
						System.out.println("Cert = " + cert);
					}
				}

				response = messageFactory.createResponse(200, request);
				toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				toHeader.setTag("4321"); // Application is supposed to set.
				response.addHeader(contactHeader);
				st.sendResponse(response);
				this.inviteTid = st;
			} catch (Exception ex) {
				ex.printStackTrace();
				TlsTest.fail("Unexpected exception");
			}
		}

		/** Process the bye request.
		 */
		public void processBye(
				RequestEvent requestEvent,
				ServerTransaction serverTransactionId) {
			// SipProvider sipProvider = (SipProvider) requestEvent.getSource();
			Request request = requestEvent.getRequest();
			try {
				System.out.println("shootme:  got a bye sending OK.");
				Response response =
						messageFactory.createResponse(200, request, null, null);
				serverTransactionId.sendResponse(response);
				System.out.println("Dialog State is " + serverTransactionId.getDialog().getState());

			} catch (Exception ex) {
				ex.printStackTrace();
				TlsTest.fail("Unexpected exception");
			}
		}

		public void processResponse(ResponseEvent responseReceivedEvent) {
			System.out.println("Got a response");
			Response response = (Response) responseReceivedEvent.getResponse();
			Transaction tid = responseReceivedEvent.getClientTransaction();

			System.out.println(
					"Response received with client transaction id "
							+ tid
							+ ":\n"
							+ response);
			try {
				if (response.getStatusCode() == Response.OK
						&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
						.getMethod()
						.equals(
								Request.INVITE)) {
					if (tid != this.inviteTid) {
						new Exception().printStackTrace();
						TlsTest.fail("Unexpected exception");
					}
					Dialog dialog = tid.getDialog();
					// Save the tags for the dialog here.
					Request request = tid.getRequest();
					dialog.sendAck(request);
				}
				Dialog dialog = tid.getDialog();
				System.out.println("Dalog State = " + dialog.getState());
			} catch (Exception ex) {
				ex.printStackTrace();
				TlsTest.fail("unexpected exception");
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
			System.out.println(
					"dialogState = " + transaction.getDialog().getState());
			TlsTest.fail("Tx timed out");
		}

		public void init() {
			SipFactory sipFactory = null;
			sipStack = null;
			sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");
			Properties properties = new Properties();
			properties.setProperty("javax.sip.STACK_NAME", "shootme");
			// You need  16 for logging traces. 32 for debug + traces.
			// Your code will limp at 32 but it is best for debugging.
			properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "LOG4J");
			properties.setProperty("gov.nist.javax.sip.TCP_POST_PARSING_THREAD_POOL_SIZE", "20");
			properties.setProperty(
					"gov.nist.javax.sip.DEBUG_LOG",
					"logs/shootmedebug.txt");
			properties.setProperty(
					"gov.nist.javax.sip.SERVER_LOG",
					"logs/shootmelog.txt");
			// Guard against starvation.
			properties.setProperty(
					"gov.nist.javax.sip.READ_TIMEOUT", "1000");
			properties.setProperty(
					"gov.nist.javax.sip.SSL_HANDSHAKE_TIMEOUT", "10000");
			String transport = "tls";
			if(System.getProperty("enableNIO") != null && System.getProperty("enableNIO").equalsIgnoreCase("true")) {
	        	properties.setProperty("gov.nist.javax.sip.MESSAGE_PROCESSOR_FACTORY", NioMessageProcessorFactory.class.getName());
	        }
			try {
				// Create SipStack object
				sipStack = sipFactory.createSipStack(properties);
				System.out.println("sipStack = " + sipStack);
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
				ListeningPoint lpTLS = sipStack.createListeningPoint("127.0.0.1", myPort, transport);

				BadShootme listener = this;

				SipProvider sipProvider = sipStack.createSipProvider(lpTLS);
				System.out.println("tls provider " + sipProvider);
				sipProvider.addSipListener(listener);

			} catch (Exception ex) {
				System.out.println(ex.getMessage());
				ex.printStackTrace();
				usage();
			}

		}

		public void checkState() {
			TlsTest.assertTrue("Invite not seen ", this.inviteSeen );
		}


		public void processIOException(IOExceptionEvent exceptionEvent) {
			TlsTest.fail("IOException was seen");
		}
		public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
			System.out.println("Transaction Terminated event: " + transactionTerminatedEvent );
		}
		public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
			System.out.println("Dialog Terminated event: " + dialogTerminatedEvent);
		}

		public void stop() {
			this.sipStack.stop();
		}

		public static void main(String args[]) throws Exception {
			// setup TLS properties
			System.setProperty( "javax.net.ssl.keyStore",  TlsTest.class.getResource("testkeys").getPath() );
			System.setProperty( "javax.net.ssl.trustStore", TlsTest.class.getResource("testkeys").getPath() );
			System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
			System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
			BadShootme shootme = new BadShootme();
			shootme.init();
		}

	}


}
