/**
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Unpublished - rights reserved under the Copyright Laws of the United States.
 * Copyright © 2003 Sun Microsystems, Inc. All rights reserved.
 *
 * U.S. Government Rights - Commercial software. Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and applicable 
 * provisions of the FAR and its supplements.
 *
 * Use is subject to license terms.
 *
 * This distribution may include materials developed by third parties. Sun, 
 * Sun Microsystems, the Sun logo, Java, Jini and JAIN are trademarks or 
 * registered trademarks of Sun Microsystems, Inc. in the U.S. and other 
 * countries.
 *
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 *
 * Module Name   : JAIN SIP Specification
 * File Name     : TransactionAlreadyExistsException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     20/12/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
package javax.sip;

/**
 * This Exception is thrown when a user attempts to get a transaction to handle
 * a message when infact a transaction is already handling this message. 
 *
 * @author Sun Microsystems
 * @version 1.1
 */
public class TransactionAlreadyExistsException extends SipException {

    /**
     * Constructs a new <code>TransactionAlreadyExistsException</code>
     */
    public TransactionAlreadyExistsException(){
        super();
    }

    /**
     * Constructs a new <code>TransactionAlreadyExistsException</code> with
     * the specified error message.
     *
     * @param  message the detail of the error message
     */
    public TransactionAlreadyExistsException(String message) {
        super(message);
    }

    /**
    * Constructs a new <code>TransactionAlreadyExistsException</code> with the
    * specified error message and specialized cause that triggered this error
    * condition.
    *
    * @param  message the detail of the error message
    * @param  cause  the specialized cause that triggered this exception
    */
    public TransactionAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }


}

