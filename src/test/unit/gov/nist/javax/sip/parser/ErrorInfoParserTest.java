/*
 * Created on Jul 28, 2004
 *
 *The Open SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.*;


public class ErrorInfoParserTest extends ParserTestCase {

	
	public void testParser() {
		String r[] = {
				"Error-Info: <sip:not-in-service-recording@atlanta.com>\n",
				"Error-Info: <http://wwww.example.com/alice/photo.jpg>\n"  };
		super.testParser(ErrorInfoParser.class,r);

	}

}
