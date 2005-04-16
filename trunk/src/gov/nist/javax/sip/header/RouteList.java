/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import java.util.*;

/**
 * A list of Route Headers.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:50 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class RouteList extends SIPHeaderList {
	private HashSet routeSet;

	/** default constructor
	 */
	public RouteList() {
		super(Route.class, RouteHeader.NAME);
		this.routeSet = new HashSet();

	}

	public boolean add(Object rh) {
		if (!routeSet.contains(rh)) {
			this.routeSet.add(rh);
			return super.add(rh);
		} else
			return false;
	}

	/** Constructor
	 * @param sip SIPObjectList to set
	 */
	public RouteList(SIPObjectList sip) {
		super(sip, RouteHeader.NAME);
	}

	/** 
	* Order is important when comparing route lists.
	*/
	public boolean equals(Object other) {
		if (!(other instanceof RouteList))
			return false;
		RouteList that = (RouteList) other;
		if (this.size() != that.size())
			return false;
		ListIterator it = this.listIterator();
		ListIterator it1 = that.listIterator();
		while (it.hasNext()) {
			Route route = (Route) it.next();
			Route route1 = (Route) it1.next();
			if (!route.equals(route1))
				return false;
		}
		return true;
	}

	public Object clone() {
		RouteList retval = (RouteList) super.clone();
		if (this.routeSet != null)
			retval.routeSet = (HashSet) this.routeSet.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:29  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
