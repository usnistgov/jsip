/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;
import gov.nist.javax.sip.message.*;

/**
* An interface for a genereic message processor for SIP Response messages.
* This is implemented by the application. The stack calls the message
* factory with a pointer to the parsed structure to create one of these
* and then calls processResponse on the newly created SIPServerResponse
* It is the applications responsibility to take care of what needs to be
* done to actually process the response.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public interface SIPServerResponseInterface {
	/**
         * Process the Response.
         * @throws SIPServerException Exception that gets thrown by 
	 * this processor when an exception is encountered in the
         * message processing.
	 * @param  incomingChannel is the incoming message channel (parameter
	 * added in response to a request by Salvador Rey Calatayud.)
         * @param sipResponse is the responseto process.
         */
         public void processResponse(SIPResponse sipResponse,  
			MessageChannel incomingChannel ) 
			throws SIPServerException ;

	/** Get the Channel for the sender. 
 	*@return the MessageChannel through which you can send a
	* new request to the responder.
	*/
	public MessageChannel getRequestChannel();

	/** Get auxiliary information that is generated while logging for
	* the purpose of writing out the log file.
	*/
	public String getProcessingInfo();
}
