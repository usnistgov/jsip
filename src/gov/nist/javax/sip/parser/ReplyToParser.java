package gov.nist.javax.sip.parser;
import java.text.ParseException;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
/** Parser for a list of RelpyTo headers.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  <br/>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*@version 1.0
*/

public class ReplyToParser  extends AddressParametersParser {
    
    /** Creates a new instance of ReplyToParser 
     * @param replyTo the header to parse
     */
    public ReplyToParser(String replyTo) {
        super(replyTo);
    }
    
    /** Cosntructor
      * param lexer the lexer to use to parse the header
      */
     protected ReplyToParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the String message and generate the ReplyTo List Object
     * @return SIPHeader the ReplyTo List object
     * @throws SIPParseException if errors occur during the parsing
     */
    public SIPHeader parse() throws ParseException {
        ReplyTo replyTo=new ReplyTo();
        if (debug) dbg_enter("ReplyTo.parse");
        
        try {
            headerName(TokenTypes.REPLY_TO);
            
            replyTo.setHeaderName(SIPHeaderNames.REPLY_TO);
                    
            super.parse(replyTo);
             
            return replyTo;
        } finally {
            if (debug) dbg_leave("ReplyTo.parse");
        }
        
    }
    
/**
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Reply-To: Bob <sip:bob@biloxi.com>\n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            ReplyToParser rt =
            new ReplyToParser(r[i]);
            ReplyTo re = (ReplyTo) rt.parse();
            System.out.println("encoded = " +re.encode());
        }
        
    }
*/
    
}

