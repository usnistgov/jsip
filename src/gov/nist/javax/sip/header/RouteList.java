/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.*;
import javax.sip.header.*;
import java.util.*;

/**
* A list of Route Headers.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class RouteList extends SIPHeaderList {
	private HashSet routeSet;
        
        
        /** default constructor
         */    
	public RouteList () {
		super(Route.class , RouteHeader.NAME);
		this.routeSet = new HashSet();
		
	}

	public boolean add(Object rh) {
	   if (! routeSet.contains(rh) ) {
		this.routeSet.add(rh);
		return super.add(rh);
	   } else return false;
	}
        
        /** Constructor
         * @param sip SIPObjectList to set
         */    
	public RouteList (SIPObjectList sip) {
		super(sip, RouteHeader.NAME);
	}

	/** 
	* Order is important when comparing route lists.
	*/
	public boolean equals(Object other) {
		if (!(other instanceof RouteList)) return false;
		RouteList that  = (RouteList) other;
		if (this.size() != that.size()) return false;
		ListIterator it = this.listIterator();
		ListIterator it1 = that.listIterator();
		while (it.hasNext()) {
		    Route route = (Route) it.next();
		    Route route1 = (Route) it1.next();
		    if (! route.equals(route1)) return false;
		}
		return true;
	}

        
}
