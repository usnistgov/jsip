/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import javax.sip.header.*;
import java.text.ParseException;

/**
* Event SIP Header.
*
* @version JAIN-SIP-1.1 $Revision: 1.3 $ $Date: 2004-01-22 13:26:29 $
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*@author Olivier Deruelle <deruelle@nist.gov><br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class Event extends ParametersHeader implements EventHeader {

	protected String eventType;

	/**
	 * Creates a new instance of Event
	 */
	public Event() {
		super(EVENT);
	}

	/**
	* Sets the eventType to the newly supplied eventType string.
	*
	* @param eventType - the  new string defining the eventType supported
	* in this EventHeader
	* @throws ParseException which signals that an error has been reached
	* unexpectedly while parsing the eventType value.
	*/
	public void setEventType(String eventType) throws ParseException {
		if (eventType == null)
			throw new NullPointerException(" the eventType is null");
		this.eventType = eventType;
	}

	/**
	 * Gets the eventType of the EventHeader. 
	 *
	 * @return the string object identifing the eventType of EventHeader.
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * Sets the id to the newly supplied <var>eventId</var> string.
	 *
	 * @param eventId - the new string defining the eventId of this EventHeader
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the eventId value.
	 */
	public void setEventId(String eventId) throws ParseException {
		if (eventId == null)
			throw new NullPointerException(" the eventId parameter is null");
		setParameter(ParameterNames.ID, eventId);
	}

	/**
	 * Gets the id of the EventHeader. This method may return null if the 
	 * "eventId" is not set.
	 * @return the string object identifing the eventId of EventHeader.
	 */
	public String getEventId() {
		return getParameter(ParameterNames.ID);
	}

	/**
	 * Encode in canonical form.
	 * @return String
	 */
	public String encodeBody() {
		StringBuffer retval = new StringBuffer();

		if (eventType != null)
			retval.append(SP + eventType);

		if (!parameters.isEmpty())
			retval.append(SEMICOLON + this.parameters.encode());
		return retval.toString();
	}

	/**
	 *  Return true if the given event header matches the supplied one.
	 *
	 * @param matchTarget -- event header to match against.
	 */
	public boolean match(Event matchTarget) {
		if (matchTarget.eventType == null && this.eventType != null)
			return false;
		else if (matchTarget.eventType != null && this.eventType == null)
			return false;
		else if (this.eventType == null && matchTarget.eventType == null)
			return false;
		else if (getEventId() == null && matchTarget.getEventId() != null)
			return false;
		else if (getEventId() != null && matchTarget.getEventId() == null)
			return false;
		return matchTarget.eventType.equalsIgnoreCase(this.eventType)
			&& ((this.getEventId() == matchTarget.getEventId())
				|| this.getEventId().equalsIgnoreCase(matchTarget.getEventId()));
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
