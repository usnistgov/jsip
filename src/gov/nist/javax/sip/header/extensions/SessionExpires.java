
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.header.extensions;

import java.text.ParseException;
import gov.nist.javax.sip.header.*;

import javax.sip.InvalidArgumentException;
import javax.sip.header.ExtensionHeader;

import gov.nist.javax.sip.address.*;

/**  
 * ReferredBy SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.1 $ $Date: 2006-10-12 11:57:52 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * Created from ReferTo by Peter Musgrave. 
 *
 */
public final class SessionExpires
	extends ParametersHeader implements ExtensionHeader, SessionExpiresHeader {

	// TODO: Need a unique UID
	private static final long serialVersionUID = 8765762413224043300L;

	// TODO: When the MinSEHeader is added to javax - move this there...pmusgrave
	public static final String NAME = "Session-Expires";

	public int expires;
	
	/** default Constructor.
	 */
	public SessionExpires() {
		super(NAME);
	}
	
	/**
	 * Gets the expires value of the SessionExpiresHeader. This expires value is
	 * relative time.
	 *
	 *
	 *
	 * @return the expires value of the ExpiresHeader.
	 *
	 * @since JAIN SIP v1.1
	 *
	 */
	public int getExpires() {
		return expires;
	}

	/**
	 * Sets the relative expires value of the SessionExpiresHeader. 
	 * The expires value MUST be greater than zero and MUST be 
	 * less than 2**31.
	 *
	 * @param expires - the new expires value
	 *
	 * @throws InvalidArgumentException if supplied value is less than zero.
	 *
	 * @since JAIN SIP v1.1
	 *
	 */
	public void setExpires(int expires) throws InvalidArgumentException {
		if (expires < 0)
			throw new InvalidArgumentException("bad argument " + expires);
		this.expires = expires;
	}
	
	public void setValue(String value) throws ParseException {
		// not implemented.
		throw new ParseException(value,0);
		
	}
	
	/**
	 * Encode the header content into a String.
	 * @return String
	 */
	protected String encodeBody() {

		String retval = new Integer(expires).toString(); // seems overkill - but Expires did this.

		if (!parameters.isEmpty()) {
			retval += SEMICOLON + parameters.encode();
		}
		return retval;
	}
}



