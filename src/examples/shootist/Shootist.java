package examples.shootist;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;



/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme 
 * is the guy that gets shot.
 *
 *@author M. Ranganathan
 */

public class Shootist implements SipListener {

	private static SipProvider tcpProvider;
	private static SipProvider udpProvider;
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private int reInviteCount;
	private ContactHeader contactHeader;
	private ListeningPoint tcpListeningPoint;
	private ListeningPoint udpListeningPoint;
	private int counter;
	private static String PEER_ADDRESS = Shootme.myAddress;
	
	// To run on two machines change these to suit.
	public static final String myAddress = "127.0.0.1";
	private static final int myPort = 5060;

	protected ClientTransaction inviteTid;

	protected static final String usageString =
		"java "
			+ "examples.shootist.Shootist \n"
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
			sipStack.deleteListeningPoint(tcpListeningPoint);
			sipStack.deleteListeningPoint(udpListeningPoint);
			// This will close down the stack and exit all threads
			tcpProvider.removeSipListener(this);
			udpProvider.removeSipListener(this);
			while (true) {
			  try {
			      sipStack.deleteSipProvider(udpProvider);
			      sipStack.deleteSipProvider(tcpProvider);
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
			tcpProvider = null;
			udpProvider = null;
			this.inviteTid = null;
			this.contactHeader = null;
			addressFactory = null;
			headerFactory = null;
			messageFactory = null;
			this.udpListeningPoint = null;
			this.tcpListeningPoint = null;
			this.reInviteCount = 0;
			System.gc();
			//Redo this from the start.
			if (counter < 10 ) 
				this.init();
			else counter ++;
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

		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);
		else if (request.getMethod().equals(Request.INVITE))
			processInvite(request, serverTransactionId);
		else if ( request.getMethod().equals(Request.ACK) ) 
			processAck(request, serverTransactionId);

	}

	public void processInvite( Request request, ServerTransaction st ) {
		try {
			Dialog dialog = st.getDialog();
			Response response = messageFactory.createResponse(Response.OK,request);
			((ToHeader) response.getHeader(ToHeader.NAME)).setTag(((ToHeader)request.getHeader(ToHeader.NAME)).getTag());



			Address address =
				addressFactory.createAddress("Shootme <sip:" + myAddress+ ":" + myPort + ">");
			ContactHeader contactHeader =
				headerFactory.createContactHeader(address);
			response.addHeader(contactHeader);
			st.sendResponse( response );
		} catch ( Exception ex ) {
			ex.printStackTrace();
			System.exit(0);
		}
	}


	public void processAck( Request request, ServerTransaction tid ) {
	   try {
		System.out.println("Got an ACK! sending bye : " + tid);
		Dialog dialog = tid.getDialog();
		Request bye = dialog.createRequest(Request.BYE);
		SipProvider provider =  udpProvider;
		ClientTransaction ct = provider.getNewClientTransaction(bye);
		dialog.sendRequest(ct);
	   } catch ( Exception ex) {
		ex.printStackTrace();
		System.exit(0);
	   }
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
				// Request cancel = inviteTid.createCancel();
				// ClientTransaction ct = 
				//	sipProvider.getNewClientTransaction(cancel);
				Dialog dialog = tid.getDialog();
				Request ackRequest = dialog.createRequest(Request.ACK);
				System.out.println("Sending ACK");
				dialog.sendAck(ackRequest);

				// Send a Re INVITE but this time force it 
				// to use UDP as the transport. Else, it will
				// Use whatever transport was used to create
				// the dialog.
				if (reInviteCount == 0) {
				    Request inviteRequest = 
					dialog.createRequest(Request.INVITE);
				    ((SipURI)inviteRequest.getRequestURI()).removeParameter("transport");
				    ((ViaHeader)inviteRequest.getHeader(ViaHeader.NAME)).setTransport("udp");
				    inviteRequest.addHeader(contactHeader);
				    try {Thread.sleep(100); } catch (Exception ex) {} 
				    ClientTransaction ct = 
					udpProvider.getNewClientTransaction(inviteRequest);
				    dialog.sendRequest(ct);
				    reInviteCount ++;
				}

			} else if ( response.getStatusCode() == Response.OK
				&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
					.getMethod()
					.equals(
					Request.BYE)) {
			         this.shutDown();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out" );
		System.out.println("TimeoutEvent " + timeoutEvent.getTimeout());
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		// If you want to try TCP transport change the following to
		String transport = "udp";
		String peerHostPort = PEER_ADDRESS+":5070";
		properties.setProperty("javax.sip.IP_ADDRESS", myAddress
);
		properties.setProperty(
			"javax.sip.OUTBOUND_PROXY", 
			peerHostPort + "/" + transport);
		// If you want to use UDP then uncomment this.
		properties.setProperty(
			"javax.sip.ROUTER_PATH",
			"examples.shootist.MyRouter");
		properties.setProperty("javax.sip.STACK_NAME", "shootist");
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", 
				"on");

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
			udpListeningPoint = sipStack.createListeningPoint
							(myPort, "udp");
			udpProvider = sipStack.createSipProvider
							(udpListeningPoint);
			Shootist listener = this;
			udpProvider.addSipListener(listener);




