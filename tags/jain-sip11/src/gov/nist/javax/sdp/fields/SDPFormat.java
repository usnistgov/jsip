/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
/**
* Media Description SDP header
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class SDPFormat  extends SDPObject {
	protected String format;
	
	public void setFormat(String fmt) { format = fmt; }

	public String getFormat() { return format; }

	public SDPFormat(String s) {
		format = s;
	}
	
	public SDPFormat() {}

	public String encode() { return format; }


}

	
