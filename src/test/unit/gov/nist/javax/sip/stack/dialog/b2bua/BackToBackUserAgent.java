package test.unit.gov.nist.javax.sip.stack.dialog.b2bua;

import gov.nist.javax.sip.DialogTimeoutEvent;
import gov.nist.javax.sip.ListeningPointExt;
import gov.nist.javax.sip.SipListenerExt;
import gov.nist.javax.sip.SipProviderExt;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionAlreadyExistsException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.RouteHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import test.tck.msgflow.callflows.ProtocolObjects;


public class BackToBackUserAgent implements SipListenerExt {
    
    private HashSet<Dialog> dialogs = new HashSet<Dialog>();
    private ListeningPoint[] listeningPoints = new ListeningPoint[2]; 
    private SipProvider[] providers = new SipProvider[2];
    private MessageFactory messageFactory;
    private Hashtable<Dialog,Response> lastResponseTable = new Hashtable<Dialog,Response>();
    private ProtocolObjects protocolObjects;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    
    public Dialog getPeer(Dialog dialog) {
        Object[] dialogArray = dialogs.toArray();
        if ( dialogArray.length < 2) return null;
        if ( dialogArray[0] == dialog) return (Dialog) dialogArray[1];
        else if ( dialogArray[1] == dialog) return (Dialog) dialogArray[0];
        else return null;
    }
    
    public SipProvider getPeerProvider (SipProvider provider) {
        if ( providers[0] == provider) return providers[1];
        else return providers[0];
    }
    
    public void addDialog(Dialog dialog) {
        this.dialogs.add(dialog);
        System.out.println("Dialogs  " + this.dialogs);
    }
    
    public void forwardRequest(RequestEvent requestEvent, 
            ServerTransaction serverTransaction) throws SipException, ParseException, InvalidArgumentException  {
      
        SipProvider provider = (SipProvider) requestEvent.getSource();
        Dialog dialog = serverTransaction.getDialog();
        Dialog peerDialog = this.getPeer(dialog);
        Request request = requestEvent.getRequest(); 
        
        System.out.println("Dialog " + dialog);
        
        Request newRequest = null;
        if ( peerDialog != null ) {
             newRequest = peerDialog.createRequest(request.getMethod());
        } else {
             newRequest = (Request) request.clone();
             ((SipURI)newRequest.getRequestURI()).setPort(5090);
             newRequest.removeHeader(RouteHeader.NAME);
             FromHeader fromHeader = (FromHeader) newRequest.getHeader(FromHeader.NAME);
             fromHeader.setTag(Long.toString(Math.abs(new Random().nextLong())));
             SipProvider peerProvider = getPeerProvider(provider);
             ViaHeader viaHeader = ((ListeningPointExt) ((SipProviderExt)
                     getPeerProvider(provider)).getListeningPoint("udp")).createViaHeader();
             newRequest.setHeader(viaHeader);
             
        }
        ContactHeader contactHeader = ((ListeningPointExt) ((SipProviderExt)
                                    getPeerProvider(provider)).getListeningPoint("udp")).createContactHeader();
        newRequest.setHeader(contactHeader);
        ClientTransaction clientTransaction = provider.getNewClientTransaction(newRequest);
        clientTransaction.setApplicationData(serverTransaction);
        if (request.getMethod().equals(Request.INVITE)) {
            this.addDialog(clientTransaction.getDialog());
        }
        if ( peerDialog != null ) {
            peerDialog.sendRequest(clientTransaction);
        } else {
            clientTransaction.sendRequest();
        }
        
    }

   
    public void processDialogTimeout(DialogTimeoutEvent timeoutEvent) {
      
    }

    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        
    }
 
     public void processIOException(IOExceptionEvent exceptionEvent) {
        // TODO Auto-generated method stub
        
    }

    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            SipProvider provider = (SipProvider) requestEvent.getSource();
            if (request.getMethod().equals(Request.INVITE)) {
                if (requestEvent.getServerTransaction() == null) {
                    try {
                        ServerTransaction serverTx = provider.getNewServerTransaction(request);
                        this.addDialog(serverTx.getDialog());
                        this.forwardRequest(requestEvent,serverTx);
                    } catch (TransactionAlreadyExistsException ex) {
                        System.err.println("Transaction exists -- ignoring");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        BackToBackUserAgentTest.fail("Unepxected exception");
                    }
                } else {
                    this.forwardRequest(requestEvent,requestEvent.getServerTransaction());
                }
            } else if ( request.getMethod().equals(Request.BYE)) {
                ServerTransaction serverTransaction = requestEvent.getServerTransaction();
                if ( serverTransaction == null ) {
                    serverTransaction = provider.getNewServerTransaction(request);
                }
                this.forwardRequest(requestEvent, serverTransaction);
               
            } else if (request.getMethod().equals(Request.ACK)) {
                Dialog dialog = requestEvent.getDialog();
                Dialog peer = this.getPeer(dialog);
                Response response = this.lastResponseTable.get(peer);
                CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                String method = cseqHeader.getMethod();
                long seqno = cseqHeader.getSeqNumber();
                Request ack = peer.createAck(seqno);
                peer.sendAck(ack);
            }
           
        } catch ( Exception ex) {
            ex.printStackTrace();
            BackToBackUserAgentTest.fail("Unexpected exception forwarding request");
        }
    }

    
    public void processResponse(ResponseEvent responseEvent) {
        try {
            Response response = responseEvent.getResponse();
            Dialog dialog = responseEvent.getDialog();
            this.lastResponseTable.put(dialog, response);
             ServerTransaction serverTransaction = (ServerTransaction)responseEvent.getClientTransaction().getApplicationData();
            Request stRequest = serverTransaction.getRequest();
            Response newResponse = this.messageFactory.createResponse(response.getStatusCode(),stRequest);
            SipProvider provider = (SipProvider)responseEvent.getSource();
            SipProvider peerProvider = this.getPeerProvider(provider);
            ListeningPoint peerListeningPoint = peerProvider.getListeningPoint("udp");
            ContactHeader peerContactHeader = ((ListeningPointExt)peerListeningPoint).createContactHeader();
            newResponse.setHeader(peerContactHeader);
            serverTransaction.sendResponse(newResponse);
        } catch (Exception ex) {
            ex.printStackTrace();
            BackToBackUserAgentTest.fail("Unexpected exception");
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent) {
        // TODO Auto-generated method stub
        
    }

    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
         
    }
    
    public BackToBackUserAgent(int port1, int port2) {
        SipFactory sipFactory = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        Properties properties = new Properties();
        this.protocolObjects = new ProtocolObjects("backtobackua","gov.nist","udp",true,true, false);

     
        try {
            headerFactory = protocolObjects.headerFactory;
            addressFactory = protocolObjects.addressFactory;
            messageFactory = protocolObjects.messageFactory;
            SipStack sipStack = protocolObjects.sipStack;
            ListeningPoint lp1 = sipStack.createListeningPoint("127.0.0.1", port1, "udp");
            ListeningPoint lp2 = sipStack.createListeningPoint("127.0.0.1", port2, "udp");
            SipProvider sp1 = sipStack.createSipProvider(lp1);
            SipProvider sp2 = sipStack.createSipProvider(lp2);
            this.listeningPoints[0] = lp1;
            this.listeningPoints[1] = lp2;
            this.providers[0] = sp1;
            this.providers[1] = sp2;
            sp1.addSipListener(this);
            sp2.addSipListener(this);
        } catch (Exception ex) {
            
        }

    }

}
