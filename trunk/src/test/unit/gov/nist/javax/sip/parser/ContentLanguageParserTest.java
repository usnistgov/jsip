/*
 * Created on Jul 27, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import  gov.nist.javax.sip.parser.*;

/**
 *  
 */
public class ContentLanguageParserTest extends ParserTestCase {

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {

		String r[] = { "Content-Language: fr \n",
				"Content-Language: fr , he \n" };
		super.testParser(ContentLanguageParser.class,r);

	}
}

