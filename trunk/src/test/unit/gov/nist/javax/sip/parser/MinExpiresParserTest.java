/*
 * Created on Jul 31, 2004
 *
 *The Open SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.MinExpiresParser;

/**
 *
 */
public class MinExpiresParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
		String r[] = {
		        "Min-Expires: 60 \n",
				"MIN-EXpIrEs:     90   \n"
		        };
			
		super.testParser(MinExpiresParser.class,r);

	}

}
