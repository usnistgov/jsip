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
	
	@Override
	protected void sendMessage(byte[] msg, final boolean isClient) throws IOException {
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
		super.sendMessage(msg, false);
	}
	
	@Override
	public void sendMessage(byte message[], final InetAddress receiverAddress,
			final int receiverPort, final boolean retry) throws IOException {
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

}
