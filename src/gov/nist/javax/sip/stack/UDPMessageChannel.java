/*****************************************************************************
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 *****************************************************************************/

package gov.nist.javax.sip.stack;
import java.net.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.String;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Collection;
import java.text.ParseException;

/**
 * This is the UDP Message handler that gets created when a UDP message
 * needs to be processed. The message is processed by creating a String
 * Message parser and invoking it on the message read from the UPD socket.
 * The parsed structure is handed off via a SIP stack request for further
 * processing. This stack structure isolates the message handling logic
 * from the mechanics of sending and recieving messages (which could
 * be either udp or tcp.
 *
 *@see gov.nist.javax.sip.parser.StringMsgParser
 *@see gov.nist.javax.sip.stack.SIPServerRequestInterface
 *@author <A href=mailto:mranga@nist.gov> M. Ranganathan </A>
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * Acknowledgement: Kim Kirby of Keyvoice suggested that duplicate checking
 * should be added to the stack (later removed).
 * Lamine Brahimi suggested a single threaded behavior flag
 * be added to this. Niklas Uhrberg suggested that thread pooling support
 * be added to this for performance and resource management.
 *
 *@version  JAIN-SIP-1.1
 *
 *
 */


public class UDPMessageChannel
extends  MessageChannel
implements  ParseExceptionListener, Runnable {
    public static final String SIPHEADERS_PACKAGE = 
            PackageNames.SIPHEADERS_PACKAGE;
    
    /** Channel notifier (gets called on new channel creates).
     */
    protected ChannelNotifier notifier;
    /** SIP Stack structure for this channel.
     */
    protected SIPStack stack;
    /** The parser we are using for messages received from this channel.
     */
    protected StringMsgParser myParser;
    
    /** Where we got the stuff from */
    private InetAddress peerAddress;
    

    private String  myAddress;
    
    /** Reciever port -- port of the destination.  */
    private int peerPort;
    
    /** Protocol to use when talking to receiver (i.e. when sending replies).
     */
    private String peerProtocol;
    
    protected  int myPort;
    
    
    private  byte[] msgBytes;
    
    
    private int packetLength;
    
    
    private DatagramPacket incomingPacket;
    
    
    private long receptionTime;
    
    
    
    
    /**
     * Constructor - takes a datagram packet and a stack structure
     * Extracts the address of the other from the datagram packet and
     * stashes away the pointer to the passed stack structure.
     * @param packet is the UDP Packet that contains the request.
     * @param stack is the shared SIPStack structure
     * @param notifier Channel notifier (not very useful for UDP).
     *
     */
    protected UDPMessageChannel(SIPStack stack,
    ChannelNotifier notifier,
    UDPMessageProcessor messageProcessor ) {
        super.messageProcessor = messageProcessor;
        this.stack = stack;
        Thread mythread = new Thread(this);
        mythread.start();
        
    }
    
    /**
     * Constructor. We create one of these in order to process an incoming
     * message.
     *
     *@param stack is the SIP stack.
     *@param notifier is the channel notifier (not particularly relevant here).
     *@param messageProcesor is the creating message processor.
     *@param packet is the incoming datagram packet.
     */
    protected UDPMessageChannel(SIPStack stack,
    ChannelNotifier notifier,
    UDPMessageProcessor messageProcessor,
    DatagramPacket packet) {
        
        this.incomingPacket = packet;
        super.messageProcessor = messageProcessor;
        this.stack = stack;
        this.myAddress = stack.getHostAddress();
        this.myPort = messageProcessor.getPort();
        Thread mythread = new Thread(this);
        mythread.start();
        
    }
    
    
    /**
     * Constructor. We create one of these when we send out a message.
     *@param targetAddr INET address of the place where we want to send
     *		messages.
     *@param port target port (where we want to send the message).
     *@param stack our SIP Stack.
     */
    protected UDPMessageChannel(InetAddress targetAddr, int port,
    SIPStack sipStack, UDPMessageProcessor messageProcessor ) {
        if (LogWriter.needsLogging) {
            LogWriter.logMessage( "Creating message channel " +
            targetAddr.getHostAddress() + "/" + port);
        }
        peerAddress = targetAddr;
        peerPort = port;
        peerProtocol = "UDP";
        super.messageProcessor = messageProcessor;
        this.myAddress = sipStack.getHostAddress();
        this.myPort = messageProcessor.getPort();
        stack = sipStack;
    }
    
    /**
     * Run method specified by runnnable.
     */
    
    public void run() {
        // Notify application that a new message channel has been created.
        if (notifier != null ) {
            notifier.notifyOpen(this);
        }
        while(true) {
            // Create a new string message parser to parse the list of messages.
            // This is a huge performance hit -- need to optimize by pre-create
            // parser when one is needed....
            if (myParser == null) {
                myParser = new StringMsgParser();
                myParser.setParseExceptionListener(this);
            }
            // messages that we write out to him.
            DatagramPacket packet;
            
            if ( stack.threadPoolSize != -1 ) {
                synchronized
		   (((UDPMessageProcessor)messageProcessor).messageQueue) {
                    while (((UDPMessageProcessor)messageProcessor).
			    messageQueue.isEmpty()) {
                        // Check to see if we need to exit.
                        if (!((UDPMessageProcessor)messageProcessor).isRunning)
				 return;
                        try {
                            ((UDPMessageProcessor)messageProcessor).
				messageQueue.wait();
                        } catch (InterruptedException ex) {
                           if (!((UDPMessageProcessor) messageProcessor).isRunning) 
				return;
                        }
                    }
                    packet =
                    (DatagramPacket)
                    ((UDPMessageProcessor)messageProcessor).messageQueue.
				removeFirst();
			
                }
                this.incomingPacket = packet;
            } else {
                packet = this.incomingPacket;
            }
            
            this.peerAddress = packet.getAddress();
            this.packetLength = packet.getLength();
            // Read bytes and put it in a eueue.
            byte[] bytes =  packet.getData();
            byte[] msgBytes = new byte[packetLength];
            System.arraycopy(bytes,0,msgBytes,0,packetLength);
            
            // Do debug logging.
            if (LogWriter.needsLogging) {
                LogWriter.logMessage( "UDPMessageChannel: peerAddress = " +
                peerAddress.getHostAddress() + "/" + packet.getPort());
                LogWriter.logMessage( "Length = " + packetLength);
                String msgString = new String(msgBytes,0,packetLength);
                LogWriter.logMessage(msgString);
            }
            
            
            SIPMessage[] sipMessages = null;
            SIPMessage sipMessage = null;
            try {
                this.receptionTime = System.currentTimeMillis();
                sipMessage  = myParser.parseSIPMessage(msgBytes);
                myParser  = null;
            } catch ( ParseException ex) {
                myParser  = null; // let go of the parser reference.
                if (LogWriter.needsLogging) {
                    LogWriter.logMessage( "Rejecting message !  " 
			+ new String(msgBytes));
                    LogWriter.logMessage("error message " +
                    ex.getMessage());
                    LogWriter.logException(ex);
                }
                stack.logBadMessage(new String(msgBytes));
                if (stack.threadPoolSize == -1) return;
                else continue;
            }
            // No parse exception.
           if (sipMessage == null) continue;
           ViaList viaList = sipMessage.getViaHeaders();
           // Check for the required headers.
           if (sipMessage.getFrom()   == null  	 	||
	       //sipMessage.getFrom().getTag() == null  ||
               sipMessage.getTo()     == null  	 	||
               sipMessage.getCallId() == null 	 	||
               sipMessage.getCSeq()   == null 	 	||
               sipMessage.getViaHeaders()    == null	 
           ){
               String badmsg = new String(msgBytes);
                if (LogWriter.needsLogging)  {
                       LogWriter.logMessage("bad message " + badmsg);
                       LogWriter.logMessage(">>> Dropped Bad Msg " + 
			"From = " + sipMessage.getFrom() 	+
			"To = " + sipMessage.getTo() 		+
			"CallId = " + sipMessage.getCallId() 	+
			"CSeq = " + sipMessage.getCSeq() 	+
			"Via = " + sipMessage.getViaHeaders() 	);
                   }
		 
                 stack.logBadMessage(badmsg);
                 if (stack.threadPoolSize == -1) return;
                 else continue;
            }
                // For a request first via header tells where the message
                // is coming from.
                // For response, just get the port from the packet.
                if (sipMessage instanceof SIPRequest) {
                    Via v = (Via)viaList.first();
                    if (v.hasPort() ) {
                        if (sipMessage instanceof SIPRequest)  {
                            this.peerPort = v.getPort();
                        }
                    }  else this.peerPort = SIPStack.DEFAULT_PORT;
                    this.peerProtocol = v.getTransport();
                    try {
			this.peerAddress =  packet.getAddress();
		 	// Check to see if the received parameter matches
			// the peer address and tag it appropriately.
			// Bug fix by viswashanti.kadiyala@antepo.com
		        if ( !v.getSentBy().getInetAddress().equals
			    (this.peerAddress))
			   v.setParameter(Via.RECEIVED,
			   this.peerAddress.getHostName());

                        // this.peerAddress = v.getSentBy().getInetAddress();
                    } catch (java.net.UnknownHostException ex) {
                        // Could not resolve the sender address.
                        if (ServerLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
                            ServerLog.logMessage(sipMessage,
                            this.getViaHost() + ":" + this.getViaPort(),
                            stack.getHostAddress()  + ":" +
                            stack.getPort(this.getTransport()),
                            "Dropped -- " +
                            "Could not resolve VIA header address!", false);
                        }
                        if ( LogWriter.needsLogging) {
                            LogWriter.logMessage
                            ("Rejecting message -- "+
                            "could not resolve Via Address");
                        }
                        
                        continue;
                    } catch (java.text.ParseException ex1) {
			InternalErrorHandler.handleException(ex1);
		    }
		
                }
                
                if (sipMessage instanceof SIPRequest) {
                    SIPRequest sipRequest = (SIPRequest) sipMessage;
                    
                    // This is a request - process it.
                    SIPServerRequestInterface sipServerRequest =
                    stack.newSIPServerRequest(sipRequest,this);
                    // Drop it if there is no request returned
                    if (sipServerRequest == null) {
                        if (LogWriter.needsLogging) {
                            LogWriter.logMessage
                            ("Null request interface returned");
                        }
                        continue;
                    }
                    try {
                        if (LogWriter.needsLogging)
                            LogWriter.logMessage("About to process " +
                            sipRequest.getFirstLine() + "/" +
                            sipServerRequest);
                        sipServerRequest.processRequest(sipRequest,this);
                        if (LogWriter.needsLogging)
                            LogWriter.logMessage("Done processing " +
                            sipRequest.getFirstLine() + "/" +
                            sipServerRequest);
                        
                        // So far so good -- we will commit this message if
                        // all processing is OK.
                        if ( ServerLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
                            if (sipServerRequest.getProcessingInfo() == null) {
                                ServerLog.logMessage(sipMessage,
                                sipRequest.getViaHost() + ":" +
                                sipRequest.getViaPort(),
                                stack.getHostAddress()  + ":" +
                                stack.getPort(this.getTransport()),false,
                                new Long(receptionTime).toString());
                            } else {
                                ServerLog.logMessage(sipMessage,
                                sipRequest.getViaHost() + ":" +
                                sipRequest.getViaPort(),
                                stack.getHostAddress()  + ":" +
                                stack.getPort(this.getTransport()),
                                sipServerRequest.getProcessingInfo(),
                                false,new Long(receptionTime).toString());
                            }
                        }
                    } catch (SIPServerException ex) {
                        // So far so good -- we will commit this message if
                        // all processing is OK.
                        if ( ServerLog.needsLogging(ServerLog.TRACE_MESSAGES)) {
                            ServerLog.logMessage(sipMessage,
                            sipRequest.getViaHost() + ":" +
                            sipRequest.getViaPort(),
                            stack.getHostAddress()  + ":" +
                            stack.getPort(this.getTransport()),
                            ex.getMessage(), false,
                            new Long(receptionTime).toString());
                        }
                        handleException(ex);
                    }
                } else {
                    // Handle a SIP Reply message.
                    SIPResponse sipResponse = (SIPResponse) sipMessage;
                    SIPServerResponseInterface sipServerResponse =
                    stack.newSIPServerResponse(sipResponse,this);
                    try {
                        if (sipServerResponse != null) {
                            sipServerResponse.processResponse(sipResponse,this);
                            // Normal processing of message.
                        } else {
                            if (LogWriter.needsLogging) {
                                LogWriter.logMessage("null sipServerResponse!");
                            }
                        }
                        
                    } catch (SIPServerException ex) {
                        if (LogWriter.needsLogging)
                            LogWriter.logMessage(">>>>>Message  = " +
                            new String(msgBytes));
                        
                        if (ServerLog.needsLogging
                        (ServerLog.TRACE_MESSAGES)){
                            this.logResponse(sipResponse,
                            receptionTime,
                            ex.getMessage()+ "-- Dropped!");
                        }
                        
                        ServerLog.logException(ex);
                    }
                }
            ((UDPMessageProcessor)messageProcessor).useCount --;
            if (stack.threadPoolSize == -1) {
                return;
            }
        }
    }
    
    
    
    /**
     * Implementation of the ParseExceptionListener interface.
     * @param ex Exception that is given to us by the parser.
     * @throws ParseException If we choose to reject the header or message.
     */
    
    public void handleException( ParseException ex,
    SIPMessage sipMessage,
    Class hdrClass, String header, String message )
    throws ParseException {
        if (LogWriter.needsLogging) LogWriter.logException(ex);
        // Log the bad message for later reference.
        if ( hdrClass.equals(From.class)||
             hdrClass.equals(To.class )      ||
             hdrClass.equals(CSeq.class)     ||
             hdrClass.equals(Via.class)      ||
             hdrClass.equals(CallID.class)   ||
             hdrClass.equals(RequestLine.class)||
             hdrClass.equals(StatusLine.class)) {
                stack.logBadMessage(message);
                throw ex;
         }else {
                sipMessage.addUnparsed(header);
         }
    }
    
    
    /** Return a reply from a pre-constructed reply. This sends the message
     * back to the entity who caused us to create this channel in the
     * first place.
     * @param msg Message string to send.
     * @throws IOException If there is a problem with sending the
     * message.
     */
    public void sendMessage(SIPMessage sipMessage) throws IOException {
	if (LogWriter.needsLogging) 
		LogWriter.logStackTrace();
        byte[] msg = sipMessage.encodeAsBytes();
        long time = System.currentTimeMillis();
        sendMessage(msg, peerAddress, peerPort,peerProtocol);
        if (ServerLog.needsLogging(ServerLog.TRACE_MESSAGES))
            logMessage(sipMessage,peerAddress,peerPort,time);
    }
    
    
    /** Send a message to a specified receiver address.
     * @param msg message string to send.
     * @param peerAddress Address of the place to send it to.
     * @param peerPort the port to send it to.
     * @throws IOException If there is trouble sending this message.
     */
    protected void sendMessage(byte[] msg, InetAddress peerAddress,
    int peerPort) throws IOException {
        // msg += "\r\n\r\n";
        // Via is not included in the request so silently drop the reply.
        if (LogWriter.needsLogging) LogWriter.logStackTrace();
        if (peerPort == -1) {
            if (LogWriter.needsLogging) {
                LogWriter.logMessage(getClass().getName()+
                ":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        } else {
            if (LogWriter.needsLogging) {
                LogWriter.logMessage(
                getClass().getName()+":sendMessage "
                + peerAddress.getHostAddress() + "/" +
                peerPort + "\n" + new String(msg));
                LogWriter.logMessage( "*******************\n");
            }
        }
        DatagramPacket reply = new DatagramPacket(msg,
        msg.length, peerAddress, peerPort);
        try {
            DatagramSocket sock;
            if (stack.udpFlag) {
                // Bind the socket to the stack address in case there
                // are multiple interfaces on the machine (feature reqeust
                // by Will Scullin) 0 binds to an ephemeral port.
                sock = new DatagramSocket(0,stack.stackInetAddress);
            } else {
                // bind to any interface and port.
                sock = new DatagramSocket();
            }
            sock.send(reply);
            sock.close();
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            InternalErrorHandler.handleException(ex);
        }
    }
    /** Send a message to a specified receiver address.
     * @param msg message string to send.
     * @param peerAddress Address of the place to send it to.
     * @param peerPort the port to send it to.
     * @param peerProtocol protocol to use to send.
     * @throws IOException If there is trouble sending this message.
     */
    protected void sendMessage(byte[] msg, InetAddress peerAddress,
    int peerPort, String peerProtocol) throws IOException {
        // msg += "\r\n\r\n";
        // Via is not included in the request so silently drop the reply.
        if (peerPort == -1) {
            if (LogWriter.needsLogging) {
                LogWriter.logMessage(
                getClass().getName()+":sendMessage: Dropping reply!");
            }
            throw new IOException("Receiver port not set ");
        } else {
            if (LogWriter.needsLogging) {
                LogWriter.logMessage(
                getClass().getName()+":sendMessage "
                + peerAddress.getHostAddress() + "/" +
                peerPort + "\n" + new String(msg));
                LogWriter.logMessage("*******************\n");
            }
        }
        if (peerProtocol.compareToIgnoreCase("UDP") == 0) {
            DatagramPacket reply = new DatagramPacket(msg,
            msg.length, peerAddress, peerPort);
            
            try {
                DatagramSocket sock;
                if (stack.udpFlag) {
                    // Bind the socket to the stack address in case there
                    // are multiple interfaces on the machine (feature reqeust
                    // by Will Scullin) 0 binds to an ephemeral port.
                    sock = new DatagramSocket(0,stack.stackInetAddress);
                } else {
                    // bind to any interface and port.
                    sock = new DatagramSocket();
                }
                sock.send(reply);
                sock.close();
            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
            }
            
        } else {
            // Use TCP to talk back to the sender.
	    // System.out.println("peerAddress " + peerPort);
            Socket outputSocket = new Socket(peerAddress,peerPort);
            OutputStream myOutputStream = outputSocket.getOutputStream();
            myOutputStream.write(msg, 0, msg.length);
            myOutputStream.flush();
            outputSocket.close();
        }
    }
    
    /** get the stack pointer.
     * @return The sip stack for this channel.
     */
    public SIPStack getSIPStack() {
        return stack;
    }
    
    /**
     * Return a transport string.
     * @return the string "udp" in this case.
     */
    
    public String getTransport() {
        return SIPConstants.UDP;
    }
    
    /**
     * get the stack address for the stack that received this message.
     * @return The stack address for our stack.
     */
    public String getHost() {
        return stack.stackAddress;
    }
    /** get the port.
     * @return Our port (on which we are getting datagram
     * packets).
     */
    public int getPort() {
        return ((UDPMessageProcessor)messageProcessor).port;
    }
    
    
    /** Handle an exception - construct a sip reply and send it back to the
     * caller.
     * @param ex The exception thrown at us by our
     * application.
     */
    
    public void handleException(SIPServerException ex) {
        // Return a parse error message to the client on the other end
        // if he is still alive.
        // ex.printStackTrace();
        int rc = ex.getRC();
        SIPRequest request = (SIPRequest) ex.getSIPMessage();
        SIPResponse response;
        String msgString = ex.getMessage();
        if (rc != 0) {
            response = request.createResponse(rc,msgString);
            // messageFormatter.newSIPResponse(rc,request,msgString);
            try {
                sendMessage(response);
            } catch (IOException ioex) {
                ServerLog.logException(ioex);
            }
        }  else {
            // Assume that the message has already been formatted.
            try {
                sendMessage(msgString);
            } catch (IOException ioex) {
                ServerLog.logException(ioex);
            }
        }
    }
    
    /** get the name (address) of the host that sent me the message
     * @return The name of the sender (from
     * the datagram packet).
     */
    public String getPeerName() {
        return peerAddress.getHostName();
    }
    
    /**
     * get the address of the host that sent me the message
     * @return The senders ip address.
     */
    public String getPeerAddress() {
        return peerAddress.getHostAddress();
    }
    
    
    /** Compare two UDP Message channels for equality.
     *@param other The other message channel with which to compare oursleves.
     */
    public boolean equals(Object other) {
        
        if (other == null ) return false;
        boolean retval;
        if (!this.getClass().equals(other.getClass())) {
            retval =  false;
        } else {
            UDPMessageChannel that = (UDPMessageChannel) other;
            retval =  this.getKey().equals(that.getKey());
        }
        
        return retval;
    }
    
    
    
    public String getKey() {
        return getKey(peerAddress,peerPort,"UDP");
    }
    
    
    private void sendMessage(String msg)
    throws IOException {
        sendMessage(msg.getBytes(),peerAddress,
        peerPort,peerProtocol);
    }
    
    private void sendMessage(byte[] msg)
    throws IOException {
        sendMessage(msg,peerAddress,peerPort,peerProtocol);
    }
    
    /** Get the logical originator of the message (from the top via header).
     *@return topmost via header sentby field
     */
    public String getViaHost() {
        return this.myAddress;
    }
    
    /** Get the logical port of the message orginator (from the top via hdr).
     *@return the via port from the topmost via header.
     */
    public int  getViaPort() { return this.myPort; }
    
    /** Returns "false" as this is an unreliable transport.
     */
    public boolean isReliable() {
        return false;
    }
    
    /** UDP is not a secure protocol.
     */
    public boolean isSecure() {
        return false;
    }
    
    public int getPeerPort() { return peerPort; }

	
    /** Close the message channel.
    */
    public void close() {}

    
}
