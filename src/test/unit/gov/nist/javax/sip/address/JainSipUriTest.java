package test.unit.gov.nist.javax.sip.address;

import javax.sip.address.SipURI;

/**
 * Tests from RFC3261 §19.1.4 URI Comparison
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

    private javax.sip.SipFactory sipFactory;

    public void setUp() {
        sipFactory = javax.sip.SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
    }

    private SipURI sipUri(String uri) throws Exception {
        return (SipURI) sipFactory.createAddressFactory().createURI(uri);
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
}
