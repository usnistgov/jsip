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
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.core.LogWriter;
import gov.nist.javax.sip.SipStackImpl;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
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

class IOHandler {

	private Semaphore ioSemaphore = new Semaphore(1);

	private SipStackImpl sipStack;

	private static String TCP = "tcp";

	// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
	private static String TLS = "tls";

	// A cache of client sockets that can be re-used for
	// sending tcp messages.
	private ConcurrentHashMap<String, Socket> socketTable;

	protected static String makeKey(InetAddress addr, int port) {
		return addr.getHostAddress() + ":" + port;

	}

	protected IOHandler(SIPTransactionStack sipStack) {
		this.sipStack = (SipStackImpl) sipStack;
		this.socketTable = new ConcurrentHashMap<String, Socket>();

	}

	protected void putSocket(String key, Socket sock) {
		socketTable.put(key, sock);

	}

	protected Socket getSocket(String key) {
		return (Socket) socketTable.get(key);
	}

	protected void removeSocket(String key) {
		socketTable.remove(key);
	}

	/**
	 * A private function to write things out. This needs to be syncrhonized as
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
	 * Send an array of bytes.
	 * 
	 * @param receiverAddress --
	 *            inet address
	 * @param contactPort --
	 *            port to connect to.
	 * @param transport --
	 *            tcp or udp.
	 * @param retry --
	 *            retry to connect if the other end closed connection
	 * @throws IOException --
	 *             if there is an IO exception sending message.
	 */

	public Socket sendBytes(InetAddress senderAddress,
			InetAddress receiverAddress, int contactPort, String transport,
			byte[] bytes, boolean retry) throws IOException {
		int retry_count = 0;
		int max_retry = retry ? 2 : 1;
		// Server uses TCP transport. TCP client sockets are cached
		int length = bytes.length;
		if (sipStack.isLoggingEnabled()) {
			sipStack.logWriter.logDebug("sendBytes " + transport + " inAddr "
					+ receiverAddress.getHostAddress() + " port = "
					+ contactPort + " length = " + length);
		}
		if ( sipStack.isLoggingEnabled() && sipStack.logStackTraceOnMessageSend ) {
		    sipStack.logWriter.logStackTrace(LogWriter.TRACE_MESSAGES);
		}
		if (transport.compareToIgnoreCase(TCP) == 0) {
			String key = makeKey(receiverAddress, contactPort);
			// This should be in a synchronized block ( reported by
			// Jayashenkhar ( lucent ).

			try {
				boolean retval = this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS);  // TODO - make this a stack config parameter?
				if ( !retval ) {
					throw new IOException("Could not acquire IO Semaphore after 10 second -- giving up ");
				}
			} catch (InterruptedException ex) {
				throw new IOException("exception in aquiring sem");
			}
			Socket clientSock = getSocket(key);

			try {

				while (retry_count < max_retry) {
					if (clientSock == null) {
						if (sipStack.isLoggingEnabled()) {
							sipStack.logWriter.logDebug("inaddr = "
									+ receiverAddress);
							sipStack.logWriter
									.logDebug("port = " + contactPort);
						}
						// note that the IP Address for stack may not be
						// assigned.
						// sender address is the address of the listening point.
						// in version 1.1 all listening points have the same IP
						// address (i.e. that of the stack). In version 1.2
						// the IP address is on a per listening point basis.
						clientSock = sipStack.getNetworkLayer().createSocket(
								receiverAddress, contactPort, senderAddress);
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
							if (sipStack.isLoggingEnabled())
								sipStack.logWriter.logDebug("IOException occured retryCount " + retry_count);
							// old connection is bad.
							// remove from our table.
							removeSocket(key);
							try {
								clientSock.close();
							} catch (Exception e) {
							}
							clientSock = null;
							retry_count++;
						}
					}
				}
			} finally {
				ioSemaphore.release();
			}

			if (clientSock == null) {
				throw new IOException("Could not connect to " + receiverAddress
						+ ":" + contactPort);
			} else
				return clientSock;

			// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
			// Copied and modified from the former section for TCP
		} else if (transport.compareToIgnoreCase(TLS) == 0) {
			String key = makeKey(receiverAddress, contactPort);
			try {
				boolean retval = this.ioSemaphore.tryAcquire(10000, TimeUnit.MILLISECONDS);
				if ( ! retval ) throw new IOException ("Timeout aquiring IO SEM");
			} catch (InterruptedException ex) {
				throw new IOException("exception in aquiring sem");
			}
			Socket clientSock = getSocket(key);
			try {
				while (retry_count < max_retry) {
					if (clientSock == null) {
						if (sipStack.isLoggingEnabled()) {
							sipStack.logWriter.logDebug("inaddr = "
									+ receiverAddress);
							sipStack.logWriter
									.logDebug("port = " + contactPort);
						}
						if (!sipStack.useTlsAccelerator) {
							clientSock = sipStack.getNetworkLayer()
									.createSSLSocket(receiverAddress,
											contactPort, senderAddress);
						} else {
							clientSock = sipStack.getNetworkLayer()
									.createSocket(receiverAddress, contactPort,
											senderAddress);
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
							if (sipStack.isLoggingEnabled())
								sipStack.logWriter.logException(ex);
							// old connection is bad.
							// remove from our table.
							removeSocket(key);
							try {
								clientSock.close();
							} catch (Exception e) {
							}
							clientSock = null;
							retry_count++;
						}
					}
				}
			} finally {
				ioSemaphore.release();
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

	/**
	 * Close all the cached connections.
	 */
	public void closeAll() {
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
