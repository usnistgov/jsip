/*
 * Created on Jul 31, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;

/**
 *
 */
public class FromParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
			String from[] = {
			"From: foobar at com<sip:4855@166.34.120.100 >;tag=1024181795\n",
			"From: sip:user@company.com\n",
			"From: sip:caller@university.edu\n",
		        "From: sip:localhost\n",
		        "From: \"A. G. Bell\" <sip:agb@bell-telephone.com> ;tag=a48s\n"
		         };
					
			super.testParser(gov.nist.javax.sip.parser.FromParser.class,from);

	}

}
