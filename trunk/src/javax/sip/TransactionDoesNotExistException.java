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
 * File Name     : TransactionDoesNotExistException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

/**
 * This Exception is thrown when a user attempts to reference
 * a client or server transaction that does currently not exist in the
 * underlying SipProvider
 *
 * @author BEA Systems, NIST
 * @version 1.2
 */



public class TransactionDoesNotExistException extends SipException {



    /**

     * Constructs a new <code>TransactionDoesNotExistException</code>

     */

    public TransactionDoesNotExistException(){

        super();

    }



    /**

     * Constructs a new <code>TransactionDoesNotExistException</code> with

     * the specified error message.

     *

    * @param  message the detail of the error message

     */

    public TransactionDoesNotExistException(String message) {

        super(message);

    }



    /**

    * Constructs a new <code>TransactionDoesNotExistException</code> with the

    * specified error message and specialized cause that triggered this error

    * condition.

    *

    * @param  message the detail of the error message

    * @param  cause  the specialized cause that triggered this exception

    */

    public TransactionDoesNotExistException(String message, Throwable cause) {

        super(message, cause);

    }



}

