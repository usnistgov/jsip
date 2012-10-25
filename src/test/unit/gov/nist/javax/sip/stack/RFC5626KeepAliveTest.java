package test.unit.gov.nist.javax.sip.stack;

import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.IOExceptionEventExt;
import gov.nist.javax.sip.IOExceptionEventExt.Reason;
import gov.nist.javax.sip.ListeningPointExt;
import gov.nist.javax.sip.ListeningPointImpl;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.ConnectionOrientedMessageChannel;
import gov.nist.javax.sip.stack.ConnectionOrientedMessageProcessor;
import gov.nist.javax.sip.stack.MessageProcessor;
import gov.nist.javax.sip.stack.SIPTransactionStack;
import gov.nist.javax.sip.stack.TCPMessageChannel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
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
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import test.tck.msgflow.callflows.ProtocolObjects;
import test.tck.msgflow.callflows.ScenarioHarness;

/**
 * Test for RFC 5626 Section 4.4.1 tests the various conditions outlined in the flow available at  
 * https://docs.google.com/drawings/d/1vWHQU4Xqy_gkbsprJ8gumoBi2ajdFw71uw_5vrn2V8g/edit?hl=en_US
 * 
 * @author jean.deruelle@gmail.com
 * @author : Alex Vinogradov
 */
public class RFC5626KeepAliveTest extends ScenarioHarness implements SipListenerExt {

    protected Shootist shootist;
    
    protected Shootme shootme;

    private static Logger logger = Logger.getLogger("test.tck");

    static {
        if (!logger.isAttached(console)) {
            logger.addAppender(console);
        }
    }

    class Shootme  implements SipListenerExt {


        private ProtocolObjects  protocolObjects;
        private ListeningPoint listeningPoint;

        // To run on two machines change these to suit.
        public static final String myAddress = "127.0.0.1";

        public static final int myPort = 5070;

        private ServerTransaction inviteTid;


        private Dialog dialog;

        private ServerTransaction reSendSt = null;
        private Response reSendResponse = null;
        private int dropAckCount = 0;


		private boolean keepAliveTimeoutFired;

        public Shootme(ProtocolObjects protocolObjects) {
            this.protocolObjects = protocolObjects;
            ((SIPTransactionStack)protocolObjects.sipStack).setReliableConnectionKeepAliveTimeout(4000);
        }
        
        public ConnectionOrientedMessageProcessor getTestMessageProcessor() {
            return (ConnectionOrientedMessageProcessor)((ListeningPointImpl) shootme.listeningPoint).getMessageProcessor();
        }
        
        public ConnectionOrientedMessageChannel getTestChannel() {
            try {
                MessageProcessor processor = ((ListeningPointImpl) shootme.listeningPoint).getMessageProcessor();
                Field field = ConnectionOrientedMessageProcessor.class.getDeclaredField("messageChannels");
                field.setAccessible(true);

                Map<String, ConnectionOrientedMessageChannel> tcpMessageChannels = (Map<String, ConnectionOrientedMessageChannel>) field.get(processor);
                Iterator<ConnectionOrientedMessageChannel> itr = tcpMessageChannels.values().iterator();
                while (itr.hasNext()) {
                	ConnectionOrientedMessageChannel tcpMessageChannel = itr.next();
					logger.info("tcpMessageChannel port " + tcpMessageChannel.getPort() + " peerPort " + tcpMessageChannel.getPeerPort());					
				}
                return tcpMessageChannels.values().iterator().next();
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage(), e);
                return null;
            }
        }

        public void processRequest(RequestEvent requestEvent) {
            Request request = requestEvent.getRequest();
            ServerTransaction serverTransactionId = requestEvent
                    .getServerTransaction();

            logger.info("\n\nRequest " + request.getMethod()
                    + " received at " + protocolObjects.sipStack.getStackName()
                    + " with server transaction id " + serverTransactionId);

            if (request.getMethod().equals(Request.INVITE)) {
                processInvite(requestEvent, serverTransactionId);
            } 

        }        

