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
 * File Name     : SipException.java
 * Author        : Phelim O'Doherty
 *
 *  HISTORY
 *  Version   Date      Author              Comments
 *  1.1     08/10/2002  Phelim O'Doherty    
 *~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */

package javax.sip;



/**

 * A SipException is thrown when a general SIP exception is encountered, when

 * no other specialized exception defined in this specification can handle

 * the error.

 *

 * @author Sun Microsystems

 * @version 1.1

 */



public class SipException extends Exception {



    /**

     * Constructs a new <code>SipException</code>

     */

    public SipException() {

        super();

    }



    /**

     * Constructs a new <code>SipException</code> with the specified error

     * message.

     *

     * @param <var>message</var> the error message of this Exception.

     */

    public SipException(String message) {

        super(message);

    }



    /**

    * Constructs a new <code>SipException</code> with the specified error

    * message and specialized cause that triggered this error condition.

    *

    * @param  message the detail of the error message

    * @param  cause  the specialized cause that triggered this exception

    */

    public SipException(String message, Throwable cause) {
        super(message);
        m_Cause = cause;
    }

    
    /**
     * Returns the cause of this throwable or null if the cause is 
     * nonexistent or unknown. (The cause is the throwable that caused this 
     * throwable to get thrown.) This implementation returns the cause that 
     * was supplied via the constructor requiring a Throwable.
     *
     * @return the cause of this throwable or null if the cause is 
     * nonexistent or unknown.
     */
    public Throwable getCause() {
   		return(m_Cause);
    }

    /**
     * The specialized cause that triggered this Exception. This cause
     * informs an application of the underlying implementation problem that
     * triggered this Exception.
     */
    protected Throwable m_Cause = null;      
    
    
}

