package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Parser for Server header.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2004-01-22 13:26:32 $
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
					try {
						tok = this.lexer.getString('/');
						if (tok.charAt(tok.length() - 1) == '\n')
							tok = tok.trim();
						server.addProductToken(tok);
					} catch (ParseException ex) {
						tok = this.lexer.getRest();
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

	/** Test program
	public static void main(String args[]) throws ParseException {
	String server[] = {
	        "Server: Softphone/Beta1.5 \n",
	        "Server: HomeServer v2\n",
	        "Server: Nist/Beta1 (beta version) \n",
	        "Server: Nist proxy (beta version)\n",
	        "Server: Nist1.0/Beta2 UbiServer/vers.1.0 (new stuff) (Cool) \n"
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
 */
