/*
 * Created on Jul 26, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


/**
 *  
 */
public class CallInfoParserTest extends ParserTestCase {

	

	public void testParser() {
		String r[] = {
				"Call-Info: <http://wwww.example.com/alice/photo.jpg> ;purpose=icon,"
						+ "<http://www.example.com/alice/> ;purpose=info\n",
				"Call-Info: <http://wwww.example.com/alice/photo1.jpg>\n" };
		super.testParser(CallInfoParser.class,r);
	}

}