/*
 * Created on Jul 27, 2004
 *
 *The Open SIP project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;

/**
 *
 */
public class ContentTypeParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
	    
			String content[] = {
				"c: text/html; charset=ISO-8859-4\n",
				"Content-Type: text/html; charset=ISO-8859-4\n",
				"Content-Type: application/sdp\n",
	                        "Content-Type: application/sdp; o=we ;l=ek ; i=end \n"
	                };
				
			super.testParser(ContentTypeParser.class,content);
				
	

	}

}
