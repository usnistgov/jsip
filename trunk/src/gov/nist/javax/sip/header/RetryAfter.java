/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header;

import javax.sip.*;
import java.text.ParseException;
import javax.sip.header.*;

/**
 * Retry-After SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class RetryAfter extends ParametersHeader implements RetryAfterHeader {

	/** constant DURATION parameter.
	 */
	public static final String DURATION = ParameterNames.DURATION;

	/** duration field
	 */
	protected Integer retryAfter;

	/** comment field
	 */
	protected String comment;

	/** Default constructor
	 */
	public RetryAfter() {
		super(NAME);
	}

	/** Encode body of this into cannonical form.
	 * @return encoded body
	 */
	public String encodeBody() {
		StringBuffer s = new StringBuffer();

		if (retryAfter != null)
			s.append(retryAfter);

		if (comment != null)
			s.append(SP + LPAREN + comment + RPAREN);

		if (!parameters.isEmpty()) {
			s.append(SEMICOLON + parameters.encode());
		}

		return s.toString();
	}

	/** Boolean function
	 * @return true if comment exist, false otherwise
	 */
	public boolean hasComment() {
		return comment != null;
	}

	/** remove comment field
	 */
	public void removeComment() {
		comment = null;
	}

	/** remove duration field
	 */
	public void removeDuration() {
		super.removeParameter(DURATION);
	}

	/**
	 * Sets the retry after value of the RetryAfterHeader. 
	 * The retry after value MUST be greater than zero and 
	 * MUST be less than 2**31.
	 *
	 * @param retryAfter - the new retry after value of this RetryAfterHeader
	 * @throws InvalidArgumentException if supplied value is less than zero.
	 * @since JAIN SIP v1.1
	 */

	public void setRetryAfter(int retryAfter) throws InvalidArgumentException {
		if (retryAfter < 0)
			throw new InvalidArgumentException(
				"invalid parameter " + retryAfter);
		this.retryAfter = new Integer(retryAfter);
	}

	/**
	 * Gets the retry after value of the RetryAfterHeader. This retry after
	 * value is relative time.
	 *
	 * @return the retry after value of the RetryAfterHeader.
	 * @since JAIN SIP v1.1
	 */

	public int getRetryAfter() {
		return retryAfter.intValue();
	}

	/**
	 * Gets the comment of RetryAfterHeader.
	 *
	 * @return the comment of this RetryAfterHeader, return null if no comment
	 * is available.
	 */

	public String getComment() {
		return comment;
	}

	/**
	 * Sets the comment value of the RetryAfterHeader.
	 *
	 * @param comment - the new comment string value of the RetryAfterHeader.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the comment.
	 */

	public void setComment(String comment) throws ParseException {
		if (comment == null)
			throw new NullPointerException("the comment parameter is null");
		this.comment = comment;
	}

	/**
	 * Sets the duration value of the RetryAfterHeader. The retry after value
	 * MUST be greater than zero and MUST be less than 2**31.
	 *
	 * @param duration - the new duration value of this RetryAfterHeader
	 * @throws InvalidArgumentException if supplied value is less than zero.
	 * @since JAIN SIP v1.1
	 */

	public void setDuration(int duration) throws InvalidArgumentException {
		if (duration < 0)
			throw new InvalidArgumentException("the duration parameter is <0");
		super.setParameter(DURATION, new Integer(duration));
	}

	/**
	 * Gets the duration value of the RetryAfterHeader. This duration value
	 * is relative time.
	 *
	 * @return the duration value of the RetryAfterHeader, return zero if not 
	 * set.
	 * @since JAIN SIP v1.1
	 */

	public int getDuration() {
		return super.getParameterAsInt(DURATION);
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
