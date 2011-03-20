/*
 * This software has been contributed by the author to the public domain.
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
package gov.nist.javax.sip.stack;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

public class HandshakeCompletedListenerImpl implements HandshakeCompletedListener {

    private HandshakeCompletedEvent handshakeCompletedEvent;
    private Object eventWaitObject = new Object();

    public HandshakeCompletedListenerImpl(TLSMessageChannel tlsMessageChannel) {
        tlsMessageChannel.setHandshakeCompletedListener(this);
    }

    public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
        this.handshakeCompletedEvent = handshakeCompletedEvent;
        synchronized (eventWaitObject) {
            eventWaitObject.notify();
        }
    }

    /**
     * Gets the event indicating that the SSL handshake has completed. The
     * method waits until the event has been obtained by the listener or a
     * timeout of 5 seconds has elapsed.
     * 
     * @return the handshakeCompletedEvent or null when the timeout elapsed
     */
    public HandshakeCompletedEvent getHandshakeCompletedEvent() {
        try {
            synchronized (eventWaitObject) {
                if (handshakeCompletedEvent == null)
                    eventWaitObject.wait(5000);
            }
        }
        catch (InterruptedException e) {
            // we don't care
        }
        return handshakeCompletedEvent;
    }
}
