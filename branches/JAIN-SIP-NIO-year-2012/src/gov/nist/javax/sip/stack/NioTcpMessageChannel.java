package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.InternalErrorHandler;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.header.CSeq;
import gov.nist.javax.sip.header.CallID;
import gov.nist.javax.sip.header.ContentLength;
import gov.nist.javax.sip.header.From;
import gov.nist.javax.sip.header.RequestLine;
import gov.nist.javax.sip.header.StatusLine;
import gov.nist.javax.sip.header.To;
import gov.nist.javax.sip.header.Via;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.parser.NioPipelineParser;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.HashMap;

public class NioTcpMessageChannel extends ConnectionOrientedMessageChannel {
	private static StackLogger logger = CommonLogger
			.getLogger(NioTcpMessageChannel.class);
	private static HashMap<SocketChannel, NioTcpMessageChannel> channelMap = new HashMap<SocketChannel, NioTcpMessageChannel>();

	private SocketChannel socketChannel;
	NioPipelineParser nioParser = null;

	public static NioTcpMessageChannel create(
			NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		NioTcpMessageChannel retval = channelMap.get(socketChannel);
		if (retval == null) {
			retval = new NioTcpMessageChannel(nioTcpMessageProcessor,
					socketChannel);
			channelMap.put(socketChannel, retval);
		}
		retval.messageProcessor = nioTcpMessageProcessor;
		retval.myClientInputStream = socketChannel.socket().getInputStream();
		return retval;
	}

	public static NioTcpMessageChannel get(SocketChannel socketChannel) {
		return channelMap.get(socketChannel);
	}

	public static void putNioTcpMessageChannel(SocketChannel socketChannel,
			NioTcpMessageChannel nioTcpMessageChannel) {
		channelMap.put(socketChannel, nioTcpMessageChannel);
	}
	

