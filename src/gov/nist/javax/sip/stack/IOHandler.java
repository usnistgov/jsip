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

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

public class IOHandler {
	
	private static StackLogger logger = CommonLogger.getLogger(IOHandler.class);

    private SipStackImpl sipStack;

    private static final String TCP = "tcp";

    // Added by Daniel J. Martinez Manzano <dani@dif.um.es>
    private static final String TLS = "tls";

    // A cache of client sockets that can be re-used for
    // sending tcp messages.
    private final ConcurrentHashMap<String, Socket> socketTable = new ConcurrentHashMap<String, Socket>();

    private final ConcurrentHashMap<String, Semaphore> socketCreationMap = new ConcurrentHashMap<String, Semaphore>();

    // private Semaphore ioSemaphore = new Semaphore(1);

    protected static String makeKey(InetAddress addr, int port) {
        return addr.getHostAddress() + ":" + port;

    }

    protected static String makeKey(String addr, int port) {
        return addr + ":" + port;
    }

    protected IOHandler(SIPTransactionStack sipStack) {
        this.sipStack = (SipStackImpl) sipStack;
    }

    protected void putSocket(String key, Socket sock) {
    	if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("adding socket for key " + key);
        }
        socketTable.put(key, sock);
    }

    protected Socket getSocket(String key) {
        return (Socket) socketTable.get(key);

    }

    protected void removeSocket(String key) {
        socketTable.remove(key);
        if ( socketCreationMap.get(key) != null ) {
        	socketCreationMap.get(key).release();
        	socketCreationMap.remove(key);
        }
        if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
            logger.logDebug("removed Socket and Semaphore for key " + key);
        }
    }

    /**
     * A private function to write things out. This needs to be synchronized as
     * writes can occur from multiple threads. We write in chunks to allow the
     * other side to synchronize for large sized writes.
     */
    private void writeChunks(OutputStream outputStream, byte[] bytes, int length)
            throws IOException {
        // Chunk size is 16K - this hack is for large
        // writes over slow connections.
        synchronized (outputStream) {
            // outputStream.write(bytes,0,length);
            int chunksize = 8 * 1024;
            for (int p = 0; p < length; p += chunksize) {
                int chunk = p + chunksize < length ? chunksize : length - p;
                outputStream.write(bytes, p, chunk);
            }
        }
        outputStream.flush();
    }

    /**
     * Creates and binds, if necessary, a socket connected to the specified
     * destination address and port and then returns its local address.
     *
     * @param dst
     *            the destination address that the socket would need to connect
     *            to.
     * @param dstPort
     *            the port number that the connection would be established with.
     * @param localAddress
     *            the address that we would like to bind on (null for the "any"
     *            address).
     * @param localPort
     *            the port that we'd like our socket to bind to (0 for a random
     *            port).
     *
     * @return the SocketAddress that this handler would use when connecting to
     *         the specified destination address and port.
     *
     * @throws IOException if we fail binding the socket
     */
    public SocketAddress getLocalAddressForTcpDst(InetAddress dst, int dstPort,
            InetAddress localAddress, int localPort) throws IOException {
        String key = makeKey(dst, dstPort);

        Socket clientSock = getSocket(key);

        if (clientSock == null) {
            clientSock = sipStack.getNetworkLayer().createSocket(dst, dstPort,
                    localAddress, localPort);
            putSocket(key, clientSock);
        }

        return clientSock.getLocalSocketAddress();

    }

    /**
     * Creates and binds, if necessary, a socket connected to the specified
     * destination address and port and then returns its local address.
     *
     * @param dst the destination address that the socket would need to connect
     * to.
     * @param dstPort the port number that the connection would be established
     * with.
     * @param localAddress the address that we would like to bind on (null for
     * the "any" address).
     *
     * @param channel the message channel that will be servicing the socket
     *
     * @return the SocketAddress that this handler would use when connecting to
     * the specified destination address and port.
     *
     * @throws IOException if we fail binding the socket
     */
    public SocketAddress getLocalAddressForTlsDst(InetAddress dst, int dstPort,
             InetAddress localAddress, TLSMessageChannel channel)
             throws IOException {
        String key = makeKey(dst, dstPort);

        Socket clientSock = getSocket(key);

        if (clientSock == null) {

            clientSock = sipStack.getNetworkLayer()
                .createSSLSocket(dst, dstPort, localAddress);

            SSLSocket sslsock = (SSLSocket) clientSock;

            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                logger.logDebug(
                        "inaddr = " + dst);
                logger.logDebug(
                        "port = " + dstPort);
            }

            HandshakeCompletedListenerImpl listner
                    = new HandshakeCompletedListenerImpl(channel, sslsock);

            channel.setHandshakeCompletedListener(listner);
            sslsock.addHandshakeCompletedListener(listner);
            sslsock.setEnabledProtocols(sipStack.getEnabledProtocols());
            sslsock.setEnabledCipherSuites(sipStack.getEnabledCipherSuites());

            listner.startHandshakeWatchdog();
            sslsock.startHandshake();
            channel.setHandshakeCompleted(true);
            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        "Handshake passed");
            }

            // allow application to enforce policy by validating the
            // certificate
            try {
                sipStack.getTlsSecurityPolicy().enforceTlsPolicy(
                            channel.getEncapsulatedClientTransaction());
            }
            catch (SecurityException ex) {
                throw new IOException(ex.getMessage());
            }

            if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                this.logger.logDebug(
                        "TLS Security policy passed");
            }

            putSocket(key, clientSock);
        }

        return clientSock.getLocalSocketAddress();
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

    public Socket sendBytes(InetAddress senderAddress,
            InetAddress receiverAddress, int contactPort, String transport,
            byte[] bytes, boolean isClient, MessageChannel messageChannel)
            throws IOException {
        int retry_count = 0;
        int max_retry = isClient ? 2 : 1;
        // Server uses TCP transport. TCP client sockets are cached
        int length = bytes.length;
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug(
                    "sendBytes " + transport + " local inAddr "
                    		+ senderAddress.getHostAddress() + " remote inAddr "
                            + receiverAddress.getHostAddress() + " port = "
                            + contactPort + " length = " + length + " isClient " + isClient );

        }
        if (logger.isLoggingEnabled(LogLevels.TRACE_INFO)
                && sipStack.isLogStackTraceOnMessageSend()) {
            logger.logStackTrace(StackLogger.TRACE_INFO);
        }
        if (transport.compareToIgnoreCase(TCP) == 0) {
            String key = makeKey(receiverAddress, contactPort);
            // This should be in a synchronized block ( reported by
            // Jayashenkhar ( lucent ).

            Socket clientSock = null;
            enterIOCriticalSection(key);

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
                        	clientSock = sipStack.getNetworkLayer().createSocket(
                        			receiverAddress, contactPort, senderAddress);
                        } catch (SocketException e) { // We must catch the socket timeout exceptions here, any SocketException not just ConnectException
                        	logger.logError("Problem connecting " +
                        			receiverAddress + " " + contactPort + " " + senderAddress + " for message " + new String(bytes, "UTF-8"));
                        	// new connection is bad.
                        	// remove from our table the socket and its semaphore
                        	removeSocket(key);
                        	throw new SocketException(e.getClass() + " " + e.getMessage() + " " + e.getCause() + " Problem connecting " +
                        			receiverAddress + " " + contactPort + " " + senderAddress + " for message " + new String(bytes, "UTF-8"));
                        }
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                        	logger.logDebug("local inaddr = " + clientSock.getLocalAddress().getHostAddress());
                        }
                        OutputStream outputStream = clientSock
                                .getOutputStream();
                        writeChunks(outputStream, bytes, length);
                        putSocket(key, clientSock);
                        break;
                    } else {
                        try {
                            OutputStream outputStream = clientSock
                                    .getOutputStream();
                            writeChunks(outputStream, bytes, length);
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
            } finally {
                leaveIOCriticalSection(key);
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
        } else if (transport.compareToIgnoreCase(TLS) == 0) {
            String key = makeKey(receiverAddress, contactPort);
            Socket clientSock = null;
            enterIOCriticalSection(key);

            try {
                clientSock = getSocket(key);

                while (retry_count < max_retry) {
                    if (clientSock == null) {

                        clientSock = sipStack.getNetworkLayer()
                                .createSSLSocket(receiverAddress, contactPort,
                                        senderAddress);
                        SSLSocket sslsock = (SSLSocket) clientSock;

                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            logger.logDebug(
                                    "inaddr = " + receiverAddress);
                            logger.logDebug(
                                    "port = " + contactPort);
                        }
                        HandshakeCompletedListenerImpl listner = new HandshakeCompletedListenerImpl((TLSMessageChannel)messageChannel, clientSock);
                        ((TLSMessageChannel) messageChannel)
                                .setHandshakeCompletedListener(listner);
                        sslsock.addHandshakeCompletedListener(listner);
                        sslsock.setEnabledProtocols(sipStack
                                .getEnabledProtocols());

                        listner.startHandshakeWatchdog();
                        sslsock.startHandshake();
                        ((TLSMessageChannel)messageChannel).setHandshakeCompleted(true);
                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            this.logger.logDebug(
                                    "Handshake passed");
                        }
                        // allow application to enforce policy by validating the
                        // certificate

                        try {
                            sipStack
                                    .getTlsSecurityPolicy()
                                    .enforceTlsPolicy(
                                            messageChannel
                                                    .getEncapsulatedClientTransaction());
                        } catch (SecurityException ex) {
                            throw new IOException(ex.getMessage());
                        }

                        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
                            this.logger.logDebug(
                                    "TLS Security policy passed");
                        }
                        OutputStream outputStream = clientSock
                                .getOutputStream();
                        writeChunks(outputStream, bytes, length);
                        putSocket(key, clientSock);
                        break;
                    } else {
                        try {
                            OutputStream outputStream = clientSock
                                    .getOutputStream();
                            writeChunks(outputStream, bytes, length);
                            break;
                        } catch (IOException ex) {
                            if (logger.isLoggingEnabled())
                                logger.logException(ex);
                            // old connection is bad.
                            // remove from our table.
                            removeSocket(key);

                            try {
                                logger.logDebug(
                                        "Closing socket");
                                clientSock.close();
                            } catch (Exception e) {
                            }
                            clientSock = null;
                            retry_count++;
                        }
                    }
                }
            } catch (SSLHandshakeException ex) {
                removeSocket(key);
                throw ex;
            } catch (IOException ex) {
                removeSocket(key);
            } finally {
                leaveIOCriticalSection(key);
            }
            if (clientSock == null) {
                throw new IOException("Could not connect to " + receiverAddress
                        + ":" + contactPort);
            } else
                return clientSock;

        } else {
            // This is a UDP transport...
            DatagramSocket datagramSock = sipStack.getNetworkLayer()
                    .createDatagramSocket();
            datagramSock.connect(receiverAddress, contactPort);
            DatagramPacket dgPacket = new DatagramPacket(bytes, 0, length,
                    receiverAddress, contactPort);
            datagramSock.send(dgPacket);
            datagramSock.close();
            return null;
        }

    }

    /*
     * private void enterIOCriticalSection(String key) throws IOException { try
     * { if ( ! this.ioSemaphore.tryAcquire(10,TimeUnit.SECONDS) ) { throw new
     * IOException("Could not acquire semaphore"); } } catch
     * (InterruptedException e) { throw new
     * IOException("exception in acquiring sem"); } }
     *
     *
     * private void leaveIOCriticalSection(String key) {
     * this.ioSemaphore.release(); }
     */

    private void leaveIOCriticalSection(String key) {
        Semaphore creationSemaphore = socketCreationMap.get(key);
        if (creationSemaphore != null) {
            creationSemaphore.release();
        }
    }

    private void enterIOCriticalSection(String key) throws IOException {
        // http://dmy999.com/article/34/correct-use-of-concurrenthashmap
        Semaphore creationSemaphore = socketCreationMap.get(key);
        if(creationSemaphore == null) {
            Semaphore newCreationSemaphore = new Semaphore(1, true);
            creationSemaphore = socketCreationMap.putIfAbsent(key, newCreationSemaphore);
            if(creationSemaphore == null) {
                creationSemaphore = newCreationSemaphore;       
                if (logger.isLoggingEnabled(StackLogger.TRACE_DEBUG)) {
                    logger.logDebug("new Semaphore added for key " + key);
                }
            }
        }
        
        try {
            boolean retval = creationSemaphore.tryAcquire(10, TimeUnit.SECONDS);
            if (!retval) {
                throw new IOException("Could not acquire IO Semaphore'" + key
                        + "' after 10 seconds -- giving up ");
            }
        } catch (InterruptedException e) {
            throw new IOException("exception in acquiring sem");
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
        for (Enumeration<Socket> values = socketTable.elements(); values
                .hasMoreElements();) {
            Socket s = (Socket) values.nextElement();
            try {
                s.close();
            } catch (IOException ex) {
            }
        }

    }

}
