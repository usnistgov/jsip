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
/* This class is entirely derived from TCPMessageChannel, 
 * by making some minor changes. Daniel J. Martinez Manzano <dani@dif.um.es> 
 * made these changes. Ahmet Uyar
 * <auyar@csit.fsu.edu>sent in a bug report for TCP operation of the
 * JAIN sipStack. Niklas Uhrberg suggested that a mechanism be added to
 * limit the number of simultaneous open connections. The TLS
 * Adaptations were contributed by Daniel Martinez. Hagai Sela
 * contributed a bug fix for symmetric nat. Jeroen van Bemmel
 * added compensation for buggy clients ( Microsoft RTC clients ).
 * Bug fixes by viswashanti.kadiyala@antepo.com, Joost Yervante Damand	
 */

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

import javax.net.ssl.SSLSocket;
import javax.sip.address.Hop;
import javax.sip.message.Response;

/**
 * This is sipStack for TLS connections. This abstracts a stream of parsed
 * messages. The SIP sipStack starts this from the main SIPStack class for each
 * connection that it accepts. It starts a message parser in its own thread and
 * talks to the message parser via a pipe. The message parser calls back via the
 * parseError or processMessage functions that are defined as part of the
 * SIPMessageListener interface.
 * 
 * @see gov.nist.javax.sip.parser.PipelinedMsgParser
 * 
 * 
 * @author M. Ranganathan 
 * 
 * 
 * @version 1.2 $Revision: 1.3 $ $Date: 2006-07-02 09:52:39 $ 
 */
