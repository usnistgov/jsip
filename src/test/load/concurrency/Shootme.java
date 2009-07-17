package test.load.concurrency;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import junit.framework.TestCase;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme
 * is the guy that gets shot.
 *
 *@author M. Ranganathan
 */

public class Shootme extends TestCase implements SipListener {

    static AddressFactory addressFactory;
    static MessageFactory messageFactory;
    static HeaderFactory headerFactory;
    static SipStack sipStack;

    static String transport;




    protected static final String usageString =
        "java "
            + "examples.shootist.Shootist \n"
            + ">>>> is your class path set to the root?";


    private int terminationCount;
    private int droppedCall;

    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId =
            requestEvent.getServerTransaction();



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
                dialog = serverTransaction.getDialog();
            Request byeRequest = dialog.createRequest(Request.BYE);
            ClientTransaction tr =
                sipProvider.getNewClientTransaction(byeRequest);
            dialog.sendRequest(tr);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception");
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
            Response response = messageFactory.createResponse(180, request);
            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            String toTag = new Integer((int) (Math.random() * 1000)).toString();
            toHeader.setTag(toTag); // Application is supposed to set.
            //System.out.println("toTag = " + toTag);

            Address address =
                addressFactory.createAddress("Shootme <sip:127.0.0.1:5070;transport=" + transport + ">");

            ContactHeader contactHeader =
                headerFactory.createContactHeader(address);
            response.addHeader(contactHeader);
            ServerTransaction st = requestEvent.getServerTransaction();

            if (st == null) {
                try {
                    st = sipProvider.getNewServerTransaction(request);
                } catch (TransactionAlreadyExistsException ex) {
                    System.out.println("Tx aready exists -- ignoring ");
                    return;
                }


            }
            byte[] content = request.getRawContent();
            if (content != null) {
                ContentTypeHeader contentTypeHeader =
                headerFactory.createContentTypeHeader("application", "sdp");
                // System.out.println("response = " + response);
                response.setContent(content, contentTypeHeader);
            }
            Dialog dialog = st.getDialog();
            //System.out.println("dialog = " + dialog);
            st.sendResponse(response);
            response = messageFactory.createResponse(200, request);
            toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
            toHeader.setTag(toTag); // Application is supposed to set.
            response.addHeader(contactHeader);
            st.sendResponse(response);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception");
        }
    }

    /** Process the bye request.
     */
    public void processBye(
        RequestEvent requestEvent,
        ServerTransaction serverTransactionId) {
        SipProvider sipProvider = (SipProvider) requestEvent.getSource();
        System.out.println("sip provider = " + sipProvider);

        Request request = requestEvent.getRequest();
        try {
            Response response =
                messageFactory.createResponse(200, request, null, null);
            serverTransactionId.sendResponse(response);

        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception");

        }
    }

    public void processResponse(ResponseEvent responseReceivedEvent) {
        Response response = (Response) responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();
        if (tid == null)  {
            System.out.println("Stray response -- dropping!");
            return;
        }

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
            assertTrue("Dialog should not be null",dialog != null);
            //System.out.println("dialog = " + dialog);
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
        /*
         * System.out.println("request = " + request); System.out.println("state = " +
         * transaction.getState()); System.out.println("dialog = " +
         * transaction.getDialog()); System.out.println( "dialogState = " +
         * transaction.getDialog().getState()); System.out.println("Transaction
         * Time out"); System.out.println("Transaction " + transaction);
         * System.out.println("request " + transaction.getRequest());
         */
        this.droppedCall ++;
        System.out.println("droped call " + this.droppedCall);
    }

    public SipProvider createSipProvider() throws Exception {
        ListeningPoint listeningPoint = sipStack.createListeningPoint("127.0.0.1", 5070, transport);
        SipProvider sipProvider = sipStack.createSipProvider(listeningPoint);
        return sipProvider;
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.out.println("IOException occured while retransmitting requests:" + exceptionEvent);
    }
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        //System.out.println("Transaction Terminated event: " + transactionTerminatedEvent );
    }
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        //System.out.println("Dialog Terminated event: " + dialogTerminatedEvent);
        this.terminationCount ++;

        if (terminationCount %100 == 0 ) System.out.println("DialogTermination count = " + this.terminationCount);
    }

    public static void main(String args[]) throws Exception {
        ProtocolObjects.init("shootme", true);
        Shootme.addressFactory = ProtocolObjects.addressFactory;
        Shootme.messageFactory = ProtocolObjects.messageFactory;
        Shootme.headerFactory = ProtocolObjects.headerFactory;
        Shootme.sipStack = ProtocolObjects.sipStack;
        Shootme.transport = ProtocolObjects.transport;
        Shootme shootme = new Shootme();
        SipProvider sipProvider = shootme.createSipProvider();
        sipProvider.addSipListener(shootme);
    }

}
