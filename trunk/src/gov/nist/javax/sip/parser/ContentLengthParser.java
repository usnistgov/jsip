package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.*;
import gov.nist.core.*;
import javax.sip.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
/** Parser for Content-Length Header.
*@version  JAIN-SIP-1.1
*
*@author Olivier Deruelle  <br/>
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ContentLengthParser extends HeaderParser {

       public  ContentLengthParser(String contentLength) {
		super(contentLength);
       }

        protected ContentLengthParser(Lexer lexer) {
		super(lexer);
	}
	
	public  SIPHeader  parse() throws ParseException {
	     if (debug) dbg_enter("ContentLengthParser.enter");
             try {
		ContentLength contentLength= new ContentLength();
		headerName (TokenTypes.CONTENT_LENGTH);
                String number=this.lexer.number();
                contentLength.setContentLength(Integer.parseInt(number));
                this.lexer.SPorHT();
		this.lexer.match('\n');
                return contentLength;
              } catch (InvalidArgumentException ex) {
		   throw createParseException(ex.getMessage());
              } catch (NumberFormatException ex) {
		   throw createParseException(ex.getMessage());
              }  finally {
			if (debug) dbg_leave("ContentLengthParser.leave");
	      }
	}

/**
        public static void main(String args[]) throws ParseException {
		String content[] = {
			"l: 345\n",
			"Content-Length: 3495\n",
			"Content-Length: 0 \n"
                };
			
		for (int i = 0; i < content.length; i++ ) {
		    ContentLengthParser cp = 
			  new ContentLengthParser(content[i]);
		    ContentLength c = (ContentLength) cp.parse();
		    System.out.println("encoded = " + c.encode());
		}
			
	}
**/
	
       

}
