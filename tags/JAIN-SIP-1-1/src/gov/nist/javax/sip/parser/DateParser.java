package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import java.util.*;
import java.text.ParseException;

/**
 * Parser for SIP Date field. Converts from SIP Date to the
 * internal storage (Calendar)
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-08-10 21:35:43 $
 */
public class DateParser extends HeaderParser {

	/**
	 * Constructor
	 * @param date message to parse to set
	 */
	public DateParser(String date) {
		super(date);
	}

	protected DateParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * Parse method.
	 * @throws ParseException
	 * @return  the parsed Date header/
	 */
	public SIPHeader parse() throws ParseException {
		if (debug)
			dbg_enter("DateParser.parse");
		try {
			headerName(TokenTypes.DATE);
			int w = wkday();
			lexer.match(',');
			lexer.match(' ');
			Calendar cal = date();
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
			if (debug)
				dbg_leave("DateParser.parse");

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
