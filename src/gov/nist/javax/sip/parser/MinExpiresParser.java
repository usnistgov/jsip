package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for MinExpires header.
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
public class MinExpiresParser extends  HeaderParser{
    
        /** Creates a new instance of MinExpiresParser 
         * @param minExpires the header to parse
         */
        public MinExpiresParser(String minExpires) {
		super(minExpires);
	}

        /** Cosntructor
         * @param lexer the lexer to use to parse the header
         */
        protected MinExpiresParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message
         * @return SIPHeader (MinExpiresParser)
         * @throws SIPParseException if the message does not respect the spec.
         */
        public SIPHeader parse() throws ParseException {
            
            if (debug) dbg_enter("MinExpiresParser.parse");
            MinExpires minExpires=new MinExpires();
            try {
                headerName(TokenTypes.MIN_EXPIRES);
                
                minExpires.setHeaderName(SIPHeaderNames.MIN_EXPIRES);
                
                String number=this.lexer.number();
                try{
                    minExpires.setExpires(Integer.parseInt(number));
                } 
                catch (InvalidArgumentException ex) {
                        throw createParseException(ex.getMessage());
                }
                this.lexer.SPorHT();
                
		this.lexer.match('\n');
                
                return minExpires;
            } 
            finally {
                if (debug) dbg_leave("MinExpiresParser.parse");
            }
        }
        
        /** Test program
        public static void main(String args[]) throws ParseException {
		String r[] = {
                "Min-Expires: 60 \n"
                };
			
		for (int i = 0; i < r.length; i++ ) {
		    MinExpiresParser parser = 
			  new MinExpiresParser(r[i]);
		    MinExpires m= (MinExpires) parser.parse();
		    System.out.println("encoded = " +m.encode());
		}		
	}
         */
        
}


