package gov.nist.javax.sip;

import java.util.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import gov.nist.javax.sip.stack.*;
import java.lang.reflect.*;
import gov.nist.core.*;
import gov.nist.core.net.NetworkLayer;

//ifdef SIMULATION
/*
import sim.java.net.*;
//endif
*/

/**
 * Implementation of SipStack.
 *
 * The JAIN-SIP stack is initialized by a set of properties (see the JAIN
 * SIP documentation for an explanation of these properties).
 * In addition to these, the following are meaningful properties for 
 * the NIST SIP stack (specify these in the property array when you create
 * the JAIN-SIP statck).:
 *<ul>
 *
 *<li><b>gov.nist.javax.sip.TRACE_LEVEL = integer </b><br/>
 *Currently only 16 and 32 is meaningful. 
 *If this is set to 16 or above, then incoming
 *valid messages are  logged in SERVER_LOG. If you set this to 32 and 
 *specify a DEBUG_LOG then vast amounts of trace information will be dumped
 *in to the specified DEBUG_LOG.  The server log accumulates the signaling
 *trace. 
 *<a href="{@docRoot}/tools/tracesviewer/tracesviewer.html">
 *This can be viewed using the trace viewer tool .</a>
 *Please send us both the server log and debug log 
 * when reporting non-obvious problems.</li>
 *
 *<li><b>gov.nist.javax.sip.SERVER_LOG = fileName </b><br/>
 * Log valid incoming messages here. If this is left null AND the 
 * TRACE_LEVEL is above 16 then the messages are printed to stdout.
 * Otherwise messages are logged in a format that can later be viewed
 * using the trace viewer application which is located in 
 * the tools/tracesviewer directory. 
 *<font color=red> Mail this to us with bug reports.  </font>
 * </li>
 *
 *<li><b>gov.nist.javax.sip.LOG_MESSAGE_CONTENT = true|false </b><br/>
 * Set true if you want to capture content into the log. Default is false.
 * A bad idea to log content if you are using SIP to push a lot of 
 * bytes through TCP.
 *</li>
 *    
 *<li><b>gov.nist.javax.sip.BAD_MESSAGE_LOG = fileName </b><br/>
 *  Messages that do not contain the required headers are dropped.
 *  This file indicates where the bad (unprocessable) messages go. 
 *  Default is null (bad messages are not logged in their own file). </li>
 *
 *    
 *<li><b>gov.nist.javax.sip.DEBUG_LOG = fileName </b><br/>
 *  Where the debug log goes. 
 *	<font color=red> Mail this to us with bug reports.  </font>
 *</li>
 *
 *<li><b>gov.nist.javax.sip.MAX_MESSAGE_SIZE = integer</b> <br/>
 * Maximum size of content that a TCP connection can read. Must be
 * at least 4K. Default is "infinity" -- ie. no limit.
 * This is to prevent DOS attacks launched by writing to a
 * TCP connection until the server chokes.
 *</li>
 *
 *
 *<li><b>gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS = integer</b> <br/>
 *   Max number of open SERVER transactions in the transaction table - incoming
 *  Requests that have the capability to create ServerTransactions will
 *  not be processed if server transaction table exceeds this size
 *  (default value is "infinity"). </li>
 *
 *<li><b>gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS = [true|false] </b> <br/>
 *  Default value is true. Setting this to true makes the Stack close the server socket after
 *  a Server Transaction goes to the TERMINATED state. This allows a server to protectect against 
 *  TCP based Denial of Service attacks launched by clients (ie. initiate hundreds of client gransactions).
 *  If false (default action), the stack will keep the socket open so as to maximize performance at 
 *   the expense of Thread and memory resources - leaving itself open to DOS attacks. 
 *</li>
 *
 *<li><b>gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS = [true|false] </b> <br/>
 *  Default value is true. Setting this to true makes the Stack 
 *  close the server socket aftera Client Transaction goes to the 
 * TERMINATED state. This allows a client release any buffers
 *  threads and socket connections associated with a client 
 *  transaction after the transaction has
 *  terminated at the expense of performance.
 *</li>
 *
 *<li> <b>gov.nist.javax.sip.THREAD_POOL_SIZE = integer </b> <br/>
 *  Concurrency control for number of simultaneous active threads.
 *  If unspecificed, the default is "infinity".
 *  This feature is useful if you are trying to build a container.
 * <ul>
 * <li>
 *  <li> If this is not specified, <b> and the listener is re-entrant</b>,  
 *   each event delivered to the listener is run in the context of a 
 *   new thread. </li>
 *  <li>If this is specified and the listener is re-entrant, 
 *   then the stack will run the listener using a 
 *   thread from the thread pool.  This allows you to manage the 
 *   level of concurrency to a fixed maximum. Threads are pre-allocated
 *   when the stack is instantiated.</li>
 *  <li> If this is specified and the listener is not re-entrant, 
 *   then the stack will use the thread pool thread from this pool to
 *   parse and manage the state machine but will run the listener in
 *   its own thread.
 * </li>
 *</ul>
 *
 *<li> <b>gov.nist.javax.sip.REENTRANT_LISTENER = true|false </b> <br/>
 *  Default is false. Set to true if the listener is re-entrant. 
 * If the listener is re-entrant then the stack manages a thread pool
 * and synchronously calls the listener from the same thread which read the
 * message. Multiple transactions may concurrently receive messages and
 * this will result in multiple threads being active in the listener at
 * the same time. The listener has to be written with this in mind.
 * </li>
 *
 *<li> <b>gov.nist.javax.sip.MAX_CONNECTIONS = integer </b> <br/>
 *   Max number of simultaneous TCP connections handled by stack. 
 *  (Was mis-spelled - Documentation bug fix by Bob Johnson)</li>
 *
 *
 *<li> <b>gov.nist.javax.sip.READ_TIMEOUT = integer </b> <br/>
 * This is relevant for incoming TCP connections to prevent starvation at
 * the server.
 * This defines the timeout in miliseconds between successive reads after the 
 * first byte of a SIP message is read by the stack. All the sip headers
 * must be delivered in this interval and each successive buffer must be
 * of the content  delivered in this interval.
 * Default value is -1  (ie. the stack is wide open to starvation attacks) and
 * the client can be as slow as it wants to be.
 *</li>
 *
 *<li> <b>gov.nist.javax.sip.NETWORK_LAYER = classpath </b> <br/>
 * This is an EXPERIMENTAL property (still under active devlopment).
 * Defines a network layer that allows a client to have control over socket
 * allocations and monitoring of socket activity. A network layer should
 * implement gov.nist.core.net.NetworkLayer. The default implementation simply
 * acts as a wrapper for the standard java.net socket layer. This functionality
 * is still under active development (may be extended to support security and
 * other features).
 *</li>
 *
 * <li> <b> gov.nist.javax.sip.STRIP_ROUTE_HEADER = [ true| false ] </b>
 * If set to true (default behavior), 
 * stack will strip topmpost route header on an incoming request
 * if the route header matches the listening point on which it was received.
 * If set to false, the route header is passed up to the application and it is
 * the application layer's responsibility to strip the route header. 
 *</li>
 *
 * <li> <b> javax.sip.AUTOMATIC_DIALOG_SUPPORT = [ true | false ] </b>
 * <b> This is a planned feature for the next version of the spec </b>
 * if set to true JAIN-SIP provides automatic dialog suport 
 * (this is the default). If set to true (default) the dialog is created
 * by the stack automatically when the request is handled statefully.
 * If set to false, the stack will not create a dialog for the application
 * implicitly. In this case the stack is responsible for creating and 
 * associating the dialog.
 *
 * </li>
 *
 *</ul>
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.37 $ $Date: 2004-10-01 16:05:08 $
 * 
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 */
public class SipStackImpl
	extends SIPTransactionStack
	implements javax.sip.SipStack {

	private Hashtable listeningPoints;
	private LinkedList sipProviders;

	private String outboundProxy;
	protected String routerPath;

	protected EventScanner eventScanner;
	// All the methods of the listener must be synchronized for
	// this to work.
	protected boolean reEntrantListener;

	// Flag to indicate whether stack or application will handle
	// removal of topmost route header.
	protected boolean stripRouteHeader;

	// Flag to indicate whether the stack will provide dialog
	// support.
	protected boolean dialogSupport;


	/** Creates a new instance of SipStackImpl.
	*/

	protected SipStackImpl() {
		super();
		NistSipMessageFactoryImpl msgFactory =
			new NistSipMessageFactoryImpl(this);
		super.setMessageFactory(msgFactory);
		this.listeningPoints = new Hashtable();
		this.sipProviders = new LinkedList();
		this.eventScanner = new EventScanner(this);
	}

	/** ReInitialize the stack instance.
	*/
	protected  void reInit() {
		super.reInit();
		this.listeningPoints = new Hashtable();
		this.sipProviders = new LinkedList();
		this.eventScanner = new EventScanner(this);
	}

	public SipStackImpl(Properties configurationProperties)
		throws PeerUnavailableException {
		this();
		String address =
			configurationProperties.getProperty("javax.sip.IP_ADDRESS");
		try {
			/** Retrieve the stack IP address */
			if (address == null)
				throw new PeerUnavailableException("address not specified");
			super.setHostAddress(address);
			/** This is for STUN support  --  Stun overwrites the original adderess*/
			super.setRealHostAddress(address);
		} catch (java.net.UnknownHostException ex) {
			throw new PeerUnavailableException("bad address " + address);
		}

		/** Retrieve the stack name */
		String name =
			configurationProperties.getProperty("javax.sip.STACK_NAME");
		if (name == null)
			throw new PeerUnavailableException("stack name is missing");
		super.setStackName(name);

		/** Retrieve the router path */
		String routerPath =
			configurationProperties.getProperty("javax.sip.ROUTER_PATH");
		if (routerPath == null)
			routerPath = "gov.nist.javax.sip.stack.DefaultRouter";
		String outboundProxy =
			configurationProperties.getProperty("javax.sip.OUTBOUND_PROXY");
		try {
			Class routerClass = Class.forName(routerPath);
			Class[] constructorArgs = new Class[2];
			constructorArgs[0] = javax.sip.SipStack.class;
			constructorArgs[1] = new String().getClass();
			Constructor cons = routerClass.getConstructor(constructorArgs);
			Object[] args = new Object[2];
			args[0] = (SipStack) this;
			args[1] = outboundProxy;
			Router router = (Router) cons.newInstance(args);
			super.setRouter(router);
		} catch (InvocationTargetException ex1) {
			throw new PeerUnavailableException(
				"Cound not instantiate router - check constructor",
				ex1);
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new PeerUnavailableException(
				"Could not instantiate router",
				ex);
		}

		/* gets the NetworkLayer implementation, if any */
		
		final String NETWORK_LAYER_KEY = "gov.nist.javax.sip.NETWORK_LAYER";
		
		if (configurationProperties.containsKey(NETWORK_LAYER_KEY)) {
		    String path = configurationProperties.getProperty(NETWORK_LAYER_KEY);
		    try {
		        Class clazz = Class.forName(path);
		        Constructor c = clazz.getConstructor(new Class[0]);
		        networkLayer = (NetworkLayer) c.newInstance(new Object[0]);
		    } catch (Exception e) {
		        throw new PeerUnavailableException("can't find or instantiate NetworkLayer implementation: " + path);
		    }
		} 
		
		
		/* Retrieve the EXTENSION Methods. These are used for instantiation
		* of Dialogs.
		*/
		String extensionMethods =
			configurationProperties.getProperty("javax.sip.EXTENSION_METHODS");

		if (extensionMethods != null) {
			java.util.StringTokenizer st =
				new java.util.StringTokenizer(extensionMethods);
			LinkedList ll = new LinkedList();
			while (st.hasMoreTokens()) {
				String em = st.nextToken(":");
				if (em.equalsIgnoreCase(Request.BYE)
					|| em.equalsIgnoreCase(Request.ACK)
					|| em.equalsIgnoreCase(Request.OPTIONS))
					throw new PeerUnavailableException(
						"Bad extension method " + em);
				else
					this.addExtensionMethod(em);
			}
		}

		/*Set the retransmission filter.
		*/
		String rf =
			configurationProperties.getProperty(
				"javax.sip.RETRANSMISSION_FILTER");
		if (rf != null
			&& ("true".equalsIgnoreCase(rf.trim()) 
			|| "on".equalsIgnoreCase(rf.trim()))) {
			this.retransmissionFilter = true;
		}

		/* Following are NIST-SIP only features.  */
		String debugLog =
			configurationProperties.getProperty("gov.nist.javax.sip.DEBUG_LOG");

		if (debugLog != null)
			setDebugLogFileName(debugLog);

		String logLevel =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.TRACE_LEVEL");

		if (logLevel != null) {
			try {
				int ll = Integer.parseInt(logLevel);
				if (ll == 32)
					LogWriter.needsLogging = true;
				serverLog.setTraceLevel(ll);
			} catch (NumberFormatException ex) {
				System.out.println("WARNING Bad integer " + logLevel);
				System.out.println("logging dislabled ");
				serverLog.setTraceLevel(0);
			}
		}

		String badMessageLog =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.BAD_MESSAGE_LOG");
		if (badMessageLog != null)
			super.badMessageLog = badMessageLog;

		String serverLog =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.SERVER_LOG");
		if (serverLog != null) 
		     super.serverLog.setProperties (configurationProperties);
		super.serverLog.checkLogFile();
		String maxConnections =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.MAX_CONNECTIONS");
		if (maxConnections != null) {
			try {
				this.maxConnections = new Integer(maxConnections).intValue();
			} catch (NumberFormatException ex) {
				System.out.println(
					"max connections - bad value " + ex.getMessage());
			}
		}

		
		String threadPoolSize =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.THREAD_POOL_SIZE");
		if (threadPoolSize != null) {
			try {
				this.threadPoolSize = new Integer(threadPoolSize).intValue();
			} catch (NumberFormatException ex) {
				System.out.println(
					"thread pool size - bad value " + ex.getMessage());
			}
		}

		String transactionTableSize =
			configurationProperties.getProperty(
				"gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
		if (transactionTableSize != null) {
			try {
				this.transactionTableSize =
					new Integer(transactionTableSize).intValue();
			} catch (NumberFormatException ex) {
				System.out.println(
					"transaction table size - bad value " + ex.getMessage());
			}
		}

		super.cacheServerConnections = true;
                String flag = 
		    configurationProperties.getProperty
		     ("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS");

		if (flag != null && "false".equalsIgnoreCase(flag.trim())) {
		        super.cacheServerConnections = false;
		}

		super.cacheClientConnections = true;
                String cacheflag = 
		    configurationProperties.getProperty
		     ("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS");

		if (cacheflag != null && "false".equalsIgnoreCase(cacheflag.trim())) {
		        super.cacheClientConnections = false;
		}
                
                String readTimeout = configurationProperties.getProperty 
                                        ("gov.nist.javax.sip.READ_TIMEOUT");
                 if (readTimeout != null) {
                       try {
                           
                            int rt = Integer.parseInt(readTimeout);
                            if (rt >= 100 ) {
                                 super.readTimeout = rt;
                            }else {
                                 System.out.println("Value too low " + readTimeout);
                            }
                       } catch (NumberFormatException nfe) {
                           // Ignore.
                           System.out.println("Bad read timeout " + readTimeout);
                       }
                 }

		// Get the address of the stun server.

//ifndef SIMULATION
//
	        String stunAddr = 
			configurationProperties.getProperty(
				"gov.nist.javax.sip.STUN_SERVER");
       		if (stunAddr != null) {
           	  int k = stunAddr.indexOf(':');
           	  if  ( k  == -1) {
              		super.stunServerAddress = stunAddr;
              		super.stunServerPort = 3478;
           	   } else  {
              		super.stunServerAddress = stunAddr.substring(0, k); 
			// Bug fix by Dave Stuart
              		String portString = stunAddr.substring(k+1).trim();
           		try {
               		   super.stunServerPort = Integer.parseInt( portString);
           		} catch (NumberFormatException ex) {
               			super.stunServerPort = 3478;
           		}
          	   }
		}

//endif
//

		String maxMsgSize = configurationProperties.getProperty(
				"gov.nist.javax.sip.MAX_MESSAGE_SIZE");

		try {
		   if (maxMsgSize != null) {
			super.maxMessageSize = new Integer(maxMsgSize).intValue();
		        if (super.maxMessageSize < 4096) 
				super.maxMessageSize = 4096;
		   } else  {
			// Allow for "infinite" size of message
			super.maxMessageSize = 0;
		   }
		} catch (NumberFormatException ex) {
				System.out.println(
					"maxMessageSize - bad value " + ex.getMessage());
		}

	 	String rel = configurationProperties.getProperty("gov.nist.javax.sip.REENTRANT_LISTENER");
	        this.reEntrantListener = (rel != null && "true".equals(rel));

		String flagStr = configurationProperties.getProperty("gov.nist.javax.sip.STRIP_ROUTE_HEADER");
		this.stripRouteHeader = true;
		this.stripRouteHeader = (flagStr == null? true : "true".equals(flagStr));

		// flag to indicate whether the stack will provide dialog support.
		String dialogSupportStr = configurationProperties.getProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT");
		
		this.dialogSupport = (dialogSupportStr == null ? true : !("false".equals(dialogSupportStr)));

		
//ifdef SIMULATION
/*
		SimProcess.hold((double) 100);
//endif
*/

	}

	/** Creates a new peer ListeningPoint on this SipStack on a specified
	 * host, port and transport and returns a reference to the newly created
	 * ListeningPoint object. The newly created ListeningPoint is implicitly
	 * attached to this SipStack upon execution of this method, by adding the
	 * ListeningPoint to the {@link SipStack#getListeningPoints()} of this
	 * SipStack, once it has been successfully created.
	 *
	 * @return The peer ListeningPoint attached to this SipStack.
	 * @param port The port of the new ListeningPoint.
	 * @param transport The transport of the new ListeningPoint.
	 * SipStack.
	 */
	public synchronized ListeningPoint createListeningPoint(
		int port,
		String transport)
		throws TransportNotSupportedException, InvalidArgumentException {
		if (transport == null)
			throw new NullPointerException("null transport");
		if (port <= 0)
			throw new InvalidArgumentException("bad port");
		if (!transport.equalsIgnoreCase("UDP")
			&& !transport.equalsIgnoreCase("TCP"))
			throw new TransportNotSupportedException(
				"bad transport " + transport);

		/** Reusing an old stack instance */
		if (! this.isAlive()) {
			this.toExit = false;
			this.reInit();
		}

		String key =
			ListeningPointImpl.makeKey(super.stackAddress, port, transport);
//ifdef SIMULATION
/*
		System.out.println("key = " + key);
//endif
*/

		ListeningPointImpl lip = (ListeningPointImpl) listeningPoints.get(key);
		if (lip != null) {
			return lip;
		} else {
			try {
				MessageProcessor messageProcessor =
					this.createMessageProcessor(port, transport);
				//System.out.println("createMessageProcessor " + port + "/"
				//+ transport);
				lip = new ListeningPointImpl(this, port, transport);
				lip.messageProcessor = messageProcessor;
				messageProcessor.setListeningPoint(lip);
				this.listeningPoints.put(key, lip);
				return (ListeningPoint) lip;
			} catch (java.io.IOException ex) {
				throw new InvalidArgumentException(ex.getMessage());
			}
		}
	}

	/** Creates a new peer SipProvider on this SipStack on a specified
	 * ListeningPoint and returns a reference to the newly created SipProvider
	 * object. The newly created SipProvider is implicitly attached to this
	 * SipStack upon execution of this method, by adding the SipProvider to the
	 * {@link SipStack#getSipProviders()} of this SipStack, once it has been
	 * successfully created.
	 *
	 * @return The peer SipProvider attached to this SipStack on the specified
	 * ListeningPoint.
	 * @param listeningPoint the ListeningPoint the SipProvider is to
	 * be attached to in order to send and Receive messages.
	 * @throws ListeningPointUnavailableException thrown if another
	 * SipProvider is already using the ListeningPoint.
	 */
	public SipProvider createSipProvider(ListeningPoint listeningPoint)
		throws ObjectInUseException {
		if (listeningPoint == null)
			throw new NullPointerException("null listeningPoint");
		ListeningPointImpl listeningPointImpl =
			(ListeningPointImpl) listeningPoint;
		if (listeningPointImpl.sipProviderImpl != null)
			throw new ObjectInUseException("Provider already attached!");

		SipProviderImpl provider = new SipProviderImpl(this.eventScanner);
		provider.setSipStack(this);
		provider.setListeningPoint(listeningPointImpl);
		listeningPointImpl.sipProviderImpl = provider;
		this.sipProviders.add(provider);
		return provider;
	}

	/** Deletes the specified peer ListeningPoint attached to this SipStack. The
	 * specified ListeningPoint is implicitly detached from this SipStack upon
	 * execution of this method, by removing the ListeningPoint from the
	 * {@link SipStack#getListeningPoints()} of this SipStack.
	 *
	 * @param listeningPoint the peer SipProvider to be deleted from
	 * this SipStack.
	 * @exception ObjectInUseException thrown if the specified peer
	 * ListeningPoint cannot be deleted because the peer ListeningPoint is
	 * currently in use.
	 *
	 * @since v1.1
	 */
	public void deleteListeningPoint(ListeningPoint listeningPoint)
		throws ObjectInUseException {
		if (listeningPoint == null)
			throw new NullPointerException("null listeningPoint arg");
		ListeningPointImpl lip = (ListeningPointImpl) listeningPoint;
		// Stop the message processing thread in the listening point.
		super.removeMessageProcessor(lip.messageProcessor);
		String key = lip.getKey();
		this.listeningPoints.remove(key);

	}

	/** Deletes the specified peer SipProvider attached to this SipStack. The
	 * specified SipProvider is implicitly detached from this SipStack upon
	 * execution of this method, by removing the SipProvider from the
	 * {@link SipStack#getSipProviders()} of this SipStack. Deletion of a
	 * SipProvider does not automatically delete the ListeningPoint from the
	 * SipStack.
	 *
	 * @param sipProvider the peer SipProvider to be deleted from
	 * this SipStack.
	 * @exception ObjectInUseException thrown if the specified peer
	 * SipProvider cannot be deleted because the peer SipProvider is currently
	 * in use.
	 *
	 */
	public void deleteSipProvider(SipProvider sipProvider)
		throws ObjectInUseException {

		if (sipProvider == null)
			throw new NullPointerException("null provider arg");
		SipProviderImpl sipProviderImpl = (SipProviderImpl) sipProvider;
		if (sipProviderImpl.listeningPoint.messageProcessor.inUse()) {
			throw new ObjectInUseException("Provider in use");
		}
		sipProviderImpl.sipListener = null;
		//Bug reported by Rafael Barriuso
		sipProviderImpl.stop();
		sipProviders.remove(sipProvider);
		if (sipProviders.isEmpty()) {
			this.stopStack();
		}
	}

	/** Gets the IP Address that identifies this SipStack instance. Every Sip
	 * Stack object must have an IP Address and only a single SipStack object
	 * can service a single IP Address. This value is set using the Properties
	 * object passed to the {@link SipFactory#createSipStack(Properties)} method upon
	 * creation of the SIP Stack object.
	 *
	 * @return a string identifing the IP Address
	 * @since v1.1
	 */
	public String getIPAddress() {
		return super.getHostAddress();
	}

	/** Returns an Iterator of existing ListeningPoints created by this
	 * SipStackImpl. All of the peer SipProviders of this SipStack will be
	 * proprietary objects belonging to the same stack vendor.
	 *
	 * @return an Iterator containing all existing peer ListeningPoints created
	 * by this SipStack. Returns an empty Iterator if no ListeningPoints exist.
	 */
	public java.util.Iterator getListeningPoints() {
		return this.listeningPoints.values().iterator();
	}

	/** Get the listening point for a given transport and port.
	 *
	 */
	public ListeningPointImpl getListeningPoint(int port, String transport) {
		String key =
			ListeningPointImpl.makeKey(super.stackAddress, port, transport);
		return (ListeningPointImpl) listeningPoints.get(key);
	}

	/** get the outbound proxy specification. Return null if no outbound
	 * proxy is specified.
	 */
	public String getOutboundProxy() {
		return this.outboundProxy;
	}

	/** This method returns the value of the retransmission filter helper
	 * function for User Agent Client and User Agent Server applications. This
	 * value is set using the Properties object passed to the
	 * {@link SipFactory#createSipStack(Properties)} method upon creation of the SIP Stack
	 * object.
	 * <p>
	 * The default value of the retransmission filter boolean is <var>false</var>.
	 * When retransmissions are handled by the SipProvider the application will
	 * not receive {@link Timeout#RETRANSMIT} notifications encapsulated in
	 * {@link javax.sip.TimeoutEvent}'s. However an application will get
	 * notified when a the underlying transaction expired with
	 * {@link Timeout#TRANSACTION} notifications encapsulated in a
	 * {@link javax.sip.TimeoutEvent}.</p>
	 *
	 * @return the value of the retransmission filter, true if the filter
	 * is set false otherwise.
	 * @since v1.1
	 */
	public boolean isRetransmissionFilterActive() {
		return this.retransmissionFilter;
	}

	/** Gets the Router object that identifies the default 
	 * Routing policy of this
	 * SipStack. It also provides means to set an outbound proxy. This value is
	 * set using the Properties object passed to the
	 * {@link SipFactory#createSipStack(Properties)} method upon 
	 * creation of the SIP Stack object.
	 *
	 * @return a the Router object identifying the Router policy.
	 * @since v1.1
	 */
	public javax.sip.address.Router getRouter() {
		return super.getRouter();
	}

	/** Returns an Iterator of existing peer SipProviders that have been
	 * created by this SipStackImpl. All of the peer SipProviders of this
	 * SipStack will be proprietary objects belonging to the same stack vendor.
	 *
	 * @return an Iterator containing all existing peer SipProviders created
	 * by this SipStack. Returns an empty Iterator if no SipProviders exist.
	 */
	public java.util.Iterator getSipProviders() {
		return this.sipProviders.iterator();
	}

	/** Gets the user friendly name that identifies this SipStack instance. This
	 * value is set using the Properties object passed to the
	 * {@link SipFactory#createSipStack(Properties)} method upon creation of the SIP Stack
	 * object.
	 *
	 * @return a string identifing the stack instance
	 */
	public String getStackName() {
		return this.stackName;
	}

	/** The default transport to use for via headers.
	 */
	protected String getDefaultTransport() {
		if (isTransportEnabled("udp"))
			return "udp";
		else if (isTransportEnabled("tcp"))
			return "tcp";
		else
			return null;
	}

	/** Finalization -- stop the stack on finalization.
	* Exit the transaction scanner and release all resources.
	*/
	public void finalize() {
		this.stopStack();
	}


}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.36  2004/09/28 04:07:03  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 * Reviewed by:
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
 * Revision 1.35  2004/09/27 18:51:18  mranga
 * Reviewed by:   mranga
 *
 * Additional config flag for proxy servers (dialog is not tracked by stack).
 *
 * Revision 1.34  2004/09/26 14:48:02  mranga
 * Submitted by:  John Martin
 * Reviewed by:   mranga
 *
 * Remove unnecssary synchronization.
 *
 * Revision 1.33  2004/09/07 18:25:53  mranga
 * Submitted by:  Bill Roome
 * Reviewed by:   mranga
 *
 * flag to govern configuration should be tested against true
 *
 * Revision 1.32  2004/09/01 18:09:05  mranga
 * Reviewed by:   mranga
 * Allow application to see route header on incoming request though
 * use of a configuration parameter.
 *
 * Revision 1.31  2004/09/01 12:07:40  xoba
 * fixed html documentation bug --- removed an extra 'ul' tag... now html javadocs formatted correctly.
 *
 * Revision 1.30  2004/09/01 11:18:30  xoba
 * fixed network layer property name
 *
 * Revision 1.29  2004/08/30 16:04:47  mranga
 * Submitted by:  Mike Andrews
 * Reviewed by:   mranga
 *
 * Added a network layer.
 *
 * Revision 1.28  2004/06/16 02:53:17  mranga
 * Submitted by:  mranga
 * Reviewed by:   implement re-entrant multithreaded listener model.
 *
 * Revision 1.27  2004/06/15 09:54:42  mranga
 * Reviewed by:   mranga
 * re-entrant listener model added.
 * (see configuration property gov.nist.javax.sip.REENTRANT_LISTENER)
 *
 * Revision 1.26  2004/05/16 14:13:21  mranga
 * Reviewed by:   mranga
 * Fixed the use-count issue reported by Peter Parnes.
 * Added property to prevent against content-length dos attacks.
 *
 * Revision 1.25  2004/05/14 20:20:02  mranga
 *
 * Submitted by:  Dave Stuart
 * Reviewed by:  mranga
 *
 * Stun support hacks -- use the original address specified to bind tcp transport
 * socket.
 *
 * Revision 1.24  2004/04/29 19:27:46  mranga
 * Submitted by:  Dave Stuart (Sipquest).
 * Reviewed by:  mranga
 *
 * Fix parsing of stun server address.
 *
 * Revision 1.23  2004/04/27 17:18:53  mranga
 * Reviewed by:   mranga
 * Turn off logging of content  by default.
 *
 * Revision 1.22  2004/04/19 21:51:03  mranga
 * Submitted by:  mranga
 * Reviewed by:  ivov
 * Support for stun.
 *
 * Revision 1.21  2004/03/30 15:17:37  mranga
 * Reviewed by:   mranga
 * Added reInitialization for stack in support of applets.
 *
 * Revision 1.20  2004/03/25 15:15:03  mranga
 * Reviewed by:   mranga
 * option to log message content added.
 *
 * Revision 1.19  2004/03/18 14:40:38  mranga
 * Reviewed by:   mranga
 * Removed event scanning thread from provider and added a single class that
 * scans for events and delivers to the listener (previously each provider had
 * its own scanning thread).
 * Added code in stack finalization to exit all threads and release all resources
 * held by the stack.
 *
 * Revision 1.18  2004/03/09 00:34:43  mranga
 * Reviewed by:   mranga
 * Added TCP connection management for client and server side
 * Transactions. See configuration parameter
 * gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false
 * Releases Client TCP Connections after linger time
 *
 * Revision 1.17  2004/03/07 22:25:22  mranga
 * Reviewed by:   mranga
 * Added a new configuration parameter that instructs the stack to
 * drop a server connection after server transaction termination
 * set gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false for this
 * Default behavior is true.
 *
 * Revision 1.16  2004/02/29 15:32:58  mranga
 * Reviewed by:   mranga
 * bug fixes on limiting the max message size.
 *
 * Revision 1.15  2004/02/29 00:46:33  mranga
 * Reviewed by:   mranga
 * Added new configuration property to limit max message size for TCP transport.
 * The property is gov.nist.javax.sip.MAX_MESSAGE_SIZE
 *
 * Revision 1.14  2004/02/20 16:36:42  mranga
 * Reviewed by:   mranga
 * Minor changes to debug logging -- record the properties with which the stack
 * was created. Be slightly more forgiving when checking for retransmission
 * filter when configuring stack.
 *
 * Revision 1.13  2004/01/22 18:39:41  mranga
 * Reviewed by:   M. Ranganathan
 * Moved the ifdef SIMULATION and associated tags to the first column so Prep preprocessor can deal with them.
 *
 * Revision 1.12  2004/01/22 14:23:45  mranga
 * Reviewed by:   mranga
 * Fixed some minor formatting issues.
 *
 * Revision 1.11  2004/01/22 13:26:28  sverker
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
