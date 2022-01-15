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
 *The Open SLEE project
 */
package gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


public class AuthenticationInfoParserTest extends ParserTestCase {



    public void testParser() {
            String r[] = {
                    "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\"\n",
                    "Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\",rspauth=\"hello\"\n",
                    "Authentication-Info: NTLM opaque=\"O\",snum=\"N\",srand=\"*\",realm=\"R\",targetname=\"T\"," +
                            "rspauth=\"hello\"\n",
            };

             super.testParser(AuthenticationInfoParser.class,r);

    }

}
