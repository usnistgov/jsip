/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.*;

/**
 * An interface for a genereic message processor for SIP Request messages.
 * This is implemented by the application. The stack calls the message
 * factory with a pointer to the parsed structure to create one of these
 * and then calls processRequest on the newly created SIPServerRequest
 * It is the applications responsibility to take care of what needs to be
 * done to actually process the request.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-05-18 15:26:43 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public interface SIPServerRequestInterface {
	/**
	 * Get the channel to where to send the response
	 * (the outgoing message channel).
	 */
	public MessageChannel getResponseChannel();

	/**
	 * Process the message.  This incorporates a feature request
	 * by Salvador Rey Calatayud <salreyca@TELECO.UPV.ES>
	 * @param sipRequest is the incoming SIP Request.
	 * @param  incomingChannel is the incoming message channel (parameter
	 * added in response to a request by Salvador Rey Calatayud.)
	 */
	public void processRequest(
		SIPRequest sipRequest,
		MessageChannel incomingChannel);

	/**
	 * Get processing information. 
	 * The stack queries processing information to add to the message log.
	 * by calling this interface. Return null if no processing information
	 * of interes thas been generated.
	 */
	public String getProcessingInfo();
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/22 13:26:33  sverker
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
