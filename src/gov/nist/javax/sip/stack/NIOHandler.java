/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 United States Code Section 105, works of NIST
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
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.CommonLogger;
import gov.nist.core.LogLevels;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.javax.sip.SipStackImpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/*
 * TLS support Added by Daniel J.Martinez Manzano <dani@dif.um.es>
 *
 */

/**
 * Low level Input output to a socket. Caches TCP connections and takes care of
 * re-connecting to the remote party if the other end drops the connection
 *
 * @version 1.2
 *
 * @author M. Ranganathan <br/>
 *
 *
 */

public class NIOHandler {
	
	private static StackLogger logger = CommonLogger.getLogger(NIOHandler.class);

    private SipStackImpl sipStack;
    
    private NioTcpMessageProcessor messageProcessor;

    private static final String TCP = "tcp";

    // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
    private static final String TLS = "tls";
    
    Timer timer = new Timer();

    // A cache of client sockets that can be re-used for
    // sending tcp messages.
    private final ConcurrentHashMap<String, SocketChannel> socketTable = new ConcurrentHashMap<String, SocketChannel>();

    
    KeyedSemaphore keyedSemaphore = new KeyedSemaphore();
    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + ":" + port;

    }

    protected static String makeKey(String addr, int port) {
        return addr + ":" + port;
    }

    protected NIOHandler(SIPTransactionStack sipStack, NioTcpMessageProcessor messageProcessor) {
        this.sipStack = (SipStackImpl) sipStack;
        this.messageProcessor = messageProcessor;
        if(sipStack.nioSocketMaxIdleTime > 0) 
        	timer.scheduleAtFixedRate(new SocketTimeoutAuditor(), 20000, 20000);
    }

    protected void putSocket(String key, SocketChannel sock) {
    	synchronized(socketTable) {
    		if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
    			logger.logDebug("adding socket for key " + key);
    		}
    		socketTable.put(key, sock);
    	}
    }

    protected SocketChannel getSocket(String key) {
    	// no need to synchrnize here
        return (SocketChannel) socketTable.get(key);

    }

    protected void removeSocket(String key) {
    	synchronized(socketTable) {
    		socketTable.remove(key);
    		keyedSemaphore.remove(key);
    		if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
    			logger.logDebug("removed Socket and Semaphore for key " + key);
    		}
    	}
    }

    /**
     * A private function to write things out. This needs to be synchronized as
     * writes can occur from multiple threads. We write in chunks to allow the
     * other side to synchronize for large sized writes.
     */
    private void writeChunks(SocketChannel channel, byte[] bytes, int length)
            throws IOException {
        // Chunk size is 16K - this hack is for large
        // writes over slow connections.
        synchronized (channel) {
            // outputStream.write(bytes,0,length);
        	byte[] buff = new byte[length];
        	System.arraycopy(bytes, 0, buff, 0, length);
        	messageProcessor.send(channel, bytes);
        }
    }


    /**
     * Send an array of bytes.
     *
     * @param receiverAddress
     *            -- inet address
     * @param contactPort
     *            -- port to connect to.
     * @param transport
     *            -- tcp or udp.
     * @param isClient
     *            -- retry to connect if the other end closed connection
     * @throws IOException
     *             -- if there is an IO exception sending message.
     */

    public SocketChannel sendBytes(InetAddress senderAddress,
            InetAddress receiverAddress, int contactPort, String transport,
            byte[] bytes, boolean isClient, NioTcpMessageChannel messageChannel)
            throws IOException {
        int retry_count = 0;
        int max_retry = isClient ? 2 : 1;
        // Server uses TCP transport. TCP client sockets are cached
        int length = bytes.length;
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug(
                    "sendBytes " + transport + " inAddr "
                            + receiverAddress.getHostAddress() + " port = "
                            + contactPort + " length = " + length + " isClient " + isClient );

        }
        if (logger.isLoggingEnabled(LogLevels.TRACE_INFO)
                && sipStack.isLogStackTraceOnMessageSend()) {
            logger.logStackTrace(StackLogger.TRACE_INFO);
        }
        if (transport.compareToIgnoreCase(TCP) == 0 || transport.compareToIgnoreCase(TLS) == 0) {
            String key = makeKey(receiverAddress, contactPort);
            // This should be in a synchronized block ( reported by
            // Jayashenkhar ( lucent ).

            SocketChannel clientSock = null;
            keyedSemaphore.enterIOCriticalSection(key);

            try {
                clientSock = getSocket(key);
                while (retry_count < max_retry) {
                    if (clientSock == null) {
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            logger.logDebug(
                                    "inaddr = " + receiverAddress);
                            logger.logDebug(
                                    "port = " + contactPort);
                        }
                        // note that the IP Address for stack may not be
                        // assigned.
                        // sender address is the address of the listening point.
                        // in version 1.1 all listening points have the same IP
                        // address (i.e. that of the stack). In version 1.2
                        // the IP address is on a per listening point basis.
                        try {
                        	clientSock = messageProcessor.blockingConnect(new InetSocketAddress(receiverAddress, contactPort), 10000);
                        	//sipStack.getNetworkLayer().createSocket(
                        	//		receiverAddress, contactPort, senderAddress); TODO: sender address needed
                        } catch (SocketException e) { // We must catch the socket timeout exceptions here, any SocketException not just ConnectException
                        	logger.logError("Problem connecting " +
                        			receiverAddress + " " + contactPort + " " + senderAddress + " for message " + new String(bytes, "UTF-8"));
                        	// new connection is bad.
                        	// remove from our table the socket and its semaphore
                        	removeSocket(key);
                        	throw new SocketException(e.getClass() + " " + e.getMessage() + " " + e.getCause() + " Problem connecting " +
                        			receiverAddress + " " + contactPort + " " + senderAddress + " for message " + new String(bytes, "UTF-8"));
                        }
                        writeChunks(clientSock, bytes, length);
                        putSocket(key, clientSock);
                        break;
                    } else {
                        try {
                            writeChunks(clientSock, bytes, length);
                            break;
                        } catch (IOException ex) {
                            if (logger
                                    .isLoggingEnabled(LogWriter.TRACE_WARN))
                                logger.logWarning(
                                        "IOException occured retryCount "
                                                + retry_count);                            
                            try {
                                clientSock.close();
                            } catch (Exception e) {
                            }
                            clientSock = null;
                            retry_count++;
                            // This is a server tx trying to send a response.
                            if ( !isClient ) {
   								removeSocket(key);
                                throw ex;
                            }
                            if(retry_count >= max_retry) {
								// old connection is bad.
								// remove from our table the socket and its semaphore
								removeSocket(key);
							} else {
								// don't remove the semaphore on retry
								socketTable.remove(key);
							}
                        }
                    }
                }
            } catch (IOException ex) {
                if (logger.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
                    logger.logError(
                            "Problem sending: sendBytes " + transport
                                    + " inAddr "
                                    + receiverAddress.getHostAddress()
                                    + " port = " + contactPort +
                            " remoteHost " + messageChannel.getPeerAddress() +
                            " remotePort " + messageChannel.getPeerPort() +
                            " peerPacketPort "
                                    + messageChannel.getPeerPacketSourcePort() + " isClient " + isClient);
                }

                removeSocket(key);

                /*
                 * For TCP responses, the transmission of responses is
                 * controlled by RFC 3261, section 18.2.2 :
                 *
                 * o If the "sent-protocol" is a reliable transport protocol
                 * such as TCP or SCTP, or TLS over those, the response MUST be
                 * sent using the existing connection to the source of the
                 * original request that created the transaction, if that
                 * connection is still open. This requires the server transport
                 * to maintain an association between server transactions and
                 * transport connections. If that connection is no longer open,
                 * the server SHOULD open a connection to the IP address in the
                 * "received" parameter, if present, using the port in the
                 * "sent-by" value, or the default port for that transport, if
                 * no port is specified. If that connection attempt fails, the
                 * server SHOULD use the procedures in [4] for servers in order
                 * to determine the IP address and port to open the connection
                 * and send the response to.
                 */
                if (!isClient) {
                    receiverAddress = InetAddress.getByName(messageChannel
                            .getViaHost());
                    contactPort = messageChannel.getViaPort();
                    if (contactPort == -1)
                        contactPort = 5060;

                    key = makeKey(receiverAddress, messageChannel
                            .getViaPort());
                    clientSock = this.getSocket(key);
                    if (clientSock == null) {
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            logger.logDebug(
                                    "inaddr = " + receiverAddress +
                                    " port = " + contactPort);
                        }
						clientSock = messageProcessor.blockingConnect(new InetSocketAddress(receiverAddress, contactPort), 10000);
						
                        
                        writeChunks(clientSock, bytes, length);
                        putSocket(key, clientSock);
                        return clientSock;
                    } else {
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            logger.logDebug(
                                    "sending to " + key );
                        }
                        try {
                            writeChunks(clientSock, bytes, length);
                            return clientSock;
                        } catch (IOException ioe) {
                            if (logger
                                    .isLoggingEnabled(LogWriter.TRACE_ERROR))
                                logger.logError(
                                        "IOException occured  ", ioe);
                            if (logger
                                    .isLoggingEnabled(LogWriter.TRACE_DEBUG))
                                logger.logDebug(
                                        "Removing and Closing socket");
                            // old connection is bad.
                            // remove from our table.
                            removeSocket(key);
                            try {
                                clientSock.close();
                            } catch (Exception e) {
                            }
                            clientSock = null;
                            throw ioe;
                        }
                    }
                } else {
                    logger.logError("IOException occured at " , ex);
                    throw ex;
                }
            } finally {
                keyedSemaphore.leaveIOCriticalSection(key);
            }

            if (clientSock == null) {

                if (logger.isLoggingEnabled(LogWriter.TRACE_ERROR)) {
                    logger.logError(
                            this.socketTable.toString());
                    logger.logError(
                            "Could not connect to " + receiverAddress + ":"
                                    + contactPort);
                }

                throw new IOException("Could not connect to " + receiverAddress
                        + ":" + contactPort);
            } else {
                return clientSock;
            }

            // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
            // Copied and modified from the former section for TCP
        }
        return null;

    }

    /**
     * Close all the cached connections.
     */
    public void closeAll() {
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
            logger
                    .logDebug(
                            "Closing " + socketTable.size()
                                    + " sockets from IOHandler");
        
        for (Enumeration<SocketChannel> values = socketTable.elements(); values
                .hasMoreElements();) {
        	SocketChannel s = (SocketChannel) values.nextElement();
            try {
                s.close();
            } catch (IOException ex) {
            }
        }

    }
    
    public void stop() {
    	try {
        	timer.cancel();
        	timer.purge();
        	synchronized(socketTable) {
        		HashSet<String> keysToRemove = new HashSet<String>();
        		for(String key : socketTable.keySet()) {
        			try {
        				SocketChannel socketChannel = socketTable.get(key);
        				NioTcpMessageChannel messageChannel = NioTcpMessageChannel.getMessageChannel(socketChannel);
        				if(messageChannel == null) {
        					keysToRemove.add(key);
        				} else {
        					keysToRemove.add(key);
        					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        						logger.logDebug("stop() : Will remove socket " + key + " lastActivity=" + messageChannel.getLastActivityTimestamp() + " current= " + System.currentTimeMillis());
        					NioTcpMessageChannel.removeMessageChannel(socketChannel);
        					messageChannel.close();
        				}
        			} catch (Exception anything) {

        			}
        		}
        		for(String key : keysToRemove) {
        			socketTable.remove(key);
        			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
        				logger.logDebug("stop() : Removed socket " + key);
        		}
        	}
        } catch (Exception e) {
        	
        }
    }
    
    
    // TODO: FIXME: It is absolutely essential to have this method synchrnized based on the makeKey(addr, port),
    // it is not needed to sync per class instance like it's done now, this is just temporary fix
    // UAC case with rapid outbound socket creation might end up overwriting the assigned socket
    public synchronized SocketChannel createOrReuseSocket(InetAddress inetAddress, int port) throws IOException {
    	SocketChannel channel = getSocket(NIOHandler.makeKey(inetAddress, port));
    	if(channel != null && !channel.isConnected()) {
    		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
				logger.logDebug("Channel disconnected " + channel);
    		logger.logError("HERE " + channel);
    		channel = null;
    	}
		if(channel == null) { // this is where the threads will race
			SocketAddress sockAddr = new InetSocketAddress(inetAddress, port);
			channel = messageProcessor.blockingConnect((InetSocketAddress) sockAddr, 10000);
			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    								logger.logDebug("create channel = " + channel + "  " + inetAddress + " " + port);
			if(channel != null && channel.isConnected()) {
				putSocket(NIOHandler.makeKey(inetAddress, port), channel);
				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    								logger.logDebug("channel cached channel = " + channel);
			}
		} 
		return channel;
    }
    
    private class SocketTimeoutAuditor extends TimerTask {
    	public void run() {
    		synchronized(socketTable) {
    			try {
    				HashSet<String> keysToRemove = new HashSet<String>();
    				for(String key : socketTable.keySet()) {
    					SocketChannel socketChannel = socketTable.get(key);
    					NioTcpMessageChannel messageChannel = NioTcpMessageChannel.getMessageChannel(socketChannel);
    					if(messageChannel == null) {
    						keysToRemove.add(key);
    					} else {
    						if(System.currentTimeMillis() - messageChannel.getLastActivityTimestamp() > sipStack.nioSocketMaxIdleTime) {
    							keysToRemove.add(key);
    							if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    								logger.logDebug("Will remove socket " + key + " lastActivity=" + messageChannel.getLastActivityTimestamp() + " current= " + System.currentTimeMillis());
    							NioTcpMessageChannel.removeMessageChannel(socketChannel);
    							messageChannel.close();
    						}
    					}
    				}
    				for(String key : keysToRemove) {
    					socketTable.remove(key);
    					if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    						logger.logDebug("Removed socket " + key);
    				}
    			} catch (Exception anything) {

    			}
    		}
    	}
    }

}
