/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sdp.fields;
import gov.nist.core.*;
import javax.sdp.*;
import java.util.*;
/**
* Time Field.
*@version  JSR141-PUBLIC-REVIEW (subject to change).
*
*@author Olivier Deruelle <deruelle@antd.nist.gov>
*@author M. Ranganathan <mranga@nist.gov>  
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*Bug Report contributed by Brian J. Collins.
*
*/
public class TimeField extends SDPField implements Time {
	protected long startTime;
	protected long stopTime;
	public TimeField() {
		super(TIME_FIELD);
	}
	public long getStartTime() {
		return startTime;
	}
	public long getStopTime() {
		return stopTime;
	}
	/**
	* Set the startTime member  
	*/
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	/**
	* Set the stopTime member  
	*/
	public void setStopTime(long stopTime) {
		this.stopTime = stopTime;
	}

	/** Returns the start time of the conference/session.
	* @throws SdpParseException
	* @return the date
	*/
	public Date getStart() throws SdpParseException {
		return SdpFactory.getDateFromNtp( startTime );
	}

	/** Returns the stop time of the session
	 * @throws SdpParseException
	 * @return the stop time of the session.
	 */
	public Date getStop() throws SdpParseException {
		return SdpFactory.getDateFromNtp( stopTime );
	}

	/** Sets the stop time of the session.
	 * @param stop start - the start time
	 * @throws SdpException if the date is null
	 */
	public void setStop(Date stop) throws SdpException {
		if (stop == null)
			throw new SdpException("The date is null");
		else {
			this.stopTime = SdpFactory.getNtpTime( stop );
		}
	}

	/** Sets the start time of the conference/session.
	 * @param start start - the start time for the session.
	 * @throws SdpException if the date is null
	 */
	public void setStart(Date start) throws SdpException {
		if (start == null)
			throw new SdpException("The date is null");
		else {
			this.startTime = SdpFactory.getNtpTime( start );
		}
	}

	/** Returns whether the field will be output as a typed time 
	 * or a integer value.
	 *
	 *     Typed time is formatted as an integer followed by a unit character.
	 * The unit indicates an appropriate multiplier for
	 *     the integer.
	 *
	 *     The following unit types are allowed.
	 *          d - days (86400 seconds)
	 *          h - hours (3600 seconds)
	 *          m - minutes (60 seconds)
	 *          s - seconds ( 1 seconds)
	 * @return true, if the field will be output as a 
	 * typed time; false, if as an integer value.
	 */
	public boolean getTypedTime() {
		return false;
	}

	/** Sets whether the field will be output as a typed time or a integer value.
	 *
	 *     Typed time is formatted as an integer followed by a unit character. 
	 * The unit indicates an appropriate multiplier for
	 *     the integer.
	 *
	 *     The following unit types are allowed.
	 *          d - days (86400 seconds)
	 *          h - hours (3600 seconds)
	 *          m - minutes (60 seconds)
	 *          s - seconds ( 1 seconds)
	 * @param typedTime typedTime - if set true, the start and stop times will
	 * be output in an optimal typed time format; if false, the
	 *          times will be output as integers.
	 */
	public void setTypedTime(boolean typedTime) {

	}

	/** Returns whether the start and stop times were set to zero (in NTP).
	 * @return boolean
	 */
	public boolean isZero() {
		long stopTime = getStopTime();
		long startTime = getStartTime();
		if (stopTime == 0 && startTime == 0)
			return true;
		else
			return false;
	}

	/** Sets the start and stop times to zero (in NTP).
	 */
	public void setZero() {
		setStopTime(0);
		setStartTime(0);
	}

	/**
	 *  Get the string encoded version of this object
	 * @since v1.0
	 */
	public String encode() {
		return new StringBuffer()
			.append(TIME_FIELD)
			.append(startTime)
			.append(Separators.SP)
			.append(stopTime)
			.append(Separators.NEWLINE)
			.toString();
	}

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2004/01/22 13:26:27  sverker
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
