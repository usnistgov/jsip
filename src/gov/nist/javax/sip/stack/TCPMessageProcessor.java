/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.SocketException;
import gov.nist.core.*;
import java.net.*;


/**
* Sit in a loop waiting for incoming tcp connections and start a
* new thread to handle each new connection. This is the active
* object that creates new TCP MessageChannels (one for each new
* accept socket).  
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*Acknowledgement: Jeff Keyser suggested that a
*Stop mechanism be added to this. Niklas Uhrberg suggested that
*a means to limit the number of simultaneous active connections
*should be added.
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class TCPMessageProcessor extends MessageProcessor  {


    protected int useCount;
	
    protected int port;
    
    protected int nConnections;
    
    private boolean isRunning;
    
    private ServerSocket sock;
    /** The SIP Stack Structure.
     */
    protected SIPStack sipStack;
    /** Optional channel notifier (method gets invoked on channel open
     * and close).
     */
    protected ChannelNotifier notifier;
    
    /** Constructor.
     * @param sipStack SIPStack structure.
     * @param channelNotifier Optional channel notifier.
     */
    protected TCPMessageProcessor(SIPStack sipStack, 
    	ChannelNotifier channelNotifier , int port) {
        this.sipStack = sipStack;
        notifier = channelNotifier;
	this.port = port;
    }
    
    /**
     * Start the processor.
     */
    public void start() throws IOException {
        Thread thread = new Thread(this);
        this.sock = new ServerSocket(this.port,0,sipStack.stackInetAddress);
        this.isRunning = true;
        thread.start();
        
    }
    
    /** Run method for the thread that gets created for each accept
     * socket.
     */
    public void run() {
        // Accept new connectins on our socket.
        while(this.isRunning) {
            try {
                synchronized(this) {
                    // sipStack.maxConnections == -1 means we are
                    // willing to handle an "infinite" number of
                    // simultaneous connections (no resource limitation).
                    // This is the default behavior.
                    while ( this.isRunning &&
                    sipStack.maxConnections != -1 &&
                    this.nConnections >= sipStack.maxConnections)  {
                        try {
                            this.wait();
                            if (! this.isRunning)  return;
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    this.nConnections ++;
                }
                Socket newsock = sock.accept();
                if (LogWriter.needsLogging) {
                    LogWriter.logMessage(
                    "Accepting new connection!");
                }
                TCPMessageChannel tcpMessageChannel =
                new TCPMessageChannel
                (newsock,sipStack,notifier,this);
                
                if (notifier != null) {
                    notifier.notifyOpen(tcpMessageChannel);
                }
            } catch (SocketException ex) {
                this.isRunning = false;
            }  catch (IOException ex) {
                ex.printStackTrace();
                this.isRunning = false;
            } catch (Exception ex) {
                InternalErrorHandler.handleException(ex);
            }
        }
    }
    
    /**
     * Return the transport string.
     *@return the transport string
     */
    public String getTransport() {
        return "tcp";
    }
    
    /** Returns the port that we are listening on.
     * @return Port address for the tcp accept.
     */
    
    public int getPort() {
        return this.port;
    }
    
    /**
     * Returns the stack.
     *@return my sip stack.
     */
    public SIPStack getSIPStack() {
        return sipStack;
    }
    
    /** Stop the message processor.
     *Feature suggested by Jeff Keyser.
     */
    public synchronized void stop() {
        isRunning = false;
	this.listeningPoint = null;
        try{
            sock.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        this.notify();
        
    }
    
    
    /** Create and return new TCPMessageChannel for the given host/port.
     */
    public MessageChannel createMessageChannel(HostPort targetHostPort)
    throws IOException {
        return new
        TCPMessageChannel(targetHostPort.getInetAddress(),
        targetHostPort.getPort(), sipStack, notifier,this);
    }
    
    public MessageChannel createMessageChannel(InetAddress host,
    int port ) throws IOException {
        return new
        TCPMessageChannel(host,port,sipStack,notifier,this);
    }
    
    
    
    
    /** TCP can handle an unlimited number of bytes.
     */
    public int getMaximumMessageSize() {
        return Integer.MAX_VALUE;
    }
    
    /** TCP NAPTR service name.
     */
    public String getNAPTRService() {
        return "SIP+D2T";
    }
    
    /** TCP SRV prefix.
     */
    public String getSRVPrefix() {
        return "_sip._tcp.";
    }

    public boolean inUse() {
	return this.useCount != 0;
    }

    /** Default target port for TCP
     */
    public int getDefaultTargetPort() {
        return 5060;
    }
    
    /** TCP is not a secure protocol.
     */
    public boolean isSecure() {
        return false;
    }
    
}

