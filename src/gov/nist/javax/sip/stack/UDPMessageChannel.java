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

import java.net.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.message.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.String;
import java.text.ParseException;

import javax.sip.address.Hop;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

/*
 * Kim Kirby (Keyvoice) suggested that duplicate checking
 * should be added to the stack (later removed). Lamine Brahimi suggested a
 * single threaded behavior flag be added to this. Niklas Uhrberg suggested that
 * thread pooling support be added to this for performance and resource
 * management. Peter Parnes found a bug with this code that was sending it into
 * an infinite loop when a bad incoming message was parsed.
 * Bug fix by viswashanti.kadiyala@antepo.com. Hagai Sela addded fixes
 * for NAT traversal. Jeroen van Bemmel fixed up for buggy clients (such
 * as windows messenger) and added code to return BAD_REQUEST.
 *  David Alique fixed an address recording bug.
 * Jeroen van Bemmel fixed a performance issue where the stack was doing
 * DNS lookups (potentially unnecessary). Ricardo Bora (Natural Convergence )
 * added code that prevents the stack from exitting when an exception is encountered.
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
 * @version 1.2 $Revision: 1.40 $ $Date: 2006-12-05 05:18:50 $
 */
public class UDPMessageChannel extends MessageChannel implements
		ParseExceptionListener, Runnable {

	/**
	 * SIP Stack structure for this channel.
	 */
	protected SIPTransactionStack sipStack;

	/**
	 * The parser we are using for messages received from this channel.
	 */
	protected StringMsgParser myParser;

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

	/**
	 * Constructor - takes a datagram packet and a stack structure Extracts the
	 * address of the other from the datagram packet and stashes away the
	 * pointer to the passed stack structure.
	 *
	 * @param stack
	 *            is the shared SIPStack structure
	 * @param messageProcessor
	 *            is the creating message processor.
	 */
	protected UDPMessageChannel(SIPTransactionStack stack,
			UDPMessageProcessor messageProcessor) {
		super.messageProcessor = messageProcessor;
		this.sipStack = stack;

		Thread mythread = new Thread(this);

		this.myAddress = messageProcessor.getIPAddress().getHostAddress();
		this.myPort = messageProcessor.getPort();

		mythread.setName("UDPMessageChannelThread");
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

		this.myAddress = messageProcessor.getIPAddress().getHostAddress();
		this.myPort = messageProcessor.getPort();
		Thread mythread = new Thread(this);
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
		this.myAddress = messageProcessor.getIPAddress().getHostAddress();
		this.myPort = messageProcessor.getPort();
		this.sipStack = sipStack;
		if (sipStack.isLoggingEnabled()) {
			this.sipStack.logWriter.logDebug("Creating message channel "
					+ targetAddr.getHostAddress() + "/" + port);
		}
	}

	/**
	 * Run method specified by runnnable.
	 */
	public void run() {
		// Assume no thread pooling (bug fix by spierhj)
		ThreadAuditor.ThreadHandle threadHandle = null;

		while (true) {
			// Create a new string message parser to parse the list of messages.
			if (myParser == null) {
				myParser = new StringMsgParser();
				myParser.setParseExceptionListener(this);
			}
			// messages that we write out to him.
			DatagramPacket packet;

			if (sipStack.threadPoolSize != -1) {
				synchronized (((UDPMessageProcessor) messageProcessor).messageQueue) {
					while (((UDPMessageProcessor) messageProcessor).messageQueue
							.isEmpty()) {
						// Check to see if we need to exit.
						if (!((UDPMessageProcessor) messageProcessor).isRunning)
							return;
						try {
							// We're part of a thread pool. Ask the auditor to monitor this thread.
							if (threadHandle == null) {
								threadHandle = sipStack.getThreadAuditor().addCurrentThread();
							}

							// Send a heartbeat to the thread auditor
							threadHandle.ping();

							// Wait for packets
							// Note: getPingInterval returns 0 (infinite) if the thread auditor is disabled.
							((UDPMessageProcessor) messageProcessor).messageQueue
									.wait(threadHandle.getPingIntervalInMillisecs());
						} catch (InterruptedException ex) {
							if (!((UDPMessageProcessor) messageProcessor).isRunning)
								return;
						}
					}
					packet = (DatagramPacket) ((UDPMessageProcessor) messageProcessor).messageQueue
							.removeFirst();

				}
				this.incomingPacket = packet;
			} else {
				packet = this.incomingPacket;
			}

			// Process the packet. Catch and log any exception we may throw.
			try {
				processIncomingDataPacket(packet);
			} catch (Exception e) {
				e.printStackTrace();
				sipStack.logWriter.logError("Error while processing incoming UDP packet", e);
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
	private void processIncomingDataPacket(DatagramPacket packet) throws Exception
	{
		this.peerAddress = packet.getAddress();
		int packetLength = packet.getLength();
		// Read bytes and put it in a eueue.
		byte[] bytes = packet.getData();
		byte[] msgBytes = new byte[packetLength];
		System.arraycopy(bytes, 0, msgBytes, 0, packetLength);

		// Do debug logging.
		if (sipStack.isLoggingEnabled()) {
			this.sipStack.logWriter
					.logDebug("UDPMessageChannel: peerAddress = "
							+ peerAddress.getHostAddress() + "/"
							+ packet.getPort());
			this.sipStack.logWriter.logDebug("Length = " + packetLength);
			String msgString = new String(msgBytes, 0, packetLength);
			this.sipStack.logWriter.logDebug(msgString);
		}

		SIPMessage sipMessage = null;
		try {
			this.receptionTime = System.currentTimeMillis();
			sipMessage = myParser.parseSIPMessage(msgBytes);
			myParser = null;
		} catch (ParseException ex) {
			myParser = null; // let go of the parser reference.
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logDebug("Rejecting message !  "
						+ new String(msgBytes));
				this.sipStack.logWriter.logDebug("error message "
						+ ex.getMessage());
				this.sipStack.logWriter.logException(ex);
			}
			if (sipStack.isLoggingEnabled()) {
				sipStack.getLogWriter().logDebug(new String(msgBytes));
			}

			// JvB: send a 400 response for requests (except ACK)
			// Currently only UDP, @todo also other transports
			String msgString = new String(msgBytes, 0, packetLength);
			if ( !msgString.startsWith("SIP/") && !msgString.startsWith("ACK ") ) {

				String badReqRes = createBadReqRes( msgString, ex );
				if (badReqRes!=null) {
					if (sipStack.isLoggingEnabled()) {
						sipStack.getLogWriter().logDebug( "Sending automatic 400 Bad Request:");
						sipStack.getLogWriter().logDebug( badReqRes );
					}
					try {
						this.sendMessage( badReqRes.getBytes(), peerAddress,
								peerPort, peerProtocol, false );
					} catch (IOException e) {
						this.sipStack.logWriter.logException(e);
					}
				} else {
					if (sipStack.isLoggingEnabled()) {
						sipStack.getLogWriter().logDebug(
								"Could not formulate automatic 400 Bad Request" );
					}
				}
			}

			return;
		}
		// No parse exception but null message - reject it and
		// march on (or return).
		// exit this message processor if the message did not parse.

		if (sipMessage == null) {
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logDebug("Rejecting message !  "
						+ new String(msgBytes));
				this.sipStack.logWriter.logDebug("Null message parsed.");
			}
			return;
		}
		ViaList viaList = sipMessage.getViaHeaders();
		// Check for the required headers.
		if (sipMessage.getFrom() == null
				|| sipMessage.getTo() == null
				|| sipMessage.getCallId() == null
				|| sipMessage.getCSeq() == null
				|| sipMessage.getViaHeaders() == null) {
			String badmsg = new String(msgBytes);
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logError("bad message " + badmsg);
				this.sipStack.logWriter.logError(">>> Dropped Bad Msg "
						+ "From = " + sipMessage.getFrom() + "To = "
						+ sipMessage.getTo() + "CallId = "
						+ sipMessage.getCallId() + "CSeq = "
						+ sipMessage.getCSeq() + "Via = "
						+ sipMessage.getViaHeaders());
			}

			sipStack.logWriter.logError("BAD MESSAGE!");
			sipStack.logWriter.logError(badmsg);
			return;
		}
		// For a request first via header tells where the message
		// is coming from.
		// For response, just get the port from the packet.
		if (sipMessage instanceof SIPRequest) {
			Via v = (Via) viaList.getFirst();
			Hop hop = sipStack.addressResolver.resolveAddress(v.getHop());
			this.peerPort = hop.getPort();
			this.peerProtocol = v.getTransport();

			this.peerPacketSourceAddress = packet.getAddress();
			this.peerPacketSourcePort = packet.getPort();
			try {
				this.peerAddress = packet.getAddress();
				// Check to see if the received parameter matches
				// the peer address and tag it appropriately.

				// JvB: Better not do a DNS lookup here, this is costly
				// InetAddress sentByAddress = InetAddress.getByName(hop.getHost());

				boolean hasRPort = v.hasParameter(Via.RPORT);
				if (hasRPort || !hop.getHost().equals(this.peerAddress.getHostAddress()) ) {
					v.setParameter(Via.RECEIVED, this.peerAddress.getHostAddress());
				}

				if (hasRPort) {
					v.setParameter(Via.RPORT, Integer.toString(
							this.peerPacketSourcePort) );
				}
			} catch (java.text.ParseException ex1) {
				InternalErrorHandler.handleException(ex1);
			}

		} else {

			this.peerPacketSourceAddress = packet.getAddress();
			this.peerPacketSourcePort = packet.getPort();
			this.peerAddress = packet.getAddress();
			this.peerPort = packet.getPort();
			this.peerProtocol = ((Via) viaList.getFirst()).getTransport();
		}

		if (sipMessage instanceof SIPRequest) {
			SIPRequest sipRequest = (SIPRequest) sipMessage;

			// This is a request - process it.
			//So far so good -- we will commit this message if
			// all processing is OK.
			if (sipStack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES)) {

				this.sipStack.serverLog.logMessage(sipMessage,
						this.getPeerHostPort().toString(), this.getHost()
						+ ":" + this.myPort, false, receptionTime);

			}
			ServerRequestInterface sipServerRequest = sipStack
					.newSIPServerRequest(sipRequest, this);
			// Drop it if there is no request returned
			if (sipServerRequest == null) {
				if (sipStack.isLoggingEnabled()) {
					this.sipStack.logWriter
							.logWarning("Null request interface returned -- dropping request");
				}

				if (!sipRequest.getMethod().equals(Request.ACK)) {
  					SIPResponse response = sipRequest.createResponse(Response.SERVICE_UNAVAILABLE);
  					response.addHeader(sipStack.createServerHeaderForStack());
  					RetryAfter retryAfter = new RetryAfter();

  					// Be a good citizen and send a decent response code back.
  					try {
  						retryAfter.setRetryAfter((int)(10 * (Math.random())));
  						response.setHeader(retryAfter);
  						this.sendMessage(response);
  					} catch (Exception e) {
  						this.sipStack.logWriter.logError( "Exception while sending service_unavailable", e );
  					}
  				}
  				return;
			}
			if (sipStack.isLoggingEnabled())
				this.sipStack.logWriter.logDebug("About to process "
						+ sipRequest.getFirstLine() + "/"
						+ sipServerRequest);
			try {
				sipServerRequest.processRequest(sipRequest, this);
			} finally {
				if (sipServerRequest instanceof SIPTransaction) {
					SIPServerTransaction sipServerTx = (SIPServerTransaction) sipServerRequest;
					if (! sipServerTx.passToListener()) {
						((SIPTransaction) sipServerRequest).releaseSem();
					}
				}
			}
			if (sipStack.isLoggingEnabled())
				this.sipStack.logWriter.logDebug("Done processing "
						+ sipRequest.getFirstLine() + "/"
						+ sipServerRequest);

			// So far so good -- we will commit this message if
			// all processing is OK.

		} else {
			// Handle a SIP Reply message.
			SIPResponse sipResponse = (SIPResponse) sipMessage;
			if (sipResponse.getStatusCode() == 100) sipResponse.getTo().removeParameter("tag");
			try {
				sipResponse.checkHeaders();
			} catch (ParseException ex) {
				if (sipStack.isLoggingEnabled())
					sipStack.logWriter.logError("Dropping Badly formatted response message >>> " + sipResponse);
				return;
			}
			ServerResponseInterface sipServerResponse = sipStack
					.newSIPServerResponse(sipResponse, this);
			if (sipServerResponse != null) {
				try {
					sipServerResponse.processResponse(sipResponse, this);
				} finally {
					if (sipServerResponse instanceof SIPTransaction &&
							!((SIPTransaction)sipServerResponse).passToListener())
						((SIPTransaction) sipServerResponse).releaseSem();
				}

				// Normal processing of message.
			} else {
				if (sipStack.isLoggingEnabled()) {
					this.sipStack.logWriter
							.logDebug("null sipServerResponse!");
				}
			}

		}
	}

  /**
   * JvB: added method to check for known buggy clients (Windows Messenger)
   * to fix the port to which responses are sent
   *
   * checks for User-Agent: RTC/1.3.5470 (Messenger 5.1.0701)
   *
   * JvB 22/7/2006 better to take this out for the moment, it is
   *     only a problem in rare cases (unregister)
   *
  private final boolean isBuggyClient( SIPRequest r ) {
    UserAgent uah = (UserAgent) r.getHeader( UserAgent.NAME );
    if (uah!=null) {
       java.util.ListIterator i = uah.getProduct();
       if (i.hasNext()) {
         String p = (String) uah.getProduct().next();
         return p.startsWith( "RTC" );
       }
    }
    return false;
  }
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
		if (sipStack.isLoggingEnabled())
			this.sipStack.logWriter.logException(ex);
		// Log the bad message for later reference.
		if ((hdrClass != null)
				&& (hdrClass.equals(From.class) || hdrClass.equals(To.class)
						|| hdrClass.equals(CSeq.class)
						|| hdrClass.equals(Via.class)
						|| hdrClass.equals(CallID.class)
						|| hdrClass.equals(RequestLine.class) || hdrClass
						.equals(StatusLine.class))) {
			sipStack.logWriter.logError("BAD MESSAGE!");
			sipStack.logWriter.logError(message);
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
	public void sendMessage(SIPMessage sipMessage) throws IOException {
		if (sipStack.isLoggingEnabled())
			this.sipStack.logWriter.logStackTrace();
		byte[] msg = sipMessage.encodeAsBytes();

		long time = System.currentTimeMillis();

		sendMessage(msg, peerAddress, peerPort, peerProtocol,
				sipMessage instanceof SIPRequest);

		if (sipStack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES))
			logMessage(sipMessage, peerAddress, peerPort, time);
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
		if (sipStack.isLoggingEnabled())
			this.sipStack.logWriter.logStackTrace();
		if (peerPort == -1) {
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logDebug(getClass().getName()
						+ ":sendMessage: Dropping reply!");
			}
			throw new IOException("Receiver port not set ");
		} else {
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logDebug(getClass().getName()
						+ ":sendMessage " + peerAddress.getHostAddress() + "/"
						+ peerPort + "\n" + new String(msg));
				this.sipStack.logWriter.logDebug("*******************\n");
			}

		}
		DatagramPacket reply = new DatagramPacket(msg, msg.length, peerAddress,
				peerPort);
		try {
			DatagramSocket sock;
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
			}
			sock.send(reply);
			if (!sipStack.udpFlag)
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
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logDebug(getClass().getName()
						+ ":sendMessage: Dropping reply!");
			}
			throw new IOException("Receiver port not set ");
		} else {
			if (sipStack.isLoggingEnabled()) {
				this.sipStack.logWriter.logDebug(getClass().getName()
						+ ":sendMessage " + peerAddress.getHostAddress() + "/"
						+ peerPort + "\n" + new String(msg));
				this.sipStack.logWriter.logDebug("*******************\n");
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
				if (sipStack.isLoggingEnabled()) {
					this.sipStack.logWriter.logDebug(
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
					this.messageProcessor.getIPAddress(),
					peerAddress, peerPort, "tcp", msg, retry
					);
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
		return messageProcessor.getIPAddress().getHostAddress();
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
	}

	/**
	 * Creates a response to a bad request (ie one that causes a ParseException)
	 *
	 * @param badReq
	 * @return message bytes, null if unable to formulate response
	 */
	private final String createBadReqRes( String badReq, ParseException pe ) {

		StringBuffer buf = new StringBuffer( 512 );
		buf.append( "SIP/2.0 400 Bad Request (" + pe.getLocalizedMessage() + ')' );

		// We need the following headers: all Vias, CSeq, Call-ID, From, To
		if (!copyViaHeaders(badReq, buf)) return null;
		if (!copyHeader( CSeqHeader.NAME, badReq, buf)) return null;
		if (!copyHeader( CallIdHeader.NAME, badReq, buf)) return null;
		if (!copyHeader( FromHeader.NAME, badReq, buf)) return null;
		if (!copyHeader( ToHeader.NAME, badReq, buf)) return null;

		// Should add a to-tag if not already present...
		int toStart = buf.indexOf( ToHeader.NAME );
		if (toStart!=-1 && buf.indexOf( "tag", toStart) == -1 ) {
			buf.append( ";tag=badreq" );
		}

		// Let's add a Server header too..
		Server s = sipStack.createServerHeaderForStack();
		buf.append( "\r\n" + s.toString() );
		return buf.toString();
	}

	/**
	 * Copies a header from a request
	 *
	 * @param name
	 * @param fromReq
	 * @param buf
	 * @return
	 *
	 * Note: some limitations here: does not work for short forms of headers, or continuations;
	 * 	 problems when header names appear in other parts of the request
	 */
	private static final boolean copyHeader( String name, String fromReq, StringBuffer buf ) {
		int start = fromReq.indexOf( name );
		if (start!=-1) {
			int end = fromReq.indexOf( "\r\n", start );
			if (end!=-1) {
				// XX Assumes no continuation here...
				buf.append( fromReq.subSequence(start-2, end) );	// incl CRLF in front
				return true;
			}
		}
		return false;
	}

	/**
	 * Copies all via headers from a request
	 *
	 * @param fromReq
	 * @param buf
	 * @return
	 *
	 * Note: some limitations here: does not work for short forms of headers, or continuations
	 */
	private static final boolean copyViaHeaders( String fromReq, StringBuffer buf ) {
		int start = fromReq.indexOf( ViaHeader.NAME );
		boolean found = false;
		while (start!=-1) {
			int end = fromReq.indexOf( "\r\n", start );
			if (end!=-1) {
				// XX Assumes no continuation here...
				buf.append( fromReq.subSequence(start-2, end) );	// incl CRLF in front
				found = true;
				start = fromReq.indexOf( ViaHeader.NAME, end );
			} else {
				return false;
			}
		}
		return found;
	}

}
