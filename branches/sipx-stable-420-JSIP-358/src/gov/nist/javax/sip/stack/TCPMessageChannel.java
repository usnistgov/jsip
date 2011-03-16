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
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.core.*;

import java.net.*;
import java.io.*;
import java.text.ParseException;
import java.util.TimerTask;

import javax.sip.address.Hop;

/*
 * Ahmet Uyar <auyar@csit.fsu.edu>sent in a bug report for TCP operation of the JAIN sipStack.
 * Niklas Uhrberg suggested that a mechanism be added to limit the number of simultaneous open
 * connections. The TLS Adaptations were contributed by Daniel Martinez. Hagai Sela contributed a
 * bug fix for symmetric nat. Jeroen van Bemmel added compensation for buggy clients ( Microsoft
 * RTC clients ). Bug fixes by viswashanti.kadiyala@antepo.com, Joost Yervante Damand
 */

/**
 * This is a stack abstraction for TCP connections. This abstracts a stream of parsed messages.
 * The SIP sipStack starts this from the main SIPStack class for each connection that it accepts.
 * It starts a message parser in its own thread and talks to the message parser via a pipe. The
 * message parser calls back via the parseError or processMessage functions that are defined as
 * part of the SIPMessageListener interface.
 * 
 * @see gov.nist.javax.sip.parser.PipelinedMsgParser
 * 
 * 
 * @author M. Ranganathan <br/>
 * 
 * @version 1.2 $Revision: 1.65.2.9 $ $Date: 2010-12-02 08:06:38 $
 */
