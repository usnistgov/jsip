package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for Authentication-Info header.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov> 
*@author M. Ranganathan <mranga@nist.gov>  
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class AuthenticationInfoParser extends ParametersParser{
    
    /**
     * Creates a new instance of AuthenticationInfoParser
     * @param authenticationInfo the header to parse 
     */
    public AuthenticationInfoParser(String authenticationInfo) {
        super(authenticationInfo);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected  AuthenticationInfoParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the AuthenticationInfo String header
     * @return SIPHeader (AuthenticationInfoList object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("AuthenticationInfoParser.parse");
        
        try {
            	headerName(TokenTypes.AUTHENTICATION_INFO);
            
                AuthenticationInfo authenticationInfo= 
			new AuthenticationInfo();
                authenticationInfo.setHeaderName
			(SIPHeaderNames.AUTHENTICATION_INFO);
                
                this.lexer.SPorHT();
                
                NameValue nv=super.nameValue();
                authenticationInfo.setParameter(nv);
                this.lexer.SPorHT();
                while (lexer.lookAhead(0) == ',') {
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    
                    nv=super.nameValue();
                    authenticationInfo.setParameter(nv);
                    this.lexer.SPorHT();
                }
                this.lexer.SPorHT();
                //this.lexer.match('\n');
                
              return authenticationInfo;
        }
        finally {
            if (debug) dbg_leave("AuthenticationInfoParser.parse");
        }
    }
    
/**
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\"\n",
            "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\",rspauth=\"hello\"\n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            AuthenticationInfoParser parser =
            new AuthenticationInfoParser(r[i]);
            AuthenticationInfo a= (AuthenticationInfo) parser.parse();
            System.out.println("encoded = " + a.encode());
        }    
    }
**/
    
}
