/*
 * Created on Jul 27, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;
import gov.nist.javax.sip.parser.*;


public class CSeqParserTest extends ParserTestCase {

	/* (non-Javadoc)
	 * @see test.unit.gov.nist.javax.sip.parser.ParserTestCase#testParser()
	 */
	public void testParser() {
	
			String cseq[] = {
				"CSeq: 17 INVITE\n",
				"CSeq: 17 ACK\n",
				"CSeq : 18   BYE\n",
	                        "CSeq:1 CANCEL\n",
	                        "CSeq: 3 BYE\n"
	                };
				
			super.testParser(CSeqParser.class,cseq);

	}

}
