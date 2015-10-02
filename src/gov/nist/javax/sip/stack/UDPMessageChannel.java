/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 *
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 *
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *
 * .
 *
 */
/*****************************************************************************
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 *****************************************************************************/

package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.ServerLogger;
import gov.nist.core.StackLogger;
import gov.nist.core.ThreadAuditor;
import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.parser.MessageParser;
import gov.nist.javax.sip.parser.ParseExceptionListener;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import javax.sip.address.Hop;
import javax.sip.message.Response;

/*
 * Kim Kirby (Keyvoice) suggested that duplicate checking should be added to the
 * stack (later removed). Lamine Brahimi suggested a single threaded behavior
 * flag be added to this. Niklas Uhrberg suggested that thread pooling support
 * be added to this for performance and resource management. Peter Parnes found
 * a bug with this code that was sending it into an infinite loop when a bad
 * incoming message was parsed. Bug fix by viswashanti.kadiyala@antepo.com.
 * Hagai Sela addded fixes for NAT traversal. Jeroen van Bemmel fixed up for
 * buggy clients (such as windows messenger) and added code to return
 * BAD_REQUEST. David Alique fixed an address recording bug. Jeroen van Bemmel
 * fixed a performance issue where the stack was doing DNS lookups (potentially
 * unnecessary). Ricardo Bora (Natural Convergence ) added code that prevents
 * the stack from exitting when an exception is encountered.
 *
 */

/**
 * This is the UDP Message handler that gets created when a UDP message needs to
 * be processed. The message is processed by creating a String Message parser
 * and invoking it on the message read from the UDP socket. The parsed structure
 * is handed off via a SIP stack request for further processing. This stack
 * structure isolates the message handling logic from the mechanics of sending
 * and recieving messages (which could be either udp or tcp.
 *
 *
 * @author M. Ranganathan <br/>
 *
 *
 *
 * @version 1.2 $Revision: 1.87 $ $Date: 2010-12-02 22:44:53 $
 */
