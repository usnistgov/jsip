/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.message.*;

/**
* An interface for a genereic message processor for SIP Request messages.
* This is implemented by the application. The stack calls the message
* factory with a pointer to the parsed structure to create one of these
* and then calls processRequest on the newly created SIPServerRequest
* It is the applications responsibility to take care of what needs to be
* done to actually process the request.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public interface SIPServerRequestInterface {
        /** Get the channel to where to send the response
	* (the outgoing message channel).
	*/
        public MessageChannel getResponseChannel();
	/**
         * Process the message.  This incorporates a feature request
	 * by Salvador Rey Calatayud <salreyca@TELECO.UPV.ES>
	 * @param sipRequest is the incoming SIP Request.
	 * @param  incomingChannel is the incoming message channel (parameter
	 * added in response to a request by Salvador Rey Calatayud.)
         * @throws SIPServerException Exception that gets thrown by 
	 * this processor when an exception is encountered in the
         * message processing.
         */
	public void processRequest(SIPRequest sipRequest, 
		MessageChannel incomingChannel ) 
		throws SIPServerException ;


	/** Get processing information. 
	* The stack queries processing information to add to the message log.
	* by calling this interface. Return null if no processing information
	* of interes thas been generated.
	*/
	public String getProcessingInfo();


}
