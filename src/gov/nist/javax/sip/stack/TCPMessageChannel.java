/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.core.*;
import java.net.*;
import java.io.*;
import java.text.ParseException;
//ifdef SIMULATION
/*
import sim.java.*;
import sim.java.net.*;
//endif
*/
/**
 * This is stack for TCP connections. This abstracts a stream of parsed
 * messages. The SIP stack starts this from
 * the main SIPStack class for each connection that it accepts. It starts
 * a message parser in its own thread and talks to the message parser via
 * a pipe. The message parser calls back via the parseError or processMessage
 * functions that are defined as part of the SIPMessageListener interface.
 *
 * @see gov.nist.javax.sip.parser.PipelinedMsgParser
 *
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * Acknowedgement: Ahmet Uyar  <auyar@csit.fsu.edu> sent in a bug report
 * for TCP operation of the JAIN stack.
 * Niklas Uhrberg suggested that a mechanism be added to limit the number
 * of simultaneous open connections.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.25 $ $Date: 2004-05-16 14:13:23 $
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class TCPMessageChannel
	extends MessageChannel
	implements SIPMessageListener, Runnable {

//ifndef SIMULATION
//
	private Socket mySock;
//else
/*
	private SimSocket mySock;
//endif
*/
	private PipelinedMsgParser myParser;
	private InputStream myClientInputStream; // just to pass to thread.
	private OutputStream myClientOutputStream;

	private String key ;
	protected boolean isCached;
	protected boolean isRunning;



//ifndef SIMULATION
//
	private Thread mythread;
//else
/*
	private SimThread mythread;
//endif
*/

	private SIPStack stack;

	private String myAddress;
	private int myPort;

	private InetAddress peerAddress;
	private int peerPort;

	private String peerProtocol;


	//Incremented whenever a transaction gets assigned
	// to the message channel and decremented when 
	// a transaction gets freed from the message channel.
	protected int useCount;

	private TCPMessageProcessor tcpMessageProcessor;



	/**
	 * Constructor - gets called from the SIPStack class with a socket
	 * on accepting a new client. All the processing of the message is
	 * done here with the stack being freed up to handle new connections.
	 * The sock input is the socket that is returned from the accept.
	 * Global data that is shared by all threads is accessible in the Server
	 * structure.
	 * @param sock Socket from which to read and write messages. The socket
	 *   is already connected (was created as a result of an accept).
	 *
	 * @param sipStack Ptr to SIP Stack
	 */

//ifndef SIMULATION
//
	protected TCPMessageChannel(
		Socket sock,
		SIPStack sipStack,
		TCPMessageProcessor msgProcessor)
		throws IOException {
		if (LogWriter.needsLogging)  {
			sipStack.logWriter.logMessage(
			"creating new TCPMessageChannel ");
			sipStack.logWriter.logStackTrace();
		}
		mySock = sock;
		peerAddress = mySock.getInetAddress();
		myAddress = sipStack.getHostAddress();
		myClientInputStream = mySock.getInputStream();
		myClientOutputStream = mySock.getOutputStream();
		mythread = new Thread(this);
		mythread.setName("TCPMessageChannelThread");
		// Stash away a pointer to our stack structure.
		stack = sipStack;
		this.tcpMessageProcessor = msgProcessor;
		this.myPort = this.tcpMessageProcessor.getPort();
		// Bug report by Vishwashanti Raj Kadiayl 
		super.messageProcessor = msgProcessor;
		// Can drop this after response is sent potentially.
		mythread.start();
	}
