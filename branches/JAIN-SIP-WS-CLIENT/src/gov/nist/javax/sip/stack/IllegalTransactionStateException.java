/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
*
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*
* .
*
*/
package gov.nist.javax.sip.stack;

import javax.sip.SipException;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class IllegalTransactionStateException extends SipException {
	
	Reason reason = Reason.GenericReason;
	
	public enum Reason {
		RequestAlreadySent, MissingRequiredHeader, UnmatchingCSeq, ExpiresHeaderMandatory, ContactHeaderMandatory, GenericReason
	}
	
	/**
     * Constructs a new <code>IllegalTransactionStateException</code>
     */
    public IllegalTransactionStateException(Reason reason) {
        super();
        this.reason = reason;
    }



    /**
     * Constructs a new <code>IllegalTransactionStateException</code> with the specified error
     * message.
     *
     * @param message the error message of this Exception.
     */
    public IllegalTransactionStateException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    /**
    * Constructs a new <code>IllegalTransactionStateException</code> with the specified error
    * message and specialized cause that triggered this error condition.
    *
    * @param  message the detail of the error message
    * @param  cause  the specialized cause that triggered this exception
    */
    public IllegalTransactionStateException(String message, Throwable cause, Reason reason) {
        super(message, cause);
        this.reason = reason;
    }


    /**
     * Returns the reason of this exception 
     * 
     * @return the reason of this exception
     */
    public Reason getReason() {
          return(reason);
    }

}
