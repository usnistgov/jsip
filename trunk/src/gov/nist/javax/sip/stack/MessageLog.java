/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/

package gov.nist.javax.sip.stack;

/**
 * This class stores a message along with some other informations
 * Used to log messages.
 *
 *@version 1.2 $Revision: 1.6 $ $Date: 2006-07-02 09:52:43 $
 *
 * @author M. Ranganathan   <br/>
 * @author Marc Bednarek  <br/>
 * 
 *
 */
class MessageLog {

	private String message;

	private String source;

	private String destination;

	private long timeStamp;

	private boolean isSender;

	private String firstLine;

	private String statusMessage;

	private String tid;

	private String callId;

	private int debugLine;

	private long timeStampHeaderValue;

	public boolean equals(Object other) {
		if (!(other instanceof MessageLog)) {
			return false;
		} else {
			MessageLog otherLog = (MessageLog) other;
			return otherLog.message.equals(message)
				&& otherLog.timeStamp == timeStamp;
		}
	}

	/**
	 * Constructor
	 */

	public MessageLog(
		String message,
		String source,
		String destination,
		String timeStamp,
		boolean isSender,
		String firstLine,
		String statusMessage,
		String tid,
		String callId,
		int lineCount,
		long timeStampHeaderValue) {
		if (message == null || message.equals(""))
			throw new IllegalArgumentException("null msg");
		this.message = message;
		this.source = source;
		this.destination = destination;
		try {
			long ts = Long.parseLong(timeStamp);
			if (ts < 0)
				throw new IllegalArgumentException("Bad time stamp ");
			this.timeStamp = ts;
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException(
				"Bad number format " + timeStamp);
		}
		this.isSender = isSender;
		this.firstLine = firstLine;
		this.statusMessage = statusMessage;
		this.tid = tid;
		this.callId = callId;
		this.debugLine = lineCount;
		this.timeStampHeaderValue = timeStampHeaderValue;
	}

	protected long getTimeStamp() {
		return this.timeStamp;
	}

	public MessageLog(
		String message,
		String source,
		String destination,
		long timeStamp,
		boolean isSender,
		String firstLine,
		String statusMessage,
		String tid,
		String callId,
		int lineCount,
		long timestampVal) {
		if (message == null || message.equals(""))
			throw new IllegalArgumentException("null msg");
		this.message = message;
		this.source = source;
		this.destination = destination;
		if (timeStamp < 0)
			throw new IllegalArgumentException("negative ts");
		this.timeStamp = timeStamp;
		this.isSender = isSender;
		this.firstLine = firstLine;
		this.statusMessage = statusMessage;
		this.tid = tid;
		this.callId = callId;
		this.debugLine = lineCount;
		this.timeStampHeaderValue = timestampVal;
	}

	public String flush(long startTime) {
		String log;

		if (statusMessage != null) {
			log =
				"<message\nfrom=\""
					+ source
					+ "\" \nto=\""
					+ destination
					+ "\" \ntime=\""
					+ (timeStamp - startTime)
					+ "\"" 
					+ (this.timeStampHeaderValue != 0 ? "\ntimeStamp = \"" + timeStampHeaderValue + "\"": "")
					+ "\nisSender=\""
					+ isSender
					+ "\" \nstatusMessage=\""
					+ statusMessage
					+ "\" \ntransactionId=\""
					+ tid
					+ "\" \ncallId=\""
					+ callId
					+ "\" \nfirstLine=\""
					+ firstLine.trim()
					+ "\" \ndebugLine=\""
					+ debugLine
					+ "\">\n";
			log += "<![CDATA[";
			log += message;
			log += "]]>\n";
			log += "</message>\n";
		} else {
			log =
				"<message\nfrom=\""
					+ source
					+ "\" \nto=\""
					+ destination
					+ "\" \ntime=\""
					+ (timeStamp - startTime)+ "\" "
					+ (this.timeStampHeaderValue != 0 ? "\ntimeStamp = \"" + timeStampHeaderValue + "\"": "")
					+ "\nisSender=\""
					+ isSender
					+ "\" \ntransactionId=\""
					+ tid
					+ "\" \ncallId=\""
					+ callId
					+ "\" \nfirstLine=\""
					+ firstLine.trim()
					+ "\" \ndebugLine=\""
					+ debugLine
					+ "\">\n";
			log += "<![CDATA[";
			log += message;
			log += "]]>\n";
			log += "</message>\n";
		}
		return log;
	}
	/**
	 * Get an XML String for this message
	 */

	public String flush() {
		String log;

		if (statusMessage != null) {
			log =
				"<message\nfrom=\""
					+ source
					+ "\" \nto=\""
					+ destination
					+ "\" \ntime=\""
					+ timeStamp
					+ "\"" 
					+(this.timeStampHeaderValue != 0 ? "\ntimeStamp = \"" + timeStampHeaderValue + "\"": "")
					+"\nisSender=\""
					+ isSender
					+ "\" \nstatusMessage=\""
					+ statusMessage
					+ "\" \ntransactionId=\""
					+ tid
					+ "\" \nfirstLine=\""
					+ firstLine.trim()
					+ "\" \ncallId=\""
					+ callId
					+ "\" \ndebugLine=\""
					+ debugLine
					+ "\" \n>\n";
			log += "<![CDATA[";
			log += message;
			log += "]]>\n";
			log += "</message>\n";
		} else {
			log =
				"<message\nfrom=\""
					+ source
					+ "\" \nto=\""
					+ destination
					+ "\" \ntime=\""
					+ timeStamp
					+ "\"" + 
					(this.timeStampHeaderValue != 0 ? "\ntimeStamp = \"" + timeStampHeaderValue + "\"": "")	
					+"\nisSender=\""
					+ isSender
					+ "\" \ntransactionId=\""
					+ tid
					+ "\" \ncallId=\""
					+ callId
					+ "\" \nfirstLine=\""
					+ firstLine.trim()
					+ "\" \ndebugLine=\""
					+ debugLine
					+ "\" \n>\n";
			log += "<![CDATA[";
			log += message;
			log += "]]>\n";
			log += "</message>\n";
		}
		return log;
	}
}
