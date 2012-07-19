package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.stack.SSLStateMachine.MessageSendCallback;

import javax.net.ssl.SSLSession;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;

public class NioTlsMessageChannel extends NioTcpMessageChannel{

	private static StackLogger logger = CommonLogger
			.getLogger(NioTlsMessageChannel.class);
	
	SSLStateMachine sslStateMachine;

	private ByteBuffer appFrameBuffer;
	private ByteBuffer encryptedFrameBuffer;
	
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
        
		sslStateMachine = new SSLStateMachine( 
				((NioTlsMessageProcessor)messageProcessor).sslCtx.createSSLEngine(), this);

        sslStateMachine.sslEngine.setUseClientMode(clientMode);
        String auth = ((SipStackImpl)super.sipStack).
        		getConfigurationProperties().getProperty("gov.nist.javax.sip.TLS_CLIENT_AUTH_TYPE");
        if(auth == null) {
        	auth = "Enabled";
        }
        if(auth.equals("Disabled")) {
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

        String clientProtocols = ((SipStackImpl)super.sipStack)
        		.getConfigurationProperties().getProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS");
        if(clientProtocols != null) {
        	sslStateMachine.sslEngine.setEnabledProtocols(clientProtocols.split(","));
        }

	}
	
	public ByteBuffer prepareEncryptedDataBuffer() {
		encryptedFrameBuffer.clear();
		return encryptedFrameBuffer;
	}
	
	public ByteBuffer prepareAppDataBuffer() {
		appFrameBuffer.clear();
		return appFrameBuffer;
	}
	
	public static class SSLReconnectedException extends IOException {
		private static final long serialVersionUID = 1L;}
	
	@Override
	protected void sendMessage(final byte[] msg, final boolean isClient) throws IOException {
		lastMessage = new byte[msg.length];
		for(int q=0;q<msg.length;q++) lastMessage[q] = msg[q];
		String s = new String(lastMessage);
		//logger.logError(">>>>>>>>>>>>>>FFF>>>>>" + s);
		ByteBuffer b = ByteBuffer.wrap(msg);
		try {
			sslStateMachine.wrap(b, encryptedFrameBuffer, new MessageSendCallback() {

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
	private byte[] lastMessage = null;
	@Override
	public void sendMessage(final byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
		lastMessage = new byte[message.length];
		for(int q=0;q<message.length;q++) {

			lastMessage[q] = message[q];
			if(message[q]<0) {
				int r = 0 ;
				r++;r=r;
			}
		}
		String s = new String(lastMessage); 

		//logger.logError(">>>>>>>>>>>>>>FFF>>>>>" + s);
		
		ByteBuffer b = ByteBuffer.wrap(message);
		try {
			sslStateMachine.wrap(b, encryptedFrameBuffer, new MessageSendCallback() {
				
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
	 private void createBuffers() {

	        SSLSession session = sslStateMachine.sslEngine.getSession();
	        int appBufferMax = session.getApplicationBufferSize();
	        int netBufferMax = session.getPacketBufferSize();
	        
	        if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
	        	logger.logDebug("appBufferMax=" + appBufferMax + " netBufferMax=" + netBufferMax);
	        }
	        appFrameBuffer = ByteBuffer.allocateDirect(2*appBufferMax + 50);

	        encryptedFrameBuffer = ByteBuffer.allocateDirect(netBufferMax);
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
	public void onNewSocket() {
		super.onNewSocket();
		logger.logError("NEW SOCKET", new RuntimeException());
		try {
			init(true);
			createBuffers();
			String s = new String(lastMessage, "UTF-8");
			sendMessage(lastMessage, false);
		} catch (Exception e) {
			logger.logError("Cant reinit", e);
		}
	}
	
	@Override
	public boolean isSecure() {
		return true;
	}

}
