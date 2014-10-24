/*
 * This software has been contributed by the author to the public domain.
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
import gov.nist.core.StackLogger;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.cert.Certificate;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

public class HandshakeCompletedListenerImpl implements HandshakeCompletedListener {
	private static StackLogger logger = CommonLogger.getLogger(HandshakeCompletedListenerImpl.class);          
	
    private HandshakeCompletedEvent handshakeCompletedEvent;
    private final Object eventWaitObject = new Object();

    private HandshakeWatchdog watchdog;
    private SIPTransactionStack sipStack;
    
    // Added for https://java.net/jira/browse/JSIP-483, NIO doesn't provide a HandshakeCompletedEvent 
    private Certificate[] peerCertificates;
    private Certificate[] localCertificates;
    private String cipherSuite;

    public HandshakeCompletedListenerImpl(TLSMessageChannel tlsMessageChannel, Socket socket) {
        tlsMessageChannel.setHandshakeCompletedListener(this);
        sipStack = tlsMessageChannel.getSIPStack();
        if(sipStack.getSslHandshakeTimeout() > 0) {
        	this.watchdog = new HandshakeWatchdog(socket);        	
        }
    }
    
    public HandshakeCompletedListenerImpl(NioTlsMessageChannel tlsMessageChannel, SocketChannel socket) {
        tlsMessageChannel.setHandshakeCompletedListener(this);
        sipStack = tlsMessageChannel.getSIPStack();
        if(sipStack.getSslHandshakeTimeout() > 0) {
        	this.watchdog = new HandshakeWatchdog(socket.socket());        	
        }
    }

    public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
        if (this.watchdog != null) {
            sipStack.getTimer().cancel(watchdog);
            this.watchdog = null;
        }
        this.handshakeCompletedEvent = handshakeCompletedEvent;
        synchronized (eventWaitObject) {
            eventWaitObject.notify();
        }
    }

    /**
     * Gets the event indicating that the SSL handshake has completed. The
     * method waits until the event has been obtained by the listener or a
     * timeout of 5 seconds has elapsed.
     * 
     * @return the handshakeCompletedEvent or null when the timeout elapsed
     */
    public HandshakeCompletedEvent getHandshakeCompletedEvent() {
        try {
            synchronized (eventWaitObject) {
                if (handshakeCompletedEvent == null)
                    eventWaitObject.wait(5000);
            }
        }
        catch (InterruptedException e) {
            // we don't care
        }
        return handshakeCompletedEvent;
    }

    public void startHandshakeWatchdog() {
        if (this.watchdog != null) {
        	logger.logInfo("starting watchdog for socket " + watchdog.socket + " on sslhandshake " + sipStack.getSslHandshakeTimeout());
        	sipStack.getTimer().schedule(watchdog, sipStack.getSslHandshakeTimeout());
        }
    }

    
    /**
	 * @return the peerCertificates
	 */
	public Certificate[] getPeerCertificates() {
		return peerCertificates;
	}

	/**
	 * @param peerCertificates the peerCertificates to set
	 */
	public void setPeerCertificates(Certificate[] peerCertificates) {
		this.peerCertificates = peerCertificates;
	}


	/**
	 * @return the cipherSuite
	 */
	public String getCipherSuite() {
		return cipherSuite;
	}

	/**
	 * @param cipherSuite the cipherSuite to set
	 */
	public void setCipherSuite(String cipherSuite) {
		this.cipherSuite = cipherSuite;
	}


	/**
	 * @return the localCertificates
	 */
	public Certificate[] getLocalCertificates() {
		return localCertificates;
	}

	/**
	 * @param localCertificates the localCertificates to set
	 */
	public void setLocalCertificates(Certificate[] localCertificates) {
		this.localCertificates = localCertificates;
	}


	class HandshakeWatchdog extends SIPStackTimerTask {
    	Socket  socket;
    	 
    	private HandshakeWatchdog(Socket socket) {
    		this.socket = socket;
    	}
    	
		@Override
		public void runTask() {
			logger.logInfo("closing socket " + socket + " on sslhandshaketimeout");
			 try {
                 socket.close();
             } catch (IOException ex) {
                 logger.logInfo("couldn't close socket on sslhandshaketimeout");
             }
			 logger.logInfo("socket closed " + socket + " on sslhandshaketimeout");
		}
    	
    }


}
