/*
 * Created on Aug 10, 2004
 *
 *The Open SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.OrganizationParser;

/**
 *
 */
public class OrganizationParserTest extends ParserTestCase {
	String o[] = {
	        "Organization: Boxes by Bob\n"
	        };
	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
		super.testParser(OrganizationParser.class,o);
		

	}

}
