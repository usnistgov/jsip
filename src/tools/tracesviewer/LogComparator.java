/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package tools.tracesviewer;
import java.util.Comparator;

/** A class that is used for comparing log records.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

class LogComparator implements Comparator {
	public int compare(Object obj1, Object obj2) {
	     try {
		TracesMessage m1 = (TracesMessage) obj1;
		TracesMessage m2 = (TracesMessage) obj2;
		long ts1 = Long.parseLong(m1.getTime());
		long ts2 = Long.parseLong(m2.getTime());
		if (ts1 < ts2) return -1;
		else if (ts1 > ts2 ) return 1;
		else return 0;
	     } catch (NumberFormatException ex) {
		ex.printStackTrace();
		System.exit(0);
		return 0;
	     }
	}

	public boolean equals(Object obj2) {
		return super.equals(obj2);

	}

    }
