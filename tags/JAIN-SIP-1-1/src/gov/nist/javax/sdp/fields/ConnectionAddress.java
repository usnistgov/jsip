/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
/**
* Connection Address of the SDP header (appears as part of the Connection field)
*
*@version  JSR141-PUBLIC-REVIEW (subject to change)
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ConnectionAddress extends SDPObject {
	protected Host address;
	protected int ttl;
	protected int port;

	public Host getAddress() {
		return address;
	}
	public int getTtl() {
		return ttl;
	}
	public int getPort() {
		return port;
	}
	/**
	* Set the address member  
	*/
	public void setAddress(Host a) {
		address = a;
	}
	/**
	* Set the ttl member  
	*/
	public void setTtl(int ttl) {
		this.ttl = ttl;
	}
	/**
	* Set the port member  
	*/
	public void setPort(int p) {
		port = p;
	}

	/**
	*  Get the string encoded version of this object
	* @since v1.0
	*/
	public String encode() {
		String encoded_string = "";

		if (address != null)
			encoded_string = address.encode();
		if (ttl != 0 && port != 0) {
			encoded_string += Separators.SLASH + ttl + Separators.SLASH + port;
		} else if (ttl != 0) {
			encoded_string += Separators.SLASH + ttl;
		}
		return encoded_string;
	}
	public Object clone() {
		ConnectionAddress retval = (ConnectionAddress) super.clone();
		if (this.address != null)
			retval.address = (Host) this.address.clone();
		return retval;
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:27  sverker
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