        /**
         * Process the invite request.
         */
        public void processInvite(RequestEvent requestEvent,
                ServerTransaction serverTransaction) {
            SipProvider sipProvider = (SipProvider) requestEvent.getSource();
            Request request = requestEvent.getRequest();
            logger.info("Got an INVITE  " + request);
            try {
                Response response = protocolObjects.messageFactory.createResponse(180, request);
                ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                toHeader.setTag("4321");
                Address address = protocolObjects.addressFactory.createAddress("Shootme <sip:"
                        + myAddress + ":" + myPort + ">");
                ContactHeader contactHeader = protocolObjects.headerFactory
                        .createContactHeader(address);
                response.addHeader(contactHeader);
                ServerTransaction st = requestEvent.getServerTransaction();

                if (st == null) {
                    st = sipProvider.getNewServerTransaction(request);
                    logger.info("Server transaction created!" + request);

                    logger.info("Dialog = " + st.getDialog());
                }

                byte[] content = request.getRawContent();
                if (content != null) {
                    logger.info(" content = " + new String(content));
                    ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
                            .createContentTypeHeader("application", "sdp");
                    logger.info("response = " + response);
                    response.setContent(content, contentTypeHeader);
                }
                dialog = st.getDialog();
                if (dialog != null) {
                    logger.info("Dialog " + dialog);
                    logger.info("Dialog state " + dialog.getState());
                }
                st.sendResponse(response);
                response = protocolObjects.messageFactory.createResponse(200, request);
                toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                toHeader.setTag("4321");
                // Application is supposed to set.
                response.addHeader(contactHeader);
                st.sendResponse(response);
                reSendSt = st;
                reSendResponse = response;
                logger.info("TxState after sendResponse = " + st.getState());
                this.inviteTid = st;
            } catch (Exception ex) {
                String s = "unexpected exception";

                logger.error(s,ex);
                AckReTransmissionTest.fail(s);
            }
        }


        public void processResponse(ResponseEvent responseReceivedEvent) {
            logger.info("Got a response");
            Response response = (Response) responseReceivedEvent.getResponse();
            Transaction tid = responseReceivedEvent.getClientTransaction();

            logger.info("Response received with client transaction id "
                    + tid + ":\n" + response);
            try {
                if (response.getStatusCode() == Response.OK
                        && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                                .getMethod().equals(Request.INVITE)) {
                    Dialog dialog = tid.getDialog();
                    CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                    Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                    dialog.sendAck(ackRequest);
                }
                if ( tid != null ) {
                    Dialog dialog = tid.getDialog();
                    logger.info("Dialog State = " + dialog.getState());
                }
            } catch (Exception ex) {
                String s = "Unexpected exception";
                logger.error(s,ex);
                AckReTransmissionTest.fail(s);
            }

        }

        public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
            Transaction transaction;
            if (timeoutEvent.isServerTransaction()) {
                transaction = timeoutEvent.getServerTransaction();
            } else {
                transaction = timeoutEvent.getClientTransaction();
            }
            logger.info("state = " + transaction.getState());
            logger.info("dialog = " + transaction.getDialog());
            logger.info("dialogState = "
                    + transaction.getDialog().getState());
            logger.info("Transaction Time out");
        }




        public SipProvider createSipProvider() throws Exception {
            listeningPoint = protocolObjects.sipStack.createListeningPoint(myAddress,
                    myPort, protocolObjects.transport);


            SipProvider sipProvider = protocolObjects.sipStack.createSipProvider(listeningPoint);
            return sipProvider;
        }




        public void checkState() {
            AckReTransmissionTest.assertTrue("ACK was not successfully retransmitted 4 times", 4==dropAckCount);
        }
        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
         */
        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.info("Shootme An IO Exception was detected : "
                    + exceptionEvent.getHost());
            keepAliveTimeoutFired |= (exceptionEvent instanceof IOExceptionEventExt && ((IOExceptionEventExt)exceptionEvent).getReason() == Reason.KeepAliveTimeout);

            logger.info("Shootme KeepAlive Time out " + keepAliveTimeoutFired);         
        }

        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processTransactionTerminated(javax.sip.TransactionTerminatedEvent)
         */
        public void processTransactionTerminated(
                TransactionTerminatedEvent transactionTerminatedEvent) {
            logger.info("Tx terminated event ");

        }

        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processDialogTerminated(javax.sip.DialogTerminatedEvent)
         */
        public void processDialogTerminated(
                DialogTerminatedEvent dialogTerminatedEvent) {
            logger.info("Dialog terminated event detected ");

        }


		public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {
			// TODO Auto-generated method stub
			
		}		
    }
    
    class Shootist implements SipListenerExt {

        private SipProvider provider;

        private ContactHeader contactHeader;

        private ListeningPoint listeningPoint;

        // To run on two machines change these to suit.
        public static final String myAddress = "127.0.0.1";

        public static final int myPort = 5055;

        protected ClientTransaction inviteTid;

        private ProtocolObjects protocolObjects;

        private Dialog dialog;
        
        Timer timer = new Timer();

        class MyTimerTask extends TimerTask {            
        	private int keepAliveSent = 0;
        	
			@Override
            public void run() {                

//                assertTrue(shootist.getTestMessageProcessor().setKeepAliveTimeout(
//                        Shootme.myAddress, Shootme.myPort, 1000));
				logger.info("keepAliveSent =" + keepAliveSent + " / KeepAliveToSend ="+keepAliveToSend);
				
            	if(((SipStackImpl)shootist.protocolObjects.sipStack).isAlive() && (keepAliveToSend < 0 || keepAliveSent <= keepAliveToSend)) {
	                try {
	                    ((ListeningPointExt)shootist.listeningPoint).sendHeartbeat( Shootme.myAddress, Shootme.myPort);
	                    keepAliveSent++;
	                } catch (Exception e) {
	                	logger.info("keepAliveSender received Exception =" + e + " ,cancelling keepalivesender timer");
	                    e.printStackTrace();
	                    this.cancel();
//	                    fail();
	                }
	                if(keepAliveSent > keepAliveToSend) {
	                	((SIPTransactionStack)protocolObjects.sipStack).setKeepAliveTimeout(myAddress, myPort, transport, Shootme.myAddress, Shootme.myPort, -1);	
	                }
            	} else {
            		this.cancel();
            	}
            }
        };
        
        public Shootist(ProtocolObjects protocolObjects) {
            super();
            this.protocolObjects = protocolObjects;
            ((SIPTransactionStack)protocolObjects.sipStack).setReliableConnectionKeepAliveTimeout(4000);
        }


        public void processRequest(RequestEvent requestReceivedEvent) {
        }


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
                if (response.getStatusCode() == Response.OK
                        && ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
                                .getMethod().equals(Request.INVITE)) {

                    Dialog dialog = responseReceivedEvent.getDialog();
                    CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                    Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                    logger.info("Ack request to send = " + ackRequest);
                    logger.info("Sending ACK");
                    dialog.sendAck(ackRequest);
                }
            } catch (Exception ex) {
                logger.error(ex);
                ex.printStackTrace();
                RFC5626KeepAliveTest.fail("unexpected exception");
            }
        	timer.schedule(new MyTimerTask(), 1000, 3000);
        }

        public volatile boolean keepAliveTimeoutFired;

		public int keepAliveToSend = -1;

        public void processTimeout(TimeoutEvent timeoutEvent) {

            
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
                        toUser, Shootme.myAddress + ":" + Shootme.myPort);

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
                callIdHeader = protocolObjects.headerFactory.createCallIdHeader(callIdHeader.getCallId());


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

