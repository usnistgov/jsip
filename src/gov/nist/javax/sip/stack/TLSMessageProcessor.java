/* This class is entirely derived from TCPMessageProcessor,
   by making some minor changes.

                Daniel J. Martinez Manzano <dani@dif.um.es>
*/

/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.stack;
//ifndef SIMULATION
//
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
//else
/*
import sim.java.net.*;
//endif
*/
import java.io.IOException;
import java.net.SocketException;
import gov.nist.core.*;
import java.net.*;
import java.util.*;

/**
 * Sit in a loop waiting for incoming tls connections and start a
 * new thread to handle each new connection. This is the active
 * object that creates new TLS MessageChannels (one for each new
 * accept socket).  
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.1 $ $Date: 2004-10-28 19:02:52 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * Acknowledgement: Jeff Keyser suggested that a
 * Stop mechanism be added to this. Niklas Uhrberg suggested that
 * a means to limit the number of simultaneous active connections
 * should be added. Mike Andrews suggested that the thread be
 * accessible so as to implement clean stop using Thread.join().
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class TLSMessageProcessor extends MessageProcessor {

//ifdef SIMULATION
/*
	protected SimThread thread;
//else
*/
	protected Thread thread;
//endif
//


	protected int port;

	protected int nConnections;

	private boolean isRunning;


	private Hashtable tlsMessageChannels;

//ifndef SIMULATION
//
	private SSLServerSocket sock;
