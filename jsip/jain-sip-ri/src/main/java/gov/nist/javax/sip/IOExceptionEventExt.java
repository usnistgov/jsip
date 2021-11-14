/*
 * This source code has been contributed to the public domain by Mobicents
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
 * of the terms of this agreement.
 */
package gov.nist.javax.sip;

import gov.nist.javax.sip.DialogTimeoutEvent.Reason;

import javax.sip.IOExceptionEvent;

/**
 * Created: 09.09.11 15:16
 *
 * @author : Alex Vinogradov
 */
public class IOExceptionEventExt extends IOExceptionEvent {

    private final String myHost;
    private final int myPort;   
    private Reason reason = null;
    public enum Reason {KeepAliveTimeout};

    public IOExceptionEventExt(Object source, Reason reason, String myHost, int myPort, String peerHost, int peerPort, String transport) {
        super(source, peerHost, peerPort, transport);
        this.myHost = myHost;
        this.myPort = myPort; 
        this.reason = reason;
    }
   
    public String getLocalHost() {
        return myHost;
    }

    public int getLocalPort() {
        return myPort;
    }

    public String getPeerHost() {
        return getHost();
    }

    public int getPeerPort() {
        return getPort();
    }
    
    /**
     * The reason for the Dialog Timeout Event being delivered to the application.
     * 
     * @return the reason for the timeout event.
     */
    public Reason getReason() {
    	return reason;
    }

    @Override
    public String toString() {
        return "KeepAliveTimeoutEvent{" +
                "myHost='" + myHost + '\'' +
                ", myPort=" + myPort +
                ", peerHost='" + getHost() + '\'' +
                ", peerPort=" + getPort() +
                ", transport='" + getTransport() + '\'' +
                '}';
    }
}
