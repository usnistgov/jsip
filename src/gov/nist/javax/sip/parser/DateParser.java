package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import java.util.*;
import java.text.ParseException;

/** Parser for SIP Date field. Converts from SIP Date to the
 * internal storage (Calendar)
 */
public class DateParser extends HeaderParser {

        /** Constructor
         * @param String route message to parse to set
         */
        public DateParser (String date) {
		super(date);
	}

        protected DateParser(Lexer lexer) {
		super(lexer);
	}

        

	/** Parse method.
         * @throws ParseException
         * @return  the parsed Date header/
         */
	public SIPHeader parse()  throws ParseException {
	   if (debug) dbg_enter("DateParser.parse");
            try {
                headerName(TokenTypes.DATE);
                int w = wkday();
                lexer.match(',');
                lexer.match(' ');
                Calendar cal =date();
                lexer.match(' ');
                time(cal);
                lexer.match(' ');
                String tzone = this.lexer.ttoken().toLowerCase();
                if (!"gmt".equals(tzone)) 
                        throw createParseException("Bad Time Zone " + tzone);
		this.lexer.match('\n');
                SIPDateHeader retval = new SIPDateHeader();
                retval.setDate(cal);
                return retval;
            } finally {
		if (debug) dbg_leave("DateParser.parse");
                
            }
            
        }
        
/**
        public static void main(String args[]) throws ParseException {
		String date[] = {
			"Date: Sun, 07 Jan 2001 19:05:06 GMT\n",
			"Date: Mon, 08 Jan 2001 19:05:06 GMT\n" };
			
		for (int i = 0; i < date.length; i++ ) {
		    System.out.println("Parsing " + date[i]);
		    DateParser dp = 
			  new DateParser(date[i]);
		    SIPDateHeader d = (SIPDateHeader) dp.parse();
		    System.out.println("encoded = " +d.encode());
		}
			
	}
**/
        

}
