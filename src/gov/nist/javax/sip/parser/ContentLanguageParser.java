package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for ContentLanguage header.
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
public class ContentLanguageParser  extends HeaderParser{
    
     /**
     * Creates a new instance of ContentLanguageParser
     * @param contentLanguage the header to parse 
     */
    public ContentLanguageParser(String contentLanguage) {
        super(contentLanguage);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected  ContentLanguageParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the ContentLanguageHeader String header
     * @return SIPHeader (ContentLanguageList object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("ContentLanguageParser.parse");
        ContentLanguageList list=new ContentLanguageList();
        
        try {
            headerName(TokenTypes.CONTENT_LANGUAGE);
            
            while (lexer.lookAhead(0) != '\n') {
                ContentLanguage cl= new ContentLanguage();
                cl.setHeaderName(SIPHeaderNames.CONTENT_LANGUAGE);
                
                this.lexer.SPorHT();
                this.lexer.match(TokenTypes.ID);
                
                Token token=lexer.getNextToken();
                cl.setContentLanguage(new Locale(token.getTokenValue(),Locale.US.getCountry()) );
                
                this.lexer.SPorHT();
                list.add(cl);
                
                while (lexer.lookAhead(0) == ',') {
                    cl= new ContentLanguage();
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    this.lexer.match(TokenTypes.ID);
                    this.lexer.SPorHT();
                    token=lexer.getNextToken();
                    cl.setContentLanguage(new Locale( token.getTokenValue(),Locale.US.getCountry() ) );
                    
                    this.lexer.SPorHT();
                    list.add(cl);
                }
            }
            
            return list;
        } catch (ParseException ex ) {
            throw createParseException(ex.getMessage());
        } finally {
            if (debug) dbg_leave("ContentLanguageParser.parse");
        }
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Content-Language: fr \n",
            "Content-Language: fr , he \n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            ContentLanguageParser parser =
            new ContentLanguageParser(r[i]);
            ContentLanguageList e= (ContentLanguageList) parser.parse();
            System.out.println("encoded = " + e.encode());
        }    
    }
     */
    
}
