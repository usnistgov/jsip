package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for Event header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-08-10 21:35:43 $
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

	
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2004/01/22 13:26:31  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
