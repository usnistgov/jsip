/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * List of Unsupported headers.
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 */
public class UnsupportedList extends SIPHeaderList {

	/** Default Constructor
	 */
	public UnsupportedList() {
		super(Unsupported.class, UnsupportedHeader.NAME);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
