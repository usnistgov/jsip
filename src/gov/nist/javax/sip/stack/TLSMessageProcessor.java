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
/* This class is entirely derived from TCPMessageProcessor,
 *  by making some minor changes.
 *
 *               Daniel J. Martinez Manzano <dani@dif.um.es>
 * Acknowledgement: Jeff Keyser suggested that a
 * Stop mechanism be added to this. Niklas Uhrberg suggested that
 * a means to limit the number of simultaneous active connections
 * should be added. Mike Andrews suggested that the thread be
 * accessible so as to implement clean stop using Thread.join().
 *
 */

/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;

/**
 * Sit in a loop waiting for incoming tls connections and start a new thread to handle each new
 * connection. This is the active object that creates new TLS MessageChannels (one for each new
 * accept socket).
 * 
 * @version 1.2 $Revision: 1.29 $ $Date: 2010-12-02 22:04:13 $
 * 
 * @author M. Ranganathan <br/>
 * 
 */
public class TLSMessageProcessor extends ConnectionOrientedMessageProcessor implements Runnable {
	
	private static StackLogger logger = CommonLogger.getLogger(TLSMessageProcessor.class);
    
	/**
     * Constructor.
     * 
     * @param ipAddress -- inet address where I am listening.
     * @param sipStack SIPStack structure.
     * @param port port where this message processor listens.
     */
    protected TLSMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
        super(ipAddress, port, "tls",sipStack);    
    }

    /**
     * Start the processor.
     */
    public void start() throws IOException {
        Thread thread = new Thread(this);
        thread.setName("MessageProcessorThread-TLS-" + getIpAddress().getHostAddress() + '/' + getPort());
        // ISSUE 184
        thread.setPriority(sipStack.getThreadPriority());
        thread.setDaemon(true);

        this.sock = sipStack.getNetworkLayer().createSSLServerSocket(this.getPort(), 0,
                this.getIpAddress());
        if(sipStack.getClientAuth() == ClientAuthType.Want || sipStack.getClientAuth() == ClientAuthType.Default) {
            // we set it to true in Default case as well to keep backward compatibility and default behavior            
            ((SSLServerSocket) this.sock).setWantClientAuth(true);            
        } else {
            ((SSLServerSocket) this.sock).setWantClientAuth(false);
        }
        if(sipStack.getClientAuth() == ClientAuthType.Enabled) {
            ((SSLServerSocket) this.sock).setNeedClientAuth(true);            
        } else {
            ((SSLServerSocket) this.sock).setNeedClientAuth(false);
        }            
        ((SSLServerSocket) this.sock).setUseClientMode(false);
        String []enabledCiphers = ((SipStackImpl)sipStack).getEnabledCipherSuites();
        ((SSLServerSocket)sock).setEnabledProtocols(((SipStackImpl)sipStack).getEnabledProtocols());
        ((SSLServerSocket) this.sock).setEnabledCipherSuites(enabledCiphers);        
        if(sipStack.getClientAuth() == ClientAuthType.Want || sipStack.getClientAuth() == ClientAuthType.Default) {
            // we set it to true in Default case as well to keep backward compatibility and default behavior            
            ((SSLServerSocket) this.sock).setWantClientAuth(true);            
        } else {
            ((SSLServerSocket) this.sock).setWantClientAuth(false);
        }     

        if(logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("SSLServerSocket want client auth " + ((SSLServerSocket) this.sock).getWantClientAuth());
            logger.logDebug("SSLServerSocket need client auth " + ((SSLServerSocket) this.sock).getNeedClientAuth());
        }
        
        this.isRunning = true;
        thread.start();

    }

    /**
     * Run method for the thread that gets created for each accept socket.
     */
    public void run() {
        // Accept new connectins on our socket.
        while (this.isRunning) {
        	Socket newsock = null; 
            try {
            	 
                synchronized (this) {
                    // sipStack.maxConnections == -1 means we are
                    // willing to handle an "infinite" number of
                    // simultaneous connections (no resource limitation).
                    // This is the default behavior.
                    while (sipStack.maxConnections != -1
                            && this.nConnections >= sipStack.maxConnections) {
                        try {
                        	
                            this.wait();

                            if (!this.isRunning)
                                return;
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    this.nConnections++;
                }
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug(" waiting to accept new connection!");
                }
                
                newsock = sock.accept();
               
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug("Accepting new connection!");
                }

            } catch (SocketException ex) {
                if ( this.isRunning ) {
                  logger.logError(
                    "Fatal - SocketException occured while Accepting connection", ex);
                  	this.isRunning = false;
                  	break;
                }
            } catch (SSLException ex) {
                this.isRunning = false;
                logger.logError(
                        "Fatal - SSSLException occured while Accepting connection", ex);
                break;
            } catch (IOException ex) {
                // Problem accepting connection.
                logger.logError("Problem Accepting Connection", ex);
				continue;
            } catch (Exception ex) {
                logger.logError("Unexpected Exception!", ex);
                continue;
            }
            
            // Note that for an incoming message channel, the
            // thread is already running
            if(isRunning) {
	            try {
	            	// lyolik: even if SocketException is thrown (could be a result of bad handshake, 
	            	// it's not a reason to stop execution
		            TLSMessageChannel newChannel = new TLSMessageChannel(newsock, sipStack, this, "TLSMessageChannelThread-" + nConnections);
		            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
		                 logger.logDebug(Thread.currentThread() + " adding incoming channel " + newChannel.getKey());
		            // https://code.google.com/p/jain-sip/issues/detail?id=14 add it only if the handshake has been completed successfully
		            if(newChannel.isHandshakeCompleted()) {
		                incomingMessageChannels.put(newChannel.getKey(), newChannel);
		            }
	            } catch (Exception ex) {
	                logger.logError("A problem occured while Accepting connection", ex);
	            }
            }
        }
    }   

    /**
     * Stop the message processor. Feature suggested by Jeff Keyser.
     */
    public synchronized void stop() {
        if (!isRunning)
            return;

        isRunning = false;
        try {
            sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collection en = messageChannels.values();
        for (Iterator it = en.iterator(); it.hasNext();) {
            TLSMessageChannel next = (TLSMessageChannel) it.next();
            next.close();
        }
        for (Iterator incomingMCIterator = incomingMessageChannels.values().iterator(); incomingMCIterator
                .hasNext();) {
            TLSMessageChannel next = (TLSMessageChannel) incomingMCIterator.next();
            next.close();
        }
        this.notify();

    }

    public synchronized MessageChannel createMessageChannel(HostPort targetHostPort)
            throws IOException {
        String key = MessageChannel.getKey(targetHostPort, "TLS");
        if (messageChannels.get(key) != null) {
            return (TLSMessageChannel) this.messageChannels.get(key);
        } else {
            TLSMessageChannel retval = new TLSMessageChannel(targetHostPort.getInetAddress(),
                    targetHostPort.getPort(), sipStack, this);
            this.messageChannels.put(key, retval);
            retval.isCached = true;
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug("key " + key);
                logger.logDebug("Creating " + retval);
            }
            return retval;
        }
    }

    public synchronized MessageChannel createMessageChannel(InetAddress host, int port)
            throws IOException {
        try {
            String key = MessageChannel.getKey(host, port, "TLS");
            if (messageChannels.get(key) != null) {
                return (TLSMessageChannel) this.messageChannels.get(key);
            } else {
                TLSMessageChannel retval = new TLSMessageChannel(host, port, sipStack, this);
                this.messageChannels.put(key, retval);
                retval.isCached = true;
                if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                    logger.logDebug("key " + key);
                    logger.logDebug("Creating " + retval);
                }
                return retval;
            }
        } catch (UnknownHostException ex) {
            throw new IOException(ex.getMessage());
        }
    }    

    /**
     * Default target port for TLS
     */
    public int getDefaultTargetPort() {
        return 5061;
    }

    /**
     * TLS is a secure protocol.
     */
    public boolean isSecure() {
        return true;
    }
}

