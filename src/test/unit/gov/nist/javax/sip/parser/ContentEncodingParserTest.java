/*
 * Created on Jul 27, 2004
 *
 *The JAIN-SIP project.
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.*;

/**
 *  
 */
public class ContentEncodingParserTest extends ParserTestCase {

	
	public void testParser() {
		// TODO Auto-generated method stub

		String r[] = { "Content-Encoding: gzip \n",
				"Content-Encoding: gzip, tar \n" };

		super.testParser(ContentEncodingParser.class, r);
	}
}

