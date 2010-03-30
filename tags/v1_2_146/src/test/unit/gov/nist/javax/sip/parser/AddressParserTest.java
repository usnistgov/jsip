/*
* Conditions Of Use
*
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), and others.
* This software is has been contributed to the public domain.
* As a result, a formal license is not needed to use the software.
*
* This software is provided "AS IS."
* NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
*
*
*/
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
                AddressImpl addr = addressParser.address(true);
                assertEquals(addr, new AddressParser(addr.encode()).address(true));
            }
        } catch (ParseException ex) {
            fail(this.getClass().getName());
        }

    }



}
