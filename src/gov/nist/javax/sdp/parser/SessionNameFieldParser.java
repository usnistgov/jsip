/*
 * SessionNameFieldParser.java
 *
 * Created on February 25, 2002, 10:26 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.text.*;

/**
 *
 * @author  deruelle
 * @version 1.0
 */
public class SessionNameFieldParser extends SDPParser {

    /** Creates new SessionNameFieldParser */
    public SessionNameFieldParser(String sessionNameField) {
        this.lexer = new Lexer("charLexer",sessionNameField);
    }
    
    /** Get the SessionNameField
     * @return SessionNameField
     */
    public SessionNameField sessionNameField() throws ParseException  {
        try{
            this.lexer.match ('s');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            SessionNameField sessionNameField=new SessionNameField();
            String rest= lexer.getRest(); 
            sessionNameField.setSessionName(rest.trim());
            
            return sessionNameField;
        }
        catch(Exception e) {
            throw lexer.createParseException();
        }  
        
    }

    public SDPField parse() throws ParseException {
	return this.sessionNameField();
    }
    
    public static void main(String[] args) throws ParseException {
	    String session[] = {
			"s=SDP Seminar \n",
                        "s= Session SDP\n"
                };

	    for (int i = 0; i < session.length; i++) {
	      SessionNameFieldParser sessionNameFieldParser=
			new SessionNameFieldParser( session[i] );
	        SessionNameField sessionNameField=
			sessionNameFieldParser.sessionNameField();
		System.out.println("encoded: " +sessionNameField.encode());
	    }

	}



}
