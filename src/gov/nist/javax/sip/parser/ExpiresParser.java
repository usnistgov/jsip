
package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import java.util.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/** Parser for SIP Expires Parser. Converts from SIP Date to the
* internal storage (Calendar).
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 */
public class ExpiresParser extends HeaderParser {
    
    
    
    /** protected constructor.
     *@param text is the text of the header to parse
     */
    public ExpiresParser(String text) {
        super(text);
    }
    
    /** constructor.
     *@param lexer is the lexer passed in from the enclosing parser.
     */
    protected ExpiresParser(Lexer lexer) {
        super(lexer);
    }
    
    /** Parse the header.
     */
    public SIPHeader parse() throws ParseException {
        Expires expires = new Expires();
	if (debug) dbg_enter("parse");
        try {
            lexer.match(TokenTypes.EXPIRES);
            lexer.SPorHT();
            lexer.match(':');
            lexer.SPorHT();
            String nextId = lexer.getNextId();
	    lexer.match('\n');
            try {
                int delta = Integer.parseInt(nextId);
                expires.setExpires(delta);
                return expires;
            } catch (NumberFormatException ex) {
		throw createParseException("bad integer format");
	    } catch (InvalidArgumentException ex) {
		throw createParseException(ex.getMessage());
	    }
        } finally  {
		if (debug) dbg_leave("parse");
        }
        
        
    }
    
    /** Test program -- to be removed in final version.
    public static void main(String args[]) throws ParseException {
        String expires[] = {
            "Expires: 1000\n" };
            
            for (int i = 0; i < expires.length; i++ ) {
		try {
                	System.out.println("Parsing " + expires[i]);
                	ExpiresParser ep = new ExpiresParser(expires[i]);
                	Expires e = (Expires) ep.parse();
                	System.out.println("encoded = " +e.encode());
		} catch (ParseException ex) {
		  	System.out.println(ex.getMessage());
		}
            }
            
    }
     */
    
    
}
