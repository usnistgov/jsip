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

}
/*
 * $Log: not supported by cvs2svn $
 */
