/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/
package gov.nist.javax.sip.stack;

import java.io.IOException;

import gov.nist.core.*;
import gov.nist.core.net.DefaultNetworkLayer;
import gov.nist.core.net.NetworkLayer;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;

import java.net.*;
import javax.sip.address.*;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/

/**
 * This class defines a SIP Stack. In order to build a SIP server (UAS/UAC or
 * Proxy etc.) you need to extend this class and instantiate it in your
 * application. After you have done so, call
 * <a href="SIPStack.html#createMessageProcessor">createMessageProcessor</a>
 * to create message processors and then start these message processors to
 * get the stack the process messages.
 * This will start the necessary threads that wait for incoming SIP messages.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-08-30 16:04:47 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * 
 * Jeff Keyser suggested that MessageProcessors be accessible and applications
 * should have control over message processors.
 * 
 * IPv6 Support added by Emil Ivov (emil_ivov@yahoo.com)<br/>
 * Network Research Team (http://www-r2.u-strasbg.fr))<br/>
 * Louis Pasteur University - Strasbourg - France<br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */

public abstract class SIPMessageStack {

	protected LogWriter logWriter;

	protected ServerLog serverLog;

	protected boolean tcpFlag;
	protected boolean udpFlag;

//ifdef SIMULATION
/*
	    protected int stackProcessingTime;
//endif
*/

	/**
	 * Class that handles caching of TCP connections.
	 */
	protected IOHandler ioHandler;

	/**
	 * Flag that indicates that the stack is active.
	 */
	protected boolean toExit;

	/**
	 * Bad message log.  The name of a file that stores bum messages for
	 * debugging.
	 */
	protected String badMessageLog;

	/**
	 * Internal flag for debugging
	 */
	protected boolean debugFlag;

	/**
	 * Name of the stack.
	 */
	protected String stackName;

	/**
	 * IP address of stack -- this can be re-written by stun.
	 */
	protected String stackAddress; // My host address.
	protected InetAddress stackInetAddress; // INET address of stack.

	/**
	 * IP address of stack
	 */
	protected String savedStackAddress; // My host address.
	protected InetAddress savedStackInetAddress; // INET address of stack.

	/**
	 * Request factory interface (to be provided by the application)
	 */
	protected StackMessageFactory sipMessageFactory;

	/**
	 * Default UDP port (5060)
	 */
	public static final int DEFAULT_PORT = 5060;

	/**
	 * Router to determine where to forward the request.
	 */
	protected javax.sip.address.Router router;

	/**
	 * start a single processing thread for all UDP messages (otherwise, the
	 * stack will start a new thread for each UDP message).
	 */
	protected int threadPoolSize;

	/**
	 * max number of simultaneous connections.
	 */
	protected int maxConnections;

	/** 
	* Close accept socket on completion.
	*/
	protected boolean cacheServerConnections;
		
	/** Close connect socket on termination.
	*/
	protected boolean cacheClientConnections;



	/** 
	 * Max size of message that can be read from a TCP connection.
	 */
	protected int maxContentLength;

	/** 
	 * Max # of headers that a SIP message can contain. 
	 */
	protected int maxMessageSize;

	/**
	 * A collection of message processors.
	 */
	private Collection messageProcessors;

	protected String stunServerAddress;

	protected int stunServerPort;

	/** Read timeout on TCP incoming sockets -- defines the time between reads for 
	*after delivery of first byte of message.
	*/
	protected int readTimeout;

	protected NetworkLayer networkLayer;
	
	public NetworkLayer getNetworkLayer() {
	    if (networkLayer == null) {
	        return DefaultNetworkLayer.SINGLETON;
	    } else {
	        return networkLayer;
	    }
	}
	
	/**
	 * Log a bad message (invoked when a parse exception arises).
	 *
	 * @param message is a string that contains the bad message to log.
	 */
	public void logBadMessage(String message) {
		if (badMessageLog != null)
			logWriter.logMessage(message, badMessageLog);
	}

	/**
	 * debug log writer.
	 *
	 * @param message is the message to log.
	 */
	public void logMessage(String message) {
		this.logWriter.logMessage(message);
	}

	public LogWriter getLogWriter() {
		return this.logWriter;
	}

	public ServerLog getServerLog() {
		return this.serverLog;
	}


	public int getMaxMessageSize() {
		return this.maxMessageSize;
	}

	/**
	 * Log the stack trace.
	 */
	public void logStackTrace() {
		this.logWriter.logStackTrace();
	}

