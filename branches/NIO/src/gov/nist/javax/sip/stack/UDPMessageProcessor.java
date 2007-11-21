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
 *   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).     *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.net.*;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoServiceConfig;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.transport.socket.nio.DatagramAcceptor;
import org.apache.mina.transport.socket.nio.DatagramAcceptorConfig;
import org.apache.mina.transport.socket.nio.DatagramSessionConfig;

import gov.nist.core.*;

/**
 * Sit in a loop and handle incoming udp datagram messages. For each Datagram
 * packet, a new UDPMessageChannel is created (upto the max thread pool size).
 * Each UDP message is processed in its own thread).
 * 
 * @version 1.2 $Revision: 1.29.4.1 $ $Date: 2007-11-21 23:55:33 $
 * 
 * @author M. Ranganathan <br/>
 * 
 * 
 * 
 * <a href="{@docRoot}/../uml/udp-request-processing-sequence-diagram.jpg">
 * See the implementation sequence diagram for processing incoming requests.
 * </a>
 * 
 * 
 * Acknowledgement: Jeff Keyser contributed ideas on starting and stoppping the
 * stack that were incorporated into this code. Niklas Uhrberg suggested that
 * thread pooling be added to limit the number of threads and improve
 * performance.
 */
public class UDPMessageProcessor extends MessageProcessor implements IoHandler {

	/**
	 * The Mapped port (in case STUN suport is enabled)
	 */
	private int port;

	
	/**
	 * Max datagram size.
	 */
	protected static final int MAX_DATAGRAM_SIZE = 8 * 1024;

	/**
	 * Our stack (that created us).
	 */
	protected SIPTransactionStack sipStack;
	
	private BlockingQueue<Runnable> messageQueue;


	/**
	 * A flag that is set to false to exit the message processor (suggestion by
	 * Jeff Keyser).
	 */
	protected boolean isRunning;

	private ThreadPoolExecutor threadPoolExecutor;

	private IoSession session;
	
	private long lastBytesRead;

	/**
	 * Constructor.
	 * 
	 * @param sipStack
	 *            pointer to the stack.
	 */
	protected UDPMessageProcessor(InetAddress ipAddress,
			SIPTransactionStack sipStack, int port) throws IOException {
		super(ipAddress, port, "udp");

		this.sipStack = sipStack;

		int coreThreads = sipStack.threadPoolSize;
		if ( coreThreads == -1 ) coreThreads = 1;
		
		// This does the actual processing of the message
		this.messageQueue = new LinkedBlockingQueue<Runnable>();
		this.threadPoolExecutor      = new ThreadPoolExecutor(coreThreads, 2*coreThreads, 5, TimeUnit.SECONDS, messageQueue);
		this.threadPoolExecutor.prestartAllCoreThreads();

		

		IoAcceptor acceptor = new DatagramAcceptor(Executors.newCachedThreadPool());
		

		IoServiceConfig acceptorConfig = acceptor.getDefaultConfig();
		

		acceptorConfig.setThreadModel(ThreadModel.MANUAL);

		// Prepare the service configuration.
		DatagramAcceptorConfig cfg = new DatagramAcceptorConfig();
		DatagramSessionConfig dcfg = cfg.getSessionConfig();

		dcfg.setTrafficClass(0x10);
		/** IPTOS_LOWDELAY */
		dcfg.setReuseAddress(false);

		// Start the listener
		InetSocketAddress address = new InetSocketAddress(ipAddress, port);

		acceptor.bind(address, this);

		this.port = port;

		if (ipAddress.getHostAddress().equals(IN_ADDR_ANY)
				|| ipAddress.getHostAddress().equals(IN6_ADDR_ANY)) {
			// Store the address to which we are actually bound
			// Note that on WINDOWS this is actually broken. It will
			// return IN_ADDR_ANY again. On linux it will return the
			// address to which the socket was actually bound.
			super.setIpAddress(new DatagramSocket().getLocalAddress());

		}

	}

