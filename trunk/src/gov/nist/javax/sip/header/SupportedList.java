/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * A list of supported headers.
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 * @see Supported
 */
public class SupportedList extends SIPHeaderList {

	/** Default Constructor
	 */
	public SupportedList() {
		super(Supported.class, SupportedHeader.NAME);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
