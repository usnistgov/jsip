package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.*;
/** Parser for CALL ID header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov> 
*@author  Olivier Deruelle <deruelle@nist.gov>
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class CallIDParser extends HeaderParser {
    
    /** Creates new CallIDParser
     * @param String callID message to parse to set
     */
    public CallIDParser(String callID) {
        super(callID);
    }
    
    /** Constructor
     * @param Lexer lexer to set
     */
    protected CallIDParser(Lexer lexer) {
        super(lexer);
    }
    
        /** parse the String message
         * @return SIPHeader (CallID object)
         * @throws ParseException if the message does not respect the spec.
         */
    public SIPHeader parse() throws ParseException {
      if (debug) dbg_enter("parse");
      try {  
        this.lexer.match (TokenTypes.CALL_ID);
        this.lexer.SPorHT();
        this.lexer.match(':');
        this.lexer.SPorHT();
        
        CallID callID=new CallID();
        
        this.lexer.SPorHT();
        String rest=lexer.getRest();
        callID.setCallId(rest.trim());
        return callID;
       }finally {
		if (debug) dbg_leave("parse");
	}
    }
    
/**
    public static void main(String args[]) throws ParseException {
        String call[] = {
	    "Call-ID: f0b40bcc-3485-49e7-ad1a-f1dfad2e39c9@10.5.0.53\n",
            "Call-ID: f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
            "i:f81d4fae-7dec-11d0-a765-00a0c91e6bf6@foo.bar.com\n",
            "Call-ID: 1@10.0.0.1\n",
            "Call-ID: kl24ahsd546folnyt2vbak9sad98u23naodiunzds09a3bqw0sdfbsk34poouymnae0043nsed09mfkvc74bd0cuwnms05dknw87hjpobd76f\n",
            "Call-Id: 281794\n"
        };
        
        for (int i = 0; i <call.length; i++ ) {
            CallIDParser cp =
            new CallIDParser(call[i]);
            CallID callID= (CallID) cp.parse();
            System.out.println("encoded = " + callID.encode());
        }
        
    }
**/
    
    
    
}


