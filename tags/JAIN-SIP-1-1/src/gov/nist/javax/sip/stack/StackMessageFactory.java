/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;
import gov.nist.javax.sip.message.*;

/**
 * An interface for generating new requests and responses. This is implemented
 * by the application and called by the stack for processing requests
 * and responses. When a Request comes in off the wire, the stack calls
 * newSIPServerRequest which is then responsible for processing the request.
 * When a response comes off the wire, the stack calls newSIPServerResponse
 * to process the response. 
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.2 $ $Date: 2004-06-21 05:42:33 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public interface StackMessageFactory {
	/**
	 * Make a new SIPServerResponse given a SIPRequest and a message 
	 * channel.
	 *
	 * @param sipRequest is the incoming request.
	 * @param msgChan is the message channel on which this request was
	 *	received.
	 */
	public ServerRequestInterface newSIPServerRequest(
		SIPRequest sipRequest,
		MessageChannel msgChan);

	/**
	 * Generate a new server response for the stack.
	 *
	 * @param sipResponse is the incoming response.
	 * @param msgChan is the message channel on which the response was
	 * received.
	 */
	public ServerResponseInterface newSIPServerResponse(
		SIPResponse sipResponse,
		MessageChannel msgChan);
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2004/06/21 05:32:51  mranga
 * Reviewed by:   mranga
 * re factored
 *
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
