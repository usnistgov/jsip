package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.SipStackImpl;

import java.util.ArrayList;
import java.util.EventObject;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import test.tck.msgflow.callflows.ProtocolObjects;
import test.tck.msgflow.callflows.ScenarioHarness;

public class SelfroutingTest extends ScenarioHarness {

    protected Shootist shootist;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console))
            logger.addAppender(console);
    }

    class Shootist  implements SipListener {

        private SipProvider provider;


        private ContactHeader contactHeader;

        private ListeningPoint listeningPoint;

        // To run on two machines change these to suit.
        public static final String myAddress = "127.0.0.1";

        private static final int myPort = 5060;

        protected ClientTransaction inviteTid;




        private ProtocolObjects protocolObjects;

        private Dialog dialog;





        public Shootist(ProtocolObjects protocolObjects) {
            super();
            this.protocolObjects = protocolObjects;

        }



        public void processRequest(RequestEvent requestReceivedEvent) {
            Request request = requestReceivedEvent.getRequest();
            ServerTransaction serverTransactionId = requestReceivedEvent
                    .getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod() + " received at "
                    + protocolObjects.sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            if (request.getMethod().equals(Request.BYE))
                processBye(request, serverTransactionId);
            else if (request.getMethod().equals(Request.ACK))
                processAck(request, serverTransactionId);
            else if (request.getMethod().equals(Request.INVITE))
                processInvite(request, serverTransactionId);
        }

        public void processBye(Request request,
                ServerTransaction serverTransactionId) {
            try {
                logger.info("shootist:  got a bye .");
                if (serverTransactionId == null) {
                    logger.info("shootist:  null TID.");
                    return;
                }
                Response response = protocolObjects.messageFactory.createResponse(
                        200, request);
                serverTransactionId.sendResponse(response);
            } catch (Exception ex) {
                logger.error("unexpected exception",ex);
                fail("unexpected exception");

            }
        }
        
        public void processInvite(Request request,
                ServerTransaction serverTransactionId) {
            try {
                Response response = protocolObjects.messageFactory.createResponse(
                        200, request);
                provider.sendResponse(response);
            } catch (Exception ex) {
                logger.error("unexpected exception",ex);
                fail("unexpected exception");

            }
        }
        
        public void processAck(Request request,
                ServerTransaction serverTransactionId) {
            try {
                logger.info("shootist:  got ACK .");
                if (serverTransactionId == null) {
                    logger.info("shootist:  null TID.");
                    return;
                }
                Request bye = dialog.createRequest(Request.BYE);
                ClientTransaction ctx = provider.getNewClientTransaction(bye);
                ctx.sendRequest();
            } catch (Exception ex) {
                logger.error("unexpected exception",ex);
                fail("unexpected exception");

            }
        }
        
        public boolean okToInviteReceived;

        public void processResponse(ResponseEvent responseReceivedEvent) {
            logger.info("Got a response");

            Response response = (Response) responseReceivedEvent.getResponse();
            Transaction tid = responseReceivedEvent.getClientTransaction();

            logger.info("Response received with client transaction id " + tid
                    + ":\n" + response.getStatusCode());
            if (tid != null) {
				logger.info("Dialog = " + responseReceivedEvent.getDialog());
				logger.info("Dialog State is "
						+ responseReceivedEvent.getDialog().getState());
			}
            SipProvider provider = (SipProvider) responseReceivedEvent.getSource();

            try {
            	if (response.getStatusCode() == Response.OK) {
            		if(((CSeqHeader) response.getHeader(CSeqHeader.NAME))
            				.getMethod().equals(Request.INVITE)) {
            			okToInviteReceived = true;
            		}
            	}
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
                fail("unexpected exception");
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

            logger.info("Transaction Time out");
            logger.info("TimeoutEvent " + timeoutEvent.getTimeout());
        }

        public SipProvider createSipProvider() {
            try {
                listeningPoint = protocolObjects.sipStack.createListeningPoint(
                        myAddress, myPort, protocolObjects.transport);

                provider = protocolObjects.sipStack
                        .createSipProvider(listeningPoint);
                return provider;
            } catch (Exception ex) {
                logger.error(ex);
                fail("unable to create provider");
                return null;
            }
        }

        public void sendInvite() {

            try {

                // Note that a provider has multiple listening points.
                // all the listening points must have the same IP address
                // and port but differ in their transport parameters.

                String fromName = "BigGuy";
                String fromSipAddress = "here.com";
                String fromDisplayName = "The Master Blaster";

                String toSipAddress = "there.com";
                String toUser = "LittleGuy";
                String toDisplayName = "The Little Blister";

                // create >From Header
                SipURI fromAddress = protocolObjects.addressFactory.createSipURI(
                        fromName, fromSipAddress);

                Address fromNameAddress = protocolObjects.addressFactory
                        .createAddress(fromAddress);
                fromNameAddress.setDisplayName(fromDisplayName);
                FromHeader fromHeader = protocolObjects.headerFactory
                        .createFromHeader(fromNameAddress, new Integer((int) (Math
                                .random() * Integer.MAX_VALUE)).toString());

                // create To Header
                SipURI toAddress = protocolObjects.addressFactory.createSipURI(
                        toUser, toSipAddress);
                Address toNameAddress = protocolObjects.addressFactory
                        .createAddress(toAddress);
                toNameAddress.setDisplayName(toDisplayName);
                ToHeader toHeader = protocolObjects.headerFactory.createToHeader(
                        toNameAddress, null);

                // create Request URI addressed to me
                SipURI requestURI = protocolObjects.addressFactory.createSipURI(
                        toUser, myAddress + ":" + myPort);

                // Create ViaHeaders

                ArrayList viaHeaders = new ArrayList();
                int port = provider.getListeningPoint(protocolObjects.transport)
                        .getPort();

                ViaHeader viaHeader = protocolObjects.headerFactory
                        .createViaHeader(myAddress, port,
                                protocolObjects.transport, null);

                // add via headers
                viaHeaders.add(viaHeader);

                // Create ContentTypeHeader
                ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
                        .createContentTypeHeader("application", "sdp");

                // Create a new CallId header
                CallIdHeader callIdHeader = provider.getNewCallId();
                // JvB: Make sure that the implementation matches the messagefactory
                callIdHeader = protocolObjects.headerFactory.createCallIdHeader( callIdHeader.getCallId() );


                // Create a new Cseq header
                CSeqHeader cSeqHeader = protocolObjects.headerFactory
                        .createCSeqHeader(1L, Request.INVITE);

                // Create a new MaxForwardsHeader
                MaxForwardsHeader maxForwards = protocolObjects.headerFactory
                        .createMaxForwardsHeader(70);

                // Create the request.
                Request request = protocolObjects.messageFactory.createRequest(
                        requestURI, Request.INVITE, callIdHeader, cSeqHeader,
                        fromHeader, toHeader, viaHeaders, maxForwards);
                // Create contact headers

                // Create the contact name address.
                SipURI contactURI = protocolObjects.addressFactory.createSipURI(
                        fromName, myAddress);
                contactURI.setPort(provider.getListeningPoint(
                        protocolObjects.transport).getPort());

                Address contactAddress = protocolObjects.addressFactory
                        .createAddress(contactURI);

                // Add the contact address.
                contactAddress.setDisplayName(fromName);

                contactHeader = protocolObjects.headerFactory
                        .createContactHeader(contactAddress);
                request.addHeader(contactHeader);

                // Add the extension header.
                Header extensionHeader = protocolObjects.headerFactory
                        .createHeader("My-Header", "my header value");
                request.addHeader(extensionHeader);

                String sdpData = "v=0\r\n"
                        + "o=4855 13760799956958020 13760799956958020"
                        + " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
                        + "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
                        + "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
                        + "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
                        + "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";

                request.setContent(sdpData, contentTypeHeader);

                // The following is the preferred method to route requests
                // to the peer. Create a route header and set the "lr"
                // parameter for the router header.

                Address address = protocolObjects.addressFactory
                        .createAddress("<sip:" + myAddress + ":" + myPort
                                + ">");
                // SipUri sipUri = (SipUri) address.getURI();
                // sipUri.setPort(PEER_PORT);

                RouteHeader routeHeader = protocolObjects.headerFactory
                        .createRouteHeader(address);
                ((SipURI)address.getURI()).setLrParam();
                request.addHeader(routeHeader);
                extensionHeader = protocolObjects.headerFactory.createHeader(
                        "My-Other-Header", "my new header value ");
                request.addHeader(extensionHeader);

                Header callInfoHeader = protocolObjects.headerFactory.createHeader(
                        "Call-Info", "<http://www.antd.nist.gov>");
                request.addHeader(callInfoHeader);

                // Create the client transaction.
                this.inviteTid = provider.getNewClientTransaction(request);
                this.dialog = this.inviteTid.getDialog();
                // Note that the response may have arrived right away so
                // we cannot check after the message is sent.
                assertTrue(this.dialog.getState() == null);

                // send the request out.
                this.inviteTid.sendRequest();


            } catch (Exception ex) {
                logger.error("Unexpected exception", ex);
                fail("unexpected exception");
            }
        }



        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
         */
        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.error("IO Exception!");
            fail("Unexpected exception");

        }

        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
         */
        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {

            logger.info("Transaction Terminated Event!");
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
         */
        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Dialog Terminated Event!");

        }
    }


    public SelfroutingTest()
    {
        super("Selfrouting", true);
    }

    public void setUp() {


    }

    public void testSelfroutingUDP() {
        try {
            this.transport = "udp";

            super.setUp();
            shootist = new Shootist(getRiProtocolObjects());
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);
            SipStackImpl ss = (SipStackImpl) shootistProvider.getSipStack();

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    	new Thread() {
    		@Override
    		public void run() {
    			shootist.sendInvite();
    			super.run();
    		}
    	}.start();
        
        try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        assertTrue(this.shootist.okToInviteReceived);
    }
    
    public void testSelfroutingTCP() {
        try {
            this.transport = "tcp";

            super.setUp();
            shootist = new Shootist(getRiProtocolObjects());
            SipProvider shootistProvider = shootist.createSipProvider();
            shootistProvider.addSipListener(shootist);

            getRiProtocolObjects().start();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getTiProtocolObjects().start();
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }
    	new Thread() {
    		@Override
    		public void run() {
    			shootist.sendInvite();
    			super.run();
    		}
    	}.start();
        
        try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        assertTrue(this.shootist.okToInviteReceived);
    }

    public void tearDown() {
        try {
            Thread.sleep(1000);
            getTiProtocolObjects().destroy();
            if (getTiProtocolObjects() != getRiProtocolObjects())
                getRiProtocolObjects().destroy();
            Thread.sleep(1000);
            this.providerTable.clear();
            logTestCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
