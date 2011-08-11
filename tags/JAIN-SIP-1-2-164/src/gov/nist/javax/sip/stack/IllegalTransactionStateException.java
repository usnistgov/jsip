/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
