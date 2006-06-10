package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.*;
import java.text.ParseException;

/**
 * Parser for ProxyAuthorization headers.
 *
 * @version JAIN-SIP-1.1 $Revision: 1.4 $ $Date: 2005-02-24 16:13:11 $
 *
 * @author M. Ranganathan <mranga@nist.gov>  <br/>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 *
 */
public class ProxyAuthorizationParser extends ChallengeParser {

	/**
	 * Constructor
	 * @param proxyAuthorization --  header to parse
	 */
	public ProxyAuthorizationParser(String proxyAuthorization) {
		super(proxyAuthorization);
	}

	/**
	 * Cosntructor
	 * @param Lexer lexer to set
	 */
	protected ProxyAuthorizationParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message 
	 * @return SIPHeader (ProxyAuthenticate object)
	 * @throws ParseException if the message does not respect the spec.
	 */
	public SIPHeader parse() throws ParseException {
		headerName(TokenTypes.PROXY_AUTHORIZATION);
		ProxyAuthorization proxyAuth = new ProxyAuthorization();
		super.parse(proxyAuth);
		return proxyAuth;
	}

/**

	public static void main(String args[]) throws ParseException {
	String paAuth[] = {
	"Proxy-Authorization: Digest realm=\"MCI WorldCom SIP\","+
	"domain=\"sip:ss2.wcom.com\",nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","+
	"opaque=\"\",stale=FALSE,algorithm=MD5\n",
	                
	"Proxy-Authorization: Digest realm=\"MCI WorldCom SIP\","+ 
	"qop=\"auth\" , nonce-value=\"oli\"\n"
	        };
		
	for (int i = 0; i < paAuth.length; i++ ) {
	    ProxyAuthorizationParser pap = 
		  new ProxyAuthorizationParser(paAuth[i]);
	    ProxyAuthorization pa= (ProxyAuthorization) pap.parse();
	    String encoded =   pa.encode();
	    System.out.println ("original = \n" + paAuth[i]);
	    System.out.println("encoded = \n" + encoded);
	    pap = new ProxyAuthorizationParser(encoded.trim() + "\n");
	    pap.parse();
	}
		
	}
**/

}
/*
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2004/01/22 13:26:31  sverker
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
