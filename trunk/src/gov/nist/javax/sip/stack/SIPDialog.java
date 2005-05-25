/**************************************************************************/
/* Product of NIST Advanced Networking Technologies Division		  */
/**************************************************************************/
package gov.nist.javax.sip.stack;

import java.util.*;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.message.*;
import gov.nist.core.*;
import javax.sip.header.*;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import java.io.IOException;
import java.text.ParseException;

/**
 * Tracks dialogs. A dialog is a peer to peer association of communicating SIP
 * entities. For INVITE transactions, a Dialog is created when a success message
 * is received (i.e. a response that has a To tag). The SIP Protocol stores
 * enough state in the message structure to extract a dialog identifier that can
 * be used to retrieve this structure from the SipStack.
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.16 $ $Date: 2005-05-25 18:12:32 $
 * 
 * @author M. Ranganathan <mranga@nist.gov><br/>Bugs were reported by Antonis
 *         Karydas, Brad Templeton, Jeff Adams and Alex Rootham.
 * 
 * <a href=" {@docRoot}/uncopyright.html">This code is in the public domain.
 * </a>
 * 
 *  
 */

public class SIPDialog implements javax.sip.Dialog, PendingRecord {
    private boolean reInviteFlag;

    private Object applicationData; // Opaque pointer to application data.

    private SIPRequest originalRequest;

    private SIPTransaction firstTransaction;

    private SIPTransaction lastTransaction;

    private String dialogId;

    private int localSequenceNumber;

    private int remoteSequenceNumber;

    private String myTag;

    private String hisTag;

    private RouteList routeList;

    private Route contactRoute;

    private String user;

    private Route defaultRoute;

    private SIPTransactionStack sipStack;

    private int dialogState;

    private boolean ackSeen;

    protected SIPRequest lastAck;

    protected boolean ackProcessed;

    protected DialogTimerTask timerTask;

    protected Integer nextSeqno;

    protected static final int WINDOW_SIZE = 8;

    protected Hashtable pendingRecords;

    private int retransmissionTicksLeft;

    private int prevRetransmissionTicks;

    protected boolean inPendingQueue;
    
    private int increment;

    // This is for debugging only.
    private int ackLine;

    // The following fields are extracted from the request that created the
    // Dialog.
  
    
    protected javax.sip.address.Address localParty;

    protected javax.sip.address.Address remoteParty;

    protected CallIdHeader callIdHeader;

    public final static int EARLY_STATE = DialogState._EARLY;

    public final static int CONFIRMED_STATE = DialogState._CONFIRMED;

    public final static int COMPLETED_STATE = DialogState._COMPLETED;

    public final static int TERMINATED_STATE = DialogState._TERMINATED;

    /**
     * Put a pendig request (to be consumed later if possible). If seqno is way
     * too big (outside window) reject it.
     */
    public void putPending(NistSipMessageHandlerImpl pendingRecord, int seqno) {
        boolean toInsert = false;
        synchronized (pendingRecords) {
            // check for null added by Alex Rootham
            if ((nextSeqno != null)
                    && (seqno > nextSeqno.intValue() + WINDOW_SIZE)) {
                return;
            } else if (this.pendingRecords.containsKey(new Integer(seqno))) {
                return;
            } else {
                this.pendingRecords.put(new Integer(seqno), pendingRecord);
                toInsert = true;
            }
        }
        if (toInsert)
            sipStack.putPending(this);
    }

    /**
     * return true if the record is done.
     */
    public boolean isTerminated() {
        return this.dialogState == TERMINATED_STATE;
    }

    /**
     * Set ptr to app data.
     */
    public void setApplicationData(Object applicationData) {
        this.applicationData = applicationData;
    }

    /**
     * Get ptr to opaque application data.
     */
    public Object getApplicationData() {
        return this.applicationData;
    }

    /**
     * Updates the next consumable seqno and notifies the pending thread if
     * there are any queued requests.
     */
    public void requestConsumed() {
        boolean toNotify = false;
        this.nextSeqno = new Integer(this.getRemoteSequenceNumber() + 1);
      
        if ( LogWriter.needsLogging){
            this.sipStack.logWriter.logMessage("Request Consumed -- next consumable Request Seqno = " + 
                    this.nextSeqno);
        }
        // got the next thing we were looking for.
        synchronized (this.pendingRecords) {
            if (this.pendingRecords.containsKey(nextSeqno)) {
                toNotify = true;
            }
        }
        if (toNotify)
            this.sipStack.notifyPendingRecordScanner();
    }

    /**
     * Return true if this request can be consumed by the dialog.
     * 
     * @param dialogRequest
     *            is the request to check with the dialog.
     * @return true if the dialogRequest sequence number matches the next
     *         consumable seqno.
     */
    public boolean isRequestConsumable(SIPRequest dialogRequest) {
        // have not yet set remote seqno - this is a fresh
        if (this.getRemoteSequenceNumber() == -1)
            return true;
        else if (this.nextSeqno == null)
            return false;
        else
            return this.nextSeqno.intValue() == dialogRequest.getCSeq()
                    .getSequenceNumber();
    }

