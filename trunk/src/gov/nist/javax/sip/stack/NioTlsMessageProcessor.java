package gov.nist.javax.sip.stack;


import gov.nist.core.CommonLogger;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.security.KeyStore;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class NioTlsMessageProcessor extends NioTcpMessageProcessor{

    private static StackLogger logger = CommonLogger.getLogger(NioTlsMessageProcessor.class);
    SSLContext sslCtx;
	public NioTlsMessageProcessor(InetAddress ipAddress,
			SIPTransactionStack sipStack, int port) {
		super(ipAddress, sipStack, port);
		transport = "TLS";
		try {
			init();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public NioTcpMessageChannel createMessageChannel(NioTcpMessageProcessor nioTcpMessageProcessor, SocketChannel client) throws IOException {
    	return NioTlsMessageChannel.create(NioTlsMessageProcessor.this, client);
    }
	
    @Override
    public MessageChannel createMessageChannel(HostPort targetHostPort) throws IOException {
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    		logger.logDebug("NioTlsMessageProcessor::createMessageChannel: " + targetHostPort);
    	}
    	try {
    		String key = MessageChannel.getKey(targetHostPort, "TLS");
    		if (messageChannels.get(key) != null) {
    			return (NioTlsMessageChannel) this.messageChannels.get(key);
    		} else {
    			NioTlsMessageChannel retval = new NioTlsMessageChannel(targetHostPort.getInetAddress(),
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
    			logger.logDebug("MessageChannel::createMessageChannel - exit");
    		}
    	}
    }

    @Override
    public MessageChannel createMessageChannel(InetAddress targetHost, int port) throws IOException {
        String key = MessageChannel.getKey(targetHost, port, "TLS");
        if (messageChannels.get(key) != null) {
            return (NioTlsMessageChannel) this.messageChannels.get(key);
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
		String passphraseString = (String) ((SipStackImpl)super.sipStack).getConfigurationProperties().getProperty("javax.net.ssl.keyStorePassword");
        String keyStoreType = (String) ((SipStackImpl)super.sipStack).getConfigurationProperties().getProperty("javax.net.ssl.keyStoreType");
        if(passphraseString == null) passphraseString = System.getProperty("javax.net.ssl.keyStorePassword");
        if(keyStoreType == null) keyStoreType = System.getProperty("javax.net.ssl.keyStoreType");
        
        KeyStore ks = KeyStore.getInstance(keyStoreType);
        KeyStore ts = KeyStore.getInstance(keyStoreType);

        char[] passphrase = passphraseString.toCharArray();

        String keyStoreFilename = (String) ((SipStackImpl)super.sipStack).getConfigurationProperties().getProperty("javax.net.ssl.keyStore");
        String trustStoreFilename = (String) ((SipStackImpl)super.sipStack).getConfigurationProperties().getProperty("javax.net.ssl.trustStore");
        
        if(keyStoreFilename == null) keyStoreFilename = System.getProperty("javax.net.ssl.keyStore");
        if(trustStoreFilename == null) trustStoreFilename = System.getProperty("javax.net.ssl.trustStore");

        if(keyStoreFilename != null && trustStoreFilename != null) {
        	ks.load(new FileInputStream(new File(keyStoreFilename)), passphrase);
        	ts.load(new FileInputStream(new File(trustStoreFilename)), passphrase);
          	KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        	kmf.init(ks, passphrase);

        	TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        	tmf.init(ts);

        	sslCtx = SSLContext.getInstance("TLS");

        	sslCtx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        } else {
        	logger.logWarning("TLS key and trust stores are not configured. javax.net.ssl.keyStore="
        			+keyStoreFilename + " javax.net.ssl.trustStore=" + 
        			trustStoreFilename);

        }

	}

}