	/**
	 * Get the file name of the bad message log.
	 *
	 * @return the file where bad messages are logged.
	 *
	 */
	public String getBadMessageLog() {
		return this.badMessageLog;
	}

	/**
	 * Set the flag that instructs the stack to only start a single
	 * thread for sequentially processing incoming udp messages (thus
	 * serializing the processing).
	 * Caution: If the user-defined function called by the
	 * processing thread blocks, then the entire server will block.
	 * This feature was requested by Lamine Brahimi (IBM Zurich).
	 */
	public void setSingleThreaded() {
		this.threadPoolSize = 1;
	}

	/**
	 * Set the thread pool size for processing incoming UDP messages.
	 * Limit the total number of threads for processing udp messages.
	 * Caution: If the user-defined function called by the
	 * processing thread blocks, then the entire server will block.
	 */
	public void setThreadPoolSize(int size) {
		this.threadPoolSize = size;
	}

	/**
	 * Set the max # of simultaneously handled TCP connections.
	 */
	public void setMaxConnections(int nconnections) {
		this.maxConnections = nconnections;
	}

	/**
	 * Get the default route string.
	 * @param sipRequest is the request for which we want to compute
	 * the next hop.
	 */
	public Iterator getNextHop(SIPRequest sipRequest) {
		return router.getNextHops(sipRequest);
	}

	/**
	 * Construcor for the stack. Registers the request and response
	 * factories for the stack.
	 * @param messageFactory User-implemented factory for processing
	 * messages.
	 * @param stackAddress -- IP address or host name of the stack.
	 * @param stackName -- descriptive name for the stack.
	 */

	public SIPMessageStack(
		StackMessageFactory messageFactory,
		String stackAddress,
		String stackName)
		throws UnknownHostException {
		this();
		sipMessageFactory = messageFactory;
		if (stackAddress == null) {
			throw new UnknownHostException("stack Address not set");
		}

		this.stackInetAddress = InetAddress.getByName(stackAddress);

	}

	/**
	 * Set the server Request and response factories.
	 * @param messageFactory User-implemented factory for processing
	 * messages.
	 */
	public void setStackMessageFactory(StackMessageFactory messageFactory) {
		sipMessageFactory = messageFactory;
	}

	/**
	 * Set the descriptive name of the stack.
	 * @param stackName -- descriptive name of the stack.
	 */
	public void setStackName(String stackName) {
		this.stackName = stackName;
	}

	/**
	 * Get the Stack name.
	 *
	 * @return name of the stack.
	 */
	public String getStackName() {
		return this.stackName;
	}

