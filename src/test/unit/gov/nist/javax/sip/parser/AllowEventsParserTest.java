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
public class AllowEventsParserTest extends ParserTestCase {

	

	public void testParser() {
		String r[] = { "Allow-Events: pack1.pack2, pack3 , pack4\n",
				"Allow-Events: pack1\n" };
		super.testParser(AllowEventsParser.class,r);
	}

}