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

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.parser.AddressParser;

import java.text.ParseException;
import java.util.regex.Pattern;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;

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
        // Non regression test for Issue 316 : createAddress can add spurious angle brackets
        try {
            AddressFactory addressFactory = SipFactory.getInstance().createAddressFactory();
            String uriString = "<sip:1004@172.16.0.99;user=phone>";            
            try {
                addressFactory.createURI(uriString);
                fail("uriString should throw a ParseException because the angle brackets are not valid");
            } catch (ParseException e) {} 
            try {
                Address address = addressFactory.createAddress(uriString);
                assertEquals(uriString, address.toString());     
                address.setDisplayName("1004");
                assertEquals("\"1004\" " + uriString, address.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                fail(this.getClass().getName());
            }  
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
            fail(this.getClass().getName());
        }

    }
}
