/**************************************************************************/
/* Product of NIST Advanced Networking Technologies Division		  */
/**************************************************************************/
package gov.nist.javax.sip.stack;

import java.util.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.parser.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.message.*;
import gov.nist.core.*;
import javax.sip.header.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import java.io.IOException;
import java.net.*;
import java.text.ParseException;


/** Tracks dialogs. A dialog is a peer to peer association of communicating
 * SIP entities. For INVITE transactions, a Dialog is created when a success
 * message is received (i.e. a response that has a To tag).
 * The SIP Protocol stores enough state in the
 * message structure to extract a dialog identifier that can be used to
 * retrieve this structure from the SipStack. Bugs against route set 
 * management were reported by Antonis Karydas and Brad Templeton.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 *
 */

public class DialogImpl implements javax.sip.Dialog {
    private Object applicationData; // Opaque pointer to application data.
    private SIPTransaction firstTransaction;
    private SIPTransaction lastTransaction;
    private String dialogId;
    private int localSequenceNumber;
    private int remoteSequenceNumber;
    private String myTag;
    private String hisTag;
    private RouteList  routeList;
    private Route     contactRoute;
    private String user;
    private Route defaultRoute;
    private SIPTransactionStack sipStack;
    private int dialogState;
    protected boolean ackSeen;
    protected SIPRequest lastAck;
    private GenericURI requestURI;
    protected int retransmissionCount;
    
    public final static int EARLY_STATE     = DialogState._EARLY;
    public final static int CONFIRMED_STATE = DialogState._CONFIRMED;
    public final static int COMPLETED_STATE = DialogState._COMPLETED;
    public final static int TERMINATED_STATE = DialogState._TERMINATED;
    
    

    /** Set ptr to app data.
     */
    public void setApplicationData(Object applicationData) {
	this.applicationData = applicationData;
    }

    /** Get ptr to opaque application data.
    */
    public Object getApplicationData() { 
	return this.applicationData; 
    }
    
    /**
     *A debugging print routine.
     */
    private void printRouteList() {
        if (LogWriter.needsLogging) {
            LogWriter.logMessage("this : " + this);
            LogWriter.logMessage("printRouteList : " +
            this.routeList.encode());
            if (this.contactRoute != null) {
                LogWriter.logMessage("contactRoute : " +
                this.contactRoute.encode());
            } else {
                LogWriter.logMessage("contactRoute : null");
            }
        }
    }

    /** Get the next hop to which requests in the dialog will be routed
     * to.
     *@return the next hop to which to send the outbound request.
     */

    public HopImpl getNextHop()  throws SipException {
       // This is already an established dialog so dont consult the router.
       // Route the request based on the request URI.
		
       RouteList rl = this.getRouteList();
       SipUri sipUri = null;
       if (rl != null && ! rl.isEmpty()) {
	      Route route = (Route) this.getRouteList().getFirst();
	      sipUri = (SipUri) (route.getAddress().getURI());
       } else if (contactRoute != null) {
	      sipUri = (SipUri) (contactRoute.getAddress().getURI());
       } else throw new SipException("No route found!");

       String host = sipUri.getHost();
       int port = sipUri.getPort(); 
       if (port == -1)  port = 5060;
       String transport = sipUri.getTransportParam();
       if (transport == null) transport = "udp";
       return new HopImpl( host,port,transport);
     }
    
    
    
    /** Return true if this is a client dialog.
     *
     *@return true if the transaction that created this dialog is a
     *client transaction and false otherwise.
     */
    public boolean isClientDialog() {
        SIPTransaction transaction =
        (SIPTransaction) this.getFirstTransaction();
        return transaction instanceof SIPClientTransaction;
    }
    
    /** Set the state for this dialog.
     *
     *@param state is the state to set for the dialog.
     */
    
    public void setState(int state) {
	if (LogWriter.needsLogging) {
	    LogWriter.logMessage("Setting dialog state for " +
			this);
	    LogWriter.logStackTrace();
	    if (state != -1 && 
		state != this.dialogState ) 
		if (LogWriter.needsLogging) {
		   LogWriter.logMessage("New dialog state is " + 
			DialogState.getObject(state) +  
			"dialogId = " +
			this.getDialogId() );
		}

	}
        this.dialogState = state;
    }
    
    /** Debugging print for the dialog.
     */
    public void printTags() {
        if (LogWriter.needsLogging) {
            LogWriter.logMessage( "isServer = " + isServer());
            LogWriter.logMessage("localTag = " + getLocalTag());
            LogWriter.logMessage( "remoteTag = " + getRemoteTag());
            LogWriter.logMessage("firstTransaction = " +
            ((SIPTransaction) firstTransaction).getOriginalRequest());
            
        }
    }
    
    
    /** Mark that the dialog has seen an ACK.
     */
    public void ackReceived(SIPRequest sipRequest) {


	    Transaction tr = this.getLastTransaction();
            // Suppress retransmission of the final response (in case
	    // retransmission filter is being used).
	    if (tr != null && tr instanceof SIPServerTransaction ) {
		SIPServerTransaction st = (SIPServerTransaction) tr;
	    	if (st.getOriginalRequest().getCSeq().getSequenceNumber() ==
			sipRequest.getCSeq().getSequenceNumber()) {
			st.setState(SIPTransaction.TERMINATED_STATE);
			this.ackSeen = true;
	    	}
	    
            	if (LogWriter.needsLogging)
	        	LogWriter.logMessage
			("ackReceived for " + 
			((SIPRequest)st.getOriginalRequest()).getMethod());

	    	if (st == null) return;
	    }
    }
    
    
    /** Get the transaction that created this dialog.
     */
    public Transaction getFirstTransaction() {
	return this.firstTransaction;
    }
    
