package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for InReplyTo header.
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
public class InReplyToParser extends   HeaderParser{
    
        /** Creates a new instance of InReplyToParser 
         * @param inReplyTo the header to parse
         */
        public InReplyToParser(String inReplyTo) {
		super(inReplyTo);
	}

        /** Constructor
         * @param lexer the lexer to use to parse the header
         */
        protected InReplyToParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message
         * @return SIPHeader (InReplyToList object)
         * @throws SIPParseException if the message does not respect the spec.
         */
        public SIPHeader parse() throws ParseException {
            
            if (debug) dbg_enter("InReplyToParser.parse");
            InReplyToList list=new InReplyToList();

            try {
                headerName(TokenTypes.IN_REPLY_TO);
                
                while (lexer.lookAhead(0) != '\n') {
                    InReplyTo inReplyTo= new InReplyTo();
                    inReplyTo.setHeaderName(SIPHeaderNames.IN_REPLY_TO);
                    
                  
                    this.lexer.match(TokenTypes.ID);
                    Token token=lexer.getNextToken();
                    if (lexer.lookAhead(0)=='@') {
                        this.lexer.match('@');
                        this.lexer.match(TokenTypes.ID);
                        Token secToken=lexer.getNextToken();
                        inReplyTo.setCallId(token.getTokenValue() +"@"+
                            secToken.getTokenValue());
                    }
                    else {
                        inReplyTo.setCallId(token.getTokenValue() );
                    }
                    
                    this.lexer.SPorHT();
                    
                    list.add(inReplyTo);
                    
                    while (lexer.lookAhead(0) == ',') {
                        this.lexer.match(',');
                        this.lexer.SPorHT();
                        
                        inReplyTo= new InReplyTo();
                        
                        this.lexer.match(TokenTypes.ID);
                        token=lexer.getNextToken();
                        if (lexer.lookAhead(0)=='@') {
                            this.lexer.match('@');
                            this.lexer.match(TokenTypes.ID);
                            Token secToken=lexer.getNextToken();
                            inReplyTo.setCallId(token.getTokenValue() +"@"+
                            secToken.getTokenValue());
                        }
                        else {
                            inReplyTo.setCallId(token.getTokenValue() );
                        }
                        
                        list.add(inReplyTo);
                    }
                }

                return list;
            }
            finally {
                if (debug) dbg_leave("InReplyToParser.parse");
            }
        }
        
        /** Test program
        public static void main(String args[]) throws ParseException {
		String p[] = {
                "In-Reply-To: 70710@saturn.bell-tel.com, 17320@saturn.bell-tel.com\n",
                "In-Reply-To: 70710 \n"
                };
			
		for (int i = 0; i < p.length; i++ ) {
		    InReplyToParser parser = 
			  new InReplyToParser(p[i]);
		    InReplyToList in= (InReplyToList) parser.parse();
		    System.out.println("encoded = " + in.encode());
		}		
	}
         */
        
}

