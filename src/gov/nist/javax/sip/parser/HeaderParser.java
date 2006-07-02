/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.util.*;
import java.text.ParseException;

/**
 * Generic header parser class. The parsers for various headers extend this
 * class. To create a parser for a new header, extend this class and change
 * the createParser class.
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006-07-02 09:51:03 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class HeaderParser extends Parser {

	/**
	 * Parse the weekday field
	 * @return an integer with the calendar content for wkday.
	 */
	protected int wkday() throws ParseException {
		dbg_enter("wkday");
		try {
			String tok = lexer.ttoken();
			String id = tok.toLowerCase();

			if (TokenNames.MON.equalsIgnoreCase(id))
				return Calendar.MONDAY;
			else if (TokenNames.TUE.equalsIgnoreCase(id))
				return Calendar.TUESDAY;
			else if (TokenNames.WED.equalsIgnoreCase(id))
				return Calendar.WEDNESDAY;
			else if (TokenNames.THU.equalsIgnoreCase(id))
				return Calendar.THURSDAY;
			else if (TokenNames.FRI.equalsIgnoreCase(id))
				return Calendar.FRIDAY;
			else if (TokenNames.SAT.equalsIgnoreCase(id))
				return Calendar.SATURDAY;
			else if (TokenNames.SUN.equalsIgnoreCase(id))
				return Calendar.SUNDAY;
			else
				throw createParseException("bad wkday");
		} finally {
			dbg_leave("wkday");
		}

	}

	/**
	 * parse and return a date field.
	 * @return a date structure with the parsed value.
	 */
	protected Calendar date() throws ParseException {
		try {
			Calendar retval = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			String s1 = lexer.number();
			int day = Integer.parseInt(s1);
			if (day <= 0 || day >= 31)
				throw createParseException("Bad day ");
			retval.set(Calendar.DAY_OF_MONTH, day);
			lexer.match(' ');
			String month = lexer.ttoken().toLowerCase();
			if (month.equals("jan")) {
				retval.set(Calendar.MONTH, Calendar.JANUARY);
			} else if (month.equals("feb")) {
				retval.set(Calendar.MONTH, Calendar.FEBRUARY);
			} else if (month.equals("mar")) {
				retval.set(Calendar.MONTH, Calendar.MARCH);
			} else if (month.equals("apr")) {
				retval.set(Calendar.MONTH, Calendar.APRIL);
			} else if (month.equals("may")) {
				retval.set(Calendar.MONTH, Calendar.MAY);
			} else if (month.equals("jun")) {
				retval.set(Calendar.MONTH, Calendar.JUNE);
			} else if (month.equals("jul")) {
				retval.set(Calendar.MONTH, Calendar.JULY);
			} else if (month.equals("aug")) {
				retval.set(Calendar.MONTH, Calendar.AUGUST);
			} else if (month.equals("sep")) {
				retval.set(Calendar.MONTH, Calendar.SEPTEMBER);
			} else if (month.equals("oct")) {
				retval.set(Calendar.MONTH, Calendar.OCTOBER);
			} else if (month.equals("nov")) {
				retval.set(Calendar.MONTH, Calendar.NOVEMBER);
			} else if (month.equals("dec")) {
				retval.set(Calendar.MONTH, Calendar.DECEMBER);
			}
			lexer.match(' ');
			String s2 = lexer.number();
			int yr = Integer.parseInt(s2);
			retval.set(Calendar.YEAR, yr);
			return retval;

		} catch (Exception ex) {
			throw createParseException("bad date field");
		}

	}

	/**
	 * Set the time field. This has the format hour:minute:second
	 */
	protected void time(Calendar calendar) throws ParseException {
		try {
			String s = lexer.number();
			int hour = Integer.parseInt(s);
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			lexer.match(':');
			s = lexer.number();
			int min = Integer.parseInt(s);
			calendar.set(Calendar.MINUTE, min);
			lexer.match(':');
			s = lexer.number();
			int sec = Integer.parseInt(s);
			calendar.set(Calendar.SECOND, sec);
		} catch (Exception ex) {
			throw createParseException("error processing time ");

		}

	}

	/**
	 * Creates new HeaderParser
	 * @param String to parse.
	 */
	protected HeaderParser(String header) {
		this.lexer = new Lexer("command_keywordLexer", header);
	}

	protected HeaderParser(Lexer lexer) {
		this.lexer = lexer;
		this.lexer.selectLexer("command_keywordLexer");
	}

	/**
	 * Parse the SIP header from the buffer and return a parsed
	 * structure.
	 * @throws ParseException if there was an error parsing.
	 */
	public SIPHeader parse() throws ParseException {
		String name = lexer.getNextToken(':');
		lexer.consume(1);
		String body = lexer.getLine().trim();
		// we dont set any fields because the header is
		// ok
		ExtensionHeaderImpl retval = new ExtensionHeaderImpl(name);
		retval.setValue(body);
		return retval;

	}

	/**
	 * Parse the header name until the colon  and chew WS after that.
	 */
	protected void headerName(int tok) throws ParseException {
		this.lexer.match(tok);
		this.lexer.SPorHT();
		this.lexer.match(':');
		this.lexer.SPorHT();
	}
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:35  mranga
 *
 * Import
 *
 *
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
