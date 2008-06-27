	package examples.tls;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme 
 * is the guy that gets shot.
 *
 *@author Daniel Martinez
 */

public class Shootme implements SipListener {
  
	private static AddressFactory addressFactory;
	private static MessageFactory messageFactory;
	private static HeaderFactory headerFactory;
	private static SipStack sipStack;
	private static final String myAddress = "127.0.0.1";
	private static final int myPort    = 5071;

	protected ServerTransaction inviteTid;

	Dialog dialog;

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
		try {
			System.out.println("shootme: got an Invite sending OK");
			//System.out.println("shootme:  " + request);
			Response response = messageFactory.createResponse(180, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			Address address =
				addressFactory.createAddress("Shootme <sip:" + myAddress+ ":" + myPort + ">"
				/*+ ";transport=tls>"*/ );
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
			response = messageFactory.createResponse(200, request);
			toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag("4321"); // Application is supposed to set.
			response.addHeader(contactHeader);
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
//ifdef SIMULATION
/*
		        properties.setProperty("javax.sip.IP_ADDRESS","129.6.55.62");
//else
*/
		properties.setProperty("javax.sip.IP_ADDRESS", myAddress );
//endif
//
		properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
		properties.setProperty("javax.sip.STACK_NAME", "shootme");
		// You need  16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
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
			ListeningPoint lpTLS = sipStack.createListeningPoint(sipStack.getIPAddress(), myPort, "tcp");

			Shootme listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lpTLS);
			System.out.println("tls provider " + sipProvider);
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

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException occured while retransmitting requests:" + exceptionEvent);
	}
	public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction Terminated event: " + transactionTerminatedEvent );
	}
	public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("Dialog Terminated event: " + dialogTerminatedEvent);
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2007/02/12 19:45:45  jbemmel
 * use sips
 *
 * Revision 1.5  2006/12/13 15:15:28  mranga
 * Issue number:  90
 * Obtained from:
 * Submitted by:  Zohair
 * Reviewed by:  mranga
 *
 * Fixed transport = tls in the example.
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
 * Revision 1.4  2006/07/13 09:02:54  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
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
 * Revision 1.2  2006/01/16 21:36:07  ivelin
 * cleaned up usage of deprecated APIs, fixed some script paths and stale imports
 *
 * Revision 1.1.1.1  2005/10/04 17:12:33  mranga
 *
 * Import
 *
 *
 * Revision 1.1  2004/11/24 21:00:38  mranga
 *
 * Added
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
