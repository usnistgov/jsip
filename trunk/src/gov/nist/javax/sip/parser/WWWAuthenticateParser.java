package gov.nist.javax.sip.parser;
import java.text.ParseException;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;

/** Parser for WWW authenitcate header.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* @version 1.0
*/
public class WWWAuthenticateParser extends ChallengeParser {

        /** Constructor
         * @param wwwAuthenticate -  message to parse
         */
        public WWWAuthenticateParser(String wwwAuthenticate) {
		super(wwwAuthenticate);
	}

        /** Cosntructor
         * @param  lexer - lexer to use.
         */
        protected WWWAuthenticateParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message 
         * @return SIPHeader (WWWAuthenticate object)
         * @throws SIPParseException if the message does not respect the spec.
         */
	public SIPHeader parse() throws ParseException {
		if (debug) dbg_enter("parse");
		try {
	       		headerName(TokenTypes.WWW_AUTHENTICATE);
	       		WWWAuthenticate wwwAuthenticate = new WWWAuthenticate();
	       		super.parse(wwwAuthenticate);
               		return wwwAuthenticate;
		} finally {
		   if (debug) dbg_leave("parse");
		}
	}

	
       

        
}

