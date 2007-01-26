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
import java.util.*;

/**
 * Sit in a loop waiting for incoming tcp connections and start a
 * new thread to handle each new connection. This is the active
 * object that creates new TCP MessageChannels (one for each new
 * accept socket).  
 *
 * @version 1.2 $Revision: 1.25 $ $Date: 2007-01-26 16:50:44 $
 *
 * @author M. Ranganathan   <br/>
 * Acknowledgement: Jeff Keyser suggested that a
 * Stop mechanism be added to this. Niklas Uhrberg suggested that
 * a means to limit the number of simultaneous active connections
 * should be added. Mike Andrews suggested that the thread be
 * accessible so as to implement clean stop using Thread.join().
 *
 * 
 */
public class TCPMessageProcessor extends MessageProcessor {

	
	protected int nConnections;

	private boolean isRunning;


	private Hashtable tcpMessageChannels;

	private ServerSocket sock;

	protected int useCount;
	
	
	/**
	 * The SIP Stack Structure.
	 */
	protected SIPTransactionStack sipStack;


   
	/**
	 * Constructor.
	 * @param sipStack SIPStack structure.
	 * @param port port where this message processor listens.
	 */
	protected TCPMessageProcessor(InetAddress ipAddress, SIPTransactionStack sipStack, int port) {
	    super( ipAddress,port,"tcp");
	   
		this.sipStack = sipStack;
		
		
		this.tcpMessageChannels = new Hashtable();
	}

	/**
	 * Start the processor.
	 */
	public void start() throws IOException {
		Thread thread = new Thread(this);
		thread.setName("TCPMessageProcessorThread");
		thread.setDaemon(true);
		this.sock = sipStack.getNetworkLayer().createServerSocket(getPort(), 0, getIpAddress());
		if ( getIpAddress().getHostAddress().equals(IN_ADDR_ANY)  ||
				 getIpAddress().getHostAddress().equals(IN6_ADDR_ANY)){
				// Store the address to which we are actually bound
				super.setIpAddress(sock.getInetAddress());
				
		}
		this.isRunning = true;
		thread.start();

	}

	
	