//                Address address = protocolObjects.addressFactory
//                        .createAddress("<sip:" + myAddress + ":" + Shootist.myPort
//                                + ">");
//                // SipUri sipUri = (SipUri) address.getURI();
//                // sipUri.setPort(PEER_PORT);
//
//                RouteHeader routeHeader = protocolObjects.headerFactory
//                        .createRouteHeader(address);
//                ((SipURI) address.getURI()).setLrParam();
//                request.addHeader(routeHeader);
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

        TCPMessageChannel getTestChannel() {
            try {
                MessageProcessor processor = ((ListeningPointImpl) shootist.listeningPoint).getMessageProcessor();
                Field field = ConnectionOrientedMessageProcessor.class.getDeclaredField("messageChannels");
                field.setAccessible(true);

                Map<String, TCPMessageChannel> tcpMessageChannels = (Map<String, TCPMessageChannel>) field.get(processor);
                return tcpMessageChannels.values().iterator().next();
            } catch (Exception e) {
                e.printStackTrace();
                fail(e.getMessage(), e);
                return null;
            }
        }

        ConnectionOrientedMessageProcessor getTestMessageProcessor() {
            return (ConnectionOrientedMessageProcessor)((ListeningPointImpl) shootist.listeningPoint).getMessageProcessor();
        }


        /*
         * (non-Javadoc)
         *
         * @see javax.sip.SipListener#processIOException(javax.sip.IOExceptionEvent)
         */
        public void processIOException(IOExceptionEvent exceptionEvent) {
            logger.info("Shootist IO Exception ! ");
            keepAliveTimeoutFired |= (exceptionEvent instanceof IOExceptionEventExt && ((IOExceptionEventExt)exceptionEvent).getReason() == Reason.KeepAliveTimeout);

            logger.info("Shootist KeepAlive Time out " + keepAliveTimeoutFired);              
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

		public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {
			// TODO Auto-generated method stub
			
		}		
    }

    public RFC5626KeepAliveTest() {
        super("KeepAlive", true);
    }

    public void setUp() throws NoSuchFieldException {
    }

    public void testKeepaliveTCP() {
        try {
            this.transport = "tcp";

            super.setUp();
            shootist = new Shootist(getRiProtocolObjects());
            SipProvider shootistProvider = shootist.createSipProvider();
            providerTable.put(shootistProvider, shootist);

            shootme = new Shootme(getTiProtocolObjects());
            SipProvider shootmeProvider = shootme.createSipProvider();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(this);
            shootmeProvider.addSipListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }        
        shootist.sendInvite();
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(this.shootist.keepAliveTimeoutFired);
        assertFalse(this.shootme.keepAliveTimeoutFired);
    }
    
    public void testKeepaliveTCPShootmeTimeout() {
        try {
            this.transport = "tcp";

            super.setUp();
            shootist = new Shootist(getRiProtocolObjects());
            SipProvider shootistProvider = shootist.createSipProvider();
            providerTable.put(shootistProvider, shootist);

            shootme = new Shootme(getTiProtocolObjects());
            SipProvider shootmeProvider = shootme.createSipProvider();
            providerTable.put(shootmeProvider, shootme);
            shootistProvider.addSipListener(this);
            shootmeProvider.addSipListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("unexpected exception ");
        }        
        shootist.keepAliveToSend  = 1;
        shootist.sendInvite();
        
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(this.shootist.keepAliveTimeoutFired);
        assertTrue(this.shootme.keepAliveTimeoutFired);
    }
    
    public void testKeepaliveTCPShootistTimeout() throws Exception {       
        this.transport = "tcp";

        super.setUp();
        shootist = new Shootist(getRiProtocolObjects());
        SipProvider shootistProvider = shootist.createSipProvider();
        providerTable.put(shootistProvider, shootist);

        shootme = new Shootme(getTiProtocolObjects());
        SipProvider shootmeProvider = shootme.createSipProvider();
        providerTable.put(shootmeProvider, shootme);
        shootistProvider.addSipListener(this);
        shootmeProvider.addSipListener(this);
                    
        shootist.keepAliveToSend  = 100;
        shootist.sendInvite();
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(this.shootist.keepAliveTimeoutFired);
        assertFalse(this.shootme.keepAliveTimeoutFired);
        System.out.println("Destroying Shootme to provoke timeout on Shootist");
        getTiProtocolObjects().destroy();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        assertTrue(this.shootist.keepAliveTimeoutFired);
        assertFalse(this.shootme.keepAliveTimeoutFired);
    }

    public void testKeepaliveTCPModifyKeepAliveTimeout() throws Exception {
    	this.transport = "tcp";

        super.setUp();
        shootist = new Shootist(getRiProtocolObjects());
        SipProvider shootistProvider = shootist.createSipProvider();
        providerTable.put(shootistProvider, shootist);

        shootme = new Shootme(getTiProtocolObjects());
        SipProvider shootmeProvider = shootme.createSipProvider();
        providerTable.put(shootmeProvider, shootme);
        shootistProvider.addSipListener(this);
        shootmeProvider.addSipListener(this);
        shootist.keepAliveToSend  = 100;
        shootist.sendInvite();        

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(this.shootist.keepAliveTimeoutFired);
        assertFalse(this.shootme.keepAliveTimeoutFired);        
        shootist.keepAliveToSend = 0;
        
        // cannot be found due to ephemeral port
//        assertTrue(shootme.getTestMessageProcessor().setKeepAliveTimeout(Shootist.myAddress, Shootist.myPort, 400));
        
        shootme.getTestChannel().setKeepAliveTimeout(400);

        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }        
        assertFalse(this.shootist.keepAliveTimeoutFired);
        assertTrue(this.shootme.keepAliveTimeoutFired);
    }

    public void tearDown() {
        try {        	
            Thread.sleep(1000);            
            if (getTiProtocolObjects() != getRiProtocolObjects()) {
                getRiProtocolObjects().destroy();
            }            
            getTiProtocolObjects().destroy();
            Thread.sleep(1000);
            this.providerTable.clear();
            logTestCompleted();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void processRequest(RequestEvent requestEvent) {
        getSipListener(requestEvent).processRequest(requestEvent);

    }

    public void processResponse(ResponseEvent responseEvent) {
        getSipListener(responseEvent).processResponse(responseEvent);

    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        getSipListener(timeoutEvent).processTimeout(timeoutEvent);
    }

    public void processIOException(IOExceptionEvent exceptionEvent) {
    	 getSipListener(exceptionEvent).processIOException(exceptionEvent);
    }

    public void processTransactionTerminated(
            TransactionTerminatedEvent transactionTerminatedEvent) {
        getSipListener(transactionTerminatedEvent)
                .processTransactionTerminated(transactionTerminatedEvent);

    }

    public void processDialogTerminated(
            DialogTerminatedEvent dialogTerminatedEvent) {
        getSipListener(dialogTerminatedEvent).processDialogTerminated(
                dialogTerminatedEvent);

    }
    
    private SipListenerExt getSipListener(EventObject sipEvent) {
        SipProvider source = (SipProvider) sipEvent.getSource();
        SipListenerExt listener = (SipListenerExt) providerTable.get(source);
        assertTrue(listener != null);
        return listener;
    }

	public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {
		getSipListener(timeoutEvent).processDialogTimeout(timeoutEvent);
		
	}	
}
