/**************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
***************************************************************************/
package gov.nist.javax.sdp.fields;

public class TypedTime extends SDPObject {
	String unit;
	int time;

	public String encode() {
		String retval = "";
		retval += new Integer(time).toString();
		if (unit != null) retval += unit;
		return retval;
	}
	
	public void setTime( int t ) {
		time = t;
	}

	public int getTime() {
		return time;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String u) {
		unit = u;
	}

}