    /** Gets the route set for the dialog.
     * When acting as an User Agent Server
     * the route set MUST be set to the list of URIs in the
     * Record-Route header field from the request, taken in order and
     * preserving all URI parameters. When acting as an User Agent
     * Client the route set MUST be set to the list of URIs in the
     * Record-Route header field from the response, taken in
     * reverse order and preserving all URI parameters. If no Record-Route
     * header field is present in the request or response,
     * the route set MUST be set to the empty set. This route set,
     * even if empty, overrides any
     * pre-existing route set for future requests in this dialog.
     * <p>
     * Requests within a dialog MAY contain Record-Route
     * and Contact header fields.
     * However, these requests do not cause the dialog's route set to be
     * modified.
     * <p>
     * The User Agent Client uses the remote target
     * and route set to build the
     * Request-URI and Route header field of the request.
     *
     * @return an Iterator containing a list of route headers to be used for
     *  forwarding. Empty iterator is returned if route has not 
     * 	been established.
     */
     public Iterator getRouteSet() {
	if (this.routeList == null) return null;
	else return this.getRouteList().listIterator();
     }
    
    
    
    private RouteList getRouteList() {
	if (LogWriter.needsLogging)
	    LogWriter.logMessage("getRouteList " + this);
        // Find the top via in the route list.
        ListIterator li = routeList.listIterator(routeList.size());
        boolean flag = true;
        RouteList retval = new RouteList();
        while (li.hasPrevious()) {
           Route route = (Route) li.previous();
	   String host = ((SipUri)route.getAddress().getURI()).getHost();
	   int port = ((SipUri)route.getAddress().getURI()).getPort();
	    if (port == -1) port = 5060;
	    String transport = 
			((SipUri)route.getAddress().getURI()).
			getTransportParam();
	    String sh = this.sipStack.getHostAddress();
	    int tp = this.firstTransaction.getPort();
	    if (flag && sh.equalsIgnoreCase(host) &&
			port == tp ) {
		    flag = false;
	    } else retval.addFirst(route.clone());
        }
	
	// If I am a UA then I am not record routing the request.

	if (flag)  {
           retval = new RouteList();
	   li = routeList.listIterator();
           while (li.hasNext()) {
             Route route = (Route) li.next();
	     retval.add(route.clone());
	   }
	}
	
        if (LogWriter.needsLogging) {
            LogWriter.logMessage("----- " );
            LogWriter.logMessage("getRouteList for " + this );
            if (retval != null)
                LogWriter.logMessage("RouteList = " +
                retval.encode());
            LogWriter.logMessage("myRouteList = " +
            routeList.encode());
            LogWriter.logMessage("----- " );
        }
        return retval;
    }
    
    
    /** Set the stack address.
     * Prevent us from routing messages to ourselves.
     *
     *@param stackAddress the address of the SIP stack.
     *
     */
    public void setStack(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }
    
    
    
    
    /**
     * Set the default route (the default next hop for the proxy or
     * the proxy address for the user agent).
     *
     *@param defaultRoute is the default route to set.
     *
     */
    
    public void setDefaultRoute(Route defaultRoute) {
        this.defaultRoute = (Route) defaultRoute.clone();
        // addRoute(defaultRoute,false);
    }
    
    /**
     * Set the user name for the default route.
     *
     *@param user is the user name to set for the default route.
     *
     */
    public void setUser(String user) {
        this.user = user;
    }
    
    
    /** Add a route list extracted from a record route list.
     * If this is a server dialog then we assume that the record
     * are added to the route list IN order. If this is a client
     * dialog then we assume that the record route headers give us
     * the route list to add in reverse order.
     *
     *@param recordRouteList -- the record route list from the incoming
     *      message.
     */
    
    private void addRoute(RecordRouteList recordRouteList) {
        if (this.isClientDialog()) {
            // This is a client dialog so we extract the record
            // route from the response and reverse its order to
            // careate a route list.
            this.routeList =  new RouteList();
            // start at the end of the list and walk backwards
            ListIterator li = recordRouteList.listIterator
            (recordRouteList.size());
            while (li.hasPrevious()) {
                RecordRoute rr = (RecordRoute) li.previous();
                AddressImpl addr = (AddressImpl) rr.getAddress();
                Route route = new Route();
                route.setAddress
                  ((AddressImpl)((AddressImpl)rr.getAddress()).clone());
                  route.setParameters
			((NameValueList)rr.getParameters().clone());
                this.routeList.add(route);
            }
        } else {
            // This is a server dialog. The top most record route
            // header is the one that is closest to us. We extract the
            // route list in the same order as the addresses in the
            // incoming request.
            this.routeList = new RouteList();
            ListIterator li = recordRouteList.listIterator();
            while(li.hasNext()) {
                RecordRoute rr = (RecordRoute) li.next();
                Route route = new Route();
                route.setAddress
			((AddressImpl)((AddressImpl) rr.getAddress()).clone());
                route.setParameters((NameValueList)rr.getParameters().
                clone());
                routeList.add(route);
            }
        }
    }
    
    
    /** Add a route list extacted from the contact list of the incoming
     *message.
     *
     *@param contactList -- contact list extracted from the incoming
     *  message.
     *
     */
    
