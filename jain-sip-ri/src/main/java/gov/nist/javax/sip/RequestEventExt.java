package gov.nist.javax.sip;

import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.message.Request;


/**
 * Extension of the RequestEvent.
 * 
 * 
 */


public class RequestEventExt extends RequestEvent {
    private String remoteIpAddress;
    
    private int    remotePort;
    
    public RequestEventExt(Object source, ServerTransaction serverTransaction, Dialog dialog, Request request) {
        super(source,serverTransaction,dialog,request);
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getRemotePort() {
        return remotePort;
    }
}
