package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;

/** Parser for ProxyAuthorization headers.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProxyAuthorizationParser extends ChallengeParser {

        /** Constructor
         * @param proxyAuthorization --  header to parse
         */
        public ProxyAuthorizationParser (String proxyAuthorization) {
		super(proxyAuthorization);
	}

        /** Cosntructor
         * @param Lexer lexer to set
         */
        protected ProxyAuthorizationParser (Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message 
         * @return SIPHeader (ProxyAuthenticate object)
         * @throws ParseException if the message does not respect the spec.
         */
	public SIPHeader parse() throws ParseException {
	       headerName(TokenTypes.PROXY_AUTHORIZATION);
	       ProxyAuthorization proxyAuth = new ProxyAuthorization();
	       super.parse(proxyAuth);
               return proxyAuth;
	}

        /** Test program
        public static void main(String args[]) throws ParseException {
		String paAuth[] = {
     "Proxy-Authorization: Digest realm=\"MCI WorldCom SIP\","+
     "domain=\"sip:ss2.wcom.com\", nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","+
     "opaque=\"\", stale=FALSE, algorithm=MD5\n",
                        
     "Proxy-Authenticate: Digest realm=\"MCI WorldCom SIP\","+ 
	"qop=\"auth\" , nonce-value=\"oli\"\n"
                };
			
		for (int i = 0; i < paAuth.length; i++ ) {
		    ProxyAuthorizationParser pap = 
			  new ProxyAuthorizationParser(paAuth[i]);
		    ProxyAuthorization pa= (ProxyAuthorization) pap.parse();
		    System.out.println("encoded = " + pa.encode());
		}
			
	}
         */
	
       

        
}

