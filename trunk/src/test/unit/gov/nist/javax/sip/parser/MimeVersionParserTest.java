/*
 * Created on Jul 31, 2004
 *
 *The Open SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.MimeVersionParser;

/**
 *
 */
public class MimeVersionParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
		String r[] = {
		        "MIME-Version: 1.0 \n",
				"Mime-version: 2.5 \n"
		        };
			
		super.testParser(MimeVersionParser.class,r);

	}

}
