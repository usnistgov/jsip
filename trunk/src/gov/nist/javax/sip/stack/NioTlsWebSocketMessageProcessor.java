package gov.nist.javax.sip.stack;


import gov.nist.core.CommonLogger;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import javax.net.ssl.SSLContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;

public class NioTlsWebSocketMessageProcessor extends NioWebSocketMessageProcessor {

    private static StackLogger logger = CommonLogger.getLogger(NioTlsWebSocketMessageProcessor.class);

    SSLContext sslServerCtx;
    SSLContext sslClientCtx;

	public NioTlsWebSocketMessageProcessor(InetAddress ipAddress,
			SIPTransactionStack sipStack, int port) {
		super(ipAddress, sipStack, port);
		transport = "WSS";
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public NioTcpMessageChannel createMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor, SocketChannel client) throws IOException {
    	return NioTlsMessageChannel.create(NioTlsWebSocketMessageProcessor.this, client);
    }
	
    @Override
    public MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    		logger.logDebug("NioTlsWebSocketMessageProcessor::createMessageChannel: " + targetHostPort);
    	}
    	NioTlsMessageChannel retval = null;
    	try {
    		String key = MessageChannel.getKey(targetHostPort, "WSS");
			
    		if (messageChannels.get(key) != null) {
    			retval = (NioTlsMessageChannel) this.messageChannels.get(key);
    			return retval;
    		} else {
    			retval = new NioTlsMessageChannel(targetHostPort.getInetAddress(),
    					targetHostPort.getPort(), sipStack, this);
    			
    		//	retval.getSocketChannel().register(selector, SelectionKey.OP_READ);
    			synchronized(messageChannels) {
    				this.messageChannels.put(key, retval);
    			}
    			retval.isCached = true;
    			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    				logger.logDebug("key " + key);
    				logger.logDebug("Creating " + retval);
    			}
    			selector.wakeup();
    			return retval;

    		}
    	} finally {
    		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    			logger.logDebug("MessageChannel::createMessageChannel - exit " + retval);
    		}
    	}
    }

    @Override
    public MessageChannel createMessageChannel(InetAddress targetHost, int port) throws IOException {
        String key = MessageChannel.getKey(targetHost, port, "WSS");
        if (messageChannels.get(key) != null) {
            return this.messageChannels.get(key);
        } else {
            NioTlsMessageChannel retval = new NioTlsMessageChannel(targetHost, port, sipStack, this);
            
            selector.wakeup();
 //           retval.getSocketChannel().register(selector, SelectionKey.OP_READ);
            this.messageChannels.put(key, retval);
            retval.isCached = true;
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("key " + key);
                logger.logDebug("Creating " + retval);
            }
            return retval;
        }

    }
	public void init() throws Exception, CertificateException, FileNotFoundException, IOException {
		if(sipStack.securityManagerProvider.getKeyManagers(false) == null ||
				sipStack.securityManagerProvider.getTrustManagers(false) == null ||
                sipStack.securityManagerProvider.getTrustManagers(true) == null) {
			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("TLS initialization failed due to NULL security config");
            }
			return; // The settings 
		}
			
        sslServerCtx = SSLContext.getInstance("TLS");
        sslServerCtx.init(sipStack.securityManagerProvider.getKeyManagers(false), 
                sipStack.securityManagerProvider.getTrustManagers(false),
                null);

        sslClientCtx = SSLContext.getInstance("TLS");
        sslClientCtx.init(sipStack.securityManagerProvider.getKeyManagers(true),
                sipStack.securityManagerProvider.getTrustManagers(true),
                null);

    }

}
