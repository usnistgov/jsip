package gov.nist.javax.sip;
import java.util.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import java.net.UnknownHostException;
import java.lang.reflect.*;
import gov.nist.core.*;

/** Implementation of SipStack.
*
* The JAIN-SIP stack is initialized by a set of properties (see the JAIN
* SIP documentation for an explanation of these properties).
* In addition to these, the following are meaningful properties for 
*the NIST SIP stack (specify these in the property array when you create
*the JAIN-SIP statck).:
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
*<li><b>gov.nist.javax.sip.BAD_MESSAGE_LOG = fileName </b><br/>
*  Messages that do not contain the required headers are dropped.
*  This file indicates where the bad (unprocessable) messages go. 
*  Default is null (bad messages are not logged). </li>
*
*<li><b>gov.nist.javax.sip.ACCESS_LOG_VIA_RMI = boolean</b>  <br/>
*indicate whether log files should be accessible via RMI. Default (false).
* A log root name for naming the log for remote access. The log can be
*remotely  accessedas the following RMI URL <br/>
*<it>
*rmi://stackIPAddress:rmiPort/stackName/gov.nist.javax.sip.MessageLogTableImpl
*</it> <br/>
* Once the log has been collected, you can view it using the 
* provided trace viewer application (in the tools/tracesviewer directory).
*Note that you need ot specify TRACE_LEVEL=16  or above (see below) 
*to enable message logging. You can view the log using  the trace viewer.
*directly using the generated log table. 
*</li>
*    
*<li><b>gov.nist.javax.sip.RMI_PORT = intetger</b> <br/>
* This is <b>only relevant</b> if you want to allow remote access to the
* log table via RMI.
* Port where the rmi registry listens. (defaults to 1099). </li>
*    
*<li><b>gov.nist.javax.sip.DEBUG_LOG = fileName </b><br/>
*  Where the debug log goes. 
*	<font color=red> Mail this to us with bug reports.  </font>
</li>
*    
*<li><b>gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS = integer</b> <br/>
*   Max number of open SERVER transactions in the transaction table - incoming
*  Requests that have the capability to create ServerTransactions will
*  not be processed if server transaction table exceeds this size
*  (default value is "infinity"). </li>
*    
*<li> <b>gov.nist.javax.sip.THREAD_POOL_SIZE = integer </b> <br/>
*  Concurrency control for number of simultaneous active threads for
*  processing incoming UDP messages. </li>
*
*<li> <b>gov.nist.javax.sip.MAX_CONNECTIONS = integer </b> <br/>
*   Max number of simultaneous TCP connections handled by stack. 
*  (Was mis-spelled - Documentation bug fix by Bob Johnson)</li>
*  </ul>
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*
*/

