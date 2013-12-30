package examples.nistgoodies.leakaudit;

import gov.nist.javax.sip.stack.SIPDialog;
import gov.nist.javax.sip.stack.SIPClientTransaction;
import gov.nist.javax.sip.stack.SIPTransactionStack;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.AddressFactory;
import javax.sip.header.*;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.message.MessageFactory;
import java.util.*;
import java.text.ParseException;

/**
 * This example demonstrates how an application can monitor the SIP Stack
 * for leaked dialogs and transactions.
 * <p/>
 * This code is in the public domain.
 *
 * @author R. Borba (Natural Convergence)
 */
public class LeakingApp implements SipListener {

    /// Dialogs indexed by call id
    private Map dialogs = new HashMap();

    /// Current CSeq
    private long cseq = 0;

    /// Stack objects
    private HeaderFactory headerFactory;
    private MessageFactory messageFactory;
    private AddressFactory addressFactory;
    private AllowHeader ALLOW;

    /// Constructor
    public LeakingApp() {
        SipFactory l_oSipFactory = SipFactory.getInstance();
        try {
            headerFactory = l_oSipFactory.createHeaderFactory();
            messageFactory = l_oSipFactory.createMessageFactory();
            addressFactory = l_oSipFactory.createAddressFactory();
            ALLOW = headerFactory.createAllowHeader(Request.REGISTER + ',' + Request.INVITE + ',' + Request.CANCEL
                    + ',' + Request.ACK + ',' + Request.BYE + ',' + Request.REFER + ',' + Request.INFO);
        }
        catch (Exception e) {
            System.out.println("LeakingApp: Error creating stack objects. Exception:  " + e.toString());
        }
    }

    // ---------------------
    // SipListener interface
    // ---------------------

    public void processRequest(RequestEvent a_oEvent) {
        Request l_oRequest = a_oEvent.getRequest();
        String l_sCallID = ((CallIdHeader) l_oRequest.getHeader(CallIdHeader.NAME)).getCallId();
        System.out.println("processRequest(): CallID=" + l_sCallID +
                ", Request= " + l_oRequest.getMethod());
    }

    public void processResponse(ResponseEvent a_oEvent) {
        Response l_oResponse = a_oEvent.getResponse();
        String l_sCallID = ((CallIdHeader) l_oResponse.getHeader(CallIdHeader.NAME)).getCallId();
        System.out.println("processResponse(): CallID=" + l_sCallID +
                ", Response= " + l_oResponse.toString());
    }

    public void processTimeout(TimeoutEvent a_oEvent) {
        System.out.println("processTimeout()");
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOExceptionEvent: " + exceptionEvent.toString());
    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("TransactionTerminatedEvent: " + transactionTerminatedEvent.toString());
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("DialogTerminatedEvent: " + dialogTerminatedEvent.toString());
    }


    /// Creates a request containing an INVITE message
    private Request createRequest() {
        try {
            CallIdHeader callIdHeader = LeakAudit.sipProvider.getNewCallId();
            CSeqHeader cseqHeader = headerFactory.createCSeqHeader(++cseq, Request.INVITE);
            SipURI requestURI = addressFactory.createSipURI("to-user", "127.0.0.1");
            requestURI.setTransportParam("udp");

            SipURI fromURI = addressFactory.createSipURI("from-user", LeakAudit.sipStack.getIPAddress());
            Address fromAddress = addressFactory.createAddress("from-user", fromURI);
            String fromTag = String.valueOf((int) (Math.random() * 10000));
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, fromTag);

            SipURI toURI = addressFactory.createSipURI("to-user", "127.0.0.1");
            Address toAddress = addressFactory.createAddress("to-user", toURI);
            String toTag = String.valueOf((int) (Math.random() * 10000));
            ToHeader toHeader = headerFactory.createToHeader(toAddress, toTag);

            ListeningPoint listeningPoint = LeakAudit.sipProvider.getListeningPoints()[0];
            ViaHeader viaHeader = headerFactory.createViaHeader(LeakAudit.sipStack.getIPAddress(),
                    listeningPoint.getPort(),
                    listeningPoint.getTransport(),
                    null);

            // add via headers
            ArrayList viaList = new ArrayList();
            viaList.add(viaHeader);

            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

            Request request = messageFactory.createRequest(requestURI,
                    Request.INVITE,
                    callIdHeader,
                    cseqHeader,
                    fromHeader,
                    toHeader,
                    viaList,
                    maxForwards);

            // Add the Contact header
            SipURI sipUri = addressFactory.createSipURI(null, LeakAudit.sipStack.getIPAddress());
            Address contactAddress = addressFactory.createAddress(sipUri);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.addHeader(contactHeader);

            // Add Allow headers
            request.addHeader(ALLOW);

            return request;
        }
        catch (ParseException e) {
            System.err.println("ParseException " + e.getMessage());
        }
        catch (InvalidArgumentException e) {
            System.err.println("InvalidArgumentException " + e.getMessage());
        }
        catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
        }
        return null;
    }


    // Creates a transaction for this request
    private ClientTransaction createTransaction(Request request) {
        try {
            return LeakAudit.sipProvider.getNewClientTransaction(request);
        }
        catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
        }
        return null;
    }


    /// Sends an INVITE request to a dummy location
    public void sendRequest() {
        try {
            // Create the request and the transaction
            Request request = createRequest();
            ClientTransaction transaction = createTransaction(request);

            // Send the request
            // System.out.println("Sending Request: " + request.toString());
            transaction.sendRequest();

            // Remember this call id and this transaction
            CallIdHeader callIdHeader = (CallIdHeader) request.getHeader(CallIdHeader.NAME);
            dialogs.put(callIdHeader.getCallId(), transaction);

            System.out.println("Sent INVITE, Call ID = " + callIdHeader.getCallId());
        }
        catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
        }
    }


    /// Leaks a dialog into the stack
    public void leakDialog() {
        // Insert a foreign dialog from nowhere just so we can detect it
        // Note: Normally we would ask the application to "forget" about one of its call ids by removing one of
        // the entries in the "dialogs" hashmap". However, the stack only creates a dialog for a client transaction
        // *after* the client transaction gets a response. Since our little example is not getting any response for
        // its INVITEs (there's nobody there to respond), removing a dialog from its hashmap doesn't have any effect
        // on this test. So, we have to do it the hard way, i.e., insert a fake dialog by force into the stack and
        // wait until it is detected as leaked.
        // You should see something like this at the console:
        //
        //      Leaked dialogs:
        //          dialog id: 1234, dialog state: null
        //          Total: 1 leaked dialogs detected and removed.
        //
        // Also notice that leaked transactions are a little harder to simulate so I'm not even trying in this example.
        Request request = createRequest();
        SIPClientTransaction transaction = (SIPClientTransaction) createTransaction(request);
        SIPDialog dialog = ((SIPTransactionStack) LeakAudit.sipStack).createDialog(transaction);
        dialog.setDialogId("1234");
        ((SIPTransactionStack) LeakAudit.sipStack).putDialog(dialog);
        System.out.println("Intentionally inserted a leaked dialog, CallID = " + dialog.getCallId());
    }


    /// Returns a list (set) of active call IDs used by this application
    public Set getActiveCallIDs() {
        return dialogs.keySet();
    }
}
