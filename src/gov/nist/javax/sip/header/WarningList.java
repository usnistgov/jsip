/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;

/**
 * A Warning SIPObject. (A list of Warning headers).
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class WarningList extends SIPHeaderList {

	/**
	 * Constructor.
	 */
	public WarningList() {
		super(Warning.class, WarningHeader.NAME);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
