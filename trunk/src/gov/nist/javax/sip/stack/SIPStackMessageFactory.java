/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.stack;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;

/**
* An interface for generating new requests and responses. This is implemented
* by the application and called by the stack for processing requests
* and responses. When a Request comes in off the wire, the stack calls
* newSIPServerRequest which is then responsible for processing the request.
* When a response comes off the wire, the stack calls newSIPServerResponse
* to process the response. 
*
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public interface SIPStackMessageFactory {
	/**
	* Make a new SIPServerResponse given a SIPRequest and a message 
	* channel.
	*
	*@param sipRequest is the incoming request.
	*@param msgChan is the message channel on which this request was
	*	received.
	*/
	public SIPServerRequestInterface
	newSIPServerRequest(SIPRequest sipRequest, MessageChannel msgChan);

	/**
	* Generate a new server response for the stack.
	*
	*@param sipResponse is the incoming response.
	*@param msgChan is the message channel on which the response was
	* received.
	*/
	public SIPServerResponseInterface
         newSIPServerResponse 
		(SIPResponse sipResponse, MessageChannel msgChan);
}
