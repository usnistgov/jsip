package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for AlertInfo header.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  
*@author M. Ranganathan <mranga@nist.gov>  
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
* @version 1.0
*/
public class AlertInfoParser extends ParametersParser{
    
     /**
     * Creates a new instance of AlertInfo Parser
     * @param alertInfo  the header to parse 
     */
    public AlertInfoParser(String alertInfo ) {
        super(alertInfo );
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected AlertInfoParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the AlertInfo  String header
     * @return SIPHeader (AlertInfoList  object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("AlertInfoParser.parse");
        AlertInfoList list=new AlertInfoList();
        
        try {
            headerName(TokenTypes.ALERT_INFO);
            
            while (lexer.lookAhead(0) != '\n') {
                AlertInfo alertInfo= new AlertInfo();
                alertInfo.setHeaderName(SIPHeaderNames.ALERT_INFO);
                
                this.lexer.SPorHT();
                this.lexer.match('<');
                URLParser urlParser=new URLParser((Lexer)this.lexer);
                GenericURI uri=urlParser.uriReference(); 
                alertInfo.setAlertInfo(uri);
                this.lexer.match('>');
                this.lexer.SPorHT();
                
                super.parse(alertInfo);
                list.add(alertInfo);
                
                while (lexer.lookAhead(0) == ',') {
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    
                    alertInfo= new AlertInfo();
                    
                    this.lexer.SPorHT();
                    this.lexer.match('<');
                    urlParser=new URLParser((Lexer)this.lexer);
                    uri=urlParser.uriReference();
                    alertInfo.setAlertInfo(uri);
                    this.lexer.match('>');
                    this.lexer.SPorHT();
                    
                    super.parse(alertInfo);
                    list.add(alertInfo);
                }
            }
            
            return list;
        }
        finally {
            if (debug) dbg_leave("AlertInfoParser.parse");
        }
    }
    
    
}

