/****************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).    *
 ****************************************************************************/
package gov.nist.javax.sip.header;
import gov.nist.core.*;
import javax.sip.header.AcceptLanguageHeader;
import javax.sip.InvalidArgumentException;
import java.util.Locale;

/**
 * Accept Language body.
 *
 * @author M. Ranganathan <mranga@nist.gov>
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 * <pre>
 * HTTP RFC 2616 Section 14.4
 * Accept-Language = "Accept-Language" ":"
 *                         1#( language-range [ ";" "q" "=" qvalue ] )
 *       language-range  = ( ( 1*8ALPHA *( "-" 1*8ALPHA ) ) | "*" )
 *
 * </pre>
 *
 * @see AcceptLanguageList
 */
public class AcceptLanguage
	extends ParametersHeader
	implements AcceptLanguageHeader {

	/** languageRange field
	 */
	protected String languageRange;

	/** default constructor
	 */
	public AcceptLanguage() {
		super(NAME);
	}

	/** Encode the value of this header to a string.
	 *@return  encoded header as a string.
	 */
	protected String encodeBody() {
		StringBuffer encoding = new StringBuffer();
		if (languageRange != null) {
			encoding.append(SP).append(languageRange);
		}
		if (!parameters.isEmpty()) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}
		return encoding.toString();
	}

	/** get the LanguageRange field
	 * @return String
	 */
	public String getLanguageRange() {
		return languageRange;
	}

	/** get the QValue field. Return -1 if the parameter has not been 
	 * set.
	 * @return float
	 */

	public float getQValue() {
		if (!hasParameter("q"))
			return -1;
		return ((Float) parameters.getValue("q")).floatValue();
	}

	/**
	 * Return true if the q value has been set.
	 * @since 1.0
	 * @return boolean
	 */
	public boolean hasQValue() {
		return hasParameter("q");
	}

	/**
	 * Remove the q value.
	 * @since 1.0
	 */
	public void removeQValue() {
		removeParameter("q");
	}

	/**
	 * Set the languageRange.
	 *
	 * @param languageRange is the language range to set.
	 *
	 */
	public void setLanguageRange(String languageRange) {
		this.languageRange = languageRange.trim();
	}

	/**
	 * Sets q-value for media-range in AcceptLanguageHeader. Q-values allow the
	 *
	 * user to indicate the relative degree of preference for that media-range,
	 *
	 * using the qvalue scale from 0 to 1. If no q-value is present, the
	 *
	 * media-range should be treated as having a q-value of 1.
	 *
	 *
	 *
	 * @param q The new float value of the q-value, a value of -1 resets
	 * the qValue.
	 *
	 * @throws InvalidArgumentException if the q parameter value is not
	 *
	 * <code>-1</code> or between <code>0 and 1</code>.
	 *
	 */
	public void setQValue(float q) throws InvalidArgumentException {
		if (q < 0.0 || q > 1.0)
			throw new InvalidArgumentException("qvalue out of range!");
		if (q == -1)
			this.removeParameter("q");
		else
			this.setParameter(new NameValue("q", new Float(q)));
	}

	/**
	 * Gets the language value of the AcceptLanguageHeader.
	 *
	 *
	 *
	 * @return the language Locale value of this AcceptLanguageHeader
	 *
	 */
	public Locale getAcceptLanguage() {
		if (this.languageRange == null)
			return null;
		else
			return new Locale(
				this.languageRange,
				Locale.getDefault().getCountry());
	}

	/**
	 * Sets the language parameter of this AcceptLanguageHeader.
	 *
	 *
	 *
	 * @param language - the new Locale value of the language of
	 *
	 * AcceptLanguageHeader
	 *
	 *
	 */
	public void setAcceptLanguage(Locale language) {
		this.languageRange = language.getLanguage();
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
