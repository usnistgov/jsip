package gov.nist.javax.sip.address;
import java.text.ParseException;

/**
 * Implementation of the URI class. This relies on the 1.4 URI class.
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2005-04-16 20:38:47 $
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class GenericURI extends NetObject implements javax.sip.address.URI {
	public static final String SIP = ParameterNames.SIP_URI_SCHEME;
	public static final String SIPS = ParameterNames.SIPS_URI_SCHEME;
	public static final String TEL = ParameterNames.TEL_URI_SCHEME;
	public static final String POSTDIAL = ParameterNames.POSTDIAL;
	public static final String PHONE_CONTEXT_TAG =
		ParameterNames.PHONE_CONTEXT_TAG;
	public static final String ISUB = ParameterNames.ISUB;
	public static final String PROVIDER_TAG = ParameterNames.PROVIDER_TAG;

	/** Imbedded URI
	 */
	protected String uriString;

	protected String scheme;

	/** Consturctor
	 */
	protected GenericURI() {
	}

	/** Constructor given the URI string
	 * @param uriString The imbedded URI string.
	 * @throws URISyntaxException When there is a syntaz error in the imbedded URI.
	 */
	public GenericURI(String uriString) throws ParseException {
		try {
			this.uriString = uriString;
			int i = uriString.indexOf(":");
			scheme = uriString.substring(0, i);
		} catch (Exception e) {
			throw new ParseException("GenericURI, Bad URI format", 0);
		}
	}

	/** Encode the URI.
	 * @return The encoded URI
	 */
	public String encode() {
		return uriString;

	}

	/** Encode this URI.
	 * @return The encoded URI
	 */
	public String toString() {
		return this.encode();

	}

	/** Returns the value of the "scheme" of
	 * this URI, for example "sip", "sips" or "tel".
	 *
	 * @return the scheme paramter of the URI
	 */
	public String getScheme() {
		return scheme;
	}

	/** This method determines if this is a URI with a scheme of
	 * "sip" or "sips".
	 *
	 * @return true if the scheme is "sip" or "sips", false otherwise.
	 */
	public boolean isSipURI() {
		return this instanceof SipUri;
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:28  sverker
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
