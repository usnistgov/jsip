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
 * File Name     : DialogDoesNotExistException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.2     16/06/2005  Phelim O'Doherty    Initial version  
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

/**
 * This Exception is thrown when a user attempts to reference
 * Dialog that does currently not exist in the underlying SipProvider
 *
 * @author BEA Systems, NIST
 * @since 1.2
 */



public class DialogDoesNotExistException extends SipException {



    /**
     * Constructs a new <code>DialogDoesNotExistException</code>
     */

    public DialogDoesNotExistException(){

        super();

    }



    /**

     * Constructs a new <code>DialogDoesNotExistException</code> with

     * the specified error message.

     *

    * @param  message the detail of the error message

     */

    public DialogDoesNotExistException(String message) {

        super(message);

    }



    /**

    * Constructs a new <code>DialogDoesNotExistException</code> with the

    * specified error message and specialized cause that triggered this error

    * condition.

    *

    * @param  message the detail of the error message

    * @param  cause  the specialized cause that triggered this exception

    */

    public DialogDoesNotExistException(String message, Throwable cause) {

        super(message, cause);

    }



}

