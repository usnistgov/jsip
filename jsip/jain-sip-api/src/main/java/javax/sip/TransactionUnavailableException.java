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
 * File Name     : TransactionUnavailableException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     01/04/2003  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

/**
 * The TransactionUnavailableException indicates that a vendor's implementation 
 * could not create a Transaction for some reason.
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */
public class TransactionUnavailableException extends SipException {

    /**
     * Constructs a new <code>TransactionUnavailableException</code>.
     */
    public TransactionUnavailableException() {
        super();
    }

    /**
     * Constructs a new <code>TransactionUnavailableException</code> with
     * the specified error message.
     *
     * @param message the error message of this Exception.
     */
    public TransactionUnavailableException(String message) {
        super(message);
    }

    /**
    * Constructs a new <code>TransactionUnavailableException</code> with the
    * specified error message and specialized cause that triggered this error
    * condition.
    *
    * @param  message the detail of the error message
    * @param  cause  the specialized cause that triggered this exception
    */
    public TransactionUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

