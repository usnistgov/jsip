/*
 * Created on Jul 27, 2004
 *
 *The JAIN-SIP project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.parser.ContactParser;

/**
 * Test case for contact parser
 * 
 * @author mranga
 */
public class ContactParserTest extends ParserTestCase {

	public void testParser() {

		String contact[] = {
				"Contact:<sip:utente@127.0.0.1:5000;transport=udp>;expires=3600\n",
				"Contact:BigGuy<sip:utente@127.0.0.1:5000>;expires=3600\n",
				"Contact: sip:4855@166.35.224.216:5060\n",
				"Contact: sip:user@host.company.com\n",
				"Contact: Bo Bob Biggs\n"
						+ "< sip:user@example.com?Route=%3Csip:sip.example.com%3E >\n",
				"Contact: Joe Bob Briggs <sip:mranga@nist.gov>\n",
				"Contact: \"Mr. Watson\" <sip:watson@worcester.bell-telephone.com>"
						+ " ; q=0.7; expires=3600,\"Mr. Watson\" <mailto:watson@bell-telephone.com>"
						+ ";q=0.1\n",
				"Contact: LittleGuy <sip:UserB@there.com;user=phone>"
						+ ",<sip:+1-972-555-2222@gw1.wcom.com;user=phone>,tel:+1-972-555-2222"
						+ "\n",
				"Contact:*\n",
				"Contact:BigGuy<sip:utente@127.0.0.1;5000>;Expires=3600\n" };
		super.testParser(ContactParser.class, contact);

	}

}