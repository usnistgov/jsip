/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 ******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import java.util.ListIterator;

/**
 * Keeps a list and a hashtable of via header functions.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public final class ViaList extends SIPHeaderList {

	private String stringRep;

	/**
	 * Constructor.
	 * @param hl SIPObjectList to set
	 */
	public ViaList(SIPObjectList hl) {
		super(hl, VIA);
	}

	/**
	 * Default Constructor.
	 */
	public ViaList() {
		super(Via.class, ViaHeader.NAME);
	}

	/**
	 * make a clone of this header list. This supercedes the parent
	 * function of the same signature - here for speed. Cloning based
	 * on introspection is slower.
	 *
	 * @return clone of this Header list.
	 */
	public Object clone() {
		ViaList vlist = new ViaList();
		ListIterator it = this.listIterator();
		while (it.hasNext()) {
			Via v = (Via) ((Via) it.next()).clone();
			vlist.add(v);
		}
		return (Object) vlist;
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
