package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Parser for Server header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.6 $ $Date: 2004-01-30 17:10:47 $
 * @version  JAIN-SIP-1.1
 *
 * @author Olivier Deruelle <deruelle@nist.gov>  <br/>
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class ServerParser extends HeaderParser {

	/**
	 * Creates a new instance of ServerParser
	 * @param server the header to parse
	 */
	public ServerParser(String server) {
		super(server);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected ServerParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String server
	 * @return SIPHeader (Server object)
	 * @throws SIPParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {

		if (debug)
			dbg_enter("ServerParser.parse");
		Server server = new Server();
		try {
			headerName(TokenTypes.SERVER);
			if (this.lexer.lookAhead(0) == '\n')
				throw createParseException("empty header");

			//  mandatory token: product[/product-version] | (comment)
			while (this.lexer.lookAhead(0) != '\n'
				&& this.lexer.lookAhead(0) != '\0') {
				if (this.lexer.lookAhead(0) == '(') {
					String comment = this.lexer.comment();
					server.addProductToken('(' + comment + ')');
				} else {
					String tok;
					int marker = 0;
					try {
						marker = this.lexer.markInputPosition();
						tok = this.lexer.getString('/');
							
						if (tok.charAt(tok.length() - 1) == '\n')
							tok = tok.trim();
						server.addProductToken(tok);
					} catch (ParseException ex) {
						this.lexer.rewindInputPosition(marker);
						tok = this.lexer.getRest().trim();
						server.addProductToken(tok);
						break;
					}
				}
			}

		} finally {
			if (debug)
				dbg_leave("ServerParser.parse");
		}

		return server;
	}

/*
	public static void main(String args[]) throws ParseException {
	String server[] = {
	        "Server: Softphone/Beta1.5 \n",
	        "Server: HomeServer v2\n",
	        "Server: Nist/Beta1 (beta version) \n",
	        "Server: Nist proxy (beta version)\n",
	        "Server: Nist1.0/Beta2 UbiServer/vers.1.0 (new stuff) (Cool) \n",
		"Server: Sip EXpress router (0.8.11 (sparc64/solaris))\n"
	        };
		
	for (int i = 0; i < server.length; i++ ) {
	    ServerParser parser = 
		  new ServerParser(server[i]);
	    Server s= (Server) parser.parse();
	    System.out.println("encoded = " + s.encode());
	}
		
	}
*/

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.5  2004/01/27 13:52:11  mranga
 * Reviewed by:   mranga
 * Fixed server/user-agent parser.
 * suppress sending ack to TU when retransFilter is enabled and ack is retransmitted.
 *
 * Revision 1.4  2004/01/22 13:26:32  sverker
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
