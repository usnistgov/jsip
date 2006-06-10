package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for Authentication-Info header.
 *
 * @version  JAIN-SIP-1.1 $Revision: 1.6 $ $Date: 2004-07-28 14:13:54 $
 *
 * @author Olivier Deruelle <deruelle@nist.gov> 
 * @author M. Ranganathan <mranga@nist.gov>  
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class AuthenticationInfoParser extends ParametersParser {

	/**
	 * Creates a new instance of AuthenticationInfoParser
	 * @param authenticationInfo the header to parse 
	 */
	public AuthenticationInfoParser(String authenticationInfo) {
		super(authenticationInfo);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected AuthenticationInfoParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the AuthenticationInfo String header
	 * @return SIPHeader (AuthenticationInfoList object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("AuthenticationInfoParser.parse");

		try {
			headerName(TokenTypes.AUTHENTICATION_INFO);

			AuthenticationInfo authenticationInfo = new AuthenticationInfo();
			authenticationInfo.setHeaderName(
				SIPHeaderNames.AUTHENTICATION_INFO);

			this.lexer.SPorHT();

			NameValue nv = super.nameValue();
			authenticationInfo.setParameter(nv);
			this.lexer.SPorHT();
			while (lexer.lookAhead(0) == ',') {
				this.lexer.match(',');
				this.lexer.SPorHT();

				nv = super.nameValue();
				authenticationInfo.setParameter(nv);
				this.lexer.SPorHT();
			}
			this.lexer.SPorHT();
			//this.lexer.match('\n');

			return authenticationInfo;
		} finally {
			if (debug)
				dbg_leave("AuthenticationInfoParser.parse");
		}
	}

	/*
	   
	**/
}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2004/01/22 13:26:31  sverker
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
