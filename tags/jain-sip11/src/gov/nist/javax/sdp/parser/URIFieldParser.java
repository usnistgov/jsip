/*
 * URIFieldParser.java
 *
 * Created on February 25, 2002, 11:10 AM
 */

package gov.nist.javax.sdp.parser;
import gov.nist.javax.sdp.fields.*;
import gov.nist.core.*;
import java.net.*;
import java.text.ParseException;

/** URI Field Parser.
*
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle <deruelle@nist.gov>  
*@author M. Ranganathan <mranga@nist.gov> <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/

public class URIFieldParser extends SDPParser {

    /** Creates new URIFieldParser */
    public URIFieldParser(String uriField) {
        this.lexer = new Lexer("charLexer",uriField);
    }
    
    /** Get the URI field
     * @return URIField
     */
    public URIField uriField() throws ParseException  {
        try{
            this.lexer.match ('u');
            this.lexer.SPorHT();
            this.lexer.match('=');
            this.lexer.SPorHT();
            
            URIField uriField=new URIField();
            String rest= lexer.getRest().trim(); 
            uriField.setURI(rest);
            return uriField;  
        }
        catch(Exception e) {
            throw lexer.createParseException();
        }  
    }

    public SDPField parse() throws ParseException {
	return this.uriField();
    }

/**
    
    public static void main(String[] args) throws ParseException {
	    String uri[] = {
			"u=http://www.cs.ucl.ac.uk/staff/M.Handley/sdp.03.ps\n",
                        "u=sip:j.doe@big.com\n",
                        "u=sip:j.doe:secret@big.com;transport=tcp\n",
                        "u=sip:j.doe@big.com?subject=project\n",
                        "u=sip:+1-212-555-1212:1234@gateway.com;user=phone\n"
                };

	    for (int i = 0; i < uri.length; i++) {
	       URIFieldParser uriFieldParser=new URIFieldParser(
                uri[i] );
		URIField uriField=uriFieldParser.uriField();
		System.out.println("toParse: " +uri[i]);
		System.out.println("encoded: " +uriField.encode());
	    }

	}
**/



}

