/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;
import javax.sip.header.*;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

/**
 * the WarningValue SIPObject. 
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-04-21 16:25:21 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @see WarningList SIPHeader which strings these together.
 */
public class Warning extends SIPHeader implements WarningHeader {

	/** warn code field, the warn code consists of three digits.
	 */
	protected int code;

	/** the name or pseudonym of the server adding
	 * the Warning header, for use in debugging
	 */
	protected String agent;

	/** warn-text field
	 */
	protected String text;

	/**
	 * constructor.
	 */
	public Warning() {
		super(WARNING);
	}

	/** Encode the body of the header (return the stuff following name:).
	 *@return the string encoding of the header value.
	 */
	public String encodeBody() {
		return text != null
			? new Integer(code).toString()
				+ SP
				+ agent
				+ SP
				+ DOUBLE_QUOTE
				+ text
				+ DOUBLE_QUOTE
			: new Integer(code).toString() + SP + agent;
	}

	/**
	* Gets code of WarningHeader
	* @return code of WarningHeader
	*/
	public int getCode() {
		return code;
	}

	/**
	* Gets agent host of WarningHeader
	* @return agent host of WarningHeader
	*/
	public String getAgent() {
		return agent;
	}

	/**
	* Gets text of WarningHeader
	* @return text of WarningHeader
	*/
	public String getText() {
		return text;
	}

	/**
	 * Sets code of WarningHeader
	 * @param code int to set
	 * @throws SipParseException if code is not accepted by implementation
	 */
	public void setCode(int code) throws InvalidArgumentException {
		this.code = code;
	}

	/**
	 * Sets host of WarningHeader
	 * @param host String to set
	 * @throws ParseException if host is not accepted by implementation
	 */
	public void setAgent(String host) throws ParseException {
		if (host == null)
			throw new NullPointerException
			("the host parameter in the Warning header is null");
		else {
			this.agent = host;
		}
	}

	/**
	 * Sets text of WarningHeader
	 * @param text String to set
	 * @throws ParseException if text is not accepted by implementation
	 */
	public void setText(String text) throws ParseException {
		if (text == null) {
			throw new ParseException(
				"The text parameter in the Warning header is null",
				0);
		} else
			this.text = text;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:30  sverker
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
