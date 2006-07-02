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
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;

import java.text.ParseException;
import java.util.Vector;

/**
 * Parser For SIP and Tel URLs. Other kinds of URL's are handled by the 
 * J2SE 1.4 URL class.
 * @version 1.2 $Revision: 1.11 $ $Date: 2006-07-02 09:51:19 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class URLParser extends Parser {

	public URLParser(String url) {
		this.lexer = new Lexer("sip_urlLexer", url);
	}

	URLParser(Lexer lexer) {
		this.lexer = lexer;
		this.lexer.selectLexer("sip_urlLexer");
	}
	protected static boolean isMark(char next) {
		return next == '-'
			|| next == '_'
			|| next == '.'
			|| next == '!'
			|| next == '~'
			|| next == '*'
			|| next == '\''
			|| next == '('
			|| next == ')';
	}

	protected static boolean isUnreserved(char next) {
		return Lexer.isAlpha(next) || Lexer.isDigit(next) || isMark(next);
	}

	protected static boolean isReservedNoSlash(char next) {
		return next == ';'
			|| next == '?'
			|| next == ':'
			|| next == '@'
			|| next == '&'
			|| next == '+'
			|| next == '$'
			|| next == ',';

	}

	// Missing '=' bug in character set - discovered by interop testing 
	// at SIPIT 13 by Bob Johnson and Scott Holben.
	// change . to ; by Bruno Konik
	protected static boolean isUserUnreserved(char la) {
		return la == '&'
			|| la == '?'
			|| la == '+'
			|| la == '$'
			|| la == '#'
			|| la == '/'
			|| la == ','
			|| la == ';'
			|| la == '=';
	}

	protected String unreserved() throws ParseException {
		char next = lexer.lookAhead(0);
		if (isUnreserved(next)) {
			lexer.consume(1);
			return new StringBuffer().append(next).toString();
		} else
			throw createParseException("unreserved");

	}

	/** Name or value of a parameter.
	 */
	protected String paramNameOrValue() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (lexer.hasMoreChars()) {
			char next = lexer.lookAhead(0);
			if (next == '['
				|| next == ']'	// JvB: fixed this one
				|| next == '/'
				|| next == ':'
				|| next == '&'
				|| next == '+'
				|| next == '$'
				|| isUnreserved(next)) {
				retval.append(next);
				lexer.consume(1);
			} else if (isEscaped()) {
				String esc = lexer.charAsString(3);
				lexer.consume(3);
				retval.append(esc);
			} else
				break;
		}
		return retval.toString();
	}

	private NameValue uriParam() throws ParseException {
		if (debug)
			dbg_enter("uriParam");
		try {
			String pvalue = null;
			String pname = paramNameOrValue();
			char next = lexer.lookAhead(0);
			if (next == '=') {
				lexer.consume(1);
				pvalue = paramNameOrValue();
			}
			if (pname.toString().equals("") &&
			    ( pvalue == null || 
			    pvalue.toString().equals("") ))
			    return null;
			else return new NameValue(pname, pvalue);
		} finally {
			if (debug)
				dbg_leave("uriParam");
		}
	}

	protected static boolean isReserved(char next) {
		return next == ';'
			|| next == '/'
			|| next == '?'
			|| next == ':'
			|| next == '=' // Bug fix by Bruno Konik
			|| next == '@'
			|| next == '&'
			|| next == '+'
			|| next == '$'
			|| next == ',';
	}

	protected String reserved() throws ParseException {
		char next = lexer.lookAhead(0);
		if (isReserved(next)) {
			lexer.consume(1);
			return new StringBuffer().append(next).toString();
		} else
			throw createParseException("reserved");
	}

	protected boolean isEscaped() {
		try {
			char next = lexer.lookAhead(0);
			char next1 = lexer.lookAhead(1);
			char next2 = lexer.lookAhead(2);
			return (
				next == '%'
					&& Lexer.isHexDigit(next1)
					&& Lexer.isHexDigit(next2));
		} catch (Exception ex) {
			return false;
		}
	}

	protected String escaped() throws ParseException {
		if (debug)
			dbg_enter("escaped");
		try {
			StringBuffer retval = new StringBuffer();
			char next = lexer.lookAhead(0);
			char next1 = lexer.lookAhead(1);
			char next2 = lexer.lookAhead(2);
			if (next == '%'
				&& Lexer.isHexDigit(next1)
				&& Lexer.isHexDigit(next2)) {
				lexer.consume(3);
				retval.append(next);
				retval.append(next1);
				retval.append(next2);
			} else
				throw createParseException("escaped");
			return retval.toString();
		} finally {
			if (debug)
				dbg_leave("escaped");
		}
	}

	protected String mark() throws ParseException {
		if (debug)
			dbg_enter("mark");
		try {
			char next = lexer.lookAhead(0);
			if (isMark(next)) {
				lexer.consume(1);
				return new String( new char[]{next} );
			} else
				throw createParseException("mark");
		} finally {
			if (debug)
				dbg_leave("mark");
		}
	}

	protected String uric() {
		if (debug)
			dbg_enter("uric");
		try {
			try {
				char la = lexer.lookAhead(0);
				if (isUnreserved(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else if (isReserved(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else if (isEscaped()) {
					String retval = lexer.charAsString(3);
					lexer.consume(3);
					return retval;
				} else
					return null;
			} catch (Exception ex) {
				return null;
			}
		} finally {
			if (debug)
				dbg_leave("uric");
		}

	}

	protected String uricNoSlash() {
		if (debug)
			dbg_enter("uricNoSlash");
		try {
			try {
				char la = lexer.lookAhead(0);
				if (isEscaped()) {
					String retval = lexer.charAsString(3);
					lexer.consume(3);
					return retval;
				} else if (isUnreserved(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else if (isReservedNoSlash(la)) {
					lexer.consume(1);
					return Lexer.charAsString(la);
				} else
					return null;
			} catch (ParseException ex) {
				return null;
			}
		} finally {
			if (debug)
				dbg_leave("uricNoSlash");
		}
	}

	protected String uricString() {
		StringBuffer retval = new StringBuffer();
		while (true) {
			String next = uric();
			if (next == null)
				break;
			retval.append(next);
		}
		return retval.toString();
	}

	/**
	 * Parse and return a structure for a generic URL.
	 * Note that non SIP URLs are just stored as a string (not parsed).
	 * @return URI is a URL structure for a SIP url.
	 * @throws ParsException if there was a problem parsing.
	 */
	public GenericURI uriReference() throws ParseException {
		if (debug)
			dbg_enter("uriReference");
		GenericURI retval = null;
		Vector vect = lexer.peekNextToken(2);
		Token t1 = (Token) vect.elementAt(0);
		Token t2 = (Token) vect.elementAt(1);
		try {

			// System.out.println("token = "  + t1.getTokenValue());
			// System.out.println("tokenval = " + t1.getTokenType());

			if (t1.getTokenType() == TokenTypes.SIP) {
				if (t2.getTokenType() == ':')
					retval = sipURL();
				else
					throw createParseException("Expecting \':\'");
			} else if (t1.getTokenType() == TokenTypes.TEL) {
				if (t2.getTokenType() == ':') {
					retval = telURL();
				} else
					throw createParseException("Expecting \':\'");
			} else {
				String urlString = uricString();
				try {
					retval = new GenericURI(urlString);
				} catch (ParseException ex) {
					throw createParseException(ex.getMessage());
				}
			}
		} finally {
			if (debug)
				dbg_leave("uriReference");
		}
		return retval;
	}

	/**
	 * Parser for the base phone number.
	 */
	private String base_phone_number() throws ParseException {
		StringBuffer s = new StringBuffer();

		if (debug)
			dbg_enter("base_phone_number");
		try {
			int lc = 0;
			while (lexer.hasMoreChars()) {
				char w = lexer.lookAhead(0);
				if (Lexer.isDigit(w)
					|| w == '-'
					|| w == '.'
					|| w == '('
					|| w == ')') {
					lexer.consume(1);
					s.append(w);
					lc++;
				} else if (lc > 0)
					break;
				else
					throw createParseException("unexpected " + w);
			}
			return s.toString();
		} finally {
			if (debug)
				dbg_leave("base_phone_number");
		}

	}

	/**
	 * Parser for the local phone #.
	 */
	private String local_number() throws ParseException {
		StringBuffer s = new StringBuffer();
		if (debug)
			dbg_enter("local_number");
		try {
			int lc = 0;
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				if (la == '*'
					|| la == '#'
					|| la == '-'
					|| la == '.'
					|| la == '('
					|| la == ')'
						// JvB: allow 'A'..'F', should be uppercase
					|| Lexer.isHexDigit(la)) {	
					lexer.consume(1);
					s.append(la);
					lc++;
				} else if (lc > 0)
					break;
				else
					throw createParseException("unexepcted " + la);
			}
			return s.toString();
		} finally {
			if (debug)
				dbg_leave("local_number");
		}

	}

	/**
	 * Parser for telephone subscriber.
	 *
	 * @return the parsed telephone number.
	 */
	public final TelephoneNumber parseTelephoneNumber() throws ParseException {
		TelephoneNumber tn;

		if (debug)
			dbg_enter("telephone_subscriber");
		lexer.selectLexer("charLexer");
		try {
			char c = lexer.lookAhead(0);
			if (c == '+')
				tn = global_phone_number();
			else if (
				Lexer.isHexDigit(c)	// see RFC3966
					|| c == '#'	
					|| c == '*'
					|| c == '-'						
					|| c == '.'
					|| c == '('
					|| c == ')' ) {
				tn = local_phone_number();
			} else
				throw createParseException("unexpected char " + c);
			return tn;
		} finally {
			if (debug)
				dbg_leave("telephone_subscriber");
		}

	}

	private final TelephoneNumber global_phone_number() throws ParseException {
		if (debug)
			dbg_enter("global_phone_number");
		try {
			TelephoneNumber tn = new TelephoneNumber();
			tn.setGlobal(true);
			NameValueList nv = null;
			this.lexer.match(PLUS);
			String b = base_phone_number();
			tn.setPhoneNumber(b);
			if (lexer.hasMoreChars()) {
				char tok = lexer.lookAhead(0);
				if (tok == ';') {
					this.lexer.consume(1);
					nv = tel_parameters();
					tn.setParameters(nv);
				}
			}
			return tn;
		} finally {
			if (debug)
				dbg_leave("global_phone_number");
		}
	}

	private TelephoneNumber local_phone_number() throws ParseException {
		if (debug)
			dbg_enter("local_phone_number");
		TelephoneNumber tn = new TelephoneNumber();
		tn.setGlobal(false);
		NameValueList nv = null;
		String b = null;
		try {
			b = local_number();
			tn.setPhoneNumber(b);
			if (lexer.hasMoreChars()) {
				Token tok = this.lexer.peekNextToken();
				switch (tok.getTokenType()) {
					case SEMICOLON :
						{
							this.lexer.consume(1);
							nv = tel_parameters();
							tn.setParameters(nv);
							break;
						}
					default :
						{
							break;
						}
				}
			}
		} finally {
			if (debug)
				dbg_leave("local_phone_number");
		}
		return tn;
	}

	private NameValueList tel_parameters() throws ParseException {
		NameValueList nvList = new NameValueList();
		
		// JvB: Need to handle 'phone-context' specially
		// 'isub' (or 'ext') MUST appear first, but we accept any order here
		NameValue nv;
		while ( true ) {
			String pname = paramNameOrValue();
			
			// Handle 'phone-context' specially, it may start with '+'
			if ( pname.equalsIgnoreCase("phone-context")) {
				nv = phone_context();
			} else {
				if (lexer.lookAhead(0) == '=') {
					lexer.match('=');
					String value = paramNameOrValue();
					nv = new NameValue( pname, value );
				} else {
					nv = new NameValue( pname, "" );	// flag param
				}
			}
			nvList.add( nv );
			
			if ( lexer.lookAhead(0) == ';' ) {
				lexer.match(';');
			} else {
				return nvList;
			}
		}
		
	}

	/**
	 * Parses the 'phone-context' parameter in tel: URLs
	 * @throws ParseException
	 */
	private NameValue phone_context() throws ParseException {
		lexer.match('=');
	
		char la = lexer.lookAhead(0);
		Object value;
		if (la=='+') {	// global-number-digits
			lexer.consume(1);	// skip '+'
			value = "+" + base_phone_number();			
		} else if ( Lexer.isAlpha(la) || Lexer.isDigit(la) ) {
			Token t = lexer.match( Lexer.ID );	// more broad than allowed
			value = t.getTokenValue();
		} else {
			throw new ParseException( "Invalid phone-context:" + la , -1 );
		}
		return new NameValue( "phone-context", value );
	}
	
	/**
	 * Parse and return a structure for a Tel URL.
	 * @return a parsed tel url structure.
	 */
	public TelURLImpl telURL() throws ParseException {
		lexer.match(TokenTypes.TEL);
		lexer.match(':');
		TelephoneNumber tn = this.parseTelephoneNumber();
		TelURLImpl telUrl = new TelURLImpl();
		telUrl.setTelephoneNumber(tn);
		return telUrl;

	}

	/**
	 * Parse and return a structure for a SIP URL.
	 * @return a URL structure for a SIP url.
	 * @throws ParsException if there was a problem parsing.
	 */
	public SipUri sipURL() throws ParseException {
		if (debug)
			dbg_enter("sipURL");
		SipUri retval = new SipUri();
		try {
			lexer.match(TokenTypes.SIP);
			lexer.match(':');
			retval.setScheme(TokenNames.SIP);
			int m = lexer.markInputPosition();
			try {
				String user = user();
				char la;
				la = lexer.lookAhead(0);
				// name:password@hostPort
				lexer.match(':');
				String password = password();
				lexer.match('@');
				HostNameParser hnp = new HostNameParser(this.getLexer());
				HostPort hp = hnp.hostPort();
				retval.setUser(user);
				retval.setUserPassword(password);
				retval.setHostPort(hp);
			} catch (ParseException ex) {
				// name@hostPort
				try {
					lexer.rewindInputPosition(m);
					String user = user();
					lexer.match('@');
					HostNameParser hnp = new HostNameParser(this.getLexer());
					HostPort hp = hnp.hostPort();
					retval.setUser(user);
					retval.setHostPort(hp);
				} catch (ParseException e) {
					// hostPort
					lexer.rewindInputPosition(m);
					HostNameParser hnp = new HostNameParser(this.getLexer());
					HostPort hp = hnp.hostPort();
					retval.setHostPort(hp);
				}
			}
			lexer.selectLexer("charLexer");
			while (lexer.hasMoreChars()) {
				if (lexer.lookAhead(0) != ';')
					break;
				lexer.consume(1);
				NameValue parms = uriParam();
				if (parms != null) retval.setUriParameter(parms);
			}

			if (lexer.hasMoreChars() && lexer.lookAhead(0) == '?') {
				lexer.consume(1);
				while (lexer.hasMoreChars()) {
					NameValue parms = qheader();
					retval.setQHeader(parms);
					if (lexer.hasMoreChars() && lexer.lookAhead(0) != '&')
						break;
					else
						lexer.consume(1);
				}
			}
			return retval;
		} finally {
			if (debug)
				dbg_leave("sipURL");
		}
	}

	public String peekScheme() throws ParseException {
		Vector tokens = lexer.peekNextToken(1);
		if (tokens.size() == 0)
			return null;
		String scheme = ((Token) tokens.elementAt(0)).getTokenValue();
		return scheme;
	}

	/**
	 * Get a name value for a given query header (ie one that comes
	 * after the ?).
	 */
	protected NameValue qheader() throws ParseException {
		String name = lexer.getNextToken('=');
		lexer.consume(1);
		String value = hvalue();
		return new NameValue(name, value);

	}

	protected String hvalue() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (lexer.hasMoreChars()) {
			char la = lexer.lookAhead(0);
			// Look for a character that can terminate a URL.
			if (la == '+'
				|| la == '?'
				|| la == ':'
				|| la == '['
				|| la == ']'
				|| la == '/'
				|| la == '$'
				|| la == '_'
				|| la == '-'
				|| la == '"'
				|| la == '!'
				|| la == '~'
				|| la == '*'
				|| la == '.'
				|| la == '('
				|| la == ')'
				|| Lexer.isAlpha(la)
				|| Lexer.isDigit(la)) {
				lexer.consume(1);
				retval.append(la);
			} else if (la == '%') {
				retval.append(escaped());
			} else
				break;
		}
		return retval.toString();
	}

	/** 
	 * Scan forward until you hit a terminating character for a URL.
	 * We do not handle non sip urls in this implementation.
	 * @return the string that takes us to the end of this URL (i.e. to
	 * the next delimiter).
	 */
	protected String urlString() throws ParseException {
		StringBuffer retval = new StringBuffer();
		lexer.selectLexer("charLexer");

		while (lexer.hasMoreChars()) {
			char la = lexer.lookAhead(0);
			// Look for a character that can terminate a URL.
			if (la == ' '
				|| la == '\t'
				|| la == '\n'
				|| la == '>'
				|| la == '<')
				break;
			lexer.consume(0);
			retval.append(la);
		}
		return retval.toString();
	}

	protected String user() throws ParseException {
		if (debug)
			dbg_enter("user");
		try {
			StringBuffer retval = new StringBuffer();
			while (lexer.hasMoreChars()) {
				char la = lexer.lookAhead(0);
				//if (la == '=') break;
				if (isUnreserved(la) || isUserUnreserved(la)) {
					retval.append(la);
					lexer.consume(1);
				} else if (isEscaped()) {
					String esc = lexer.charAsString(3);
					lexer.consume(3);
					retval.append(esc);
				} else
					break;
			}
			return retval.toString();
		} finally {
			if (debug)
				dbg_leave("user");
		}

	}

	protected String password() throws ParseException {
		StringBuffer retval = new StringBuffer();
		while (true) {
			char la = lexer.lookAhead(0);
			if (isUnreserved(la)
				|| la == '&'
				|| la == '='
				|| la == '+'
				|| la == '$'
				|| la == ',') {
				retval.append(la);
				lexer.consume(1);
			} else if (isEscaped()) {
				String esc = lexer.charAsString(3);
				retval.append(esc);
                                lexer.consume(3); // bug reported by
                                                  // Jeff Haynie
			} else
				break;

		}
		return retval.toString();

	}

	/**
	 * Default parse method. This method just calls uriReference.
	 */
	public GenericURI parse() throws ParseException {
		return uriReference();
	}

	/**
	
	**/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.9  2006/06/19 06:47:27  mranga
 * javadoc fixups
 *
 * Revision 1.8  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.7  2006/05/17 05:20:18  jeroen
 * fixed phone-context string value being returned
 *
 * Revision 1.6  2006/05/15 19:31:11  jeroen
 * fixed tel: URI case, characters are only allowed in %-encoded form
 *
 * Revision 1.5  2006/05/14 21:30:27  jeroen
 * more fixed tel: URL parsing, in line with RFC3966
 *
 * Revision 1.3  2006/05/14 20:20:32  jeroen
 * fix for local phone numbers
 *
 * Revision 1.2  2005/10/22 23:53:35  jeroen
 * fixed nasty little bug in URI parser: typo in bracket ('[' instead of ']')
 *
 * Revision 1.1.1.1  2005/10/04 17:12:36  mranga
 *
 * Import
 *
 *
 * Revision 1.9  2005/07/14 14:53:58  mranga
 * Submitted by:  mranga
 * Reviewed by:   mranga
 *
 * Applied Jeff Heynie's bug fix for URL parser.
 *
 * Revision 1.8  2004/07/28 14:13:55  mranga
 * Submitted by:  mranga
 *
 * Move out the test code to a separate test/unit class.
 * Fixed some encode methods.
 *
 * Revision 1.7  2004/04/22 22:51:18  mranga
 * Submitted by:  Thomas Froment
 * Reviewed by:   mranga
 *
 * Fixed corner cases.
 *
 * Revision 1.6  2004/02/06 20:15:55  mranga
 * Submitted by:  Bruno Konik
 * Reviewed by:   mranga
 * Fixed character sets for parsing of urls and methods.
 *
 * Revision 1.5  2004/01/22 13:26:32  sverker
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
