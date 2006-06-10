/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;

import gov.nist.core.*;
import java.net.*;

/** Implementation of URI field.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*/

public class URIField extends SDPField implements javax.sdp.URI {
	protected URL url;
	protected String urlString;

	public URIField() {
		super(URI_FIELD);
	}

	public String getURI() {
		return urlString;
	}

	public void setURI(String uri) {
		this.urlString = uri;
		this.url = null;
	}

	public URL get() {
		if (this.url != null) {
			return this.url;
		} else {
			try {
				this.url = new URL(this.urlString);
				return this.url;
			} catch (Exception ex) {
				return null;
			}
		}
	}

	public void set(URL uri) {
		this.url = uri;
		this.urlString = null;
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		if (urlString != null) {
			return URI_FIELD + urlString + Separators.NEWLINE;
		} else if (url != null) {
			return URI_FIELD + url.toString() + Separators.NEWLINE;
		} else
			return "";
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
