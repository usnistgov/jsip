package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for CallInfo header.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov> 
*@author M. Ranganathan <mranga@nist.gov> 
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* @version 1.0
*/
public class CallInfoParser  extends ParametersParser{
    
    /**
     * Creates a new instance of CallInfoParser
     * @param callInfo the header to parse 
     */
    public CallInfoParser(String callInfo) {
        super(callInfo);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected  CallInfoParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the CallInfo String header
     * @return SIPHeader (CallInfoList object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("CallInfoParser.parse");
        CallInfoList list=new CallInfoList();
        
        try {
            headerName(TokenTypes.CALL_INFO);
            
            while (lexer.lookAhead(0) != '\n') {
                CallInfo callInfo= new CallInfo();
                callInfo.setHeaderName(SIPHeaderNames.CALL_INFO);
                
                this.lexer.SPorHT();
                this.lexer.match('<');
                URLParser urlParser=new URLParser((Lexer)this.lexer);
                GenericURI uri=urlParser.uriReference(); 
                callInfo.setInfo(uri);
                this.lexer.match('>');
                this.lexer.SPorHT();
                
                super.parse(callInfo);
                list.add(callInfo);
                
                while (lexer.lookAhead(0) == ',') {
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    
                    callInfo= new CallInfo();
                    
                    this.lexer.SPorHT();
                    this.lexer.match('<');
                    urlParser=new URLParser((Lexer)this.lexer);
                    uri=urlParser.uriReference();
                    callInfo.setInfo(uri);
                    this.lexer.match('>');
                    this.lexer.SPorHT();
                    
                    super.parse(callInfo);
                    list.add(callInfo);
                }
            }
            
            return list;
        }
        finally {
            if (debug) dbg_leave("CallInfoParser.parse");
        }
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Call-Info: <http://wwww.example.com/alice/photo.jpg> ;purpose=icon,"+
            "<http://www.example.com/alice/> ;purpose=info\n",
            "Call-Info: <http://wwww.example.com/alice/photo1.jpg>\n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            CallInfoParser parser =
            new CallInfoParser(r[i]);
            CallInfoList e= (CallInfoList) parser.parse();
            System.out.println("encoded = " + e.encode());
        }    
    }
     */
    
}
