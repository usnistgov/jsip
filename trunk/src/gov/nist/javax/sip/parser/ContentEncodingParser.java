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
public class ContentEncodingParser extends HeaderParser {
    
     /**
     * Creates a new instance of ContentEncodingParser
     * @param contentEncoding the header to parse 
     */
    public ContentEncodingParser(String contentEncoding) {
        super(contentEncoding);
    }
    
    /** Constructor
     * @param lexer the lexer to use to parse the header
     */
    protected  ContentEncodingParser(Lexer lexer) {
        super(lexer);
    }
    
    /** parse the ContentEncodingHeader String header
     * @return SIPHeader (ContentEncodingList object)
     * @throws SIPParseException if the message does not respect the spec.
     */
    public SIPHeader parse() throws ParseException {
        
        if (debug) dbg_enter("ContentEncodingParser.parse");
        ContentEncodingList list=new ContentEncodingList();
        
        try {
            headerName(TokenTypes.CONTENT_ENCODING);
            
            while (lexer.lookAhead(0) != '\n') {
                ContentEncoding cl= new ContentEncoding();
                cl.setHeaderName(SIPHeaderNames.CONTENT_ENCODING);
                
                this.lexer.SPorHT();
                this.lexer.match(TokenTypes.ID);
                
                Token token=lexer.getNextToken();
                cl.setEncoding(token.getTokenValue());
                
                this.lexer.SPorHT();
                list.add(cl);
                
                while (lexer.lookAhead(0) == ',') {
                    cl= new ContentEncoding();
                    this.lexer.match(',');
                    this.lexer.SPorHT();
                    this.lexer.match(TokenTypes.ID);
                    this.lexer.SPorHT();
                    token=lexer.getNextToken();
                    cl.setEncoding(token.getTokenValue());
                    this.lexer.SPorHT();
                    list.add(cl);
                }
            }
            
            return list;
        } catch (ParseException ex ) {
            throw createParseException(ex.getMessage());
        } finally {
            if (debug) dbg_leave("ContentEncodingParser.parse");
        }
    }
    
    /** Test program
    public static void main(String args[]) throws ParseException {
        String r[] = {
            "Content-Encoding: gzip \n",
            "Content-Encoding: gzip, tar \n"
        };
        
        for (int i = 0; i < r.length; i++ ) {
            ContentEncodingParser parser =
            new ContentEncodingParser(r[i]);
            ContentEncodingList e= (ContentEncodingList) parser.parse();
            System.out.println("encoded = " + e.encode());
        }    
    }
     */
    
}

