/*
 * Created on Jul 31, 2004
 *
 *The Open SLEE project
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
			"Max-Forwards: 3495\n",
			"Max-Forwards: 0 \n"
                };
			
		
			
		super.testParser(MaxForwardsParser.class,content);

	}

}
