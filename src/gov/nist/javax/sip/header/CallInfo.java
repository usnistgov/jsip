/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

/**
 * CallInfo SIPHeader.
 *
 * @author "M. Ranganathan" <mranga@nist.gov> <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class CallInfo
	extends ParametersHeader
	implements javax.sip.header.CallInfoHeader {

	protected GenericURI info;

	/**
	 * Default constructor
	 */
	public CallInfo() {
		super(CALL_INFO);
	}

	/**
	 * Return canonical representation.
	 * @return String 
	 */
	public String encodeBody() {
		StringBuffer encoding = new StringBuffer();

		encoding.append(LESS_THAN).append(info.toString()).append(GREATER_THAN);

		if (parameters != null && !parameters.isEmpty())
			encoding.append(SEMICOLON).append(parameters.encode());

		return encoding.toString();
	}

	/**
	 * get the purpose field
	 * @return String
	 */
	public String getPurpose() {
		return this.getParameter("purpose");
	}

	/**
	 * get the URI field
	 * @return URI
	 */
	public javax.sip.address.URI getInfo() {
		return info;
	}

	/**
	 * set the purpose field
	 * @param purpose is the purpose field.
	 */
	public void setPurpose(String purpose) {
		if (purpose == null)
			throw new NullPointerException("null arg");
		try {
			this.setParameter("purpose", purpose);
		} catch (ParseException ex) {
		}
	}

	/**
	 * set the URI field
	 * @param info is the URI to set.
	 */
	public void setInfo(javax.sip.address.URI info) {
		this.info = (GenericURI) info;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
