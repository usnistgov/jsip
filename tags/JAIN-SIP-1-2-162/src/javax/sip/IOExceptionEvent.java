/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : IOExceptionEvent.java
 * Author        : M. Ranganathan
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.2     05/03/2005    M. Ranganathan    Initial version
 *
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;


import java.util.EventObject;

/**
 * This object is used to signal to the application that an IO Exception has
 * occured. The transaction state machine requires to report asynchronous IO Exceptions to
 * the application immediately (according to RFC 3261).
 * This class represents an IOExceptionEvent that is passed from a SipProvider to its SipListener.
 * This event enables an implementation to propagate the asynchronous handling
 * of IO Exceptions to the application. An application (SipListener) will
 * register with the SIP protocol stack (SipProvider) and listen for
 * IO Exceptions from the SipProvider.
 * In many cases, when sending a SIP message, the sending function will return before
 * the message was actually sent.
 * This will happen for example if there is a need to wait for a response from a DNS server
 * or to perform other asynchronous actions such as connecting a TCP connection.
 * Later on if the message sending fails an IO exception event will be given to the application.
 * IO Exception events may also be reported asynchronously when the Transaction State machine
 * attempts to resend a pending request. Note that synchronous IO Exceptions
 * are presented to the caller as SipException.
 *
 * @author BEA Systems, NIST
 * @since v1.2
 */
public class IOExceptionEvent extends EventObject {

    /** Constructor
     *
     * @param source -- the object that is logically deemed to have caused the IO Exception (dialog/transaction/provider).
     * @param remoteHost -- host where the request/response was heading
     * @param port -- port where the request/response was heading
     * @param transport -- transport ( i.e. UDP/TCP/TLS).
     */
    public IOExceptionEvent ( Object source,  String remoteHost, int port, String transport) {
        super(source);
        this.m_host = remoteHost;
        this.m_port = port;
        this.m_transport = transport;
    }

    /**
     * Return the host where Socket was pointing.
     *
     * @return host
     *
     */
    public String getHost() {
        return m_host;
    }

    /**
     * Returns the port where the socket was trying to send amessage.
     *
     * @return port associated with the IOException
     */
    public  int getPort () {
        return m_port;
    }

    /**
     * Return transport used for the failed write attempt.
     *
     * @return the transaction associated with the IOException
     */
    public String getTransport() {
        return this.m_transport;
    }




    private String      m_host;
    private int         m_port;
    private String      m_transport;


}

