
package examples.shootist;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.message.*;
import java.util.*;
import java.io.*;
import java.net.*;


/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme 
 * is the guy that gets shot.
 *
 *@author M. Ranganathan
 */

public class Shootme implements SipListener {
    
    private static AddressFactory addressFactory;
    private static MessageFactory messageFactory;
    private static HeaderFactory  headerFactory;
    private static SipStack    sipStack;
    
    protected   ServerTransaction   inviteTid;
    
    
    
    protected static final String usageString =
    "java " +
     "examples.shootist.Shootist \n"+
    ">>>> is your class path set to the root?";
    
    private static void usage() {
        System.out.println(usageString);
        System.exit(0);
        
    }
    
    public void
    processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransactionId =
        requestEvent.getServerTransaction();
        
        System.out.println
        ("\n\nRequest " + request.getMethod() +
        " received at " + sipStack.getStackName() +
        " with server transaction id " + serverTransactionId);
        
	if (request.getMethod().equals(Request.INVITE)) {
	    processInvite(requestEvent, serverTransactionId);
	} else if (request.getMethod().equals(Request.ACK)) {
	    processAck(requestEvent, serverTransactionId);
	} else if (request.getMethod().equals(Request.BYE)) {
            processBye(requestEvent,serverTransactionId);
	}
        
    }

   /** Process the ACK request. Send the bye and complete the call flow.
   */
   public void processAck
	(RequestEvent requestEvent, ServerTransaction serverTransaction) {
	SipProvider sipProvider = (SipProvider) requestEvent.getSource();
	try {
		System.out.println("shootme: got an ACK -- sending bye ");
 	Dialog dialog = inviteTid.getDialog();
	Request byeRequest = dialog.createRequest(Request.BYE);
	ClientTransaction tr = 
		sipProvider.getNewClientTransaction(byeRequest);
	dialog.sendRequest(tr);
	} catch (Exception ex) {
		ex.printStackTrace();
		System.exit(0);
	}
   }

   /** Process the invite request.
    */
    public void processInvite
	(RequestEvent requestEvent, ServerTransaction serverTransaction) {
	SipProvider sipProvider = (SipProvider) requestEvent.getSource();
	Request request = requestEvent.getRequest();
	try {
		System.out.println("shootme: got an Invite sending OK");
		System.out.println("shootme:  " + request );
		Response response = messageFactory.createResponse(200,request);
		ToHeader toHeader = (ToHeader) 
				response.getHeader(ToHeader.NAME);
		toHeader.setTag("4321"); // Application is supposed to set.
		Address address = addressFactory.createAddress(
			"Shootme <sip:127.0.0.1:5070>");
		ContactHeader contactHeader = 
			headerFactory.createContactHeader(address);
		response.addHeader(contactHeader);
		ServerTransaction st = 
			sipProvider.getNewServerTransaction(request);
		System.out.println("got a server tranasaction " + st);
		byte[] content = request.getRawContent();
		//System.out.println("Content = " + new String(content));
                ContentTypeHeader contentTypeHeader =
                headerFactory.createContentTypeHeader
                ("application", "sdp");
                response.setContent(content,contentTypeHeader);
		Dialog dialog = st.getDialog();
		if (dialog != null) 
		System.out.println("Dialog state " + dialog.getState());
		st.sendResponse(response);
		this.inviteTid = st;
	} catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
	}
    }

    
   
    
    /** Process the bye request.
     */
    public void processBye
    (RequestEvent requestEvent, ServerTransaction serverTransactionId ) {
	SipProvider sipProvider = (SipProvider) requestEvent.getSource();
	Request request = requestEvent.getRequest();
        try {
            System.out.println("shootme:  got a bye sending OK.");
            Response response = messageFactory.createResponse
                    (200,request,null,null);
            serverTransactionId.sendResponse(response);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(0);
            
        }
    }
    
    
    
    public void
    processResponse(ResponseEvent responseReceivedEvent) {
        System.out.println("Got a response");
        Response response = (Response)responseReceivedEvent.getResponse();
        Transaction tid = responseReceivedEvent.getClientTransaction();
        
        
        
        System.out.println("Response received with client transaction id "
        + tid + ":\n" + response);
        try {
            if (response.getStatusCode() == Response.OK &&
            ((CSeqHeader)response.getHeader(CSeqHeader.NAME)).getMethod().
            equals(Request.INVITE)) {
                if (tid != this.inviteTid) {
                    new Exception().printStackTrace();
                    System.exit(0);
                }
                Dialog dialog = tid.getDialog();
                // Save the tags for the dialog here.
                Request request = tid.getRequest();
                dialog.sendAck(request);
            }
        } catch (SipException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        
    }
    
    public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
        
        System.out.println("Transaction Time out");
    }
    
    
    public void init( ) {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        properties.setProperty("javax.sip.IP_ADDRESS","127.0.0.1");
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
        properties.setProperty("javax.sip.STACK_NAME", "shootme");
	// You need  16 for logging traces. 32 for debug + traces.
	// Your code will limp at 32 but it is best for debugging.
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", 
			"shootmedebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
		"shootmelog.txt");
        
        
        try {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
	    System.out.println("sipStack = " + sipStack);
        } catch(PeerUnavailableException e) {
            // could not find
            // gov.nist.jain.protocol.ip.sip.SipStackImpl
            // in the classpath
	    e.printStackTrace();
            System.err.println(e.getMessage());
	    if (e.getCause() != null) e.getCause().printStackTrace();
	    System.exit(0);
        }
        
        try {
            headerFactory =
            sipFactory.createHeaderFactory();
            addressFactory =
            sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            ListeningPoint lp  
			= sipStack.createListeningPoint (5070,"udp");
            ListeningPoint lp1  = 
			sipStack.createListeningPoint (5070,"tcp");
            
            Shootme listener = this;

            SipProvider sipProvider = sipStack.createSipProvider(lp);
            sipProvider.addSipListener(listener);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            usage();
        }

    }
    
    
    public static void main(String args[])  {
	new Shootme().init();
    }
    
}
