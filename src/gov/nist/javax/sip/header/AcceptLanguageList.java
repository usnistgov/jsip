/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;

/**
 * AcceptLanguageList: Strings together a list of AcceptLanguage SIPHeaders.
 *@author M. Ranganathan <mranga@nist.gov> 
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AcceptLanguageList extends SIPHeaderList {

	public AcceptLanguageList() {
		super(AcceptLanguage.class, AcceptLanguageHeader.NAME);
	}

	public SIPHeader getFirst() {
		SIPHeader retval = super.getFirst();
		if (retval != null)
			return retval;
		else
			return new AcceptLanguage();
	}

	public SIPHeader getLast() {
		SIPHeader retval = super.getLast();
		if (retval != null)
			return retval;
		else
			return new AcceptLanguage();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
