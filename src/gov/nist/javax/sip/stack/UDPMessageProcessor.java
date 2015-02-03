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

import gov.nist.core.CommonLogger;
import gov.nist.core.HostPort;
import gov.nist.core.LogWriter;
import gov.nist.core.StackLogger;
import gov.nist.core.ThreadAuditor;
import gov.nist.javax.sip.SipStackImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.sip.IOExceptionEvent;
import javax.sip.SipListener;

/**
 * Sit in a loop and handle incoming udp datagram messages. For each Datagram
 * packet, a new UDPMessageChannel is created (upto the max thread pool size).
 * Each UDP message is processed in its own thread).
 *
 * @version 1.2 $Revision: 1.46 $ $Date: 2010-12-02 22:04:12 $
 *
 * @author M. Ranganathan  <br/>
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
public class UDPMessageProcessor extends MessageProcessor implements Runnable {
	
	private static StackLogger logger = CommonLogger.getLogger(UDPMessageProcessor.class);
    /**
     * The Mapped port (in case STUN suport is enabled)
     */
    private int port;

    /**
     * Incoming messages are queued here.
     */
    protected BlockingQueue<DatagramQueuedMessageDispatch> messageQueue;
    
    /**
     * Auditing taks that checks for outdated requests in the queue
     */
    BlockingQueueDispatchAuditor congestionAuditor;

    /**
     * A list of message channels that we have started.
     */
    protected LinkedList messageChannels;

    /**
     * Max # of udp message channels
     */
    protected int threadPoolSize;

    protected DatagramSocket sock;

    /**
     * A flag that is set to false to exit the message processor (suggestion by
     * Jeff Keyser).
     */
    protected boolean isRunning;
    
    private static final int HIGHWAT=5000;
    
    private static final int LOWAT=2500;

    private int maxMessageSize = SipStackImpl.MAX_DATAGRAM_SIZE;
    private int exceptionsReportedCounter;
    private static final int MAX_EXCEPTIONS_TO_REPORT = 10;
    
    /**
     * Constructor.
     *
     * @param sipStack
     *            pointer to the stack.
     */
    protected UDPMessageProcessor(InetAddress ipAddress,
            SIPTransactionStack sipStack, int port) throws IOException {
        super(ipAddress, port, "udp",sipStack);

        this.sipStack = sipStack;
        if(sipStack.getMaxMessageSize() < SipStackImpl.MAX_DATAGRAM_SIZE && sipStack.getMaxMessageSize() > 0) {
            this.maxMessageSize = sipStack.getMaxMessageSize();
        }
        if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
            logger.logDebug("Max Message size is " + maxMessageSize);
        }
        this.messageQueue = new LinkedBlockingQueue<DatagramQueuedMessageDispatch>();
        if(sipStack.stackCongenstionControlTimeout>0) {
        	this.congestionAuditor = new BlockingQueueDispatchAuditor(this.messageQueue);
        	this.congestionAuditor.setTimeout(sipStack.stackCongenstionControlTimeout);
        	this.congestionAuditor.start(2000);
        }

        this.port = port;
        try {
            this.sock = sipStack.getNetworkLayer().createDatagramSocket(port,
                    ipAddress);
            // Create a new datagram socket.
            sock.setReceiveBufferSize(sipStack.getReceiveUdpBufferSize());
            sock.setSendBufferSize(sipStack.getSendUdpBufferSize());

            /**
             * If the thread auditor is enabled, define a socket timeout value in order to
             * prevent sock.receive() from blocking forever
             */
            if (sipStack.getThreadAuditor().isEnabled()) {
                sock.setSoTimeout((int) sipStack.getThreadAuditor().getPingIntervalInMillisecs());
            }
            if ( ipAddress.getHostAddress().equals(IN_ADDR_ANY)  ||
                 ipAddress.getHostAddress().equals(IN6_ADDR_ANY)){
                // Store the address to which we are actually bound
                // Note that on WINDOWS this is actually broken. It will
                // return IN_ADDR_ANY again. On linux it will return the
                // address to which the socket was actually bound.
                super.setIpAddress( sock.getLocalAddress() );

            }
        } catch (Exception ex) {
        	if(this.congestionAuditor != null) this.congestionAuditor.stop();
            throw new IOException(ex);
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
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        // Issue #32 on java.net
        thread.setName("MessageProcessorThread-UDP-" + getIpAddress().getHostAddress() + '/' + getPort());
        // Issue #184
        thread.setPriority(sipStack.getThreadPriority());
        thread.start();
    }

    /**
     * Thread main routine.
     */
    public void run() {
        // Check for running flag.
        this.messageChannels = new LinkedList();
        // start all our messageChannels (unless the thread pool size is
        // infinity.
        if (sipStack.threadPoolSize != -1) {
            for (int i = 0; i < sipStack.threadPoolSize; i++) {
                UDPMessageChannel channel = new UDPMessageChannel(sipStack,
                        this, ((SipStackImpl)sipStack).getStackName() + "-UDPMessageChannelThread-" + i);
                this.messageChannels.add(channel);

            }
        }

        // Ask the auditor to monitor this thread
        ThreadAuditor.ThreadHandle threadHandle = sipStack.getThreadAuditor().addCurrentThread();

        // Somebody asked us to exit. if isRunnning is set to false.
        while (this.isRunning) {

            try {
                // Let the thread auditor know we're up and running
                threadHandle.ping();

                int bufsize = this.maxMessageSize;
                byte message[] = new byte[bufsize];
                DatagramPacket packet = new DatagramPacket(message, bufsize);
                sock.receive(packet);
                
                // Count of # of packets in process.
                // this.useCount++;
                if (sipStack.threadPoolSize != -1) {
                    // Note: the only condition watched for by threads
                    // synchronizing on the messageQueue member is that it is
                    // not empty. As soon as you introduce some other
                    // condition you will have to call notifyAll instead of
                    // notify below.

                    this.messageQueue.offer(new DatagramQueuedMessageDispatch(packet, System.currentTimeMillis()));                 

                } else {
                    new UDPMessageChannel(sipStack, this, packet);
                }
                exceptionsReportedCounter = 0;	// reset lock flooding checker
            }
            catch (SocketTimeoutException ex) {
              // This socket timeout allows us to ping the thread auditor periodically
            }
            catch (SocketException ex) {
            	if( !isRunning ) {
					if (logger.isLoggingEnabled(LogWriter.TRACE_DEBUG)) {
	                    logger.logDebug("UDPMessageProcessor: Stopping");
					}
                    return;
            	}
            	else {
            		reportSockeException(ex);		// report exception but try to continue to receive data ...
            	}
            }
            catch (IOException ex) {
        		reportSockeException(ex);		// report exception but try to continue to receive data ...
            }
            catch (Exception ex) {
        		reportSockeException(ex);		// report exception but try to continue to receive data ...
            }
        }
    }
    
    private void reportSockeException(Exception e) {
		if( exceptionsReportedCounter < MAX_EXCEPTIONS_TO_REPORT ) {
			exceptionsReportedCounter++;
	    	boolean lastBeforeFloodingChecker = exceptionsReportedCounter == MAX_EXCEPTIONS_TO_REPORT;
	    	String msg = String.format("Caught '%s' on UDP receive socket on %s:%s, message '%s'. Trying to ignore it ... %s",
			                     e.getClass().getSimpleName(), sock.getLocalAddress().getHostAddress(), getPort(), e.getMessage(), lastBeforeFloodingChecker ? "(Flooding checker active, no more socket IO-exceptions will be reported)" : "");
	    	logger.logWarning(msg);
	    	logger.logException(e);
			SipListener listener = sipStack.getSipListener();
			if( listener != null ) {
				listener.processIOException( new SocketIOExceptionEvent(msg));
			}
		}
    }
    
    /**
     * Helper class used to give a more detailed log output in its toString() method
     */
    private class SocketIOExceptionEvent extends IOExceptionEvent {
		private static final long serialVersionUID = 778500971662697296L;
		private final String msg;

		public SocketIOExceptionEvent(String msg) {
			super(UDPMessageProcessor.this, sock.getLocalAddress().getHostAddress(), port, transport);
			this.msg = msg;
		}
		
		@Override
		public String toString() {
			return msg;
		}
    }

    /**
     * Shut down the message processor. Close the socket for recieving incoming
     * messages.
     */
    public void stop() {
            this.isRunning = false;
            sock.close();        
          // closing the channels
          for (Object messageChannel : messageChannels) {
			((MessageChannel)messageChannel).close();
          }
          if(sipStack.stackCongenstionControlTimeout > 0 && congestionAuditor != null) {
          	this.congestionAuditor.stop();
          }
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
        return sipStack.getReceiveUdpBufferSize();
    }

    /**
     * Return true if there are any messages in use.
     */
    public boolean inUse() {
    	return !messageQueue.isEmpty();
    }

}