public class UDPMessageChannel extends MessageChannel implements
        ParseExceptionListener, Runnable, RawMessageChannel {
    private static StackLogger logger = CommonLogger.getLogger(UDPMessageChannel.class);
    /**
     * SIP Stack structure for this channel.
     */
    protected SIPTransactionStack sipStack;

    /**
     * The parser we are using for messages received from this channel.
     */
    protected MessageParser myParser;

    /**
     * Where we got the stuff from
     */
    private InetAddress peerAddress;

    private String myAddress;

    private int peerPacketSourcePort;

    private InetAddress peerPacketSourceAddress;

    /**
     * Reciever port -- port of the destination.
     */
    private int peerPort;

    /**
     * Protocol to use when talking to receiver (i.e. when sending replies).
     */
    private String peerProtocol;

    protected int myPort;

    private DatagramPacket incomingPacket;

    private long receptionTime;

    private Thread mythread = null;

    /*
     * A table that keeps track of when the last pingback was sent to a given
     * remote IP address and port. This is for NAT compensation. This stays in
     * the table for 1 seconds and prevents infinite loop. If a second pingback
     * happens in that period of time, it will be dropped.
     */
    private static Hashtable<String, PingBackTimerTask> pingBackRecord = new Hashtable<String, PingBackTimerTask>();

    class PingBackTimerTask extends SIPStackTimerTask {
        String ipAddress;
        int port;

        public PingBackTimerTask(String ipAddress, int port) {
            this.ipAddress = ipAddress;
            this.port = port;
        }

        public void runTask() {
            pingBackRecord.remove(ipAddress + ":" + port);
        }

        @Override
        public int hashCode() {
            return (ipAddress + ":" + port).hashCode();
        }
    }

    /**
     * Constructor - takes a datagram packet and a stack structure Extracts the
     * address of the other from the datagram packet and stashes away the
     * pointer to the passed stack structure.
     *
     * @param stack
     *            is the shared SIPStack structure
     * @param messageProcessor
     *            is the creating message processor.
     * @param threadName
     *            name that will be affected to the underlying thread
     */
    protected UDPMessageChannel(SIPTransactionStack stack,
            UDPMessageProcessor messageProcessor, String threadName) {
        super.messageProcessor = messageProcessor;
        this.sipStack = stack;

        // jeand : Create a new string message parser to parse the list of
        // messages.
        myParser = sipStack.getMessageParserFactory().createMessageParser(
                sipStack);

        mythread = new Thread(this);

        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.myPort = messageProcessor.getPort();

        mythread.setName(threadName);
        mythread.setDaemon(true);
        mythread.start();

    }

    /**
     * Constructor. We create one of these in order to process an incoming
     * message.
     *
     * @param stack
     *            is the SIP sipStack.
     * @param messageProcessor
     *            is the creating message processor.
     * @param packet
     *            is the incoming datagram packet.
     */
    protected UDPMessageChannel(SIPTransactionStack stack,
            UDPMessageProcessor messageProcessor, DatagramPacket packet) {

        this.incomingPacket = packet;
        super.messageProcessor = messageProcessor;
        this.sipStack = stack;

        // jeand : Create a new string message parser to parse the list of
        // messages.
        myParser = sipStack.getMessageParserFactory().createMessageParser(
                sipStack);

        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.myPort = messageProcessor.getPort();
        mythread = new Thread(this);
        mythread.setDaemon(true);

        mythread.start();

    }

    /**
     * Constructor. We create one of these when we send out a message.
     *
     * @param targetAddr
     *            INET address of the place where we want to send messages.
     * @param port
     *            target port (where we want to send the message).
     * @param sipStack
     *            our SIP Stack.
     */
    protected UDPMessageChannel(InetAddress targetAddr, int port,
            SIPTransactionStack sipStack, UDPMessageProcessor messageProcessor) {
        peerAddress = targetAddr;
        peerPort = port;
        peerProtocol = "UDP";
        super.messageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        this.myPort = messageProcessor.getPort();
        this.sipStack = sipStack;
        // jeand : Create a new string message parser to parse the list of
        // messages.
        myParser = sipStack.getMessageParserFactory().createMessageParser(
                sipStack);

        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            this.logger.logDebug(
                    "Creating message channel " + targetAddr.getHostAddress()
                            + "/" + port);
        }
    }

    /**
     * Run method specified by runnnable.
     */
    public void run() {
        // Assume no thread pooling (bug fix by spierhj)
        ThreadAuditor.ThreadHandle threadHandle = null;

        final UDPMessageProcessor udpMessageProcessor = (UDPMessageProcessor) messageProcessor;

        while (true) {
            // messages that we write out to him.
            DatagramPacket packet = null;

            if (sipStack.threadPoolSize != -1) {

                if (threadHandle == null) {
                    threadHandle = sipStack.getThreadAuditor()
                            .addCurrentThread();
                }

                // Send a heartbeat to the thread auditor
                threadHandle.ping();

                try {
                	DatagramQueuedMessageDispatch work = null;
                	// adding condition to avoid looping and taking too much CPU if the 
                	// auditing is not enabled
                	if (sipStack.getThreadAuditor().isEnabled()) {
                		work = udpMessageProcessor.messageQueue.poll(threadHandle
	                        .getPingIntervalInMillisecs(), TimeUnit.MILLISECONDS);
                	} else {
                		work = udpMessageProcessor.messageQueue.take();
                	}
	                if (!udpMessageProcessor.isRunning) {
	                    return;
	                }
	                if (work == null) {
	                	continue;
	                } else {
	                	packet = work.packet;
		                this.incomingPacket = work.packet;						
	                }	                	
                } catch (InterruptedException ex) {
					if (!udpMessageProcessor.isRunning) {
						return;
					}
				}
            } else {
                packet = this.incomingPacket;
            }

            // Process the packet. Catch and log any exception we may throw.
            try {
                processIncomingDataPacket(packet);
            } catch (Exception e) {

                logger.logError(
                        "Error while processing incoming UDP packet" + Arrays.toString(packet.getData()), e);
            }

            if (sipStack.threadPoolSize == -1) {
                return;
            }
        }
    }

    /**
     * Process an incoming datagram
     *
     * @param packet
     *            is the incoming datagram packet.
     */
    private void processIncomingDataPacket(DatagramPacket packet)
            throws Exception {
        this.peerAddress = packet.getAddress();
        int packetLength = packet.getLength();
        // Read bytes and put it in a eueue.
        byte[] bytes = packet.getData();
        byte[] msgBytes = new byte[packetLength];
        System.arraycopy(bytes, 0, msgBytes, 0, packetLength);

        // Do debug logging.
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            this.logger.logDebug(
                    "UDPMessageChannel: processIncomingDataPacket : peerAddress = "
                            + peerAddress.getHostAddress() + "/"
                            + packet.getPort() + " Length = " + packetLength);

        }

        SIPMessage sipMessage = null;
        try {
            this.receptionTime = System.currentTimeMillis();
            sipMessage = myParser.parseSIPMessage(msgBytes, true, false, this);
            /*@see Issue 292 */
            if (sipMessage instanceof SIPRequest) {
                String sipVersion = ((SIPRequest)sipMessage).getRequestLine().getSipVersion();
                if (! sipVersion.equals("SIP/2.0")) {
                     Response versionNotSupported = ((SIPRequest) sipMessage).createResponse(Response.VERSION_NOT_SUPPORTED, "Bad version " + sipVersion);
                     this.sendMessage(versionNotSupported.toString().getBytes(),peerAddress,packet.getPort(),"UDP",false);
                     return;
                }
                String method = ((SIPRequest) sipMessage).getMethod();
                String cseqMethod = ((SIPRequest) sipMessage).getCSeqHeader()
                        .getMethod();

                if (!method.equalsIgnoreCase(cseqMethod)) {
                    SIPResponse sipResponse = ((SIPRequest) sipMessage)
                    .createResponse(SIPResponse.BAD_REQUEST);
                    byte[] resp = sipResponse
                            .encodeAsBytes(this.getTransport());
                    this.sendMessage(resp,peerAddress,packet.getPort(),"UDP",false);
                    return;

                }
            }

        } catch (ParseException ex) {
            // myParser = null; // let go of the parser reference.
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        "Rejecting message !  " + new String(msgBytes));
                this.logger.logDebug(
                        "error message " + ex.getMessage());
                this.logger.logException(ex);
            }

            // JvB: send a 400 response for requests (except ACK)
            // Currently only UDP, @todo also other transports
            String msgString = new String(msgBytes, 0, packetLength);
            if (!msgString.startsWith("SIP/") && !msgString.startsWith("ACK ")) {

                String badReqRes = createBadReqRes(msgString, ex);
                if (badReqRes != null) {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger.logDebug(
                                "Sending automatic 400 Bad Request:");
                        logger.logDebug(badReqRes);
                    }
                    try {
                        this.sendMessage(badReqRes.getBytes(), peerAddress,
                                packet.getPort(), "UDP", false);
                    } catch (IOException e) {
                        this.logger.logException(e);
                    }
                } else {
                    if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        logger
                                .logDebug(
                                        "Could not formulate automatic 400 Bad Request");
                    }
                }
            }

            return;
        }
        // No parse exception but null message - reject it and
        // march on (or return).
        // exit this message processor if the message did not parse.

        if (sipMessage == null) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        "Rejecting message !  + Null message parsed.");
            }
            String key = packet.getAddress().getHostAddress() + ":"
                    + packet.getPort();
            if (pingBackRecord.get(key) == null
                    && sipStack.getMinKeepAliveInterval() > 0) {
                byte[] retval = "\r\n\r\n".getBytes();
                DatagramPacket keepalive = new DatagramPacket(retval, 0,
                        retval.length, packet.getAddress(), packet.getPort());
                PingBackTimerTask task = new PingBackTimerTask(packet
                        .getAddress().getHostAddress(), packet.getPort());
                this.pingBackRecord.put(key, task);
                this.sipStack.getTimer().schedule(task,
                        sipStack.getMinKeepAliveInterval() * 1000);
                ((UDPMessageProcessor) this.messageProcessor).sock
                        .send(keepalive);
            } else {
                logger.logDebug("Not sending ping back");
            }
            return;
        }
        Via topMostVia = sipMessage.getTopmostVia();
        // Check for the required headers.
        if (sipMessage.getFrom() == null || sipMessage.getTo() == null
                || sipMessage.getCallId() == null
                || sipMessage.getCSeq() == null || topMostVia == null) {
            String badmsg = new String(msgBytes);
            if (logger.isLoggingEnabled()) {
                this.logger
                        .logError("bad message " + badmsg);
                this.logger.logError(
                        ">>> Dropped Bad Msg " + "From = "
                                + sipMessage.getFrom() + "To = "
                                + sipMessage.getTo() + "CallId = "
                                + sipMessage.getCallId() + "CSeq = "
                                + sipMessage.getCSeq() + "Via = "
                                + sipMessage.getViaHeaders());
            }
            return;
        }

        if(sipStack.sipEventInterceptor != null) {
            sipStack.sipEventInterceptor.beforeMessage(sipMessage);
        }
        // For a request first via header tells where the message
        // is coming from.
        // For response, just get the port from the packet.
        if (sipMessage instanceof SIPRequest) {
            Hop hop = sipStack.addressResolver.resolveAddress(topMostVia
                    .getHop());
            this.peerPort = hop.getPort();
            this.peerProtocol = topMostVia.getTransport();

            this.peerPacketSourceAddress = packet.getAddress();
            this.peerPacketSourcePort = packet.getPort();
            try {
                this.peerAddress = packet.getAddress();
                // Check to see if the received parameter matches
                // the peer address and tag it appropriately.

                boolean hasRPort = topMostVia.hasParameter(Via.RPORT);
                if (hasRPort
                        || !hop.getHost().equals(
                                this.peerAddress.getHostAddress())) {
                    topMostVia.setParameter(Via.RECEIVED, this.peerAddress
                            .getHostAddress());
                }

                if (hasRPort) {
                    topMostVia.setParameter(Via.RPORT, Integer
                            .toString(this.peerPacketSourcePort));
                }
            } catch (java.text.ParseException ex1) {
                InternalErrorHandler.handleException(ex1);
            }

        } else {

            this.peerPacketSourceAddress = packet.getAddress();
            this.peerPacketSourcePort = packet.getPort();
            this.peerAddress = packet.getAddress();
            this.peerPort = packet.getPort();
            this.peerProtocol = topMostVia.getTransport();
        }

        this.processMessage(sipMessage);
        if(sipStack.sipEventInterceptor != null) {
            sipStack.sipEventInterceptor.afterMessage(sipMessage);
        }
    }

    /**
     * Actually proces the parsed message.
     *
     * @param sipMessage
     */
    public void processMessage(SIPMessage sipMessage) {
        sipMessage.setRemoteAddress(this.peerAddress);
        sipMessage.setRemotePort(this.getPeerPort());
        sipMessage.setLocalPort(this.getPort());
        sipMessage.setLocalAddress(this.getMessageProcessor().getIpAddress());
        //Issue 3: https://telestax.atlassian.net/browse/JSIP-3
        if(logger.isLoggingEnabled(LogWriter.TRACE_INFO)) {
        	logger.logInfo("Setting SIPMessage peerPacketSource to: "+peerPacketSourceAddress+":"+peerPacketSourcePort);
        }
        sipMessage.setPeerPacketSourceAddress(this.peerPacketSourceAddress);
        sipMessage.setPeerPacketSourcePort(this.peerPacketSourcePort);

        if (sipMessage instanceof SIPRequest) {
            SIPRequest sipRequest = (SIPRequest) sipMessage;

            // This is a request - process it.
            // So far so good -- we will commit this message if
            // all processing is OK.
            if (logger.isLoggingEnabled(
                    ServerLogger.TRACE_MESSAGES)) {

                this.sipStack.serverLogger.logMessage(sipMessage, this
                        .getPeerHostPort().toString(), this.getHost() + ":"
                        + this.myPort, false, receptionTime);

            }
            final ServerRequestInterface sipServerRequest = sipStack
                    .newSIPServerRequest(sipRequest, this);
            // Drop it if there is no request returned
            if (sipServerRequest == null) {
                if (logger.isLoggingEnabled()) {
                    logger
                            .logWarning(
                                    "Null request interface returned -- dropping request");
                }

                return;
            }
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                this.logger.logDebug(
                        "About to process " + sipRequest.getFirstLine() + "/"
                                + sipServerRequest);
            try {
                sipServerRequest.processRequest(sipRequest, this);
            } finally {
                if (sipServerRequest instanceof SIPTransaction) {
                    SIPServerTransaction sipServerTx = (SIPServerTransaction) sipServerRequest;
                    if (!sipServerTx.passToListener()) {
                        ((SIPTransaction) sipServerRequest).releaseSem();
                    }
                }
            }
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                this.logger.logDebug(
                        "Done processing " + sipRequest.getFirstLine() + "/"
                                + sipServerRequest);

            // So far so good -- we will commit this message if
            // all processing is OK.

        } else {
            // Handle a SIP Reply message.
            SIPResponse sipResponse = (SIPResponse) sipMessage;
            try {
                sipResponse.checkHeaders();
            } catch (ParseException ex) {
                if (logger.isLoggingEnabled())
                    logger.logError(
                            "Dropping Badly formatted response message >>> "
                                    + sipResponse);
                return;
            }
            ServerResponseInterface sipServerResponse = sipStack
                    .newSIPServerResponse(sipResponse, this);
            if (sipServerResponse != null) {
                try {
                    if (sipServerResponse instanceof SIPClientTransaction
                            && !((SIPClientTransaction) sipServerResponse)
                                    .checkFromTag(sipResponse)) {
                        if (logger.isLoggingEnabled())
                            logger.logError(
                                    "Dropping response message with invalid tag >>> "
                                            + sipResponse);
                        return;
                    }

                    sipServerResponse.processResponse(sipResponse, this);
                } finally {
                    if (sipServerResponse instanceof SIPTransaction
                            && !((SIPTransaction) sipServerResponse)
                                    .passToListener())
                        ((SIPTransaction) sipServerResponse).releaseSem();
                }

                // Normal processing of message.
            } else {
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    this.logger.logDebug(
                            "null sipServerResponse!");
                }
            }

        }
    }

    /**
     * JvB: added method to check for known buggy clients (Windows Messenger) to
     * fix the port to which responses are sent
     *
     * checks for User-Agent: RTC/1.3.5470 (Messenger 5.1.0701)
     *
     * JvB 22/7/2006 better to take this out for the moment, it is only a
     * problem in rare cases (unregister)
     *
     * private final boolean isBuggyClient( SIPRequest r ) { UserAgent uah =
     * (UserAgent) r.getHeader( UserAgent.NAME ); if (uah!=null) {
     * java.util.ListIterator i = uah.getProduct(); if (i.hasNext()) { String p
     * = (String) uah.getProduct().next(); return p.startsWith( "RTC" ); } }
     * return false; }
     */

    /**
     * Implementation of the ParseExceptionListener interface.
     *
     * @param ex
     *            Exception that is given to us by the parser.
     * @throws ParseException
     *             If we choose to reject the header or message.
     */
    public void handleException(ParseException ex, SIPMessage sipMessage,
            Class hdrClass, String header, String message)
            throws ParseException {
        if (logger.isLoggingEnabled())
            this.logger.logException(ex);
        // Log the bad message for later reference.
        if ((hdrClass != null)
                && (hdrClass.equals(From.class) || hdrClass.equals(To.class)
                        || hdrClass.equals(CSeq.class)
                        || hdrClass.equals(Via.class)
                        || hdrClass.equals(CallID.class)
                        || hdrClass.equals(ContentLength.class)
                        || hdrClass.equals(RequestLine.class) || hdrClass
                        .equals(StatusLine.class))) {
            if (logger.isLoggingEnabled()) {
                logger.logError("BAD MESSAGE!");
                logger.logError(message);
            }
            throw ex;
        } else {
            sipMessage.addUnparsed(header);
        }
    }

    /**
     * Return a reply from a pre-constructed reply. This sends the message back
     * to the entity who caused us to create this channel in the first place.
     *
     * @param sipMessage
     *            Message string to send.
     * @throws IOException
     *             If there is a problem with sending the message.
     */
    public void sendMessage(final SIPMessage sipMessage) throws IOException {
        if (logger.isLoggingEnabled(LogLevels.TRACE_INFO)
                && this.sipStack.isLogStackTraceOnMessageSend()) {
            if (sipMessage instanceof SIPRequest
                    && ((SIPRequest) sipMessage).getRequestLine() != null) {
                /*
                 * We dont want to log empty trace messages.
                 */
                this.logger.logStackTrace(
                        StackLogger.TRACE_INFO);
            } else {
                this.logger.logStackTrace(
                        StackLogger.TRACE_INFO);
            }
        }

        // Test and see where we are going to send the messsage. If the message
        // is sent back to oursleves, just
        // shortcircuit processing.
        long time = System.currentTimeMillis();
        try {
            for (MessageProcessor messageProcessor : sipStack
                    .getMessageProcessors()) {
                if (messageProcessor.getIpAddress().equals(this.peerAddress)
                        && messageProcessor.getPort() == this.peerPort
                        && messageProcessor.getTransport().equalsIgnoreCase(
                                this.peerProtocol)) {
                    MessageChannel messageChannel = messageProcessor
                            .createMessageChannel(this.peerAddress,
                                    this.peerPort);
                    if (messageChannel instanceof RawMessageChannel) {

                        final RawMessageChannel channel = (RawMessageChannel) messageChannel;
                        Runnable processMessageTask = new Runnable() {
                            public void run() {
                                try {
                                    ((RawMessageChannel) channel)
                                            .processMessage((SIPMessage) sipMessage.clone());
                                } catch (Exception ex) {
                                    if (logger
                                            .isLoggingEnabled(
                                                    ServerLogger.TRACE_ERROR)) {
                                        logger
                                                .logError(
                                                        "Error self routing message cause by: ",
                                                        ex);
                                    }
                                }
                            }
                        };
                        getSIPStack().getSelfRoutingThreadpoolExecutor()
                                .execute(processMessageTask);
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                            logger.logDebug(
                                    "Self routing message");
                        return;
                    }
                }
            }

            byte[] msg = sipMessage.encodeAsBytes(this.getTransport());

            sendMessage(msg, peerAddress, peerPort, peerProtocol,
                    sipMessage instanceof SIPRequest);

            // we didn't run into problems while sending so let's set ports and
            // addresses before feeding the message to the loggers.
            sipMessage.setRemoteAddress(peerAddress);
            sipMessage.setRemotePort(peerPort);
            sipMessage.setLocalPort(this.getPort());
            sipMessage.setLocalAddress(this.getMessageProcessor().getIpAddress());

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.logError(
                    "An exception occured while sending message", ex);
            throw new IOException("An exception occured while sending message");
        } finally {
            if (logger.isLoggingEnabled(
                    ServerLogger.TRACE_MESSAGES)
                    && !sipMessage.isNullRequest())
                logMessage(sipMessage, peerAddress, peerPort, time);
            else if (logger.isLoggingEnabled(
                    ServerLogger.TRACE_DEBUG))
                logger.logDebug("Sent EMPTY Message");
        }
    }

    /**
     * Send a message to a specified receiver address.
     *
     * @param msg
     *            string to send.
     * @param peerAddress
     *            Address of the place to send it to.
     * @param peerPort
     *            the port to send it to.
     * @throws IOException
     *             If there is trouble sending this message.
     */
    protected void sendMessage(byte[] msg, InetAddress peerAddress,
            int peerPort, boolean reConnect) throws IOException {
        // Via is not included in the request so silently drop the reply.
        if (logger.isLoggingEnabled(LogLevels.TRACE_INFO)
                && this.sipStack.isLogStackTraceOnMessageSend()) {
            this.logger
                    .logStackTrace(StackLogger.TRACE_INFO);
        }
        if (peerPort == -1) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        getClass().getName() + ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        } else {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        "sendMessage " + peerAddress.getHostAddress() + "/"
                                + peerPort + "\n" + "messageSize =  "
                                + msg.length + " message = " + new String(msg));
                this.logger
                        .logDebug("*******************\n");
            }

        }
        DatagramPacket reply = new DatagramPacket(msg, msg.length, peerAddress,
                peerPort);
        try {
            DatagramSocket sock;
            boolean created = false;

            if (sipStack.udpFlag) {
                // Use the socket from the message processor (for firewall
                // support use the same socket as the message processor
                // socket -- feature request # 18 from java.net). This also
                // makes the whole thing run faster!
                sock = ((UDPMessageProcessor) messageProcessor).sock;

                // Bind the socket to the stack address in case there
                // are multiple interfaces on the machine (feature reqeust
                // by Will Scullin) 0 binds to an ephemeral port.
                // sock = new DatagramSocket(0,sipStack.stackInetAddress);
            } else {
                // bind to any interface and port.
                sock = new DatagramSocket();
                created = true;
            }
            sock.send(reply);
            if (created)
                sock.close();
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
    }

    /**
     * Send a message to a specified receiver address.
     *
     * @param msg
     *            message string to send.
     * @param peerAddress
     *            Address of the place to send it to.
     * @param peerPort
     *            the port to send it to.
     * @param peerProtocol
     *            protocol to use to send.
     * @throws IOException
     *             If there is trouble sending this message.
     */
    protected void sendMessage(byte[] msg, InetAddress peerAddress,
            int peerPort, String peerProtocol, boolean retry)
            throws IOException {
        // Via is not included in the request so silently drop the reply.
        if (peerPort == -1) {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        getClass().getName() + ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        } else {
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        ":sendMessage " + peerAddress.getHostAddress() + "/"
                                + peerPort + "\n" + " messageSize = "
                                + msg.length);
            }
        }
        if (peerProtocol.compareToIgnoreCase("UDP") == 0) {
            DatagramPacket reply = new DatagramPacket(msg, msg.length,
                    peerAddress, peerPort);

            try {
                DatagramSocket sock;
                if (sipStack.udpFlag) {
                    sock = ((UDPMessageProcessor) messageProcessor).sock;

                } else {
                    // bind to any interface and port.
                    sock = sipStack.getNetworkLayer().createDatagramSocket();
                }
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    this.logger.logDebug(
                            "sendMessage " + peerAddress.getHostAddress() + "/"
                                    + peerPort + "\n" + new String(msg));
                }
                sock.send(reply);
                if (!sipStack.udpFlag)
                    sock.close();
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
            }

        } else {
            // Use TCP to talk back to the sender.
            Socket outputSocket = sipStack.ioHandler.sendBytes(
                    this.messageProcessor.getIpAddress(), peerAddress,
                    peerPort, "tcp", msg, retry, this);
            OutputStream myOutputStream = outputSocket.getOutputStream();
            myOutputStream.write(msg, 0, msg.length);
            myOutputStream.flush();
            // The socket is cached (dont close it!);
        }
    }

    /**
     * get the stack pointer.
     *
     * @return The sip stack for this channel.
     */
    public SIPTransactionStack getSIPStack() {
        return sipStack;
    }

    /**
     * Return a transport string.
     *
     * @return the string "udp" in this case.
     */
    public String getTransport() {
        return SIPConstants.UDP;
    }

    /**
     * get the stack address for the stack that received this message.
     *
     * @return The stack address for our sipStack.
     */
    public String getHost() {
        return messageProcessor.getIpAddress().getHostAddress();
    }

    /**
     * get the port.
     *
     * @return Our port (on which we are getting datagram packets).
     */
    public int getPort() {
        return ((UDPMessageProcessor) messageProcessor).getPort();
    }

    /**
     * get the name (address) of the host that sent me the message
     *
     * @return The name of the sender (from the datagram packet).
     */
    public String getPeerName() {
        return peerAddress.getHostName();
    }

    /**
     * get the address of the host that sent me the message
     *
     * @return The senders ip address.
     */
    public String getPeerAddress() {
        return peerAddress.getHostAddress();
    }

    protected InetAddress getPeerInetAddress() {
        return peerAddress;
    }

    /**
     * Compare two UDP Message channels for equality.
     *
     * @param other
     *            The other message channel with which to compare oursleves.
     */
    public boolean equals(Object other) {

        if (other == null)
            return false;
        boolean retval;
        if (!this.getClass().equals(other.getClass())) {
            retval = false;
        } else {
            UDPMessageChannel that = (UDPMessageChannel) other;
            retval = this.getKey().equals(that.getKey());
        }

        return retval;
    }

    public String getKey() {
        return getKey(peerAddress, peerPort, "UDP");
    }

    public int getPeerPacketSourcePort() {
        return peerPacketSourcePort;
    }

    public InetAddress getPeerPacketSourceAddress() {
        return peerPacketSourceAddress;
    }

    /**
     * Get the logical originator of the message (from the top via header).
     *
     * @return topmost via header sentby field
     */
    public String getViaHost() {
        return this.myAddress;
    }

    /**
     * Get the logical port of the message orginator (from the top via hdr).
     *
     * @return the via port from the topmost via header.
     */
    public int getViaPort() {
        return this.myPort;
    }

    /**
     * Returns "false" as this is an unreliable transport.
     */
    public boolean isReliable() {
        return false;
    }

    /**
     * UDP is not a secure protocol.
     */
    public boolean isSecure() {
        return false;
    }

    public int getPeerPort() {
        return peerPort;
    }

    public String getPeerProtocol() {
        return this.peerProtocol;
    }

    /**
     * Close the message channel.
     */
    public void close() {
        if (mythread != null) {
            // jeand stop the thread waiting on the queue if the processor is
            // stopped
            mythread.interrupt();
            mythread = null;
        }
    }

}
