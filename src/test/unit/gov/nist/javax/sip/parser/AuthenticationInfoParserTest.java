/*
 * Created on Jul 25, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


public class AuthenticationInfoParserTest extends ParserTestCase {

	

	public void testParser() {
			String r[] = {
					"Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\"\n",
					"Authentication-Info: nextnonce=\"47364c23432d2e131a5fb210812c\",rspauth=\"hello\"\n" };

			 super.testParser(AuthenticationInfoParser.class,r);

	}

}