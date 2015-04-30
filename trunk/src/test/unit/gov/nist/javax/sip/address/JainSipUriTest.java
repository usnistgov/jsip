package test.unit.gov.nist.javax.sip.address;

import gov.nist.javax.sip.address.SipUri;

import java.text.ParseException;

import javax.sip.address.SipURI;
import javax.sip.address.URI;

/**
 * Tests from RFC3261 ��19.1.4 URI Comparison
 */
public class JainSipUriTest extends junit.framework.TestCase {

    static String[][] equal = {
            // should be escaped
            {"sip:%61lice@atlanta.com;transport=TCP", "sip:alice@AtlanTa.CoM;Transport=tcp"},
            {"sip:carol@chicago.com", "sip:carol@chicago.com;newparam=5"},
            {"sip:carol@chicago.com", "sip:carol@chicago.com;lr"},
            {"sip:carol@chicago.com;security=on", "sip:carol@chicago.com;newparam=5"},
            {"sip:alice@atlanta.com?subject=project%20x&priority=urgent", "sip:alice@atlanta.com?priority=urgent&subject=project%20x"},
            {"sip:carol@chicago.com", "sip:carol@chicago.com;security=on"},
            {"sip:carol@chicago.com;security=on", "sip:carol@chicago.com"},
            {"sip:biloxi.com;transport=tcp;method=REGISTER?to=sip:bob%40biloxi.com","sip:biloxi.com;method=REGISTER;transport=tcp?to=sip:bob%40biloxi.com"}
    };

    static String[][] different = {
            {"sip:alice@atlanta.com", "sip:ALICE@atlanta.com"},
            {"sip:bob@biloxi.com", "sip:bob@biloxi.com:5060"},
            {"sip:carol@chicago.com;newparam=6", "sip:carol@chicago.com;newparam=5"},
            {"sip:carol@chicago.com?Subject=next%20meeting", "sip:carol@chicago.com?Subject=another%20meeting"},
            {"sip:bob@biloxi.com", "sip:bob@biloxi.com;transport=tcp"},
            {"sip:carol@chicago.com", "sip:carol@chicago.com?Subject=next%20meeting"},
            {"sip:carol@chicago.com;security=off", "sip:carol@chicago.com;security=on"}
    };
    
    static String[] goodURIs = {"sip:darth-vader@darkstar.org", "tel:+122222222", "sips:yoda@dark-star.org", "urn:service:sos", 
    	"ftp://ftp.is.co.za/rfc/rfc1808.txt","http://www.ietf.org/rfc/rfc2396.txt","ldap://[2001:db8::7]/c=GB?objectClass?one","mailto:John.Doe@example.com",
    	"news:comp.infosystems.www.servers.unix","tel:+1-816-555-1212","telnet://192.0.2.16:80/","urn:oasis:names:specification:docbook:dtd:xml:4.1.2"};
    
    static String[] badURIs = {"darth-vader@darkstar.org", "tel@:+122222222", "darth-vader@127.0.0.1:5080" };

    private javax.sip.SipFactory sipFactory;

    public void setUp() {
        sipFactory = javax.sip.SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
    }
    
    public void testUserNullabilityInSipUri() throws ParseException {
		// Given a URI with host set and null set as the user
		SipUri uri = new SipUri();
		uri.setUser(null);
		uri.setHost("172.18.3.242");

		// When
		String userAtHost = uri.getUserAtHost();
		String userAtHostPort = uri.getUserAtHostPort();

		// Then
		assertEquals("userAtHost equals host", userAtHost, "172.18.3.242");
		assertEquals("userAtHostPort equals host", userAtHostPort, "172.18.3.242");
	}
    
    public void testHeaderCaseSensitivity() throws Exception {

    	String uri2 = ((SipURI) sipFactory.createAddressFactory().createURI("sip:conf@dev;transport=udp?companyId=1")).getHeader("companyId");
    	assertNotNull(uri2);

    }


    private SipURI sipUri(String uri) throws Exception {
        return (SipURI) uri(uri);
    }

    
    private URI uri(String uri) throws Exception {
        return sipFactory.createAddressFactory().createURI(uri);
    }

    
    public void testEqual() throws Exception {
        for (int i = 0; i < equal.length; i++) {
            SipURI uri1 = sipUri(equal[i][0]);
            SipURI uri2 = sipUri(equal[i][1]);
            assertTrue(uri1 + " is different than " + uri2, uri1.equals(uri2));
            assertTrue(uri2 + " is different than " + uri1, uri2.equals(uri1));
        }
    }

    public void testDifferent() throws Exception {
        for (int i = 0; i < different.length; i++) {
            SipURI uri1 = sipUri(different[i][0]);
            SipURI uri2 = sipUri(different[i][1]);
            assertFalse(uri1 + " is the same as " + uri2, uri1.equals(uri2));
            assertFalse(uri2 + " is the same as " + uri1, uri2.equals(uri1));
        }
    }
    
    public void testGoodURIs() throws Exception {
        for (int i = 0; i < goodURIs.length; i++) {
        	URI uri = uri(goodURIs[i]);
        }
    }
    
    public void testBadURIs() throws Exception {
        for (int i = 0; i < badURIs.length; i++) {
        	try {
        		URI uri = uri(badURIs[i]);
        		fail(uri + " should throw parse exception");
        	} catch (ParseException e) {
        		 // ok
        	}
        }
    }
}
