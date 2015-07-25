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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Low level Input output to a socket. Caches TCP connections and takes care of
 * re-connecting to the remote party if the other end drops the connection
 *
 * @version 1.2
 *
 * @author Vladimir Ralev <br/>
 *
 *
 */

public class NIOHandler {
	
	private static StackLogger logger = CommonLogger.getLogger(NIOHandler.class);

    private SipStackImpl sipStack;
    
    private NioTcpMessageProcessor messageProcessor;
    
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
    
    protected void removeSocket(SocketChannel channel) {
    	if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
			logger.logDebug("Trying to remove cached socketChannel without key"
					+ this + " socketChannel = " + channel);
		}
    	LinkedList<String> keys = new LinkedList<String>();
    	synchronized(socketTable) {
    		Set<Entry<String, SocketChannel>> e = socketTable.entrySet();
    		for(Entry<String, SocketChannel> entry : e ) {
    			SocketChannel sc = entry.getValue();
    			if(sc.equals(channel)) {
    				keys.add(entry.getKey());
    			}
    		}
    		for(String key : keys) {
    			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
    				logger.logDebug("Removing cached socketChannel without key"
    						+ this + " socketChannel = " + channel + " key = " + key);
    			}
    			removeSocket(key);
    		}
    	}
    }

    /**
     * A private function to write things out. This needs to be synchronized as
     * writes can occur from multiple threads. We write in chunks to allow the
     * other side to synchronize for large sized writes.
     */
    private void writeChunks(SocketChannel channel, byte[] bytes, int length) {
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
        
        String key = makeKey(receiverAddress, contactPort);
        // This should be in a synchronized block ( reported by
        // Jayashenkhar ( lucent ).

        SocketChannel clientSock = null;
        keyedSemaphore.enterIOCriticalSection(key);

        boolean newSocket = false;
        try {
        	clientSock = getSocket(key);
        	while (retry_count < max_retry) {
        		if(clientSock != null && (!clientSock.isConnected() || !clientSock.isOpen()) ) {
        			removeSocket(key);
        			clientSock = null;
        			newSocket = true;
        		}
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
        				if(messageChannel instanceof NioTlsMessageChannel) {
        					// Added for https://java.net/jira/browse/JSIP-483 
	        				HandshakeCompletedListenerImpl listner = new HandshakeCompletedListenerImpl((NioTlsMessageChannel)messageChannel, clientSock);
	                        ((NioTlsMessageChannel) messageChannel)
	                                .setHandshakeCompletedListener(listner);
        				}
        				newSocket = true;
        				//sipStack.getNetworkLayer().createSocket(
        				//		receiverAddress, contactPort, senderAddress); TODO: sender address needed
        			} catch (SocketException e) { // We must catch the socket timeout exceptions here, any SocketException not just ConnectException
        				logger.logError("Problem connecting " +
        						receiverAddress + " " + contactPort + " " + senderAddress + " for message " + (messageChannel.isSecure()?"<<<ENCRYPTED MESSAGE>>>":new String(bytes, "UTF-8")));
        				// new connection is bad.
        				// remove from our table the socket and its semaphore
        				removeSocket(key);
        				throw new SocketException(e.getClass() + " " + e.getMessage() + " " + e.getCause() + " Problem connecting " +
        						receiverAddress + " " + contactPort + " " + senderAddress + " for message " + new String(bytes, "UTF-8"));
        			}
        			putSocket(key, clientSock);
        			break;
        		} else {
        			break;
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
        				.peerAddressAdvertisedInHeaders);
        		contactPort = messageChannel.peerPortAdvertisedInHeaders;
        		if (contactPort <= 0)
        			contactPort = 5060;

        		key = makeKey(receiverAddress, contactPort);
        		clientSock = this.getSocket(key);
        		if (clientSock == null || !clientSock.isConnected() || !clientSock.isOpen()) {
        			if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        				logger.logDebug(
        						"inaddr = " + receiverAddress +
        						" port = " + contactPort);
        			}
        			clientSock = messageProcessor.blockingConnect(new InetSocketAddress(receiverAddress, contactPort), 10000);
        			newSocket = true;
        			messageChannel.peerPort = contactPort;
        			putSocket(key, clientSock);
        		} 

        		if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
        			logger.logDebug(
        					"sending to " + key );
        		}


        	} else {
        		logger.logError("IOException occured at " , ex);
        		throw ex;
        	}

        	return clientSock;
        } finally {
        	try {
        		if(clientSock != null) {
        			if(newSocket && messageChannel instanceof NioTlsMessageChannel) {
        				//We dont write data when using TLS, the new socket needs to handshake first
        			} else {
        				writeChunks(clientSock, bytes, length);
        			}
        		}

        	} finally {
        		keyedSemaphore.leaveIOCriticalSection(key);
        	}
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
        	// Reworked the method for https://java.net/jira/browse/JSIP-471
			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
				logger.logDebug("keys to check for inactivity removal " + NioTcpMessageChannel.channelMap.keySet());
				logger.logDebug("existing socket in NIOHandler " + socketTable.keySet());
			}
			Iterator<Entry<SocketChannel, NioTcpMessageChannel>> entriesIterator = NioTcpMessageChannel.channelMap.entrySet().iterator();
			while(entriesIterator.hasNext()) {
				Entry<SocketChannel, NioTcpMessageChannel> entry = entriesIterator.next();
				SocketChannel socketChannel = entry.getKey();
				NioTcpMessageChannel messageChannel = entry.getValue();
				
				if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
					logger.logDebug("stop() : Removing socket " + messageChannel.key 
							+ " socketChannel = " + socketChannel);
				}
				messageChannel.close();
				entriesIterator = NioTcpMessageChannel.channelMap.entrySet().iterator();
			}
        } catch (Exception e) {
        	
        }
    }
    
    public SocketChannel createOrReuseSocket(InetAddress inetAddress, int port) throws IOException {
    	String key = NIOHandler.makeKey(inetAddress, port);
    	SocketChannel channel = null;
    	keyedSemaphore.enterIOCriticalSection(key);
    	try {
    		channel = getSocket(key);
    		if(channel != null && (!channel.isConnected() || !channel.isOpen())) {
    			if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
    				logger.logDebug("Channel disconnected " + channel);
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
    	} finally {
    		keyedSemaphore.leaveIOCriticalSection(key);
    		if(logger.isLoggingEnabled(LogWriter.TRACE_DEBUG))
				logger.logDebug("Returning socket " + key + " channel = " + channel);
    	}
    }
}
