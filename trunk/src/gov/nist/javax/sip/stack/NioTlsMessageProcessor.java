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
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.channels.SocketChannel;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class NioTlsMessageProcessor extends NioTcpMessageProcessor{

    private static StackLogger logger = CommonLogger.getLogger(NioTlsMessageProcessor.class);

    // Create a trust manager that does not validate certificate chains
    TrustManager[] trustAllCerts = new TrustManager[] { 
      new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
          return new X509Certificate[0]; 
        }
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug(
                        "checkClientTrusted : Not validating certs " + certs + " authType " + authType);
            }
        }
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug(
                        "checkServerTrusted : Not validating certs " + certs + " authType " + authType);
            }
        }
    }};
    
    SSLContext sslServerCtx;
    SSLContext sslClientCtx;

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
    	NioTlsMessageChannel retval = null;
    	try {
    		String key = MessageChannel.getKey(targetHostPort, "TLS");
			
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
        String key = MessageChannel.getKey(targetHost, port, "TLS");
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
        sslClientCtx = SSLContext.getInstance("TLS");
        
        if(sipStack.getClientAuth() == ClientAuthType.DisabledAll) {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug(
                        "ClientAuth " + sipStack.getClientAuth()  +  " bypassing all cert validations");
            }
        	sslServerCtx.init(sipStack.securityManagerProvider.getKeyManagers(false), trustAllCerts, null);
        	sslClientCtx.init(sipStack.securityManagerProvider.getKeyManagers(true), trustAllCerts, null);
        } else {
        	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug(
                        "ClientAuth " + sipStack.getClientAuth());
            }
        	 sslServerCtx.init(sipStack.securityManagerProvider.getKeyManagers(false), 
                     sipStack.securityManagerProvider.getTrustManagers(false),
                     null);
        	 sslClientCtx.init(sipStack.securityManagerProvider.getKeyManagers(true),
                     sipStack.securityManagerProvider.getTrustManagers(true),
                     null);

        }
    }

}