			tcpListeningPoint = sipStack.createListeningPoint
								(myPort, "tcp");
			tcpProvider = sipStack.createSipProvider(tcpListeningPoint);
			tcpProvider.addSipListener(listener);

			SipProvider sipProvider = transport.equalsIgnoreCase("udp")? 
					udpProvider: tcpProvider;

			String fromName = "BigGuy";
			String fromSipAddress = "here.com";
			String fromDisplayName = "The Master Blaster";

			String toSipAddress = "there.com";
			String toUser = "LittleGuy";
			String toDisplayName = "The Little Blister";

			// create >From Header
			SipURI fromAddress =
				addressFactory.createSipURI(fromName, fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader =
				headerFactory.createFromHeader(fromNameAddress, "12345");

			// create To Header
			SipURI toAddress =
				addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader =
				headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI =
				addressFactory.createSipURI(toUser, peerHostPort);

			// Create ViaHeaders

			ArrayList viaHeaders = new ArrayList();
			int port = sipProvider.getListeningPoint().getPort();
			ViaHeader viaHeader =
				headerFactory.createViaHeader(
					sipStack.getIPAddress(),
					sipProvider.getListeningPoint().getPort(),
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
				headerFactory.createCSeqHeader(1, Request.INVITE);

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
			String host = sipStack.getIPAddress();

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(tcpListeningPoint.getPort());

			// Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint().getPort());

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

			request.setContent(sdpData, contentTypeHeader);

			extensionHeader =
				headerFactory.createHeader(
					"My-Other-Header",
					"my new header value ");
			request.addHeader(extensionHeader);

			Header callInfoHeader =
				headerFactory.createHeader(
					"Call-Info",
					"<http://www.antd.nist.gov>");
			request.addHeader(callInfoHeader);


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
		new Shootist().init();

	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.35  2005/03/14 15:39:53  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Minor tweak
 *
 * Revision 1.34  2005/03/07 19:05:04  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * change ip address and port to be manifest constants
 *
 * Revision 1.33  2005/01/20 17:31:12  mranga
 * Reviewed by:   mranga
 * added something to get content in example
 *
 * Revision 1.32  2004/12/01 19:05:14  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
 * Revision 1.31  2004/08/30 14:33:53  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Revision 1.30  2004/07/23 06:50:04  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Clean up - Get rid of annoying eclipse warnings.
 *
 * Revision 1.29  2004/06/27 00:41:51  mranga
 * Submitted by:  Thomas Froment and Pierre De Rop
 * Reviewed by:   mranga
 * Performance improvements
 * (auxiliary data structure for fast lookup of transactions).
 *
 * Revision 1.28  2004/06/15 09:54:38  mranga
 * Reviewed by:   mranga
 * re-entrant listener model added.
 * (see configuration property gov.nist.javax.sip.REENTRANT_LISTENER)
 *
 * Revision 1.27  2004/05/30 18:55:56  mranga
 * Reviewed by:   mranga
 * Move to timers and eliminate the Transaction scanner Thread
 * to improve scalability and reduce cpu usage.
 *
 * Revision 1.26  2004/05/16 14:13:20  mranga
 * Reviewed by:   mranga
 * Fixed the use-count issue reported by Peter Parnes.
 * Added property to prevent against content-length dos attacks.
 *
 * Revision 1.25  2004/04/07 18:56:05  mranga
 * Reviewed by:   got rid of confusing sleep
 *
 * Revision 1.24  2004/04/07 13:46:29  mranga
 * Reviewed by:   mranga
 * move processing of delayed responses outside the synchronized block.
 *
 * Revision 1.23  2004/03/30 18:10:53  mranga
 * Reviewed by:   mranga
 * added code to demonstrate cleanup
 *
 * Revision 1.22  2004/03/18 14:40:37  mranga
 * Reviewed by:   mranga
 * Removed event scanning thread from provider and added a single class that
 * scans for events and delivers to the listener (previously each provider had
 * its own scanning thread).
 * Added code in stack finalization to exit all threads and release all resources
 * held by the stack.
 *
 * Revision 1.21  2004/03/12 21:54:32  mranga
 * Reviewed by:   mranga
 * minor re-arrangement
 *
 * Revision 1.20  2004/03/12 21:53:08  mranga
 * Reviewed by:   mranga
 * moved some comments around for ifdef support.
 *
 * Revision 1.19  2004/03/09 00:34:42  mranga
 * Reviewed by:   mranga
 * Added TCP connection management for client and server side
 * Transactions. See configuration parameter
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false
 * Releases Client TCP Connections after linger time
 *
 * Revision 1.18  2004/03/07 22:25:21  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.17  2004/03/05 20:36:54  mranga
 * Reviewed by:   mranga
 * put in some debug printfs and cleaned some things up.
 *
 * Revision 1.16  2004/02/26 14:28:50  mranga
 * Reviewed by:   mranga
 * Moved some code around (no functional change) so that dialog state is set
 * when the transaction is added to the dialog.
 * Cleaned up the Shootist example a bit.
 *
 * Revision 1.15  2004/02/13 13:55:31  mranga
 * Reviewed by:   mranga
 * per the spec, Transactions must always have a valid dialog pointer. Assigned a dummy dialog for transactions that are not assigned to any dialog (such as Message).
 *
 * Revision 1.14  2004/01/22 13:26:27  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
