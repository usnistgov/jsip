package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;
import gov.nist.core.*;
import java.text.ParseException;

/** Parser for ProxyAuthenticate headers.
*
*@version  JAIN-SIP-1.1
*
*@author M. Ranganathan <mranga@nist.gov>  <br/>
*
*<a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
*
*/
public class ProxyAuthenticateParser extends ChallengeParser {

        /** Constructor
         * @param String paAuthenticate message to parse
         */
        public ProxyAuthenticateParser(String proxyAuthenticate) {
		super(proxyAuthenticate);
	}

        /** Cosntructor
         * @param Lexer lexer to set
         */
        protected ProxyAuthenticateParser(Lexer lexer) {
		super(lexer);
	}
        
        /** parse the String message 
         * @return SIPHeader (ProxyAuthenticate object)
         * @throws ParseException if the message does not respect the spec.
         */
	public SIPHeader parse() throws ParseException {
	       headerName(TokenTypes.PROXY_AUTHENTICATE);
	       ProxyAuthenticate proxyAuthenticate = new ProxyAuthenticate();
	       super.parse(proxyAuthenticate);
               return proxyAuthenticate;
	}

        /** Test program
        public static void main(String args[]) throws ParseException {
		String paAuth[] = {
     "Proxy-Authenticate: Digest realm=\"MCI WorldCom SIP\","+
     "domain=\"sip:ss2.wcom.com\", nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\","+
     "opaque=\"\", stale=FALSE, algorithm=MD5\n",
                        
     "Proxy-Authenticate: Digest realm=\"MCI WorldCom SIP\","+ 
	"qop=\"auth\" , nonce-value=\"oli\"\n"
                };
			
		for (int i = 0; i < paAuth.length; i++ ) {
		    ProxyAuthenticateParser pap = 
			  new ProxyAuthenticateParser(paAuth[i]);
		    ProxyAuthenticate pa= (ProxyAuthenticate) pap.parse();
		    System.out.println("encoded = " + pa.encode());
		}
			
	}
         */
	
       

        
}

