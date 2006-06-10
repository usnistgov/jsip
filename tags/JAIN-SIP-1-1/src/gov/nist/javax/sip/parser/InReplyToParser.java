package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for InReplyTo header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-08-10 21:35:44 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class InReplyToParser extends HeaderParser {

	/**
	 * Creates a new instance of InReplyToParser 
	 * @param inReplyTo the header to parse
	 */
	public InReplyToParser(String inReplyTo) {
		super(inReplyTo);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected InReplyToParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (InReplyToList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("InReplyToParser.parse");
		InReplyToList list = new InReplyToList();

		try {
			headerName(TokenTypes.IN_REPLY_TO);

			while (lexer.lookAhead(0) != '\n') {
				InReplyTo inReplyTo = new InReplyTo();
				inReplyTo.setHeaderName(SIPHeaderNames.IN_REPLY_TO);

				this.lexer.match(TokenTypes.ID);
				Token token = lexer.getNextToken();
				if (lexer.lookAhead(0) == '@') {
					this.lexer.match('@');
					this.lexer.match(TokenTypes.ID);
					Token secToken = lexer.getNextToken();
					inReplyTo.setCallId(
						token.getTokenValue() + "@" + secToken.getTokenValue());
				} else {
					inReplyTo.setCallId(token.getTokenValue());
				}

				this.lexer.SPorHT();

				list.add(inReplyTo);

				while (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();

					inReplyTo = new InReplyTo();

					this.lexer.match(TokenTypes.ID);
					token = lexer.getNextToken();
					if (lexer.lookAhead(0) == '@') {
						this.lexer.match('@');
						this.lexer.match(TokenTypes.ID);
						Token secToken = lexer.getNextToken();
						inReplyTo.setCallId(
							token.getTokenValue()
								+ "@"
								+ secToken.getTokenValue());
					} else {
						inReplyTo.setCallId(token.getTokenValue());
					}

					list.add(inReplyTo);
				}
			}

			return list;
		} finally {
			if (debug)
				dbg_leave("InReplyToParser.parse");
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
