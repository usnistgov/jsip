package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for Allow header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  
 * @author M. Ranganathan <mranga@nist.gov> 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AllowParser extends HeaderParser {

	/**
	 * Creates a new instance of AllowParser
	 * @param allow the header to parse 
	 */
	public AllowParser(String allow) {
		super(allow);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected AllowParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the Allow String header
	 * @return SIPHeader (AllowList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("AllowParser.parse");
		AllowList list = new AllowList();

		try {
			headerName(TokenTypes.ALLOW);

			Allow allow = new Allow();
			allow.setHeaderName(SIPHeaderNames.ALLOW);

			this.lexer.SPorHT();
			this.lexer.match(TokenTypes.ID);
			Token token = lexer.getNextToken();
			allow.setMethod(token.getTokenValue());

			list.add(allow);
			this.lexer.SPorHT();
			while (lexer.lookAhead(0) == ',') {
				this.lexer.match(',');
				this.lexer.SPorHT();

				allow = new Allow();
				this.lexer.match(TokenTypes.ID);
				token = lexer.getNextToken();
				allow.setMethod(token.getTokenValue());

				list.add(allow);
				this.lexer.SPorHT();
			}
			this.lexer.SPorHT();
			this.lexer.match('\n');

			return list;
		} finally {
			if (debug)
				dbg_leave("AllowParser.parse");
		}
	}

	/* Test program
	    public static void main(String args[]) throws ParseException {
	        String r[] = {
	            "Allow: INVITE, ACK, OPTIONS, CANCEL, BYE\n",
	            "Allow: INVITE\n"
	        };
	        
	        for (int i = 0; i < r.length; i++ ) {
	            AllowParser parser =
	            new AllowParser(r[i]);
	            AllowList a= (AllowList) parser.parse();
	            System.out.println("encoded = " + a.encode());
	        }    
	    }
	*/
}
/*
 * $Log: not supported by cvs2svn $
 */
