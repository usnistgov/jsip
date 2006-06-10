/*
 * Created on Jul 25, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;

/**
 *  
 */
public class AllowParserTest extends ParserTestCase {

	

	public void testParser() {

		String r[] = { "Allow: INVITE, ACK, OPTIONS, CANCEL, BYE\n",
				"Allow: INVITE\n" };
		super.testParser(AllowParser.class,r);
		
	}

}