	public void readChannel() {
		logger.logDebug("NioTcpMessageChannel::readChannel");
		int bufferSize = 4096;
		byte[] msg = new byte[bufferSize];
		((ConnectionOrientedMessageProcessor) this.messageProcessor).useCount++;
		this.isRunning = true;
		try {
			ByteBuffer byteBuffer  = ByteBuffer.wrap(msg);
			int nbytes = this.socketChannel.read(byteBuffer);
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Read " + nbytes + " from socketChannel");
			}
			
			// TODO: This happens on weird conditions when a dead socket suddently resurects but we already have another socket
			if (nbytes == -1) {
				
				nioParser.addBytes("\r\n\r\n".getBytes("UTF-8"));
				try {
					if (sipStack.maxConnections != -1) {
						synchronized (messageProcessor) {
							((ConnectionOrientedMessageProcessor) this.messageProcessor).nConnections--;
							messageProcessor.notify();
						}
					}
					nioParser.close();
					close();
				} catch (Exception ioex) {
				}
				channelMap.remove(socketChannel);
				this.isRunning = false;
				((ConnectionOrientedMessageProcessor) this.messageProcessor)
						.remove(this);
				((ConnectionOrientedMessageProcessor) this.messageProcessor).useCount--;
				// parser could be null if the socket was closed by the remote
				// end already
				if (myParser != null) {
					myParser.close();
				}
				((NioTcpMessageProcessor) messageProcessor).checkPending();
				return;
			}
			byte[] bytes = new byte[nbytes];
			System.arraycopy(msg, 0, bytes, 0, nbytes);
			nioParser.addBytes(bytes);

		} catch (IOException ex) {
			// Terminate the message.
			try {
				nioParser.addBytes("\r\n\r\n".getBytes("UTF-8"));
			} catch (Exception e) {
				// InternalErrorHandler.handleException(e);
			}

			try {
				if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
					logger.logDebug("IOException  closing sock " + ex);

				if (sipStack.maxConnections != -1) {
					synchronized (messageProcessor) {
						((ConnectionOrientedMessageProcessor) this.messageProcessor).nConnections--;
						messageProcessor.notify();
					}
				}
				close();
				nioParser.close();
				this.isRunning = false;
				((ConnectionOrientedMessageProcessor) this.messageProcessor)
				.remove(this);
				((ConnectionOrientedMessageProcessor) this.messageProcessor).useCount--;
				// parser could be null if the socket was closed by the
				// remote end already
				if (myParser != null) {
					myParser.close();
				}
			} catch (Exception ex1) {
				// Do nothing.
			}
			return;
		} catch (Exception ex) {
			InternalErrorHandler.handleException(ex, logger);
		}

	}

	private NioTcpMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		super(nioTcpMessageProcessor.getSIPStack());
		super.myClientInputStream = socketChannel.socket().getInputStream();
		try {
			this.peerAddress = socketChannel.socket().getInetAddress();
			this.peerPort = socketChannel.socket().getPort();
			this.socketChannel = socketChannel;
			// messages that we write out to him.
			nioParser = new NioPipelineParser(sipStack, this,
					this.sipStack.getMaxMessageSize());
			this.peerProtocol = "TCP";
		} finally {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Done creating NioTcpMessageChannel " + this);
			}
		}

	}

	public NioTcpMessageChannel(InetAddress inetAddress, int port,
			SIPTransactionStack sipStack,
			NioTcpMessageProcessor nioTcpMessageProcessor) throws IOException {
		super(sipStack);
		logger.logDebug("NioTcpMessageChannel::NioTcpMessageChannel: "
				+ inetAddress.getHostAddress() + ":" + port);
		try {
			messageProcessor = nioTcpMessageProcessor;
			SocketAddress sockAddr = new InetSocketAddress(inetAddress, port);
			socketChannel = nioTcpMessageProcessor.blockingConnect((InetSocketAddress) sockAddr, 10000);
			peerAddress = socketChannel.socket().getInetAddress();
			peerPort = socketChannel.socket().getPort();
			peerProtocol = "TCP";
			nioParser = new NioPipelineParser(sipStack, this,
					this.sipStack.getMaxMessageSize());
			putNioTcpMessageChannel(socketChannel, this);
			if(this.socketChannel != null && this.socketChannel.isConnected()) {
				nioTcpMessageProcessor.nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
			}
		} finally {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger
						.logDebug("NioTcpMessageChannel::NioTcpMessageChannel: Done creating NioTcpMessageChannel " + this);
			}
		}
	}

	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	@Override
	protected void close(boolean b) {
		try {
			socketChannel.close();
		} catch (IOException e) {
			logger.logError("Problem occured while closing");
		}

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
	 * Send message to whoever is connected to us. Uses the topmost via address
	 * to send to.
	 * 
	 * @param msg
	 *            is the message to send.
	 * @param isClient
	 */
	protected void sendMessage(byte[] msg, boolean isClient) throws IOException {

		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("sendMessage isClient  = " + isClient + " this = " + this);
		}
		/*
		 * /* Patch from kircuv@dev.java.net (Issue 119 ) This patch avoids the
		 * case where two TCPMessageChannels are now pointing to the same
		 * socket.getInputStream().
		 * 
		 * JvB 22/5 removed
		 */
		SocketChannel sock = null;
		NIOHandler nioHandler = ((NioTcpMessageProcessor) messageProcessor).nioHandler;
		if(this.socketChannel != null && this.socketChannel.isConnected()) {
			nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
		}
		IOException problem = null;
		try {
			sock = ((NioTcpMessageProcessor) messageProcessor).nioHandler.sendBytes(this.messageProcessor
					.getIpAddress(), this.peerAddress, this.peerPort,
					this.peerProtocol, msg, isClient, this);
		} catch (IOException any) {
			problem = any;
			logger.logWarning("Failed to connect " + this.peerAddress + ":"
					+ this.peerPort + " but trying the advertised port="
					+ this.peerPortAdvertisedInHeaders
					+ " if it's different than the port we just failed on");
		}
		if (sock == null) { // If we couldn't connect to the host, try the
							// advertised port as failsafe
			if (this.peerPort != this.peerPortAdvertisedInHeaders
					&& peerPortAdvertisedInHeaders > 0) { // no point in trying
															// same port
				logger.logWarning("Couldn't connect to peerAddress = "
						+ peerAddress + " peerPort = " + peerPort + " key = "
						+ key + " retrying on peerPortAdvertisedInHeaders "
						+ peerPortAdvertisedInHeaders);

				sock = ((NioTcpMessageProcessor) messageProcessor).nioHandler.sendBytes(this.messageProcessor
						.getIpAddress(), this.peerAddress,
						this.peerPortAdvertisedInHeaders, this.peerProtocol,
						msg, isClient, this);
				this.peerPort = this.peerPortAdvertisedInHeaders;
				this.key = MessageChannel.getKey(peerAddress, peerPort, "TCP");
				logger.logWarning("retry suceeded to peerAddress = "
						+ peerAddress + " peerPortAdvertisedInHeaders = "
						+ peerPortAdvertisedInHeaders + " key = " + key);
			} else {
				throw problem; // throw the original excpetion we had from the
								// first attempt
			}
		}

		// Created a new socket so close the old one and stick the new
		// one in its place but dont do this if it is a datagram socket.
		// (could have replied via udp but received via tcp!).
		// if (mySock == null && s != null) {
		// this.uncache();
		// } else
		if (sock != socketChannel && sock != null) {
			if (socketChannel != null) {
				if (logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
					logger
							.logWarning("Old socket different than new socket on channel "
									+ sock + " " + socketChannel);
					logger.logStackTrace();
					logger.logWarning("Old socket local ip address "
							+ socketChannel.socket().getLocalSocketAddress());
					logger.logWarning("Old socket remote ip address "
							+ socketChannel.socket().getRemoteSocketAddress());
					logger.logWarning("New socket local ip address "
							+ sock.socket().getLocalSocketAddress());
					logger.logWarning("New socket remote ip address "
							+ sock.socket().getRemoteSocketAddress());
				}
				close(false);
			}
			if (problem == null) {
				if (socketChannel != null) {
					if (logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
						logger
								.logWarning("There was no exception for the retry mechanism so creating a new thread based on the new socket for incoming "
										+ key);
					}
				}
				socketChannel = sock;

			} else {
				if (logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
					logger
							.logWarning("There was an exception for the retry mechanism so not creating a new thread based on the new socket for incoming "
									+ key);
				}
				socketChannel = sock;
			}
		}

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
		if (message == null || receiverAddress == null) {
			logger.logError("receiverAddress = " + receiverAddress);
			throw new IllegalArgumentException("Null argument");
		}

		if (peerPortAdvertisedInHeaders <= 0) {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("receiver port = " + receiverPort
						+ " for this channel " + this + " key " + key);
			}
			if (receiverPort <= 0) {
				// if port is 0 we assume the default port for TCP
				this.peerPortAdvertisedInHeaders = 5060;
			} else {
				this.peerPortAdvertisedInHeaders = receiverPort;
			}
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("2.Storing peerPortAdvertisedInHeaders = "
						+ peerPortAdvertisedInHeaders + " for this channel "
						+ this + " key " + key);
			}
		}

		SocketChannel sock = null;
		IOException problem = null;
		try {
			sock = ((NioTcpMessageProcessor) messageProcessor).nioHandler.sendBytes(this.messageProcessor
					.getIpAddress(), receiverAddress, receiverPort, "TCP",
					message, retry, this);
		} catch (IOException any) {
			problem = any;
			logger.logWarning("Failed to connect " + this.peerAddress + ":"
					+ receiverPort + " but trying the advertised port="
					+ this.peerPortAdvertisedInHeaders
					+ " if it's different than the port we just failed on");
			logger.logError("Error is ", any);

		}
		if (sock == null) { // If we couldn't connect to the host, try the
							// advertised port as failsafe
			if (receiverPort != this.peerPortAdvertisedInHeaders
					&& peerPortAdvertisedInHeaders > 0) { // no point in trying
															// same port
				logger.logWarning("Couldn't connect to receiverAddress = "
						+ receiverAddress + " receiverPort = " + receiverPort
						+ " key = " + key
						+ " retrying on peerPortAdvertisedInHeaders "
						+ peerPortAdvertisedInHeaders);
				sock = ((NioTcpMessageProcessor) messageProcessor).nioHandler.sendBytes(this.messageProcessor
						.getIpAddress(), receiverAddress,
						this.peerPortAdvertisedInHeaders, "TCP", message,
						retry, this);
				this.peerPort = this.peerPortAdvertisedInHeaders;
				this.key = MessageChannel.getKey(peerAddress,
						peerPortAdvertisedInHeaders, "TCP");

				logger.logWarning("retry suceeded to receiverAddress = "
						+ receiverAddress + " peerPortAdvertisedInHeaders = "
						+ peerPortAdvertisedInHeaders + " key = " + key);
			} else {
				throw problem; // throw the original excpetion we had from the
								// first attempt
			}
		}

		if (sock != socketChannel && sock != null) {
			if (sock != null) {
				if (logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
					logger
							.logWarning("[2] Old socket different than new socket on channel "
									+ key + socketChannel + " " + sock);
					logger.logStackTrace();
					logger.logWarning("Old socket local ip address "
							+ socketChannel.socket().getLocalSocketAddress());
					logger.logWarning("Old socket remote ip address "
							+ socketChannel.socket().getRemoteSocketAddress());
					logger.logWarning("New socket local ip address "
							+ sock.socket().getLocalSocketAddress());
					logger.logWarning("New socket remote ip address "
							+ sock.socket().getRemoteSocketAddress());
				}
				close(false);
			}
			if (problem == null) {
				if (sock != null) {
					if (logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
						logger
								.logWarning("There was no exception for the retry mechanism so we keep going "
										+ key);
					}
				}
				socketChannel = sock;
			} else {
				if (logger.isLoggingEnabled(LogWriter.TRACE_WARN)) {
					logger
							.logWarning("There was an exception for the retry mechanism so stop "
									+ key);
				}
				socketChannel = sock;
			}
		}

	}

	/**
	 * Exception processor for exceptions detected from the parser. (This is
	 * invoked by the parser when an error is detected).
	 * 
	 * @param sipMessage
	 *            -- the message that incurred the error.
	 * @param ex
	 *            -- parse exception detected by the parser.
	 * @param header
	 *            -- header that caused the error.
	 * @throws ParseException
	 *             Thrown if we want to reject the message.
	 */
	public void handleException(ParseException ex, SIPMessage sipMessage,
			Class hdrClass, String header, String message)
			throws ParseException {
		if (logger.isLoggingEnabled())
			logger.logException(ex);
		// Log the bad message for later reference.
		if ((hdrClass != null)
				&& (hdrClass.equals(From.class) || hdrClass.equals(To.class)
						|| hdrClass.equals(CSeq.class)
						|| hdrClass.equals(Via.class)
						|| hdrClass.equals(CallID.class)
						|| hdrClass.equals(ContentLength.class)
						|| hdrClass.equals(RequestLine.class) || hdrClass
						.equals(StatusLine.class))) {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("Encountered Bad Message \n"
						+ sipMessage.toString());
			}

			// JvB: send a 400 response for requests (except ACK)
			// Currently only UDP, @todo also other transports
			String msgString = sipMessage.toString();
			if (!msgString.startsWith("SIP/") && !msgString.startsWith("ACK ")) {
				if (socketChannel != null) {
					if (logger.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
						logger
								.logError("Malformed mandatory headers: closing socket! :"
										+ socketChannel.toString());
					}

					try {
						socketChannel.close();

					} catch (IOException ie) {
						if (logger.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
							logger.logError("Exception while closing socket! :"
									+ socketChannel.toString() + ":" + ie.toString());
						}

					}
				}
			}

			throw ex;
		} else {
			sipMessage.addUnparsed(header);
		}
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
			NioTcpMessageChannel that = (NioTcpMessageChannel) other;
			if (this.socketChannel != that.socketChannel)
				return false;
			else
				return true;
		}
	}

	/**
	 * TCP Is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}

}
