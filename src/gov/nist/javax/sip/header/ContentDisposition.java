/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import java.text.*;

/**
 * Content Dispositon SIP Header.
 * 
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * <a href="${docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class ContentDisposition
	extends ParametersHeader
	implements javax.sip.header.ContentDispositionHeader {

	/**
	 * DispositionType field.  
	 */
	protected String dispositionType;

	/**
	 * Default constructor.
	 */
	public ContentDisposition() {
		super(NAME);
	}

	/**
	 * Encode value of header into canonical string.
	 * @return encoded value of header.
	 *
	 */
	public String encodeBody() {
		StringBuffer encoding = new StringBuffer(dispositionType);
		if (!this.parameters.isEmpty()) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}
		return encoding.toString();
	}

	/**
	 * Set the disposition type.
	 * @param dispositionType type.
	 */
	public void setDispositionType(String dispositionType)
		throws ParseException {
		if (dispositionType == null)
			throw new NullPointerException(
				"JAIN-SIP Exception"
					+ ", ContentDisposition, setDispositionType(), the dispositionType parameter is null");
		this.dispositionType = dispositionType;
	}

	/**
	 * Get the disposition type.
	 * @return Disposition Type
	 */
	public String getDispositionType() {
		return this.dispositionType;
	}

	/**
	 * Get the dispositionType field.
	 * @return String
	 */
	public String getHandling() {
		return this.getParameter("handling");
	}

	/** set the dispositionType field.
	 * @param handling String to set.
	 */
	public void setHandling(String handling) throws ParseException {
		if (handling == null)
			throw new NullPointerException(
				"JAIN-SIP Exception"
					+ ", ContentDisposition, setHandling(), the handling parameter is null");
		this.setParameter("handling", handling);
	}

	/**
	 * Gets the interpretation of the message body or message body part of
	 * this ContentDispositionHeader.
	 * 
	 * @return interpretation of the message body or message body part
	 */
	public String getContentDisposition() {
		return this.encodeBody();
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
