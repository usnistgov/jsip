package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for Reason header.
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
public class ReasonParser extends   ParametersParser {
    
    /** Creates a new instance of ReasonParser 
     * @param reason the header to parse
     */
    public ReasonParser(String reason) {
        super(reason);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected ReasonParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the String message
     * @return SIPHeader (ReasonParserList object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        ReasonList reasonList=new ReasonList();
        if (debug) dbg_enter("ReasonParser.parse");
        
        try {
            headerName(TokenTypes.REASON);
            this.lexer.SPorHT();
            while (lexer.lookAhead(0) != '\n') {
                Reason reason= new Reason();
                this.lexer.match(TokenTypes.ID);
                Token token= lexer.getNextToken();
                String value=token.getTokenValue();
                
                reason.setProtocol(value); 
		super.parse(reason);
                reasonList.add(reason);
                if (lexer.lookAhead(0) == ',') {
                        this.lexer.match(',');
                        this.lexer.SPorHT();
                } else this.lexer.SPorHT();
                        
            }
	} catch (ParseException ex ) {
		ex.printStackTrace();
		System.out.println(lexer.getRest());
        } finally {
            if (debug) dbg_leave("ReasonParser.parse");
        }
        
        return reasonList;
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Reason: SIP ;cause=200 ;text=\"Call completed elsewhere\"\n",
            "Reason: Q.850 ;cause=16 ;text=\"Terminated\"\n",
            "Reason: SIP ;cause=600 ;text=\"Busy Everywhere\"\n",
            "Reason: SIP ;cause=580 ;text=\"Precondition Failure\","+
            "SIP ;cause=530 ;text=\"Pre Failure\"\n",
            "Reason: SIP \n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            ReasonParser parser =
            new ReasonParser(r[i]);
            ReasonList rl= (ReasonList) parser.parse();
            System.out.println("encoded = " + rl.encode());
        }    
    }
     */
    
}