    private void addRoute(ContactList contactList) {
        if (contactList.size() == 0 ) return;
        Contact contact = (Contact) contactList.getFirst();
        Route route = new Route();
        route.setAddress
        ((AddressImpl)((AddressImpl)(contact.getAddress())).clone());
        this.contactRoute = route;
    }
    
    
    /**
     * Extract the route information from this SIP Message and
     * add the relevant information to the route set.
     *
     *@param sipMessage is the SIP message for which we want
     *  to add the route.
     *
     *@param SIPMessage is the incoming SIP message from which we
     * want to extract out the route.
     */
    public synchronized void addRoute(SIPMessage sipMessage) {
        // cannot add route list after the dialog is initialized.
	try {
	if (LogWriter.needsLogging) {
		LogWriter.logMessage
		("addRoute: dialogState: " + this + "state = " +
		 this.getState() );
	}
        if ( this.dialogState == CONFIRMED_STATE ||
	this.dialogState == COMPLETED_STATE ||
        this.dialogState == TERMINATED_STATE) return;
	
        if (!isServer()) {
            // I am CLIENT dialog.
            if (sipMessage instanceof SIPResponse) {
	        SIPResponse sipResponse = (SIPResponse) sipMessage;
		if (sipResponse.getStatusCode() == 100)  {
			// Do nothing for trying messages.
			return;
		}
                RecordRouteList rrlist = sipMessage.getRecordRouteHeaders();
                // Add the route set from the incoming response in reverse
                // order
                if (rrlist != null ) {
                    this.addRoute(rrlist);
                } else {
                    // Set the rotue list to the last seen route list.
                    this.routeList = new RouteList();
                }
                ContactList contactList = sipMessage.getContactHeaders();
                if (contactList != null) {
                    this.addRoute(contactList);
                }
            }
        }  else  {
            if (sipMessage instanceof SIPRequest) {
                // Incoming Request has the route list
                RecordRouteList rrlist = sipMessage.getRecordRouteHeaders();
                // Add the route set from the incoming response in reverse
                // order
                if (rrlist != null ) {
                    
                    this.addRoute(rrlist);
                } else {
                    // Set the rotue list to the last seen route list.
                    this.routeList = new RouteList();
                }
                // put the contact header from the incoming request into
                // the route set.
                ContactList contactList = sipMessage.getContactHeaders();
                if (contactList != null) {
                    this.addRoute(contactList);
                }
            }
        }
	} finally {
          if (LogWriter.needsLogging)  {
	      LogWriter.logStackTrace();
              LogWriter.logMessage
              ("added a route = " + routeList.encode() + 
		"contactRoute = " + contactRoute);
	    
          }
	}
    }
    
    
    
