package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for UserAgent header.
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
public class UserAgentParser extends   HeaderParser {
    
        /** Constructor
         * @param userAgent - UserAgent header to parse
	 *
         */
        public UserAgentParser(String userAgent) {
		super(userAgent);
	}

        /** Constructor
         * @param lexer - the lexer to use.
         */
        protected UserAgentParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message
         * @return SIPHeader (UserAgent object)
         * @throws SIPParseException if the message does not respect the spec.
         */
        public SIPHeader parse() throws ParseException {
            
            if (debug) dbg_enter("UserAgentParser.parse");
            UserAgent userAgent=new UserAgent();
            try {
                headerName(TokenTypes.USER_AGENT);
		if (this.lexer.lookAhead(0) == '\n') 
			throw createParseException("empty header");
                    
                //  mandatory token: product[/product-version] | (comment)
	       while (this.lexer.lookAhead(0) != '\n'   
			&& this.lexer.lookAhead(0) != '\0') {
                  if (this.lexer.lookAhead(0) == '(' ) {
                    String comment=this.lexer.comment();
                    userAgent.addProductToken('(' + comment + ')' );
                  } else {
		    String tok;
		    try {
		       tok = this.lexer.getString('/');
		       if (tok.charAt(tok.length()-1) == '\n') 
			  tok = tok.trim();
		       userAgent.addProductToken(tok);
		    } catch (ParseException ex) {
			tok = this.lexer.getRest();
			userAgent.addProductToken(tok);
			break;
		    }
		  }
		}
            } 
            finally {
                if (debug) dbg_leave("UserAgentParser.parse");
            }
            
            return  userAgent;
        }
        
        /** 
        public static void main(String args[]) throws ParseException {
		String userAgent[] = {
                "User-Agent: Softphone/Beta1.5 \n",
                "User-Agent: Nist/Beta1 (beta version) \n",
                "User-Agent: Nist UA (beta version)\n",
                "User-Agent: Nist1.0/Beta2 Ubi/vers.1.0 (very cool) \n"
                };
			
		for (int i = 0; i < userAgent.length; i++ ) {
		    UserAgentParser parser = 
			  new UserAgentParser(userAgent[i]);
		    UserAgent ua= (UserAgent) parser.parse();
		    System.out.println("encoded = " + ua.encode());
		}
			
	}
         */
}
