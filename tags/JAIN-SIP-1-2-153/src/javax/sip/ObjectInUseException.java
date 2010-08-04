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
 * File Name     : ObjectInUseException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    Initial version
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;



/**

 * This exception is thrown by a method that is unable to delete a specified

 * Object because the Object is still in use by the underlying implementation.

 *

 * @author BEA Systems, NIST
 * @version 1.2

 */



public class ObjectInUseException extends SipException {



    /**

     * Constructs a new <code>ObjectInUseException</code>.

     */

    public ObjectInUseException() {

        super();

    }



    /**

     * Constructs a new <code>ObjectInUseException</code> with the specified

     * error message.

     *

     * @param message the detailed error message

     */

    public ObjectInUseException(String message) {

        super(message);

    }



    /**

    * Constructs a new <code>ObjectInUseException</code> with the

    * specified error message and specialized cause that triggered this error

    * condition.

    *

    * @param  message - the detail of the error message

    * @param  cause  - the specialized cause that triggered this exception

    */

    public ObjectInUseException(String message, Throwable cause) {

        super(message, cause);

    }

}

