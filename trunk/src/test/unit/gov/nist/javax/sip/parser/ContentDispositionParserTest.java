/*
 * Created on Jul 27, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


/**
 *
 */
public class ContentDispositionParserTest extends ParserTestCase {
	public void testParser() {
	
	    String r[] = {
	        "Content-Disposition: session\n",
	        "Content-Disposition: render;handling=hand;optional=opt \n"
	    };
	    
	    super.testParser(ContentDispositionParser.class,r);
	}

}
