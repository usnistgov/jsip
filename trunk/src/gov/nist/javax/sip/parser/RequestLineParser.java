package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.address.*;
import java.text.ParseException;
import gov.nist.javax.sip.header.*;

/**
 * Parser for the SIP request line.
 *
 * @version  JAIN-SIP-1.1
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
class RequestLineParser extends Parser {
	public RequestLineParser(String requestLine) {
		this.lexer = new Lexer("method_keywordLexer", requestLine);
	}
	public RequestLineParser(Lexer lexer) {
		this.lexer = lexer;
		this.lexer.selectLexer("method_keywordLexer");
	}

	public RequestLine parse() throws ParseException {
		if (debug)
			dbg_enter("parse");
		try {
			RequestLine retval = new RequestLine();
			String m = method();
			lexer.SPorHT();
			retval.setMethod(m);
			this.lexer.selectLexer("sip_urlLexer");
			URLParser urlParser = new URLParser(this.getLexer());
			GenericURI url = urlParser.uriReference();
			lexer.SPorHT();
			retval.setUri(url);
			this.lexer.selectLexer("request_lineLexer");
			String v = sipVersion();
			retval.setSipVersion(v);
			lexer.SPorHT();
			lexer.match('\n');
			return retval;
		} finally {
			if (debug)
				dbg_leave("parse");
		}
	}

			public static void main(String args[]) throws ParseException {
			String requestLines[] = {
				"REGISTER sip:192.168.0.68 SIP/2.0\n",
				"REGISTER sip:company.com SIP/2.0\n",
				"INVITE sip:3660@166.35.231.140 SIP/2.0\n",
				"INVITE sip:user@company.com SIP/2.0\n",
				"OPTIONS sip:135.180.130.133 SIP/2.0\n" };
			for (int i = 0; i < requestLines.length; i++ ) {
			    RequestLineParser rlp = 
				  new RequestLineParser(requestLines[i]);
			    RequestLine rl = rlp.parse();
			    System.out.println("encoded = " + rl.encode());
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
