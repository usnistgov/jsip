package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Parser for UserAgent header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.9 $ $Date: 2005-11-28 19:02:29 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class UserAgentParser extends HeaderParser {

	/**
	 * Constructor
	 * @param userAgent - UserAgent header to parse
	 */
	public UserAgentParser(String userAgent) {
		super(userAgent);
	}

	/**
	 * Constructor
	 * @param lexer - the lexer to use.
	 */
	protected UserAgentParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (UserAgent object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {
		if (debug)
			dbg_enter("UserAgentParser.parse");
		UserAgent userAgent = new UserAgent();
		try {
			headerName(TokenTypes.USER_AGENT);
			if (this.lexer.lookAhead(0) == '\n')
				throw createParseException("empty header");

			//  mandatory token: product[/product-version] | (comment)
			while (this.lexer.lookAhead(0) != '\n'
				&& this.lexer.lookAhead(0) != '\0') {
				if (this.lexer.lookAhead(0) == '(') {
					String comment = this.lexer.comment();
					userAgent.addProductToken('(' + comment + ')');
				} else {
					int marker = 0;
					String tok;
					try {
						marker = this.lexer.markInputPosition();
						tok = this.lexer.getString('/');
						if (tok.charAt(tok.length() - 1) == '\n')
							tok = tok.trim();
						userAgent.addProductToken(tok);
					} catch (ParseException ex) {
						this.lexer.rewindInputPosition(marker);
						tok = this.lexer.getRest().trim();
						userAgent.addProductToken(tok);
						break;
					}
				}
			}
		} finally {
			if (debug)
				dbg_leave("UserAgentParser.parse");
		}

		return userAgent;
	}

	
	public static void main(String args[]) throws ParseException {
	String userAgent[] = {
	        "User-Agent: Softphone/Beta1.5 \n",
	        "User-Agent: Nist/Beta1 (beta version) \n",
	        "User-Agent: Nist UA (beta version)\n",
	        "User-Agent: Nist1.0/Beta2 Ubi/vers.1.0 (very cool) \n"
	        };
		
	for (int i = 0; i < userAgent.length; i++ ) {
	    UserAgentParser parser = 
		  new UserAgentParser(userAgent[i]);
	    UserAgent ua= (UserAgent) parser.parse();
	    System.out.println("encoded = " + ua.encode());
	}
		
	}
	 
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.7  2004/01/30 17:10:47  mranga
 * Reviewed by:   mranga
 * Server and user agent parser leave an extra Linefeed at the end of token.
 *
 * Revision 1.6  2004/01/27 13:52:11  mranga
 * Reviewed by:   mranga
 * Fixed server/user-agent parser.
 * suppress sending ack to TU when retransFilter is enabled and ack is retransmitted.
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
