package gov.nist.javax.sip.parser;
import gov.nist.core.*;
import java.text.ParseException;
import java.util.Vector;

/**
 * Base parser class.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2005-11-21 23:25:35 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public abstract class Parser extends ParserCore implements TokenTypes {

	protected ParseException createParseException(String exceptionString) {
		return new ParseException(
			lexer.getBuffer() + ":" + exceptionString,
			lexer.getPtr());
	}

	protected Lexer getLexer() {
		return (Lexer) this.lexer;
	}

	protected String sipVersion() throws ParseException {
		if (debug)
			dbg_enter("sipVersion");
		try {
			Token tok = lexer.match(SIP);
			if (!tok.getTokenValue().equalsIgnoreCase("SIP"))
				createParseException("Expecting SIP");
			lexer.match('/');
			tok = lexer.match(ID);
			if (!tok.getTokenValue().equals("2.0"))
				createParseException("Expecting SIP/2.0");

			return "SIP/2.0";
		} finally {
			if (debug)
				dbg_leave("sipVersion");
		}
	}

	/**
	 * parses a method. Consumes if a valid method has been found.
	 */
	protected String method() throws ParseException {
		try {
			if (debug)
				dbg_enter("method");
			Vector tokens = this.lexer.peekNextToken(1);
			Token token = (Token) tokens.elementAt(0);
			if (token.getTokenType() == INVITE
				|| token.getTokenType() == ACK
				|| token.getTokenType() == OPTIONS
				|| token.getTokenType() == BYE
				|| token.getTokenType() == REGISTER
				|| token.getTokenType() == CANCEL
				|| token.getTokenType() == SUBSCRIBE
				|| token.getTokenType() == NOTIFY
				|| token.getTokenType() == ID) {
				lexer.consume();
				return token.getTokenValue();
			} else {
				throw createParseException("Invalid Method");
			}
		} finally {
			if (Debug.debug)
				dbg_leave("method");
		}
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2004/01/22 13:26:31  sverker
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
