/*
 * Created on Jul 25, 2004
 *
 *The Jain-SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.*;


/** Unit test case for Authorization parser
 * 
 *  @author mranga
 */
public class AuthorizationParserTest extends ParserTestCase {
	

	public void testParser() {

		String auth[] = {
				"Authorization: Digest username=\"UserB\", realm=\"MCI WorldCom SIP\","
						+ " nonce=\"ea9c8e88df84f1cec4341ae6cbe5a359\", opaque=\"\","
						+ " uri=\"sip:ss2.wcom.com\", response=\"dfe56131d1958046689cd83306477ecc\"\n",

				"Authorization: Digest username=\"aprokop\",realm=\"Realm\",nonce=\"MTA1MDMzMjE5ODUzMjUwM2QyMzBhOTJlMTkxYjIxYWY1NDlhYzk4YzNiMGYz\",uri=\"sip:nortelnetworks.com:5060\",response=\"dbfba6c0e9664b45b7d224d2b52a1d01\",algorithm=\"MD5\",cnonce=\"VG05eWRHVnNJRTVsZEhkdmNtdHpNVEExTURNek16WTFOREUyTUE9PQ==\",qop=auth-int,nc=00000001\n"

		};
		super.testParser(AuthorizationParser.class,auth);

	}

}