public class SipStackImpl
extends SIPTransactionStack
implements javax.sip.SipStack {
    
    private Hashtable listeningPoints;
    private LinkedList sipProviders;
    
    private String     outboundProxy;
    protected String   routerPath;
    
    
   
    /** Creates a new instance of SipStackImpl.
    */

    protected SipStackImpl() {
	super();
	NistSipMessageFactoryImpl msgFactory = 
			new NistSipMessageFactoryImpl(this);
	super.setMessageFactory(msgFactory);
        this.listeningPoints = new Hashtable();
        this.sipProviders = new LinkedList();
    }

    public SipStackImpl(Properties configurationProperties) 
        throws PeerUnavailableException {
	this();
	String address = configurationProperties.getProperty
			("javax.sip.IP_ADDRESS");
	try {
	   /** Retrieve the stack IP address */
	   if (address == null) 
		throw new PeerUnavailableException("address not specified");
	   super.setHostAddress(address);
	} catch (java.net.UnknownHostException ex) {
		throw new PeerUnavailableException("bad address " + address);
	}
	
	/** Retrieve the stack name */
	String name = configurationProperties.getProperty
			("javax.sip.STACK_NAME");
	if (name == null) 
		throw new PeerUnavailableException("stack name is missing");
	super.setStackName(name);


	/** Retrieve the router path */
	String routerPath = 
		configurationProperties.getProperty
			("javax.sip.ROUTER_PATH");
	if (routerPath == null) 
		routerPath = "gov.nist.javax.sip.stack.DefaultRouter";
	String outboundProxy = 
		configurationProperties.getProperty
			("javax.sip.OUTBOUND_PROXY");
	try {
	 	Class routerClass = Class.forName(routerPath);
                Class[] constructorArgs = new Class[2];
		constructorArgs[0] = javax.sip.SipStack.class;
		constructorArgs[1] = new String().getClass();
                Constructor cons = routerClass.getConstructor(constructorArgs);
                Object[] args = new Object[2];
                args[0] = (SipStack)this;
	        args[1] = outboundProxy;
		Router router = (Router) cons.newInstance(args);
		super.setRouter(router);
	 } catch (InvocationTargetException ex1) {
	    throw new PeerUnavailableException
		("Cound not instantiate router - check constructor",
			ex1);
         } catch (Exception ex) {
	    ex.printStackTrace();
	    throw new PeerUnavailableException("Could not instantiate router",
			ex);
         }

                       

	/* Retrieve the EXTENSION Methods. These are used for instantiation
	* of Dialogs.
	*/
	String extensionMethods = 
		configurationProperties.getProperty
			("javax.sip.EXTENSION_METHODS");

	if (extensionMethods != null) {
		java.util.StringTokenizer st = new 
			java.util.StringTokenizer(extensionMethods);
                LinkedList ll = new LinkedList();
                while (st.hasMoreTokens()) {
                    String em = st.nextToken(":");
		    if (em.equalsIgnoreCase(Request.BYE) 
			|| em.equalsIgnoreCase(Request.ACK)
			|| em.equalsIgnoreCase(Request.OPTIONS) )
			throw new PeerUnavailableException
			("Bad extension method " + em);
		    else this.addExtensionMethod(em);
                }
	}

	/*Set the retransmission filter.
	*/
	String rf = 
		configurationProperties.getProperty
			("javax.sip.RETRANSMISSION_FILTER");
	if (rf != null &&   
	   ("true".equalsIgnoreCase(rf) || "on".equalsIgnoreCase(rf)) ) {
		this.retransmissionFilter = true;
	}

	/* Initialize the remote logging features. This is a NIST-SIP only
	* feature (not part of the JAIN SPEC)
	*/
	String accessLogViaRMI = configurationProperties.getProperty
			("gov.nist.javax.sip.ACCESS_LOG_VIA_RMI");
	if (accessLogViaRMI != null && 
		"true".equalsIgnoreCase(accessLogViaRMI)) {
	   try {
	      String logRootName =  this.stackName;
	      int rmiPort = 1099;
	      String rport = 
			configurationProperties.getProperty
			("gov.nist.javax.sip.RMI_PORT");
	      if (rport != null) 
			rmiPort = Integer.parseInt(rport);
	      
	      String lifetime = configurationProperties.getProperty
			("gov.nist.javax.sip.LOG_LIFETIME");
	      // Default log lifetime is 10 minutes.
	      int loglife = 600;
	      if (lifetime != null) 
	      	    loglife = Integer.parseInt(lifetime);
	        super.exportServerLog(logRootName,rmiPort,loglife);
	   } catch (NumberFormatException ex) {
			System.out.println
			("remote log access disabled bad integer " + 
				ex.getMessage());
	    }
	 }
	 String debugLog =  configurationProperties.getProperty
			("gov.nist.javax.sip.DEBUG_LOG");
	 if (debugLog != null) LogWriter.setLogFileName(debugLog);

	 String logLevel = configurationProperties.getProperty
			("gov.nist.javax.sip.TRACE_LEVEL");

	 if (logLevel != null) {
	   try {
	      int ll = Integer.parseInt(logLevel);
	     if (ll == 32) LogWriter.needsLogging = true;
	     ServerLog.setTraceLevel(ll);
	   } catch (NumberFormatException ex) {
		System.out.println("WARNING Bad integer " + logLevel);
		System.out.println("logging dislabled ");
	        ServerLog.setTraceLevel(0);
	    }
	 }
	  
	 String badMessageLog = configurationProperties.getProperty
			("gov.nist.javax.sip.BAD_MESSAGE_LOG");
	 if (badMessageLog != null) super.badMessageLog = badMessageLog;
          
         String serverLog = configurationProperties.getProperty
			("gov.nist.javax.sip.SERVER_LOG");
	 if (serverLog!= null) ServerLog.setLogFileName(serverLog);
	 ServerLog.checkLogFile();
	  String maxConnections = 
		configurationProperties.getProperty
			("gov.nist.javax.sip.MAX_CONNECTIONS");
	 if (maxConnections != null) {
	    try {
		this.maxConnections = new Integer(maxConnections).intValue();
	    } catch (NumberFormatException ex)  {
		System.out.println
		("max connections - bad value " + ex.getMessage());
	    }
	}
	String threadPoolSize = configurationProperties.getProperty
			("gov.nist.javax.sip.THREAD_POOL_SIZE");
	 if (threadPoolSize != null) {
	    try {
		this.threadPoolSize = new Integer(threadPoolSize).intValue();
	    } catch (NumberFormatException ex)  {
		System.out.println
		("thread pool size - bad value " + ex.getMessage());
	    }
	}

	String transactionTableSize = configurationProperties.getProperty
			("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS");
	 if (transactionTableSize != null ) {
	    try {
		this.transactionTableSize = 
			new Integer(transactionTableSize).intValue();
	    } catch (NumberFormatException ex)  {
		System.out.println
		("transaction table size - bad value " + ex.getMessage());
	    }
	}
	
	 
	
    }



    
    /** Creates a new peer ListeningPoint on this SipStack on a specified
     * host, port and transport and returns a reference to the newly created
     * ListeningPoint object. The newly created ListeningPoint is implicitly
     * attached to this SipStack upon execution of this method, by adding the
     * ListeningPoint to the {@link SipStack#getListeningPoints()} of this
     * SipStack, once it has been successfully created.
     *
     * @return The peer ListeningPoint attached to this SipStack.
     * @param <var>port</var> the port of the new ListeningPoint.
     * @param <var>transport</var> the transport of the new ListeningPoint.
     * SipStack.
     */
    public synchronized ListeningPoint createListeningPoint(int port,
    String transport)
    throws TransportNotSupportedException, InvalidArgumentException {
	if (transport == null) 
		throw new NullPointerException("null transport");
	if (port <= 0 ) throw new InvalidArgumentException("bad port");
	if (! transport.equalsIgnoreCase("UDP") &&
	    ! transport.equalsIgnoreCase("TCP") ) 
	    throw new TransportNotSupportedException
		("bad transport " + transport);

        
        String key = ListeningPointImpl.makeKey
			(super.stackAddress,port,transport);
        
        ListeningPointImpl lip = (ListeningPointImpl)listeningPoints.get(key);
        if (lip != null) {
            return lip;
        } else {
            try {
                MessageProcessor messageProcessor =
                this.createMessageProcessor(port,transport);
                lip = new ListeningPointImpl(this);
		lip.messageProcessor = messageProcessor;
		messageProcessor.setListeningPoint(lip);
                this.listeningPoints.put(key,lip);
                return (ListeningPoint) lip;
            } catch (java.io.IOException ex) {
                throw new InvalidArgumentException
                (ex.getMessage());
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
     * @param <var>listeningPoint</var> the ListeningPoint the SipProvider is to
     * be attached to in order to send and Receive messages.
     * @throws <var>ListeningPointUnavailableException</var> thrown if another
     * SipProvider is already using the ListeningPoint.
     */
    public SipProvider createSipProvider(ListeningPoint listeningPoint)
    throws ObjectInUseException {
        if (listeningPoint == null)
            throw new NullPointerException("null listeningPoint");
        ListeningPointImpl listeningPointImpl =
        (ListeningPointImpl)listeningPoint;
        if (listeningPointImpl.sipProviderImpl != null)
            throw new ObjectInUseException
            ("Provider already attached!");
        
        SipProviderImpl provider = new SipProviderImpl();
	provider.setSipStack(this);
        provider.setListeningPoint(listeningPointImpl);
        listeningPointImpl.sipProviderImpl= provider;
        this.sipProviders.add(provider);
        return provider;
    }
    
    /** Deletes the specified peer ListeningPoint attached to this SipStack. The
     * specified ListeningPoint is implicitly detached from this SipStack upon
     * execution of this method, by removing the ListeningPoint from the
     * {@link SipStack#getListeningPoints()} of this SipStack.
     *
     * @param <var>listeningPoint</var> the peer SipProvider to be deleted from
     * this SipStack.
     * @exception <var>ObjectInUseException</var> thrown if the specified peer
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
        lip.messageProcessor.stop();
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
     * @param <var>sipProvider</var> the peer SipProvider to be deleted from
     * this SipStack.
     * @exception <var>ObjectInUseException</var> thrown if the specified peer
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
        sipProviders.remove(sipProvider);
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
	String key = ListeningPointImpl.makeKey
			(super.stackAddress,port,transport);
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
        return  super.getRouter();
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
	if (isTransportEnabled("udp")) return "udp";
	else if (isTransportEnabled("tcp")) return "tcp";
	else return null;
     }
    
    
    
    /** Invoked when an error has ocurred with a transaction.
     *
     * 	@param transactionErrorEvent Error event.
     */
    public void transactionErrorEvent
        (SIPTransactionErrorEvent transactionErrorEvent) {
            // TODO
            SIPTransaction transaction =
                    (SIPTransaction) transactionErrorEvent.getSource();
            
    }
    
 
    
  
    
    
    
    
    
}
