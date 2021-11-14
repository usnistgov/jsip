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
 * File Name     : ProviderDoesNotExistException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     07/07/2005  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;

/**
 * This Exception is thrown when a user attempts to start the SipStack without
 * any SipProviders created to service requests and responses.
 *
 * @author BEA Systems, NIST
 * @since 1.2
 */



public class ProviderDoesNotExistException extends SipException {



    /**

     * Constructs a new <code>ProviderDoesNotExistException</code>

     */

    public ProviderDoesNotExistException(){

        super();

    }



    /**

     * Constructs a new <code>ProviderDoesNotExistException</code> with

     * the specified error message.

     *

    * @param  message the detail of the error message

     */

    public ProviderDoesNotExistException(String message) {

        super(message);

    }



    /**

    * Constructs a new <code>ProviderDoesNotExistException</code> with the

    * specified error message and specialized cause that triggered this error

    * condition.

    *

    * @param  message the detail of the error message

    * @param  cause  the specialized cause that triggered this exception

    */

    public ProviderDoesNotExistException(String message, Throwable cause) {

        super(message, cause);

    }



}

