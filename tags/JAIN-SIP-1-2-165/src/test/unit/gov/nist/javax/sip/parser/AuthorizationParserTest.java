/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
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
