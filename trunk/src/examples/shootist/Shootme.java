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

public class Shootme implements SipListener {

	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;

	protected ServerTransaction inviteTid;

	Dialog dialog;

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
		try {
			System.out.println("shootme: got an Invite sending OK");
			System.out.println("shootme:  " + request);
			Response response = messageFactory.createResponse(200, request);
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
				System.out.println("This is a RE INVITE ");
				if (st.getDialog() != dialog) {
				   System.out.println("Whoopsa Daisy Dialog Mismatch");
				   System.exit(0);
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
			this.inviteTid = st;
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
			System.out.println("shootme:  got a bye sending OK.");
			Response response =
				messageFactory.createResponse(200, request, null, null);
			serverTransactionId.sendResponse(response);
			System.out.println("Dialog State is " + serverTransactionId.getDialog().getState());

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
				+ response);
		try {
			if (response.getStatusCode() == Response.OK
				&& ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
					.getMethod()
					.equals(
					Request.INVITE)) {
				if (tid != this.inviteTid) {
					new Exception().printStackTrace();
					System.exit(0);
				}
				Dialog dialog = tid.getDialog();
				// Save the tags for the dialog here.
				Request request = tid.getRequest();
				dialog.sendAck(request);
			}
			Dialog dialog = tid.getDialog();
			System.out.println("Dalog State = " + dialog.getState());
		} catch (SipException ex) {
			ex.printStackTrace();
			System.exit(0);
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
		System.out.println("Transaction Time out");
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
		properties.setProperty("javax.sip.STACK_NAME", "shootme");
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty(
			"gov.nist.javax.sip.DEBUG_LOG",
			"shootmedebug.txt");
		properties.setProperty(
			"gov.nist.javax.sip.SERVER_LOG",
			"shootmelog.txt");
		properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
		properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

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
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.11  2004/03/05 20:36:54  mranga
 * Reviewed by:   mranga
 * put in some debug printfs and cleaned some things up.
 *
 * Revision 1.10  2004/02/26 14:28:50  mranga
 * Reviewed by:   mranga
 * Moved some code around (no functional change) so that dialog state is set
 * when the transaction is added to the dialog.
 * Cleaned up the Shootist example a bit.
 *
 * Revision 1.9  2004/02/13 13:55:31  mranga
 * Reviewed by:   mranga
 * per the spec, Transactions must always have a valid dialog pointer. Assigned a dummy dialog for transactions that are not assigned to any dialog (such as Message).
 *
 * Revision 1.8  2004/01/22 13:26:27  sverker
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
