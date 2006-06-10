/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.javax.sip.address.*;
import javax.sip.header.*;
import javax.sip.address.*;
import java.text.ParseException;

/**
 * ErrorInfo SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:50 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public final class ErrorInfo
	extends ParametersHeader
	implements ErrorInfoHeader {

	protected GenericURI errorInfo;

	/**
	 * Default constructor.
	 */
	public ErrorInfo() {
		super(NAME);
	}

	/**
	 * Constructor given the error info
	 * @param errorInfo -- the error information to set.
	 */
	public ErrorInfo(GenericURI errorInfo) {
		this();
		this.errorInfo = errorInfo;
	}

	/**
	 * Encode into canonical form.
	 * @return String
	 */
	public String encodeBody() {
		StringBuffer retval =
			new StringBuffer(LESS_THAN).append(errorInfo.toString()).append(
				GREATER_THAN);
		if (!parameters.isEmpty()) {
			retval.append(SEMICOLON).append(parameters.encode());
		}
		return retval.toString();
	}

	/**
	 * Sets the ErrorInfo of the ErrorInfoHeader to the <var>errorInfo</var>
	 * parameter value.
	 *
	 * @param errorInfo the new ErrorInfo of this ErrorInfoHeader.
	 */
	public void setErrorInfo(javax.sip.address.URI errorInfo) {
		this.errorInfo = (GenericURI) errorInfo;

	}

	/**
	 * Returns the ErrorInfo value of this ErrorInfoHeader. This message
	 * may return null if a String message identifies the ErrorInfo.
	 *
	 * @return the URI representing the ErrorInfo.
	 */
	public URI getErrorInfo() {
		return errorInfo;
	}

	/**
	 * Sets the Error information message to the new <var>message</var> value
	 * supplied to this method.
	 *
	 * @param message - the new string value that represents the error message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the error message.
	 */
	public void setErrorMessage(String message) throws ParseException {
		if (message == null)
			throw new NullPointerException(
				"JAIN-SIP Exception "
					+ ", ErrorInfoHeader, setErrorMessage(), the message parameter is null");
		setParameter("message", message);
	}

	/**
	 * Get the Error information message of this ErrorInfoHeader. 
	 *
	 * @return the stringified version of the ErrorInfo header.
	 */
	public String getErrorMessage() {
		return getParameter("message");
	}

	public Object clone() {
		ErrorInfo retval = (ErrorInfo) super.clone();
		if (this.errorInfo != null)
			retval.errorInfo = (GenericURI) this.errorInfo.clone();
		return retval;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:29  sverker
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
