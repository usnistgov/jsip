package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.*;
import javax.sip.*;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;
/** Parser for Max Forwards Header.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
 * @version 1.0
 */
public class MaxForwardsParser extends HeaderParser {

       public  MaxForwardsParser(String contentLength) {
		super(contentLength);
       }

        protected MaxForwardsParser(Lexer lexer) {
		super(lexer);
	}
	
	public  SIPHeader  parse() throws ParseException {
	     if (debug) dbg_enter("MaxForwardsParser.enter");
             try {
		MaxForwards contentLength= new MaxForwards();
		headerName (TokenTypes.MAX_FORWARDS);
                String number=this.lexer.number();
                contentLength.setMaxForwards(Integer.parseInt(number));
                this.lexer.SPorHT();
		this.lexer.match('\n');
                return contentLength;
              } catch (InvalidArgumentException ex) {
		   throw createParseException(ex.getMessage());
              } catch (NumberFormatException ex) {
		   throw createParseException(ex.getMessage());
              }  finally {
			if (debug) dbg_leave("MaxForwardsParser.leave");
	      }
	}

/**
        public static void main(String args[]) throws ParseException {
		String content[] = {
			"Max-Forwards: 3495\n",
			"Max-Forwards: 0 \n"
                };
			
		for (int i = 0; i < content.length; i++ ) {
		    MaxForwardsParser cp = 
			  new MaxForwardsParser(content[i]);
		    MaxForwards c = (MaxForwards) cp.parse();
		    System.out.println("encoded = " + c.encode());
		}
			
	}
**/
	
       

}
