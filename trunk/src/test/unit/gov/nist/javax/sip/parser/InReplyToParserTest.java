/*
 * Created on Jul 31, 2004
 *
 *The JAIN-SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.InReplyToParser;

/**
 *
 */
public class InReplyToParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
		String p[] = {
		        "In-Reply-To: 70710@saturn.bell-tel.com, 17320@saturn.bell-tel.com\n",
		        "In-Reply-To: 70710 \n"
		        };
			
		super.testParser(InReplyToParser.class,p);
	}

}
