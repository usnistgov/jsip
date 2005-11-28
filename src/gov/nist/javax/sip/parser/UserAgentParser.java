package gov.nist.javax.sip.parser;

import gov.nist.core.Token;
import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Parser for UserAgent header.
 * 
 * @version JAIN-SIP-1.1 $Revision: 1.8 $ $Date: 2005-11-28 18:11:35 $
 * 
 * @author Olivier Deruelle <deruelle@nist.gov> <br/>
 * @author M. Ranganathan <mranga@nist.gov> <br/>
 * 
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public class UserAgentParser extends HeaderParser {

	/**
	 * Constructor
	 * 
	 * @param userAgent -
	 *            UserAgent header to parse
	 */
	public UserAgentParser(String userAgent) {
		super(userAgent);
	}

	/**
	 * Constructor
	 * 
	 * @param lexer -
	 *            the lexer to use.
	 */
	protected UserAgentParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * 
	 * @return SIPHeader (UserAgent object)
	 * @throws SIPParseException
	 *             if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {
		if (debug)
			dbg_enter("UserAgentParser.parse");
		UserAgent userAgent = new UserAgent();
		try {
			headerName(TokenTypes.USER_AGENT);
			if (this.lexer.lookAhead(0) == '\n')
				throw createParseException("empty header");

			/*
			 * BNF User-Agent = "User-Agent" HCOLON server-val *(LWS server-val)
			 * server-val = product / comment product = token [SLASH
			 * product-version] product-version = token
			 */
			while (this.lexer.lookAhead(0) != '\n'
					&& this.lexer.lookAhead(0) != '\0') {

				if (this.lexer.lookAhead(0) == '(') {
					String comment = this.lexer.comment();
					userAgent.addProductToken('(' + comment + ')');
				} else {
					// product = token [SLASHproduct-version]
					// product-version = token
					Token product = null;
					try {
						product = this.lexer.match(TokenTypes.ID);
					} catch (ParseException e) {
						throw createParseException("expected a product");
					}
					StringBuffer productSb = new StringBuffer(product
							.getTokenValue());
					// do we possibily have the optional product-version?
					if (this.lexer.peekNextToken().getTokenType() == TokenTypes.SLASH) {
						// yes
						this.lexer.match(TokenTypes.SLASH);
						// product-version
						Token productVersion = null;
						try {
							productVersion = this.lexer.match(TokenTypes.ID);
						} catch (ParseException e1) {
							throw createParseException("expected product-version");
						}
						productSb.append("/");

						productSb.append(productVersion.getTokenValue());
					}

					userAgent.addProductToken(productSb.toString());
				}
				// LWS
				this.lexer.SPorHT();
			}
		} finally {
			if (debug)
				dbg_leave("UserAgentParser.parse");
		}

		return userAgent;
	}

	
	  /*public static void main(String args[]) throws ParseException { String
	  userAgent[] = { "User-Agent: Softphone/Beta1.5 \n", "User-Agent:Nist/Beta1 (beta version) \n", "User-Agent: Nist UA (beta version)\n",
	  "User-Agent: Nist1.0/Beta2 Ubi/vers.1.0 (very cool) \n" };
	  
	  for (int i = 0; i < userAgent.length; i++ ) { UserAgentParser parser =
	  new UserAgentParser(userAgent[i]); UserAgent ua= (UserAgent)
	  parser.parse(); System.out.println("encoded = " + ua.encode()); }
	   }*/
	 
}