//else
/*
	private SimServerSocket sock;
	private SimMessageObject msgObject;
//endif
*/

	protected int useCount=0;

	/**
	 * The SIP Stack Structure.
	 */
	protected SIPMessageStack sipStack;

	/**
	 * Constructor.
	 * @param sipStack SIPStack structure.
	 * @param port port where this message processor listens.
	 */
	protected TLSMessageProcessor(SIPMessageStack sipStack, int port) {
		this.sipStack = sipStack;
		this.port = port;
		this.tlsMessageChannels = new Hashtable();
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
		thread.setName("TLSMessageProcessorThread");
		thread.setDaemon(true);
		this.sock = sipStack.getNetworkLayer().createSSLServerSocket(this.port, 0, sipStack.savedStackInetAddress);
//else 
/*
		this.sock = new SimServerSocket (sipStack.stackInetAddress,this.port);
		thread = new SimThread(this);
//endif
*/
		this.isRunning = true;
		thread.start();

	}

	
	/**
	* Return our thread.
	*
	*@return -- our thread. This is used for joining 
	*/
	public Thread getThread() {
		return this.thread;
	}

	/**
	 * Run method for the thread that gets created for each accept
	 * socket.
	 */
	public void run() {
		// Accept new connectins on our socket.
		while (this.isRunning) {
			try {
//ifndef SIMULATION
//
				synchronized (this)
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
					while (this.isRunning
						&& sipStack.maxConnections != -1
						&& this.nConnections >= sipStack.maxConnections) {
						try {
//ifndef SIMULATION
//
							this.wait();
//else
/*
				 			this.msgObject.doWait();
//endif
*/

							if (!this.isRunning)
								return;
						} catch (InterruptedException ex) {
							break;
						}
					}
					this.nConnections++;
				}
//ifdef SIMULATION
/*
				finally { this.msgObject.leaveCriticalSection(); }
//endif
*/

//ifndef SIMULATION
//
				SSLSocket newsock = (SSLSocket) sock.accept();
//else
/*
				SimSocket newsock = sock.accept();
//endif
*/
				if (LogWriter.needsLogging) {
					getSIPStack().logWriter.logMessage(
						"Accepting new connection!");
				}
				// Note that for an incoming message channel, the
				// thread is already running
				TLSMessageChannel tlsMessageChannel =
					new TLSMessageChannel(newsock, sipStack, this);
			} catch (SocketException ex) {
				this.isRunning = false;
			} catch (IOException ex) {
				// Problem accepting connection.
				if (LogWriter.needsLogging)
					getSIPStack().logWriter.logException(ex);
				continue;
			} catch (Exception ex) {
				InternalErrorHandler.handleException(ex);
			}
		}
	}

	/**
	 * Return the transport string.
	 * @return the transport string
	 */
	public String getTransport() {
		return "tls";
	}

	/**
	 * Returns the port that we are listening on.
	 * @return Port address for the tcp accept.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Returns the stack.
	 * @return my sip stack.
	 */
	public SIPMessageStack getSIPStack() {
		return sipStack;
	}

	/**
	 * Stop the message processor.
	 * Feature suggested by Jeff Keyser.
	 */
	public synchronized void stop() {
		isRunning = false;
		this.listeningPoint = null;
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collection en = tlsMessageChannels.values();
		for ( Iterator it = en.iterator(); it.hasNext(); ) {
		       TLSMessageChannel next = 
				(TLSMessageChannel)it.next() ;
			next.close();
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


	protected synchronized void remove
		(TLSMessageChannel tlsMessageChannel) {

		String key = tlsMessageChannel.getKey();
		if (LogWriter.needsLogging) {
		   sipStack.logWriter.logMessage	
		   ( Thread.currentThread() + " removing " + key);
		}

		/** May have been removed already */
		if (tlsMessageChannels.get(key) == tlsMessageChannel) 
			this.tlsMessageChannels.remove(key);
	}



	public synchronized  
		MessageChannel createMessageChannel(HostPort targetHostPort)
		throws IOException {
		String key = MessageChannel.getKey(targetHostPort,"TLS");
		if (tlsMessageChannels.get(key) != null)  {
			return (TLSMessageChannel) 
			this.tlsMessageChannels.get(key);
		} else {
		     TLSMessageChannel retval = new TLSMessageChannel(
			targetHostPort.getInetAddress(),
			targetHostPort.getPort(),
			sipStack,
			this);
		     this.tlsMessageChannels.put(key,retval);
		     retval.isCached = true;
		     if (LogWriter.needsLogging ) {
			  sipStack.logWriter.logMessage
				("key " + key);
		          sipStack.logWriter.logMessage("Creating " + retval);
		      }
		     return retval;
		}
	}


	protected synchronized  void cacheMessageChannel 
		(TLSMessageChannel messageChannel) {
		String key = messageChannel.getKey();
		TLSMessageChannel currentChannel = 
			(TLSMessageChannel) tlsMessageChannels.get(key);
		if (currentChannel != null)  {
		        if (LogWriter.needsLogging) 
				sipStack.logWriter.logMessage("Closing " + key);
			currentChannel.close();
		}
		if (LogWriter.needsLogging) 
			sipStack.logWriter.logMessage("Caching " + key);
	        this.tlsMessageChannels.put(key,messageChannel);

	}

	public  synchronized MessageChannel 
	       createMessageChannel(InetAddress host, int port)
		throws IOException {
		try {
		   String key = MessageChannel.getKey(host,port,"TLS");
		   if (tlsMessageChannels.get(key) != null)  {
			return (TLSMessageChannel) 
				this.tlsMessageChannels.get(key);
		   } else {
		        TLSMessageChannel retval  = new TLSMessageChannel(host, port, sipStack, this);
			this.tlsMessageChannels.put(key,retval);
		        retval.isCached = true;
			if (LogWriter.needsLogging) {
		        	sipStack.logMessage("key " + key);
		        	sipStack.logMessage("Creating " + retval);
			}
			return retval;
		   }
		} catch (UnknownHostException ex) {
			throw new IOException (ex.getMessage());
		}
	}



	/**
	 * TLS can handle an unlimited number of bytes.
	 */
	public int getMaximumMessageSize() {
		return Integer.MAX_VALUE;
	}

	/**
	 * TLS NAPTR service name.
	 */
	public String getNAPTRService() {
		return "SIPS+D2T";
	}

	/**
	 * TLS SRV prefix.
	 */
	public String getSRVPrefix() {
		return "_sip._tls.";
	}

	public boolean inUse() {
		return this.useCount != 0;
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
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.21  2004/09/04 14:59:54  mranga
 * Reviewed by:   mranga
 *
 * Added a method to expose the Thread for the message processors so that
 * stack.stop() can join to wait for the threads to die rather than sleep().
 * Feature requested by Mike Andrews.
 *
 * Revision 1.20  2004/08/30 16:04:47  mranga
 * Submitted by:  Mike Andrews
 * Reviewed by:   mranga
 *
 * Added a network layer.
 *
 * Revision 1.19  2004/08/23 23:56:21  mranga
 * Reviewed by:   mranga
 * forgot to set isDaemon in one or two places where threads were being
 * created and cleaned up some minor junk.
 *
 * Revision 1.18  2004/06/21 04:59:53  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.17  2004/05/14 20:20:03  mranga
 *
 * Submitted by:  Dave Stuart
 * Reviewed by:  mranga
 *
 * Stun support hacks -- use the original address specified to bind tcp transport
 * socket.
 *
 * Revision 1.16  2004/03/30 16:40:30  mranga
 * Reviewed by:   mranga
 * more tweaks to reference counting for cleanup.
 *
 * Revision 1.15  2004/03/30 15:38:18  mranga
 * Reviewed by:   mranga
 * Name the threads so as to facilitate debugging.
 *
 * Revision 1.14  2004/03/25 19:01:44  mranga
 * Reviewed by:   mranga
 * check for key before removing it from cache
 *
 * Revision 1.13  2004/03/25 18:08:15  mranga
 * Reviewed by:   mranga
 * Fix connection caching for ill behaved clients which connect multiple times
 * for the same incoming request.
 *
 * Revision 1.12  2004/03/25 15:15:05  mranga
 * Reviewed by:   mranga
 * option to log message content added.
 *
 * Revision 1.11  2004/03/19 23:41:30  mranga
 * Reviewed by:   mranga
 * Fixed connection and thread caching.
 *
 * Revision 1.10  2004/03/19 17:06:20  mranga
 * Reviewed by:   mranga
 * Fixed some stack cleanup issues. Stack should release all resources when
 * finalized.
 *
 * Revision 1.9  2004/01/22 18:39:42  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.8  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.7  2004/01/22 13:26:33  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
