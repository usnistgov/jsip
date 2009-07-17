package examples.forked.invite;

import gov.nist.javax.sip.address.SipUri;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import junit.framework.TestCase;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * A very simple forking proxy server.
 *
 * @author M. Ranganathan
 *
 */
public class Proxy extends TestCase implements SipListener {




    //private ServerTransaction st;

    private SipProvider inviteServerTxProvider;

    private Hashtable clientTxTable = new Hashtable();

    private static String transport = "udp";

    private static String host = "127.0.0.1";

    private int port = 5070;

    private SipProvider sipProvider;

    private static String unexpectedException = "Unexpected exception";

    private static Logger logger = Logger.getLogger(Proxy.class);

    static {
        try {
            logger.addAppender(new FileAppender(new SimpleLayout(),
                    ProtocolObjects.logFileDirectory + "proxlog.txt"));
        } catch (Exception ex) {
            throw new RuntimeException("could not open shootistconsolelog.txt");
        }
    }

    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            this.inviteServerTxProvider = sipProvider;
            if (request.getMethod().equals(Request.INVITE)) {

                ListeningPoint lp = sipProvider.getListeningPoint(transport);
                String host = lp.getIPAddress();
                int port = lp.getPort();

                ServerTransaction st = null;
                if (requestEvent.getServerTransaction() == null) {
                    st = sipProvider.getNewServerTransaction(request);

                }
                Request newRequest = (Request) request.clone();
                SipURI sipUri = ProtocolObjects.addressFactory.createSipURI("UA1", "127.0.0.1");
                sipUri.setPort(5080);
                sipUri.setLrParam();
                Address address = ProtocolObjects.addressFactory.createAddress("client1",
                        sipUri);
                RouteHeader rheader = ProtocolObjects.headerFactory.createRouteHeader(address);

                newRequest.addFirst(rheader);
                ViaHeader viaHeader = ProtocolObjects.headerFactory.createViaHeader(host, port,
                        transport, null);
                newRequest.addFirst(viaHeader);
                ClientTransaction ct1 = sipProvider
                        .getNewClientTransaction(newRequest);
                sipUri = ProtocolObjects.addressFactory.createSipURI("proxy", "127.0.0.1");
                address = ProtocolObjects.addressFactory.createAddress("proxy",sipUri);
                sipUri.setPort(5070);
                sipUri.setLrParam();
                RecordRouteHeader recordRoute = ProtocolObjects.headerFactory.createRecordRouteHeader(address);
                newRequest.addHeader(recordRoute);
                ct1.setApplicationData(st);
                this.clientTxTable.put(new Integer(5080), ct1);

                newRequest = (Request) request.clone();
                sipUri = ProtocolObjects.addressFactory.createSipURI("UA2", "127.0.0.1");
                sipUri.setLrParam();
                sipUri.setPort(5090);
                address = ProtocolObjects.addressFactory.createAddress("client2", sipUri);
                rheader = ProtocolObjects.headerFactory.createRouteHeader(address);
                newRequest.addFirst(rheader);
                viaHeader = ProtocolObjects.headerFactory.createViaHeader(host, port,
                        transport, null);
                newRequest.addFirst(viaHeader);
                sipUri = ProtocolObjects.addressFactory.createSipURI("proxy", "127.0.0.1");
                sipUri.setPort(5070);
                sipUri.setLrParam();
                sipUri.setTransportParam(transport);
                address = ProtocolObjects.addressFactory.createAddress("proxy",sipUri);

                recordRoute = ProtocolObjects.headerFactory.createRecordRouteHeader(address);

                newRequest.addHeader(recordRoute);
                ClientTransaction ct2 = sipProvider
                        .getNewClientTransaction(newRequest);
                ct2.setApplicationData(st);
                this.clientTxTable.put(new Integer(5090), ct2);

                // Send the requests out to the two listening points of the client.

                ct2.sendRequest();
                ct1.sendRequest();

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

    public void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
            logger.info("ClientTxID = " + responseEvent.getClientTransaction()
                    + " client tx id " + ((ViaHeader) response.getHeader(ViaHeader.NAME)).getBranch()
                    + " CSeq header = " + response.getHeader(CSeqHeader.NAME)
                    + " status code = " + response.getStatusCode());

            // JvB: stateful proxy MUST NOT forward 100 Trying
            if ( response.getStatusCode() == 100 ) return;


            if (cseq.getMethod().equals(Request.INVITE)) {
                ClientTransaction ct = responseEvent.getClientTransaction();
                if (ct != null) {
                    ServerTransaction st = (ServerTransaction) ct
                            .getApplicationData();

                    // Strip the topmost via header
                    Response newResponse = (Response) response.clone();
                    newResponse.removeFirst(ViaHeader.NAME);
                    // The server tx goes to the terminated state.

                    st.sendResponse(newResponse);
                } else {
                    // Client tx has already terminated but the UA is retransmitting
                    // just forward the response statelessly.
                    // Strip the topmost via header

                    Response newResponse = (Response) response.clone();
                    newResponse.removeFirst(ViaHeader.NAME);
                    // Send the retransmission statelessly
                    this.inviteServerTxProvider.sendResponse(newResponse);
                }
            } else {
                // Can be BYE due to Record-Route
                logger.info("Got a non-invite response " + response);
                SipProvider p = (SipProvider) responseEvent.getSource();
                response.removeFirst( ViaHeader.NAME );
                p.sendResponse( response );
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        logger.info("Timeout occured");
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.info("IOException occured");
    }
    public SipProvider createSipProvider() {
        try {
            ListeningPoint listeningPoint = ProtocolObjects.sipStack.createListeningPoint(
                    host, port, transport);

            sipProvider = ProtocolObjects.sipStack
                    .createSipProvider(listeningPoint);
            return sipProvider;
        } catch (Exception ex) {
            logger.error(unexpectedException, ex);
            fail(unexpectedException);
            return null;
        }

    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        logger.info("Transaction terminated event occured -- cleaning up");
        if (!transactionTerminatedEvent.isServerTransaction()) {
            ClientTransaction ct = transactionTerminatedEvent
                    .getClientTransaction();
            for (Iterator it = this.clientTxTable.values().iterator(); it
                    .hasNext();) {
                if (it.next().equals(ct)) {
                    it.remove();
                }
            }
        } else {
            logger.info("Server tx terminated! ");
        }
    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        logger.info("HUH!! why do i see this event??");
    }


    public static void main(String[] args) throws Exception {
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
        ProtocolObjects.init("proxy",false);
        Proxy proxy = new Proxy();
        proxy.createSipProvider();
        proxy.sipProvider.addSipListener(proxy);


    }

}
