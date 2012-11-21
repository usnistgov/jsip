package gov.nist.javax.sip.stack;

import java.io.IOException;

import gov.nist.javax.sip.message.SIPRequest;

import javax.sip.SipException;
import javax.sip.address.Hop;

/**
 * Implement this and set it in a SIPDialog to override the sending of an ack message
 * 
 * @author jonathan.agaliotis (CTS)
 */
public interface AckSendingStrategy {

    /**
     * send the ack request
     * 
     * @param ackRequest
     * @throws SipException
     * @throws IOException
     */
    public void send(SIPRequest ackRequest) throws SipException, IOException;

    /**
     * Returns the last hop that a send was attempted on.
     * 
     * @return the last hop used to send a message or null if an error was encountered while
     *         determining the hops or if send hasn't been called yet..
     */
    public Hop getLastHop();

}