    /**
     * A debugging print routine.
     */
    private void printRouteList() {
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("this : " + this);
            sipStack.logWriter.logMessage("printRouteList : "
                    + this.routeList.encode());
            if (this.contactRoute != null) {
                sipStack.logWriter.logMessage("contactRoute : "
                        + this.contactRoute.encode());
            } else {
                sipStack.logWriter.logMessage("contactRoute : null");
            }
        }
    }

    public class DialogTimerTask extends TimerTask {
        SIPDialog dialog;

        SIPTransactionStack stack;

        SIPServerTransaction transaction;

        public DialogTimerTask(SIPDialog dialog,
                SIPServerTransaction transaction) {
            this.dialog = dialog;
            this.stack = dialog.sipStack;
            this.transaction = transaction;
        }

        public void run() {
            // If I ACK has not been seen on Dialog,
            // resend last response.
            if (LogWriter.needsLogging)
                sipStack.logMessage("Running dialog timer");
            if (!dialog.ackSeen) {
                SIPResponse response = transaction.getLastResponse();
                // Retransmit to 200 until ack receivedialog.
                if (response.getStatusCode() == 200) {
                    try {
                        // If retransmission filter is
                        // enabled, send the last response.
                        if (sipStack.retransmissionFilter
                                && dialog.toRetransmitFinalResponse())
                            transaction.sendMessage(response);
                    } catch (IOException ex) {
                        dialog.setState(SIPDialog.TERMINATED_STATE);
                    } finally {
                        // Need to fire the timer so
                        // transaction will eventually
                        // time out whether or not
                        // the IOException occurs
                        // (bug fix sent in by Christophe).
                        // Note that this firing also
                        // drives Listener timeout.
                        transaction.fireTimer();
                    }
                }
            }

            // Stop running this timer if the dialog is in the
            // confirmed state or ack seen if retransmit filter on.
            if (this.dialog.isAckSeen()
                    || this.dialog.dialogState == TERMINATED_STATE) {
                this.cancel();
                this.dialog.timerTask = null;
            }

        }

    }

    /**
     * Get the next hop to which requests in the dialog will be routed to.
     * 
     * @return the next hop to which to send the outbound request.
     */

    public HopImpl getNextHop() throws SipException {
        // This is already an established dialog so dont consult the router.
        // Route the request based on the request URI.

        RouteList rl = this.getRouteList();
        SipUri sipUri = null;
        if (rl != null && !rl.isEmpty()) {
            Route route = (Route) this.getRouteList().getFirst();
            sipUri = (SipUri) (route.getAddress().getURI());
        } else if (contactRoute != null
                && contactRoute.getAddress().getURI() instanceof SipUri) {
            sipUri = (SipUri) (contactRoute.getAddress().getURI());
        } else
            throw new SipException("No route found!");

        // Use the maddr param to get the host if one exists.
        String host = sipUri.getMAddrParam() != null ? sipUri.getMAddrParam()
                : sipUri.getHost();

        // Get the transport parameter.
        String transport = sipUri.getTransportParam();
        if (transport == null)
            transport = "udp";

        int port = sipUri.getPort();

	// Added by Daniel J. Martinez Manzano <dani@dif.um.es>
        // Checks if transport parameter is TLS and assigns 5061.
	if("tls".equalsIgnoreCase(transport))
		port = 5061;

        if (port == -1)
            port = 5060;
        return new HopImpl(host, port, transport);
    }

    /**
     * Return true if this is a client dialog.
     * 
     * @return true if the transaction that created this dialog is a client
     *         transaction and false otherwise.
     */
    public boolean isClientDialog() {
        SIPTransaction transaction = (SIPTransaction) this
                .getFirstTransaction();
        return transaction instanceof SIPClientTransaction;
    }

    /**
     * Set the state for this dialog.
     * 
     * @param state
     *            is the state to set for the dialog.
     */

    public void setState(int state) {
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("Setting dialog state for " + this);
            sipStack.logWriter.logStackTrace();
            if (state != -1 && state != this.dialogState)
                if (LogWriter.needsLogging) {
                    sipStack.logWriter.logMessage("New dialog state is "
                            + DialogState.getObject(state) + "dialogId = "
                            + this.getDialogId());
                }

        }
        this.dialogState = state;
        // Dialog is in terminated state set it up for GC.
        if (state == TERMINATED_STATE) {
            this.sipStack.removeDialog(this);
            this.stopTimer();
            this.sipStack.removePending(this);
        }
    }

    /**
     * Debugging print for the dialog.
     */
    public void printDebugInfo() {
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("isServer = " + isServer());
            sipStack.logWriter.logMessage("localTag = " + getLocalTag());
            sipStack.logWriter.logMessage("remoteTag = " + getRemoteTag());
            sipStack.logWriter.logMessage("localSequenceNumer = "
                    + getLocalSequenceNumber());
            sipStack.logWriter.logMessage("remoteSequenceNumer = "
                    + getRemoteSequenceNumber());
            sipStack.logWriter.logMessage("ackLine:" + this.getRemoteTag()
                    + " " + ackLine);
        }
    }

    /**
     * Mark that the dialog has seen an ACK.
     */
    public void ackReceived(SIPRequest sipRequest) {

        // Suppress retransmission of the final response (in case
        // retransmission filter is being used).
        if (this.ackSeen)
            return;
        SIPServerTransaction tr = this.getInviteTransaction();
        if (tr != null) {
            if (tr.getCSeq() == sipRequest.getCSeq().getSequenceNumber()) {
                //st.setState(SIPTransaction.TERMINATED_STATE);
                this.ackSeen = true;
                this.lastAck = sipRequest;
                if (LogWriter.needsLogging) {
                    sipStack.logWriter.logMessage("ackReceived for "
                            + ((SIPTransaction) tr).getMethod());
                    this.ackLine = sipStack.logWriter.getLineCount();
                    this.printDebugInfo();
                }
                this.setState(CONFIRMED_STATE);
            }
        }
    }

    /**
     * Return true if the dialog has already seen the ack.
     * 
     * @return flag that records if the ack has been seen.
     */
    public boolean isAckSeen() {
        return this.ackSeen;
    }

    /**
     * Get the last ACK for this transaction.
     */
    public SIPRequest getLastAck() {
        return this.lastAck;
    }

    /**
     * Get the transaction that created this dialog.
     */
    public Transaction getFirstTransaction() {
        return this.firstTransaction;
    }

    /**
     * Gets the route set for the dialog. When acting as an User Agent Server
     * the route set MUST be set to the list of URIs in the Record-Route header
     * field from the request, taken in order and preserving all URI parameters.
     * When acting as an User Agent Client the route set MUST be set to the list
     * of URIs in the Record-Route header field from the response, taken in
     * reverse order and preserving all URI parameters. If no Record-Route
     * header field is present in the request or response, the route set MUST be
     * set to the empty set. This route set, even if empty, overrides any
     * pre-existing route set for future requests in this dialog.
     * <p>
     * Requests within a dialog MAY contain Record-Route and Contact header
     * fields. However, these requests do not cause the dialog's route set to be
     * modified.
     * <p>
     * The User Agent Client uses the remote target and route set to build the
     * Request-URI and Route header field of the request.
     * 
     * @return an Iterator containing a list of route headers to be used for
     *         forwarding. Empty iterator is returned if route has not been
     *         established.
     */
    public Iterator getRouteSet() {
        if (this.routeList == null) {
            return new LinkedList().listIterator();
        } else {
            return this.getRouteList().listIterator();
        }
    }

    private synchronized RouteList getRouteList() {
        if (LogWriter.needsLogging)
            sipStack.logWriter.logMessage("getRouteList " + this);
        // Find the top via in the route list.
        ListIterator li = routeList.listIterator(routeList.size());
        boolean flag = true;
        RouteList retval = new RouteList();
        while (li.hasPrevious()) {
            Route route = (Route) li.previous();
            String host = ((SipUri) route.getAddress().getURI()).getHost();
            int port = ((SipUri) route.getAddress().getURI()).getPort();
            if (port == -1)
                port = 5060;
            String transport = ((SipUri) route.getAddress().getURI())
                    .getTransportParam();
            String sh = this.sipStack.getHostAddress();
            int tp = this.firstTransaction.getPort();
            if (flag && sh.equalsIgnoreCase(host) && port == tp) {
                flag = false;
            } else
                retval.addFirst(route.clone());
        }

        // If I am a UA then I am not record routing the request.

        if (flag) {
            retval = new RouteList();
            li = routeList.listIterator();
            while (li.hasNext()) {
                Route route = (Route) li.next();
                retval.add(route.clone());
            }
        }

        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("----- ");
            sipStack.logWriter.logMessage("getRouteList for " + this);
            if (retval != null)
                sipStack.logWriter.logMessage("RouteList = " + retval.encode());
            sipStack.logWriter
                    .logMessage("myRouteList = " + routeList.encode());
            sipStack.logWriter.logMessage("----- ");
        }
        return retval;
    }

    /**
     * Set the stack address. Prevent us from routing messages to ourselves.
     * 
     * @param sipStack
     *            the address of the SIP stack.
     *  
     */
    public void setStack(SIPTransactionStack sipStack) {
        this.sipStack = sipStack;
    }

    /**
     * Set the default route (the default next hop for the proxy or the proxy
     * address for the user agent).
     * 
     * @param defaultRoute
     *            is the default route to set.
     *  
     */
    public void setDefaultRoute(Route defaultRoute) {
        this.defaultRoute = (Route) defaultRoute.clone();
    }

    /**
     * Set the user name for the default route.
     * 
     * @param user
     *            is the user name to set for the default route.
     *  
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Add a route list extracted from a record route list. If this is a server
     * dialog then we assume that the record are added to the route list IN
     * order. If this is a client dialog then we assume that the record route
     * headers give us the route list to add in reverse order.
     * 
     * @param recordRouteList --
     *            the record route list from the incoming message.
     */

    private void addRoute(RecordRouteList recordRouteList) {
        if (this.isClientDialog()) {
            // This is a client dialog so we extract the record
            // route from the response and reverse its order to
            // careate a route list.
            this.routeList = new RouteList();
            // start at the end of the list and walk backwards
            ListIterator li = recordRouteList.listIterator(recordRouteList
                    .size());
            while (li.hasPrevious()) {
                RecordRoute rr = (RecordRoute) li.previous();
                AddressImpl addr = (AddressImpl) rr.getAddress();
                Route route = new Route();
                route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress())
                        .clone());
                route.setParameters((NameValueList) rr.getParameters().clone());
                this.routeList.add(route);
            }
        } else {
            // This is a server dialog. The top most record route
            // header is the one that is closest to us. We extract the
            // route list in the same order as the addresses in the
            // incoming request.
            this.routeList = new RouteList();
            ListIterator li = recordRouteList.listIterator();
            while (li.hasNext()) {
                RecordRoute rr = (RecordRoute) li.next();
                Route route = new Route();
                route.setAddress((AddressImpl) ((AddressImpl) rr.getAddress())
                        .clone());
                route.setParameters((NameValueList) rr.getParameters().clone());
                routeList.add(route);
            }
        }
    }

    /**
     * Add a route list extacted from the contact list of the incoming message.
     * 
     * @param contactList --
     *            contact list extracted from the incoming message.
     *  
     */

    private void addRoute(ContactList contactList) {
        if (contactList.size() == 0)
            return;
        Contact contact = (Contact) contactList.getFirst();
        Route route = new Route();
        route.setAddress((AddressImpl) ((AddressImpl) (contact.getAddress()))
                .clone());
        this.contactRoute = route;
    }

    /**
     * Extract the route information from this SIP Message and add the relevant
     * information to the route set.
     * 
     * @param sipMessage
     *            is the SIP message for which we want to add the route.
     */
    public synchronized void addRoute(SIPMessage sipMessage) {

        // cannot add route list after the dialog is initialized.
        try {
            if (LogWriter.needsLogging) {
                sipStack.logWriter.logMessage("addRoute: dialogState: " + this
                        + "state = " + this.getState());
            }
            if (this.dialogState == CONFIRMED_STATE
                    || this.dialogState == COMPLETED_STATE
                    || this.dialogState == TERMINATED_STATE)
                return;

            if (!isServer()) {
                // I am CLIENT dialog.
                if (sipMessage instanceof SIPResponse) {
                    SIPResponse sipResponse = (SIPResponse) sipMessage;
                    if (sipResponse.getStatusCode() == 100) {
                        // Do nothing for trying messages.
                        return;
                    }
                    RecordRouteList rrlist = sipMessage.getRecordRouteHeaders();
                    // Add the route set from the incoming response in reverse
                    // order
                    if (rrlist != null) {
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
            } else {
                if (sipMessage instanceof SIPRequest) {
                    // Incoming Request has the route list
                    RecordRouteList rrlist = sipMessage.getRecordRouteHeaders();
                    // Add the route set from the incoming response in reverse
                    // order
                    if (rrlist != null) {

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
            if (LogWriter.needsLogging) {
                sipStack.logWriter.logStackTrace();
                sipStack.logWriter
                        .logMessage("added a route = " + routeList.encode()
                                + "contactRoute = " + contactRoute);

            }
        }
    }

    /**
     * Protected Dialog constructor.
     */
    protected SIPDialog() {
        this.pendingRecords = new Hashtable();
        this.routeList = new RouteList();
        this.dialogState = -1; // not yet initialized.
        localSequenceNumber = 0;
        remoteSequenceNumber = -1;

    }

    /**
     * Set the dialog identifier.
     */
    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    /**
     * Constructor given the first transaction.
     * 
     * @param transaction
     *            is the first transaction.
     */
    protected SIPDialog(SIPTransaction transaction) {
        this();
        this.sipStack = transaction.sipStack;
        this.addTransaction(transaction);
    }

    /**
     * Return true if is server.
     * 
     * @return true if is server transaction created this dialog.
     */
    public boolean isServer() {
        return this.firstTransaction instanceof SIPServerTransaction;

    }

    /**
     * Return true if this is a re-establishment of the dialog.
     * 
     * @return true if the reInvite flag is set.
     */
    protected boolean isReInvite() {
        return this.reInviteFlag;
    }

    /**
     * Get the id for this dialog.
     * 
     * @return the string identifier for this dialog.
     *  
     */
    public String getDialogId() {

        if (firstTransaction instanceof SIPServerTransaction) {
            if (this.originalRequest != null)
                this.dialogId = originalRequest.getDialogId(true, this.myTag);
        } else {
            if (this.getFirstTransaction() != null
                    && ((SIPClientTransaction) this.getFirstTransaction())
                            .getLastResponse() != null) {
                this.dialogId = ((SIPClientTransaction) firstTransaction)
                        .getLastResponse().getDialogId(false, this.hisTag);
            }

        }

        return this.dialogId;
    }

    /**
     * Add a transaction record to the dialog.
     * 
     * @param transaction
     *            is the transaction to add to the dialog.
     */
    public void addTransaction(SIPTransaction transaction) {

        SIPRequest sipRequest = (SIPRequest) transaction.getOriginalRequest();

        // Proessing a re-invite.
        if (firstTransaction != null && firstTransaction != transaction
                && transaction.getMethod().equals(firstTransaction.getMethod())) {
            this.reInviteFlag = true;
        }

        // Set state to Completed if we are processing a
        // BYE transaction for the dialog.
        // Will be set to TERMINATED after the BYE
        // transaction completes.

        if (sipRequest.getMethod().equals(Request.BYE)) {
            this.setState(COMPLETED_STATE);
        }

        if (firstTransaction == null) {
            // Record the local and remote sequenc
            // numbers and the from and to tags for future
            // use on this dialog.
            firstTransaction = transaction;
            this.setLocalParty(sipRequest);
            this.setRemoteParty(sipRequest);
            this.setCallId(sipRequest);
            this.originalRequest = sipRequest;

            if (transaction instanceof SIPServerTransaction) {
                hisTag = sipRequest.getFrom().getTag();
                // My tag is assigned when sending response
            } else {
                setLocalSequenceNumber(sipRequest.getCSeq().getSequenceNumber());
                // his tag is known when receiving response
		// Exception test suggested by John Barton
                myTag = sipRequest.getFrom().getTag();
                if (myTag == null)
                    throw new RuntimeException("The request's From header is missing the required Tag parameter.");
            }
        } else if (transaction.getMethod().equals(firstTransaction.getMethod())
                && (((firstTransaction instanceof SIPServerTransaction) && (transaction instanceof SIPClientTransaction)) || ((firstTransaction instanceof SIPClientTransaction) && (transaction instanceof SIPServerTransaction)))) {
            // Switch from client side to server side for re-invite
            //  (put the other side on hold).

            firstTransaction = transaction;
            this.setLocalParty(sipRequest);
            this.setRemoteParty(sipRequest);
            this.setCallId(sipRequest);
            this.originalRequest = sipRequest;

        }
        if (transaction instanceof SIPServerTransaction)
            setRemoteSequenceNumber(sipRequest.getCSeq().getSequenceNumber());

        // If this is a server transaction record the remote
        // sequence number to avoid re-processing of requests
        // with the same sequence number directed towards this
        // dialog.

        this.lastTransaction = transaction;
        // set a back ptr in the incoming dialog.
        transaction.setDialog(this);
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("Transaction Added " + this + myTag
                    + "/" + hisTag);
            sipStack.logWriter.logMessage("TID = "
                    + transaction.getTransactionId() + "/"
                    + transaction.IsServerTransaction());
            sipStack.logWriter.logStackTrace();
        }
    }

    /**
     * Set the remote tag.
     * 
     * @param hisTag
     *            is the remote tag to set.
     */
    public void setRemoteTag(String hisTag) {
        this.hisTag = hisTag;
    }

    /**
     * Get the last transaction from the dialog.
     */
    public SIPTransaction getLastTransaction() {
        return this.lastTransaction;
    }

    /**
     * Get the INVITE transaction (null if no invite transaction).
     */
    public SIPServerTransaction getInviteTransaction() {
        if (this.timerTask != null)
            return this.timerTask.transaction;
        else
            return null;
    }

    /**
     * Set the local sequece number for the dialog (defaults to 1 when the
     * dialog is created).
     * 
     * @param lCseq
     *            is the local cseq number.
     *  
     */
    protected void setLocalSequenceNumber(int lCseq) {
        this.localSequenceNumber = lCseq;
    }

    /**
     * Set the remote sequence number for the dialog.
     * 
     * @param rCseq
     *            is the remote cseq number.
     *  
     */
    public void setRemoteSequenceNumber(int rCseq) {
        if (LogWriter.needsLogging)
            sipStack.logWriter.logMessage("setRemoteSeqno " + this + "/"
                    + rCseq);
        this.remoteSequenceNumber = rCseq;
    }

    /**
     * Increment the local CSeq # for the dialog. This is useful for if you want
     * to create a hole in the sequence number i.e. route a request outside the
     * dialog and then resume within the dialog.
     */
    public void incrementLocalSequenceNumber() {
        ++this.localSequenceNumber;
    }

    /**
     * Get the remote sequence number (for cseq assignment of outgoing requests
     * within this dialog).
     * 
     * @return local sequence number.
     */
    public int getRemoteSequenceNumber() {
        return this.remoteSequenceNumber;
    }

    /**
     * Get the local sequence number (for cseq assignment of outgoing requests
     * within this dialog).
     * 
     * @return local sequence number.
     */

    public int getLocalSequenceNumber() {
        return this.localSequenceNumber;
    }

    /**
     * Get local identifier for the dialog. This is used in From header tag
     * construction for all outgoing client transaction requests for this dialog
     * and for all outgoing responses for this dialog. This is used in To tag
     * constuction for all outgoing transactions when we are the server of the
     * dialog. Use this when constucting To header tags for BYE requests when we
     * are the server of the dialog.
     * 
     * @return the local tag.
     */
    public String getLocalTag() {
        return this.myTag;
    }

    /**
     * Get peer identifier identifier for the dialog. This is used in To header
     * tag construction for all outgoing requests when we are the client of the
     * dialog. This is used in From tag construction for all outgoing requests
     * when we are the Server of the dialog. Use this when costructing From
     * header Tags for BYE requests when we are the server of the dialog.
     * 
     * @return the remote tag (note this is read from a response to an INVITE).
     *  
     */
    public String getRemoteTag() {
        return hisTag;
    }

    /**
     * Set local tag for the transaction.
     * 
     * @param mytag
     *            is the tag to use in From headers client transactions that
     *            belong to this dialog and for generating To tags for Server
     *            transaction requests that belong to this dialog.
     */
    public void setLocalTag(String mytag) {
        if (LogWriter.needsLogging) {
            sipStack.logWriter.logMessage("set Local tag " + mytag + " "
                    + this.dialogId);
            sipStack.logWriter.logStackTrace();
        }

        this.myTag = mytag;

    }

    /**
     * Mark all the transactions in the dialog inactive and ready for garbage
     * collection.
     */
    protected void deleteTransactions() {
        this.firstTransaction = null;
        this.lastTransaction = null;
    }

    /**
     * This method will release all resources associated with this dialog that
     * are tracked by the Provider. Further references to the dialog by incoming
     * messages will result in a mismatch. Since dialog destruction is left
     * reasonably open ended in RFC3261, this delete method is provided for
     * future use and extension methods that do not require a BYE to terminate a
     * dialogue. The basic case of the INVITE and all dialogues that we are
     * aware of today it is expected that BYE requests will end the dialogue.
     */

    public void delete() {
        // the reaper will get him later.
        this.setState(TERMINATED_STATE);
    }

    /**
     * Returns the Call-ID for this SipSession. This is the value of the Call-ID
     * header for all messages belonging to this session.
     * 
     * @return the Call-ID for this Dialogue
     */
    public CallIdHeader getCallId() {
        return this.callIdHeader;
    }

    private void setCallId(SIPRequest sipRequest) {
        this.callIdHeader = sipRequest.getCallId();
    }

    /**
     * Get the local Address for this dialog.
     * 
     * @return the address object of the local party.
     */

    public javax.sip.address.Address getLocalParty() {
        return this.localParty;
    }

    private void setLocalParty(SIPRequest sipRequest) {
        if (!isServer()) {
            this.localParty = sipRequest.getFrom().getAddress();
        } else {
            this.localParty = sipRequest.getTo().getAddress();
        }
    }

    /**
     * Returns the Address identifying the remote party. This is the value of
     * the To header of locally initiated requests in this dialogue when acting
     * as an User Agent Client.
     * <p>
     * This is the value of the From header of recieved responses in this
     * dialogue when acting as an User Agent Server.
     * 
     * @return the address object of the remote party.
     */
    public javax.sip.address.Address getRemoteParty() {
        return this.remoteParty;

    }

    private void setRemoteParty(SIPRequest sipRequest) {
        if (!isServer()) {
            this.remoteParty = sipRequest.getTo().getAddress();
        } else {
            this.remoteParty = sipRequest.getFrom().getAddress();
        }
    }

    /**
     * Returns the Address identifying the remote target. This is the value of
     * the Contact header of recieved Responses for Requests or refresh Requests
     * in this dialogue when acting as an User Agent Client
     * <p>
     * This is the value of the Contact header of recieved Requests or refresh
     * Requests in this dialogue when acting as an User Agent Server. Bug fix
     * sent in by Steve Crossley.
     * 
     * @return the address object of the remote target.
     */
    public javax.sip.address.Address getRemoteTarget() {
        if (this.contactRoute == null)
            return null;
        return this.contactRoute.getAddress();
    }

    /**
     * Returns the current state of the dialogue. The states are as follows:
     * <ul>
     * <li>Early - A dialog is in the "early" state, which occurs when it is
     * created when a provisional response is recieved to the INVITE Request.
     * <li>Confirmed - A dialog transitions to the "confirmed" state when a 2xx
     * final response is received to the INVITE Request.
     * <li>Completed - A dialog transitions to the "completed" state when a BYE
     * request is sent or received by the User Agent Client.
     * <li>Terminated - A dialog transitions to the "terminated" state when it
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
        if (this.dialogState == -1)
            return null; // not yet initialized
        return DialogState.getObject(this.dialogState);
    }

    /**
     * Returns true if this Dialog is secure i.e. if the request arrived over
     * TLS, and the Request-URI contained a SIPS URI, the "secure" flag is set
     * to TRUE.
     * 
     * @return <code>true</code> if this dialogue was established using a sips
     *         URI over TLS, and <code>false</code> otherwise.
     */
    public boolean isSecure() {
        return this.getFirstTransaction().getRequest().getRequestURI()
                .getScheme().equalsIgnoreCase("sips");
    }

    /**
     * Sends ACK Request to the remote party of this Dialogue.
     * 
     * @param request
     *            the new ACK Request message to send.
     * @throws SipException
     *             if implementation cannot send the ACK Request for any other
     *             reason
     */
    public void sendAck(Request request) throws SipException {
        SIPRequest ackRequest = (SIPRequest) request;
        if (LogWriter.needsLogging)
            sipStack.logWriter.logMessage("sendAck" + this);
        // Loosen up check for re-invites (Andreas B)
        // if (this.isServer())
        //   throw new SipException
        // ("Cannot sendAck from Server side of Dialog");

        if (!ackRequest.getMethod().equals(Request.ACK)
                && !ackRequest.getMethod().equals(Request.PRACK))
            throw new SipException("Bad request method -- should be ACK");
        if (ackRequest.getMethod().equals(Request.ACK)
                && (this.getState() == null || this.getState().getValue() == EARLY_STATE)) {
            throw new SipException("Bad dialog state " + this.getState());
        } else if (ackRequest.getMethod().equals(Request.PRACK)
                && this.getState() == null) {
            throw new SipException("Bad dialog state sending PRACK"
                    + this.getState());
        }

        // Set the dialog state to CONFIRMED when the Prack is sent
        // out.
        if (ackRequest.getMethod().equals(Request.PRACK)
                && this.getState().getValue() == EARLY_STATE) {
            this.setState(CONFIRMED_STATE);
        }

        if (!this.getCallId().getCallId().equals(
                ((SIPRequest) request).getCallId().getCallId())) {
            throw new SipException("Bad call ID in request");
        }
        try {
            if (LogWriter.needsLogging) {
                sipStack.logWriter
                        .logMessage("setting from tag For outgoing ACK= "
                                + this.getLocalTag());
                sipStack.logWriter
                        .logMessage("setting To tag for outgoing ACK = "
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
            if (rl.size() > 0) {
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
                    if (rl.size() > 0)
                        ackRequest.addHeader(rl);
                    if (contactRoute != null)
                        ackRequest.addHeader(contactRoute);
                }
            } else {
                if (this.getRemoteTarget() != null)
                    ackRequest.setRequestURI(this.getRemoteTarget().getURI());
            }
        }
        HopImpl hop = this.getNextHop();
        try {
            MessageChannel messageChannel = sipStack
                    .createRawMessageChannel(this.firstTransaction.getPort(),hop);
            if (messageChannel == null) {
                // Bug fix from Antonis Karydas
                // At this point the procedures of 8.1.2
                // and 12.2.1.1 of RFC3261 have been tried
                // but the resulting next hop cannot be resolved
                // (recall that the exception thrown
                // is caught and ignored in SIPStack.createMessageChannel()
                // so we end up here with a null messageChannel
                // instead of the exception handler below).
                // All else failing, try the outbound proxy in accordance
                // with 8.1.2, in particular:
                // This ensures that outbound proxies that do not add
                // Record-Route header field values will drop out of
                // the path of subsequent requests. It allows endpoints
                // that cannot resolve the first Route
                // URI to delegate that task to an outbound proxy.
                //
                // if one considers the 'first Route URI' of a
                // request constructed according to 12.2.1.1
                // to be the request URI when the route set is empty.
                Hop outboundProxy = sipStack.getRouter().getOutboundProxy();
                if (outboundProxy == null)
                    throw new SipException("No route found!");
                messageChannel = sipStack
                  .createRawMessageChannel(this.firstTransaction.getPort(), outboundProxy);

            }
            // Wrap a client transaction around the raw message channel.
            SIPClientTransaction clientTransaction = (SIPClientTransaction) sipStack
                    .createMessageChannel(messageChannel);
            clientTransaction.setOriginalRequest(ackRequest);
            clientTransaction.sendMessage((SIPMessage) ackRequest);
            this.lastAck = ackRequest;
            // Do not retransmit the ACK so terminate the transaction
            // immediately.
            clientTransaction.setState(SIPTransaction.TERMINATED_STATE);
        } catch (Exception ex) {
            if (LogWriter.needsLogging)
                sipStack.logWriter.logException(ex);
            throw new SipException("Cold not create message channel");
        }

    }

    /**
     * Creates a new Request message based on the dialog creating request. This
     * method should be used for but not limited to creating Bye's, Refer's and
     * re-Invite's on the Dialog. The returned Request will be correctly
     * formatted that is it will contain the correct CSeq header, Route headers
     * and requestURI (derived from the remote target). This method should not
     * be used for Ack, that is the application should create the Ack from the
     * MessageFactory.
     * 
     * If the route set is not empty, and the first URI in the route set
     * contains the lr parameter (see Section 19.1.1), the UAC MUST place the
     * remote target URI into the Request-URI and MUST include a Route header
     * field containing the route set values in order, including all parameters.
     * If the route set is not empty, and its first URI does not contain the lr
     * parameter, the UAC MUST place the first URI from the route set into the
     * Request-URI, stripping any parameters that are not allowed in a
     * Request-URI. The UAC MUST add a Route header field containing the
     * remainder of the route set values in order, including all parameters. The
     * UAC MUST then place the remote target URI into the Route header field as
     * the last value.
     * 
     * @param method
     *            the string value that determines if the request to be created.
     * @return the newly created Request message on this Dialog.
     * @throws SipException
     *             if the Dialog is not yet established.
     */
    public Request createRequest(String method) throws SipException {
        // Check if the dialog is in the right state (RFC 3261 section 15).
        // The caller's UA MAY send a BYE for either
        // CONFIRMED or EARLY dialogs, and the callee's UA MAY send a BYE on
        // CONFIRMED dialogs, but MUST NOT send a BYE on EARLY dialogs.
        if (method == null)
            throw new NullPointerException("null method");
        else if (this.getState() == null
                || (this.getState().getValue() == TERMINATED_STATE && !method
                        .equalsIgnoreCase(Request.BYE))
                || (this.isServer()
                        && this.getState().getValue() == EARLY_STATE && method
                        .equalsIgnoreCase(Request.BYE)))
            throw new SipException("Dialog  " + getDialogId()
                    + " not yet established or terminated " + this.getState());

        RequestLine requestLine = new RequestLine();
        requestLine.setUri((GenericURI) getRemoteParty().getURI());
        requestLine.setMethod(method);

        SIPRequest sipRequest = originalRequest.createSIPRequest(requestLine,
                this.isServer());

        try {
            // Guess of local sequence number - this is being re-set when
            // the request is actually dispatched ( reported by Brad Templeton
            // and Antonis Karydas).
            if (!method.equals(Request.ACK)) {
                CSeq cseq = (CSeq) sipRequest.getCSeq();
                cseq.setSequenceNumber(this.localSequenceNumber + 1);
            } else {
                // This is an ACK request. Get the last transaction and
                // assign a seq number from there.
                // Bug noticed by Andreas Bystrom.
                SIPTransaction transaction = this.lastTransaction;
                if (transaction == null)
                    throw new SipException("Could not create ack!");
                SIPResponse response = (SIPResponse) this.lastTransaction
                        .getLastResponse();
                if (response == null)
                    throw new SipException("Could not find response!");
                int seqno = response.getCSeq().getSequenceNumber();
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
            MessageProcessor messageProcessor = sipStack
                    .getMessageProcessor(firstTransaction.encapsulatedChannel
                            .getTransport());
            Via via = messageProcessor.getViaHeader();
            sipRequest.addHeader(via);
        }

        From from = (From) sipRequest.getFrom();
        To to = (To) sipRequest.getTo();

        try {
            if (this.getLocalTag() != null)
                from.setTag(this.getLocalTag());
            if (this.getRemoteTag() != null)
                to.setTag(this.getRemoteTag());
        } catch (ParseException ex) {
            InternalErrorHandler.handleException(ex);
        }

        // get the route list from the dialog.
        RouteList rl = this.getRouteList();

        // Add it to the header.
        if (rl.size() > 0) {
            Route route = (Route) rl.getFirst();
            SipURI sipUri = (SipUri) route.getAddress().getURI();
            if (sipUri.hasLrParam()) {
                if (this.getRemoteTarget() != null)
                    sipRequest.setRequestURI(this.getRemoteTarget().getURI());
                sipRequest.addHeader(rl);
            } else {
                // First route is not a lr
                // Add the contact route to the end.
                rl.removeFirst();
                sipRequest.setRequestURI(sipUri);

                // Bug report from Brad Templeton
                // Check for 0 size - Bug report from Andreas Bystrom.
                if (rl.size() > 0)
                    sipRequest.addHeader(rl);
                if (this.contactRoute != null)
                    sipRequest.addHeader(contactRoute);
            }
        } else {
            // Bug report from Antonis Karydas
            if (this.getRemoteTarget() != null)
                sipRequest.setRequestURI(this.getRemoteTarget().getURI());
        }
        // Set the transport to be the same for the outgoing request.
        try {
            if (sipRequest.getRequestURI() instanceof SipUri) {
                ((SipUri) sipRequest.getRequestURI())
                        .setTransportParam(sipRequest.getTopmostVia()
                                .getTransport());
            }
        } catch (ParseException ex) {
        }
        return sipRequest;

    }

    /**
     * Sends a Request to the remote party of this dialog. This method implies
     * that the application is functioning as UAC hence the underlying
     * SipProvider acts statefully. This method is useful for sending Bye's for
     * terminating a dialog or Re-Invites on the Dialog for third party call
     * control.
     * <p>
     * This methods will set the From and the To tags for the outgoing request
     * and also set the correct sequence number to the outgoing Request and
     * associate the client transaction with this dialog. Note that any tags
     * assigned by the user will be over-written by this method.
     * <p>
     * The User Agent must not send a BYE on a confirmed INVITE until it has
     * received an ACK for its 2xx response or until the server transaction
     * timeout is received.
     * <p>
     * When the retransmissionFilter is <code>true</code>, that is the
     * SipProvider takes care of all retransmissions for the application, and
     * the SipProvider can not deliver the Request after multiple retransmits
     * the SipListener will be notified with a {@link TimeoutEvent}when the
     * transaction expires.
     * 
     * @param clientTransactionId
     *            the new ClientTransaction object identifying this transaction,
     *            this clientTransaction should be requested from
     *            SipProvider.getNewClientTransaction
     * @throws TransactionDoesNotExistException
     *             if the serverTransaction does not correspond to any existing
     *             server transaction.
     * @throws SipException
     *             if implementation cannot send the Request for any reason.
     */
    public void sendRequest(ClientTransaction clientTransactionId)
            throws TransactionDoesNotExistException, SipException {

        SIPRequest dialogRequest = ((SIPClientTransaction) clientTransactionId)
                .getOriginalRequest();
        if (clientTransactionId == null)
            throw new NullPointerException("null parameter");

        if (dialogRequest.getMethod().equals(Request.ACK)
                || dialogRequest.getMethod().equals(Request.CANCEL))
            throw new SipException("Bad Request Method. "
                    + dialogRequest.getMethod());
        // Cannot send bye until the dialog has been established.
        if (this.getState() == null) {
            throw new SipException("Bad dialog state " + this.getState());

        }

        if (LogWriter.needsLogging)
            sipStack.logWriter.logMessage("dialog.sendRequest " + " dialog = "
                    + this + "\ndialogRequest = \n" + dialogRequest);

        if (dialogRequest.getTopmostVia() == null) {
            Via via = ((SIPClientTransaction) clientTransactionId)
                    .getOutgoingViaHeader();
            dialogRequest.addHeader(via);
        }

        if (!this.getCallId().getCallId().equals(
                dialogRequest.getCallId().getCallId())) {
            throw new SipException("Bad call ID in request");
        }

        // Set the dialog back pointer.
        ((SIPClientTransaction) clientTransactionId).dialog = this;

        this.addTransaction((SIPTransaction) clientTransactionId);
        // Enable the retransmission filter for the transaction

        ((SIPClientTransaction) clientTransactionId).isMapped = true;

        From from = (From) dialogRequest.getFrom();
        To to = (To) dialogRequest.getTo();

        try {
            if (this.getLocalTag() != null)
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
            if (rl.size() > 0) {
                Route route = (Route) rl.getFirst();
                SipURI sipUri = (SipUri) route.getAddress().getURI();
                if (sipUri.hasLrParam()) {
                    dialogRequest
                            .setRequestURI(this.getRemoteTarget().getURI());
                    dialogRequest.addHeader(rl);
                } else {
                    // First route is not a lr
                    // Add the contact route to the end.
                    rl.removeFirst();
                    dialogRequest.setRequestURI(sipUri);
                    // Check for 0 size - Bug report from Andreas Bystrom.
                    if (rl.size() > 0)
                        dialogRequest.addHeader(rl);
                    if (contactRoute != null)
                        dialogRequest.addHeader(contactRoute);
                }
            } else {
                // Bug report from Antonis Karydas
                if (this.getRemoteTarget() != null)
                    dialogRequest
                            .setRequestURI(this.getRemoteTarget().getURI());
            }
        }

        // 2543 Backward compatibility hack contributed by
        // Steve Crossley (Nortel Networks).
        HopImpl hop;
        try {
            hop = this.getNextHop();
        } catch (SipException se) {

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
            TCPMessageChannel oldChannel = null;
	    TLSMessageChannel oldTLSChannel = null;

	   
            MessageChannel messageChannel = sipStack.createRawMessageChannel(this.firstTransaction.getPort(),hop);
            if (((SIPClientTransaction) clientTransactionId).encapsulatedChannel instanceof TCPMessageChannel) {
                // Remove this from the connection cache if it is in the
                // connection
                // cache and is not yet active.
                oldChannel = (TCPMessageChannel)
                ((SIPClientTransaction)clientTransactionId).encapsulatedChannel;
                if (oldChannel.isCached && ! oldChannel.isRunning) {
                    oldChannel.uncache();
                }
		// Not configured to cache client connections.
		if ( !sipStack.cacheClientConnections  )  {
			oldChannel.useCount --;
			if (LogWriter.needsLogging) 
				sipStack.logWriter.logMessage("oldChannel: useCount " + oldChannel.useCount);

		}
            }
	    // This section is copied & pasted from the previous one,
	    // and then modified for TLS management (Daniel Martinez)
            else if ( ((SIPClientTransaction) clientTransactionId).encapsulatedChannel
            	instanceof TLSMessageChannel )  {
                // Remove this from the connection cache if it is in the connection
                // cache and is not yet active.
                oldTLSChannel = (TLSMessageChannel)
                ((SIPClientTransaction)clientTransactionId).encapsulatedChannel;
                if (oldTLSChannel.isCached && ! oldTLSChannel.isRunning) {
                    oldTLSChannel.uncache();
                }
		// Not configured to cache client connections.
		if ( !sipStack.cacheClientConnections  )  {
			oldTLSChannel.useCount --;
			if (LogWriter.needsLogging) 
				sipStack.logWriter.logMessage("oldChannel: useCount " + oldTLSChannel.useCount);
                }
            }
            ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel(messageChannel);

            if (messageChannel == null) {
                // Bug fix from Antonis Karydas
                // At this point the procedures of 8.1.2
                // and 12.2.1.1 of RFC3261 have been tried
                // but the resulting next hop cannot be resolved
                // (recall that the exception thrown
                // is caught and ignored in SIPStack.createMessageChannel()
                // so we end up here with a null messageChannel
                // instead of the exception handler below).
                // All else failing, try the outbound proxy in accordance
                // with 8.1.2, in particular:
                // This ensures that outbound proxies that do not add
                // Record-Route header field values will drop out of
                // the path of subsequent requests. It allows endpoints
                // that cannot resolve the first Route
                // URI to delegate that task to an outbound proxy.
                //
                // if one considers the 'first Route URI' of a
                // request constructed according to 12.2.1.1
                // to be the request URI when the route set is empty.
		if (LogWriter.needsLogging) 
			sipStack.logWriter.logMessage("Null message channel using outbound proxy !" );
                Hop outboundProxy = sipStack.getRouter().getOutboundProxy();
                if (outboundProxy == null)
                    throw new SipException("No route found!");
                messageChannel = sipStack
                        .createRawMessageChannel(this.firstTransaction.getPort(),outboundProxy);
                ((SIPClientTransaction) clientTransactionId).setEncapsulatedChannel (messageChannel);
            } else {
		if (LogWriter.needsLogging) 
			sipStack.logWriter.logMessage("using message channel " + messageChannel );
	    }

            if (messageChannel != null &&
	        messageChannel instanceof TCPMessageChannel ) 
	        ((TCPMessageChannel) messageChannel).useCount ++;
            if (messageChannel != null &&
	        messageChannel instanceof TLSMessageChannel ) 
	        ((TLSMessageChannel) messageChannel).useCount ++;
	    // See if we need to release the previously mapped channel.
	    if (  ( ! sipStack.cacheClientConnections ) && 
		oldChannel != null  && 
		oldChannel.useCount == 0 ) 
		oldChannel.close();
	    if (  ( ! sipStack.cacheClientConnections ) && 
		oldTLSChannel != null  && 
		oldTLSChannel.useCount == 0 ) 
		oldTLSChannel.close();
        } catch (Exception ex) {
            if (LogWriter.needsLogging)
                sipStack.logWriter.logException(ex);
            throw new SipException("Cold not create message channel");
        }

        try {
            // Increment before setting!!
            localSequenceNumber++;
            dialogRequest.getCSeq().setSequenceNumber(getLocalSequenceNumber());
        } catch (InvalidArgumentException ex) {
            ex.printStackTrace();
        }

        if (this.isServer()) {
            SIPServerTransaction serverTransaction = (SIPServerTransaction) this
                    .getFirstTransaction();

            try {
                if (this.myTag != null)
                    from.setTag(this.myTag);
                if (this.hisTag != null)
                    to.setTag(this.hisTag);
            } catch (ParseException ex) {
                throw new SipException(ex.getMessage());
            }

            try {
                ((SIPClientTransaction) clientTransactionId)
                        .sendMessage(dialogRequest);
                // If the method is BYE then mark the dialog completed.
                if (dialogRequest.getMethod().equals(Request.BYE))
                    this.setState(COMPLETED_STATE);
            } catch (IOException ex) {
                throw new SipException("error sending message");
            }
        } else {
            // I am the client so I do not swap headers.

            try {

                if (LogWriter.needsLogging) {
                    sipStack.logWriter.logMessage("setting tags from "
                            + this.getDialogId());
                    sipStack.logWriter.logMessage("fromTag " + this.myTag);
                    sipStack.logWriter.logMessage("toTag " + this.hisTag);
                }

                if (this.myTag != null)
                    from.setTag(this.myTag);
                if (this.hisTag != null)
                    to.setTag(this.hisTag);
            } catch (ParseException ex) {
                throw new SipException(ex.getMessage());
            }

            try {
                ((SIPClientTransaction) clientTransactionId)
                        .sendMessage(dialogRequest);
                // go directly to terminated state.
                if (dialogRequest.getMethod().equalsIgnoreCase(Request.BYE))
                    this.setState(COMPLETED_STATE);
            } catch (IOException ex) {
                if (LogWriter.needsLogging)
                    sipStack.logWriter.logException(ex);
                throw new SipException("error sending message");
            }

        }
    }

    /**
     * Return yes if the last response is to be retransmitted.
     */
    protected boolean toRetransmitFinalResponse() {
        if (--retransmissionTicksLeft == 0) {
            this.retransmissionTicksLeft = 2 * prevRetransmissionTicks;
            this.prevRetransmissionTicks = retransmissionTicksLeft;
            return true;
        } else
            return false;

    }

    protected void setRetransmissionTicks() {
        this.retransmissionTicksLeft = 1;
        this.prevRetransmissionTicks = 1;
    }

    /**
     * Resend the last ack.
     */
    public void resendAck() throws SipException {
        // Check for null.
        // Bug report and fix by Antonis Karydas.
        if (this.lastAck != null)
            this.sendAck(lastAck);
    }

    protected String getMethod() {
        return this.originalRequest.getMethod();
    }
    
    public boolean isInviteDialog() {
        return originalRequest.getMethod().equals(Request.INVITE);
    }

    protected void startTimer(SIPServerTransaction transaction) {
        if (this.timerTask != null && this.timerTask.transaction == transaction) {
            sipStack.logMessage("Timer already running for " + getDialogId());
            return;
        }
        if (LogWriter.needsLogging)
            sipStack.logMessage("Starting dialog timer for " + getDialogId());
        this.ackSeen = false;
        if (this.timerTask != null) {
            this.timerTask.transaction = transaction;
        } else {
            this.timerTask = new DialogTimerTask(this, transaction);
            sipStack.timer.schedule(timerTask, SIPTransactionStack.BASE_TIMER_INTERVAL,
                    SIPTransactionStack.BASE_TIMER_INTERVAL);
        }
        this.setRetransmissionTicks();
    }

    protected void stopTimer() {
        try {
            if (this.timerTask != null)
                this.timerTask.cancel();
        } catch (Exception ex) {
        }
    }

    public void clearPending() {
    }

    public boolean hasPending() {
        synchronized (this.pendingRecords) {
            return nextSeqno != null
                    && this.pendingRecords.containsKey(nextSeqno);
        }
    }

    public void processPending() {
        NistSipMessageHandlerImpl msgHandler = null;
        synchronized (this.pendingRecords) {
            msgHandler = (NistSipMessageHandlerImpl) pendingRecords
                    .remove(nextSeqno);
            if (this.pendingRecords.size() != 0)
                sipStack.putPending(this);
        }
        if (msgHandler != null) {
            msgHandler.processPending();
        }

    }
    
   

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.15  2005/05/06 15:06:49  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 *
 * Added method check while updating dialog state
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
 * Revision 1.14  2005/04/21 00:01:59  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 * Reviewed by:  mranga
 *
 * Adjust remote sequence number when sending out a 491
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
 * Revision 1.13  2005/04/19 15:57:57  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Fixed remote seqno issue reported by Daniel Vazquez
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
 * Revision 1.12  2005/03/29 03:49:59  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  mranga
 *
 * Remove transaction for early bye.
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
 * Revision 1.11  2004/12/07 18:33:16  mranga
 * Submitted by:  Jeff Adams
 * Reviewed by:   M. Ranganathan
 *
 * Timer bug fix
 * Sniffer tool enhancement.
 *
 * Revision 1.10  2004/12/01 19:05:15  mranga
 * Reviewed by:   mranga
 * Code cleanup remove the unused SIMULATION code to reduce the clutter.
 * Fix bug in Dialog state machine.
 *
 * Revision 1.9  2004/11/19 16:22:56  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 * Route bye request to right target (if there is no record routing enabled).
 *
 * Revision 1.8  2004/11/18 18:59:40  mranga
 *
 * Reviewed by:   mranga
 * added debug
 *
 * Revision 1.7  2004/10/28 19:02:51  mranga
 * Submitted by:  Daniel Martinez
 * Reviewed by:   M. Ranganathan
 *
 * Added changes for TLS support contributed by Daniel Martinez
 *
 * Revision 1.6  2004/10/05 16:22:37  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  Xavi Ferro
 * Reviewed by:   mranga
 *
 * Another attempted fix for memory leak.
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
 * Revision 1.5 2004/10/04 16:03:53 mranga Reviewed by:
 * mranga attempted fix for memory leak
 * 
 * Revision 1.4 2004/09/01 02:04:14 xoba Issue number: no particular issue
 * number.
 * 
 * this code passes TCK
 * 
 * fixed multiple javadoc errors throughout javax.* and gov.nist.*
 * 
 * added junit and log4j jars to cvs module, although log4j is not being used
 * yet.
 * 
 * modified and expanded build.xml and fixed javadoc reference to outdated jre
 * documentation (now javadocs hyperlink to jre api documentation). since
 * top-level 'docs' directory already contains cvs-controlled files, i
 * redirected output of javadocs to their own separate directories, which are
 * 'cleaned' along with 'clean' target. also created other javadoc which just
 * outputs javax.* classes for those wishing to develop sip applications without
 * reference to nist.gov.*.
 * 
 * completed switchover to NetworkLayer for network access.
 * 
 * DID NOT modify makefile's.... so, developers beware.
 * 
 * 
 * 
 * 
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 * 
 * Revision 1.3 2004/08/04 18:42:52 mranga Submitted by: alex rootham Reviewed
 * by: mranga fix null ptr.
 * 
 * Revision 1.2 2004/07/29 20:26:15 mranga Submitted by: Alex Rootham Reviewed
 * by: mranga
 * 
 * check for null before putting SipDialog on the pending queue.
 * 
 * Revision 1.1 2004/06/21 04:59:51 mranga Refactored code - no functional
 * changes.
 * 
 * Revision 1.35 2004/06/17 15:36:10 mranga Reviewed by: mranga minor tweaks
 * 
 * Revision 1.34 2004/06/17 15:22:30 mranga Reviewed by: mranga
 * 
 * Added buffering of out-of-order in-dialog requests for more efficient
 * processing of such requests (this is a performance optimization ).
 * 
 * Revision 1.33 2004/06/16 16:31:07 mranga Sequence number checking for
 * in-dialog messages
 * 
 * Revision 1.32 2004/06/16 02:53:19 mranga Submitted by: mranga Reviewed by:
 * implement re-entrant multithreaded listener model.
 * 
 * Revision 1.31 2004/06/15 09:54:44 mranga Reviewed by: mranga re-entrant
 * listener model added. (see configuration property
 * gov.nist.javax.sip.REENTRANT_LISTENER)
 * 
 * Revision 1.30 2004/06/02 13:09:57 mranga Submitted by: Peter Parnes Reviewed
 * by: mranga Fixed illegal state exception.
 * 
 * Revision 1.29 2004/06/01 11:42:58 mranga Reviewed by: mranga timer fix missed
 * starting the transaction timer in a couple of places.
 * 
 * Revision 1.28 2004/05/30 18:55:57 mranga Reviewed by: mranga Move to timers
 * and eliminate the Transaction scanner Thread to improve scalability and
 * reduce cpu usage.
 * 
 * Revision 1.27 2004/04/30 14:03:26 mranga Reviewed by: mranga
 * 
 * Look at maddr URL contact parameter when deciding next hop in dialog.
 * 
 * Revision 1.26 2004/03/30 17:53:55 mranga Reviewed by: mranga more reference
 * counting cleanup
 * 
 * Revision 1.25 2004/03/12 23:26:42 mranga Reviewed by: mranga Fixed a
 * synchronization problem
 * 
 * Revision 1.24 2004/03/09 00:34:44 mranga Reviewed by: mranga Added TCP
 * connection management for client and server side Transactions. See
 * configuration parameter gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS=false
 * Releases Server TCP Connections after linger time
 * gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS=false Releases Client TCP
 * Connections after linger time
 * 
 * Revision 1.23 2004/02/26 14:28:51 mranga Reviewed by: mranga Moved some code
 * around (no functional change) so that dialog state is set when the
 * transaction is added to the dialog. Cleaned up the Shootist example a bit.
 * 
 * Revision 1.22 2004/02/24 22:39:34 mranga Reviewed by: mranga Only terminate
 * the client side dialog when the bye Terminates or times out and not when the
 * bye is initially sent out.
 * 
 * Revision 1.21 2004/02/15 20:49:10 mranga Reviewed by: mranga Return an empty
 * iterator if the route set is empty.
 * 
 * Revision 1.20 2004/02/13 13:55:31 mranga Reviewed by: mranga per the spec,
 * Transactions must always have a valid dialog pointer. Assigned a dummy dialog
 * for transactions that are not assigned to any dialog (such as Message).
 * 
 * Revision 1.19 2004/02/04 22:07:24 mranga Reviewed by: mranga Fix for handling
 * of out of order sequence numbers in the dialog layer.
 * 
 * Revision 1.18 2004/02/04 18:44:18 mranga Reviewed by: mranga check sequence
 * number before delivering event to application.
 * 
 * Revision 1.17 2004/02/03 16:31:50 mranga Reviewed by: mranga finer grained
 * check on bye creation (conform to section 15 of spec).
 * 
 * Revision 1.16 2004/02/03 15:43:48 mranga Reviewed by: mranga check for dialog
 * state when creating bye request.
 * 
 * Revision 1.15 2004/01/27 15:11:06 mranga Submitted by: jeand Reviewed by:
 * mranga If retrans filter enabled then ack should be seen only once by
 * application. Else each retransmitted ack is seen by application.
 * 
 * Revision 1.14 2004/01/27 13:52:11 mranga Reviewed by: mranga Fixed
 * server/user-agent parser. suppress sending ack to TU when retransFilter is
 * enabled and ack is retransmitted.
 * 
 * Revision 1.13 2004/01/22 13:26:33 sverker Issue number: Obtained from:
 * Submitted by: sverker Reviewed by: mranga
 * 
 * Major reformat of code to conform with style guide. Resolved compiler and
 * javadoc warnings. Added CVS tags.
 * 
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number: CVS: If this change addresses one or more issues, CVS:
 * then enter the issue number(s) here. CVS: Obtained from: CVS: If this change
 * has been taken from another system, CVS: then name the system in this line,
 * otherwise delete it. CVS: Submitted by: CVS: If this code has been
 * contributed to the project by someone else; i.e., CVS: they sent us a patch
 * or a set of diffs, then include their name/email CVS: address here. If this
 * is your work then delete this line. CVS: Reviewed by: CVS: If we are doing
 * pre-commit code reviews and someone else has CVS: reviewed your changes,
 * include their name(s) here. CVS: If you have not had it reviewed then delete
 * this line.
 *  
 */