	/**
	 * Get port on which to listen for incoming stuff.
	 * 
	 * @return port on which I am listening.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Start our processor thread.
	 */
	public void start() throws IOException {
		this.isRunning = true;
	}

	

	/**
	 * Shut down the message processor. Close the socket for recieving incoming
	 * messages.
	 */
	public void stop() {
		this.isRunning = false;
		this.threadPoolExecutor.shutdown();
		this.getSession().close();
	}

	/**
	 * Return the transport string.
	 * 
	 * @return the transport string
	 */
	public String getTransport() {
		return "udp";
	}

	/**
	 * Returns the stack.
	 * 
	 * @return my sip stack.
	 */
	public SIPTransactionStack getSIPStack() {
		return sipStack;
	}

	/**
	 * Create and return new TCPMessageChannel for the given host/port.
	 */
	public MessageChannel createMessageChannel(HostPort targetHostPort)
			throws UnknownHostException {
		return new UDPMessageChannel(targetHostPort.getInetAddress(),
				targetHostPort.getPort(), sipStack, this);
	}

	public MessageChannel createMessageChannel(InetAddress host, int port)
			throws IOException {
		return new UDPMessageChannel(host, port, sipStack, this);
	}

	/**
	 * Default target port for UDP
	 */
	public int getDefaultTargetPort() {
		return 5060;
	}

	/**
	 * UDP is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}

	/**
	 * UDP can handle a message as large as the MAX_DATAGRAM_SIZE.
	 */
	public int getMaximumMessageSize() {
		return MAX_DATAGRAM_SIZE;
	}

	/**
	 * Return true if there are any messages in use.
	 */
	public boolean inUse() {
		try {
			return !this.threadPoolExecutor.awaitTermination(1000,
					TimeUnit.MILLISECONDS);
		} catch (Exception ex) {
			return true;
		}
	}

	@Override
	public void exceptionCaught(IoSession iosession, Throwable throwable)
			throws Exception {
		if ( sipStack.isLoggingEnabled()) {
			sipStack.getLogWriter().logError("Exception caught in UDPMessageProcessor", throwable);
		}

	}

	@Override
	public void messageReceived(IoSession iosession, Object obj) throws Exception {
		ByteBuffer byteBuffer = (ByteBuffer) obj;
		long bytesReadLength = iosession.getReadBytes();
		int readLength = (int) (  bytesReadLength - this.lastBytesRead);
		this.lastBytesRead = bytesReadLength;
		sipStack.getLogWriter().logDebug("bytesReadLength = " + bytesReadLength);
		byte[] bytesRead = new byte[readLength];
		byteBuffer.get(bytesRead); 
		InetSocketAddress sockAddr = (InetSocketAddress)iosession.getRemoteAddress();
		InetAddress inetAddress = sockAddr.getAddress();
		int port = sockAddr.getPort();
		this.threadPoolExecutor.execute(new UDPMessageChannel(
				this.sipStack, this,  bytesRead, inetAddress,port));

	}

	@Override
	public void messageSent(IoSession ioSession, Object object) throws Exception {
		this.sipStack.getLogWriter().logDebug("UDPMessageProcessor: messageSent");

	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		if (sipStack.isLoggingEnabled())
			getSIPStack().logWriter
					.logDebug("UDPMessageProcessor: Stopping");
		isRunning = false;
		this.threadPoolExecutor.shutdown();

	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		
		if ( sipStack.isLoggingEnabled())
			sipStack.logWriter.logDebug("UDPMessageProcessor: mina sessionCreated");
		this.session = session;

	}

	@Override
	public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		this.setSession(session);

	}

	

	/**
	 * @return the session
	 */
	public IoSession getSession() {
		return session;
	}

	/**
	 * @param session the session to set
	 */
	protected void setSession(IoSession session) {
		this.session = session;
	}

}
