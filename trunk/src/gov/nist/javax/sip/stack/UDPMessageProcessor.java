
/******************************************************************************
*   Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
*******************************************************************************/
package gov.nist.javax.sip.stack;
import java.net.Socket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.ServerSocket;
import java.net.DatagramPacket;
import java.io.IOException;
import java.util.LinkedList;
import java.net.InetAddress;
import java.net.UnknownHostException;
import gov.nist.core.*;
import gov.nist.javax.sip.*;

/**
* Sit in a loop and handle incoming udp datagram messages. For each Datagram
* packet, a new UDPMessageChannel is created (upto the max thread pool size). 
* Each UDP message is processed in its own thread). 
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* Acknowledgement: Jeff Keyser contributed ideas on
* starting and stoppping the stack that were incorporated into this code.
* Niklas Uhrberg suggested that thread pooling be added to limit the number
* of threads and improve performance.
*/
public class UDPMessageProcessor  extends MessageProcessor {

    /** Increment this when a message comes in and decrement after
     * processing is done.
     */
    int useCount;
	
    /** port on which to listen for incoming messaes. */

    int port;

    /** Incoming messages are queued here. */
    
    protected LinkedList messageQueue;
    
    
    /** A list of message channels that we have started. */
    
    protected LinkedList messageChannels;
    
    /** Max # of udp message channels */
    
    protected int threadPoolSize;

    /** Max datagram size.  */

    protected  static final int MAX_DATAGRAM_SIZE = 8 * 1024;
    /** Our stack (that created us).  */
    protected SIPStack sipStack; 

     /** Channel notifier (not very useful for UDP).  */
    protected ChannelNotifier notifier;
    
    protected DatagramSocket sock;
    
    /** A flag that is set to false to exit the message processor
     *(suggestion by Jeff Keyser).
     */
    protected boolean isRunning;
    
    /**
     * Constructor.
     * @param sipStack pointer to the stack.
     * @param channelNotifier channel notifier.
     */
    protected  UDPMessageProcessor( SIPStack sipStack,
    ChannelNotifier channelNotifier, int port ) {
        this.sipStack = sipStack;
        this.notifier = channelNotifier;
	this.messageQueue = new LinkedList();
	this.port =  port;
    }

    /** Get port on which to listen for incoming stuff.
     *@return port on which I am listening.
     */
    public int getPort() { 
	return this.port; 
    }
    
    /**
     * Start our processor thread.
     */
    public void start() throws IOException {
        // Create a new datagram socket.
	// Bug uncovered by
        this.sock =
        new DatagramSocket(port,sipStack.stackInetAddress);
         sock.setReceiveBufferSize
        (MAX_DATAGRAM_SIZE);
        this.isRunning = true;
        Thread thread = new Thread(this);
        thread.start();
    }
    
    
    /**
     * Thread main routine.
     */
    public  void run() {
        // Check for running flag.
        this.messageChannels = new LinkedList();
        // start all our messageChannels (unless the thread pool size is
        // infinity.
        if (sipStack.threadPoolSize != -1) {
            for (int i = 0; i < sipStack.threadPoolSize; i++) {
                UDPMessageChannel channel =
                 new UDPMessageChannel
                    (sipStack,notifier,this);
                 this.messageChannels.add(channel);
             
            }
        }
        while (this.isRunning) {  
                // Somebody asked us to exit.
            try {
                int bufsize = sock.getReceiveBufferSize();
                byte message[] = new byte[bufsize];
                DatagramPacket packet =
                	new DatagramPacket( message, bufsize);
		// System.out.println("received " + new String(message));
                sock.receive(packet);

		// Count of # of packets in process.
	  	this.useCount ++;
		if (sipStack.threadPoolSize != -1 ) {
		// Note: the only condition watched for by threads 
		// synchronizing on the messageQueue member is that it is 
		// not empty. As soon as you introduce some other
		// condition you will have to call notifyAll instead of 
		// notify below.
                 synchronized(this.messageQueue) {
                     this.messageQueue.addLast(packet);
                     this.messageQueue.notify();
                  }
		} else {
                   new UDPMessageChannel(sipStack,notifier,this,packet);
                }
            } catch (SocketException ex) {
		if (LogWriter.needsLogging)
                	LogWriter.logMessage("UDPMessageProcessor: Stopping");
                isRunning = false;
		// The notifyAll should be in a synchronized block.
		// ( bug report by Niklas Uhrberg ).
		synchronized (this.messageQueue) {
                	this.messageQueue.notifyAll();
		}
            } catch (IOException ex) {
                isRunning = false;
                ex.printStackTrace();
		if (LogWriter.needsLogging)
                   LogWriter.logMessage
		  ("UDPMessageProcessor: Got an IO Exception");
            } catch (Exception ex) {
		if (LogWriter.needsLogging)
		LogWriter.logMessage
                ("UDPMessageProcessor: Unexpected Exception - quitting");
                InternalErrorHandler.handleException(ex);
                return;
            }
        }
    }
    
    /** Shut down the message processor. Close the socket for recieving
     * incoming messages.
     */
    
    public void stop() {
	synchronized(this.messageQueue) {
           this.isRunning = false;
           this.messageQueue.notifyAll();
           sock.close();
	}
    }
    
    /**
     * Return the transport string.
     *@return the transport string
     */
    public String getTransport() {
        return "udp";
    }
    
    
    
    /**
     * Returns the stack.
     *@return my sip stack.
     */
    public SIPStack getSIPStack() {
        return sipStack;
    }
    
    
	/** Create and return new TCPMessageChannel for the given host/port.
	 */
	public MessageChannel createMessageChannel(HostPort targetHostPort)
		throws UnknownHostException {
		return new UDPMessageChannel(targetHostPort.getInetAddress(), 
			targetHostPort.getPort(), sipStack,this);
	}

	public MessageChannel createMessageChannel
		(InetAddress host, int port) throws IOException {
		return new UDPMessageChannel(host,port,sipStack,this);
	}

	/** Default target port for UDP
	 */
	public int getDefaultTargetPort() {
		return 5060;
	}

	/** UDP is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}

	/** UDP can handle a message as large as the MAX_DATAGRAM_SIZE.
	 */
	public int getMaximumMessageSize() {
		return MAX_DATAGRAM_SIZE;
	}


	/** Return true if there are any messages in use.
	*/
	public boolean inUse() { return useCount != 0; }

}
