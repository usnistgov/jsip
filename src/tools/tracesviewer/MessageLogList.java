/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package tools.tracesviewer;

import java.util.*;

import java.io.*;

/**
*This class stores a sorted list messages for logging. 
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  
*@author Marc Bednarek 
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class MessageLogList extends TreeSet {
    private String logFileName;
    protected String description;
    protected static long startTime;

	

    static {
        startTime = -1;
    }

    /** Constructor.
    *@param comp comparator for sorting the logs
    */
    public MessageLogList(Comparator comp) {
	super(comp);
    }

    /** set a descriptive string for this log (for id purposes).
     *@param description is the decriptive string to add.
     */
    public void addDescription(String description) {
	this.description = description;
    }


    /** Constructor given callId and a comparator.
    *@param callId is the call id for which to store the log.
    *@param comp is the comparator to sort the log records.
    */

    public MessageLogList(String callId, Comparator comp) {
	super(comp);
    }

    /** Add a comparable object to the messgageLog 
    *@param obj is the comparable object to add to the message log.
    */
    public synchronized boolean add(Object obj) {
	TracesMessage log = (TracesMessage) obj;
	long ts = Long.parseLong(log.getTime());
	if ( ts < startTime ||
		startTime < 0 ) startTime = ts;
	return super.add(obj);
    }

	

}