public class TCPMessageChannel extends MessageChannel implements SIPMessageListener, Runnable,
        RawMessageChannel {

    private Socket mySock;

    private PipelinedMsgParser myParser;

    protected InputStream myClientInputStream; // just to pass to thread.

    protected OutputStream myClientOutputStream;

    protected String key;

    protected boolean isCached;

    // Set here on initialization to avoid thread leak. See issue 266
    protected boolean isRunning = true;

    private Thread mythread;

    protected SIPTransactionStack sipStack;

    protected String myAddress;

    protected int myPort;

    protected InetAddress peerAddress;

    // This is the TCP source port that the peer actually used the first time he connected to us
    protected int peerPort;
    
    // This is the port that we will find in the headers of the messages from the peer
    protected int peerPortAdvertisedInHeaders;

    protected String peerProtocol;

    // Incremented whenever a transaction gets assigned
    // to the message channel and decremented when
    // a transaction gets freed from the message channel.
    // protected int useCount;

    private TCPMessageProcessor tcpMessageProcessor;

    protected TCPMessageChannel(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;

    }

    /**
     * Constructor - gets called from the SIPStack class with a socket on accepting a new client.
     * All the processing of the message is done here with the sipStack being freed up to handle
     * new connections. The sock input is the socket that is returned from the accept. Global data
     * that is shared by all threads is accessible in the Server structure.
     * 
     * @param sock Socket from which to read and write messages. The socket is already connected
     *        (was created as a result of an accept).
     * 
     * @param sipStack Ptr to SIP Stack
     */

    protected TCPMessageChannel(Socket sock, SIPTransactionStack sipStack,
            TCPMessageProcessor msgProcessor) throws IOException {

        if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            sipStack.getStackLogger().logDebug("creating new TCPMessageChannel ");
            sipStack.getStackLogger().logStackTrace();
        }
        mySock = sock;
        peerAddress = mySock.getInetAddress();
        myAddress = msgProcessor.getIpAddress().getHostAddress();
        myClientInputStream = mySock.getInputStream();
        myClientOutputStream = mySock.getOutputStream();
        mythread = new Thread(this);
        mythread.setDaemon(true);
        mythread.setName("TCPMessageChannelThread");
        // Stash away a pointer to our sipStack structure.
        this.sipStack = sipStack;
        this.peerPort = mySock.getPort();
        this.key = MessageChannel.getKey(peerAddress, peerPort, "TCP");
        
        this.tcpMessageProcessor = msgProcessor;
        this.myPort = this.tcpMessageProcessor.getPort();
        // Bug report by Vishwashanti Raj Kadiayl
        super.messageProcessor = msgProcessor;
        // Can drop this after response is sent potentially.
        mythread.start();
    }

    /**
     * Constructor - connects to the given inet address. Acknowledgement -- Lamine Brahimi (IBM
     * Zurich) sent in a bug fix for this method. A thread was being uncessarily created.
     * 
     * @param inetAddr inet address to connect to.
     * @param sipStack is the sip sipStack from which we are created.
     * @throws IOException if we cannot connect.
     */
    protected TCPMessageChannel(InetAddress inetAddr, int port, SIPTransactionStack sipStack,
            TCPMessageProcessor messageProcessor) throws IOException {
        if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            sipStack.getStackLogger().logDebug("creating new TCPMessageChannel ");
            sipStack.getStackLogger().logStackTrace();
        }
        this.peerAddress = inetAddr;
        this.peerPort = port;
        this.myPort = messageProcessor.getPort();
        this.peerProtocol = "TCP";
        this.sipStack = sipStack;
        this.tcpMessageProcessor = messageProcessor;
        this.myAddress = messageProcessor.getIpAddress().getHostAddress();
        // Bug report by Vishwashanti Raj Kadiayl
        this.key = MessageChannel.getKey(peerAddress, peerPort, "TCP");
        super.messageProcessor = messageProcessor;

    }

    /**
     * Returns "true" as this is a reliable transport.
     */
    public boolean isReliable() {
        return true;
    }

    /**
     * Close the message channel.
     */
    public void close() {   
    	isRunning = false;
    	// we need to close everything because the socket may be closed by the other end
    	// like in LB scenarios sending OPTIONS and killing the socket after it gets the response    	
    	if (mySock != null) {
    		if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    			sipStack.getStackLogger().logDebug("Closing socket " + key);
    		try {
    			mySock.close();
    		} catch (IOException ex) {
    			if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    				sipStack.getStackLogger().logDebug("Error closing socket " + ex);
    		}
    	}        
    	if(myParser != null) {
    		if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    			sipStack.getStackLogger().logDebug("Closing my parser " + myParser);
    		myParser.close();            
    	}  
    	// no need to close myClientInputStream since myParser.close() above will do it
    	if(myClientOutputStream != null) {
    		if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    			sipStack.getStackLogger().logDebug("Closing client output stream " + myClientOutputStream);
    		try {
    			myClientOutputStream.close();
    		} catch (IOException ex) {
    			if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    				sipStack.getStackLogger().logDebug("Error closing client output stream" + ex);
    		}
    	}                     
    	// remove the "tcp:" part of the key to cleanup the ioHandler hashmap
    	String ioHandlerKey = key.substring(4);
    	if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    		sipStack.getStackLogger().logDebug("Closing TCP socket " + ioHandlerKey);
    	// Issue 358 : remove socket and semaphore on close to avoid leaking
    	sipStack.ioHandler.removeSocket(ioHandlerKey);
    	if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    		sipStack.getStackLogger().logDebug("Closing message Channel " + this);       
    }

    /**
     * Get my SIP Stack.
     * 
     * @return The SIP Stack for this message channel.
     */
    public SIPTransactionStack getSIPStack() {
        return sipStack;
    }

    /**
     * get the transport string.
     * 
     * @return "tcp" in this case.
     */
    public String getTransport() {
        return "TCP";
    }

    /**
     * get the address of the client that sent the data to us.
     * 
     * @return Address of the client that sent us data that resulted in this channel being
     *         created.
     */
    public String getPeerAddress() {
        if (peerAddress != null) {
            return peerAddress.getHostAddress();
        } else
            return getHost();
    }

    protected InetAddress getPeerInetAddress() {
        return peerAddress;
    }

    public String getPeerProtocol() {
        return this.peerProtocol;
    }

    /**
     * Send message to whoever is connected to us. Uses the topmost via address to send to.
     * 
     * @param msg is the message to send.
     * @param retry
     */
    private void sendMessage(byte[] msg, boolean retry) throws IOException {

        /*
         * Patch from kircuv@dev.java.net (Issue 119 ) This patch avoids the case where two
         * TCPMessageChannels are now pointing to the same socket.getInputStream().
         * 
         * JvB 22/5 removed
         */
       // Socket s = this.sipStack.ioHandler.getSocket(IOHandler.makeKey(
       // this.peerAddress, this.peerPort));
        Socket sock = null;
        IOException problem = null;
        try {
        	sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(),
                this.peerAddress, this.peerPort, this.peerProtocol, msg, retry, this);
        } catch (IOException any) {
        	problem = any;
        	this.sipStack.getStackLogger().logWarning("Failed to connect " + this.peerAddress + ":" + this.peerPort +" but we can try the advertised port=" + this.peerPortAdvertisedInHeaders);
        }
        if(sock == null) {
        	if(this.peerPort != this.peerPortAdvertisedInHeaders) { // no point in trying same port
        		sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(),
                    this.peerAddress, this.peerPortAdvertisedInHeaders, this.peerProtocol, msg, retry, this);
        		this.peerPort = this.peerPortAdvertisedInHeaders;
        		this.key = MessageChannel.getKey(peerAddress, peerPort, "TCP");
        	} else {
        		throw problem; // throw the original excpetion we had from the first attempt
        	}
        }

        // Created a new socket so close the old one and stick the new
        // one in its place but dont do this if it is a datagram socket.
        // (could have replied via udp but received via tcp!).
        // if (mySock == null && s != null) {
        // this.uncache();
        // } else
        if (sock != mySock && sock != null) {
        	 if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                 sipStack.getStackLogger().logDebug(
                         "Old socket different than new socket");
                 sipStack.getStackLogger().logStackTrace();
                 if(mySock != null) {	                 
	                 sipStack.getStackLogger().logDebug(
	                		 "Old socket local ip address " + mySock.getLocalSocketAddress());
	                 sipStack.getStackLogger().logDebug(
	                		 "Old socket remote ip address " + mySock.getRemoteSocketAddress());
                 }
                 sipStack.getStackLogger().logDebug(
                		 "New socket local ip address " + sock.getLocalSocketAddress());
                 sipStack.getStackLogger().logDebug(
                		 "New socket remote ip address " + sock.getRemoteSocketAddress());
            }
            close();
            mySock = sock;
            this.myClientInputStream = mySock.getInputStream();
            this.myClientOutputStream = mySock.getOutputStream();
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName("TCPMessageChannelThread");
            thread.start();
        }

    }

    /**
     * Return a formatted message to the client. We try to re-connect with the peer on the other
     * end if possible.
     * 
     * @param sipMessage Message to send.
     * @throws IOException If there is an error sending the message
     */
    public void sendMessage(final SIPMessage sipMessage) throws IOException {
    	
        for (MessageProcessor messageProcessor : getSIPStack().getMessageProcessors()) {
        	if (getSIPStack().getStackLogger().isLoggingEnabled(LogWriter.TRACE_DEBUG)){
        		getSIPStack().getStackLogger().logDebug("messageProcessor=" + messageProcessor + 
        				", addr=" + messageProcessor.getIpAddress().getHostAddress() +
        				", peeraddr= " + this.getPeerAddress() + 
        				", mpTransport=" + messageProcessor.getTransport() +
        				", mpPort=" + messageProcessor.getPort() +
        				", peerport=" + this.getPeerPort() +
        				", peertrnsport=" + this.getPeerProtocol() + " EL\n");
        	}

            if (messageProcessor.getIpAddress().getHostAddress().equals(this.getPeerAddress())
                    && messageProcessor.getPort() == this.getPeerPort()
                    && messageProcessor.getTransport().equalsIgnoreCase(this.getPeerProtocol())) {
                	Runnable processMessageTask = new Runnable() {
						
						public void run() {
							try {
								processMessage((SIPMessage) sipMessage.clone());
							} catch (Exception ex) {
								if (getSIPStack().getStackLogger().isLoggingEnabled(ServerLogger.TRACE_ERROR)) {
					        		getSIPStack().getStackLogger().logError("Error self routing message cause by: ", ex);
					        	}
							}
						}
					};
					getSIPStack().getSelfRoutingThreadpoolExecutor().execute(processMessageTask);
                    
                    if (getSIPStack().getStackLogger().isLoggingEnabled(LogWriter.TRACE_DEBUG))
                    	getSIPStack().getStackLogger().logDebug("Self routing message");
                    return;
                }

            }
        byte[] msg = sipMessage.encodeAsBytes(this.getTransport());

        long time = System.currentTimeMillis();

        // JvB: also retry for responses, if the connection is gone we should
        // try to reconnect
        this.sendMessage(msg, /* sipMessage instanceof SIPRequest */true);

        if (this.sipStack.getStackLogger().isLoggingEnabled(ServerLogger.TRACE_MESSAGES))
            logMessage(sipMessage, peerAddress, peerPort, time);
    }

    /**
     * Send a message to a specified address.
     * 
     * @param message Pre-formatted message to send.
     * @param receiverAddress Address to send it to.
     * @param receiverPort Receiver port.
     * @throws IOException If there is a problem connecting or sending.
     */
    public void sendMessage(byte message[], InetAddress receiverAddress, int receiverPort,
            boolean retry) throws IOException {
        if (message == null || receiverAddress == null)
            throw new IllegalArgumentException("Null argument");
         Socket sock = this.sipStack.ioHandler.sendBytes(this.messageProcessor.getIpAddress(),
                receiverAddress, receiverPort, "TCP", message, retry, this);
        if (sock != mySock && sock != null) {
        	 if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                 sipStack.getStackLogger().logDebug(
                         "Old socket different than new socket");
                 sipStack.getStackLogger().logStackTrace();
                 if(mySock != null) {	                 
	                 sipStack.getStackLogger().logDebug(
	                		 "Old socket local ip address " + mySock.getLocalSocketAddress());
	                 sipStack.getStackLogger().logDebug(
	                		 "Old socket remote ip address " + mySock.getRemoteSocketAddress());

                 }
                 sipStack.getStackLogger().logDebug(
                		 "New socket local ip address " + sock.getLocalSocketAddress());
                 sipStack.getStackLogger().logDebug(
                		 "New socket remote ip address " + sock.getRemoteSocketAddress());
            }
        	
            if (mySock != null) {
                /*
                 * Delay the close of the socket for some time in case it is being used.
                 */
                sipStack.getTimer().schedule(new TimerTask() {
                    @Override
                    public boolean cancel() {
                        close();
                        return true;
                    }

                    @Override
                    public void run() {
                        close();
                    }
                }, 8000);
            }

            mySock = sock;
            this.myClientInputStream = mySock.getInputStream();
            this.myClientOutputStream = mySock.getOutputStream();
            // start a new reader on this end of the pipe.
            Thread mythread = new Thread(this);
            mythread.setDaemon(true);
            mythread.setName("TCPMessageChannelThread");
            mythread.start();
        }

    }

    /**
     * Exception processor for exceptions detected from the parser. (This is invoked by the parser
     * when an error is detected).
     * 
     * @param sipMessage -- the message that incurred the error.
     * @param ex -- parse exception detected by the parser.
     * @param header -- header that caused the error.
     * @throws ParseException Thrown if we want to reject the message.
     */
    public void handleException(ParseException ex, SIPMessage sipMessage, Class hdrClass,
            String header, String message) throws ParseException {
        if (sipStack.isLoggingEnabled())
            sipStack.getStackLogger().logException(ex);
        // Log the bad message for later reference.
        if ((hdrClass != null)
                && (hdrClass.equals(From.class) || hdrClass.equals(To.class)
                        || hdrClass.equals(CSeq.class) || hdrClass.equals(Via.class)
                        || hdrClass.equals(CallID.class) || hdrClass.equals(RequestLine.class) || hdrClass
                        .equals(StatusLine.class))) {
            if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                sipStack.getStackLogger().logDebug(
                        "Encountered Bad Message \n" + sipMessage.toString());
            }

            // JvB: send a 400 response for requests (except ACK)
            // Currently only UDP, @todo also other transports
            String msgString = sipMessage.toString();
            if (!msgString.startsWith("SIP/") && !msgString.startsWith("ACK ")) {

                String badReqRes = createBadReqRes(msgString, ex);
                if (badReqRes != null) {
                    if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        sipStack.getStackLogger().logDebug("Sending automatic 400 Bad Request:");
                        sipStack.getStackLogger().logDebug(badReqRes);
                    }
                    try {
                        this.sendMessage(badReqRes.getBytes(), this.getPeerInetAddress(), this
                                .getPeerPort(), false);
                    } catch (IOException e) {
                        this.sipStack.getStackLogger().logException(e);
                    }
                } else {
                    if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        sipStack.getStackLogger().logDebug(
                                "Could not formulate automatic 400 Bad Request");
                    }
                }
            }

            throw ex;
        } else {
            sipMessage.addUnparsed(header);
        }
    }
    
    public void processMessage(SIPMessage sipMessage, InetAddress address) {
    	this.peerAddress = address;
    	try {
			processMessage(sipMessage);
		} catch (Exception e) {
			if(sipStack.getStackLogger().isLoggingEnabled(ServerLog.TRACE_ERROR)) {
				sipStack.getStackLogger().logError("ERROR processing self routing", e);
			}
		}
    }

    /**
     * Gets invoked by the parser as a callback on successful message parsing (i.e. no parser
     * errors).
     * 
     * @param sipMessage Mesage to process (this calls the application for processing the
     *        message).
     */
    public void processMessage(SIPMessage sipMessage) throws Exception {
        try {
            if (sipMessage.getFrom() == null
                    || // sipMessage.getFrom().getTag()
                    // == null ||
                    sipMessage.getTo() == null || sipMessage.getCallId() == null
                    || sipMessage.getCSeq() == null || sipMessage.getViaHeaders() == null) {
                String badmsg = sipMessage.encode();
                if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    sipStack.getStackLogger().logDebug(">>> Dropped Bad Msg");
                    sipStack.getStackLogger().logDebug(badmsg);
                }

                return;
            }

            ViaList viaList = sipMessage.getViaHeaders();
            // For a request
            // first via header tells where the message is coming from.
            // For response, this has already been recorded in the outgoing
            // message.
            if (sipMessage instanceof SIPRequest) {
                Via v = (Via) viaList.getFirst();
                Hop hop = sipStack.addressResolver.resolveAddress(v.getHop());
                this.peerProtocol = v.getTransport();
                this.peerPortAdvertisedInHeaders = hop.getPort();
                try {
                	if(mySock != null) { // selfrouting makes socket = null
                		this.peerAddress = mySock.getInetAddress();
                	}
                    // Check to see if the received parameter matches
                    // the peer address and tag it appropriately.

                    // JvB: dont do this. It is both costly and incorrect
                    // Must set received also when it is a FQDN, regardless
                    // whether
                    // it resolves to the correct IP address
                    // InetAddress sentByAddress =
                    // InetAddress.getByName(hop.getHost());
                    // JvB: if sender added 'rport', must always set received
                    if (v.hasParameter(Via.RPORT)
                            || !hop.getHost().equals(this.peerAddress.getHostAddress())) {
                        v.setParameter(Via.RECEIVED, this.peerAddress.getHostAddress());
                    }
                    // @@@ hagai
                    // JvB: technically, may only do this when Via already
                    // contains
                    // rport
                    v.setParameter(Via.RPORT, Integer.toString(this.peerPort));
                } catch (java.text.ParseException ex) {
                    InternalErrorHandler.handleException(ex, sipStack.getStackLogger());
                }
                // Use this for outgoing messages as well.
                if (!this.isCached && mySock != null) {
                    this.isCached = true;
                    int remotePort = ((java.net.InetSocketAddress) mySock.getRemoteSocketAddress()).getPort();
                    String key = IOHandler.makeKey(mySock.getInetAddress(), remotePort);
                    sipStack.ioHandler.putSocket(key, mySock);
                    // since it can close the socket it needs to be after the mySock usage otherwise
                    // it the socket will be disconnected and NPE will be thrown in some edge cases
                    ((TCPMessageProcessor) this.messageProcessor)
                            .cacheMessageChannel(this);
                }
            }

         
            // Foreach part of the request header, fetch it and process it

            long receptionTime = System.currentTimeMillis();

            if (sipMessage instanceof SIPRequest) {
                // This is a request - process the request.
                SIPRequest sipRequest = (SIPRequest) sipMessage;
                // Create a new sever side request processor for this
                // message and let it handle the rest.

                if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    sipStack.getStackLogger().logDebug("----Processing Message---");
                }

                // Check for reasonable size - reject message
                // if it is too long.
                if (this.sipStack.getStackLogger().isLoggingEnabled(ServerLogger.TRACE_MESSAGES)) {
                    sipStack.serverLogger.logMessage(sipMessage, this.getPeerHostPort().toString(),
                            this.getMessageProcessor().getIpAddress().getHostAddress() + ":"
                                    + this.getMessageProcessor().getPort(), false, receptionTime);

                }

                if (sipStack.getMaxMessageSize() > 0
                        && sipRequest.getSize()
                                + (sipRequest.getContentLength() == null ? 0 : sipRequest
                                        .getContentLength().getContentLength()) > sipStack
                                .getMaxMessageSize()) {
                    SIPResponse sipResponse = sipRequest
                            .createResponse(SIPResponse.MESSAGE_TOO_LARGE);
                    byte[] resp = sipResponse.encodeAsBytes(this.getTransport());
                    this.sendMessage(resp, false);
                    throw new Exception("Message size exceeded");
                }

                ServerRequestInterface sipServerRequest = sipStack.newSIPServerRequest(
                        sipRequest, this);

                if (sipServerRequest != null) {
                    try {
                        sipServerRequest.processRequest(sipRequest, this);
                    } finally {
                        if (sipServerRequest instanceof SIPTransaction) {
                            SIPServerTransaction sipServerTx = (SIPServerTransaction) sipServerRequest;
                            if (!sipServerTx.passToListener())
                                ((SIPTransaction) sipServerRequest).releaseSem();
                        }
                    }
                } else {
                	if (sipStack.isLoggingEnabled())
                		this.sipStack.getStackLogger()
                            .logWarning("Dropping request -- could not acquire semaphore in 10 sec");
                }

            } else {
                SIPResponse sipResponse = (SIPResponse) sipMessage;
                // JvB: dont do this
                // if (sipResponse.getStatusCode() == 100)
                // sipResponse.getTo().removeParameter("tag");
                try {
                    sipResponse.checkHeaders();
                } catch (ParseException ex) {
                    if (sipStack.isLoggingEnabled())
                        sipStack.getStackLogger()
                                .logError("Dropping Badly formatted response message >>> "
                                        + sipResponse);
                    return;
                }
                // This is a response message - process it.
                // Check the size of the response.
                // If it is too large dump it silently.
                if (sipStack.getMaxMessageSize() > 0
                        && sipResponse.getSize()
                                + (sipResponse.getContentLength() == null ? 0 : sipResponse
                                        .getContentLength().getContentLength()) > sipStack
                                .getMaxMessageSize()) {
                    if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                        this.sipStack.getStackLogger().logDebug("Message size exceeded");
                    return;

                }
                ServerResponseInterface sipServerResponse = sipStack.newSIPServerResponse(
                        sipResponse, this);
                if (sipServerResponse != null) {
                    try {
                        if (sipServerResponse instanceof SIPClientTransaction
                                && !((SIPClientTransaction) sipServerResponse)
                                        .checkFromTag(sipResponse)) {
                            if (sipStack.isLoggingEnabled())
                                sipStack.getStackLogger()
                                        .logError("Dropping response message with invalid tag >>> "
                                                + sipResponse);
                            return;
                        }

                        sipServerResponse.processResponse(sipResponse, this);
                    } finally {
                        if (sipServerResponse instanceof SIPTransaction
                                && !((SIPTransaction) sipServerResponse).passToListener())
                            ((SIPTransaction) sipServerResponse).releaseSem();
                    }
                } else {
                    sipStack
                            .getStackLogger()
                            .logWarning(
                                    "Application is blocked -- could not acquire semaphore -- dropping response");
                }
            }
        } finally {
        }
    }

    /**
     * This gets invoked when thread.start is called from the constructor. Implements a message
     * loop - reading the tcp connection and processing messages until we are done or the other
     * end has closed.
     */
    public void run() {
        Pipeline hispipe = null;
        // Create a pipeline to connect to our message parser.
        hispipe = new Pipeline(myClientInputStream, sipStack.readTimeout,
                ((SIPTransactionStack) sipStack).getTimer());
        // Create a pipelined message parser to read and parse
        // messages that we write out to him.
        if(myParser != null) {
        	if (sipStack.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
                sipStack.getStackLogger().logError(
                        "myParser is not null, we are overwriting it, making sure we close the parser");                        
        	}
        	myParser.close();
        	myParser = null;
        }
        myParser = new PipelinedMsgParser(sipStack, this, hispipe, this.sipStack.getMaxMessageSize());
        // Start running the parser thread.
        myParser.processInput();
        // bug fix by Emmanuel Proulx
        int bufferSize = 4096;
        this.tcpMessageProcessor.useCount++;
        this.isRunning = true;
        try {
            while (true) {
                try {
                    byte[] msg = new byte[bufferSize];
                    int nbytes = myClientInputStream.read(msg, 0, bufferSize);
                    // no more bytes to read...
                    if (nbytes == -1) {
                        hispipe.write("\r\n\r\n".getBytes("UTF-8"));
                        try {
                            if (sipStack.maxConnections != -1) {
                                synchronized (tcpMessageProcessor) {
                                    tcpMessageProcessor.nConnections--;
                                    tcpMessageProcessor.notify();
                                }
                            }
                            hispipe.close();
                            close();
                        } catch (IOException ioex) {
                        }
                        return;
                    }
                    hispipe.write(msg, 0, nbytes);

                } catch (IOException ex) {
                    // Terminate the message.
                    try {
                        hispipe.write("\r\n\r\n".getBytes("UTF-8"));
                    } catch (Exception e) {
                        // InternalErrorHandler.handleException(e);
                    }

                    try {
                        if (sipStack.isLoggingEnabled(LogWriter.TRACE_DEBUG))
                            sipStack.getStackLogger().logDebug("IOException  closing sock " + ex);
                        try {
                            if (sipStack.maxConnections != -1) {
                                synchronized (tcpMessageProcessor) {
                                    tcpMessageProcessor.nConnections--;
                                    // System.out.println("Notifying!");
                                    tcpMessageProcessor.notify();
                                }
                            }
                            close();
                            hispipe.close();
                        } catch (IOException ioex) {
                        }
                    } catch (Exception ex1) {
                        // Do nothing.
                    }
                    return;
                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex, sipStack.getStackLogger());
                }
            }
        } finally {
            this.isRunning = false;
            this.tcpMessageProcessor.remove(this);
            this.tcpMessageProcessor.useCount--;
            // parser could be null if the socket was closed by the remote end already
            if(myParser != null) {
            	myParser.close();
            }
        }

    }

    protected void uncache() {
    	if (isCached  && !isRunning ) {
    		this.tcpMessageProcessor.remove(this);
    	}
    }

    /**
     * Equals predicate.
     * 
     * @param other is the other object to compare ourselves to for equals
     */

    public boolean equals(Object other) {
    	if(other==null) return false;
        if (!this.getClass().equals(other.getClass()))
            return false;
        else {
            TCPMessageChannel that = (TCPMessageChannel) other;
            if (this.mySock != that.mySock)
                return false;
            else
                return true;
        }
    }

    /**
     * Get an identifying key. This key is used to cache the connection and re-use it if
     * necessary.
     */
    public String getKey() {
        if (this.key != null) {
            return this.key;
        } else {
            this.key = MessageChannel.getKey(this.peerAddress, this.peerPort, "TCP");
            return this.key;
        }
    }

    /**
     * Get the host to assign to outgoing messages.
     * 
     * @return the host to assign to the via header.
     */
    public String getViaHost() {
        return myAddress;
    }

    /**
     * Get the port for outgoing messages sent from the channel.
     * 
     * @return the port to assign to the via header.
     */
    public int getViaPort() {
        return myPort;
    }

    /**
     * Get the port of the peer to whom we are sending messages.
     * 
     * @return the peer port.
     */
    public int getPeerPort() {
        return peerPort;
    }

    public int getPeerPacketSourcePort() {
        return this.peerPort;
    }

    public InetAddress getPeerPacketSourceAddress() {
        return this.peerAddress;
    }

    /**
     * TCP Is not a secure protocol.
     */
    public boolean isSecure() {
        return false;
    }
}