	/**
	 * Run method for the thread that gets created for each accept
	 * socket.
	 */
	public void run() {
		// Accept new connectins on our socket.
		while (this.isRunning) {
			try {
				synchronized (this) {
					// sipStack.maxConnections == -1 means we are
					// willing to handle an "infinite" number of
					// simultaneous connections (no resource limitation).
					// This is the default behavior.
					while (this.isRunning
						&& sipStack.maxConnections != -1
						&& this.nConnections >= sipStack.maxConnections) {
						try {
							this.wait();

							if (!this.isRunning)
								return;
						} catch (InterruptedException ex) {
							break;
						}
					}
					this.nConnections++;
				}

				Socket newsock = sock.accept();
				if (sipStack.isLoggingEnabled()) {
					getSIPStack().logWriter.logDebug(
						"Accepting new connection!");
				}
				// Note that for an incoming message channel, the
				// thread is already running
				TCPMessageChannel tcpMessageChannel =
					new TCPMessageChannel(newsock, sipStack, this);
			} catch (SocketException ex) {
				this.isRunning = false;
			} catch (IOException ex) {
				// Problem accepting connection.
				if (sipStack.isLoggingEnabled())
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
		return "tcp";
	}

	
	/**
	 * Returns the stack.
	 * @return my sip stack.
	 */
	public SIPTransactionStack getSIPStack() {
		return sipStack;
	}

	/**
	 * Stop the message processor.
	 * Feature suggested by Jeff Keyser.
	 */
	public synchronized void stop() {
		isRunning = false;
		//this.listeningPoint = null;
		try {
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Collection en = tcpMessageChannels.values();
		for ( Iterator it = en.iterator(); it.hasNext(); ) {
		       TCPMessageChannel next = 
				(TCPMessageChannel)it.next() ;
			next.close();
		}
		this.notify();
	}


	protected synchronized void remove
		(TCPMessageChannel tcpMessageChannel) {

		String key = tcpMessageChannel.getKey();
		if (sipStack.isLoggingEnabled()) {
		   sipStack.logWriter.logDebug	
		   ( Thread.currentThread() + " removing " + key);
		}

		/** May have been removed already */
		if (tcpMessageChannels.get(key) == tcpMessageChannel) 
			this.tcpMessageChannels.remove(key);
	}



	public synchronized  
		MessageChannel createMessageChannel(HostPort targetHostPort)
		throws IOException {
		String key = MessageChannel.getKey(targetHostPort,"TCP");
		if (tcpMessageChannels.get(key) != null)  {
			return (TCPMessageChannel) 
			this.tcpMessageChannels.get(key);
		} else {
		     TCPMessageChannel retval = new TCPMessageChannel(
			targetHostPort.getInetAddress(),
			targetHostPort.getPort(),
			sipStack,
			this);
		     this.tcpMessageChannels.put(key,retval);
		     retval.isCached = true;
		     if (sipStack.isLoggingEnabled() ) {
			  sipStack.logWriter.logDebug
				("key " + key);
		          sipStack.logWriter.logDebug("Creating " + retval);
		      }
		     return retval;
		}
	}


	protected synchronized  void cacheMessageChannel 
		(TCPMessageChannel messageChannel) {
		String key = messageChannel.getKey();
		TCPMessageChannel currentChannel = 
			(TCPMessageChannel) tcpMessageChannels.get(key);
		if (currentChannel != null)  {
		        if (sipStack.isLoggingEnabled()) 
				sipStack.logWriter.logDebug("Closing " + key);
			currentChannel.close();
		}
		if (sipStack.isLoggingEnabled()) 
			sipStack.logWriter.logDebug("Caching " + key);
	        this.tcpMessageChannels.put(key,messageChannel);

	}

	public  synchronized MessageChannel 
	       createMessageChannel(InetAddress host, int port)
		throws IOException {
		try {
		   String key = MessageChannel.getKey(host,port,"TCP");
		   if (tcpMessageChannels.get(key) != null)  {
			return (TCPMessageChannel) 
				this.tcpMessageChannels.get(key);
		   } else {
		        TCPMessageChannel retval  = new TCPMessageChannel(host, port, sipStack, this);
			this.tcpMessageChannels.put(key,retval);
		        retval.isCached = true;
			if (sipStack.isLoggingEnabled()) {
		        	sipStack.getLogWriter().logDebug("key " + key);
		        	sipStack.getLogWriter().logDebug("Creating " + retval);
			}
			return retval;
		   }
		} catch (UnknownHostException ex) {
			throw new IOException (ex.getMessage());
		}
	}



	/**
	 * TCP can handle an unlimited number of bytes.
	 */
	public int getMaximumMessageSize() {
		return Integer.MAX_VALUE;
	}

	
	public boolean inUse() {
		return this.useCount != 0;
	}

	/**
	 * Default target port for TCP
	 */
	public int getDefaultTargetPort() {
		return 5060;
	}

	/**
	 * TCP is not a secure protocol.
	 */
	public boolean isSecure() {
		return false;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.24  2006/07/13 09:01:01  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
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
 * Revision 1.7  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.6  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.5  2006/05/31 07:47:27  mranga
 * Added a simple server congestion control algorithm.
 *
 * Cleaned up some code.
 *
 * Ranga
 *
 * Revision 1.4  2005/12/05 22:33:07  mranga
 * *** empty log message ***
 *
 * Revision 1.3  2005/11/21 19:20:29  mranga
 * *** empty log message ***
 *
 * Revision 1.2  2005/11/14 22:36:01  mranga
 * Interim update of source code
 *
 * Revision 1.1.1.1  2005/10/04 17:12:36  mranga
 *
 * Import
 *
 *
 * Revision 1.22  2004/12/01 19:05:16  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
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
