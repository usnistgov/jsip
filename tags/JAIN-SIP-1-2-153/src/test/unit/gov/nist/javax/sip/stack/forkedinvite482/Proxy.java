package test.unit.gov.nist.javax.sip.stack.forkedinvite482;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

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
import javax.sip.Transaction;
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
 * A very simple forking proxy server.
 * 
 * @author M. Ranganathan
 * 
 */
public class Proxy extends TestHarness implements SipListener {

    // private ServerTransaction st;

    private SipProvider inviteServerTxProvider;

    private HashSet clientTxTable = new HashSet<ClientTransaction>();

    private static String host = "127.0.0.1";

    private int port = 5070;

    private SipProvider sipProvider;

    private static String unexpectedException = "Unexpected exception";

    private static Logger logger = Logger.getLogger(Proxy.class);

    private ProtocolObjects protocolObjects;

    private boolean loopDetectedSeen;

    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            this.inviteServerTxProvider = sipProvider;
            if (request.getMethod().equals(Request.INVITE)) {

                ListeningPoint lp = sipProvider.getListeningPoint(protocolObjects.transport);
                String host = lp.getIPAddress();
                int port = lp.getPort();

                ServerTransaction st = null;
                if (requestEvent.getServerTransaction() == null) {
                    st = sipProvider.getNewServerTransaction(request);

                    Request newRequest = (Request) request.clone();
                    SipURI sipUri = protocolObjects.addressFactory.createSipURI("UA1",
                            "127.0.0.1");
                    sipUri.setPort(5080);
                    sipUri.setLrParam();
                    Address address = protocolObjects.addressFactory.createAddress("client1",
                            sipUri);
                    RouteHeader rheader = protocolObjects.headerFactory
                            .createRouteHeader(address);

                    newRequest.addFirst(rheader);
                    ViaHeader viaHeader = protocolObjects.headerFactory.createViaHeader(host,
                            port, protocolObjects.transport, null);
                    newRequest.addFirst(viaHeader);
                    
                    ClientTransaction ct1 = sipProvider.getNewClientTransaction(newRequest);
                    sipUri = protocolObjects.addressFactory.createSipURI("proxy", "127.0.0.1");
                    address = protocolObjects.addressFactory.createAddress("proxy", sipUri);
                    sipUri.setPort(5080);
                    sipUri.setLrParam();
                    RecordRouteHeader recordRoute = protocolObjects.headerFactory
                            .createRecordRouteHeader(address);
                    newRequest.addHeader(recordRoute);
                    ct1.setApplicationData(st);
                    this.clientTxTable.add(ct1);

                    newRequest = (Request) request.clone();
                    sipUri = protocolObjects.addressFactory.createSipURI("UA2", "127.0.0.1");
                    sipUri.setLrParam();
                    sipUri.setPort(5080);
                    address = protocolObjects.addressFactory.createAddress("client2", sipUri);
                    rheader = protocolObjects.headerFactory.createRouteHeader(address);
                    newRequest.addFirst(rheader);
                    viaHeader = protocolObjects.headerFactory.createViaHeader(host, port,
                            protocolObjects.transport, null);
                    newRequest.addFirst(viaHeader);
                    sipUri = protocolObjects.addressFactory.createSipURI("proxy", "127.0.0.1");
                    sipUri.setPort(5080);
                    sipUri.setLrParam();
                    sipUri.setTransportParam(protocolObjects.transport);
                    address = protocolObjects.addressFactory.createAddress("proxy", sipUri);

                    recordRoute = protocolObjects.headerFactory.createRecordRouteHeader(address);

                    newRequest.addHeader(recordRoute);
                    ClientTransaction ct2 = sipProvider.getNewClientTransaction(newRequest);
                    ct2.setApplicationData(st);
                    this.clientTxTable.add(ct2);

                    // Send the requests out to the two listening points of the
                    // client.

                    ct2.sendRequest();
                    Thread.sleep((int) ( Math.abs((Math.random() * 1000 ))));
                    
                    ct1.sendRequest();
                }

            } else {
                // Remove the topmost route header
                // The route header will make sure it gets to the right place.
                logger.info("proxy: Got a request " + request.getMethod());
                Request newRequest = (Request) request.clone();
                newRequest.removeFirst(RouteHeader.NAME);
                sipProvider.sendRequest(newRequest);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }

    }

    public void checkState() {
        assertTrue("Should see LOOP DETECTED", loopDetectedSeen);
    }

    public synchronized void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
            logger.info("ClientTxID = " + responseEvent.getClientTransaction() + " client tx id "
                    + ((ViaHeader) response.getHeader(ViaHeader.NAME)).getBranch()
                    + " CSeq header = " + response.getHeader(CSeqHeader.NAME) + " status code = "
                    + response.getStatusCode());

            // JvB: stateful proxy MUST NOT forward 100 Trying
            if (response.getStatusCode() == 100)
                return;

            if (response.getStatusCode() == Response.LOOP_DETECTED) {
            	logger.info("Saw a LOOP DETECTED response");
                this.loopDetectedSeen = true;
            }
            if (cseq.getMethod().equals(Request.INVITE)) {
                ClientTransaction ct = responseEvent.getClientTransaction();
                if (ct != null) {
                    ServerTransaction st = (ServerTransaction) ct.getApplicationData();

                    // Strip the topmost via header
                    Response newResponse = (Response) response.clone();
                    newResponse.removeFirst(ViaHeader.NAME);
                    // The server tx goes to the terminated state.

                    st.sendResponse(newResponse);

                } else {
                    logger.debug("Discarding response - no transaction found!");
                }
            } else {
                // this is the OK for the cancel.
                logger.info("Got a non-invite response " + response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception");
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        logger.error("Timeout occured");
        fail("unexpected event");
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("IOException occured");
        fail("unexpected exception io exception");
    }

    public SipProvider createSipProvider() {
        try {
            ListeningPoint listeningPoint = protocolObjects.sipStack.createListeningPoint(host,
                    port, protocolObjects.transport);

            sipProvider = protocolObjects.sipStack.createSipProvider(listeningPoint);
            sipProvider.setAutomaticDialogSupportEnabled(false);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            fail(unexpectedException);
            return null;
        }

    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event occured -- cleaning up");
        if (!transactionTerminatedEvent.isServerTransaction()) {
            ClientTransaction ct = transactionTerminatedEvent.getClientTransaction();
            for (Iterator it = this.clientTxTable.iterator(); it.hasNext();) {
                if (it.next().equals(ct)) {
                    it.remove();
                }
            }
        } else {
            logger.info("Server tx terminated! ");
        }
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        fail("unexpected event");
    }

    public Proxy(int myPort, ProtocolObjects protocolObjects) {
        this.port = myPort;
        this.protocolObjects = protocolObjects;
    }

}
