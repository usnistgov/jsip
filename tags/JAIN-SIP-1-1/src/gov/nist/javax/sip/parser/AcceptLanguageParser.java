/*
 * AcceptLanguageParser.java
 *
 * Created on June 10, 2002, 3:31 PM
 */

package gov.nist.javax.sip.parser;
import gov.nist.core.*;
import gov.nist.javax.sip.header.*;
import javax.sip.*;
import java.text.ParseException;


/**
 * Parser for Accept Language Headers.
 *
 * Accept Language body. 
 * <pre>
 *
 * Accept-Language = "Accept-Language" ":"
 *                         1#( language-range [ ";" "q" "=" qvalue ] )
 *       language-range  = ( ( 1*8ALPHA *( "-" 1*8ALPHA ) ) | "*" )  
 *
 * HTTP RFC 2616 Section 14.4
 * </pre>
 *
 *  Accept-Language: da, en-gb;q=0.8, en;q=0.7
 *
 * @see AcceptLanguageList
 * @version  JAIN-SIP-1.1 $Revision: 1.5 $ $Date: 2004-07-28 14:13:54 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov> 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AcceptLanguageParser extends HeaderParser {

	/**
	 * Constructor
	 * @param acceptLanguage AcceptLanguage message to parse
	 */
	public AcceptLanguageParser(String acceptLanguage) {
		super(acceptLanguage);
	}

	/**
	 * Constructor
	 * @param lexer Lexer to set
	 */
	protected AcceptLanguageParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (AcceptLanguage object)
	 * @throws ParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {
		AcceptLanguageList acceptLanguageList = new AcceptLanguageList();
		if (debug)
			dbg_enter("AcceptLanguageParser.parse");

		try {
			headerName(TokenTypes.ACCEPT_LANGUAGE);

			while (lexer.lookAhead(0) != '\n') {
				AcceptLanguage acceptLanguage = new AcceptLanguage();
				acceptLanguage.setHeaderName(SIPHeaderNames.ACCEPT_LANGUAGE);
				if (lexer.lookAhead(0) != ';') {
					// Content-Coding:
					lexer.match(TokenTypes.ID);
					Token value = lexer.getNextToken();
					acceptLanguage.setLanguageRange(value.getTokenValue());
				}

				while (lexer.lookAhead(0) == ';') {
					this.lexer.match(';');
					this.lexer.SPorHT();
					this.lexer.match('q');
					this.lexer.SPorHT();
					this.lexer.match('=');
					this.lexer.SPorHT();
					lexer.match(TokenTypes.ID);
					Token value = lexer.getNextToken();
					try {
						float fl = Float.parseFloat(value.getTokenValue());
						acceptLanguage.setQValue(fl);
					} catch (NumberFormatException ex) {
						throw createParseException(ex.getMessage());
					} catch (InvalidArgumentException ex) {
						throw createParseException(ex.getMessage());
					}
					this.lexer.SPorHT();
				}

				acceptLanguageList.add(acceptLanguage);
				if (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();
				} else
					this.lexer.SPorHT();

			}
		} finally {
			if (debug)
				dbg_leave("AcceptLanguageParser.parse");
		}

		return acceptLanguageList;
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
