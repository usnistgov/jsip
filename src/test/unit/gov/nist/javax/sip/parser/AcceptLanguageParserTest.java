package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.*;

public class AcceptLanguageParserTest extends ParserTestCase {
	
	public void testParser() {
		String data[] = { "Accept-Language:  da   \n",
				"Accept-Language:\n", "Accept-Language: da, en-gb;q=0.8\n",
				"Accept-Language: *\n" };
		super.testParser(AcceptLanguageParser.class,data);
	}

}

