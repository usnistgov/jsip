/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
 *******************************************************************************/

package gov.nist.javax.sip.header;

import javax.sip.InvalidArgumentException;
import javax.sip.header.*;

/**  
 * TimeStamp SIP Header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-01-22 13:26:30 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 * @author Olivier Deruelle <deruelle@nist.gov><br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class TimeStamp extends SIPHeader implements TimeStampHeader {

	/** timeStamp field
	 */
	protected float timeStamp;

	/** delay field
	 */
	protected float delay;

	/** Default Constructor
	  */
	public TimeStamp() {
		super(TIMESTAMP);
		delay = -1;
	}

	/**
	 * Return canonical form of the header.
	 * @return String
	 */
	public String encodeBody() {
		if (delay != -1)
			return new Float(timeStamp).toString()
				+ SP
				+ new Float(delay).toString();
		else
			return new Float(timeStamp).toString();
	}

	/** return true if delay exists
	 * @return boolean
	 */
	public boolean hasDelay() {
		return delay != -1;
	}

	/* remove the Delay field
	 */
	public void removeDelay() {
		delay = -1;
	}

	/********************************************************************************/
	/********************** JAIN-SIP 1.1 methods ************************************/
	/********************************************************************************/

	/**
	* Sets the timestamp value of this TimeStampHeader to the new timestamp
	* value passed to this method.
	*
	* @param timeStamp - the new float timestamp value
	* @throws InvalidArgumentException if the timestamp value argument is a
	* negative value.
	*/
	public void setTimeStamp(float timeStamp) throws InvalidArgumentException {
		if (timeStamp < 0)
			throw new InvalidArgumentException(
				"JAIN-SIP Exception, TimeStamp, "
					+ "setTimeStamp(), the timeStamp parameter is <0");
		this.timeStamp = timeStamp;
	}

	/**
	 * Gets the timestamp value of this TimeStampHeader.
	 *
	 * @return the timestamp value of this TimeStampHeader
	 */
	public float getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Gets delay of TimeStampHeader. This method return <code>-1</code> if the
	 * delay paramater is not set.
	 *
	 * @return the delay value of this TimeStampHeader
	 */

	public float getDelay() {
		return delay;
	}

	/**
	 * Sets the new delay value of the TimestampHeader to the delay paramter
	 * passed to this method
	 *
	 * @param delay - the new float delay value
	 * @throws InvalidArgumentException if the delay value argumenmt is a
	 * negative value other than <code>-1</code>.
	 */

	public void setDelay(float delay) throws InvalidArgumentException {
		if (delay < 0 && delay != -1)
			throw new InvalidArgumentException(
				"JAIN-SIP Exception, TimeStamp, "
					+ "setDelay(), the delay parameter is <0");
		this.delay = delay;
	}
}
/*
 * $Log: not supported by cvs2svn $
 */
