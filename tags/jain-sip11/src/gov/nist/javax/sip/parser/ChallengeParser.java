package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;

/** Parser for the challenge portion of the authentication header.
 *
 *@version  JAIN-SIP-1.1
 *
 *@author Olivier Deruelle  <deruelle@nist.gov>  <br/>
 *
 *<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 * @version 1.0
 */

public abstract class ChallengeParser extends HeaderParser {
    
    /** Constructor
     * @param String challenge  message to parse to set
     */
    protected ChallengeParser(String challenge) {
		super(challenge);
    }

    /** Constructor
     * @param String challenge  message to parse to set
     */
    protected ChallengeParser(Lexer lexer) {
		super(lexer);
    }
    
    
    /** Get the parameter of the challenge string
     * @return NameValue containing the parameter
     */
    protected void parseParameter(AuthenticationHeader header) 
		throws ParseException {
	
	if (debug) dbg_enter("parseParameter");
	try {
	   NameValue nv = this.nameValue('=');
	   header.setParameter(nv);
       } finally {
	   if (debug) dbg_leave("parseParameter");
       }
	
        
    }
    
    
    /** parser the String message
     * @return Challenge object
     * @throws ParseException if the message does not respect the spec.
     */
    public void parse(AuthenticationHeader header) throws ParseException {
        
        
        // the Scheme:
        this.lexer.SPorHT();
        lexer.match(TokenTypes.ID);
        Token type= lexer.getNextToken();
        this.lexer.SPorHT();
        header.setScheme(type.getTokenValue());
        
        
        // The parameters:
	try {
        while (lexer.lookAhead(0) != '\n') {
            this.parseParameter(header);
            this.lexer.SPorHT();
            if ( lexer.lookAhead(0) == '\n' || 
		lexer.lookAhead(0) == '\0' ) break;
            this.lexer.match(',');
            this.lexer.SPorHT();
        }
	} catch (ParseException ex) {
		throw ex;
	}
        
        
    }
    
}


