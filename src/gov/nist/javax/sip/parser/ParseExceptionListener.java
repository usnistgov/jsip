/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
*******************************************************************************/
package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.message.*;
import java.text.ParseException;
/**
* A listener interface that enables customization of parse error handling.
* An class that implements this interface is registered with the 
* parser and is called back from the parser handle parse errors.
*/

public interface ParseExceptionListener {
	/**
	* This gets called from the parser when a parse error is generated.
	* The handler is supposed to introspect on the error class and 
	* header name to handle the error appropriately. The error can
	* be handled by :
	*<ul>
	* <li>1. Re-throwing an exception and aborting the parse.
	* <li>2. Ignoring the header (attach the unparseable header to
	* the SIPMessage being parsed).
	* <li>3. Re-Parsing the bad header and adding it to the sipMessage
	* </ul>
	*
	*@param  ex - parse exception being processed.
	*@param  sipMessage -- sip message being processed.
	*@param headerText --  header/RL/SL text being parsed.
	*@param messageText -- message where this header was detected.
	*/
	public void handleException(ParseException ex , 
			SIPMessage sipMessage, 
			Class headerClass,
			String headerText, 
			String messageText) 
			throws ParseException;
}
