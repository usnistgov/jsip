/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;

/**
 * Exception that gets generated when the Stack encounters an error.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:33 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class SIPServerException extends Exception {

	/**
	 * Return code.
	 */
	protected int rc;
	/**
	 * Message.
	 */
	protected String message;

	/**
	 * Get the SIPMessage
	 */
	protected SIPMessage sipMessage;

	public int getRC() {
		return this.rc;
	}
	
	/**
	 * Constructor when we are given only the error code
	 * @param rc Return code.
	 */
	public SIPServerException(int rc) {
		this.rc = rc;
	}

	/**
	 * Constructor for when we have the error code and some error info.
	 * @param rc SIP Return code 
	 * @param msg Error message
	 */
	public SIPServerException(int rc, String msg) {
		this.rc = rc;
		this.message = msg;
	}
	
	/**
	 * Constructor for when we have a return code and a SIPMessage.
	 * @param rc SIP error code
	 * @param message SIP Error message
	 * @param msg Auxiliary error message
	 */
	public SIPServerException(int rc, SIPMessage message, String msg) {
		this.rc = rc;
		this.sipMessage = message;
		this.message = msg;

	}

	/**
	 * Constructor when we have a pre-formatted response.
	 * @param response Pre-formatted response to send back to the
	 * other end.
	 */
	public SIPServerException(String response) {
		super(response);
	}

	/**
	 * Constructor that constructs the message from the standard
	 * Error messages.
	 *
	 * @param rc is the SIP Error code.
	 * @param sipMessage is the SIP Message that caused the exception.
	 */
	public SIPServerException(int rc, SIPMessage sipMessage) {
		this.rc = rc;
		this.sipMessage = sipMessage;
	}

	/**
	 * Get the message that generated this exception.
	 *
	 * @return -- the message that generated this exception.
	 */
	public SIPMessage getSIPMessage() {
		return this.sipMessage;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
