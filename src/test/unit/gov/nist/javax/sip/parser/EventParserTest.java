/*
 * Created on Jul 28, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;

/**
 *
 */
public class EventParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
		
	        String r[] = {
	            "Event: presence\n",
	            "Event: foo; param=abcd; id=1234\n",
	            "Event: foo.foo1; param=abcd; id=1234\n"
	        };
	        
	        super.testParser(gov.nist.javax.sip.parser.EventParser.class,r);

	}

}
