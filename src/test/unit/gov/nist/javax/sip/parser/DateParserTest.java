/*
 * Created on Jul 28, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


public class DateParserTest extends ParserTestCase {

	
	public void testParser() {

		
		String date[] = {
			"Date: Sun, 07 Jan 2001 19:05:06 GMT\n",
			"Date: Mon, 08 Jan 2001 19:05:06 GMT\n" };
			
		super.testParser(DateParser.class,date);
			
	
	}

}
