/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

/**
 * Root class from which all SIPHeader objects are subclassed.
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class SIPHeader
	extends SIPObject
	implements SIPHeaderNames, javax.sip.header.Header {

	/** name of this header
	 */
	protected String headerName;

	/** Value of the header.
	*/

	/** Constructor
	 * @param hname String to set
	 */
	protected SIPHeader(String hname) {
		headerName = hname;
	}

	/** Default constructor
	 */
	public SIPHeader() {
	}

	/**
	 * Name of the SIPHeader
	 * @return String
	 */
	public String getHeaderName() {
		return headerName;
	}

	/** Alias for getHaderName above.
	*
	*@return String headerName
	*
	*/
	public String getName() {
		return this.headerName;
	}

	/**
	     * Set the name of the header .
	     * @param hdrname String to set
	     */
	public void setHeaderName(String hdrname) {
		headerName = hdrname;
	}

	/** Get the header value (i.e. what follows the name:).
	* This merely goes through and lops off the portion that follows
	* the headerName:
	*/
	public String getHeaderValue() {
		String encodedHdr = null;
		try {
			encodedHdr = this.encode();
		} catch (Exception ex) {
			return null;
		}
		StringBuffer buffer = new StringBuffer(encodedHdr);
		while (buffer.length() > 0 && buffer.charAt(0) != ':') {
			buffer.deleteCharAt(0);
		}
		if (buffer.length() > 0)
			buffer.deleteCharAt(0);
		return buffer.toString().trim();
	}

	/** Return false if this is not a header list 
	* (SIPHeaderList overrrides this method).
	*@return false
	*/
	public boolean isHeaderList() {
		return false;
	}

	/** Encode this header into canonical form.
	*/
	public String encode() {
		return this.headerName + COLON + SP + this.encodeBody() + NEWLINE;
	}

	/** Encode the body of this header (the stuff that follows headerName).
	* A.K.A headerValue.
	*/
	protected abstract String encodeBody();

	/** Alias for getHeaderValue.
	 */
	public String getValue() {
		return this.getHeaderValue();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
