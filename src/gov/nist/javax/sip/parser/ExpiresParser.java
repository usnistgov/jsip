package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;
import javax.sip.*;

/**
 * Parser for SIP Expires Parser. Converts from SIP Date to the
 * internal storage (Calendar).
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-08-10 21:35:44 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ExpiresParser extends HeaderParser {

	/**
	 * protected constructor.
	 * @param text is the text of the header to parse
	 */
	public ExpiresParser(String text) {
		super(text);
	}

	/**
	 * constructor.
	 * @param lexer is the lexer passed in from the enclosing parser.
	 */
	protected ExpiresParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * Parse the header.
	 */
	public SIPHeader parse() throws ParseException {
		Expires expires = new Expires();
		if (debug)
			dbg_enter("parse");
		try {
			lexer.match(TokenTypes.EXPIRES);
			lexer.SPorHT();
			lexer.match(':');
			lexer.SPorHT();
			String nextId = lexer.getNextId();
			lexer.match('\n');
			try {
				int delta = Integer.parseInt(nextId);
				expires.setExpires(delta);
				return expires;
			} catch (NumberFormatException ex) {
				throw createParseException("bad integer format");
			} catch (InvalidArgumentException ex) {
				throw createParseException(ex.getMessage());
			}
		} finally {
			if (debug)
				dbg_leave("parse");
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
