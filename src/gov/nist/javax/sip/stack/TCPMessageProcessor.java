/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;
//ifndef SIMULATION
//
import java.net.Socket;
import java.net.ServerSocket;
//else
/*
import sim.java.net.*;
//endif
*/
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

//ifdef SIMULATION
/*
    protected SimThread thread;
//else
*/
    protected Thread thread;
//endif
//
    

    protected int useCount;
	
    protected int port;
    
    protected int nConnections;
    
    private boolean isRunning;
    
//ifndef SIMULATION
//
    private ServerSocket sock;
//else
/*
    private SimServerSocket sock;
    private SimMessageObject msgObject;
//endif
*/


    /** The SIP Stack Structure.
     */
    protected SIPStack sipStack;
    
    /** Constructor.
     * @param sipStack SIPStack structure.
     * @param port port where this message processor listens.
     */
    protected TCPMessageProcessor(SIPStack sipStack, int port) {
        this.sipStack = sipStack;
	this.port = port;
//ifdef SIMULATION
/*
	this.msgObject = new SimMessageObject();
//endif
*/
    }    
    /**
     * Start the processor.
     */
    public void start() throws IOException {
//ifndef SIMULATION
//
        thread = new Thread(this);
        this.sock = new ServerSocket(this.port,0,sipStack.stackInetAddress);
//else 
/*
	this.sock = new SimServerSocket (sipStack.stackInetAddress,this.port);
	thread = new SimThread(this);
        
//endif
*/
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
//ifndef SIMULATION
//
                synchronized(this) 
//else
/*
		this.msgObject.enterCriticalSection();
		try
//endif
*/
		 {
                    // sipStack.maxConnections == -1 means we are
                    // willing to handle an "infinite" number of
                    // simultaneous connections (no resource limitation).
                    // This is the default behavior.
                    while ( this.isRunning &&
                    sipStack.maxConnections != -1 &&
                    this.nConnections >= sipStack.maxConnections)  {
                        try {
//ifndef SIMULATION
//
                            this.wait();
//else
/*
			    this.msgObject.doWait();
//endif
*/
			   
                            if (! this.isRunning)  return;
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    this.nConnections ++;
                } 
//ifdef SIMULATION
/*
		finally { this.msgObject.leaveCriticalSection(); }
//endif
*/

//ifndef SIMULATION
//
                Socket newsock = sock.accept();
//else
/*
                SimSocket newsock = sock.accept();
//endif
*/
                if (getSIPStack().logWriter.needsLogging) {
                    getSIPStack().logWriter.logMessage(
                    "Accepting new connection!");
                }
                TCPMessageChannel tcpMessageChannel =
                new TCPMessageChannel
                (newsock,sipStack,this);
            } catch (SocketException ex) {
                this.isRunning = false;
            }  catch (IOException ex) {
		// Problem accepting connection.
                if (getSIPStack().logWriter.needsLogging) 
			getSIPStack().logWriter.logException(ex);
		continue;
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
//ifdef SIMULATION
/*
	this.msgObject.doNotify();
//else
*/
        this.notify();
//endif
//

        
    }
    
    
    /** Create and return new TCPMessageChannel for the given host/port.
     */
    public MessageChannel createMessageChannel(HostPort targetHostPort)
    throws IOException {
        return new
        TCPMessageChannel(targetHostPort.getInetAddress(),
        targetHostPort.getPort(), sipStack, this);
    }
    
    public MessageChannel createMessageChannel(InetAddress host,
    int port ) throws IOException {
        return new
        TCPMessageChannel(host,port,sipStack,this);
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