//else
/*
	    protected TCPMessageChannel( 
		       SimSocket sock, 
		       SIPStack sipStack, 
		       TCPMessageProcessor msgProcessor )  throws IOException {
	        mySock = sock;
	        myAddress = sipStack.getHostAddress();
	        myClientInputStream = mySock.getInputStream();
	        myClientOutputStream = mySock.getOutputStream();
	        mythread = new SimThread( this );
		mythread.setName("TCPMessageChannelThread");
	        // Stash away a pointer to our stack structure.
	        stack = sipStack;
	        this.tcpMessageProcessor = msgProcessor;
	        this.myPort = this.tcpMessageProcessor.getPort();
		// Bug report by Vishwashanti Raj Kadiayl 
		super.messageProcessor =  msgProcessor;
	        mythread.start();
	    }
//endif
*/

	/**
	 * Constructor - connects to the given inet address.
	 * Acknowledgement -- Lamine Brahimi (IBM Zurich) sent in a
	 * bug fix for this method. A thread was being uncessarily created.
	 * @param inetAddr inet address to connect to.
	 * @param sipStack is the sip stack from which we are created.
	 * @throws IOException if we cannot connect.
	 */
	protected TCPMessageChannel(
		InetAddress inetAddr,
		int port,
		SIPStack sipStack,
		TCPMessageProcessor messageProcessor)
		throws IOException {
		if (LogWriter.needsLogging)  {
			sipStack.logWriter.logMessage(
			"creating new TCPMessageChannel ");
			sipStack.logWriter.logStackTrace();
		}
		this.peerAddress = inetAddr;
		this.peerPort = port;
		this.myPort = messageProcessor.getPort();
		this.peerProtocol = "TCP";
		this.stack = sipStack;
		this.tcpMessageProcessor = messageProcessor;
		this.myAddress = sipStack.getHostAddress();
		// Bug report by Vishwashanti Raj Kadiayl 
		this.key = MessageChannel.getKey(peerAddress,peerPort,"TCP");
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
		try {
			if (mySock != null ) mySock.close();
			if (LogWriter.needsLogging)
				stack.logWriter.logMessage
				("Closing message Channel " + this);
		} catch (IOException ex) {
			if (LogWriter.needsLogging)
				stack.logWriter.logMessage
				("Error closing socket " + ex);
		}
	}

	/**
	 * Get my SIP Stack.
	 * @return The SIP Stack for this message channel.
	 */
	public SIPStack getSIPStack() {
		return stack;
	}

	/**
	 * get the transport string.
	 * @return "tcp" in this case.
	 */
	public String getTransport() {
		return "TCP";
	}

	/**
	 * get the name of the client that sent the data to us.
	 * @return The peer name of the entity on the other end
	 * who sent us a message (causing this channel
	 * to be created).
	 */
	public String getPeerName() {
		if (peerAddress != null) {
			return peerAddress.getHostName();
		} else
			return getHost();
	}

	/**
	 * get the address of the client that sent the data to us.
	 * @return Address of the client that sent us data that resulted in this channel being created.
	 */
	public String getPeerAddress() {
		if (peerAddress != null) {
			return peerAddress.getHostAddress();
		} else
			return getHost();
	}

	/**
	 * Send message to whoever is connected to us.
	 * Uses the topmost via address to send to.
	 * @param msg is the message to send.
	 * @param retry
	 */
//ifndef SIMULATION
//
	private void sendMessage(byte[] msg, boolean retry) throws IOException {
		Socket sock =
			this.stack.ioHandler.sendBytes(
				this.peerAddress,
				this.peerPort,
				this.peerProtocol,
				msg,
				retry);
		// Created a new socket so close the old one and stick the new
		// one in its place but dont do this if it is a datagram socket.
		// (could have replied via udp but received via tcp!).
		if (sock != mySock && sock != null) {
			try {
				if (mySock != null)
					mySock.close();
			} catch (IOException ex) {
			}
			mySock = sock;
			this.myClientInputStream = mySock.getInputStream();
			this.myClientOutputStream = mySock.getOutputStream();
		    	Thread thread = new Thread(this);
			thread.setName("TCPMessageChannelThread");
			thread.start();
		}

	}
