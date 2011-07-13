package test.tck.msgflow.callflows.recroute;

import java.util.Hashtable;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import test.tck.TestHarness;
import test.tck.msgflow.callflows.ProtocolObjects;

/**
 * A very simple Record-Routing proxy server.
 *
 * @author M. Ranganathan
 * @author Jeroen van Bemmel
 *
 */
public class Proxy extends TestHarness implements SipListener {

    // private ServerTransaction st;

    private static String host = "127.0.0.1";

    private int port = 5070;

    private SipProvider sipProvider;

    private static String unexpectedException = "Unexpected exception";

    private static Logger logger = Logger.getLogger("test.tck");

    private ProtocolObjects protocolObjects;

    private boolean ackSeen;

    private boolean inviteSeen;

    private boolean byeSeen;

    private int infoCount = 0;


    public void checkState() {
        TestHarness.assertTrue("INVITE should be seen by proxy", inviteSeen);
        TestHarness.assertTrue("Should see two INFO messages", infoCount == 2);
        TestHarness.assertTrue("BYE should be seen by proxy", byeSeen);
        TestHarness.assertTrue("ACK should be seen by proxy", ackSeen);
    }

    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            if (request.getMethod().equals(Request.INVITE)) {
                inviteSeen = true;

                ListeningPoint lp = sipProvider
                        .getListeningPoint(protocolObjects.transport);
                String host = lp.getIPAddress();
                int port = lp.getPort();

                ServerTransaction st = null;
                if (requestEvent.getServerTransaction() == null) {
                    st = sipProvider.getNewServerTransaction(request);
                }
                Request newRequest = (Request) request.clone();

                //
                // Add a Route: header to 5080
                //
                SipURI sipUri = protocolObjects.addressFactory.createSipURI(
                        "UA1", "127.0.0.1");
                sipUri.setPort(5080);
                sipUri.setLrParam();
                sipUri.setTransportParam( protocolObjects.transport );
                Address address = protocolObjects.addressFactory.createAddress(
                        "client1", sipUri);
                RouteHeader rheader = protocolObjects.headerFactory
                        .createRouteHeader(address);
                newRequest.addFirst(rheader);

                //
                // Add a Via header + Record-Route
                //
                ViaHeader viaHeader = protocolObjects.headerFactory
                        .createViaHeader(host, port, protocolObjects.transport,
                                null);
                newRequest.addFirst(viaHeader);

                ClientTransaction ct1 = sipProvider
                        .getNewClientTransaction(newRequest);

                sipUri = protocolObjects.addressFactory.createSipURI("proxy",
                        "127.0.0.1");
                address = protocolObjects.addressFactory.createAddress("proxy",
                        sipUri);
                sipUri.setPort(5070);
                sipUri.setLrParam();
                sipUri.setTransportParam(protocolObjects.transport);

                RecordRouteHeader recordRoute = protocolObjects.headerFactory
                        .createRecordRouteHeader(address);
                newRequest.addHeader(recordRoute);
                ct1.setApplicationData(st);

                // Send the request out to the two listening point of the
                // client.
                ct1.sendRequest();

                TestHarness.assertNull(ct1.getDialog());
            } else if (request.getMethod().equals(Request.ACK)) {
                Request newRequest = (Request) request.clone();
                newRequest.removeFirst(RouteHeader.NAME);
                ViaHeader viaHeader = protocolObjects.headerFactory
                        .createViaHeader(host, port, protocolObjects.transport,
                                null);
                newRequest.addFirst(viaHeader);
                this.ackSeen = true;
                logger.debug("PROXY : sendingAck "  + newRequest);
                sipProvider.sendRequest(newRequest);
            } else {
                // Remove the topmost route header
                // The route header will make sure it gets to the right place.
                logger.debug("proxy: Got a request\n" + request);
                if (request.getMethod().equals(Request.BYE)) {
                    this.byeSeen = true;
                }
                if (request.getMethod().equals(Request.INFO)) {
                    this.infoCount++;
                }

                if (requestEvent.getServerTransaction() == null) {
                    TestHarness.assertNull("Dialog should be null",
                            requestEvent.getDialog());
                    ServerTransaction stx = sipProvider
                            .getNewServerTransaction(request);
                    Request newRequest = (Request) request.clone();
                    newRequest.removeFirst(RouteHeader.NAME);
                    ViaHeader viaHeader = protocolObjects.headerFactory
                            .createViaHeader(host, port,
                                    protocolObjects.transport, null);
                    newRequest.addFirst(viaHeader);

                    ClientTransaction ctx = sipProvider
                            .getNewClientTransaction(newRequest);
                    ctx.setApplicationData(stx);
                    stx.setApplicationData(ctx);
                    ctx.sendRequest();
                } else {
                    logger.debug("Saw a retransmission? State = "
                            + requestEvent.getServerTransaction().getState());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Unexpected error forwarding request", ex);
            TestHarness.fail("Unexpected exception forwarding request");
        }

    }

    public void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
            logger.debug("ClientTxID = "
                    + responseEvent.getClientTransaction()
                    + " client tx id "
                    + ((ViaHeader) response.getHeader(ViaHeader.NAME))
                            .getBranch() + " CSeq header = "
                    + response.getHeader(CSeqHeader.NAME) + " status code = "
                    + response.getStatusCode());

            // JvB: stateful proxy MUST NOT forward 100 Trying
            if (response.getStatusCode() == 100)
                return;

            ClientTransaction ct = responseEvent.getClientTransaction();
            if (ct != null) {
                ServerTransaction st = (ServerTransaction) ct
                        .getApplicationData();

                // Strip the topmost via header
                Response newResponse = (Response) response.clone();
                newResponse.removeFirst(ViaHeader.NAME);
                // The server tx goes to the terminated state.
                if ( st.getState() != TransactionState.TERMINATED)
                 st.sendResponse(newResponse);

                TestHarness.assertNull(st.getDialog());
            } else {
                // Client tx has already terminated but the UA is
                // retransmitting
                // just forward the response statelessly.
                // Strip the topmost via header

                Response newResponse = (Response) response.clone();
                newResponse.removeFirst(ViaHeader.NAME);
                // Send the retransmission statelessly
                SipProvider sipProvider = (SipProvider) responseEvent
                        .getSource();
                sipProvider.sendResponse(newResponse);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            TestHarness.fail("unexpected exception",ex);
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        logger.error("Timeout occured");
        fail("unexpected event");
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("IOException occured");
        fail("unexpected exception io exception");
    }

    public SipProvider createSipProvider() {
        try {
            ListeningPoint listeningPoint = protocolObjects.sipStack
                    .createListeningPoint(host, port, protocolObjects.transport);

            sipProvider = protocolObjects.sipStack
                    .createSipProvider(listeningPoint);
            sipProvider.setAutomaticDialogSupportEnabled(false);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            fail(unexpectedException);
            return null;
        }

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.debug("Transaction terminated event occured -- cleaning up");
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        fail("unexpected event");
    }

    public Proxy(int myPort, ProtocolObjects protocolObjects) {
        this.port = myPort;
        this.protocolObjects = protocolObjects;
    }

}
