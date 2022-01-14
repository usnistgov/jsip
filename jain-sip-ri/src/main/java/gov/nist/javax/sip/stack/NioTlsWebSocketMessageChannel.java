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
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPMessage;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SSLStateMachine.MessageSendCallback;

import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLContext;
import javax.sip.address.SipURI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;
import java.util.Timer;
import java.util.TimerTask;

public class NioTlsWebSocketMessageChannel extends NioWebSocketMessageChannel implements NioTlsChannelInterface {

	private static StackLogger logger = CommonLogger
			.getLogger(NioTlsWebSocketMessageChannel.class);
	
	SSLStateMachine sslStateMachine;

	private int appBufferMax;
	private int netBufferMax;

	public static NioTlsWebSocketMessageChannel create(
			NioTlsWebSocketMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		NioTlsWebSocketMessageChannel retval = (NioTlsWebSocketMessageChannel) channelMap.get(socketChannel);
		if (retval == null) {
			retval = new NioTlsWebSocketMessageChannel(nioTcpMessageProcessor,
					socketChannel);
			channelMap.put(socketChannel, retval);
		}
		return retval;
	}
	
	protected NioTlsWebSocketMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		super(nioTcpMessageProcessor, socketChannel);

		messageProcessor = nioTcpMessageProcessor;
		myClientInputStream = socketChannel.socket().getInputStream();
		try {
			this.init(false);
			createBuffers();
		}catch (Exception e) {
			throw new IOException("Can't do TLS init", e);
		}
	}
	
	public void init(boolean clientMode) throws Exception, CertificateException, FileNotFoundException, IOException {
        SSLContext ctx = clientMode ?
        		((NioTlsWebSocketMessageProcessor)messageProcessor).sslClientCtx:
                ((NioTlsWebSocketMessageProcessor)messageProcessor).sslServerCtx;
		sslStateMachine = new SSLStateMachine(ctx.createSSLEngine(), this);

        sslStateMachine.sslEngine.setUseClientMode(clientMode);
        String auth = ((SipStackImpl)super.sipStack).
        		getConfigurationProperties().getProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE");

        sslStateMachine.sslEngine.setNeedClientAuth(false);
        sslStateMachine.sslEngine.setWantClientAuth(false);

        String clientProtocols = ((SipStackImpl)super.sipStack)
        		.getConfigurationProperties().getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
        if(clientProtocols != null) {
        	sslStateMachine.sslEngine.setEnabledProtocols(clientProtocols.split(","));
        }

	}
	
	public ByteBuffer prepareEncryptedDataBuffer() {
		return ByteBufferFactory.getInstance().allocateDirect(netBufferMax);
	}
	
	public ByteBuffer prepareAppDataBuffer() {
		return ByteBufferFactory.getInstance().allocateDirect(appBufferMax);
	}
	
	public ByteBuffer prepareAppDataBuffer(int capacity) {
		return ByteBufferFactory.getInstance().allocateDirect(capacity);
	}
	
	public static class SSLReconnectedException extends IOException {
		private static final long serialVersionUID = 1L;}
	
	@Override
	protected void sendMessage(final byte[] msg, final boolean isClient) throws IOException {
		checkSocketState();

		ByteBuffer b = ByteBuffer.wrap(NioWebSocketMessageChannel.wrapBufferIntoWebSocketFrame(msg, client));
		try {
			sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {

				@Override
				public void doSend(byte[] bytes) throws IOException {
					NioTlsWebSocketMessageChannel.super.sendNonWebSocketMessage(bytes, isClient);
				}
			});
		} catch (Exception e) {
			throw new IOException("Can't send message", e);
		}
	}
	
	public void sendEncryptedData(byte[] msg) throws IOException { 
		// bypass the encryption for already encrypted data or TLS metadata
		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("sendEncryptedData " + " this = " + this + " peerPort = " + peerPort + " addr = " + peerAddress);
		}
		lastActivityTimeStamp = System.currentTimeMillis();
		
		NIOHandler nioHandler = ((NioTcpMessageProcessor) messageProcessor).nioHandler;
		if(this.socketChannel != null && this.socketChannel.isConnected() && this.socketChannel.isOpen()) {
			nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
		}
		super.sendNonWebSocketMessage(msg, false);
		//super.sendMessage(msg, this.peerAddress, this.peerPort, true);
	}
	@Override
	public void sendMessage(final byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
		checkSocketState();
		
		ByteBuffer b = ByteBuffer.wrap(NioWebSocketMessageChannel.wrapBufferIntoWebSocketFrame(message, client));
		try {
			sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {
				
				@Override
				public void doSend(byte[] bytes) throws IOException {
					NioTlsWebSocketMessageChannel.super.sendTCPMessage(bytes,
							receiverAddress, receiverPort, retry);
					
				}
			});
		} catch (IOException e) {
			throw e;
		}
	}
	
	@Override
	public void sendMessage(final SIPMessage sipMessage, final InetAddress receiverAddress, final int receiverPort)
            throws IOException {
		
		if(sipMessage instanceof SIPRequest && client && !httpClientRequestSent) {
				httpClientRequestSent = true;
				SIPRequest request = (SIPRequest) sipMessage;
				SipURI requestUri = (SipURI) request.getRequestURI();
				this.httpHostHeader = requestUri.getHeader("Host");
				this.httpLocation = requestUri.getHeader("Location");
				this.httpMethod = requestUri.getMethodParam();
				final String http = this.httpMethod + " " + this.httpLocation + " HTTP/1.1\r\n" + 
						"Host: " + this.httpHostHeader + "\r\n" + 
						"Upgrade: websocket\r\n" + 
						"Connection: Upgrade\r\n" + 
						"Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n" + 
						"Sec-WebSocket-Protocol: sip\r\n" + 
						"Sec-WebSocket-Version: 13\r\n\r\n";
				
				
				ByteBuffer b = ByteBuffer.wrap(http.getBytes());
				try {
					sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {

						@Override
						public void doSend(byte[] bytes) throws IOException {
							NioTlsWebSocketMessageChannel.super.sendTCPMessage(bytes,
									receiverAddress, receiverPort, false);

							byte[] wsM = sipMessage.toString().getBytes();
							byte[] wsMessage = NioWebSocketMessageChannel.wrapBufferIntoWebSocketFrame(wsM, client);
							ByteBuffer b = ByteBuffer.wrap(wsMessage);
							try {
								sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {

									@Override
									public void doSend(byte[] bytes) throws IOException {
										NioTlsWebSocketMessageChannel.super.sendTCPMessage(bytes,
												receiverAddress, receiverPort, false);
									}
								});
							} catch (IOException e) {
								throw e;
							}
						}
					});
				} catch (IOException e) {
					throw e;
				}


		} else {
			// https://java.net/jira/browse/JSIP-497 fix transport for WSS
			final byte[] msg = sipMessage.encodeAsBytes(this.getTransport());
			sendMessage(msg, this.client);
		}
    }

	public void sendHttpMessage(final byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
		checkSocketState();
		
		ByteBuffer b = ByteBuffer.wrap(message);
		try {
			sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {
				
				@Override
				public void doSend(byte[] bytes) throws IOException {
					NioTlsWebSocketMessageChannel.super.sendMessage(bytes,
							receiverAddress, receiverPort, retry);
					
				}
			});
		} catch (IOException e) {
			throw e;
		}
	}
	 private void createBuffers() {

	        SSLSession session = sslStateMachine.sslEngine.getSession();
	        appBufferMax = session.getApplicationBufferSize();
	        netBufferMax = session.getPacketBufferSize();
	        
	        if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
	        	logger.logDebug("appBufferMax=" + appBufferMax + " netBufferMax=" + netBufferMax);
	        }
	    }
	
	public NioTlsWebSocketMessageChannel(InetAddress inetAddress, int port,
			SIPTransactionStack sipStack,
			NioTcpMessageProcessor nioTcpMessageProcessor) throws IOException {
		super(inetAddress, port, sipStack, nioTcpMessageProcessor);
		try {
			init(true);
			createBuffers();
		} catch (Exception e) {
			throw new IOException("Can't init the TLS channel", e);
		}
	}
	
	@Override
	protected void addBytes(byte[] bytes) throws Exception {
		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("Adding WSS bytes for decryption " + bytes.length);
		}
		if(bytes.length <= 0) return;
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		sslStateMachine.unwrap(buffer);
	}
	
	@Override
	protected void sendNonWebSocketMessage(byte[] msg, final boolean isClient) throws IOException {

		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("sendMessage isClient  = " + isClient + " this = " + this);
		}
		lastActivityTimeStamp = System.currentTimeMillis();

		NIOHandler nioHandler = ((NioTcpMessageProcessor) messageProcessor).nioHandler;
		if(this.socketChannel != null && this.socketChannel.isConnected() && this.socketChannel.isOpen()) {
			nioHandler.putSocket(NIOHandler.makeKey(this.peerAddress, this.peerPort), this.socketChannel);
		}
		checkSocketState();

		ByteBuffer b = ByteBuffer.wrap(msg);
		try {
			sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {

				@Override
				public void doSend(byte[] bytes) throws IOException {
					NioTlsWebSocketMessageChannel.super.sendTCPMessage(bytes,
							peerAddress, peerPort, isClient);

				}
			});
		} catch (IOException e) {
			throw e;
		}
	}

	@Override
	public String getTransport() {
		return this.messageProcessor.transport;
	}

	@Override
	public void onNewSocket(byte[] message) {
		super.onNewSocket(message);
		try {
			if(logger.isLoggingEnabled(LogLevels.TRACE_DEBUG)) {
				String last = null;
				if(message != null) {
					last = new String(message, "UTF-8");
				}
				logger.logDebug("New socket for " + this + " last message = " + last);
			}
			init(true);
			createBuffers();
			sendMessage(message, false);
		} catch (Exception e) {
			logger.logError("Cant reinit", e);
		}
	}

	private void checkSocketState() throws IOException {
		if (socketChannel != null && (!socketChannel.isConnected() || !socketChannel.isOpen())) {
			if (logger.isLoggingEnabled(LogLevels.TRACE_DEBUG))
				logger.logDebug("Need to reset SSL engine for socket " + socketChannel);
			try {
				init(sslStateMachine.sslEngine.getUseClientMode());
			} catch (Exception ex) {
				logger.logError("Cannot reset SSL engine", ex);
				throw new IOException(ex);
			}
		}
	}

	@Override
	public boolean isSecure() {
		return true;
	}

	@Override
	public void addPlaintextBytes(byte[] bytes) throws Exception {
		super.addBytes(bytes);
	}

	@Override
	public SipStackImpl getSIPStack() {
		return (SipStackImpl) super.getSIPStack();
	}
}
