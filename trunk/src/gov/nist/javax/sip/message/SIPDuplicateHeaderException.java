/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Duplicate header exception:  thrown when there is more
 * than one header of a type where there should only be one.
 * The exception handler may choose to : 
 * 1. discard the duplicate  by returning null
 * 2. keep the duplicate by just returning it.
 * 3. Discard the entire message by throwing an exception.
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:31 $
 * @author M. Ranganathan mailto:mranga@nist.gov
 */
public class SIPDuplicateHeaderException extends ParseException {
	protected SIPHeader sipHeader;
	protected SIPMessage sipMessage;
	public SIPDuplicateHeaderException(String msg) {
		super(msg, 0);
	}
	public SIPMessage getSIPMessage() {
		return sipMessage;
	}

	public SIPHeader getSIPHeader() {
		return sipHeader;
	}

	public void setSIPHeader(SIPHeader sipHeader) {
		this.sipHeader = sipHeader;
	}

	public void setSIPMessage(SIPMessage sipMessage) {
		this.sipMessage = sipMessage;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
