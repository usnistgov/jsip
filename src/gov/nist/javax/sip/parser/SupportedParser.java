package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for Supported header.
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
public class SupportedParser extends   HeaderParser {
    
    /** Creates a new instance of SupportedParser 
     * @param supported the header to parse
     */
    public SupportedParser(String supported) {
        super(supported);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected SupportedParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the String message
     * @return SIPHeader (Supported object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
            SupportedList supportedList=new SupportedList();
            if (debug) dbg_enter("SupportedParser.parse");
            
            try {
                headerName(TokenTypes.SUPPORTED);
                
                while (lexer.lookAhead(0) != '\n') {
                    this.lexer.SPorHT();
                    Supported supported= new Supported();
                    supported.setHeaderName(SIPHeaderNames.SUPPORTED);
                    
                    // Parsing the option tag
                    this.lexer.match(TokenTypes.ID);
                    Token token=lexer.getNextToken();
                    supported.setOptionTag(token.getTokenValue() );
                    this.lexer.SPorHT();
                    
                    supportedList.add(supported);
                    
                    while (lexer.lookAhead(0) == ',') {
                        this.lexer.match(',');
                        this.lexer.SPorHT();
                        
                        supported= new Supported();
                        
                        // Parsing the option tag
                        this.lexer.match(TokenTypes.ID);
                        token=lexer.getNextToken();
                        supported.setOptionTag(token.getTokenValue() );
                        this.lexer.SPorHT();
                        
                        supportedList.add(supported);
                    }
                    
                }
            } finally {
                if (debug) dbg_leave("SupportedParser.parse");
            }
            
            return supportedList;
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String supported[] = {
            "Supported: 100rel \n",
            "Supported: foo1, foo2 ,foo3 , foo4 \n"
        };
        
        for (int i = 0; i < supported.length; i++ ) {
            SupportedParser parser =
            new SupportedParser(supported[i]);
            SupportedList s= (SupportedList) parser.parse();
            System.out.println("encoded = " + s.encode());
        }
        
    }
     */
}
