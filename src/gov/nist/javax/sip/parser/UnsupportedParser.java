package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;

/** Parser for Unsupported header.
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
public class UnsupportedParser extends   HeaderParser{
    
        /** Creates a new instance of UnsupportedParser 
         * @param unsupported - Unsupported header to parse
         */
        public UnsupportedParser(String unsupported) {
		super(unsupported);
	}

        /** Constructor
         * @param lexer - the lexer to use to parse the header
         */
        protected UnsupportedParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message
         * @return SIPHeader (Unsupported object)
         * @throws SIPParseException if the message does not respect the spec.
         */
        public SIPHeader parse() throws ParseException {
            UnsupportedList unsupportedList=new UnsupportedList();
            if (debug) dbg_enter("UnsupportedParser.parse");
            
            try {
                headerName(TokenTypes.UNSUPPORTED);
                
                while (lexer.lookAhead(0) != '\n') {
                    this.lexer.SPorHT();
                    Unsupported unsupported= new Unsupported();
                    unsupported.setHeaderName(SIPHeaderNames.UNSUPPORTED);
                    
                    // Parsing the option tag
                    this.lexer.match(TokenTypes.ID);
                    Token token=lexer.getNextToken();
                    unsupported.setOptionTag(token.getTokenValue() );
                    this.lexer.SPorHT();
                    
                    unsupportedList.add(unsupported);
                    
                    while (lexer.lookAhead(0) == ',') {
                        this.lexer.match(',');
                        this.lexer.SPorHT();
                        
                        unsupported= new Unsupported();
                        
                        // Parsing the option tag
                        this.lexer.match(TokenTypes.ID);
                        token=lexer.getNextToken();
                        unsupported.setOptionTag(token.getTokenValue() );
                        this.lexer.SPorHT();
                        
                        unsupportedList.add(unsupported);
                    }
                    
                }
            } finally {
                if (debug) dbg_leave("UnsupportedParser.parse");
            }
            
            return unsupportedList;
        }
        
        /** 
        public static void main(String args[]) throws ParseException {
		String unsupported[] = {
                "Unsupported: foo \n",
                "Unsupported: foo1, foo2 ,foo3 , foo4\n"
                };
			
		for (int i = 0; i < unsupported.length; i++ ) {
		    UnsupportedParser parser = 
			  new UnsupportedParser(unsupported[i]);
		    UnsupportedList u= (UnsupportedList) parser.parse();
		    System.out.println("encoded = " + u.encode());
		}
			
	}
	*/
}
