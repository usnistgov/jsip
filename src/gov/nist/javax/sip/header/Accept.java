/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;

/**
 * Accept header : The top level header is actually AcceptList which is a list of
 * Accept headers.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:29 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class Accept
	extends ParametersHeader
	implements javax.sip.header.AcceptHeader {

	/** mediaRange field
	 */
	protected MediaRange mediaRange;

	/** default constructor
	 */
	public Accept() {
		super(NAME);
	}

	/** returns true if this header allows all ContentTypes,
	  * false otherwise.
	  * @return Boolean
	  */
	public boolean allowsAllContentTypes() {
		if (mediaRange == null)
			return false;
		else
			return mediaRange.type.compareTo(STAR) == 0;
	}

	/**
	 * returns true if this header allows all ContentSubTypes,
	 * false otherwise.
	 * @return boolean
	 */
	public boolean allowsAllContentSubTypes() {
		if (mediaRange == null) {
			return false;
		} else
			return mediaRange.getSubtype().compareTo(STAR) == 0;
	}

	/** Encode the value of this header into cannonical form.
	*@return encoded value of the header as a string.
	*/
	protected String encodeBody() {
		String s = "";
		if (mediaRange != null)
			s += mediaRange.encode();
		if (parameters != null && !parameters.isEmpty())
			s += SP + ";" + parameters.encode();
		return s;
	}

	/** get the MediaRange field
	 * @return MediaRange
	 */
	public MediaRange getMediaRange() {
		return mediaRange;
	}

	/** get the contentType field
	 * @return String
	 */
	public String getContentType() {
		if (mediaRange == null)
			return null;
		else
			return mediaRange.getType();
	}

	/** get the ContentSubType fiels
	 * @return String
	 */
	public String getContentSubType() {
		if (mediaRange == null)
			return null;
		else
			return mediaRange.getSubtype();
	}

	/**
	 * Get the q value.
	 * @return float
	 */
	public float getQValue() {
		return getParameterAsFloat(ParameterNames.Q);
	}

	/**
	 * Return true if the q value has been set.
	 * @return boolean
	 */
	public boolean hasQValue() {
		return super.hasParameter(ParameterNames.Q);

	}

	/**
	 *Remove the q value.
	 */
	public void removeQValue() {
		super.removeParameter(ParameterNames.Q);
	}

	/** set the ContentSubType field
	 * @param subtype String to set
	 */
	public void setContentSubType(String subtype) {
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setSubtype(subtype);
	}

	/** set the ContentType field
	 * @param type String to set
	 */
	public void setContentType(String type) {
		if (mediaRange == null)
			mediaRange = new MediaRange();
		mediaRange.setType(type);
	}

	/**
	 * Set the q value 
	 * @param qValue float to set
	 * @throws IllegalArgumentException if qValue is <0.0 or >1.0
	 */
	public void setQValue(float qValue) throws InvalidArgumentException {
		if (qValue == -1)
			super.removeParameter(ParameterNames.Q);
		super.setParameter(ParameterNames.Q, qValue);

	}

	/**
	     * Set the mediaRange member
	     * @param m MediaRange field
	     */
	public void setMediaRange(MediaRange m) {
		mediaRange = m;
	}

}
/*
 * $Log: not supported by cvs2svn $
 */
