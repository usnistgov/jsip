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
public class ContentLengthParserTest extends ParserTestCase {
	
	public void testParser()	{
    
		String content[] = {
			"l: 345\n",
			"Content-Length: 3495\n",
			"Content-Length: 0 \n"
                };
			
		super.testParser(ContentLengthParser.class,content);
			
	}

}
