package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;
import javax.sip.*;
import java.util.*;

/** Parser for MimeVersion header.
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
public class MimeVersionParser extends HeaderParser {
    
        /** Creates a new instance of MimeVersionParser 
         * @param mimeVersion the header to parse
         */
        public MimeVersionParser(String mimeVersion) {
		super(mimeVersion);
	}

        /** Cosntructor
         * @param lexer the lexer to use to parse the header
         */
        protected MimeVersionParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message
         * @return SIPHeader (MimeVersion object)
         * @throws SIPParseException if the message does not respect the spec.
         */
        public SIPHeader parse() throws ParseException {
            
            if (debug) dbg_enter("MimeVersionParser.parse");
            MimeVersion mimeVersion=new MimeVersion();
            try {
                headerName(TokenTypes.MIME_VERSION);
                
                mimeVersion.setHeaderName(SIPHeaderNames.MIME_VERSION);
                
                
                try{
                    String majorVersion=this.lexer.number();
                    mimeVersion.setMajorVersion(Integer.parseInt(majorVersion));
                    this.lexer.match('.');
                    String minorVersion=this.lexer.number();
                    mimeVersion.setMinorVersion(Integer.parseInt(minorVersion));
                    
                    
                } 
                catch (InvalidArgumentException ex) {
                        throw createParseException(ex.getMessage());
                }
                this.lexer.SPorHT();
                
		this.lexer.match('\n');
                
                return mimeVersion;
            } 
            finally {
                if (debug) dbg_leave("MimeVersionParser.parse");
            }
        }
        
        /** Test program
        public static void main(String args[]) throws ParseException {
		String r[] = {
                "MIME-Version: 1.0 \n"
                };
			
		for (int i = 0; i < r.length; i++ ) {
		    MimeVersionParser parser = 
			  new MimeVersionParser(r[i]);
		    MimeVersion m= (MimeVersion) parser.parse();
		    System.out.println("encoded = " +m.encode());
		}		
	}
         */
        
}



