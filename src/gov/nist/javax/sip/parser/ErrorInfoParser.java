package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for ErrorInfo header.
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
public class ErrorInfoParser extends   ParametersParser
{
    
    /**
     * Creates a new instance of ErrorInfoParser
     * @param errorInfo the header to parse 
     */
    public ErrorInfoParser(String errorInfo) {
        super(errorInfo);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected  ErrorInfoParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the ErrorInfo String header
     * @return SIPHeader (ErrorInfoList object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("ErrorInfoParser.parse");
        ErrorInfoList list=new ErrorInfoList();
        
        try {
            headerName(TokenTypes.ERROR_INFO);
            
            while (lexer.lookAhead(0) != '\n') {
                ErrorInfo errorInfo= new ErrorInfo();
                errorInfo.setHeaderName(SIPHeaderNames.ERROR_INFO);
                
                this.lexer.SPorHT();
                this.lexer.match('<');
                URLParser urlParser=new URLParser((Lexer)this.lexer);
                GenericURI uri=urlParser.uriReference(); 
                errorInfo.setErrorInfo(uri);
                this.lexer.match('>');
                this.lexer.SPorHT();
                
                super.parse(errorInfo);
                list.add(errorInfo);
                
                while (lexer.lookAhead(0) == ',') {
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    
                    errorInfo= new ErrorInfo();
                    
                    this.lexer.SPorHT();
                    this.lexer.match('<');
                    urlParser=new URLParser((Lexer)this.lexer);
                    uri=urlParser.uriReference();
                    errorInfo.setErrorInfo(uri);
                    this.lexer.match('>');
                    this.lexer.SPorHT();
                    
                    super.parse(errorInfo);
                    list.add(errorInfo);
                }
            }
            
            return list;
        }
        finally {
            if (debug) dbg_leave("ErrorInfoParser.parse");
        }
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Error-Info: <sip:not-in-service-recording@atlanta.com>\n",
            "Error-Info: <sip:not-in-service-recording@atlanta.com>;param1=oli\n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            ErrorInfoParser parser =
            new ErrorInfoParser(r[i]);
            ErrorInfoList e= (ErrorInfoList) parser.parse();
            System.out.println("encoded = " + e.encode());
        }    
    }
     */
    
}
