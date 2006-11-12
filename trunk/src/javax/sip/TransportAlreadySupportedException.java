/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2005 BEA Systems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. 
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JSIP Specification
 * File Name     : TransportAlreadySupportedException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.2     15/08/2005  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

/**
 * The TransportAlreadySupportedException indicates that a specific transport is 
 * already supported by a SipProvider via its ListeningPoints.
 *
 * @author BEA Systems, NIST
 * @since 1.2
 */

public class TransportAlreadySupportedException extends SipException {

    /**
     * Constructs a new <code>TransportAlreadySupportedException</code>.
     */
    public TransportAlreadySupportedException() {
        super();
    }

    /**
     * Constructs a new <code>TransportAlreadySupportedException</code> with
     * the specified error message.
     *
     * @param message the error message of this Exception.
     */
    public TransportAlreadySupportedException(String message) {
        super(message);
    }

    /**
    * Constructs a new <code>TransportAlreadySupportedException</code> with the
    * specified error message and specialized cause that triggered this error
    * condition.
    *
    * @param  message the detail of the error message
    * @param  cause  the specialized cause that triggered this exception
    */
    public TransportAlreadySupportedException(String message, Throwable cause) {
        super(message, cause);
    }

}

