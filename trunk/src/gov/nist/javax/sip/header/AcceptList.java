/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * Accept List of  SIP headers. 
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @see Accept
 */
public class AcceptList extends SIPHeaderList {

	/**
	 * Default constructor
	 */
	public AcceptList() {
		super(Accept.class, AcceptHeader.NAME);
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
