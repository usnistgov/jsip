package test.load.concurrency;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.io.*;



//ifdef SIMULATION
/*
import sim.java.*;
//endif
*/

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
	private ContactHeader contactHeader;
	private ListeningPoint tcpListeningPoint;
	private ListeningPoint udpListeningPoint;
	private SipProvider sipProvider;
	private String transport;
	int byeCount;
	int ackCount;
	private static int NDIALOGS = 100;

	// Keeps track of successful dialog completion.
	private static Timer timer;

	static {
	       timer = new Timer();
	}



	class TTask extends TimerTask {
		Dialog dialog;
		public TTask(Dialog dialog) {
			this.dialog = dialog;
		} 
		public void run() {
		     if (dialog.getState() != DialogState.TERMINATED) {
			  System.out.println("BYE not received for " +
				this.dialog);
			  System.out.println("State " +
				this.dialog.getState());
			  System.out.println("dialogId " +
				this.dialog.getDialogId());
			  Appdata appData = (Appdata) dialog.getApplicationData();
			  System.out.println("reInviteCount " + 
					appData.reInviteCount);
			  
			  System.out.println("ackCount " + appData.ackCount);
			  ((gov.nist.javax.sip.stack.DialogImpl) dialog).printDebugInfo();
			  System.exit(0);
		     }
		}
	}
			
			


	class Appdata {
	       protected int reInviteCount;
	       protected TimerTask ttask;
	       protected long startTime;
	       protected long endTime;
	       protected int  ackCount;
	     
	       Appdata(Dialog dialog) {
			ttask = new TTask(dialog);
			timer.schedule(ttask,64*1000);
			startTime = System.currentTimeMillis();
		}

		public void cancelTimer() {
			this.ttask.cancel();
			endTime = System.currentTimeMillis();
		}
	}

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
				Thread.sleep(4000);
		     	} catch (InterruptedException e) {
		     	}
			System.out.println("nulling reference");
			this.sipStack.deleteListeningPoint(tcpListeningPoint);
			this.sipStack.deleteListeningPoint(udpListeningPoint);
			// This will close down the stack and exit all threads
			tcpProvider.removeSipListener(this);
			udpProvider.removeSipListener(this);
			while (true) {
			  try {
			      this.sipStack.deleteSipProvider(udpProvider);
			      this.sipStack.deleteSipProvider(tcpProvider);
			      break;
			    } catch (ObjectInUseException  ex)  {
			        try {  
					Thread.sleep(2000);
			     	} catch (InterruptedException e) {
					continue;
			     	}
			   }
			}
			this.sipStack = null;
			this.tcpProvider = null;
			this.udpProvider = null;
			this.contactHeader = null;
			this.addressFactory = null;
			this.headerFactory = null;
			this.messageFactory = null;
			this.udpListeningPoint = null;
			this.tcpListeningPoint = null;
			this.byeCount = 0;
			System.gc();
			this.init();
			for (int i = 0; i < NDIALOGS; i++ ) 
				this.sendInvite();
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId =
			requestReceivedEvent.getServerTransaction();

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);

	}

	public  void processBye(
		Request request,
		ServerTransaction serverTransactionId) {
		try {
			if (serverTransactionId == null) {
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			Response response = messageFactory.createResponse
						(200, request);
			serverTransactionId.sendResponse(response);

			// so that the finalization method will run 
			// and exit all resources.

			// handled both byes then shutdown

			Appdata appdata = (Appdata) dialog.getApplicationData();
			appdata.cancelTimer();
		        if ( ++this.byeCount == NDIALOGS)  { 
					this.shutDown();
			} 

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {
		Response response = (Response) responseReceivedEvent.getResponse();
		Transaction tid = responseReceivedEvent.getClientTransaction();

		if (tid == null) {
			return;
		}

		/**
		if (response.getStatusCode() == 100 || response.getStatusCode() == 180) {
				try {
				    Thread.sleep(500);
				} catch (Exception ex) {
				}
		}
		**/
		try {
			if (response.getStatusCode() == Response.OK
				&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
					.getMethod()
					.equals(
					Request.INVITE)) {
				// Request cancel = inviteTid.createCancel();
				// ClientTransaction ct = 
				//	sipProvider.getNewClientTransaction(cancel);
				// ct.sendRequest();
				Dialog dialog = tid.getDialog();
				Request ackRequest = dialog.createRequest(Request.ACK);
				dialog.sendAck(ackRequest);

				// Send a Re INVITE but this time force it 
				// to use UDP as the transport. Else, it will
				// Use whatever transport was used to create
				// the dialog.
				Appdata appData = (Appdata) 
					dialog.getApplicationData();
				appData.ackCount ++;

			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out" );
		Request request = null;
		Transaction transaction = null;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
			request = ((ServerTransaction) transaction).getRequest();
		} else {
			transaction = timeoutEvent.getClientTransaction();
			request = ((ClientTransaction) transaction).getRequest();
		}
		System.out.println("state = " + transaction.getState());
		System.out.println("dialog = " + transaction.getDialog());
		System.out.println(
			"dialogState = " + transaction.getDialog().getState());
		System.out.println("Transaction Time out");
		System.out.println("Transaction " + transaction);
		System.out.println("request " + request);
		
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		// If you want to try TCP transport change the following to
		this.transport = "udp";
//ifdef SIMULATION
/*
		        properties.setProperty("javax.sip.IP_ADDRESS"
		        ,"129.6.55.61");
		        properties.setProperty("javax.sip.OUTBOUND_PROXY"
		        ,"129.6.55.62:5070/" + transport);
//else
*/
		properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
		properties.setProperty(
			"javax.sip.OUTBOUND_PROXY",
			"127.0.0.1:5070/" + transport);
		// If you want to use UDP then uncomment this.
//endif
//

		properties.setProperty(
			"javax.sip.ROUTER_PATH",
			"examples.shootist.MyRouter");
		properties.setProperty("javax.sip.STACK_NAME", "shootist");
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		properties.setProperty("javax.sip.MAX_MESSAGE_SIZE", "1048576");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"shootistdebug.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"shootistlog.txt");
		// Drop the client connection after we are done with the transaction.
		properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "true");
		properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
		// Set to 0 in your production code for max speed.
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");

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
			udpListeningPoint = sipStack.createListeningPoint(5060, "udp");
			udpProvider = sipStack.createSipProvider(udpListeningPoint);
			Shootist listener = this;
			udpProvider.addSipListener(listener);




			tcpListeningPoint = sipStack.createListeningPoint(5060, "tcp");
			tcpProvider = sipStack.createSipProvider(tcpListeningPoint);
			tcpProvider.addSipListener(listener);

			sipProvider = transport.equals("udp")? 
					udpProvider: tcpProvider;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public void sendInvite() {
		try {

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
			
			String tag = new Integer((int)(Math.random() * 10000)).toString();
			FromHeader fromHeader =
				headerFactory.createFromHeader(fromNameAddress, tag);

			// create To Header
			SipURI toAddress =
				addressFactory.createSipURI(toUser, toSipAddress);
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader =
				headerFactory.createToHeader(toNameAddress, null);

			// create Request URI
			SipURI requestURI =
				addressFactory.createSipURI(toUser, toSipAddress);

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
/**
			StringBuffer sdpBuff = new StringBuffer();
			for (int i = 0; i < 500; i++)  {
				sdpBuff.append(sdp);
			}
			String sdpData = sdpBuff.toString();
			File dataFile = new File("data.txt");
			long length = dataFile.length();
		        FileInputStream fis = new FileInputStream(dataFile);
			byte[] contents = new byte[(int)length];
			fis.read(contents);
			System.out.println("length = " + contents.length);
**/
			byte[]  contents = sdpData.getBytes();
			//byte[]  contents = sdpBuff.toString().getBytes();

			request.setContent(contents, contentTypeHeader);

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
			ClientTransaction inviteTid = sipProvider.getNewClientTransaction(request);

			// send the request out.
			inviteTid.sendRequest();
			Dialog dialog = inviteTid.getDialog();

			// Set a pointer to our application data
			Appdata appdata = new Appdata(dialog);
			dialog.setApplicationData(appdata);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}

	public static void main(String args[]) {
		Shootist shootist = new Shootist();
		shootist.init();
		for (int i = 0 ; i < NDIALOGS; i++ )  {
			try {
				Thread.sleep(100);
			 } catch (Exception ex) {}
			shootist.sendInvite();
		}
		

	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/06/16 02:53:20  mranga
 * Submitted by:  mranga
 * Reviewed by:   implement re-entrant multithreaded listener model.
 *
 * Revision 1.1  2004/06/15 09:54:47  mranga
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
