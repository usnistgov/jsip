/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.util.ListIterator;

/**
 * A generic extension header list.
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 */
public class ExtensionHeaderList extends SIPHeaderList { 

	public ExtensionHeaderList(String hName) {
		super( ExtensionHeaderImpl.class, hName);
	}
	public ExtensionHeaderList() {
		this(null);
	}
	
	public String encode() {
		StringBuffer retval = new StringBuffer();
		ListIterator it = this.listIterator();
		while(it.hasNext()) {
		   ExtensionHeaderImpl eh = (ExtensionHeaderImpl) it.next();
		   retval.append(eh.encode());
		}
		return retval.toString();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
