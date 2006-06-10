/*
 * Created on Jul 31, 2004
 *
 *The JAIN-SIP Project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.MaxForwardsParser;

/**
 *
 */
public class MaxForwardsParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
		String content[] = {
			"Max-Forwards: 34\n",
			"Max-Forwards: 0 \n"
                };
			
		
			
		super.testParser(MaxForwardsParser.class,content);

	}

}
