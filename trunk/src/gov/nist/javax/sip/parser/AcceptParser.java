package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for Accept header.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov> 
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class AcceptParser extends ParametersParser{
    
     /**
     * Creates a new instance of Accept Parser
     * @param accept  the header to parse 
     */
    public AcceptParser(String accept ) {
        super(accept );
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected AcceptParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the Accept  String header
     * @return SIPHeader (AcceptList  object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("AcceptParser.parse");
        AcceptList list=new AcceptList();
        
        try {
                headerName(TokenTypes.ACCEPT);
            
                Accept accept= new Accept();
                accept.setHeaderName(SIPHeaderNames.ACCEPT);
                
                this.lexer.SPorHT();
                this.lexer.match(TokenTypes.ID);
                Token token=lexer.getNextToken();
                accept.setContentType(token.getTokenValue());
                this.lexer.match('/');
                this.lexer.match(TokenTypes.ID);
                token=lexer.getNextToken();
                accept.setContentSubType(token.getTokenValue());
                this.lexer.SPorHT();
                
                super.parse(accept);
                list.add(accept);
                
                while (lexer.lookAhead(0) == ',') {
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    
                    accept= new Accept();
                    
                    this.lexer.match(TokenTypes.ID);
                    token=lexer.getNextToken();
                    accept.setContentType(token.getTokenValue());
                    this.lexer.match('/');
                    this.lexer.match(TokenTypes.ID);
                    token=lexer.getNextToken();
                    accept.setContentSubType(token.getTokenValue());
                    this.lexer.SPorHT();
                    super.parse(accept);
                    list.add(accept);
                    
                }
                return list;
        }
        finally {
            if (debug) dbg_leave("AcceptParser.parse");
        }
    }
    
    
}


