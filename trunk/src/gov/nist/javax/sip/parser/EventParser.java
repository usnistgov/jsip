package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for Event header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:31 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class EventParser extends ParametersParser {

	/**
	 * Creates a new instance of EventParser 
	 * @param event the header to parse 
	 */
	public EventParser(String event) {
		super(event);
	}

	/**
	 * Cosntructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected EventParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (Event object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("EventParser.parse");

		try {
			headerName(TokenTypes.EVENT);
			this.lexer.SPorHT();

			Event event = new Event();
			this.lexer.match(TokenTypes.ID);
			Token token = lexer.getNextToken();
			String value = token.getTokenValue();

			event.setEventType(value);
			super.parse(event);

			this.lexer.SPorHT();
			this.lexer.match('\n');

			return event;

		} catch (ParseException ex) {
			throw createParseException(ex.getMessage());
		} finally {
			if (debug)
				dbg_leave("EventParser.parse");
		}
	}

	/**
	    public static void main(String args[]) throws ParseException {
	        String r[] = {
	            "Event: presence\n",
	            "Event: foo; param=abcd; id=1234\n",
	            "Event: foo.foo1; param=abcd; id=1234\n"
	        };
	        
	        for (int i = 0; i < r.length; i++ ) {
	            EventParser parser =
	            new EventParser(r[i]);
	            Event e= (Event) parser.parse();
	            System.out.println("encoded = " + e.encode());
	            System.out.println("encoded = " + e.clone());
		    System.out.println(e.getEventId());
		    System.out.println(e.match(e));
	        }    
	    }
	**/
}
/*
 * $Log: not supported by cvs2svn $
 */
