/*
 * Created on Jul 25, 2004
 *
 *The Open SLEE project
 */
package test.unit.gov.nist.javax.sip.parser;

import gov.nist.javax.sip.address.*;
import gov.nist.javax.sip.parser.*;
import java.text.ParseException;

/**
 *  
 */
public class AddressParserTest extends ParserTestCase {
	
	public void testParser() {

		String[] addresses = {
				"<sip:user@example.com?Route=%3csip:sip.example.com%3e>",
				"\"M. Ranganathan\"   <sip:mranga@nist.gov>",
				"<sip:+1-650-555-2222@ss1.wcom.com;user=phone>",
				"M. Ranganathan <sip:mranga@nist.gov>" };
		try {
			for (int i = 0; i < addresses.length; i++) {
				AddressParser addressParser = new AddressParser(addresses[i]);
				AddressImpl addr = addressParser.address();
				assertEquals(addr, new AddressParser(addr.encode()).address());
			}
		} catch (ParseException ex) {
			fail(this.getClass().getName());
		}

	}
	
	 

}