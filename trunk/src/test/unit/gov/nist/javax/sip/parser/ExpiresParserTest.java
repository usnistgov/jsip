/*
 * Created on Jul 29, 2004
 *
 *The Open SIP project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;

/**
 *
 */
public class ExpiresParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
		    String expires[] = {
		        "Expires: 1000\n" };
		        
		  super.testParser(ExpiresParser.class,expires);
		        
		}
		
}

