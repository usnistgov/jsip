package test.load.concurrency;
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

public class Shootme implements SipListener {

	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;



	class ApplicationData {
		protected int ackCount;
	}

	protected static final String usageString =
		"java "
			+ "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	private static void usage() {
		System.out.println(usageString);
		System.exit(0);

	}

	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId =
			requestEvent.getServerTransaction();

		/**
		System.out.println(
			"\n\nRequest "
				+ request.getMethod()
				+ " received at "
				+ sipStack.getStackName()
				+ " with server transaction id "
				+ serverTransactionId);
		**/

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
			// System.out.println("shootme: got an ACK " );
			// maybe a late arriving ack.
			if (serverTransaction == null) return;
			Dialog dialog = serverTransaction.getDialog();
			int ackCount = 
				((ApplicationData ) dialog.getApplicationData()).ackCount;
			if (ackCount == 1) {
			   dialog = serverTransaction.getDialog();
			   Request byeRequest = dialog.createRequest(Request.BYE);
			   ClientTransaction tr =
				sipProvider.getNewClientTransaction(byeRequest);
			   //System.out.println("shootme: got an ACK -- sending bye! ");
			   dialog.sendRequest(tr);
			   //System.out.println("Dialog State = " + dialog.getState());
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
		//System.out.println("Got an INVITE  " + request);
		try {
			//System.out.println("shootme: got an Invite sending OK");
			//System.out.println("shootme:  " + request);
			Response response = messageFactory.createResponse(180, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
//ifdef SIMULATION
/*
					Address address = addressFactory.createAddress(
					"Shootme <sip:129.6.55.62:5070>");
//else
*/
			Address address =
				addressFactory.createAddress("Shootme <sip:127.0.0.1:5070>");
//endif
//
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
				//System.out.println("This is a RE INVITE ");
			}

			// Thread.sleep(5000);
			//System.out.println("got a server tranasaction " + st);
			byte[] content = request.getRawContent();
			if (content != null) {
			    ContentTypeHeader contentTypeHeader =
				headerFactory.createContentTypeHeader("application", "sdp");
			    // System.out.println("response = " + response);
			    response.setContent(content, contentTypeHeader);
			}
			Dialog dialog = st.getDialog();
			st.sendResponse(response);
			response = messageFactory.createResponse(200, request);
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			response.addHeader(contactHeader);
			st.sendResponse(response);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	/** Process the bye request.
	 */
	public void processBye(
		RequestEvent requestEvent,
		ServerTransaction serverTransactionId) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			Response response =
				messageFactory.createResponse(200, request, null, null);
			serverTransactionId.sendResponse(response);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {
		Response response = (Response) responseReceivedEvent.getResponse();
		Transaction tid = responseReceivedEvent.getClientTransaction();

		try {
			if (response.getStatusCode() == Response.OK
				&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
					.getMethod()
					.equals(
					Request.INVITE)) {
				Dialog dialog = tid.getDialog();
				// Save the tags for the dialog here.
				Request request = tid.getRequest();
				dialog.sendAck(request);
			}
			Dialog dialog = tid.getDialog();
		} catch (SipException ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		Request request = null;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
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
		System.exit(0);
	}

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
//ifdef SIMULATION
/*
		        properties.setProperty("javax.sip.IP_ADDRESS","129.6.55.62");
//else
*/
		properties.setProperty("javax.sip.IP_ADDRESS", "127.0.0.1");
//endif
//
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		properties.setProperty("javax.sip.REENTRANT_LISTENER", "true");
		properties.setProperty("javax.sip.STACK_NAME", "shootme");
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"shootmedebug.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"shootmelog.txt");
		// Guard against starvation.
		properties.setProperty(
			"gov.nist.javax.sip.READ_TIMEOUT", "1000");
		// properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
		properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "true");

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
			ListeningPoint lp = sipStack.createListeningPoint(5070, "udp");
			ListeningPoint lp1 = sipStack.createListeningPoint(5070, "tcp");

			Shootme listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			System.out.println("udp provider " + sipProvider);
			sipProvider.addSipListener(listener);
			sipProvider = sipStack.createSipProvider(lp1);
			System.out.println("tcp provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}

	}

	public static void main(String args[]) {
		new Shootme().init();
	}

}
