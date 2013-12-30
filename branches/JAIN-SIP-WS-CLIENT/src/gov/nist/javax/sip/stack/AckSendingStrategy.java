/*
 * Conditions Of Use
 *
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
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