//else
/*
	    private void sendMessage(byte[] msg, boolean retry ) throws IOException {
	        SimSocket sock = this.stack.ioHandler.sendBytes(this.peerAddress,
	        this.peerPort,this.peerProtocol,msg, retry );
	        // Created a new socket so close the old one and s
	        if (sock != mySock && sock != null ) {
	            try {
	                if (mySock != null)
	                    mySock.close();
	            } catch (IOException ex) {  }
	            mySock = sock;
	            this.myClientInputStream = mySock.getInputStream();
	            this.myClientOutputStream = mySock.getOutputStream();
	            SimThread thread = new SimThread(this);
	            thread.start();
	        }
	    }
//endif
*/

	/**
	 * Return a formatted message to the client.
	 * We try to re-connect with the peer on the other end if possible.
	 * @param sipMessage Message to send.
	 * @throws IOException If there is an error sending the message
	 */
	public void sendMessage(SIPMessage sipMessage) throws IOException {
		byte[] msg = sipMessage.encodeAsBytes();

//ifdef SIMULATION
/*
	        long time = SimSystem.currentTimeMillis();
//else
*/
		long time = System.currentTimeMillis();
//endif
//

		this.sendMessage(msg, sipMessage instanceof SIPRequest);
		if (this
			.stack
			.serverLog
			.needsLogging(ServerLog.TRACE_MESSAGES))
			logMessage(sipMessage, peerAddress, peerPort, time);
	}

	/**
	 * Send a message to a specified address.
	 * @param message Pre-formatted message to send.
	 * @param receiverAddress Address to send it to.
	 * @param receiverPort Receiver port.
	 * @throws IOException If there is a problem connecting or sending.
	 */
	public void sendMessage(
		byte message[],
		InetAddress receiverAddress,
		int receiverPort,
		boolean retry)
		throws IOException {
		if (message == null || receiverAddress == null)
			throw new IllegalArgumentException("Null argument");
//ifdef SIMULATION
/*
	        SimSocket sock = this.stack.ioHandler.sendBytes
						(receiverAddress,receiverPort,
		        			"TCP",message,retry);
//else
*/
		Socket sock =
			this.stack.ioHandler.sendBytes(
				receiverAddress,
				receiverPort,
				"TCP",
				message,
				retry);
//endif
		//
		// Created a new socket so close the old one and s
		// Check for null (bug fix sent in by Christophe)
		if (sock != mySock && sock != null) {
			try {
				if (mySock != null)
					mySock.close();
			} catch (IOException ex) {
				/* ignore */
			}
			mySock = sock;
			this.myClientInputStream = mySock.getInputStream();
			this.myClientOutputStream = mySock.getOutputStream();
			// start a new reader on this end of the pipe.
//ifdef SIMULATION
/*
			SimThread mythread = new SimThread(this);
				    mythread.setName("TCPMessageChannelThread");
//else
*/
			Thread mythread = new Thread(this);
		        mythread.setName("TCPMessageChannelThread");
//endif
//

			mythread.start();
		}

	}

	/**
	 * Exception processor for exceptions detected from the application.
	 * @param ex The exception that was generated.
	 */
	public void handleException(SIPServerException ex) {
		// Return a parse error message to the client on the other end
		// if he is still alive.
		int rc = ex.getRC();
		String msgString = ex.getMessage();
		if (rc != 0) {
			// Do we have a valid Return code ? --
			// in this case format the message.
			SIPRequest request = (SIPRequest) ex.getSIPMessage();
			SIPResponse response = request.createResponse(rc);
			try {
				sendMessage(response);
			} catch (IOException ioex) {
				if (LogWriter.needsLogging)
					stack.logWriter.logException(ioex);
			}
		} else {
			// Otherwise, message is already formatted --
			// just return it
			try {
				sendMessage(msgString.getBytes(), false);
			} catch (IOException ioex) {
				if (LogWriter.needsLogging)
					stack.logWriter.logException(ioex);
			}
		}
	}

	/**
	 * Exception processor for exceptions detected from the parser. (This
	 * is invoked by the parser when an error is detected).
	 * @param sipMessage -- the message that incurred the error.
	 * @param ex -- parse exception detected by the parser.
	 * @param header -- header that caused the error.
	 * @throws ParseException Thrown if we want to reject the message.
	 */
	public void handleException(
		ParseException ex,
		SIPMessage sipMessage,
		Class hdrClass,
		String header,
		String message)
		throws ParseException {
		if (LogWriter.needsLogging)
			stack.logWriter.logException(ex);
		// Log the bad message for later reference.
		if ((hdrClass!=null) && (hdrClass.equals(From.class)
			|| hdrClass.equals(To.class)
			|| hdrClass.equals(CSeq.class)
			|| hdrClass.equals(Via.class)
			|| hdrClass.equals(CallID.class)
			|| hdrClass.equals(RequestLine.class)
			|| hdrClass.equals(StatusLine.class))) {
			stack.logBadMessage(message);
			throw ex;
		} else {
			sipMessage.addUnparsed(header);
		}
	}

	/**
	 * Gets invoked by the parser as a callback on successful message
	 * parsing (i.e. no parser errors).
	 * @param sipMessage Mesage to process (this calls the application
	 * for processing the message).
	 */
	public void processMessage(SIPMessage sipMessage)  throws Exception {
		try {
			if (sipMessage.getFrom() == null
				|| //sipMessage.getFrom().getTag() == null ||
			sipMessage.getTo()
					== null
				|| sipMessage.getCallId() == null
				|| sipMessage.getCSeq() == null
				|| sipMessage.getViaHeaders() == null) {
				String badmsg = sipMessage.encode();
				if (LogWriter.needsLogging) {
					stack.logWriter.logMessage("bad message " + badmsg);
					stack.logWriter.logMessage(">>> Dropped Bad Msg");
				}
				stack.logBadMessage(badmsg);
				return;
			}

			ViaList viaList = sipMessage.getViaHeaders();
			// For a request
			// first via header tells where the message is coming from.
			// For response, this has already been recorded in the outgoing
			// message.
			if (sipMessage instanceof SIPRequest) {
				Via v = (Via) viaList.first();
				if (v.hasPort()) {
					this.peerPort = v.getPort();
				} else
					this.peerPort = SIPStack.DEFAULT_PORT;
				this.peerProtocol = v.getTransport();
				try {
					this.peerAddress = mySock.getInetAddress();
					// Check to see if the received parameter matches
					// the peer address and tag it appropriately.
					// Bug fix by viswashanti.kadiyala@antepo.com
					if (!v
						.getSentBy()
						.getInetAddress()
						.equals(this.peerAddress))
						v.setParameter(
							Via.RECEIVED,
							this.peerAddress.getHostName());
				} catch (java.net.UnknownHostException ex) {
					// Could not resolve the sender address.
					if (LogWriter.needsLogging) {
						stack.logWriter.logMessage(
							"Rejecting message -- could not resolve Via Address");
					}
					return;
				} catch (java.text.ParseException ex) {
					InternalErrorHandler.handleException(ex);
				}
				// Use this for outgoing messages as well.
				if (! this.isCached) {
				    ( (TCPMessageProcessor) 
				    this.messageProcessor)
				      .cacheMessageChannel(this);
				    this.isCached = true;
				    String key =
					IOHandler.makeKey(mySock.getInetAddress(), this.peerPort);
			            stack.ioHandler.putSocket(key, mySock);
				} 
			}

			// System.out.println("receiver address = " + receiverAddress);

			// Foreach part of the request header, fetch it and process it

//ifdef SIMULATION
/*
 			long receptionTime = SimSystem.currentTimeMillis();
//else
*/
			long receptionTime = System.currentTimeMillis();
//endif
//

			if (sipMessage instanceof SIPRequest) {
				// This is a request - process the request.
				SIPRequest sipRequest = (SIPRequest) sipMessage;
				// Create a new sever side request processor for this
				// message and let it handle the rest.

				if (LogWriter.needsLogging) {
					stack.logWriter.logMessage("----Processing Message---");
				}

				// Check for reasonable size - reject message
				// if it is too long.
				if ( stack.getMaxMessageSize() > 0 &&
				     sipRequest.getSize() + 
				     (sipRequest.getContentLength() == null? 0 :
					sipRequest.getContentLength().getContentLength() ) >
				     stack.getMaxMessageSize() ) {
				     SIPResponse sipResponse = sipRequest.createResponse(SIPResponse.MESSAGE_TOO_LARGE);
				     byte[] resp = sipResponse.encodeAsBytes();
				     this.sendMessage(resp,false);
				     throw new Exception("Message size exceeded");
				}

				SIPServerRequestInterface sipServerRequest =
					stack.newSIPServerRequest(sipRequest, this);
				try {
					sipServerRequest.processRequest(sipRequest, this);
					if (this
						.stack
						.serverLog
						.needsLogging(ServerLog.TRACE_MESSAGES)) {
						if (sipServerRequest.getProcessingInfo() == null) {
							stack.serverLog.logMessage(
								sipMessage,
								sipRequest.getViaHost()
									+ ":"
									+ sipRequest.getViaPort(),
								stack.getHostAddress()
									+ ":"
									+ stack.getPort(this.getTransport()),
								false,
								receptionTime);
						} else {
							this.stack.serverLog.logMessage(
								sipMessage,
								sipRequest.getViaHost()
									+ ":"
									+ sipRequest.getViaPort(),
								stack.getHostAddress()
									+ ":"
									+ stack.getPort(this.getTransport()),
								sipServerRequest.getProcessingInfo(),
								false,
								receptionTime);
						}
					}
				} catch (SIPServerException ex) {
					if (this
						.stack
						.serverLog
						.needsLogging(ServerLog.TRACE_MESSAGES)) {
						this.stack.serverLog.logMessage(
							sipMessage,
							sipRequest.getViaHost()
								+ ":"
								+ sipRequest.getViaPort(),
							stack.getHostAddress()
								+ ":"
								+ stack.getPort(this.getTransport()),
							ex.getMessage(),
							false,
							receptionTime);
					}
					handleException(ex);
				}
			} else {
				SIPResponse sipResponse = (SIPResponse) sipMessage;
				// This is a response message - process it.
				// Check the size of the response.
				// If it is too large dump it silently.
				if ( stack.getMaxMessageSize() > 0 &&
				     sipResponse.getSize() + 
				     (sipResponse.getContentLength() == null? 0 :
					sipResponse.getContentLength().getContentLength() ) >
				     stack.getMaxMessageSize() ) {
				     if (LogWriter.needsLogging) 
					this.stack.logWriter.logMessage ("Message size exceeded");
				     return;
					
				}
				SIPServerResponseInterface sipServerResponse =
					stack.newSIPServerResponse(sipResponse, this);
				try {
					sipServerResponse.processResponse(sipResponse, this);
				} catch (SIPServerException ex) {
					// An error occured processing the message -- just log it.
					if (this
						.stack
						.serverLog
						.needsLogging(ServerLog.TRACE_MESSAGES)) {
						this.stack.serverLog.logMessage(
							sipMessage,
							getPeerAddress().toString() + ":" + getPeerPort(),
							stack.getHostAddress()
								+ ":"
								+ stack.getPort(this.getTransport()),
							ex.getMessage(),
							false,
							receptionTime);
					}
					// Ignore errors while processing responses??
				}
			}
		} finally {
		}
	}

	/**
	 * This gets invoked when thread.start is called from the constructor.
	 * Implements a message loop - reading the tcp connection and processing
	 * messages until we are done or the other end has closed.
	 */
	public void run() {
		String message;
		Pipeline hispipe = null;
		// Create a pipeline to connect to our message parser.
		hispipe = new Pipeline(myClientInputStream,stack.readTimeout);
		// Create a pipelined message parser to read and parse
		// messages that we write out to him.
		myParser = new PipelinedMsgParser(this, hispipe,this.stack.getMaxMessageSize());
		// Start running the parser thread.
		myParser.processInput();
		// bug fix by Emmanuel Proulx
		int bufferSize = 4096;
	   	this.tcpMessageProcessor.useCount ++;
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
						if (stack.maxConnections != -1) {
							synchronized (tcpMessageProcessor) {
								tcpMessageProcessor.nConnections--;
								tcpMessageProcessor.notify();
							}
						}
						hispipe.close();
						mySock.close();
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
					if (LogWriter.needsLogging)
						stack.logWriter.logMessage("IOException  closing sock " + ex);
					try {
						if (stack.maxConnections != -1) {
							synchronized (tcpMessageProcessor) {
								tcpMessageProcessor.nConnections--;
								// System.out.println("Notifying!");
								tcpMessageProcessor.notify();
							}
						}
						mySock.close();
						hispipe.close();
					} catch (IOException ioex) {
					}
				} catch (Exception ex1) {
					// Do nothing.
				}
				return;
			} catch (Exception ex) {
				InternalErrorHandler.handleException(ex);
			} 
		} 
	      } finally {
		 this.isRunning = false;
		 this.tcpMessageProcessor.remove(this);
		 this.tcpMessageProcessor.useCount --;
	      }

	}


	protected void uncache() {
		this.tcpMessageProcessor.remove(this);
	}

	/**
	 * Equals predicate.
	 * @param other is the other object to compare ourselves to for equals
	 */

	public boolean equals(Object other) {

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
	 * Get an identifying key. This key is used to cache the connection
	 * and re-use it if necessary.
	 */
	public String getKey() {
		if (this.key != null)  {
			return	this.key;
		} else  {
			this.key = MessageChannel.getKey(this.peerAddress,this.peerPort,"TCP");
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

	/**
	 * TCP Is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.24  2004/04/22 22:51:19  mranga
 * Submitted by:  Thomas Froment
 * Reviewed by:   mranga
 *
 * Fixed corner cases.
 *
 * Revision 1.23  2004/04/21 16:25:22  mranga
 * Reviewed by:   mranga
 * Record IP address of peer in TCP connection as soon as connection is made.
 * Remove range check on Warning.java
 *
 * Revision 1.22  2004/03/30 17:53:56  mranga
 * Reviewed by:   mranga
 * more reference counting cleanup
 *
 * Revision 1.21  2004/03/30 16:40:30  mranga
 * Reviewed by:   mranga
 * more tweaks to reference counting for cleanup.
 *
 * Revision 1.20  2004/03/30 15:38:18  mranga
 * Reviewed by:   mranga
 * Name the threads so as to facilitate debugging.
 *
 * Revision 1.19  2004/03/19 23:41:30  mranga
 * Reviewed by:   mranga
 * Fixed connection and thread caching.
 *
 * Revision 1.18  2004/03/19 17:26:20  mranga
 * Reviewed by:   mranga
 * Fixed silly bug.
 *
 * Revision 1.17  2004/03/19 17:06:19  mranga
 * Reviewed by:   mranga
 * Fixed some stack cleanup issues. Stack should release all resources when
 * finalized.
 *
 * Revision 1.16  2004/03/19 04:22:22  mranga
 * Reviewed by:   mranga
 * Added IO Pacing for long writes - split write into chunks and flush after each
 * chunk to avoid socket back pressure.
 *
 * Revision 1.15  2004/03/18 22:01:20  mranga
 * Reviewed by:   mranga
 * Get rid of the PipedInputStream from pipelined parser to avoid a copy.
 *
 * Revision 1.14  2004/03/09 00:34:45  mranga
 * Reviewed by:   mranga
 * Added TCP connection management for client and server side
 * Transactions. See configuration parameter
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false
 * Releases Client TCP Connections after linger time
 *
 * Revision 1.13  2004/03/07 22:25:25  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.12  2004/03/05 20:36:55  mranga
 * Reviewed by:   mranga
 * put in some debug printfs and cleaned some things up.
 *
 * Revision 1.11  2004/02/29 15:32:59  mranga
 * Reviewed by:   mranga
 * bug fixes on limiting the max message size.
 *
 * Revision 1.10  2004/02/29 00:46:35  mranga
 * Reviewed by:   mranga
 * Added new configuration property to limit max message size for TCP transport.
 * The property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 *
 * Revision 1.9  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.8  2004/01/22 13:26:33  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
