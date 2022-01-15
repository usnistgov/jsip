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
import gov.nist.javax.sip.stack.SSLStateMachine.MessageSendCallback;

import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;

public class NioTlsMessageChannel extends NioTcpMessageChannel implements NioTlsChannelInterface{

	private static StackLogger logger = CommonLogger
			.getLogger(NioTlsMessageChannel.class);
	
	SSLStateMachine sslStateMachine;
	// Added for https://java.net/jira/browse/JSIP-483
	private HandshakeCompletedListener handshakeCompletedListener;
	private boolean handshakeCompleted = false;

	private int appBufferMax;
	private int netBufferMax;

	public static NioTcpMessageChannel create(
			NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		NioTcpMessageChannel retval = channelMap.get(socketChannel);
		if (retval == null) {
			retval = new NioTlsMessageChannel(nioTcpMessageProcessor,
					socketChannel);
			channelMap.put(socketChannel, retval);
		}
		return retval;
	}
	
	protected NioTlsMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor,
			SocketChannel socketChannel) throws IOException {
		super(nioTcpMessageProcessor, socketChannel);

		messageProcessor = nioTcpMessageProcessor;
		myClientInputStream = socketChannel.socket().getInputStream();
		try {
			init(false);
			createBuffers();
		}catch (Exception e) {
			throw new IOException("Can't do TLS init", e);
		}
	}
	
	public void init(boolean clientMode) throws Exception, CertificateException, FileNotFoundException, IOException {
        SSLContext ctx = clientMode ?
                ((NioTlsMessageProcessor)messageProcessor).sslClientCtx :
                ((NioTlsMessageProcessor)messageProcessor).sslServerCtx;
		sslStateMachine = new SSLStateMachine(ctx.createSSLEngine(), this);

        sslStateMachine.sslEngine.setUseClientMode(clientMode);
        String auth = ((SipStackImpl)super.sipStack).
        		getConfigurationProperties().getProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE");
        if(auth == null) {
        	auth = "Enabled";
        }
        if(auth.equals("Disabled") || auth.equals("DisabledAll")) {
        	sslStateMachine.sslEngine.setNeedClientAuth(false);
        	sslStateMachine.sslEngine.setWantClientAuth(false);
        } else if(auth.equals("Enabled")) {
        	sslStateMachine.sslEngine.setNeedClientAuth(true);
        } else if(auth.equals("Want")) {
        	sslStateMachine.sslEngine.setNeedClientAuth(false);
        	sslStateMachine.sslEngine.setWantClientAuth(true);
        } else {
        	throw new RuntimeException("Invalid parameter for TLS authentication: " + auth);
        }

        // http://java.net/jira/browse/JSIP-451 - josemrecio
    	sslStateMachine.sslEngine.setEnabledProtocols(((SipStackImpl)sipStack).getEnabledProtocols());
    	// Added for https://java.net/jira/browse/JSIP-483 
		if(getHandshakeCompletedListener() == null) {
			HandshakeCompletedListenerImpl listner = new HandshakeCompletedListenerImpl(this, getSocketChannel());
			setHandshakeCompletedListener(listner);
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
		
		ByteBuffer b = ByteBuffer.wrap(msg);
		try {
			sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {

				@Override
				public void doSend(byte[] bytes) throws IOException {
					
						NioTlsMessageChannel.super.sendMessage(bytes, isClient);
					
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
		super.sendMessage(msg, this.peerAddress, this.peerPort, true);
	}
	
	@Override
	public void sendMessage(final byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
	
		checkSocketState();
		
		ByteBuffer b = ByteBuffer.wrap(message);
		try {
			sslStateMachine.wrap(b, ByteBufferFactory.getInstance().allocateDirect(netBufferMax), new MessageSendCallback() {
				
				@Override
				public void doSend(byte[] bytes) throws IOException {
					NioTlsMessageChannel.super.sendMessage(bytes,
							receiverAddress, receiverPort, retry);
					
				}
			});
		} catch (IOException e) {
			throw e;
		}
	}
	 protected void createBuffers() {

	        SSLSession session = sslStateMachine.sslEngine.getSession();
	        appBufferMax = session.getApplicationBufferSize();
	        netBufferMax = session.getPacketBufferSize();
	        
	        if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
	        	logger.logDebug("appBufferMax=" + appBufferMax + " netBufferMax=" + netBufferMax);
	        }
	    }
	
	public NioTlsMessageChannel(InetAddress inetAddress, int port,
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
			logger.logDebug("Adding TLS bytes for decryption " + bytes.length);
		}
		if(bytes.length <= 0) return;
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		sslStateMachine.unwrap(buffer);
	}
	
	@Override
	public String getTransport() {
		return "TLS";
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
		nioParser.addBytes(bytes);
	}
	
	// Methods below Added for https://java.net/jira/browse/JSIP-483 
	public void setHandshakeCompletedListener(
            HandshakeCompletedListener handshakeCompletedListenerImpl) {
        this.handshakeCompletedListener = handshakeCompletedListenerImpl;
    }

    /**
     * @return the handshakeCompletedListener
     */
    public HandshakeCompletedListenerImpl getHandshakeCompletedListener() {
        return (HandshakeCompletedListenerImpl) handshakeCompletedListener;
    }  
    
	/**
	 * @return the handshakeCompleted
	 */
	public boolean isHandshakeCompleted() {
		return handshakeCompleted;
	}

	/**
	 * @param handshakeCompleted the handshakeCompleted to set
	 */
	public void setHandshakeCompleted(boolean handshakeCompleted) {
		this.handshakeCompleted = handshakeCompleted;
	}
	
	@Override
	public SipStackImpl getSIPStack() {
		return (SipStackImpl) super.getSIPStack();
	}
}