    /** Protected Dialog constructor.
     */
    private DialogImpl() {
        this.routeList = new RouteList();
        this.dialogState =  -1; // not yet initialized.
        localSequenceNumber = 0;
        remoteSequenceNumber = -1;
	this.retransmissionCount = 8; // Max # times to retransmit OK.
				      // When retransmission filter is set.
    }
    
    
    /** Set the dialog identifier.
     */
    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }
    
    /** Constructor given the first transaction.
     *
     *@param transaction is the first transaction.
     */
    protected DialogImpl(SIPTransaction transaction) {
        this();
        this.addTransaction(transaction);
    }
    
    
    
    /** Return true if is server.
     *
     *@return true if is server transaction created this dialog.
     */
    public boolean isServer() {
        return this.firstTransaction instanceof SIPServerTransaction;
        
    }
    
    
    
    
    
    /** Get the id for this dialog.
     *
     *@return the string identifier for this dialog.
     *
     */
    public String getDialogId() {
	
        if (firstTransaction instanceof SIPServerTransaction ) {
            if (true || this.dialogId == null) {
                SIPRequest sipRequest = (SIPRequest)
                ((SIPServerTransaction) firstTransaction).getRequest();
                this.dialogId = sipRequest.getDialogId(true,this.myTag);
            }
            
        } else {
            // This is a server transaction. Compute the dialog id
            // from the tag we have assigned to the outgoing
            // response of the dialog creating transaction.
    		if (   this.getFirstTransaction() != null && 
			((SIPClientTransaction)this.getFirstTransaction()).
			getLastResponse() != null) {
                   this.dialogId =
                   ((SIPClientTransaction)firstTransaction).getLastResponse().
                   getDialogId(false,this.hisTag);
	    }
            
        }

        return this.dialogId;
    }
    
    /**
     * Add a transaction record to the dialog.
     *
     *@param transaction is the transaction to add to the dialog.
     */
    public  void addTransaction(SIPTransaction transaction) {
	
         SIPRequest sipRequest =
            (SIPRequest) transaction.getOriginalRequest();
        if (firstTransaction == null   ) {
            // Record the local and remote sequenc
            // numbers and the from and to tags for future
            // use on this dialog.
            firstTransaction = transaction;
            
            if (transaction instanceof SIPServerTransaction ) {
                setRemoteSequenceNumber
                (sipRequest.getCSeq().getSequenceNumber());
                hisTag = sipRequest.getFrom().getTag();
                // My tag is assigned when sending response
            } else {
                setLocalSequenceNumber
                (sipRequest.getCSeq().getSequenceNumber());
                // his tag is known when receiving response
                myTag = sipRequest.getFrom().getTag();
		if (myTag == null) 
			throw new RuntimeException("bad message tag missing!");
            }
        } else if (  transaction.getOriginalRequest().getMethod().equals
	           ( firstTransaction.getOriginalRequest().getMethod())  &&
	      (((firstTransaction instanceof SIPServerTransaction)   &&
	       (transaction instanceof SIPClientTransaction))  || 
	        ((firstTransaction instanceof SIPClientTransaction) &&
	        (transaction instanceof SIPServerTransaction)))) {
	    // Switch from client side to server side for re-invite
	    //  (put the other side on hold).

	     firstTransaction = transaction;

	}

	  
	this.lastTransaction = transaction;
        // set a back ptr in the incoming dialog.
        transaction.setDialog(this);
        if (LogWriter.needsLogging) {
            LogWriter.logMessage("Transaction Added " +
            this + myTag + "/" + hisTag);
            LogWriter.logMessage("TID = "
            + transaction.getTransactionId() +
            "/" + transaction.IsServerTransaction() );
            LogWriter.logStackTrace();
        }
    }
    
    /**
     * Set the remote tag.
     *
     *@param hisTag is the remote tag to set.
     */
    public void setRemoteTag(String hisTag) {
        this.hisTag = hisTag;
    }
    
    
    
    
    /**
     * Get the last transaction from the dialog.
     */
    public SIPTransaction getLastTransaction() {
        return  this.lastTransaction;
    }
    
    
    
    /**
     * Set the local sequece number for the dialog (defaults to 1 when
     * the dialog is created).
     *
     *@param lCseq is the local cseq number.
     *
     */
    protected void setLocalSequenceNumber(int lCseq) {
        this.localSequenceNumber = lCseq;
    }
    
    /**
     * Set the remote sequence number for the dialog.
     *
     * @param rCseq is the remote cseq number.
     *
     */
    protected void setRemoteSequenceNumber(int  rCseq) {
        this.remoteSequenceNumber = rCseq;
    }
    
    /**
     * Increment the Remote cseq # for the dialog.
     *
     * @return the incremented remote sequence number.
    public int  incrementRemoteSequenceNumber() {
        return ++this.remoteSequenceNumber;
    }
     */
    
    /**
     * Increment the local CSeq # for the dialog.
     * This is useful for if you want to create a hole in the sequence number
     * i.e. route a request outside the dialog and then resume within the
     * dialog.
     */
    public void incrementLocalSequenceNumber() {
         ++this.localSequenceNumber;
    }
    
    /**
     * Get the remote sequence number (for cseq assignment of outgoing
     * requests within this dialog).
     *
     *@return local sequence number.
     */
    public int getRemoteSequenceNumber() {
        return this.remoteSequenceNumber;
    }
    
    /**
     * Get the local sequence number (for cseq assignment of outgoing
     * requests within this dialog).
     *
     *@return local sequence number.
     */
    
    public int getLocalSequenceNumber() {
        return this.localSequenceNumber;
    }
    
    
    /**
     * Get local identifier for the dialog.
     *  This is used in From header tag construction
     *  for all outgoing client transaction requests for
     *  this dialog and for all outgoing responses for this dialog.
     *  This is used in To tag constuction for all outgoing
     *  transactions when we are the server of the dialog.
     *  Use this when constucting To header tags for BYE requests
     *  when we are the server of the dialog.
     *
     *@return the local tag.
     */
    public String getLocalTag() { 
	 return this.myTag;
    }
    
    /** Get peer identifier identifier for the dialog.
     * This is used in To header tag construction for all outgoing
     * requests when we are the client of the dialog.
     * This is used in From tag construction for all outgoing
     * requests when we are the Server of the dialog. Use
     * this when costructing From header Tags for BYE requests
     * when we are the server of the dialog.
     *
     *@return the remote tag
     *  (note this is read from a response to an INVITE).
     *
     */
    public String getRemoteTag() {
        return hisTag;
    }
    
    /** Set local tag for the transaction.
     *
     *@param mytag is the tag to use in From headers client
     * 	transactions that belong to this dialog and for
     *	generating To tags for Server transaction requests that belong
     * 	to this dialog.
     */
    public void setLocalTag(String mytag) {
        if (LogWriter.needsLogging)  {
            LogWriter.logMessage("set Local tag " + mytag + " "
            + this.dialogId);
            LogWriter.logStackTrace();
        }
        
        this.myTag = mytag;
	
    }
    
    
    
    /** Mark all the transactions in the dialog inactive and ready
     * for garbage collection.
     */
    protected  void deleteTransactions() {
	this.firstTransaction = null;
	this.lastTransaction = null;
    }
    
    
    
    /** This method will release all resources associated with this dialog
     * that are tracked by the Provider. Further references to the dialog by
     * incoming messages will result in a mismatch.
     * Since dialog destruction is left reasonably open ended in RFC3261,
     * this delete method is provided
     * for future use and extension methods that do not require a BYE to
     * terminate a dialogue. The basic case of the INVITE and all dialogues
     * that we are aware of today it is expected that BYE requests will
     * end the dialogue.
     */
    
    public void delete() {
        // the reaper will get him later.
        this.setState(TERMINATED_STATE);
    }
    
    /** Returns the Call-ID for this SipSession. This is the value of the
     * Call-ID header for all messages belonging to this session.
     *
     * @return the Call-ID for this Dialogue
     */
    public CallIdHeader getCallId() {
        SIPRequest sipRequest =
        ((SIPTransaction)this.getFirstTransaction()).getOriginalRequest();
        return sipRequest.getCallId();
    }
    
    
    /** Get the local Address for this dialog.
     *
     *@return the address object of the local party.
     */
    
    public javax.sip.address.Address getLocalParty()  {
       SIPRequest sipRequest =
          ((SIPTransaction)this.getFirstTransaction()).getOriginalRequest();
	if (!isServer()) {
          return sipRequest.getFrom().getAddress();
	} else  {
          return sipRequest.getTo().getAddress();
	}
    }
    
    /**
     * Returns the Address identifying the remote party.
     * This is the value of the To header of locally initiated
     * requests in this dialogue when acting as an User Agent Client.
     * <p>
     * This is the value of the From header of recieved responses in this
     * dialogue when acting as an User Agent Server.
     *
     * @return the address object of the remote party.
     */
    public javax.sip.address.Address getRemoteParty() {
        SIPRequest sipRequest =
          ((SIPTransaction)this.getFirstTransaction()).getOriginalRequest();
	if (!isServer()) {
          return sipRequest.getTo().getAddress();
	} else  {
          return sipRequest.getFrom().getAddress();
	}
        
    }
    
    /**
     * Returns the Address identifying the remote target.
     * This is the value of the Contact header of recieved Responses
     * for Requests or refresh Requests
     * in this dialogue when acting as an User Agent Client <p>
     * This is the value of the Contact header of recieved Requests
     * or refresh Requests in this dialogue when acting as an User
     * Agent Server. Bug fix sent in by Steve Crossley.
     *
     * @return the address object of the remote target.
     */
    public javax.sip.address.Address getRemoteTarget() {
	if (this.contactRoute == null) return null;
        return this.contactRoute.getAddress();
    }
    
    
    
    /** Returns the current state of the dialogue. The states are as follows:
     * <ul>
     * <li> Early - A dialog is in the "early" state, which occurs when it is
     * created when a provisional response is recieved to the INVITE Request.
     * <li> Confirmed - A dialog transitions to the "confirmed" state when a 2xx
     * final response is received to the INVITE Request.
     * <li> Completed - A dialog transitions to the "completed" state when a BYE
     * request is sent or received by the User Agent Client.
     * <li> Terminated - A dialog transitions to the "terminated" state when it
     * can be garbage collection.
     * </ul>
     * Independent of the method, if a request outside of a dialog generates a
     * non-2xx final response, any early dialogs created through provisional
     * responses to that request are terminated. If no response arrives at all
     * on the early dialog, it also terminates.
     *
     * @return a DialogState determining the current state of the dialog.
     * @see DialogState
     */
    public DialogState getState() {
	if (this.dialogState == -1) return null; // not yet initialized
        return DialogState.getObject(this.dialogState);
    }
    
    /** Returns true if this Dialog is secure i.e. if the request arrived over
     * TLS, and the Request-URI contained a SIPS URI, the "secure" flag is set
     * to TRUE.
     *
     * @return  <code>true</code> if this dialogue was established using a sips
     * URI over TLS, and <code>false</code> otherwise.
     */
    public boolean isSecure() {
        return this.getFirstTransaction().getRequest().
	getRequestURI().getScheme().equalsIgnoreCase("sips");
    }
    
    /** Sends ACK Request to the remote party of this Dialogue.
     *
     * @param ackRequest - the new ACK Request message to send.
     * @throws SipException if implementation cannot send the ACK Request for
     * any other reason
     */
    public synchronized void sendAck(Request request) throws SipException {
        SIPRequest ackRequest = (SIPRequest) request;
	if (LogWriter.needsLogging) 
		LogWriter.logMessage("sendAck" + this);
	// Loosen up check for re-invites (Andreas B)
        // if (this.isServer())
        //   throw new SipException
	// ("Cannot sendAck from Server side of Dialog");

        if (!ackRequest.getMethod().equals(Request.ACK) &&
	    !ackRequest.getMethod().equals(Request.PRACK)) 
            throw new SipException("Bad request method -- should be ACK");
	if (ackRequest.getMethod().equals(Request.ACK) &&
	    (this.getState() == null || 
	    this.getState().getValue() == EARLY_STATE))  {
	    throw new SipException
	    ("Bad dialog state " + this.getState());
	} else if (ackRequest.getMethod().equals(Request.PRACK) &&
		this.getState() == null ) {
	    throw new SipException
	    ("Bad dialog state sending PRACK" + this.getState());
	}
	
	// Set the dialog state to CONFIRMED when the Prack is sent 
	// out.
	if (ackRequest.getMethod().equals(Request.PRACK) &&
	    this.getState().getValue() == EARLY_STATE ) {
	    this.setState(CONFIRMED_STATE);
	}
	
	if (! ((SIPTransaction)this.getFirstTransaction()).
	   getOriginalRequest().getCallId().
	   getCallId().equals(((SIPRequest)request).
			getCallId().getCallId())) {
	    throw new SipException ("Bad call ID in request");
	}
	try {
	       if (LogWriter.needsLogging) {
		  LogWriter.logMessage("setting from tag For outgoing ACK= "  
			+ this.getLocalTag());
		  LogWriter.logMessage("setting To tag for outgoing ACK = "
			+ this.getRemoteTag());
	       }
	       if (this.getLocalTag() != null) 
	           ackRequest.getFrom().setTag(this.getLocalTag());
	       if (this.getRemoteTag() != null) 
	          ackRequest.getTo().setTag(this.getRemoteTag());
	} catch (ParseException ex) {
		throw new SipException(ex.getMessage());
	}
	// Create the route request and set it appropriately.
	// Note that we only need to worry about being on the client
	// side of the request.
	if (ackRequest.getHeader(RouteHeader.NAME) == null) {
	   RouteList rl = this.getRouteList();
	   if (rl.size() > 0 )  {
	 	Route route = (Route) rl.getFirst();
		SipURI sipUri = (SipUri) route.getAddress().getURI();
		if (sipUri.hasLrParam()) {
		    ackRequest.setRequestURI(this.getRemoteTarget().getURI());
		    ackRequest.addHeader(rl);
		} else {
		   // First route is not a lr 
		   // Add the contact route to the end.
		    rl.removeFirst();
		    ackRequest.setRequestURI(sipUri);

		   // Bug report from Brad Templeton
		    if (rl.size() > 0) ackRequest.addHeader(rl);
		    if (contactRoute != null) 
		    ackRequest.addHeader(contactRoute);
		}
	   }  else  {
	       if (this.getRemoteTarget() != null) 
	       ackRequest.setRequestURI (this.getRemoteTarget().getURI());
	   }
	}
	 HopImpl hop = this.getNextHop();
	 try {
	       MessageChannel messageChannel = 
		  sipStack.createRawMessageChannel(hop);
	       if ( messageChannel == null )
	        {
	   	   // Bug fix from Antonis Karydas
		   // At this point the procedures of 8.1.2 
		   // and 12.2.1.1 of RFC3261 have been tried
		   // but the resulting next hop cannot be resolved 
		   // (recall that the exception thrown
		   // is caught and ignored in SIPStack.createMessageChannel() 
		   // so we end up here  with a null messageChannel 
		   // instead of the exception handler below).
		   // All else failing, try the outbound proxy  in accordance 
		   // with 8.1.2, in particular:
		   // This ensures that outbound proxies that do not add 
		   // Record-Route header field values will drop out of 
		   // the path of subsequent requests.  It allows endpoints 
		   // that cannot resolve the first Route
		   // URI to delegate that task to an outbound proxy.
		   //
		   // if one considers the 'first Route URI' of a 
		   // request constructed according to 12.2.1.1
		   // to be the request URI when the route set is empty.  
		   Hop outboundProxy = sipStack.getRouter().getOutboundProxy();
		   if ( outboundProxy == null )
		       throw new SipException("No route found!");
		   messageChannel = sipStack.createRawMessageChannel
					(outboundProxy);
			  
	       }
	       // Wrap a client transaction around the raw message channel.
               SIPClientTransaction clientTransaction = 
			(SIPClientTransaction) 
			sipStack.createMessageChannel(messageChannel);
	       clientTransaction.setOriginalRequest(ackRequest);
               clientTransaction.sendMessage((SIPMessage)ackRequest);
	       this.lastAck = ackRequest;
		// Do not retransmit the ACK so terminate the transaction
		// immediately.
	       clientTransaction.setState(SIPTransaction.TERMINATED_STATE);
	} catch (Exception ex) {
		if (LogWriter.needsLogging)
		   LogWriter.logException(ex);
		throw new SipException("Cold not create message channel");
	 }
        
        
    }
    /**
     * Creates a new Request message based on the dialog creating request. 
     * This method should be used for but not limited to creating Bye's, 
     * Refer's and re-Invite's on the Dialog. The returned Request will be 
     * correctly formatted that is it will contain the correct CSeq header, 
     * Route headers and requestURI (derived from the remote target). This 
     * method should not be used for Ack, that is the application should 
     * create the Ack from the MessageFactory. 
     *
     * If the route set is not empty, and the first URI in the route set
     * contains the lr parameter (see Section 19.1.1), the UAC MUST place
     * the remote target URI into the Request-URI and MUST include a Route
     * header field containing the route set values in order, including all
     * parameters.
     * If the route set is not empty, and its first URI does not contain the
     * lr parameter, the UAC MUST place the first URI from the route set
     * into the Request-URI, stripping any parameters that are not allowed
     * in a Request-URI.  The UAC MUST add a Route header field containing
     * the remainder of the route set values in order, including all
     * parameters.  The UAC MUST then place the remote target URI into the
     * Route header field as the last value.
     *
     * @param method the string value that determines if the request to be  
     * created.
     * @return the newly created Request message on this Dialog.
     * @throws SipException if the Dialog is not yet established.
     */
    public Request createRequest(String method)  throws SipException {
	// Set the dialog back pointer.
	if (method == null )throw new NullPointerException("null method");
	else if (this.getState() == null || 
		this.getState().getValue() == TERMINATED_STATE) 
		throw new SipException
		("Dialog not yet established or terminated");
	SIPRequest originalRequest = 
		(SIPRequest) this.getFirstTransaction().getRequest();

	RequestLine requestLine = new RequestLine();
	requestLine.setUri((GenericURI)getRemoteParty().getURI());
	requestLine.setMethod(method);
	
	SIPRequest sipRequest = 
		originalRequest.createSIPRequest(requestLine,this.isServer());

	try {
	  // Guess of local sequence number - this is being re-set when
	  // the request is actually dispatched ( reported by Brad Templeton
	  // and Antonis Karydas).
	  if ( ! method.equals(Request.ACK)) {
	      CSeq cseq = (CSeq) sipRequest.getCSeq();
	      cseq.setSequenceNumber(this.localSequenceNumber + 1);
	  }  else {
	     // This is an ACK request. Get the last transaction and
	     // assign a seq number from there. 
	     // Bug noticed by Andreas Bystrom.
	      SIPTransaction transaction = this.lastTransaction;
	      if ( transaction == null) 
		throw new SipException ("Could not create ack!");
	      SIPResponse response = (SIPResponse) 
			this.lastTransaction.getLastResponse();
	      if ( response == null) 
		throw new SipException("Could not find response!");
	      int seqno =  response.getCSeq().getSequenceNumber();
	      CSeq cseq = (CSeq) sipRequest.getCSeq();
	      cseq.setSequenceNumber(seqno);
	   }
	      
	} catch (InvalidArgumentException ex) {
	   InternalErrorHandler.handleException(ex);
	}
	
	if (isServer()) {
	   // Remove the old via headers.
	   sipRequest.removeHeader(ViaHeader.NAME);
	   // Add a via header for the outbound request based on the
	   // transport of the message processor.
           MessageProcessor messageProcessor =
           sipStack.getMessageProcessor
		(firstTransaction.encapsulatedChannel.getTransport());
           Via via = messageProcessor.getViaHeader();
           sipRequest.addHeader(via);
	}

        From from = (From) sipRequest.getFrom();
        To to = (To) sipRequest.getTo();

	try {
		if (this.getLocalTag() != null) from.setTag(this.getLocalTag());
		if (this.getRemoteTag() != null) to.setTag(this.getRemoteTag());
	} catch (ParseException ex) {
		InternalErrorHandler.handleException(ex);
	}

	  // get the route list from the dialog.
	  RouteList rl = this.getRouteList();
	  
	 // Add it to the header.
	  if (rl.size() > 0  )  {
	 	Route route = (Route) rl.getFirst();
		SipURI sipUri = (SipUri) route.getAddress().getURI();
		if (sipUri.hasLrParam()) {
		    sipRequest.setRequestURI(this.getRemoteTarget().getURI());
		    sipRequest.addHeader(rl);
		} else {
		   // First route is not a lr 
		   // Add the contact route to the end.
		    rl.removeFirst();
		    sipRequest.setRequestURI(sipUri);
		   
		    // Bug report from Brad Templeton
		    // Check for 0 size - Bug report from Andreas Bystrom.
		    if (rl.size() > 0) sipRequest.addHeader(rl);
		    if (this.contactRoute != null) 
		     sipRequest.addHeader(contactRoute);
		}
	   }  else  {
	       // Bug report from Antonis Karydas
	       if (this.getRemoteTarget() != null) 
	       sipRequest.setRequestURI (this.getRemoteTarget().getURI());
	   }
	   return sipRequest;

    }
    
    /**
     * Sends a Request to the remote party of this dialog. This method 
     * implies that the application is functioning as UAC hence the 
     * underlying SipProvider acts statefully. This method is useful for 
     * sending Bye's for terminating a dialog or Re-Invites on the Dialog 
     * for third party call control.
     * <p>
     * This methods will set the From and the To tags for the outgoing 
     * request and also set the correct sequence number to the outgoing 
     * Request and associate the client transaction with this dialog. 
     * Note that any tags assigned by the user will be over-written by this 
     * method. 
     * <p>
     * The User Agent must not send a BYE on a confirmed INVITE until it has 
     * received an ACK for its 2xx response or until the server transaction 
     * timeout is received.
     * <p>
     * When the retransmissionFilter is <code>true</code>, 
     * that is the SipProvider takes care of all retransmissions for the 
     * application, and the SipProvider can not deliver the Request after 
     * multiple retransmits the SipListener will be notified with a 
     * {@link TimeoutEvent} when the transaction expires.
     *
     * @param request - the new Request message to send.
     * @param clientTransaction - the new ClientTransaction object identifying 
     * this transaction, this clientTransaction should be requested from 
     * SipProvider.getNewClientTransaction
     * @throws TransactionDoesNotExistException if the serverTransaction does
     * not correspond to any existing server transaction.
     * @throws SipException if implementation cannot send the Request for 
     * any reason.
     */
    public void sendRequest(ClientTransaction clientTransactionId) throws
	TransactionDoesNotExistException,SipException {
	
        SIPRequest dialogRequest = 
		 ((SIPClientTransaction)clientTransactionId)
			.getOriginalRequest();
	if (clientTransactionId == null )
		throw new NullPointerException("null parameter");

	if (dialogRequest.getMethod().equals(Request.ACK) || 
	        dialogRequest.getMethod().equals(Request.CANCEL))
		throw new SipException("Bad Request Method. " 
			+ dialogRequest.getMethod());
	// Cannot send bye until the dialog has been established.
	if ( this.getState() == null ) {
	    throw new SipException
	    ("Bad dialog state " + this.getState());

	}
	if (dialogRequest.getMethod().equalsIgnoreCase(Request.BYE) &&
	    this.getState().getValue() == EARLY_STATE)  {
	    throw new SipException
	    ("Bad dialog state " + this.getState());
	}

	if (LogWriter.needsLogging) 
		LogWriter.logMessage("dialog.sendRequest " + 
		" dialog = " + this + "\ndialogRequest = \n" +
		dialogRequest);

	if (dialogRequest.getTopmostVia() == null) {
		Via via = 
		((SIPClientTransaction) clientTransactionId).
		getOutgoingViaHeader();
		dialogRequest.addHeader(via);
	}


	if (! ((SIPTransaction)this.getFirstTransaction()).
	   getOriginalRequest().getCallId().
	   getCallId().equals(dialogRequest.
			getCallId().getCallId())) {
	    throw new SipException ("Bad call ID in request");
	}

	// Set the dialog back pointer.
         ((SIPClientTransaction)clientTransactionId).dialog = this;

	this.addTransaction((SIPTransaction) clientTransactionId);


        From from = (From) dialogRequest.getFrom();
        To to = (To) dialogRequest.getTo();

	try {
		if (this.getLocalTag()  != null) 
			from.setTag(this.getLocalTag());
		if (this.getRemoteTag() != null) 
			to.setTag(this.getRemoteTag());
	} catch (ParseException ex) {
		System.out.println("Huh??");
		ex.printStackTrace();
	}

	// Caller has not assigned the route header - set the route header
	// and the request URI for the outgoing request.
	// Bugs reported by Brad Templeton.


	if (dialogRequest.getHeader(RouteHeader.NAME) == null) {
	   // get the route list from the dialog.
	   RouteList rl = this.getRouteList();
	   // Add it to the header.
	   if (rl.size() > 0 )  {
	 	Route route = (Route) rl.getFirst();
		SipURI sipUri = (SipUri) route.getAddress().getURI();
		if (sipUri.hasLrParam()) {
		    dialogRequest.setRequestURI
				(this.getRemoteTarget().getURI());
		    dialogRequest.addHeader(rl);
		} else {
		   // First route is not a lr 
		   // Add the contact route to the end.
		    rl.removeFirst();
		    dialogRequest.setRequestURI(sipUri);
		    // Check for 0 size -  Bug report from Andreas Bystrom.
		    if (rl.size() > 0)  dialogRequest.addHeader(rl);
		    if (contactRoute != null)
		      dialogRequest.addHeader(contactRoute);
		}
	     }  else {
		// Bug report from Antonis Karydas
	        if (this.getRemoteTarget() != null) 
	        dialogRequest.setRequestURI (this.getRemoteTarget().getURI());
	     }
	}
	  

       // 2543 Backward compatibility hack contributed by 
       // Steve Crossley (Nortel Networks).
       HopImpl hop;
       try {
       	   hop = this.getNextHop();
	}  catch (SipException se) {
         
          // If no route set is defined in the dialog, consult the stack's
          // router object to determine the route as if the request were
          // being sent outside of a dialog. This is for backward
	  // compatibility with old clients that may not put contact
	  // headers in final responses.
          Iterator iter = sipStack.getNextHop(dialogRequest);
          if (iter.hasNext()) {
            Hop h = (Hop) iter.next();
            hop = new HopImpl(h.getHost(), h.getPort(), h.getTransport());
          } else {
            throw se;
          }
        } 
		
       try {
	   MessageChannel messageChannel = 
	       sipStack.createRawMessageChannel(hop);
	   ((SIPClientTransaction) clientTransactionId).
	       encapsulatedChannel = messageChannel;
       
	   if ( messageChannel == null )
	   {
	   	   // Bug fix from Antonis Karydas
		   // At this point the procedures of 8.1.2 
		   // and 12.2.1.1 of RFC3261 have been tried
		   // but the resulting next hop cannot be resolved 
		   // (recall that the exception thrown
		   // is caught and ignored in SIPStack.createMessageChannel() 
		   // so we end up here  with a null messageChannel 
		   // instead of the exception handler below).
		   // All else failing, try the outbound proxy  in accordance 
		   // with 8.1.2, in particular:
		   // This ensures that outbound proxies that do not add 
		   // Record-Route header field values will drop out of 
		   // the path of subsequent requests.  It allows endpoints 
		   // that cannot resolve the first Route
		   // URI to delegate that task to an outbound proxy.
		   //
		   // if one considers the 'first Route URI' of a 
		   // request constructed according to 12.2.1.1
		   // to be the request URI when the route set is empty.  
		   Hop outboundProxy = sipStack.getRouter().getOutboundProxy();
		   if ( outboundProxy == null )
		       throw new SipException("No route found!");
		   messageChannel = sipStack.createRawMessageChannel
					(outboundProxy);
			  
	   }
	   ((SIPClientTransaction) clientTransactionId).
	       encapsulatedChannel = messageChannel;
       } catch (Exception ex) {
	   if (LogWriter.needsLogging) LogWriter.logException(ex);
	   throw new SipException("Cold not create message channel");
       }

	
        
        try {
	 // Increment before setting!!
	 localSequenceNumber ++;
	 dialogRequest.getCSeq().
		setSequenceNumber(getLocalSequenceNumber());
	} catch (InvalidArgumentException ex) { ex.printStackTrace(); }
        
        if (this.isServer()) {
            SIPServerTransaction serverTransaction = (SIPServerTransaction)
            this.getFirstTransaction();

	    try {
		  if (this.myTag != null) from.setTag(this.myTag);
		  if (this.hisTag != null) to.setTag(this.hisTag);
	    } catch (ParseException ex) {
		throw new SipException(ex.getMessage());
	    }
            

            try {
                ((SIPClientTransaction) clientTransactionId).sendMessage
			(dialogRequest);
		// If the method is BYE then mark the dialog completed.
		if (dialogRequest.getMethod().equals(Request.BYE))
		    this.setState(COMPLETED_STATE);
            } catch (IOException ex) {
                throw new SipException("error sending message");
            }
        } else {
            // I am the client so I do not swap headers.
            SIPClientTransaction clientTransaction = (SIPClientTransaction)
            this.getFirstTransaction();

	    try {

         	if (LogWriter.needsLogging) {
			LogWriter.logMessage("setting tags from " +
				this.getDialogId());      
			LogWriter.logMessage("fromTag " + this.myTag);
			LogWriter.logMessage("toTag " + this.hisTag);
		}
		  
		if (this.myTag != null)  from.setTag(this.myTag);
		if (this.hisTag != null) to.setTag(this.hisTag);
	    } catch (ParseException ex) {
		throw new SipException(ex.getMessage());
	    }
            
            try {
                ((SIPClientTransaction)clientTransactionId).
			sendMessage(dialogRequest);
		// go directly to terminated state.
		if (dialogRequest.getMethod().equalsIgnoreCase(Request.BYE))
		    this.delete();
            } catch (IOException ex) {
                throw new SipException("error sending message");
            }
            
        }
    }


    /** Resend the last ack.
    */
    public void resendAck () throws SipException {
	// Check for null.
	// Bug report and fix by Antonis Karydas.
	if (this.lastAck != null)
	   this.sendAck(lastAck); 
    }

    protected boolean isInviteDialog() {
	return this.getFirstTransaction().getRequest().getMethod().
			equals(Request.INVITE);
    }
	

    
}
