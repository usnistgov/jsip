package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for AllowEvents header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov> 
 * @author M. Ranganathan <mranga@nist.gov>  
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class AllowEventsParser extends HeaderParser {

	/**
	 * Creates a new instance of AllowEventsParser
	 * @param allowEvents the header to parse 
	 */
	public AllowEventsParser(String allowEvents) {
		super(allowEvents);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected AllowEventsParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the AllowEvents String header
	 * @return SIPHeader (AllowEventsList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("AllowEventsParser.parse");
		AllowEventsList list = new AllowEventsList();

		try {
			headerName(TokenTypes.ALLOW_EVENTS);

			AllowEvents allowEvents = new AllowEvents();
			allowEvents.setHeaderName(SIPHeaderNames.ALLOW_EVENTS);

			this.lexer.SPorHT();
			this.lexer.match(TokenTypes.ID);
			Token token = lexer.getNextToken();
			allowEvents.setEventType(token.getTokenValue());

			list.add(allowEvents);
			this.lexer.SPorHT();
			while (lexer.lookAhead(0) == ',') {
				this.lexer.match(',');
				this.lexer.SPorHT();

				allowEvents = new AllowEvents();
				this.lexer.match(TokenTypes.ID);
				token = lexer.getNextToken();
				allowEvents.setEventType(token.getTokenValue());

				list.add(allowEvents);
				this.lexer.SPorHT();
			}
			this.lexer.SPorHT();
			this.lexer.match('\n');

			return list;
		} finally {
			if (debug)
				dbg_leave("AllowEventsParser.parse");
		}
	}

	/* 
	    public static void main(String args[]) throws ParseException {
	        String r[] = {
	            "Allow-Events: pack1.pack2, pack3 , pack4\n",
	            "Allow-Events: pack1\n"
	        };
	        
	        for (int i = 0; i < r.length; i++ ) {
	            AllowEventsParser parser =
	            new AllowEventsParser(r[i]);
	            AllowEventsList a= (AllowEventsList) parser.parse();
	            System.out.println("encoded = " + a.encode());
	        }    
	    }
	*/
}
/*
 * $Log: not supported by cvs2svn $
 */