public final class TLSMessageChannel extends MessageChannel implements
		SIPMessageListener, Runnable {

	private SSLSocket mySock;

	private PipelinedMsgParser myParser;

	private InputStream myClientInputStream; // just to pass to thread.

	private String key;

	protected boolean isCached;

	protected boolean isRunning;

	private Thread mythread;

	private String myAddress;

	private int myPort;

	private InetAddress peerAddress;

	private int peerPort;

	private String peerProtocol;

	// Incremented whenever a transaction gets assigned
	// to the message channel and decremented when
	// a transaction gets freed from the message channel.
	protected int useCount = 0;

	private TLSMessageProcessor tlsMessageProcessor;

	private SIPTransactionStack sipStack;

	/**
	 * Constructor - gets called from the SIPStack class with a socket on
	 * accepting a new client. All the processing of the message is done here
	 * with the sipStack being freed up to handle new connections. The sock
	 * input is the socket that is returned from the accept. Global data that is
	 * shared by all threads is accessible in the Server structure.
	 * 
	 * @param sock
	 *            Socket from which to read and write messages. The socket is
	 *            already connected (was created as a result of an accept).
	 * 
	 * @param sipStack
	 *            Ptr to SIP Stack
	 */

	protected TLSMessageChannel(SSLSocket sock, SIPTransactionStack sipStack,
			TLSMessageProcessor msgProcessor) throws IOException {
		if (sipStack.isLoggingEnabled()) {
			sipStack.logWriter.logDebug("creating new TLSMessageChannel ");
			sipStack.logWriter.logStackTrace();
		}
		mySock = sock;
		peerAddress = mySock.getInetAddress();
		myAddress = msgProcessor.getIPAddress().getHostAddress();
		myClientInputStream = mySock.getInputStream();

		mythread = new Thread(this);
		mythread.setDaemon(true);
		mythread.setName("TLSMessageChannelThread");
		// Stash away a pointer to our sipStack structure.
		this.sipStack = sipStack;

		this.tlsMessageProcessor = msgProcessor;
		this.myPort = this.tlsMessageProcessor.getPort();
		// Bug report by Vishwashanti Raj Kadiayl
		super.messageProcessor = msgProcessor;
		// Can drop this after response is sent potentially.
		mythread.start();
	}

	/**
	 * Constructor - connects to the given inet address. Acknowledgement --
	 * Lamine Brahimi (IBM Zurich) sent in a bug fix for this method. A thread
	 * was being uncessarily created.
	 * 
	 * @param inetAddr
	 *            inet address to connect to.
	 * @param sipStack
	 *            is the sip sipStack from which we are created.
	 * @throws IOException
	 *             if we cannot connect.
	 */
	protected TLSMessageChannel(InetAddress inetAddr, int port,
			SIPTransactionStack sipStack, TLSMessageProcessor messageProcessor)
			throws IOException {
		if (sipStack.isLoggingEnabled()) {
			sipStack.logWriter.logDebug("creating new TLSMessageChannel ");
			sipStack.logWriter.logStackTrace();
		}
		this.peerAddress = inetAddr;
		this.peerPort = port;
		this.myPort = messageProcessor.getPort();
		this.peerProtocol = "TLS";
		this.sipStack = sipStack;
		this.tlsMessageProcessor = messageProcessor;
		this.myAddress = messageProcessor.getIPAddress().getHostAddress();
		this.key = MessageChannel.getKey(peerAddress, peerPort, "TLS");
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
			if (mySock != null)
				mySock.close();
			if (sipStack.isLoggingEnabled())
				sipStack.logWriter.logDebug("Closing message Channel " + this);
		} catch (IOException ex) {
			if (sipStack.isLoggingEnabled())
				sipStack.logWriter.logDebug("Error closing socket " + ex);
		}
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
		return "tls";
	}

	/**
	 * get the address of the client that sent the data to us.
	 * 
	 * @return Address of the client that sent us data that resulted in this
	 *         channel being created.
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
	 * Send message to whoever is connected to us. Uses the topmost via address
	 * to send to.
	 * 
	 * @param msg
	 *            is the message to send.
	 * @param retry
	 */
	private void sendMessage(byte[] msg, boolean retry) throws IOException {
		SSLSocket sock = (SSLSocket) this.sipStack.ioHandler.sendBytes(this
				.getMessageProcessor().getIPAddress(), this.peerAddress,
				this.peerPort, this.peerProtocol, msg, retry);
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

			Thread thread = new Thread(this);
			thread.setDaemon(true);
			thread.setName("TLSMessageChannelThread");
			thread.start();
		}

	}

	/**
	 * Return a formatted message to the client. We try to re-connect with the
	 * peer on the other end if possible.
	 * 
	 * @param sipMessage
	 *            Message to send.
	 * @throws IOException
	 *             If there is an error sending the message
	 */
	public void sendMessage(SIPMessage sipMessage) throws IOException {
		byte[] msg = sipMessage.encodeAsBytes();

		long time = System.currentTimeMillis();

		this.sendMessage(msg, sipMessage instanceof SIPRequest);

		if (this.sipStack.serverLog.needsLogging(ServerLog.TRACE_MESSAGES))
			logMessage(sipMessage, peerAddress, peerPort, time);
	}

	/**
	 * Send a message to a specified address.
	 * 
	 * @param message
	 *            Pre-formatted message to send.
	 * @param receiverAddress
	 *            Address to send it to.
	 * @param receiverPort
	 *            Receiver port.
	 * @throws IOException
	 *             If there is a problem connecting or sending.
	 */
	public void sendMessage(byte message[], InetAddress receiverAddress,
			int receiverPort, boolean retry) throws IOException {
		if (message == null || receiverAddress == null)
			throw new IllegalArgumentException("Null argument");
		SSLSocket sock = (SSLSocket) this.sipStack.ioHandler.sendBytes(
				this.messageProcessor.getIPAddress(), receiverAddress,
				receiverPort, "TLS", message, retry);
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

			// start a new reader on this end of the pipe.
			Thread mythread = new Thread(this);
			mythread.setDaemon(true);
			mythread.setName("TLSMessageChannelThread");
			mythread.start();
		}

	}

	/**
	 * Exception processor for exceptions detected from the parser. (This is
	 * invoked by the parser when an error is detected).
	 * 
	 * @param sipMessage --
	 *            the message that incurred the error.
	 * @param ex --
	 *            parse exception detected by the parser.
	 * @param header --
	 *            header that caused the error.
	 * @throws ParseException
	 *             Thrown if we want to reject the message.
	 */
	public void handleException(ParseException ex, SIPMessage sipMessage,
			Class hdrClass, String header, String message)
			throws ParseException {
		if (sipStack.isLoggingEnabled())
			sipStack.logWriter.logException(ex);
		// Log the bad message for later reference.
		if ((hdrClass != null)
				&& (hdrClass.equals(From.class) || hdrClass.equals(To.class)
						|| hdrClass.equals(CSeq.class)
						|| hdrClass.equals(Via.class)
						|| hdrClass.equals(CallID.class)
						|| hdrClass.equals(RequestLine.class) || hdrClass
						.equals(StatusLine.class))) {
			sipStack.getLogWriter().logDebug(
					"Encountered bad message \n" + message);
			throw ex;
		} else {
			sipMessage.addUnparsed(header);
		}
	}

	/**
	 * Gets invoked by the parser as a callback on successful message parsing
	 * (i.e. no parser errors).
	 * 
	 * @param sipMessage
	 *            Mesage to process (this calls the application for processing
	 *            the message).
	 */
	public void processMessage(SIPMessage sipMessage) throws Exception {
		try {
			if (sipMessage.getFrom() == null || sipMessage.getTo() == null
					|| sipMessage.getCallId() == null
					|| sipMessage.getCSeq() == null
					|| sipMessage.getViaHeaders() == null) {
				String badmsg = sipMessage.encode();
				if (sipStack.isLoggingEnabled()) {
					sipStack.logWriter.logError("bad message " + badmsg);
					sipStack.logWriter.logError(">>> Dropped Bad Msg");
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
				//the peer address and tag it appropriately.
				Hop hop = sipStack.addressResolver.resolveAddress(
						v.getHop());
				this.peerPort = hop.getPort();
				this.peerProtocol = v.getTransport();
				try {
					this.peerAddress = mySock.getInetAddress();
					// Check to see if the received parameter matches
						InetAddress sentByAddress = InetAddress.getByName(hop
							.getHost());

					if (!sentByAddress.equals(this.peerAddress)) {
						v.setParameter(Via.RECEIVED, this.peerAddress
								.getHostAddress());
						// @@@ hagai
						v.setParameter(Via.RPORT, new Integer(this.peerPort)
								.toString());
					}
				} catch (java.net.UnknownHostException ex) {
					// Could not resolve the sender address.
					if (sipStack.isLoggingEnabled()) {
						sipStack.logWriter
								.logDebug("Rejecting message -- could not resolve Via Address");
					}
					return;
				} catch (java.text.ParseException ex) {
					InternalErrorHandler.handleException(ex);
				}
				// Use this for outgoing messages as well.
				if (!this.isCached) {
					((TLSMessageProcessor) this.messageProcessor)
							.cacheMessageChannel(this);
					this.isCached = true;
					String key = IOHandler.makeKey(mySock.getInetAddress(),
							this.peerPort);
					sipStack.ioHandler.putSocket(key, mySock);
				}
			}

			// Foreach part of the request header, fetch it and process it

			long receptionTime = System.currentTimeMillis();
			//

			if (sipMessage instanceof SIPRequest) {
				// This is a request - process the request.
				SIPRequest sipRequest = (SIPRequest) sipMessage;
				// Create a new sever side request processor for this
				// message and let it handle the rest.

				if (sipStack.isLoggingEnabled()) {
					sipStack.logWriter.logDebug("----Processing Message---");
				}
				if (this.sipStack.serverLog
						.needsLogging(ServerLog.TRACE_MESSAGES)) {

					sipStack.serverLog.logMessage(sipMessage, this.getPeerHostPort().toString() , 
							this.messageProcessor.getIPAddress()
									.getHostAddress()
									+ ":" + this.messageProcessor.getPort(),
							false, receptionTime);

				}
				// Check for reasonable size - reject message
				// if it is too long.
				if (sipStack.getMaxMessageSize() > 0
						&& sipRequest.getSize()
								+ (sipRequest.getContentLength() == null ? 0
										: sipRequest.getContentLength()
												.getContentLength()) > sipStack
								.getMaxMessageSize()) {
					SIPResponse sipResponse = sipRequest
							.createResponse(SIPResponse.MESSAGE_TOO_LARGE);
					byte[] resp = sipResponse.encodeAsBytes();
					this.sendMessage(resp, false);
					throw new Exception("Message size exceeded");
				}

				// Stack could not create a new server request interface.
				// maybe not enough resources.
				ServerRequestInterface sipServerRequest = sipStack
						.newSIPServerRequest(sipRequest, this);
				if (sipServerRequest != null) {
					try {
						sipServerRequest.processRequest(sipRequest, this);
					} finally {
						if (sipServerRequest instanceof SIPTransaction) {
							SIPServerTransaction sipServerTx = (SIPServerTransaction) sipServerRequest;
							if (!sipServerTx.passToListener())
								((SIPTransaction) sipServerRequest)
										.releaseSem();
						}
					}
				} else {
					SIPResponse response = sipRequest
							.createResponse(Response.SERVICE_UNAVAILABLE);
					response.addHeader(sipStack.createServerHeaderForStack());
					RetryAfter retryAfter = new RetryAfter();

					// Be a good citizen and send a decent response code back.
					try {
						retryAfter.setRetryAfter((int) (10 * (Math.random())));
						response.setHeader(retryAfter);
						this.sendMessage(response);
					} catch (Exception e) {
						// IGNore
					}
					sipStack.logWriter
							.logWarning("Dropping message -- could not acquire semaphore");
				}
			} else {
				SIPResponse sipResponse = (SIPResponse) sipMessage;
				if (sipResponse.getStatusCode() == 100)
					sipResponse.getTo().removeParameter("tag");
				try {
					sipResponse.checkHeaders();
				} catch (ParseException ex) {
					if (sipStack.isLoggingEnabled())
						sipStack.logWriter
								.logError("Dropping Badly formatted response message >>> "
										+ sipResponse);
					return;
				}
				// This is a response message - process it.
				// Check the size of the response.
				// If it is too large dump it silently.
				if (sipStack.getMaxMessageSize() > 0
						&& sipResponse.getSize()
								+ (sipResponse.getContentLength() == null ? 0
										: sipResponse.getContentLength()
												.getContentLength()) > sipStack
								.getMaxMessageSize()) {
					if (sipStack.isLoggingEnabled())
						this.sipStack.logWriter
								.logDebug("Message size exceeded");
					return;

				}
				ServerResponseInterface sipServerResponse = sipStack
						.newSIPServerResponse(sipResponse, this);
				if (sipServerResponse != null) {
					try {
						sipServerResponse.processResponse(sipResponse, this);
					} finally {
						if (sipServerResponse instanceof SIPTransaction
								&& !((SIPTransaction) sipServerResponse)
										.passToListener()) {
							// Note that the semaphore is released in event
							// scanner if the
							// request is actually processed by the Listener.
							((SIPTransaction) sipServerResponse).releaseSem();
						}
					}
				} else {
					sipStack.logWriter
							.logWarning("Could not get semaphore... dropping response");
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
		hispipe = new Pipeline(myClientInputStream, sipStack.readTimeout,
				((SIPTransactionStack) sipStack).timer);
		// Create a pipelined message parser to read and parse
		// messages that we write out to him.
		myParser = new PipelinedMsgParser(this, hispipe, this.sipStack
				.getMaxMessageSize());
		// Start running the parser thread.
		myParser.processInput();
		// bug fix by Emmanuel Proulx
		int bufferSize = 4096;
		this.tlsMessageProcessor.useCount++;
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
								synchronized (tlsMessageProcessor) {
									tlsMessageProcessor.nConnections--;
									tlsMessageProcessor.notify();
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
						if (sipStack.isLoggingEnabled())
							sipStack.logWriter
									.logDebug("IOException  closing sock " + ex);
						try {
							if (sipStack.maxConnections != -1) {
								synchronized (tlsMessageProcessor) {
									tlsMessageProcessor.nConnections--;
									tlsMessageProcessor.notify();
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
			this.tlsMessageProcessor.remove(this);
			this.tlsMessageProcessor.useCount--;
		}

	}

	protected void uncache() {
		this.tlsMessageProcessor.remove(this);
	}

	/**
	 * Equals predicate.
	 * 
	 * @param other
	 *            is the other object to compare ourselves to for equals
	 */

	public boolean equals(Object other) {

		if (!this.getClass().equals(other.getClass()))
			return false;
		else {
			TLSMessageChannel that = (TLSMessageChannel) other;
			if (this.mySock != that.mySock)
				return false;
			else
				return true;
		}
	}

	/**
	 * Get an identifying key. This key is used to cache the connection and
	 * re-use it if necessary.
	 */
	public String getKey() {
		if (this.key != null) {
			return this.key;
		} else {
			this.key = MessageChannel.getKey(this.peerAddress, this.peerPort,
					"TLS");
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
	 * TLS Is a secure protocol.
	 */
	public boolean isSecure() {
		return true;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.16  2006/06/27 12:52:41  mranga
 * Do not update the Local Tag of the dialog if the 2xx has a non null local tag. Fixed a bug in automatic 503 generation if listener is blocked.
 *
 * Revision 1.15  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.14  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.13  2006/06/13 06:34:32  mranga
 * Javadoc fixups and code  rearrangement. Enough javadoc tweaking for the day :-)
 *
 * Revision 1.12  2006/06/11 18:51:04  mranga
 * sentBy check for via headers fixed. Added Address lookup facility.
 * Revision 1.11 2006/06/10 14:04:47 mranga
 * Added more rate limiting mechanism. Stack will now return an error if the
 * server transaction table is too large. Fixed another performance bug.
 * Performance now at 76 to 79 caps on self test.
 * 
 * 
 * Revision 1.10 2006/06/04 18:49:31 mranga Got forked subscriptions scenario
 * worked out. FIxed some terrible race conditons with forked subscriptions (
 * respojnse arriving at the same time as NOTIFY) and other delights.
 * 
 * $Log: not supported by cvs2svn $
 * Revision 1.16  2006/06/27 12:52:41  mranga
 * Do not update the Local Tag of the dialog if the 2xx has a non null local tag. Fixed a bug in automatic 503 generation if listener is blocked.
 *
 * Revision 1.15  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.14  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.13  2006/06/13 06:34:32  mranga
 * Javadoc fixups and code  rearrangement. Enough javadoc tweaking for the day :-)
 *
 * Revision 1.12  2006/06/11 18:51:04  mranga
 * sentBy check for via headers fixed. Added Address lookup facility.
 *
 * Revision 1.11  2006/06/10 14:04:47  mranga
 * Added more rate limiting mechanism. Stack will now return an error if the server transaction table is too large. Fixed another performance bug. Performance now at 76 to 79 caps on self test.
 *
 *
 * Revision 1.10  2006/06/04 18:49:31  mranga
 * Got forked subscriptions scenario worked out. FIxed some terrible race conditons with forked subscriptions ( respojnse arriving at the same time as NOTIFY) and other delights.
 *
 * 
 * Revision 1.9 2006/05/22 18:39:02 mranga Fixed a deadlock which occurs under
 * heavy load (fix provided by Attila Ackossis). Fixed condition where ACKS are
 * dropped under heavy load. Revision 1.8 2006/03/16 21:15:43 mranga Fixes
 * reentrancy problem Revision 1.7 2006/03/15 02:22:54 mranga fixed some
 * reentrancy problems
 * 
 * Revision 1.6 2006/03/10 22:19:50 mranga Fixed a deadlock problem. Added a
 * redirect example
 * 
 * Revision 1.5 2006/03/01 21:32:56 mranga Bug fixes galore
 * 
 * Revision 1.4 2005/11/28 02:13:56 mranga *** empty log message ***
 * 
 * Revision 1.3 2005/11/21 19:20:29 mranga *** empty log message ***
 * 
 * Revision 1.2 2005/11/14 22:36:01 mranga Interim update of source code
 * 
 * Revision 1.1.1.1 2005/10/04 17:12:36 mranga
 * 
 * Import
 * 
 * Revision 1.2 2004/11/28 17:32:26 mranga Submitted by: hagai sela Reviewed by:
 * mranga
 * 
 * Support for symmetric nats
 * 
 * Revision 1.1 2004/10/28 19:02:51 mranga Submitted by: Daniel Martinez
 * Reviewed by: M. Ranganathan
 * 
 * Added changes for TLS support contributed by Daniel Martinez
 * 
 * Revision 1.31 2004/08/23 23:56:20 mranga Reviewed by: mranga forgot to set
 * isDaemon in one or two places where threads were being created and cleaned up
 * some minor junk.
 * 
 * Revision 1.30 2004/07/16 17:13:56 mranga Submitted by: Damand Joost Reviewed
 * by: mranga
 * 
 * Make threads into daemon threads, use address for received = parameter on via
 * 
 * Revision 1.29 2004/06/21 05:42:33 mranga Reviewed by: mranga more code
 * smithing
 * 
 * Revision 1.28 2004/06/21 04:59:53 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.27 2004/05/30 18:55:58 mranga Reviewed by: mranga Move to timers
 * and eliminate the Transaction scanner Thread to improve scalability and
 * reduce cpu usage.
 * 
 * Revision 1.26 2004/05/18 15:26:45 mranga Reviewed by: mranga Attempted fix at
 * race condition bug. Remove redundant exception (never thrown). Clean up some
 * extraneous junk.
 * 
 * Revision 1.25 2004/05/16 14:13:23 mranga Reviewed by: mranga Fixed the
 * use-count issue reported by Peter Parnes. Added property to prevent against
 * content-length dos attacks.
 * 
 * Revision 1.24 2004/04/22 22:51:19 mranga Submitted by: Thomas Froment
 * Reviewed by: mranga
 * 
 * Fixed corner cases.
 * 
 * Revision 1.23 2004/04/21 16:25:22 mranga Reviewed by: mranga Record IP
 * address of peer in TCP connection as soon as connection is made. Remove range
 * check on Warning.java
 * 
 * Revision 1.22 2004/03/30 17:53:56 mranga Reviewed by: mranga more reference
 * counting cleanup
 * 
 * Revision 1.21 2004/03/30 16:40:30 mranga Reviewed by: mranga more tweaks to
 * reference counting for cleanup.
 * 
 * Revision 1.20 2004/03/30 15:38:18 mranga Reviewed by: mranga Name the threads
 * so as to facilitate debugging.
 * 
 * Revision 1.19 2004/03/19 23:41:30 mranga Reviewed by: mranga Fixed connection
 * and thread caching.
 * 
 * Revision 1.18 2004/03/19 17:26:20 mranga Reviewed by: mranga Fixed silly bug.
 * 
 * Revision 1.17 2004/03/19 17:06:19 mranga Reviewed by: mranga Fixed some
 * sipStack cleanup issues. Stack should release all resources when finalized.
 * 
 * Revision 1.16 2004/03/19 04:22:22 mranga Reviewed by: mranga Added IO Pacing
 * for long writes - split write into chunks and flush after each chunk to avoid
 * socket back pressure.
 * 
 * Revision 1.15 2004/03/18 22:01:20 mranga Reviewed by: mranga Get rid of the
 * PipedInputStream from pipelined parser to avoid a copy.
 * 
 * Revision 1.14 2004/03/09 00:34:45 mranga Reviewed by: mranga Added TCP
 * connection management for client and server side Transactions. See
 * configuration parameter gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false Releases Client TCP
 * Connections after linger time
 * 
 * Revision 1.13 2004/03/07 22:25:25 mranga Reviewed by: mranga Added a new
 * configuration parameter that instructs the sipStack to drop a server
 * connection after server transaction termination set
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this Default behavior
 * is true.
 * 
 * Revision 1.12 2004/03/05 20:36:55 mranga Reviewed by: mranga put in some
 * debug printfs and cleaned some things up.
 * 
 * Revision 1.11 2004/02/29 15:32:59 mranga Reviewed by: mranga bug fixes on
 * limiting the max message size.
 * 
 * Revision 1.10 2004/02/29 00:46:35 mranga Reviewed by: mranga Added new
 * configuration property to limit max message size for TCP transport. The
 * property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 * 
 * Revision 1.9 2004/01/22 18:39:41 mranga Reviewed by: M. Ranganathan Moved the
 * ifdef SIMULATION and associated tags to the first column so Prep preprocessor
 * can deal with them.
 * 
 * Revision 1.8 2004/01/22 13:26:33 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 */
