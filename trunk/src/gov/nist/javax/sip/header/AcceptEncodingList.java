/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * AcceptEncodingList of AccepEncoding headers.
 *
 *@author M. Ranganathan <mranga@nist.gov> 
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AcceptEncodingList extends SIPHeaderList {

	/** default constructor
	 */
	public AcceptEncodingList() {
		super(AcceptEncoding.class, AcceptEncodingHeader.NAME);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
