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
 * File Name     : TransportNotSupportedException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

/**
 * The TransportNotSupportedException indicates that a specific transport is 
 * not supported by a vendor's implementation of this specification.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */

public class TransportNotSupportedException extends SipException {

    /**
     * Constructs a new <code>TransportNotSupportedException</code>.
     */
    public TransportNotSupportedException() {
        super();
    }

    /**
     * Constructs a new <code>TransportNotSupportedException</code> with
     * the specified error message.
     *
     * @param message the error message of this Exception.
     */
    public TransportNotSupportedException(String message) {
        super(message);
    }

    /**
    * Constructs a new <code>TransportNotSupportedException</code> with the
    * specified error message and specialized cause that triggered this error
    * condition.
    *
    * @param  message the detail of the error message
    * @param  cause  the specialized cause that triggered this exception
    */
    public TransportNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

}