	/**
	 * Set my address.
	 * @param stackAddress -- A string containing the stack address.
	 */
	protected void setHostAddress(String stackAddress)
		throws UnknownHostException {
		if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
			&& stackAddress.trim().charAt(0) != '[')
			this.stackAddress = '[' + stackAddress + ']';
		else
			this.stackAddress = stackAddress;
		this.stackInetAddress = InetAddress.getByName(stackAddress);
	}

	protected void setRealHostAddress(String stackAddress) 
		throws UnknownHostException {
		if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
			&& stackAddress.trim().charAt(0) != '[')
			this.savedStackAddress = '[' + stackAddress + ']';
		else
			this.savedStackAddress = stackAddress;
		this.savedStackInetAddress = InetAddress.getByName(stackAddress);
	}

	/**
	 * Get my address.
	 * @return hostAddress - my host address.
	 */
	public String getHostAddress() {
		return this.stackAddress;
	}

	/**
	 * Get the default next hop from the router.
	 */
	public Hop getNextHop() {
		return (Hop) this.router.getOutboundProxy();

	}

	/**
	 * get port of the message processor (based on the transport). If
	 * multiple ports are enabled for the same transport then the first
	 * one is retrieved.
	 *
	 * @param transport is the transport for which to get the port.
	 */
	public int getPort(String transport) {
		synchronized (messageProcessors) {
			Iterator it = messageProcessors.iterator();
			while (it.hasNext()) {
				MessageProcessor mp = (MessageProcessor) it.next();
				if (mp.getTransport().equalsIgnoreCase(transport))
					return mp.getPort();
			}
			throw new IllegalArgumentException(
				"Transport not supported " + transport);
		}
	}

	/**
	 * Return true if a transport is enabled.
	 *
	 * @param transport is the transport to check.
	 */
	public boolean isTransportEnabled(String transport) {
		synchronized (messageProcessors) {
			Iterator it = messageProcessors.iterator();
			while (it.hasNext()) {
				MessageProcessor mp = (MessageProcessor) it.next();
				if (mp.getTransport().equalsIgnoreCase(transport))
					return true;
			}
			return false;
		}
	}

	/**
	 * Return true if the transport is enabled for a given port.
	 *
	 * @param transport transport to check
	 * @param port 	port to check transport at.
	 */
	public boolean isTransportEnabled(String transport, int port) {
		synchronized (messageProcessors) {
			Iterator it = messageProcessors.iterator();
			while (it.hasNext()) {
				MessageProcessor mp = (MessageProcessor) it.next();
				if (mp.getTransport().equalsIgnoreCase(transport)
					&& mp.getPort() == port)
					return true;
			}
			return false;
		}
	}
	
	/** Reusing the same stack instance.
	*/
	protected void reInit() {
		if (LogWriter.needsLogging)
			logWriter.logMessage("Re-initializing !" ) ;
	
		// Array of message processors.
		messageProcessors = new ArrayList();
		// Handle IO for this process.
		this.ioHandler = new IOHandler(this);
	}

	/**
	 * Default constructor.
	 */
	public SIPMessageStack() {
		this.toExit = false;
		// Set an infinite thread pool size.
		this.threadPoolSize = -1;
		// Close response socket after infinte time.
		// for max performance
		this.cacheServerConnections = true;
		// Close the request socket after infinite time.
		// for max performance
		this.cacheClientConnections = true;
		// Max number of simultaneous connections.
		this.maxConnections = -1;
		// Array of message processors.
		messageProcessors = new ArrayList();
		// Handle IO for this process.
		this.ioHandler = new IOHandler(this);
		// To log debug messages.
		this.logWriter = new LogWriter();
		// Server log file.
		this.serverLog = new ServerLog(this);
		// The read time out is infinite.
		this.readTimeout = -1;

//ifdef SIMULATION
/*
		// Time taken to process the message through the stack from
		// the time of receipt on the wire.
		this.stackProcessingTime = 5;
//endif
*/

	}


	protected void setDebugLogFileName(String logFileName) {
		this.logWriter.setLogFileName(logFileName);
	}

	/**
	 * Generate a new SIPSeverRequest from the given SIPRequest. A
	 * SIPServerRequest is generated by the application
	 * SIPServerRequestFactoryImpl. The application registers the
	 * factory implementation at the time the stack is initialized.
	 * @param siprequest SIPRequest for which we want to generate
	 * thsi SIPServerRequest.
	 * @param msgchan Message channel for the request for which
	 * we want to generate the SIPServerRequest
	 * @return Generated SIPServerRequest.
	 */
	protected ServerRequestInterface newSIPServerRequest(
		SIPRequest siprequest,
		MessageChannel msgchan) {
		return sipMessageFactory.newSIPServerRequest(siprequest, msgchan);
	}

	/**
	 * Generate a new SIPSeverResponse from the given SIPResponse.
	 * @param sipresponse SIPResponse from which the SIPServerResponse
	 * is to be generated. Note - this just calls the factory interface
	 * to do its work. The factory interface is provided by the user.
	 * @param msgchan Message channel for the SIPServerResponse
	 * @return SIPServerResponse generated from this SIP Response
	 */
	protected ServerResponseInterface newSIPServerResponse(
		SIPResponse sipresponse,
		MessageChannel msgchan) {
		return sipMessageFactory.newSIPServerResponse(sipresponse, msgchan);
	}

	/**
	 * Set the router algorithm.
	 * @param router A class that implements the Router interface.
	 */
	public void setRouter(Router router) {
		this.router = router;
	}

	/**
	 * Get the router algorithm.
	 * @return Router router
	 */
	public Router getRouter() {
		return router;
	}

	/**
	 * Get the default route.
	 */
	public javax.sip.address.Hop getDefaultRoute() {
		return this.router.getOutboundProxy();
	}

	/**
	 * Get the route header for this hop.
	 *
	 * @return the route header for the hop.
	 */
	public Route getRouteHeader(Hop hop) {
		HostPort hostPort = new HostPort();
		Host h = new Host(hop.getHost());
		hostPort.setHost(h);
		hostPort.setPort(hop.getPort());
		gov.nist.javax.sip.address.SipUri uri = new SipUri();
		uri.setHostPort(hostPort);
		uri.setScheme("sip");
		try {
			uri.setTransportParam(hop.getTransport());
		} catch (java.text.ParseException ex) {
			InternalErrorHandler.handleException(ex);
		}
		AddressImpl address = new AddressImpl();
		address.setURI(uri);
		Route route = new Route();
		route.setAddress(address);
		return route;

	}

	/**
	 * Get the route header corresponding to the default route.
	 */
	public Route getDefaultRouteHeader() {
		if (router.getOutboundProxy() != null) {
			Hop hop = ((Hop) router.getOutboundProxy());
			return getRouteHeader(hop);
		} else
			return null;
	}

	/**
	 * return the status of the toExit flag.
	 * @return true if the stack object is alive and false otherwise.
	 */
	public boolean isAlive() {
		return !toExit;
	}

	/**
	 * Make the stack close all accept connections and return. This
	 * is useful if you want to start/stop the stack several times from
	 * your application. Caution : use of this function could cause
	 * peculiar bugs as messages are prcessed asynchronously by the stack.
	 */
	public void stopStack() {
		this.toExit = true;
		synchronized (this) {
			this.notifyAll();
		}
		synchronized (this.messageProcessors) {
			// Threads must periodically check this flag.
			MessageProcessor[] processorList;
			processorList = getMessageProcessors();
			for (int processorIndex = 0;
				processorIndex < processorList.length;
				processorIndex++) {
				removeMessageProcessor(processorList[processorIndex]);
			}
			this.ioHandler.closeAll();
			// Let the processing complete.

			try {

//ifndef SIMULATION
//
				Thread.sleep(500);
//else
/*
				                SimThread.sleep((double)500.0);
//endif
*/

			} catch (InterruptedException ex) {
			}
		}
	}

	/**
	 * Adds a new MessageProcessor to the list of running processors
	 * for this SIPStack and starts it. You can use this method
	 * for dynamic stack configuration.
	 *Acknowledgement: This code is contributed by Jeff Keyser.
	 */
	public void addMessageProcessor(MessageProcessor newMessageProcessor)
		throws IOException {
		synchronized (messageProcessors) {
			messageProcessors.add(newMessageProcessor);
			newMessageProcessor.start();
		}
	}

	/**
	 * Removes a MessageProcessor from this SIPStack. Acknowledgement:
	 * Code contributed by Jeff Keyser.
	 * @param oldMessageProcessor
	 */
	public void removeMessageProcessor(MessageProcessor oldMessageProcessor) {
		synchronized (messageProcessors) {
			if (messageProcessors.remove(oldMessageProcessor)) {
				oldMessageProcessor.stop();
			}
		}
	}

	/**
	 * Gets an array of running MessageProcessors on this SIPStack.
	 * Acknowledgement: Jeff Keyser suggested that applications should
	 * have access to the running message processors and contributed
	 * this code.
	 * 
	 * @return an array of running message processors.
	 */
	public MessageProcessor[] getMessageProcessors() {
		synchronized (messageProcessors) {
			return (MessageProcessor[]) messageProcessors.toArray(
				new MessageProcessor[0]);
		}
	}

	/**
	 * Get a message processor for the given transport.
	 */
	public MessageProcessor getMessageProcessor(String transport) {
		synchronized (messageProcessors) {
			Iterator it = messageProcessors.iterator();
			while (it.hasNext()) {
				MessageProcessor mp = (MessageProcessor) it.next();
				if (mp.getTransport().equalsIgnoreCase(transport))
					return mp;
			}
			return null;
		}
	}

	/**
	 * Creates the equivalent of a JAIN listening point and attaches
	 * to the stack.
	 */
	public MessageProcessor createMessageProcessor(int port, String transport)
		throws java.io.IOException {
		if (transport.equalsIgnoreCase("udp")) {
			UDPMessageProcessor udpMessageProcessor =
				new UDPMessageProcessor(this, port);
			this.addMessageProcessor(udpMessageProcessor);
			this.udpFlag = true;
			return udpMessageProcessor;
		} else if (transport.equalsIgnoreCase("tcp")) {
			TCPMessageProcessor tcpMessageProcessor =
				new TCPMessageProcessor(this, port);
			this.addMessageProcessor(tcpMessageProcessor);
			this.tcpFlag = true;
			return tcpMessageProcessor;
		} else {
			throw new IllegalArgumentException("bad transport");
		}

	}

	/**
	 * Set the message factory.
	 *
	 * @param messageFactory -- messageFactory to set.
	 */
	protected void setMessageFactory(StackMessageFactory messageFactory) {
		this.sipMessageFactory = messageFactory;
	}

	/**
	 *  Creates a new MessageChannel for a given Hop.
	 *
	 *  @param nextHop Hop to create a MessageChannel to.
	 *
	 *  @return A MessageChannel to the specified Hop, or null if
	 *  no MessageProcessors support contacting that Hop.
	 *
	 *  @throws UnknwonHostException If the host in the Hop doesn't
	 *  exist.
	 */
	public MessageChannel createMessageChannel(Hop nextHop)
		throws UnknownHostException {
		Host targetHost;
		HostPort targetHostPort;
		Iterator processorIterator;
		MessageProcessor nextProcessor;
		MessageChannel newChannel;

		// Create the host/port of the target hop
		targetHost = new Host();
		targetHost.setHostname(nextHop.getHost());
		targetHostPort = new HostPort();
		targetHostPort.setHost(targetHost);
		targetHostPort.setPort(nextHop.getPort());

		// Search each processor for the correct transport
		newChannel = null;
		processorIterator = messageProcessors.iterator();
		while (processorIterator.hasNext() && newChannel == null) {
			nextProcessor = (MessageProcessor) processorIterator.next();
			// If a processor that supports the correct
			// transport is found,
			if (nextHop
				.getTransport()
				.equalsIgnoreCase(nextProcessor.getTransport())) {
				try {
					// Create a channel to the target
					// host/port
					newChannel =
						nextProcessor.createMessageChannel(targetHostPort);
				} catch (UnknownHostException ex) {
					if (LogWriter.needsLogging)
						logWriter.logException(ex);
					throw ex;
				} catch (IOException e) {
					if (LogWriter.needsLogging)
						logWriter.logException(e);
					// Ignore channel creation error -
					// try next processor
				}
			}
		}
		// Return the newly-created channel
		return newChannel;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2004/06/21 05:42:31  mranga
 * Reviewed by:  mranga
 * more code smithing
 *
 * Revision 1.2  2004/06/21 05:32:21  mranga
 * Submitted by:  
 * Reviewed by:
 *
 * Revision 1.1  2004/06/21 04:59:51  mranga
 * Refactored code - no functional changes.
 *
 * Revision 1.20  2004/05/18 15:26:44  mranga
 * Reviewed by:   mranga
 * Attempted fix at race condition bug. Remove redundant exception (never thrown).
 * Clean up some extraneous junk.
 *
 * Revision 1.19  2004/05/16 14:13:23  mranga
 * Reviewed by:   mranga
 * Fixed the use-count issue reported by Peter Parnes.
 * Added property to prevent against content-length dos attacks.
 *
 * Revision 1.18  2004/05/14 20:20:03  mranga
 *
 * Submitted by:  Dave Stuart
 * Reviewed by:  mranga
 *
 * Stun support hacks -- use the original address specified to bind tcp transport
 * socket.
 *
 * Revision 1.17  2004/04/19 21:51:04  mranga
 * Submitted by:  mranga
 * Reviewed by:  ivov
 * Support for stun.
 *
 * Revision 1.16  2004/04/07 00:19:23  mranga
 * Reviewed by:   mranga
 * Fixes a potential race condition for client transactions.
 * Handle re-invites statefully within an established dialog.
 *
 * Revision 1.15  2004/03/30 15:17:39  mranga
 * Reviewed by:   mranga
 * Added reInitialization for stack in support of applets.
 *
 * Revision 1.14  2004/03/12 23:26:42  mranga
 * Reviewed by:   mranga
 * Fixed a synchronization problem
 *
 * Revision 1.13  2004/03/09 00:34:44  mranga
 * Reviewed by:   mranga
 * Added TCP connection management for client and server side
 * Transactions. See configuration parameter
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false
 * Releases Client TCP Connections after linger time
 *
 * Revision 1.12  2004/03/07 22:25:24  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.11  2004/02/29 00:46:35  mranga
 * Reviewed by:   mranga
 * Added new configuration property to limit max message size for TCP transport.
 * The property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 *
 * Revision 1.10  2004/02/20 16:36:42  mranga
 * Reviewed by:   mranga
 * Minor changes to debug logging -- record the properties with which the stack
 * was created. Be slightly more forgiving when checking for retransmission
 * filter when configuring stack.
 *
 * Revision 1.9  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.8  2004/01/22 13:26:33  sverker
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
