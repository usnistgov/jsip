/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.util.*;
import java.text.ParseException;
import javax.sip.header.*;

/**
 * List of ALLOW headers. The sip message can have multiple Allow headers
 *
 * @author M. Ranganathan <mranga@nist.gov>  NIST/ITL/ANTD <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 */
public class AllowList extends SIPHeaderList {

	/** default constructor
	 */
	public AllowList() {
		super(Allow.class, AllowHeader.NAME);
	}

	/**
	 * Gets an Iterator of all the methods of the AllowHeader. Returns an empty
	 *
	 * Iterator if no methods are defined in this Allow Header.
	 *
	 *
	 *
	 * @return Iterator of String objects each identifing the methods of
	 *
	 * AllowHeader.
	 *
	 * @since JAIN SIP v1.1
	 *
	 */
	public ListIterator getMethods() {
		ListIterator li = super.hlist.listIterator();
		LinkedList ll = new LinkedList();
		while (li.hasNext()) {
			Allow allow = (Allow) li.next();
			ll.add(allow.getMethod());
		}
		return ll.listIterator();
	}

	/**
	 * Sets the methods supported defined by this AllowHeader.
	 *
	 *
	 *
	 * @param methods - the Iterator of Strings defining the methods supported
	 *
	 * in this AllowHeader
	 *
	 * @throws ParseException which signals that an error has been reached
	 *
	 * unexpectedly while parsing the Strings defining the methods supported.
	 *
	 * @since JAIN SIP v1.1
	 *
	 */
	public void setMethods(List methods) throws ParseException {
		ListIterator it = methods.listIterator();
		while (it.hasNext()) {
			Allow allow = new Allow();
			allow.setMethod((String) it.next());
			this.add(allow);
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
