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


	// To run on two machines change these to suit.
	public static final String myAddress = "127.0.0.1";
	private static final int myPort    = 5070;

	protected ServerTransaction inviteTid;

	protected ClientTransaction clientTid;

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
			   this.sendReInvite(sipProvider);
			   /*
			   Request byeRequest = dialog.createRequest(Request.BYE);
			   ClientTransaction tr =
				sipProvider.getNewClientTransaction(byeRequest);
			   System.out.println("shootme: got an ACK -- sending bye! ");
			   dialog.sendRequest(tr);
			   System.out.println("Dialog State = " + dialog.getState());
			   */
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
		try {
			System.out.println("shootme: got an Invite sending OK");
			//System.out.println("shootme:  " + request);
			Response response = messageFactory.createResponse(180, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			Address address =
				addressFactory.createAddress("Shootme <sip:" + myAddress+ ":" + myPort + ">");
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
				// If Server transaction is not null, then
				// this is a re-invite.
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
			    System.out.println(" content = " + new String(content));
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
			response = messageFactory.createResponse(200, request);
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); 
			// Application is supposed to set.
			response.addHeader(contactHeader);
			st.sendResponse(response);
			this.inviteTid = st;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public void sendReInvite(SipProvider sipProvider)  throws Exception{
	    Request inviteRequest = dialog.createRequest(Request.INVITE);
	       ((SipURI)inviteRequest.getRequestURI()).removeParameter("transport");
	    ((ViaHeader)inviteRequest.getHeader(ViaHeader.NAME)).setTransport("udp");
	     Address address = addressFactory.createAddress
			("Shootme <sip:" + myAddress+ ":" + myPort + ">");
			ContactHeader contactHeader =
				headerFactory.createContactHeader(address);
	     inviteRequest.addHeader(contactHeader);
	     ClientTransaction ct = sipProvider.getNewClientTransaction(inviteRequest);
	    this.clientTid = ct;
	    dialog.sendRequest(ct);
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
				messageFactory.createResponse(200, request);
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
				Dialog dialog = tid.getDialog();
				Request request = dialog.createRequest(Request.ACK);
				dialog.sendAck(request);
			}
			Dialog dialog = tid.getDialog();
			System.out.println("Dalog State = " + dialog.getState());
		} catch (Exception ex) {
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
		properties.setProperty("javax.sip.IP_ADDRESS", myAddress );
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
		// Guard against starvation.
		properties.setProperty(
			"gov.nist.javax.sip.READ_TIMEOUT", "1000");
		// properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "4096");
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
 * Revision 1.23  2005/03/07 19:05:04  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * change ip address and port to be manifest constants
 *
 * Revision 1.22  2005/01/20 17:31:12  mranga
 * Reviewed by:   mranga
 * added something to get content in example
 *
 * Revision 1.21  2004/12/01 19:05:14  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
 * Revision 1.20  2004/09/26 14:48:01  mranga
 * Submitted by:  John Martin
 * Reviewed by:   mranga
 *
 * Remove unnecssary synchronization.
 *
 * Revision 1.19  2004/06/16 02:53:17  mranga
 * Submitted by:  mranga
 * Reviewed by:   implement re-entrant multithreaded listener model.
 *
 * Revision 1.18  2004/06/15 09:54:39  mranga
 * Reviewed by:   mranga
 * re-entrant listener model added.
 * (see configuration property gov.nist.javax.sip.REENTRANT_LISTENER)
 *
 * Revision 1.17  2004/05/16 14:13:20  mranga
 * Reviewed by:   mranga
 * Fixed the use-count issue reported by Peter Parnes.
 * Added property to prevent against content-length dos attacks.
 *
 * Revision 1.16  2004/04/07 13:46:30  mranga
 * Reviewed by:   mranga
 * move processing of delayed responses outside the synchronized block.
 *
 * Revision 1.15  2004/04/07 00:19:22  mranga
 * Reviewed by:   mranga
 * Fixes a potential race condition for client transactions.
 * Handle re-invites statefully within an established dialog.
 *
 * Revision 1.14  2004/03/30 18:10:53  mranga
 * Reviewed by:   mranga
 * added code to demonstrate cleanup
 *
 * Revision 1.13  2004/03/12 21:53:08  mranga
 * Reviewed by:   mranga
 * moved some comments around for ifdef support.
 *
 * Revision 1.12  2004/03/07 22:25:22  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
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
